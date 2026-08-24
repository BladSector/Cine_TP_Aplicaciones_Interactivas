const modules = {
    categorias: {
        label: "Categorias",
        endpoint: "/categorias",
        idPrefix: "CAT",
        fields: [
            { name: "nombre", label: "Nombre", type: "text" }
        ],
        columns: ["id", "nombre"]
    },
    peliculas: {
        label: "Peliculas",
        endpoint: "/peliculas",
        idPrefix: "PEL",
        fields: [
            { name: "titulo", label: "Titulo", type: "text" },
            { name: "duracion", label: "Duracion", type: "number" },
            { name: "categoriaId", label: "Categoria", type: "select", source: "categorias", optionLabel: "nombre" }
        ],
        columns: ["id", "titulo", "duracion", "categoriaId", "categoriaNombre"]
    },
    salas: {
        label: "Salas",
        endpoint: "/salas",
        idPrefix: "SAL",
        fields: [
            { name: "nombre", label: "Nombre", type: "text" },
            { name: "capacidad", label: "Capacidad", type: "number" }
        ],
        columns: ["id", "nombre", "capacidad"]
    },
    butacas: {
        label: "Butacas",
        endpoint: "/butacas",
        idPrefix: "BUT",
        fields: [
            { name: "fila", label: "Fila", type: "text" },
            { name: "numero", label: "Numero", type: "number" },
            { name: "salaId", label: "Sala", type: "select", source: "salas", optionLabel: "nombre" }
        ],
        columns: ["id", "fila", "numero", "estado", "bloqueoHasta", "salaId"],
        actions: [
            { label: "Bloquear", method: "PUT", path: id => `/butacas/${id}/bloquear`, body: () => ({ minutos: readActionNumber("Minutos", 5) }) },
            { label: "Ocupar", method: "PUT", path: id => `/butacas/${id}/ocupar` },
            { label: "Liberar", method: "PUT", path: id => `/butacas/${id}/liberar` },
            { label: "Fuera de servicio", method: "PUT", path: id => `/butacas/${id}/fuera-de-servicio` }
        ]
    },
    funciones: {
        label: "Funciones",
        endpoint: "/funciones",
        idPrefix: "FUN",
        fields: [
            { name: "fecha", label: "Fecha", type: "date" },
            { name: "horario", label: "Horario", type: "time" },
            { name: "peliculaId", label: "Pelicula", type: "select", source: "peliculas", optionLabel: "titulo" },
            { name: "salaId", label: "Sala", type: "select", source: "salas", optionLabel: "nombre" },
            { name: "formato", label: "Formato", type: "select", options: ["DOS_D", "TRES_D"] }
        ],
        columns: ["id", "fecha", "horario", "formato", "peliculaId", "peliculaTitulo", "salaId", "salaNombre"]
    },
    metodosPago: {
        label: "Metodos de pago",
        endpoint: "/metodos-pago",
        idPrefix: "MP",
        fields: [
            { name: "numero", label: "Numero", type: "text", placeholder: "16 digitos" },
            { name: "fechaVencimiento", label: "Vencimiento", type: "month" },
            { name: "nombre", label: "Nombre", type: "text" },
            { name: "apellido", label: "Apellido", type: "text" },
            { name: "cvv", label: "CVV", type: "password", placeholder: "3 digitos" }
        ],
        columns: ["id", "numero", "fechaVencimiento", "nombre", "apellido"]
    },
    espectadores: {
        label: "Espectadores",
        endpoint: "/espectadores",
        idPrefix: "ESP",
        fields: [
            { name: "nombre", label: "Nombre", type: "text" },
            { name: "apellido", label: "Apellido", type: "text" },
            { name: "email", label: "Email", type: "email" },
            { name: "contrasenia", label: "Contrasenia", type: "password" }
        ],
        columns: ["id", "nombre", "apellido", "email", "emailVerificado", "metodoDePagoId", "cantidadEntradas"],
        actions: [
            { label: "Asociar metodo", method: "PUT", path: id => `/espectadores/${id}/metodo-pago/${readActionNumber("ID metodo de pago")}` },
            { label: "Verificar mail", method: "PUT", path: id => `/espectadores/${id}/verificar-mail` }
        ]
    },
    entradas: {
        label: "Entradas",
        endpoint: "/entradas",
        idPrefix: "ENT",
        fields: [
            { name: "precio", label: "Precio", type: "number", step: "0.01" },
            { name: "espectadorId", label: "Espectador", type: "select", source: "espectadores", optionLabel: item => `${item.nombre} ${item.apellido}` },
            { name: "funcionId", label: "Funcion", type: "select", source: "funciones", optionLabel: item => `${item.fecha} ${item.horario}` },
            { name: "butacaId", label: "Butaca", type: "select", source: "butacas", optionLabel: item => `${item.fila}${item.numero} - ${item.estado}` }
        ],
        columns: ["id", "precio", "estado", "horario", "espectadorId", "funcionId", "butacaId", "ticketId"],
        createOnly: true,
        actions: [
            { label: "Pendiente", method: "PUT", path: id => `/entradas/${id}/pendiente-pago` },
            { label: "Pagar", method: "PUT", path: id => `/entradas/${id}/pagar` },
            { label: "Escanear", method: "PUT", path: id => `/entradas/${id}/escanear` },
            { label: "Reembolsar", method: "PUT", path: id => `/entradas/${id}/reembolsar` },
            { label: "Cancelar", method: "PUT", path: id => `/entradas/${id}/cancelar` }
        ]
    },
    tickets: {
        label: "Tickets",
        endpoint: "/tickets",
        idPrefix: "TCK",
        fields: [
            { name: "espectadorId", label: "Espectador", type: "select", source: "espectadores", optionLabel: item => `${item.nombre} ${item.apellido}` }
        ],
        columns: ["id", "espectadorId", "codigoQR", "entradasIds", "itemsConsumoIds", "total"],
        createOnly: true,
        actions: [
            { label: "Agregar entrada", method: "POST", path: id => `/tickets/${id}/entradas/${readActionNumber("ID entrada")}` },
            { label: "Agregar item", method: "POST", path: id => `/tickets/${id}/items/${readActionNumber("ID item")}` }
        ]
    },
    productosConfiteria: {
        label: "Productos confiteria",
        endpoint: "/productos-confiteria",
        idPrefix: "PROD",
        fields: [
            { name: "nombre", label: "Nombre", type: "text" },
            { name: "precio", label: "Precio", type: "number", step: "0.01" },
            { name: "tipo", label: "Tipo", type: "select", options: ["POCHOCLOS", "BEBIDA", "DULCE", "COMBO"] },
            { name: "tamano", label: "Tamano", type: "select", options: ["CHICO", "MEDIANO", "GRANDE", "UNICO"] }
        ],
        columns: ["id", "nombre", "precio", "tipo", "tamano"]
    },
    itemsConsumo: {
        label: "Items consumo",
        endpoint: "/items-consumo",
        idPrefix: "ITEM",
        fields: [
            { name: "productoId", label: "Producto", type: "select", source: "productosConfiteria", optionLabel: "nombre" },
            { name: "cantidad", label: "Cantidad", type: "number" },
            { name: "ticketId", label: "Ticket", type: "select", source: "tickets", optionLabel: item => item.codigoQR, optional: true }
        ],
        columns: ["id", "productoId", "productoNombre", "cantidad", "ticketId", "subtotal"]
    },
    precios: {
        label: "Precios",
        endpoint: "/precios",
        idPrefix: "$",
        special: true,
        fields: [
            { name: "funcionId", label: "Funcion", type: "select", source: "funciones", optionLabel: item => `${item.fecha} ${item.horario}` },
            { name: "espectadorId", label: "Espectador", type: "select", source: "espectadores", optionLabel: item => `${item.nombre} ${item.apellido}`, optional: true },
            { name: "precioBase", label: "Precio base", type: "number", step: "0.01" },
            { name: "cantidadEntradas", label: "Cantidad entradas", type: "number" }
        ],
        columns: ["total"]
    }
};

const state = {
    activeKey: "categorias",
    selectedId: null,
    rows: [],
    cache: {}
};

const nav = document.getElementById("moduleNav");
const title = document.getElementById("moduleTitle");
const formTitle = document.getElementById("formTitle");
const selectedBadge = document.getElementById("selectedBadge");
const form = document.getElementById("entityForm");
const formActions = document.getElementById("formActions");
const extraActions = document.getElementById("extraActions");
const tableHead = document.getElementById("tableHead");
const tableBody = document.getElementById("tableBody");
const output = document.getElementById("responseOutput");
const searchInput = document.getElementById("searchInput");
const connectionStatus = document.getElementById("connectionStatus");

document.getElementById("refreshButton").addEventListener("click", refreshActive);
document.getElementById("clearButton").addEventListener("click", clearSelection);
document.getElementById("copyResponseButton").addEventListener("click", () => navigator.clipboard.writeText(output.textContent));
searchInput.addEventListener("input", renderTable);

init();

async function init() {
    renderNav();
    await refreshAll();
    selectModule("categorias");
}

function renderNav() {
    nav.innerHTML = "";

    Object.entries(modules).forEach(([key, config]) => {
        const button = document.createElement("button");
        button.className = "nav-button";
        button.type = "button";
        button.innerHTML = `<strong>${config.label}</strong><span>${config.idPrefix}</span>`;
        button.addEventListener("click", () => selectModule(key));
        nav.appendChild(button);
    });
}

async function selectModule(key) {
    state.activeKey = key;
    state.selectedId = null;

    [...nav.children].forEach((button, index) => {
        button.classList.toggle("active", Object.keys(modules)[index] === key);
    });

    title.textContent = modules[key].label;
    searchInput.value = "";
    renderForm();
    await refreshActive();
}

async function refreshAll() {
    const keys = Object.keys(modules).filter(key => !modules[key].special);

    for (const key of keys) {
        try {
            state.cache[key] = await request(modules[key].endpoint);
        } catch {
            state.cache[key] = [];
        }
    }
}

async function refreshActive() {
    const config = modules[state.activeKey];

    if (config.special) {
        state.rows = [];
        connectionStatus.textContent = "API lista";
        renderTable();
        renderForm();
        return;
    }

    try {
        state.rows = await request(config.endpoint);
        state.cache[state.activeKey] = state.rows;
        connectionStatus.textContent = "API conectada";
        setOutput(state.rows);
    } catch (error) {
        connectionStatus.textContent = "Sin respuesta";
        setOutput(error, true);
    }

    renderTable();
    renderForm();
}

function renderForm() {
    const config = modules[state.activeKey];
    form.innerHTML = "";
    formActions.innerHTML = "";
    extraActions.innerHTML = "";

    formTitle.textContent = state.selectedId ? "Editar registro" : "Nuevo registro";
    selectedBadge.textContent = state.selectedId ? `ID ${state.selectedId}` : "Sin seleccion";

    config.fields.forEach(field => {
        const wrapper = document.createElement("div");
        wrapper.className = "field";

        const label = document.createElement("label");
        label.htmlFor = field.name;
        label.textContent = field.label;

        const input = createInput(field);
        input.id = field.name;
        input.name = field.name;

        wrapper.append(label, input);
        form.appendChild(wrapper);
    });

    const createButton = document.createElement("button");
    createButton.className = "primary-button";
    createButton.type = "button";
    createButton.textContent = config.special ? "Calcular entrada" : "Guardar";
    createButton.addEventListener("click", config.special ? calculateEntryPrice : createEntity);
    formActions.appendChild(createButton);

    if (config.special) {
        const totalButton = document.createElement("button");
        totalButton.className = "secondary-button";
        totalButton.type = "button";
        totalButton.textContent = "Calcular total";
        totalButton.addEventListener("click", calculateTotalPrice);
        formActions.appendChild(totalButton);
        return;
    }

    const updateButton = document.createElement("button");
    updateButton.className = "secondary-button";
    updateButton.type = "button";
    updateButton.textContent = "Actualizar";
    updateButton.disabled = !state.selectedId || config.createOnly;
    updateButton.addEventListener("click", updateEntity);
    formActions.appendChild(updateButton);

    const deleteButton = document.createElement("button");
    deleteButton.className = "danger-button";
    deleteButton.type = "button";
    deleteButton.textContent = "Eliminar";
    deleteButton.disabled = !state.selectedId;
    deleteButton.addEventListener("click", deleteEntity);
    formActions.appendChild(deleteButton);

    if (config.actions && state.selectedId) {
        config.actions.forEach(action => {
            const button = document.createElement("button");
            button.className = "secondary-button";
            button.type = "button";
            button.textContent = action.label;
            button.addEventListener("click", () => runAction(action));
            extraActions.appendChild(button);
        });
    }
}

function createInput(field) {
    if (field.type === "select") {
        const select = document.createElement("select");
        select.dataset.valueType = "number";

        if (field.optional) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "Sin asignar";
            select.appendChild(option);
        }

        if (field.options) {
            field.options.forEach(value => {
                const option = document.createElement("option");
                option.value = value;
                option.textContent = value;
                select.appendChild(option);
            });
            select.dataset.valueType = "string";
        }

        if (field.source) {
            (state.cache[field.source] || []).forEach(item => {
                const option = document.createElement("option");
                option.value = item.id;
                option.textContent = getOptionLabel(field, item);
                select.appendChild(option);
            });
        }

        return select;
    }

    const input = document.createElement("input");
    input.type = field.type;
    input.step = field.step || "";
    input.placeholder = field.placeholder || "";

    if (field.type === "number") {
        input.dataset.valueType = "number";
    }

    return input;
}

function renderTable() {
    const config = modules[state.activeKey];
    const rows = filterRows(state.rows);

    tableHead.innerHTML = "";
    tableBody.innerHTML = "";

    const headerRow = document.createElement("tr");
    config.columns.forEach(column => {
        const th = document.createElement("th");
        th.textContent = column;
        headerRow.appendChild(th);
    });
    tableHead.appendChild(headerRow);

    if (rows.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.className = "empty-state";
        td.colSpan = Math.max(config.columns.length, 1);
        td.textContent = config.special ? "Sin tabla para este modulo" : "Sin registros";
        tr.appendChild(td);
        tableBody.appendChild(tr);
        return;
    }

    rows.forEach(row => {
        const tr = document.createElement("tr");
        tr.classList.toggle("selected", row.id === state.selectedId);
        tr.addEventListener("click", () => selectRow(row));

        config.columns.forEach(column => {
            const td = document.createElement("td");
            td.textContent = formatValue(row[column]);
            tr.appendChild(td);
        });

        tableBody.appendChild(tr);
    });
}

function filterRows(rows) {
    const query = searchInput.value.trim().toLowerCase();

    if (!query) {
        return rows;
    }

    return rows.filter(row => JSON.stringify(row).toLowerCase().includes(query));
}

function selectRow(row) {
    const config = modules[state.activeKey];
    state.selectedId = row.id;

    config.fields.forEach(field => {
        const input = form.elements[field.name];
        if (!input) {
            return;
        }

        const value = row[field.name];
        input.value = value == null ? "" : value;
    });

    renderForm();
    config.fields.forEach(field => {
        const input = form.elements[field.name];
        if (input && row[field.name] != null) {
            input.value = row[field.name];
        }
    });
    renderTable();
    setOutput(row);
}

function clearSelection() {
    state.selectedId = null;
    form.reset();
    renderForm();
    renderTable();
}

async function createEntity() {
    const config = modules[state.activeKey];
    await sendAndRefresh(config.endpoint, "POST", getPayload());
}

async function updateEntity() {
    const config = modules[state.activeKey];

    if (!state.selectedId) {
        return;
    }

    await sendAndRefresh(`${config.endpoint}/${state.selectedId}`, "PUT", getPayload());
}

async function deleteEntity() {
    const config = modules[state.activeKey];

    if (!state.selectedId) {
        return;
    }

    await sendAndRefresh(`${config.endpoint}/${state.selectedId}`, "DELETE");
    clearSelection();
}

async function runAction(action) {
    if (!state.selectedId) {
        return;
    }

    const body = action.body ? action.body() : undefined;

    if (body === null) {
        return;
    }

    await sendAndRefresh(action.path(state.selectedId), action.method, body);
}

async function calculateEntryPrice() {
    const payload = getPayload();
    const params = new URLSearchParams();
    params.set("funcionId", payload.funcionId);
    params.set("precioBase", payload.precioBase);

    if (payload.espectadorId) {
        params.set("espectadorId", payload.espectadorId);
    }

    const data = await request(`/precios/entrada?${params}`);
    setOutput(data);
}

async function calculateTotalPrice() {
    const payload = getPayload();
    const params = new URLSearchParams();
    params.set("funcionId", payload.funcionId);
    params.set("precioBase", payload.precioBase);
    params.set("cantidadEntradas", payload.cantidadEntradas);

    if (payload.espectadorId) {
        params.set("espectadorId", payload.espectadorId);
    }

    const data = await request(`/precios/total?${params}`);
    setOutput(data);
}

async function sendAndRefresh(url, method, body) {
    try {
        const data = await request(url, { method, body });
        setOutput(data);
        await refreshAll();
        await refreshActive();
    } catch (error) {
        setOutput(error, true);
    }
}

function getPayload() {
    const config = modules[state.activeKey];
    const payload = {};

    config.fields.forEach(field => {
        const input = form.elements[field.name];
        let value = input.value;

        if (field.optional && value === "") {
            value = null;
        } else if (input.dataset.valueType === "number") {
            value = value === "" ? null : Number(value);
        } else if (field.name === "horario" && value && value.length === 5) {
            value = `${value}:00`;
        }

        payload[field.name] = value;
    });

    return payload;
}

async function request(url, options = {}) {
    const config = {
        method: options.method || "GET",
        headers: {}
    };

    if (options.body !== undefined) {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(options.body);
    }

    const response = await fetch(url, config);
    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json") ? await response.json() : await response.text();

    if (!response.ok) {
        throw {
            status: response.status,
            error: data
        };
    }

    return data === "" ? { status: response.status } : data;
}

function readActionNumber(label, defaultValue = "") {
    const value = window.prompt(label, defaultValue);

    if (value === null || value.trim() === "") {
        return null;
    }

    return Number(value);
}

function getOptionLabel(field, item) {
    if (typeof field.optionLabel === "function") {
        return field.optionLabel(item);
    }

    return item[field.optionLabel] || `ID ${item.id}`;
}

function formatValue(value) {
    if (Array.isArray(value)) {
        return value.length === 0 ? "-" : value.join(", ");
    }

    if (value === null || value === undefined || value === "") {
        return "-";
    }

    if (typeof value === "number") {
        return Number.isInteger(value) ? String(value) : value.toFixed(2);
    }

    return String(value);
}

function setOutput(data, isError = false) {
    output.classList.toggle("error-text", isError);
    output.textContent = JSON.stringify(data, null, 2);
}
