const api = {
    categorias: "/categorias",
    peliculas: "/peliculas",
    salas: "/salas",
    butacas: "/butacas",
    funciones: "/funciones",
    metodosPago: "/metodos-pago",
    espectadores: "/espectadores",
    entradas: "/entradas",
    tickets: "/tickets",
    productos: "/productos-confiteria",
    items: "/items-consumo"
};

const state = {
    role: null,
    adminTab: "catalogo",
    spectator: null,
    selectedMovie: null,
    selectedFunction: null,
    selectedSeats: [],
    data: emptyData()
};

const $ = selector => document.querySelector(selector);

let loginView;
let appView;
let spectatorView;
let adminView;
let responseOutput;

document.addEventListener("DOMContentLoaded", initApp);

async function initApp() {
    loginView = $("#loginView");
    appView = $("#appView");
    spectatorView = $("#spectatorView");
    adminView = $("#adminView");
    responseOutput = $("#responseOutput");

    bind("#enterSpectatorButton", "click", enterSelectedSpectator);
    bind("#createSpectatorButton", "click", createAndEnterSpectator);
    bind("#enterAdminButton", "click", enterAdmin);
    bind("#logoutButton", "click", logout);
    bind("#refreshButton", "click", refreshCurrentView);
    bind("#movieSearch", "input", renderMovies);
    bind("#categoryFilter", "change", renderMovies);
    bind("#ticketQuantity", "input", renderCheckout);
    bind("#productTypeFilter", "change", renderConsumption);
    bind("#buyButton", "click", buyTicket);
    bind("#buildRoomMatrixButton", "click", renderRoomMatrixEditor);

    bind("#categoryForm", "submit", createCategory);
    bind("#movieForm", "submit", createMovie);
    bind("#roomForm", "submit", createRoomWithSeats);
    bind("#functionForm", "submit", createFunction);
    bind("#productForm", "submit", createProduct);
    document.querySelectorAll("[data-admin-tab]").forEach(button => {
        button.addEventListener("click", () => setAdminTab(button.dataset.adminTab));
    });

    try {
        await loadAll();
        renderLogin();
        setOutput({ estado: "Frontend listo", ayuda: "Elegí un modo para comenzar." });
    } catch (error) {
        setOutput(error, true);
    }
}

function bind(selector, event, handler) {
    const element = $(selector);

    if (!element) {
        console.warn(`No se encontró el elemento ${selector}`);
        return;
    }

    element.addEventListener(event, handler);
}

function emptyData() {
    return {
        categorias: [],
        peliculas: [],
        salas: [],
        butacas: [],
        funciones: [],
        metodosPago: [],
        espectadores: [],
        entradas: [],
        tickets: [],
        productos: [],
        items: []
    };
}

async function loadAll() {
    const entries = Object.entries(api);

    for (const [key, url] of entries) {
        try {
            state.data[key] = await request(url);
        } catch {
            state.data[key] = [];
        }
    }
}

function renderLogin() {
    const select = $("#spectatorSelect");
    select.innerHTML = "";

    if (state.data.espectadores.length === 0) {
        select.innerHTML = `<option value="">No hay espectadores cargados</option>`;
        return;
    }

    state.data.espectadores.forEach(espectador => {
        const option = document.createElement("option");
        option.value = espectador.id;
        option.textContent = `${espectador.nombre} ${espectador.apellido} - ${espectador.email}`;
        select.appendChild(option);
    });
}

async function enterSelectedSpectator() {
    const id = Number($("#spectatorSelect").value);

    if (!id) {
        setOutput({ error: "Primero creá o seleccioná un espectador." }, true);
        return;
    }

    state.spectator = await request(`${api.espectadores}/${id}`);
    enterRole("spectator");
}

async function createAndEnterSpectator() {
    const payload = {
        nombre: $("#newSpectatorName").value,
        apellido: $("#newSpectatorLastName").value,
        email: $("#newSpectatorEmail").value,
        contrasenia: $("#newSpectatorPassword").value
    };

    try {
        state.spectator = await request(api.espectadores, { method: "POST", body: payload });
        await loadAll();
        enterRole("spectator");
    } catch (error) {
        setOutput(error, true);
    }
}

function enterAdmin() {
    state.adminTab = "catalogo";
    enterRole("admin");
}

async function enterRole(role) {
    state.role = role;
    loginView.classList.add("hidden");
    appView.classList.remove("hidden");
    spectatorView.classList.toggle("hidden", role !== "spectator");
    adminView.classList.toggle("hidden", role !== "admin");

    await loadAll();

    if (role === "spectator") {
        $("#roleLabel").textContent = "Espectador";
        $("#viewTitle").textContent = `${state.spectator.nombre} ${state.spectator.apellido}`;
        resetPurchase();
        renderSpectatorView();
    } else {
        $("#roleLabel").textContent = "Administrador";
        $("#viewTitle").textContent = "Gestión del cine";
        renderAdminView();
    }
}

function logout() {
    state.role = null;
    state.spectator = null;
    resetPurchase();
    appView.classList.add("hidden");
    loginView.classList.remove("hidden");
    renderLogin();
}

async function refreshCurrentView() {
    await loadAll();

    if (state.spectator) {
        state.spectator = await request(`${api.espectadores}/${state.spectator.id}`);
    }

    if (state.role === "spectator") {
        renderSpectatorView();
    } else if (state.role === "admin") {
        renderAdminView();
    } else {
        renderLogin();
    }
}

function resetPurchase() {
    state.selectedMovie = null;
    state.selectedFunction = null;
    state.selectedSeats = [];
}

function renderSpectatorView() {
    renderCategoryFilter();
    renderMovies();
    renderCheckout();
}

function renderCategoryFilter() {
    const select = $("#categoryFilter");
    const selected = select.value;
    select.innerHTML = `<option value="">Todas</option>`;

    state.data.categorias.forEach(categoria => {
        const option = document.createElement("option");
        option.value = categoria.id;
        option.textContent = categoria.nombre;
        select.appendChild(option);
    });

    select.value = selected;
}

function renderMovies() {
    const container = $("#movieList");
    const query = $("#movieSearch").value.trim().toLowerCase();
    const categoryId = Number($("#categoryFilter").value);

    const movies = state.data.peliculas.filter(pelicula => {
        const matchesName = !query || pelicula.titulo.toLowerCase().includes(query);
        const matchesCategory = !categoryId || pelicula.categoriaId === categoryId;
        return matchesName && matchesCategory;
    });

    container.innerHTML = "";

    if (movies.length === 0) {
        container.innerHTML = `<div class="info-box">No hay películas para mostrar.</div>`;
        return;
    }

    movies.forEach(pelicula => {
        const functions = state.data.funciones.filter(funcion => funcion.peliculaId === pelicula.id);
        const formatos = [...new Set(functions.map(funcion => formatFunction(funcion.formato)))];
        const idiomas = [...new Set(functions.map(funcion => formatLanguage(funcion.idioma)))];
        const card = document.createElement("article");
        card.className = "movie-card movie-detail";
        card.classList.toggle("selected", state.selectedMovie?.id === pelicula.id);
        const poster = pelicula.portadaUrl
            ? `<img class="movie-poster" src="${pelicula.portadaUrl}" alt="Portada de ${escapeHtml(pelicula.titulo)}">`
            : `<div class="movie-poster placeholder-poster">Sin portada</div>`;

        const functionButtons = functions.length === 0
            ? `<p class="muted">Sin funciones cargadas.</p>`
            : functions.map(funcion => `
                <button class="function-button ${state.selectedFunction?.id === funcion.id ? "selected" : ""}"
                        type="button"
                        data-function-id="${funcion.id}">
                    ${funcion.fecha} ${shortTime(funcion.horario)} - ${formatFunction(funcion.formato)} - ${formatLanguage(funcion.idioma)} - $${money(funcion.precioEntrada)}
                </button>
              `).join("");

        card.innerHTML = `
            ${poster}
            <div class="movie-info">
                <h3>${escapeHtml(pelicula.titulo)}</h3>
                <div class="movie-facts">
                    <div>
                        <span>Formatos</span>
                        <strong>${formatos.length ? escapeHtml(formatos.join(", ")) : "Sin funciones"}</strong>
                    </div>
                    <div>
                        <span>Duración</span>
                        <strong>${formatDuration(pelicula.duracion)}</strong>
                    </div>
                    <div>
                        <span>Categoría</span>
                        <strong>${escapeHtml(pelicula.categoriaNombre)}</strong>
                    </div>
                    <div>
                        <span>Idiomas</span>
                        <strong>${idiomas.length ? escapeHtml(idiomas.join(", ")) : "Sin funciones"}</strong>
                    </div>
                </div>
                <h4>Sinopsis</h4>
                <p>${escapeHtml(pelicula.descripcion || "Sin descripción cargada.")}</p>
                <h4>Funciones disponibles</h4>
                <div class="function-list">${functionButtons}</div>
            </div>
        `;

        card.querySelectorAll("[data-function-id]").forEach(button => {
            button.addEventListener("click", () => selectFunction(Number(button.dataset.functionId)));
        });

        container.appendChild(card);
    });
}

function selectFunction(id) {
    state.selectedFunction = state.data.funciones.find(funcion => funcion.id === id);
    state.selectedMovie = state.data.peliculas.find(pelicula => pelicula.id === state.selectedFunction.peliculaId);
    state.selectedSeats = [];
    renderMovies();
    renderCheckout();
}

function renderCheckout() {
    state.selectedSeats = state.selectedSeats.slice(0, selectedQuantity());
    renderPurchaseSteps();
    renderSelectionSummary();
    renderSeats();
    renderConsumption();
    renderPayment();
    renderBuyButton();
}

function renderPurchaseSteps() {
    const steps = $("#purchaseSteps");

    if (!steps) {
        return;
    }

    const cantidad = selectedQuantity();
    const firstReady = state.selectedFunction && state.selectedSeats.length === cantidad;
    const secondReady = firstReady;
    const thirdReady = Boolean(state.spectator?.metodosDePago?.length);

    steps.innerHTML = `
        <span class="${firstReady ? "done" : "active"}">1. Película y butacas</span>
        <span class="${secondReady ? "active" : ""}">2. Consumo opcional</span>
        <span class="${thirdReady ? "done" : ""}">3. Pago</span>
    `;
}

function renderSelectionSummary() {
    const summary = $("#selectionSummary");

    if (!state.selectedFunction || !state.selectedMovie) {
        summary.innerHTML = `
            <h3>Datos de la compra</h3>
            <p>Elegí una función para comenzar.</p>
        `;
        return;
    }

    const cantidad = selectedQuantity();
    const butacas = state.selectedSeats.map(butaca => `${butaca.fila}${butaca.numero}`).join(", ");

    summary.innerHTML = `
        <h3>Datos de la compra</h3>
        <p><strong>Fecha y hora de la función:</strong><br>${state.selectedFunction.fecha}, ${shortTime(state.selectedFunction.horario)}.</p>
        <p><strong>Película:</strong><br>${escapeHtml(state.selectedMovie.titulo)}.</p>
        <p><strong>Sala:</strong><br>${escapeHtml(state.selectedFunction.salaNombre)}.</p>
        <p><strong>Formato e idioma:</strong><br>${formatFunction(state.selectedFunction.formato)} - ${formatLanguage(state.selectedFunction.idioma)}.</p>
        <p><strong>Butacas:</strong><br>${butacas ? escapeHtml(butacas) : `Elegí ${cantidad} butaca${cantidad === 1 ? "" : "s"}.`}</p>
        <div class="summary-total">
            <span>${cantidad} entrada${cantidad === 1 ? "" : "s"}</span>
            <strong>$${money(state.selectedFunction.precioEntrada * cantidad)}</strong>
        </div>
    `;
}

function renderSeats() {
    const seatList = $("#seatList");
    seatList.innerHTML = "";

    if (!state.selectedFunction) {
        return;
    }

    const seats = seatsForSelectedRoom();

    if (seats.length === 0) {
        seatList.innerHTML = `<div class="info-box">No hay butacas disponibles.</div>`;
        return;
    }

    const seatMap = document.createElement("div");
    seatMap.className = "seat-map";
    seatMap.appendChild(renderScreen());
    seatMap.appendChild(renderSeatMatrix(seats, butaca => {
        const button = document.createElement("button");
        const selected = state.selectedSeats.some(selectedSeat => selectedSeat.id === butaca.id);
        button.className = `seat-button ${selected ? "selected" : ""} ${butaca.estado !== "DISPONIBLE" ? "blocked" : ""}`;
        button.type = "button";
        button.textContent = `${butaca.fila}${butaca.numero}`;
        button.disabled = butaca.estado !== "DISPONIBLE";
        button.addEventListener("click", () => {
            toggleSeatSelection(butaca);
            renderCheckout();
        });
        return button;
    }));
    seatMap.appendChild(renderSeatLegend());
    seatList.appendChild(seatMap);
}

function selectedQuantity() {
    const quantity = Number($("#ticketQuantity")?.value || 1);
    return Math.max(1, quantity);
}

function selectionIsReady() {
    return Boolean(state.selectedFunction) && state.selectedSeats.length === selectedQuantity();
}

function toggleSeatSelection(butaca) {
    const exists = state.selectedSeats.some(selectedSeat => selectedSeat.id === butaca.id);

    if (exists) {
        state.selectedSeats = state.selectedSeats.filter(selectedSeat => selectedSeat.id !== butaca.id);
        return;
    }

    if (state.selectedSeats.length >= selectedQuantity()) {
        state.selectedSeats.shift();
    }

    state.selectedSeats.push(butaca);
}

function seatsForSelectedRoom() {
    return state.data.butacas
        .filter(butaca => butaca.salaId === state.selectedFunction.salaId)
        .sort((a, b) => `${a.fila}${a.numero}`.localeCompare(`${b.fila}${b.numero}`, "es", { numeric: true }));
}

function renderSeatMatrix(seats, buttonFactory) {
    const matrix = document.createElement("div");
    matrix.className = "seat-matrix";

    const filas = [...new Set(seats.map(butaca => butaca.fila))];
    const maxNumero = Math.max(...seats.map(butaca => butaca.numero));

    filas.forEach(fila => {
        const row = document.createElement("div");
        row.className = "seat-row";

        const rowLabel = document.createElement("span");
        rowLabel.className = "seat-row-label";
        rowLabel.textContent = fila;
        row.appendChild(rowLabel);

        for (let numero = 1; numero <= maxNumero; numero++) {
            const butaca = seats.find(seat => seat.fila === fila && seat.numero === numero);

            if (!butaca) {
                const gap = document.createElement("span");
                gap.className = "seat-gap";
                row.appendChild(gap);
                continue;
            }

            row.appendChild(buttonFactory(butaca));
        }

        matrix.appendChild(row);
    });

    return matrix;
}

function renderScreen() {
    const screen = document.createElement("div");
    screen.className = "screen-shape";
    screen.textContent = "PANTALLA";
    return screen;
}

function renderSeatLegend() {
    const legend = document.createElement("div");
    legend.className = "seat-legend";
    legend.innerHTML = `
        <span><i class="legend-seat available"></i>Disponible</span>
        <span><i class="legend-seat selected"></i>Seleccionada</span>
        <span><i class="legend-seat blocked"></i>No disponible</span>
    `;
    return legend;
}

function renderConsumption() {
    const container = $("#consumptionList");
    const filterBox = $("#productTypeFilterBox");
    const selectedType = $("#productTypeFilter")?.value || "";
    container.innerHTML = "";
    filterBox?.classList.toggle("hidden", state.data.productos.length === 0);

    if (!selectionIsReady()) {
        filterBox?.classList.add("hidden");
        return;
    }

    if (state.data.productos.length === 0) {
        container.innerHTML = `<div class="info-box">No hay productos de confitería cargados.</div>`;
        return;
    }

    const title = document.createElement("h3");
    title.textContent = "Consumo opcional";
    container.appendChild(title);

    const productos = state.data.productos.filter(producto => !selectedType || producto.tipo === selectedType);

    if (productos.length === 0) {
        const empty = document.createElement("div");
        empty.className = "info-box";
        empty.textContent = "No hay productos para esa categoría.";
        container.appendChild(empty);
        return;
    }

    productos.forEach(producto => {
        const row = document.createElement("label");
        row.className = "consumption-row";
        row.innerHTML = `
            <span>${escapeHtml(producto.nombre)} - ${labelProductType(producto.tipo)} - $${money(producto.precio)}</span>
            <input type="number" min="0" value="0" data-product-id="${producto.id}">
        `;
        container.appendChild(row);
    });
}

function renderPayment() {
    const box = $("#paymentBox");

    if (!state.spectator || !selectionIsReady()) {
        box.innerHTML = "";
        return;
    }

    const metodos = state.spectator.metodosDePago || [];
    const opciones = metodos.map(metodo => `
        <option value="${metodo.id}">Tarjeta terminada en ${escapeHtml(metodo.ultimosNumeros)} - ${escapeHtml(metodo.nombre)} ${escapeHtml(metodo.apellido)}</option>
    `).join("");

    box.innerHTML = `
        <h3>Método de pago</h3>
        <label>Tarjeta para pagar
            <select id="paymentMethodSelect">
                ${opciones}
                <option value="new">${metodos.length ? "Agregar otra tarjeta" : "Cargar nueva tarjeta"}</option>
            </select>
        </label>
        <div class="payment-grid">
            <label>Número <input id="payNumber" type="text" placeholder="16 dígitos"></label>
            <label>Vencimiento <input id="payExpiration" type="month"></label>
            <label>Nombre <input id="payName" type="text"></label>
            <label>Apellido <input id="payLastName" type="text"></label>
            <label>CVV <input id="payCvv" type="password" placeholder="3 dígitos"></label>
        </div>
    `;
}

function renderBuyButton() {
    const button = $("#buyButton");

    if (!button) {
        return;
    }

    button.disabled = !selectionIsReady();
    button.textContent = selectionIsReady() && selectedQuantity() > 1 ? "Comprar entradas" : "Comprar entrada";

    if (!selectionIsReady()) {
        button.textContent = "Elegí función y butacas";
    }
}

async function buyTicket() {
    const cantidad = selectedQuantity();

    if (!state.selectedFunction || state.selectedSeats.length !== cantidad) {
        setOutput({ error: `Elegí una función y ${cantidad} butaca${cantidad === 1 ? "" : "s"}.` }, true);
        return;
    }

    try {
        const metodoDePagoId = await ensurePaymentMethod();

        const entradas = [];
        for (const butaca of state.selectedSeats) {
            const entrada = await request(api.entradas, {
                method: "POST",
                body: {
                    precio: state.selectedFunction.precioEntrada,
                    espectadorId: state.spectator.id,
                    funcionId: state.selectedFunction.id,
                    butacaId: butaca.id
                }
            });
            entradas.push(entrada);
        }

        const ticket = await request(api.tickets, {
            method: "POST",
            body: { espectadorId: state.spectator.id, metodoDePagoId }
        });

        for (const entrada of entradas) {
            await request(`${api.tickets}/${ticket.id}/entradas/${entrada.id}`, { method: "POST" });
        }

        const items = [];
        for (const input of document.querySelectorAll("[data-product-id]")) {
            const cantidad = Number(input.value);

            if (cantidad > 0) {
                const item = await request(api.items, {
                    method: "POST",
                    body: {
                        productoId: Number(input.dataset.productId),
                        cantidad,
                        ticketId: ticket.id
                    }
                });
                items.push(item);
            }
        }

        const entradasPagadas = [];
        for (const entrada of entradas) {
            const entradaPagada = await request(`${api.entradas}/${entrada.id}/pagar`, { method: "PUT" });
            entradasPagadas.push(entradaPagada);
        }

        const ticketCompleto = await request(`${api.tickets}/${ticket.id}`);
        await loadAll();
        state.spectator = await request(`${api.espectadores}/${state.spectator.id}`);
        resetPurchase();
        renderSpectatorView();
        setOutput({ ticket: ticketCompleto, entradas: entradasPagadas, items });
    } catch (error) {
        setOutput(error, true);
    }
}

async function ensurePaymentMethod() {
    const selected = $("#paymentMethodSelect")?.value;

    if (selected && selected !== "new") {
        return Number(selected);
    }

    const metodoPago = await request(api.metodosPago, {
        method: "POST",
        body: {
            numero: $("#payNumber").value,
            fechaVencimiento: $("#payExpiration").value,
            nombre: $("#payName").value,
            apellido: $("#payLastName").value,
            cvv: $("#payCvv").value
        }
    });

    state.spectator = await request(`${api.espectadores}/${state.spectator.id}/metodo-pago/${metodoPago.id}`, {
        method: "PUT"
    });

    return metodoPago.id;
}

function renderAdminView() {
    renderAdminTabs();
    renderAdminSummary();
    fillAdminSelects();
    renderAdminData();
}

function setAdminTab(tab) {
    state.adminTab = tab;
    renderAdminTabs();
}

function renderAdminTabs() {
    document.querySelectorAll("[data-admin-tab]").forEach(button => {
        button.classList.toggle("active", button.dataset.adminTab === state.adminTab);
    });

    document.querySelectorAll("[data-admin-panel]").forEach(panel => {
        panel.classList.toggle("active", panel.dataset.adminPanel === state.adminTab);
    });
}

function renderAdminSummary() {
    setText("#categoryCount", `Categorías: ${state.data.categorias.length}`);
    setText("#movieCount", `Películas: ${state.data.peliculas.length}`);
    setText("#roomCount", `Salas: ${state.data.salas.length}`);
    setText("#functionCount", `Funciones: ${state.data.funciones.length}`);
    setText("#ticketCount", `Tickets: ${state.data.tickets.length}`);
}

function fillAdminSelects() {
    fillSelect("#movieForm select[name='categoriaId']", state.data.categorias, item => item.nombre, true);
    fillSelect("#functionForm select[name='peliculaId']", state.data.peliculas, item => item.titulo);
    fillSelect("#functionForm select[name='salaId']", state.data.salas, item => `${item.nombre} (${item.capacidad})`);
}

function fillSelect(selector, items, labelFactory, allowEmpty = false) {
    const select = $(selector);
    select.innerHTML = allowEmpty ? `<option value="">Crear categoría nueva si se completa abajo</option>` : "";

    items.forEach(item => {
        const option = document.createElement("option");
        option.value = item.id;
        option.textContent = labelFactory(item);
        select.appendChild(option);
    });
}

async function createCategory(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await adminPost(api.categorias, { nombre: form.nombre.value });
    form.reset();
}

async function createMovie(event) {
    event.preventDefault();
    const form = event.currentTarget;
    let categoriaId = Number(form.categoriaId.value);
    const portadaUrl = await readImageAsDataUrl(form.portada.files[0]);

    if (form.nuevaCategoria.value.trim()) {
        const categoria = await request(api.categorias, {
            method: "POST",
            body: { nombre: form.nuevaCategoria.value.trim() }
        });
        categoriaId = categoria.id;
    }

    await adminPost(api.peliculas, {
        titulo: form.titulo.value,
        duracion: Number(form.duracion.value),
        descripcion: form.descripcion.value,
        portadaUrl,
        categoriaId
    });
    form.reset();
}

async function createRoomWithSeats(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if ($("#roomMatrixEditor").children.length === 0) {
        renderRoomMatrixEditor();
    }

    const butacas = collectRoomSeats();

    if (butacas.length === 0) {
        setOutput({ error: "La sala debe tener al menos una butaca activa." }, true);
        return;
    }

    await adminPost(`${api.salas}/con-matriz`, {
        nombre: form.nombre.value,
        butacas
    });
    form.reset();
    $("#roomMatrixEditor").innerHTML = "";
}

function renderRoomMatrixEditor() {
    const form = $("#roomForm");
    const editor = $("#roomMatrixEditor");
    const filas = Number(form.filas.value);
    const butacasPorFila = Number(form.butacasPorFila.value);

    editor.innerHTML = "";

    if (filas <= 0 || butacasPorFila <= 0) {
        setOutput({ error: "Indicá filas y butacas por fila para armar la matriz." }, true);
        return;
    }

    const seatMap = document.createElement("div");
    seatMap.className = "seat-map room-map-editor";
    seatMap.appendChild(renderScreen());

    const matrix = document.createElement("div");
    matrix.className = "seat-matrix";

    for (let fila = 0; fila < filas; fila++) {
        const letraFila = String.fromCharCode("A".charCodeAt(0) + fila);
        const row = document.createElement("div");
        row.className = "seat-row";

        const rowLabel = document.createElement("span");
        rowLabel.className = "seat-row-label";
        rowLabel.textContent = letraFila;
        row.appendChild(rowLabel);

        for (let numero = 1; numero <= butacasPorFila; numero++) {
            const button = document.createElement("button");
            button.className = "seat-button matrix-seat active";
            button.type = "button";
            button.textContent = `${letraFila}${numero}`;
            button.dataset.fila = letraFila;
            button.dataset.numero = String(numero);
            button.addEventListener("click", () => {
                button.classList.toggle("active");
                button.classList.toggle("removed");
                button.textContent = button.classList.contains("active") ? `${letraFila}${numero}` : "";
                updateRoomSeatCounter();
            });
            row.appendChild(button);
        }

        matrix.appendChild(row);
    }

    seatMap.appendChild(matrix);
    seatMap.appendChild(renderRoomEditorHelp());
    editor.appendChild(seatMap);
    updateRoomSeatCounter();
}

function collectRoomSeats() {
    return [...document.querySelectorAll("#roomMatrixEditor .matrix-seat.active")].map(button => ({
        fila: button.dataset.fila,
        numero: Number(button.dataset.numero)
    }));
}

function renderRoomEditorHelp() {
    const help = document.createElement("div");
    help.className = "room-editor-help";
    help.innerHTML = `
        <strong id="roomSeatCounter">0 butacas activas</strong>
        <span>Tocá una butaca para quitarla. Volvé a tocarla para agregarla otra vez.</span>
    `;
    return help;
}

function updateRoomSeatCounter() {
    const counter = $("#roomSeatCounter");

    if (!counter) {
        return;
    }

    const activeSeats = collectRoomSeats().length;
    counter.textContent = `${activeSeats} butaca${activeSeats === 1 ? "" : "s"} activa${activeSeats === 1 ? "" : "s"}`;
}

async function createFunction(event) {
    event.preventDefault();
    const form = event.currentTarget;

    await adminPost(api.funciones, {
        fecha: form.fecha.value,
        horario: normalizeTime(form.horario.value),
        peliculaId: Number(form.peliculaId.value),
        salaId: Number(form.salaId.value),
        formato: form.formato.value,
        idioma: form.idioma.value,
        precioEntrada: Number(form.precioEntrada.value)
    });
    form.reset();
}

async function createProduct(event) {
    event.preventDefault();
    const form = event.currentTarget;

    await adminPost(api.productos, {
        nombre: form.nombre.value,
        precio: Number(form.precio.value),
        tipo: form.tipo.value,
        tamano: form.tamano.value
    });
    form.reset();
}

async function adminPost(url, body) {
    try {
        const response = await request(url, { method: "POST", body });
        setOutput(response);
        await loadAll();
        renderAdminView();
    } catch (error) {
        setOutput(error, true);
    }
}

function renderAdminData() {
    const allLists = [
        ["Categorías", state.data.categorias, ["id", "nombre"]],
        ["Películas", state.data.peliculas, ["id", "titulo", "descripcion", "categoriaNombre"]],
        ["Salas", state.data.salas, ["id", "nombre", "capacidad"]],
        ["Butacas", state.data.butacas, ["id", "fila", "numero", "estado", "salaId"]],
        ["Funciones", state.data.funciones, ["id", "peliculaTitulo", "fecha", "horario", "formato", "idioma", "precioEntrada"]],
        ["Espectadores", state.data.espectadores, ["id", "nombre", "apellido", "email", "metodosDePago"]],
        ["Métodos de pago", state.data.metodosPago, ["id", "ultimosNumeros", "nombre", "apellido", "espectadorId"]],
        ["Entradas", state.data.entradas, ["id", "precio", "estado", "espectadorId", "funcionId", "butacaId", "ticketId"]],
        ["Tickets", state.data.tickets, ["id", "espectadorId", "metodoDePagoResumen", "total", "entradas", "itemsConsumo"]],
        ["Productos", state.data.productos, ["id", "nombre", "precio", "tipo", "tamano"]],
        ["Consumos", state.data.items, ["id", "productoNombre", "cantidad", "ticketId", "subtotal"]]
    ];

    renderDataGroup("#catalogData", allLists.slice(0, 2));
    renderDataGroup("#roomData", allLists.slice(2, 4));
    renderDataGroup("#functionData", [allLists[4]]);
    renderDataGroup("#spectatorData", allLists.slice(5, 9));
    renderDataGroup("#productData", allLists.slice(9, 11));
    renderDataGroup("#adminData", allLists);
}

function renderDataGroup(selector, lists) {
    const container = $(selector);

    if (!container) {
        return;
    }

    container.innerHTML = lists.map(([title, rows, columns]) => `
        <article class="data-card">
            <h3>${title}</h3>
            <label class="table-search">Buscar <input type="search" data-table-filter placeholder="Filtrar ${title.toLowerCase()}"></label>
            <div class="data-list">${renderTable(rows, columns)}</div>
        </article>
    `).join("");

    container.querySelectorAll("[data-table-filter]").forEach(input => {
        input.addEventListener("input", () => filterTable(input));
    });
}

function renderTable(rows, columns) {
    if (!rows.length) {
        return `<p class="muted">Sin datos</p>`;
    }

    return `
        <table>
            <thead><tr>${columns.map(column => `<th>${labelForColumn(column)}</th>`).join("")}</tr></thead>
            <tbody>
                ${rows.map(row => `
                    <tr>${columns.map(column => `<td>${escapeHtml(formatValue(row[column]))}</td>`).join("")}</tr>
                `).join("")}
            </tbody>
        </table>
    `;
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
        throw { status: response.status, error: data };
    }

    return data === "" ? { status: response.status } : data;
}

function normalizeTime(value) {
    return value && value.length === 5 ? `${value}:00` : value;
}

function shortTime(value) {
    return value ? value.substring(0, 5) : "";
}

function money(value) {
    return Number(value || 0).toFixed(2);
}

function formatFunction(value) {
    if (value === "DOS_D") {
        return "2D";
    }

    if (value === "TRES_D") {
        return "3D";
    }

    return value || "";
}

function formatLanguage(value) {
    if (value === "ESPANIOL") {
        return "Español";
    }

    if (value === "SUBTITULADA") {
        return "Subtitulada";
    }

    return value || "";
}

function formatDuration(minutes) {
    const total = Number(minutes || 0);

    if (total < 60) {
        return `${total} min`;
    }

    const hours = Math.floor(total / 60);
    const remainingMinutes = total % 60;
    return `${hours} h ${remainingMinutes} min`;
}

function labelProductType(value) {
    const labels = {
        POCHOCLOS: "Pochoclos",
        BEBIDA: "Bebidas",
        DULCE: "Dulces",
        COMBO: "Combos"
    };

    return labels[value] || value;
}

function labelForColumn(column) {
    const labels = {
        id: "ID",
        titulo: "Título",
        descripcion: "Descripción",
        categoriaNombre: "Categoría",
        nombre: "Nombre",
        capacidad: "Capacidad",
        fila: "Fila",
        numero: "Número",
        estado: "Estado",
        salaId: "Sala ID",
        peliculaTitulo: "Título",
        fecha: "Fecha",
        horario: "Horario",
        formato: "Formato",
        idioma: "Idioma",
        precioEntrada: "Precio entrada",
        apellido: "Apellido",
        email: "Email",
        metodosDePago: "Métodos de pago",
        metodoDePagoId: "Método de pago ID",
        metodoDePagoResumen: "Método de pago",
        ultimosNumeros: "Últimos números",
        precio: "Precio",
        espectadorId: "Espectador ID",
        funcionId: "Función ID",
        butacaId: "Butaca ID",
        ticketId: "Ticket ID",
        total: "Total",
        entradasIds: "Entradas IDs",
        itemsConsumoIds: "Consumos IDs",
        tipo: "Tipo",
        tamano: "Tamaño",
        productoNombre: "Producto",
        cantidad: "Cantidad",
        subtotal: "Subtotal"
    };

    return labels[column] || column;
}

function formatValue(value) {
    if (Array.isArray(value)) {
        if (!value.length) {
            return "-";
        }

        return value.map(item => {
            if (typeof item !== "object" || item === null) {
                return item;
            }

            if (item.ultimosNumeros) {
                return `Tarjeta terminada en ${item.ultimosNumeros}`;
            }

            if (item.butaca) {
                return `${item.pelicula} - ${item.butaca} - $${money(item.precio)}`;
            }

            if (item.producto) {
                return `${item.producto} x${item.cantidad} - $${money(item.subtotal)}`;
            }

            return `#${item.id}`;
        }).join(", ");
    }

    if (value === "DOS_D" || value === "TRES_D") {
        return formatFunction(value);
    }

    if (value === "ESPANIOL" || value === "SUBTITULADA") {
        return formatLanguage(value);
    }

    if (["POCHOCLOS", "BEBIDA", "DULCE", "COMBO"].includes(value)) {
        return labelProductType(value);
    }

    if (value === null || value === undefined || value === "") {
        return "-";
    }

    if (typeof value === "number") {
        return Number.isInteger(value) ? String(value) : value.toFixed(2);
    }

    return String(value);
}

function readImageAsDataUrl(file) {
    if (!file) {
        return Promise.resolve("");
    }

    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = () => reject(new Error("No se pudo leer la portada."));
        reader.readAsDataURL(file);
    });
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}

function filterTable(input) {
    const card = input.closest(".data-card");
    const rows = card.querySelectorAll("tbody tr");
    const query = input.value.trim().toLowerCase();

    rows.forEach(row => {
        row.hidden = query && !row.textContent.toLowerCase().includes(query);
    });
}

function setText(selector, text) {
    const element = $(selector);

    if (element) {
        element.textContent = text;
    }
}

function setOutput(data, isError = false) {
    responseOutput.classList.toggle("error-text", isError);
    responseOutput.textContent = JSON.stringify(data, null, 2);
}
