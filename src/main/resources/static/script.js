document.addEventListener("DOMContentLoaded", () => {
    loadAllData();

    document.getElementById("inspection-form").addEventListener("submit", (e) => {
        e.preventDefault();
        submitInspection();
    });
});

async function loadAllData() {
    try {
        const [rooms, housekeepers, tasks, inspections, workload, turnaround] = await Promise.all([
            fetchAPI('/api/rooms'),
            fetchAPI('/api/housekeepers'),
            fetchAPI('/api/cleaning-tasks'),
            fetchAPI('/api/inspections'),
            fetchAPI('/api/housekeepers/workload'),
            fetchAPI('/api/rooms/turnaround-time')
        ]);
        
        renderRooms(rooms);
        renderHousekeepers(housekeepers);
        renderTasks(tasks);
        renderInspections(inspections);
        renderWorkload(workload);
        renderTurnaround(turnaround);
        updateSummary(rooms, housekeepers, tasks);
    } catch (e) {
        showAlert("Error loading data: " + e.message, false);
    }
}

async function fetchAPI(url, options = {}) {
    const res = await fetch(url, options);
    if (!res.ok) {
        let msg = "HTTP Error " + res.status;
        try {
            const data = await res.json();
            if (data.message) msg = data.message;
        } catch(e) {}
        throw new Error(msg);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : {};
}

function showAlert(message, isSuccess = true) {
    const container = document.getElementById("alert-container");
    const div = document.createElement("div");
    div.className = `alert ${isSuccess ? 'success' : 'error'}`;
    div.textContent = message;
    container.appendChild(div);
    setTimeout(() => div.remove(), 4000);
}

function renderRooms(rooms) {
    const tbody = document.querySelector("#rooms-table tbody");
    tbody.innerHTML = "";
    rooms.forEach(r => {
        let actions = "";
        if (r.status === "READY") {
            actions += `<button onclick="checkoutRoom(${r.id})">Checkout</button>`;
            actions += `<button onclick="assignRoom(${r.id})">Assign Guest</button>`;
        } else if (r.status === "CLEANING") {
            actions += `<button onclick="openInspectionModal(${r.id})">Inspect</button>`;
        }
        tbody.innerHTML += `
            <tr>
                <td>${r.id}</td>
                <td>${r.roomNumber}</td>
                <td>${r.roomType}</td>
                <td><span class="status ${r.status}">${r.status}</span></td>
                <td>${actions}</td>
            </tr>
        `;
    });
}

function renderHousekeepers(hk) {
    const tbody = document.querySelector("#hk-table tbody");
    tbody.innerHTML = "";
    hk.forEach(h => {
        tbody.innerHTML += `
            <tr>
                <td>${h.id}</td>
                <td>${h.name}</td>
                <td>${h.phone || '-'}</td>
                <td>${h.available ? 'Yes' : 'No'}</td>
            </tr>
        `;
    });
}

function renderTasks(tasks) {
    const tbody = document.querySelector("#tasks-table tbody");
    tbody.innerHTML = "";
    tasks.forEach(t => {
        let actions = "";
        if (t.status === "ASSIGNED") {
            actions += `<button onclick="startCleaning(${t.id})">Start</button>`;
        } else if (t.status === "IN_PROGRESS") {
            actions += `<button onclick="completeCleaning(${t.id})">Complete</button>`;
        }
        const created = t.createdAt ? new Date(t.createdAt).toLocaleString() : '-';
        const started = t.startedAt ? new Date(t.startedAt).toLocaleString() : '-';
        const completed = t.completedAt ? new Date(t.completedAt).toLocaleString() : '-';
        
        tbody.innerHTML += `
            <tr>
                <td>${t.id}</td>
                <td>${t.room ? t.room.id : '-'}</td>
                <td>${t.housekeeper ? t.housekeeper.name : '-'}</td>
                <td><span class="status ${t.status}">${t.status}</span></td>
                <td>${created}</td>
                <td>${started}</td>
                <td>${completed}</td>
                <td>${actions}</td>
            </tr>
        `;
    });
}

function renderInspections(inspections) {
    const tbody = document.querySelector("#inspections-table tbody");
    tbody.innerHTML = "";
    inspections.forEach(i => {
        const time = i.inspectionTime ? new Date(i.inspectionTime).toLocaleString() : '-';
        tbody.innerHTML += `
            <tr>
                <td>${i.id}</td>
                <td>${i.room ? i.room.id : '-'}</td>
                <td>${i.supervisorName}</td>
                <td>${i.passed ? 'Pass' : 'Fail'}</td>
                <td>${i.remarks}</td>
                <td>${time}</td>
            </tr>
        `;
    });
}

function renderWorkload(workload) {
    const tbody = document.querySelector("#workload-table tbody");
    tbody.innerHTML = "";
    workload.forEach(w => {
        tbody.innerHTML += `
            <tr>
                <td>${w.name}</td>
                <td>${w.activeTasks}</td>
                <td>${w.completedTasks}</td>
                <td>${w.available ? 'Yes' : 'No'}</td>
            </tr>
        `;
    });
}

function renderTurnaround(data) {
    let min = 0;
    if(data.averageTurnaroundMinutes) min = data.averageTurnaroundMinutes.toFixed(1);
    document.getElementById("stat-turnaround").textContent = min;
}

function updateSummary(rooms, housekeepers, tasks) {
    document.getElementById("stat-total-rooms").textContent = rooms.length;
    document.getElementById("stat-ready-rooms").textContent = rooms.filter(r=>r.status==="READY").length;
    document.getElementById("stat-cleaning-rooms").textContent = rooms.filter(r=>r.status==="CLEANING").length;
    document.getElementById("stat-dirty-rooms").textContent = rooms.filter(r=>r.status==="DIRTY").length;
    document.getElementById("stat-avail-hk").textContent = housekeepers.filter(h=>h.available).length;
    document.getElementById("stat-active-tasks").textContent = tasks.filter(t=>t.status==="ASSIGNED"||t.status==="IN_PROGRESS").length;
}

// Actions
async function actionHandler(url, method, successMsg) {
    try {
        await fetchAPI(url, { method });
        showAlert(successMsg, true);
        loadAllData();
    } catch (e) {
        showAlert(e.message, false);
    }
}

function checkoutRoom(id) { actionHandler(`/api/rooms/${id}/checkout`, 'POST', 'Room checked out successfully!'); }
function assignRoom(id) { actionHandler(`/api/rooms/${id}/assign`, 'POST', 'Room assigned to guest!'); }
function startCleaning(id) { actionHandler(`/api/cleaning-tasks/${id}/start`, 'PUT', 'Cleaning started!'); }
function completeCleaning(id) { actionHandler(`/api/cleaning-tasks/${id}/complete`, 'PUT', 'Cleaning completed!'); }

// Modal
function openInspectionModal(roomId) {
    document.getElementById("inspect-room-id").textContent = roomId;
    document.getElementById("insp-room-id-input").value = roomId;
    document.getElementById("inspection-modal").style.display = "block";
}
function closeInspectionModal() {
    document.getElementById("inspection-modal").style.display = "none";
}

async function submitInspection() {
    const roomId = document.getElementById("insp-room-id-input").value;
    const body = {
        supervisorName: document.getElementById("insp-supervisor").value,
        passed: document.getElementById("insp-passed").value === "true",
        remarks: document.getElementById("insp-remarks").value
    };
    try {
        await fetchAPI(`/api/rooms/${roomId}/inspection`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });
        showAlert("Inspection submitted!", true);
        closeInspectionModal();
        loadAllData();
    } catch (e) {
        showAlert(e.message, false);
    }
}
