let sensorChart;

document.addEventListener("DOMContentLoaded", () => {
    initializeChart();
    loadServerMetrics();
    setInterval(loadServerMetrics, 1000); // 10초마다 자원 현황 갱신
});

async function initializeChart() {
    const ctx = document.getElementById('sensorChart').getContext('2d');
    sensorChart = new Chart(ctx, {
        type: 'line',
        data: {labels: [], datasets: []},
        options: {
            responsive: true,
            // 이건 dataset 단위에만 있어도 되지만, 전체에도 꺼내 놓으시면 됩니다
            spanGaps: true,
            scales: {
                x: {type: 'category', display: true, title: {display: true, text: '시간'}},
                y: {display: true, title: {display: true, text: '값'}}
            }
        }
    });
}

async function updateChart() {
    const sensorUid = document.getElementById('sensorSelect').value;
    const fromDate = document.getElementById('fromDate').value;
    const toDate = document.getElementById('toDate').value;

    try {
        const res = await fetch(
            `/api/sensors/chart-data?sensorUid=${sensorUid}&from=${fromDate}&to=${toDate}`
        );
        const dto = await res.json();

        sensorChart.data.labels = dto.datasets[0].timestamps;

        sensorChart.data.datasets = dto.datasets.map(ds => ({
            label: ds.label,
            data: ds.data,
            fill: false,
            spanGaps: true
        }));

        sensorChart.update();

    } catch (err) {
        console.error(err);
        alert("센서 데이터를 불러오는 중 오류가 발생했습니다.");
    }
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
        document.getElementById('cpuUsage').innerText = (cpuData.measurements[0].value * 100).toFixed(2) + '%';

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
