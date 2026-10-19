
"use strict";

const API_BASE = "http://localhost:8080/api";

let vehicles = [];
let currentVehicles = [];
let isBackendOnline = false;

const elements = {
    backendStatus: document.getElementById("backendStatus"),
    searchInput: document.getElementById("searchInput"),
    searchButton: document.getElementById("searchButton"),
    binarySearchButton: document.getElementById("binarySearchButton"),
    linearActionButton: document.getElementById("linearActionButton"),
    binaryActionButton: document.getElementById("binaryActionButton"),
    sortButton: document.getElementById("sortButton"),
    resetButton: document.getElementById("resetButton"),
    findNearestButton: document.getElementById("findNearestButton"),
    emergencyType: document.getElementById("emergencyType"),
    searchMessage: document.getElementById("searchMessage"),
    recommendationResult: document.getElementById("recommendationResult"),
    vehicleTableBody: document.getElementById("vehicleTableBody"),
    recordCount: document.getElementById("recordCount"),
    tableFooterCount: document.getElementById("tableFooterCount"),
    ambulanceCount: document.getElementById("ambulanceCount"),
    ambulanceAvailable: document.getElementById("ambulanceAvailable"),
    fireCount: document.getElementById("fireCount"),
    fireAvailable: document.getElementById("fireAvailable"),
    policeCount: document.getElementById("policeCount"),
    policeAvailable: document.getElementById("policeAvailable"),
    totalCount: document.getElementById("totalCount")
};

// Safely create table cells and badges.
function createCell(text, className = "") {
    const cell = document.createElement("td");

    if (className) {
        const span = document.createElement("span");
        span.className = className;
        span.textContent = text;
        cell.appendChild(span);
    } else {
        cell.textContent = text;
    }

    return cell;
}

function getTypeClass(type) {
    const normalized = String(type || "").toLowerCase();

    if (normalized.includes("ambulance")) return "type-badge type-ambulance";
    if (normalized.includes("fire")) return "type-badge type-fire";
    return "type-badge type-police";
}

function getStatusClass(status) {
    return String(status || "").toLowerCase() === "available"
        ? "status-badge status-available"
        : "status-badge status-busy";
}

function getPriorityInfo(priority) {
    const value = Number(priority);

    if (value === 1) return { label: "High", className: "priority-badge priority-high" };
    if (value === 2) return { label: "Medium", className: "priority-badge priority-medium" };
    if (value === 3) return { label: "Low", className: "priority-badge priority-low" };

    return { label: String(priority ?? "—"), className: "priority-badge" };
}

function displayVehicles(list) {
    currentVehicles = [...list];
    elements.vehicleTableBody.replaceChildren();

    elements.recordCount.textContent =
        `${list.length} vehicle${list.length === 1 ? "" : "s"}`;

    elements.tableFooterCount.textContent =
        `Showing ${list.length} of ${vehicles.length} vehicles`;

    if (list.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.className = "empty-state";
        cell.textContent = "No vehicles to display.";

        row.appendChild(cell);
        elements.vehicleTableBody.appendChild(row);
        return;
    }

    list.forEach(vehicle => {
        const row = document.createElement("tr");
        const priority = getPriorityInfo(vehicle.priority);

        row.appendChild(createCell(vehicle.id, "vehicle-id"));
        row.appendChild(createCell(vehicle.type, getTypeClass(vehicle.type)));
        row.appendChild(createCell(vehicle.location));
        row.appendChild(createCell(`${Number(vehicle.distance).toFixed(1)} km`));
        row.appendChild(createCell(priority.label, priority.className));
        row.appendChild(createCell(vehicle.status, getStatusClass(vehicle.status)));

        elements.vehicleTableBody.appendChild(row);
    });
}

function updateSummary() {
    const countType = type =>
        vehicles.filter(vehicle =>
            String(vehicle.type).toLowerCase() === type.toLowerCase()
        );

    const updateGroup = (group, totalElement, availableElement) => {
        const all = countType(group);
        const available = all.filter(vehicle =>
            String(vehicle.status).toLowerCase() === "available"
        ).length;

        totalElement.textContent = all.length;
        availableElement.textContent = `${available} available`;
    };

    updateGroup(
        "Ambulance",
        elements.ambulanceCount,
        elements.ambulanceAvailable
    );

    updateGroup(
        "Fire Truck",
        elements.fireCount,
        elements.fireAvailable
    );

    updateGroup(
        "Police",
        elements.policeCount,
        elements.policeAvailable
    );

    elements.totalCount.textContent = vehicles.length;
}

function setSearchMessage(message, type = "") {
    elements.searchMessage.textContent = message;
    elements.searchMessage.className = "message";

    if (type) {
        elements.searchMessage.classList.add(type);
    }
}

function setBackendStatus(online) {
    isBackendOnline = online;

    const dot = document.querySelector(".live-dot");

    if (online) {
        elements.backendStatus.textContent = "Java Backend Connected";
        dot.classList.remove("offline");
    } else {
        elements.backendStatus.textContent = "Java Backend Offline";
        dot.classList.add("offline");
    }
}

async function fetchJSON(url) {
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}`);
    }

    return response.json();
}

async function loadVehicles() {
    try {
        const data = await fetchJSON(`${API_BASE}/vehicles`);

        if (!Array.isArray(data)) {
            throw new Error("Unexpected vehicle data received.");
        }

        vehicles = data;
        setBackendStatus(true);
        updateSummary();
        displayVehicles(vehicles);
        setSearchMessage("Vehicle records loaded successfully.", "success");
    } catch (error) {
        setBackendStatus(false);
        elements.vehicleTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.className = "empty-state";
        cell.textContent =
            "Cannot connect to Java. Start the backend and click Reset to retry.";

        row.appendChild(cell);
        elements.vehicleTableBody.appendChild(row);

        elements.recordCount.textContent = "Unavailable";
        elements.tableFooterCount.textContent = "No data received";
        setSearchMessage("Backend connection failed. Check the Java terminal.", "error");

        console.error("Could not load vehicles:", error);
    }
}

async function searchVehicle(algorithm) {
    const targetId = elements.searchInput.value.trim();

    if (!targetId) {
        setSearchMessage("Please enter a vehicle ID first.", "error");
        elements.searchInput.focus();
        return;
    }

    const endpoint = algorithm === "binary" ? "binary-search" : "search";

    setSearchMessage(`Searching using ${algorithm === "binary" ? "Binary" : "Linear"} Search...`);

    try {
        const result = await fetchJSON(
            `${API_BASE}/${endpoint}?id=${encodeURIComponent(targetId)}`
        );

        if (result.id) {
            displayVehicles([result]);
            setSearchMessage(
                `${result.id} found using Java ${algorithm === "binary" ? "Binary" : "Linear"} Search.`,
                "success"
            );
        } else {
            displayVehicles([]);
            setSearchMessage(
                result.message || result.error || "Vehicle not found.",
                "error"
            );
        }
    } catch (error) {
        setSearchMessage(
            "Unable to connect to the Java backend. Check whether it is running.",
            "error"
        );
        console.error("Search failed:", error);
    }
}

async function sortVehicles() {
    setSearchMessage("Sorting vehicles by distance using Merge Sort...");

    try {
        const sorted = await fetchJSON(`${API_BASE}/sort`);

        if (!Array.isArray(sorted)) {
            throw new Error("Unexpected sorting response.");
        }

        displayVehicles(sorted);
        setSearchMessage("Vehicles sorted from nearest to farthest using Java Merge Sort.", "success");
    } catch (error) {
        setSearchMessage("Sorting failed. Check the Java backend.", "error");
        console.error("Sort failed:", error);
    }
}

async function findNearestVehicle() {
    const type = elements.emergencyType.value;
    const output = elements.recommendationResult;

    output.className = "recommendation-result";
    output.textContent = "Finding the nearest available vehicle...";

    try {
        const result = await fetchJSON(
            `${API_BASE}/nearest?type=${encodeURIComponent(type)}`
        );

        if (result.id) {
            output.className = "recommendation-result success-result";
            output.textContent =
                `Recommended: ${result.id} | ${result.type} | ${result.location} | ` +
                `${Number(result.distance).toFixed(1)} km | ${result.status}`;

            setSearchMessage(`Nearest available ${type} found: ${result.id}.`, "success");
        } else {
            output.className = "recommendation-result error-result";
            output.textContent =
                result.message || result.error || "No available vehicle found.";

            setSearchMessage("No matching available vehicle was found.", "error");
        }
    } catch (error) {
        output.className = "recommendation-result error-result";
        output.textContent = "Cannot connect to Java backend. Please start the server.";
        setSearchMessage("Nearest-vehicle request failed.", "error");
        console.error("Nearest vehicle request failed:", error);
    }
}

function resetDashboard() {
    elements.searchInput.value = "";
    elements.emergencyType.selectedIndex = 0;
    elements.recommendationResult.className = "recommendation-result";
    elements.recommendationResult.textContent =
        "Select a type to find the nearest available vehicle.";

    loadVehicles();
}

// Wire up existing and new dashboard buttons.
elements.searchButton.addEventListener("click", () => searchVehicle("linear"));
elements.linearActionButton.addEventListener("click", () => searchVehicle("linear"));

elements.binarySearchButton.addEventListener("click", () => searchVehicle("binary"));
elements.binaryActionButton.addEventListener("click", () => searchVehicle("binary"));

elements.sortButton.addEventListener("click", sortVehicles);
elements.resetButton.addEventListener("click", resetDashboard);
elements.findNearestButton.addEventListener("click", findNearestVehicle);

// Press Enter in the search field to run Linear Search.
elements.searchInput.addEventListener("keydown", event => {
    if (event.key === "Enter") {
        searchVehicle("linear");
    }
});

// Load data when the page opens.
loadVehicles();
