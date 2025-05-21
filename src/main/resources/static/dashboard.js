let sensorChart;
// polling 간격 (밀리초)
const STATUS_POLL_INTERVAL = 5000;

document.addEventListener("DOMContentLoaded", () => {
    initializeChart();
    setDefaultPeriod();    // ← 기본 기간 설정
    updateChart();         // ← 기본 기간으로 차트 로드
    loadServerMetrics();
    setInterval(loadServerMetrics, 1000); // 10초마다 자원 현황 갱신
    updateSensorStatuses();
    setInterval(updateSensorStatuses, STATUS_POLL_INTERVAL);
    // 윈도우 리사이즈에도 차트 크기 갱신
    window.addEventListener('resize', () => {
        if (sensorChart) sensorChart.resize();
    });
});

function formatLocalDateTime(d) {
    const Y = d.getFullYear();
    const M = String(d.getMonth() + 1).padStart(2, '0');
    const D = String(d.getDate()).padStart(2, '0');
    const h = String(d.getHours()).padStart(2, '0');
    const m = String(d.getMinutes()).padStart(2, '0');
    return `${Y}-${M}-${D}T${h}:${m}`;
}

function getDefaultFrom() {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return formatLocalDateTime(d);
}

function getDefaultTo() {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() + 1);
    return formatLocalDateTime(d);
}

function setDefaultPeriod() {
    document.getElementById('fromDateTime').value = getDefaultFrom();
    document.getElementById('toDateTime').value = getDefaultTo();
}

async function initializeChart() {
    const ctx = document.getElementById('sensorChart').getContext('2d');
    sensorChart = new Chart(ctx, {
        type: 'line',
        data: {labels: [], datasets: []},
        options: {
            responsive: true,
            maintainAspectRatio: false, //종횡비 고정 해제
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
    const from = document.getElementById('fromDateTime').value;   // datetime-local
    const to = document.getElementById('toDateTime').value;

    // 1) 기간 유효성 체크
    if (from && to && new Date(from) > new Date(to)) {
        alert("시작 일시가 종료 일시보다 이후입니다.\n기간을 다시 설정해주세요.");
        return;
    }

    // URLSearchParams 로 빈 값 자동 생략
    const params = new URLSearchParams();
    if (sensorUid) params.append('sensorUid', sensorUid);
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    try {
        const res = await fetch(`/api/sensors/chart-data?${params.toString()}`);
        const dto = await res.json();

        // (데이터가 없을 땐 빈 배열이 넘어오므로 guard 처리)
        if (dto.datasets.length === 0) {
            sensorChart.data.labels = [];
            sensorChart.data.datasets = [];
        } else {
            sensorChart.data.labels = dto.datasets[0].timestamps;
            sensorChart.data.datasets = dto.datasets.map(ds => ({
                label: ds.label,
                data: ds.data,
                fill: false,
                spanGaps: true
            }));
        }

        sensorChart.update();
    } catch (err) {
        console.error(err);
        alert("센서 데이터를 불러오는 중 오류가 발생했습니다.");
    }
}

function resetFilters() {
    setDefaultPeriod();
    document.getElementById('sensorSelect').value = '';
    updateChart();
}

async function updateSensorStatuses() {
    try {
        const res = await fetch('/api/sensors/statuses');
        const list = await res.json();
        const tbody = document.querySelector('table tbody');

        // 1) 들어온 UID 목록
        const incomingUids = list.map(st => st.sensorUid);

        list.forEach(st => {
            let tr = tbody.querySelector(`tr[data-uid="${st.sensorUid}"]`);

            // ● 행이 없으면 새로 만들기
            if (!tr) {
                tr = document.createElement('tr');
                tr.setAttribute('data-uid', st.sensorUid);
                tr.innerHTML = `
          <td class="sensor-name">${st.sensorName}</td>
          <td><span class="status-badge badge"></span></td>
          <td>
            <span class="status-time"></span>
          </td>
        `;
                tbody.appendChild(tr);
            }

            // ● 공통: 배지/시간/펄스 업데이트
            const badge = tr.querySelector('.status-badge');
            badge.textContent = st.sensorStatus;
            badge.classList.toggle('bg-success', st.sensorStatus === 'ONLINE');
            badge.classList.toggle('bg-danger', st.sensorStatus !== 'ONLINE');

            const timeEl = tr.querySelector('.status-time');
            // ISO → 보기좋은 포맷으로
            timeEl.textContent = st.lastUpdate.replace('T', ' ').slice(0, 19);

            // 펄스 아이콘 처리
            let pulse = tr.querySelector('.pulse');
            if (st.sensorStatus === 'ONLINE') {
                if (!pulse) {
                    pulse = document.createElement('span');
                    pulse.className = 'pulse';
                    pulse.setAttribute('aria-label', '온라인');
                    timeEl.insertAdjacentElement('afterend', pulse);
                }
            } else {
                pulse && pulse.remove();
            }
        });

        // 2) 기존에 있다가, 지금 응답에 없는 센서 행은 제거(Optional)
        tbody.querySelectorAll('tr[data-uid]').forEach(tr => {
            if (!incomingUids.includes(tr.getAttribute('data-uid'))) {
                tr.remove();
            }
        });

    } catch (e) {
        console.error('상태 업데이트 실패', e);
    }
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
