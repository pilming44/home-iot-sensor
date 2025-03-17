let sensorChart;

document.addEventListener("DOMContentLoaded", () => {
    initializeChart();
    loadServerMetrics();
    setInterval(loadServerMetrics, 10000); // 10초마다 자원 현황 갱신
});

async function initializeChart() {
    const ctx = document.getElementById('sensorChart').getContext('2d');
    sensorChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: []
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });
}

async function updateChart() {
    const sensorUid = document.getElementById('sensorSelect').value;
    const fromDate = document.getElementById('fromDate').value;
    const toDate = document.getElementById('toDate').value;

    const response = await fetch(`/api/sensors/data?sensorUid=${sensorUid}&from=${fromDate}&to=${toDate}`);
    const data = await response.json();

    sensorChart.data.labels = data.timestamps;
    sensorChart.data.datasets = data.datasets;
    sensorChart.update();
}

function resetFilters() {
    document.getElementById('sensorSelect').value = '';
    document.getElementById('fromDate').value = '';
    document.getElementById('toDate').value = '';
    updateChart();
}

// 서버 자원 현황 조회 (Actuator 사용)
async function loadServerMetrics() {
    try {
        const cpuRes = await fetch('/actuator/metrics/system.cpu.usage');
        const cpuData = await cpuRes.json();
        document.getElementById('cpuUsage').innerText = (cpuRes.measurements[0].value * 100).toFixed(2) + '%';

        const memRes = await fetch('/actuator/metrics/jvm.memory.used');
        const memUsed = (await memRes.json()).measurements[0].value;
        const memMaxRes = await fetch('/actuator/metrics/jvm.memory.max');
        const memMax = (await memMaxRes.json()).measurements[0].value;

        const memUsagePercent = ((memUsed / memMax) * 100).toFixed(2);
        document.getElementById('memoryUsage').innerText = memUsagePercent + '%';
    } catch (e) {
        console.error(e);
        document.getElementById('cpuUsage').textContent = 'Error';
        document.getElementById('memoryUsage').textContent = 'Error';
    }
}
