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
    spectatorPage: "catalog",
    purchaseStep: "seats",
    spectator: null,
    selectedMovie: null,
    selectedFunction: null,
    selectedSeats: [],
    selectedConsumptions: {},
    selectedProductType: "",
    selectedPublicDate: "",
    selectedMovieDate: "",
    data: emptyData()
};

const $ = selector => document.querySelector(selector);

let loginView;
let appView;
let spectatorView;
let adminView;
let responseOutput;

document.addEventListener("DOMContentLoaded", initApp);
window.addEventListener("pageshow", () => {
    if (!loginView || !loginView.classList.contains("hidden")) {
        resetLoginForms();
        renderLogin();
    }
});

async function initApp() {
    loginView = $("#loginView");
    appView = $("#appView");
    spectatorView = $("#spectatorView");
    adminView = $("#adminView");
    responseOutput = $("#responseOutput");

    setInputDateLimits();

    bind("#enterSpectatorButton", "click", enterSelectedSpectator);
    bind("#createSpectatorButton", "click", createAndEnterSpectator);
    bind("#showRegisterButton", "click", showRegisterForm);
    bind("#forgotPasswordButton", "click", showRecoverPasswordModal);
    bind("#closeMailModalButton", "click", closeMailModal);
    bind("#enterAdminButton", "click", enterAdmin);
    bind("#publicMovieSearch", "input", renderPublicMovies);
    bind("#publicCategoryFilter", "change", renderPublicMovies);
    bind("#publicFormatFilter", "change", renderPublicMovies);
    bind("#homeButton", "click", showCatalogPage);
    bind("#profileButton", "click", showProfilePage);
    bind("#logoutButton", "click", logout);
    bind("#movieSearch", "input", renderMovies);
    bind("#categoryFilter", "change", renderMovies);
    bind("#movieFormatFilter", "change", renderMovies);
    bind("#buyButton", "click", buyTicket);
    bind("#backPurchaseStepButton", "click", goBackPurchaseStep);
    bind("#nextPurchaseStepButton", "click", goNextPurchaseStep);
    bind("#buildRoomMatrixButton", "click", renderRoomMatrixEditor);

    bind("#categoryForm", "submit", createCategory);
    bind("#movieForm", "submit", createMovie);
    bind("#roomForm", "submit", createRoomWithSeats);
    bind("#functionForm", "submit", createFunction);
    bind("#productForm", "submit", createProduct);
    bind("#profileEditForm", "submit", updateProfile);
    bind("#profilePaymentForm", "submit", addPaymentFromProfile);
    bind("#showProfileEditButton", "click", () => toggleProfileForm("#profileEditForm", "#profilePaymentForm"));
    bind("#showProfilePaymentButton", "click", () => toggleProfileForm("#profilePaymentForm", "#profileEditForm"));
    bind("#showPasswordChangeButton", "click", togglePasswordChange);
    bind("#movieEditForm", "submit", updateMovie);
    bind("#roomEditForm", "submit", updateRoom);
    bind("#functionEditForm", "submit", updateFunction);
    bind("#cancelMovieEditButton", "click", () => cancelEdit("#movieEditForm"));
    bind("#cancelRoomEditButton", "click", () => cancelEdit("#roomEditForm"));
    bind("#cancelFunctionEditButton", "click", () => cancelEdit("#functionEditForm"));
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

function setInputDateLimits() {
    document.querySelectorAll("input[type='date']").forEach(input => {
        input.min = todayValue();
    });

    document.querySelectorAll("input[type='month']").forEach(input => {
        input.min = nextMonthValue();
    });
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
    const adminMode = isAdminPath();
    $("#publicCatalogPanel")?.classList.toggle("hidden", adminMode);
    $("#spectatorLoginPanel")?.classList.toggle("hidden", adminMode);
    $("#adminLoginPanel")?.classList.toggle("hidden", !adminMode);
    $("#newAccountBox")?.classList.add("hidden");

    if (adminMode) {
        $("#adminUser").value = "";
        $("#adminPassword").value = "";
        $("#loginView")?.classList.add("admin-login-mode");
    } else {
        $("#loginView")?.classList.remove("admin-login-mode");
        state.selectedPublicDate ||= todayValue();
        renderPublicCategoryFilter();
        renderDateStrip("#publicDateStrip", state.selectedPublicDate, value => {
            state.selectedPublicDate = value;
            renderPublicMovies();
        });
        renderPublicMovies();
    }

    resetLoginForms();
}

function showRegisterForm() {
    const box = $("#newAccountBox");

    if (!box) {
        return;
    }

    box.classList.toggle("hidden");
}

function resetLoginForms() {
    [
        "#loginSpectatorEmail",
        "#loginSpectatorPassword",
        "#newSpectatorName",
        "#newSpectatorLastName",
        "#newSpectatorEmail",
        "#newSpectatorPassword",
        "#newSpectatorPasswordConfirm"
    ].forEach(selector => {
        const input = $(selector);

        if (input) {
            input.value = "";
        }
    });
}

async function enterSelectedSpectator() {
    const email = $("#loginSpectatorEmail").value.trim();
    const contrasenia = $("#loginSpectatorPassword").value;

    if (!email || !contrasenia) {
        showNoticeModal("Datos incompletos", "Ingresá email y contraseña para entrar.", true);
        return;
    }

    try {
        state.spectator = await request(`${api.espectadores}/login`, {
            method: "POST",
            body: { email, contrasenia }
        });
        enterRole("spectator");
    } catch (error) {
        showNoticeModal("No se pudo ingresar", errorMessage(error), true);
        setOutput(error, true);
    }
}

async function createAndEnterSpectator() {
    const contrasenia = $("#newSpectatorPassword").value;
    const contraseniaConfirmacion = $("#newSpectatorPasswordConfirm").value;

    if (contrasenia !== contraseniaConfirmacion) {
        showNoticeModal("Contraseñas distintas", "Las contraseñas no son idénticas.", true);
        return;
    }

    const payload = {
        nombre: $("#newSpectatorName").value,
        apellido: $("#newSpectatorLastName").value,
        email: $("#newSpectatorEmail").value,
        contrasenia,
        contraseniaConfirmacion
    };

    try {
        state.spectator = await request(api.espectadores, { method: "POST", body: payload });
        await loadAll();
        showConfirmationMailModal(state.spectator);
        enterRole("spectator");
    } catch (error) {
        showNoticeModal("No se pudo crear la cuenta", errorMessage(error), true);
        setOutput(error, true);
    }
}

function showRecoverPasswordModal() {
    openMailModal(
        "Recuperar contraseña",
        `
            <div class="fake-mail">
                <p class="eyebrow">Seguridad de la cuenta</p>
                <h3>Restablecer contraseña</h3>
                <form id="recoverEmailModalForm" class="stack-form">
                    <label>Email <input name="email" type="email"></label>
                    <button class="primary-button" type="submit">Continuar</button>
                </form>
            </div>
        `
    );

    $("#recoverEmailModalForm")?.addEventListener("submit", requestPasswordRecovery);
}

async function requestPasswordRecovery(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const email = form.email.value.trim();

    if (!email) {
        showNoticeModal("Email incompleto", "Ingresá el email de la cuenta.", true);
        return;
    }

    try {
        const espectador = await request(`${api.espectadores}/solicitar-recuperacion`, {
            method: "POST",
            body: { email }
        });
        openMailModal(
            "Mail de recuperación",
            `
                <div class="fake-mail">
                    <p class="eyebrow">Cine API</p>
                    <h3>Cambiar contraseña</h3>
                    <p>Recibimos una solicitud para <strong>${escapeHtml(espectador.email)}</strong>.</p>
                    <form id="recoverPasswordModalForm" class="stack-form">
                        <input name="email" type="hidden" value="${escapeHtml(espectador.email)}">
                        <label>Nueva contraseña <input name="nuevaContrasenia" type="password"></label>
                        <label>Repetir nueva contraseña <input name="nuevaContraseniaConfirmacion" type="password"></label>
                        <button class="primary-button" type="submit">Cambiar contraseña</button>
                    </form>
                </div>
            `
        );
        $("#recoverPasswordModalForm")?.addEventListener("submit", recoverPassword);
        setOutput({
            estado: "Mail de recuperación enviado",
            detalle: "Como es ilustrativo, se abre una ventana simulando el mail.",
            espectador
        });
    } catch (error) {
        showNoticeModal("Email no encontrado", errorMessage(error), true);
        setOutput(error, true);
    }
}

async function recoverPassword(event) {
    event.preventDefault();
    const form = event.currentTarget;

    if (form.nuevaContrasenia.value !== form.nuevaContraseniaConfirmacion.value) {
        showNoticeModal("Contraseñas distintas", "Las contraseñas nuevas no son idénticas.", true);
        return;
    }

    try {
        const espectador = await request(`${api.espectadores}/recuperar-contrasenia`, {
            method: "POST",
            body: {
                email: form.email.value,
                nuevaContrasenia: form.nuevaContrasenia.value,
                nuevaContraseniaConfirmacion: form.nuevaContraseniaConfirmacion.value
            }
        });
        await loadAll();
        renderLogin();
        showNoticeModal("Contraseña actualizada", `Ya podés ingresar con la nueva contraseña de ${espectador.email}.`);
        setOutput({ estado: "Contraseña actualizada", espectador });
    } catch (error) {
        showNoticeModal("No se pudo cambiar", errorMessage(error), true);
        setOutput(error, true);
    }
}

function showConfirmationMailModal(espectador) {
    openMailModal(
        "Confirmación de email",
        `
            <div class="fake-mail">
                <p class="eyebrow">Cine API</p>
                <h3>Confirmá tu cuenta</h3>
                <p>Hola ${escapeHtml(espectador.nombre)}, confirmá el email <strong>${escapeHtml(espectador.email)}</strong> para activar tu perfil.</p>
                <button class="primary-button" type="button" id="confirmEmailModalButton">Confirmar email</button>
            </div>
        `
    );

    $("#confirmEmailModalButton")?.addEventListener("click", async () => {
        try {
            state.spectator = await request(`${api.espectadores}/${espectador.id}/verificar-mail`, { method: "PUT" });
            await loadAll();
            renderSpectatorView();
            closeMailModal();
            setOutput({ estado: "Email confirmado", espectador: state.spectator });
        } catch (error) {
            setOutput(error, true);
        }
    });
}

function openMailModal(title, bodyHtml) {
    const modal = $("#mailModal");
    const titleElement = $("#mailModalTitle");
    const body = $("#mailModalBody");

    if (!modal || !titleElement || !body) {
        return;
    }

    titleElement.textContent = title;
    body.innerHTML = bodyHtml;
    modal.classList.remove("hidden");
    bindModalCloseButtons();
}

function closeMailModal() {
    $("#mailModal")?.classList.add("hidden");
}

function bindModalCloseButtons() {
    document.querySelectorAll("[data-close-modal]").forEach(button => {
        button.addEventListener("click", closeMailModal);
    });
}

function showNoticeModal(title, message, isError = false) {
    openMailModal(
        title,
        `
            <div class="fake-mail ${isError ? "notice-error" : "notice-ok"}">
                <p class="eyebrow">${isError ? "Atención" : "Confirmación"}</p>
                <h3>${escapeHtml(title)}</h3>
                <p>${escapeHtml(message)}</p>
                <button class="primary-button" type="button" data-close-modal>Cerrar</button>
            </div>
        `
    );
}

function showConfirmModal(title, message, onConfirm) {
    openMailModal(
        title,
        `
            <div class="fake-mail">
                <p class="eyebrow">Confirmación</p>
                <h3>${escapeHtml(title)}</h3>
                <p>${escapeHtml(message)}</p>
                <div class="button-row">
                    <button class="danger-button" type="button" id="confirmModalAcceptButton">Salir</button>
                    <button class="secondary-button" type="button" data-close-modal>Cancelar</button>
                </div>
            </div>
        `
    );

    $("#confirmModalAcceptButton")?.addEventListener("click", () => {
        closeMailModal();
        onConfirm();
    });
}

function errorMessage(error) {
    if (!error) {
        return "Ocurrió un error inesperado.";
    }

    if (typeof error === "string") {
        return error;
    }

    if (typeof error.error === "string") {
        return error.error;
    }

    if (error.error?.message) {
        return error.error.message;
    }

    if (error.error?.error) {
        return error.error.error;
    }

    return "Ocurrió un error inesperado.";
}

function enterAdmin() {
    if (!isAdminPath()) {
        showNoticeModal("Acceso administrador", "Entrá desde http://localhost:8080/admin para usar el panel administrador.", true);
        return;
    }

    const usuario = $("#adminUser")?.value.trim();
    const contrasenia = $("#adminPassword")?.value;

    if (usuario !== "admin" || contrasenia !== "admin") {
        showNoticeModal("Credenciales incorrectas", "El usuario o la contraseña del administrador no son correctos.", true);
        return;
    }

    state.adminTab = "catalogo";
    enterRole("admin");
}

async function enterRole(role) {
    state.role = role;
    loginView.classList.add("hidden");
    appView.classList.remove("hidden");
    spectatorView.classList.toggle("hidden", role !== "spectator");
    adminView.classList.toggle("hidden", role !== "admin");
    $("#homeButton")?.classList.toggle("hidden", role !== "spectator");
    $("#profileButton")?.classList.toggle("hidden", role !== "spectator");
    $("#responsePanel")?.classList.toggle("hidden", role !== "admin");

    await loadAll();

    if (role === "spectator") {
        $("#roleLabel").textContent = "Espectador";
        $("#viewTitle").textContent = `${state.spectator.nombre} ${state.spectator.apellido}`;
        state.spectatorPage = "catalog";
        resetPurchase();
        renderSpectatorView();
    } else {
        $("#roleLabel").textContent = "Administrador";
        $("#viewTitle").textContent = "Gestión del cine";
        renderAdminView();
    }
}

function logout() {
    if (state.role) {
        showConfirmModal("Cerrar sesión", "¿Querés salir de tu sesión actual?", performLogout);
        return;
    }

    performLogout();
}

function performLogout() {
    state.role = null;
    state.spectator = null;
    state.spectatorPage = "catalog";
    state.purchaseStep = "seats";
    resetPurchase();
    appView.classList.add("hidden");
    loginView.classList.remove("hidden");
    $("#homeButton")?.classList.add("hidden");
    $("#profileButton")?.classList.add("hidden");
    $("#responsePanel")?.classList.add("hidden");
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
    state.selectedConsumptions = {};
    state.selectedProductType = "";
    state.purchaseStep = "seats";
    $("#purchasePanel")?.classList.add("hidden");
    $("#profilePanel")?.classList.remove("hidden");
    clearTicketOutput();
}

function renderSpectatorView() {
    state.selectedMovieDate ||= todayValue();
    renderCategoryFilter();
    renderProfile();
    renderDateStrip("#movieDateStrip", state.selectedMovieDate, value => {
        state.selectedMovieDate = value;
        renderMovies();
    });
    renderMovies();
    renderCheckout();
    renderSpectatorPages();
}

function showCatalogPage() {
    state.spectatorPage = "catalog";
    state.purchaseStep = "seats";
    renderSpectatorView();
}

function showProfilePage() {
    state.spectatorPage = "profile";
    renderSpectatorView();
}

function showPurchasePage() {
    state.spectatorPage = "purchase";
    renderSpectatorView();
}

function renderSpectatorPages() {
    $("#catalogPage")?.classList.toggle("hidden", state.spectatorPage !== "catalog");
    $("#profilePanel")?.classList.toggle("hidden", state.spectatorPage !== "profile");
    $("#purchasePanel")?.classList.toggle("hidden", state.spectatorPage !== "purchase" || !state.selectedFunction);
    $("#movieToolbar")?.classList.toggle("hidden", state.spectatorPage !== "catalog");
    $("#homeButton")?.classList.toggle("hidden", state.role !== "spectator" || state.spectatorPage === "catalog");
}

function renderProfile() {
    const box = $("#profileBox");
    const form = $("#profileEditForm");

    if (!box || !state.spectator) {
        return;
    }

    const metodos = state.spectator.metodosDePago || [];
    const tarjetas = metodos.length
        ? metodos.map(metodo => `
            <li class="payment-method-row">
                <span>
                    Tarjeta terminada en ${escapeHtml(metodo.ultimosNumeros)}
                    - vence ${escapeHtml(metodo.fechaVencimiento)}
                    ${paymentIsExpired(metodo.fechaVencimiento) ? " - vencida" : ""}
                </span>
                <button class="table-action danger-action" type="button" data-delete-payment-id="${metodo.id}">Eliminar</button>
            </li>
        `).join("")
        : `<li>No hay métodos de pago cargados.</li>`;
    const tickets = state.data.tickets.filter(ticket => ticket.espectadorId === state.spectator.id);
    const ticketsHtml = tickets.length
        ? tickets.map(ticket => {
            const entrada = ticket.entradas?.[0];
            const detalle = entrada
                ? `${entrada.pelicula} - ${formatDateShort(entrada.fecha)} ${shortTime(entrada.horario)} - ${entrada.sala}`
                : `Ticket ${ticket.id}`;
            return `
                <button class="ticket-list-button" type="button" data-ticket-id="${ticket.id}">
                    <strong>${escapeHtml(detalle)}</strong>
                    <span>${ticket.entradas?.length || 0} entrada${ticket.entradas?.length === 1 ? "" : "s"} - $${money(ticket.total)}</span>
                </button>
            `;
        }).join("")
        : `<p class="muted">Todavía no tenés entradas compradas.</p>`;

    box.innerHTML = `
        <p><strong>${escapeHtml(state.spectator.nombre)} ${escapeHtml(state.spectator.apellido)}</strong></p>
        <p>${escapeHtml(state.spectator.email)}</p>
        <p>Entradas compradas: ${state.spectator.cantidadEntradas}</p>
        <h3>Métodos de pago</h3>
        <ul>${tarjetas}</ul>
        <h3>Mis entradas</h3>
        <div class="ticket-list">${ticketsHtml}</div>
    `;

    box.querySelectorAll("[data-ticket-id]").forEach(button => {
        button.addEventListener("click", () => {
            const ticket = state.data.tickets.find(item => item.id === Number(button.dataset.ticketId));

            if (ticket) {
                showTicketModal(ticket);
            }
        });
    });

    box.querySelectorAll("[data-delete-payment-id]").forEach(button => {
        button.addEventListener("click", () => deletePaymentMethod(Number(button.dataset.deletePaymentId)));
    });

    if (form) {
        form.nombre.value = state.spectator.nombre || "";
        form.apellido.value = state.spectator.apellido || "";
        form.email.value = state.spectator.email || "";
        form.contraseniaActual.value = "";
        form.nuevaContrasenia.value = "";
        form.nuevaContraseniaConfirmacion.value = "";
    }

    $("#passwordChangeBox")?.classList.add("hidden");
}

function toggleProfileForm(selector, otherSelector) {
    const form = $(selector);
    const otherForm = $(otherSelector);

    if (!form) {
        return;
    }

    form.classList.toggle("hidden");

    if (otherForm && form !== otherForm) {
        otherForm.classList.add("hidden");
    }
}

function togglePasswordChange() {
    $("#passwordChangeBox")?.classList.toggle("hidden");
}

async function deletePaymentMethod(id) {
    try {
        await request(`${api.metodosPago}/${id}`, { method: "DELETE" });
        await loadAll();
        state.spectator = await request(`${api.espectadores}/${state.spectator.id}`);
        renderSpectatorView();
        showNoticeModal("Tarjeta eliminada", "El método de pago se quitó de tu perfil.");
        setOutput({ estado: "Método de pago eliminado", id });
    } catch (error) {
        showNoticeModal("No se pudo eliminar", errorMessage(error), true);
        setOutput(error, true);
    }
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

function renderPublicCategoryFilter() {
    const select = $("#publicCategoryFilter");

    if (!select) {
        return;
    }

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

function renderPublicMovies() {
    const container = $("#publicMovieList");

    if (!container) {
        return;
    }

    const query = $("#publicMovieSearch")?.value.trim().toLowerCase() || "";
    const categoryId = Number($("#publicCategoryFilter")?.value || 0);
    const format = $("#publicFormatFilter")?.value || "";
    const movies = filteredMovies(query, categoryId, format, state.selectedPublicDate);

    container.innerHTML = "";
    if (movies.length === 0) {
        container.innerHTML = `<div class="info-box">No hay películas para mostrar.</div>`;
        return;
    }

    movies.forEach(pelicula => {
        container.appendChild(renderScheduleMovieCard(pelicula, true));
    });
}

function renderMovies() {
    const container = $("#movieList");
    const query = $("#movieSearch").value.trim().toLowerCase();
    const categoryId = Number($("#categoryFilter").value);
    const format = $("#movieFormatFilter")?.value || "";
    const movies = filteredMovies(query, categoryId, format, state.selectedMovieDate);

    container.innerHTML = "";

    if (movies.length === 0) {
        container.innerHTML = `<div class="info-box">No hay películas para mostrar.</div>`;
        return;
    }

    movies.forEach(pelicula => {
        container.appendChild(renderScheduleMovieCard(pelicula, false));
    });
}

function filteredMovies(query, categoryId, format, date) {
    return state.data.peliculas.filter(pelicula => {
        const functions = functionsForMovie(pelicula.id, { format, date });
        const allFunctions = state.data.funciones.filter(funcion => funcion.peliculaId === pelicula.id);
        const matchesSearch = movieMatchesSearch(pelicula, allFunctions, query);
        const matchesCategory = !categoryId || pelicula.categoriaId === categoryId;
        return matchesSearch && matchesCategory && functions.length > 0;
    });
}

function functionsForMovie(peliculaId, filters = {}) {
    return state.data.funciones
        .filter(funcion => funcion.peliculaId === peliculaId)
        .filter(funcion => !filters.date || funcion.fecha === filters.date)
        .filter(funcion => !filters.format || funcion.formato === filters.format)
        .sort((a, b) => `${a.fecha} ${a.horario}`.localeCompare(`${b.fecha} ${b.horario}`));
}

function renderScheduleMovieCard(pelicula, isPublic) {
    const activeDate = isPublic ? state.selectedPublicDate : state.selectedMovieDate;
    const activeFormat = isPublic ? $("#publicFormatFilter")?.value || "" : $("#movieFormatFilter")?.value || "";
    const functions = functionsForMovie(pelicula.id, { date: activeDate, format: activeFormat });
    const groups = groupFunctions(functions);
    const card = document.createElement("article");
    card.className = "schedule-movie-card";
    card.classList.toggle("selected", state.selectedMovie?.id === pelicula.id);

    const poster = pelicula.portadaUrl
        ? `<img class="schedule-poster" src="${pelicula.portadaUrl}" alt="Portada de ${escapeHtml(pelicula.titulo)}">`
        : `<div class="schedule-poster placeholder-poster">Sin portada</div>`;

    card.innerHTML = `
        <div class="poster-wrap">
            <span class="poster-ribbon">Cartelera</span>
            ${poster}
        </div>
        <div class="schedule-movie-info">
            <span class="movie-chip">${escapeHtml(pelicula.categoriaNombre || "Sin categoría")}</span>
            <h3>${escapeHtml(pelicula.titulo)}</h3>
            <div class="public-movie-meta">
                <span>${formatDuration(pelicula.duracion)}</span>
                <span>${escapeHtml(formatDateRange(functions))}</span>
            </div>
            <p>${escapeHtml(shortDescription(pelicula.descripcion))}</p>
            <div class="showtime-groups">
                ${groups.length ? groups.map(group => `
                    <section class="showtime-group">
                        <h4>${escapeHtml(group.label)}</h4>
                        <div class="showtime-list">
                            ${group.functions.map(funcion => `
                                <button class="showtime-button ${state.selectedFunction?.id === funcion.id ? "selected" : ""}"
                                        type="button"
                                        data-function-id="${funcion.id}"
                                        data-public-function="${isPublic}">
                                    <span>${shortTime(funcion.horario)}</span>
                                    <small>${formatDateShort(funcion.fecha)} - $${money(funcion.precioEntrada)}</small>
                                </button>
                            `).join("")}
                        </div>
                    </section>
                `).join("") : `<p class="muted">Sin funciones cargadas.</p>`}
            </div>
        </div>
    `;

    card.querySelectorAll("[data-function-id]").forEach(button => {
        button.addEventListener("click", () => {
            if (isPublic) {
                showNoticeModal("Ingresá para comprar", "Iniciá sesión o creá una cuenta para elegir butacas.", false);
                return;
            }

            selectFunction(Number(button.dataset.functionId));
        });
    });

    return card;
}

function groupFunctions(functions) {
    const groups = new Map();

    functions.forEach(funcion => {
        const key = `${funcion.formato}-${funcion.idioma}`;
        const label = `${formatFunction(funcion.formato)} ${formatLanguage(funcion.idioma)}`;

        if (!groups.has(key)) {
            groups.set(key, { label, functions: [] });
        }

        groups.get(key).functions.push(funcion);
    });

    return [...groups.values()];
}

function selectFunction(id) {
    state.selectedFunction = state.data.funciones.find(funcion => funcion.id === id);
    state.selectedMovie = state.data.peliculas.find(pelicula => pelicula.id === state.selectedFunction.peliculaId);
    state.selectedSeats = [];
    state.purchaseStep = "seats";
    state.spectatorPage = "purchase";
    clearTicketOutput();
    renderMovies();
    renderCheckout();
    renderSpectatorPages();
}

function renderCheckout() {
    if (!state.selectedFunction) {
        return;
    }

    renderPurchaseSteps();
    renderCheckoutStepVisibility();
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

    const firstReady = selectionIsReady();
    const secondReady = state.purchaseStep === "consumption" || state.purchaseStep === "payment";
    const thirdReady = state.purchaseStep === "payment";

    steps.innerHTML = `
        <span class="${state.purchaseStep === "seats" ? "active" : firstReady ? "done" : ""}">1. Butacas</span>
        <span class="${state.purchaseStep === "consumption" ? "active" : secondReady ? "done" : ""}">2. Consumo opcional</span>
        <span class="${thirdReady ? "active" : ""}">3. Método de pago</span>
    `;
}

function renderCheckoutStepVisibility() {
    $("#seatStep")?.classList.toggle("hidden", state.purchaseStep !== "seats");
    $("#consumptionStep")?.classList.toggle("hidden", state.purchaseStep !== "consumption");
    $("#paymentStep")?.classList.toggle("hidden", state.purchaseStep !== "payment");
    $("#backPurchaseStepButton")?.classList.toggle("hidden", state.purchaseStep === "seats");

    const nextButton = $("#nextPurchaseStepButton");

    if (!nextButton) {
        return;
    }

    nextButton.classList.toggle("hidden", state.purchaseStep === "payment");
    nextButton.textContent = state.purchaseStep === "seats" ? "Continuar a consumo" : "Continuar al pago";
    nextButton.disabled = state.purchaseStep === "seats" && !selectionIsReady();
}

function goNextPurchaseStep() {
    if (state.purchaseStep === "seats") {
        if (!selectionIsReady()) {
            setOutput({ error: "Elegí al menos una butaca antes de continuar." }, true);
            return;
        }

        state.purchaseStep = "consumption";
    } else if (state.purchaseStep === "consumption") {
        state.purchaseStep = "payment";
    }

    renderCheckout();
}

function goBackPurchaseStep() {
    if (state.purchaseStep === "payment") {
        state.purchaseStep = "consumption";
    } else if (state.purchaseStep === "consumption") {
        state.purchaseStep = "seats";
    }

    renderCheckout();
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
    const entradasTexto = cantidad === 0
        ? "Sin entradas seleccionadas"
        : `${cantidad} entrada${cantidad === 1 ? "" : "s"}`;

    summary.innerHTML = `
        <h3>Datos de la compra</h3>
        <p><strong>Fecha y hora de la función:</strong><br>${formatDateShort(state.selectedFunction.fecha)}, ${shortTime(state.selectedFunction.horario)}.</p>
        <p><strong>Película:</strong><br>${escapeHtml(state.selectedMovie.titulo)}.</p>
        <p><strong>Sala:</strong><br>${escapeHtml(state.selectedFunction.salaNombre)}.</p>
        <p><strong>Formato e idioma:</strong><br>${formatFunction(state.selectedFunction.formato)} - ${formatLanguage(state.selectedFunction.idioma)}.</p>
        <p><strong>Butacas:</strong><br>${butacas ? escapeHtml(butacas) : "Elegí una o más butacas."}</p>
        <div class="summary-total">
            <span>${entradasTexto}</span>
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
        const unavailable = seatIsUnavailableForSelectedFunction(butaca);
        button.className = `seat-button ${selected ? "selected" : ""} ${unavailable ? "blocked" : ""}`;
        button.type = "button";
        button.textContent = `${butaca.fila}${butaca.numero}`;
        button.disabled = unavailable;
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
    return state.selectedSeats.length;
}

function selectionIsReady() {
    return Boolean(state.selectedFunction) && state.selectedSeats.length > 0;
}

function seatIsUnavailableForSelectedFunction(butaca) {
    if (butaca.estado !== "DISPONIBLE") {
        return true;
    }

    return state.data.entradas.some(entrada =>
        entrada.funcionId === state.selectedFunction.id
        && entrada.butacaId === butaca.id
        && entrada.estado !== "REEMBOLSADA"
        && entrada.estado !== "CANCELADA"
    );
}

function toggleSeatSelection(butaca) {
    const exists = state.selectedSeats.some(selectedSeat => selectedSeat.id === butaca.id);

    if (exists) {
        state.selectedSeats = state.selectedSeats.filter(selectedSeat => selectedSeat.id !== butaca.id);
        return;
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
    const tabs = $("#productTypeTabs");
    const selectedType = state.selectedProductType;
    container.innerHTML = "";
    renderConsumptionTabs();

    if (!selectionIsReady()) {
        tabs?.classList.add("hidden");
        return;
    }

    if (state.data.productos.length === 0) {
        tabs?.classList.add("hidden");
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

    const grid = document.createElement("div");
    grid.className = "consumption-grid";

    productos.forEach(producto => {
        const cantidadActual = Number(state.selectedConsumptions[producto.id] || 0);
        const card = document.createElement("article");
        card.className = `consumption-card ${cantidadActual > 0 ? "selected" : ""}`;
        card.tabIndex = 0;
        card.innerHTML = `
            <div class="product-photo ${productPhotoClass(producto.tipo)}" aria-hidden="true">
                <span>${productPhotoLabel(producto.tipo)}</span>
            </div>
            <div>
                <span class="movie-chip">${labelProductType(producto.tipo)}</span>
                <h4>${escapeHtml(producto.nombre)}</h4>
                <p>${escapeHtml(producto.tamano)} - $${money(producto.precio)}</p>
            </div>
            <div class="quantity-control">
                <button class="table-action" type="button" data-quantity-action="minus" aria-label="Quitar">-</button>
                <strong>${cantidadActual}</strong>
                <button class="table-action" type="button" data-quantity-action="plus" aria-label="Agregar">+</button>
            </div>
        `;

        card.addEventListener("click", () => updateConsumptionQuantity(producto.id, cantidadActual + 1));
        card.addEventListener("keydown", event => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                updateConsumptionQuantity(producto.id, cantidadActual + 1);
            }
        });
        card.querySelectorAll("[data-quantity-action]").forEach(button => {
            button.addEventListener("click", event => {
                event.stopPropagation();
                const nextQuantity = button.dataset.quantityAction === "plus"
                    ? cantidadActual + 1
                    : cantidadActual - 1;
                updateConsumptionQuantity(producto.id, nextQuantity);
            });
        });

        grid.appendChild(card);
    });

    container.appendChild(grid);
}

function renderConsumptionTabs() {
    const tabs = $("#productTypeTabs");

    if (!tabs) {
        return;
    }

    const types = [
        ["", "Todo"],
        ["POCHOCLOS", "Pochoclos"],
        ["BEBIDA", "Bebidas"],
        ["DULCE", "Dulces"],
        ["COMBO", "Combos"]
    ];

    tabs.classList.toggle("hidden", state.data.productos.length === 0);
    tabs.innerHTML = types.map(([value, label]) => `
        <button class="consumption-tab ${state.selectedProductType === value ? "active" : ""}"
                type="button"
                data-product-type="${value}">
            ${label}
        </button>
    `).join("");

    tabs.querySelectorAll("[data-product-type]").forEach(button => {
        button.addEventListener("click", () => {
            state.selectedProductType = button.dataset.productType;
            renderConsumption();
        });
    });
}

function updateConsumptionQuantity(productId, quantity) {
    const normalized = Math.max(0, Number(quantity || 0));

    if (normalized > 0) {
        state.selectedConsumptions[productId] = normalized;
    } else {
        delete state.selectedConsumptions[productId];
    }

    renderConsumption();
}

function productPhotoClass(tipo) {
    return `product-photo-${String(tipo || "combo").toLowerCase()}`;
}

function productPhotoLabel(tipo) {
    const labels = {
        POCHOCLOS: "POPCORN",
        BEBIDA: "DRINK",
        DULCE: "SWEET",
        COMBO: "COMBO"
    };

    return labels[tipo] || "SNACK";
}

function renderPayment() {
    const box = $("#paymentBox");

    if (!state.spectator || !selectionIsReady()) {
        box.innerHTML = "";
        return;
    }

    const metodos = state.spectator.metodosDePago || [];
    const hasValidMethod = metodos.some(metodo => !paymentIsExpired(metodo.fechaVencimiento));
    const opciones = metodos.map(metodo => `
        <option value="${metodo.id}" ${paymentIsExpired(metodo.fechaVencimiento) ? "disabled" : ""}>
            Tarjeta terminada en ${escapeHtml(metodo.ultimosNumeros)}
            - vence ${escapeHtml(metodo.fechaVencimiento)}
            ${paymentIsExpired(metodo.fechaVencimiento) ? " - vencida" : ""}
        </option>
    `).join("");

    box.innerHTML = `
        <h3>Método de pago</h3>
        <label>Tarjeta para pagar
            <select id="paymentMethodSelect">
                ${opciones}
                <option value="new" ${hasValidMethod ? "" : "selected"}>${metodos.length ? "Agregar otra tarjeta" : "Cargar nueva tarjeta"}</option>
            </select>
        </label>
        <div id="newPaymentFields" class="payment-grid ${hasValidMethod ? "hidden" : ""}">
            <label>Número <input id="payNumber" type="text" placeholder="16 dígitos"></label>
            <label>Vencimiento <input id="payExpiration" type="month"></label>
            <label>Nombre <input id="payName" type="text"></label>
            <label>Apellido <input id="payLastName" type="text"></label>
            <label>CVV <input id="payCvv" type="password" placeholder="3 dígitos"></label>
        </div>
    `;

    $("#paymentMethodSelect")?.addEventListener("change", toggleNewPaymentFields);
    toggleNewPaymentFields();
}

function toggleNewPaymentFields() {
    const selected = $("#paymentMethodSelect")?.value;
    $("#newPaymentFields")?.classList.toggle("hidden", selected !== "new");
}

function renderBuyButton() {
    const button = $("#buyButton");

    if (!button) {
        return;
    }

    button.disabled = !selectionIsReady();
    button.textContent = selectionIsReady() && selectedQuantity() > 1 ? "Comprar entradas" : "Comprar entrada";

    if (!selectionIsReady()) {
        button.textContent = "Elegí una o más butacas";
    }
}

async function buyTicket() {
    const cantidad = selectedQuantity();

    if (!state.selectedFunction || cantidad === 0) {
        setOutput({ error: "Elegí una función y al menos una butaca." }, true);
        return;
    }

    setPurchaseLoading(true);

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
        for (const [productoId, cantidad] of Object.entries(state.selectedConsumptions)) {
            const cantidadNumerica = Number(cantidad);

            if (cantidadNumerica > 0) {
                const item = await request(api.items, {
                    method: "POST",
                    body: {
                        productoId: Number(productoId),
                        cantidad: cantidadNumerica,
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

        await loadAll();
        const ticketCompleto = await request(`${api.tickets}/${ticket.id}`);
        await request(`${api.tickets}/${ticket.id}/enviar-mail`, { method: "POST" });
        await loadAll();
        state.spectator = await request(`${api.espectadores}/${state.spectator.id}`);
        const ticketFinal = state.data.tickets.find(item => item.id === ticket.id) || ticketCompleto;
        state.selectedSeats = [];
        state.selectedConsumptions = {};
        state.selectedProductType = "";
        state.selectedFunction = null;
        state.selectedMovie = null;
        state.purchaseStep = "seats";
        state.spectatorPage = "catalog";
        setPurchaseLoading(false);
        renderSpectatorView();
        showTicketModal(ticketFinal);
        setOutput({ ticket: ticketFinal, entradas: entradasPagadas, items });
    } catch (error) {
        setPurchaseLoading(false);
        showNoticeModal("No se pudo completar la compra", errorMessage(error), true);
        setOutput(error, true);
    }
}

function setPurchaseLoading(isLoading) {
    const loading = $("#purchaseLoading");
    const buyButton = $("#buyButton");

    loading?.classList.toggle("hidden", !isLoading);

    if (buyButton) {
        buyButton.disabled = isLoading || !selectionIsReady();
        buyButton.textContent = isLoading ? "Procesando compra..." : selectedQuantity() > 1 ? "Comprar entradas" : "Comprar entrada";
    }
}

async function ensurePaymentMethod() {
    const selected = $("#paymentMethodSelect")?.value;

    if (selected && selected !== "new") {
        const metodo = state.spectator.metodosDePago?.find(item => item.id === Number(selected));

        if (metodo && paymentIsExpired(metodo.fechaVencimiento)) {
            throw { error: "El método de pago seleccionado está vencido." };
        }

        return Number(selected);
    }

    validatePaymentExpiration($("#payExpiration").value);

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

async function updateProfile(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const quiereCambiarContrasenia = form.nuevaContrasenia.value || form.nuevaContraseniaConfirmacion.value;

    if (quiereCambiarContrasenia && form.nuevaContrasenia.value !== form.nuevaContraseniaConfirmacion.value) {
        showNoticeModal("Contraseñas distintas", "Las contraseñas nuevas no son idénticas.", true);
        return;
    }

    if (quiereCambiarContrasenia && !form.contraseniaActual.value) {
        showNoticeModal("Falta contraseña actual", "Ingresá la contraseña actual para cambiarla.", true);
        return;
    }

    try {
        state.spectator = await request(`${api.espectadores}/${state.spectator.id}`, {
            method: "PUT",
            body: {
                nombre: form.nombre.value,
                apellido: form.apellido.value,
                email: form.email.value,
                contraseniaActual: form.contraseniaActual.value,
                nuevaContrasenia: form.nuevaContrasenia.value,
                nuevaContraseniaConfirmacion: form.nuevaContraseniaConfirmacion.value
            }
        });
        await loadAll();
        renderSpectatorView();
        form.classList.add("hidden");
        showNoticeModal("Perfil actualizado", "Tus datos se guardaron correctamente.");
        setOutput(state.spectator);
    } catch (error) {
        showNoticeModal("No se pudo actualizar", errorMessage(error), true);
        setOutput(error, true);
    }
}

async function addPaymentFromProfile(event) {
    event.preventDefault();
    const form = event.currentTarget;

    try {
        validatePaymentExpiration(form.fechaVencimiento.value);

        const metodoPago = await request(api.metodosPago, {
            method: "POST",
            body: {
                numero: form.numero.value,
                fechaVencimiento: form.fechaVencimiento.value,
                nombre: form.nombre.value,
                apellido: form.apellido.value,
                cvv: form.cvv.value
            }
        });

        state.spectator = await request(`${api.espectadores}/${state.spectator.id}/metodo-pago/${metodoPago.id}`, {
            method: "PUT"
        });

        form.reset();
        await loadAll();
        renderSpectatorView();
        form.classList.add("hidden");
        showNoticeModal("Tarjeta agregada", "El método de pago quedó asociado a tu perfil.");
        setOutput(state.spectator);
    } catch (error) {
        showNoticeModal("No se pudo agregar la tarjeta", errorMessage(error), true);
        setOutput(error, true);
    }
}

function clearTicketOutput() {
    const output = $("#ticketOutput");

    if (output) {
        output.innerHTML = "";
    }
}

function showTicket(ticket) {
    const output = $("#ticketOutput");

    if (!output) {
        return;
    }

    output.innerHTML = ticketHtml(ticket);
}

function showTicketModal(ticket) {
    openMailModal("Ticket con QR", ticketHtml(ticket));
}

function ticketHtml(ticket) {
    const entradas = ticket.entradas || [];
    const consumos = ticket.itemsConsumo || [];
    const primeraEntrada = entradas[0];
    const butacas = entradas.map(entrada => entrada.butaca).join(", ");
    const consumosTexto = consumos.length
        ? consumos.map(item => `${escapeHtml(item.producto)} x${item.cantidad} ($${money(item.subtotal)})`).join(", ")
        : "Sin consumo";

    return `
        <article class="ticket-card">
            <div>
                <p class="eyebrow">Ticket generado</p>
                <h3>${escapeHtml(primeraEntrada?.pelicula || state.selectedMovie?.titulo || "Compra")}</h3>
                <p class="muted">${escapeHtml(ticket.codigoQR)}</p>
            </div>
            <div class="qr-box" aria-label="Código QR visual">${renderQrPattern(ticket.codigoQR)}</div>
            <div class="ticket-details">
                <p><strong>Función:</strong> ${formatDateShort(primeraEntrada?.fecha || state.selectedFunction?.fecha || "")} ${shortTime(primeraEntrada?.horario || state.selectedFunction?.horario || "")}</p>
                <p><strong>Sala:</strong> ${escapeHtml(primeraEntrada?.sala || state.selectedFunction?.salaNombre || "-")}</p>
                <p><strong>Formato:</strong> ${formatFunction(primeraEntrada?.formato || state.selectedFunction?.formato)} - ${formatLanguage(primeraEntrada?.idioma || state.selectedFunction?.idioma)}</p>
                <p><strong>Butacas:</strong> ${escapeHtml(butacas || "-")}</p>
                <p><strong>Consumos:</strong> ${consumosTexto}</p>
                <p><strong>Pago:</strong> ${escapeHtml(ticket.metodoDePagoResumen || "-")}</p>
                <div class="summary-total">
                    <span>Total</span>
                    <strong>$${money(ticket.total)}</strong>
                </div>
            </div>
        </article>
    `;
}

function renderQrPattern(value) {
    const text = String(value || "TICKET");
    let seed = 0;

    for (let index = 0; index < text.length; index++) {
        seed = (seed * 31 + text.charCodeAt(index)) % 9973;
    }

    return Array.from({ length: 100 }, (_, index) => {
        const filled = index < 30 || (index + seed + Math.floor(index / 10)) % 3 !== 0;
        return `<span class="${filled ? "filled" : ""}"></span>`;
    }).join("");
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
    fillSelect("#movieEditForm select[name='categoriaId']", state.data.categorias, item => item.nombre);
    fillSelect("#functionForm select[name='peliculaId']", state.data.peliculas, item => item.titulo);
    fillSelect("#functionForm select[name='salaId']", state.data.salas, item => `${item.nombre} (${item.capacidad})`);
    fillSelect("#functionEditForm select[name='peliculaId']", state.data.peliculas, item => item.titulo);
    fillSelect("#functionEditForm select[name='salaId']", state.data.salas, item => `${item.nombre} (${item.capacidad})`);
}

function fillSelect(selector, items, labelFactory, allowEmpty = false) {
    const select = $(selector);

    if (!select) {
        return;
    }

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

async function updateMovie(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const id = Number(form.elements.id.value);
    const peliculaActual = state.data.peliculas.find(pelicula => pelicula.id === id);
    const portadaNueva = await readImageAsDataUrl(form.portada.files[0]);

    await adminPut(`${api.peliculas}/${id}`, {
        titulo: form.titulo.value,
        duracion: Number(form.duracion.value),
        descripcion: form.descripcion.value,
        portadaUrl: portadaNueva || peliculaActual?.portadaUrl || "",
        categoriaId: Number(form.categoriaId.value)
    }, "#movieEditForm");
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

async function updateRoom(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const id = Number(form.elements.id.value);
    const butacasActivasIds = collectRoomEditSeats();

    if (butacasActivasIds.length === 0) {
        setOutput({ error: "La sala debe conservar al menos una butaca activa." }, true);
        return;
    }

    await adminPut(`${api.salas}/${id}/matriz`, {
        nombre: form.nombre.value,
        butacasActivasIds
    }, "#roomEditForm");
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

function renderRoomEditMatrixEditor(salaId) {
    const editor = $("#roomEditMatrixEditor");

    if (!editor) {
        return;
    }

    const seats = state.data.butacas
        .filter(butaca => butaca.salaId === salaId)
        .sort(compareSeats);

    editor.innerHTML = "";

    if (seats.length === 0) {
        editor.innerHTML = `<div class="info-box">La sala no tiene butacas cargadas.</div>`;
        return;
    }

    const seatMap = document.createElement("div");
    seatMap.className = "seat-map room-map-editor";
    seatMap.appendChild(renderScreen());
    seatMap.appendChild(renderSeatMatrix(seats, butaca => {
        const button = document.createElement("button");
        button.className = "seat-button matrix-seat active";
        button.type = "button";
        button.textContent = `${butaca.fila}${butaca.numero}`;
        button.dataset.id = String(butaca.id);
        button.dataset.fila = butaca.fila;
        button.dataset.numero = String(butaca.numero);
        button.addEventListener("click", () => {
            button.classList.toggle("active");
            button.classList.toggle("removed");
            button.textContent = button.classList.contains("active") ? `${butaca.fila}${butaca.numero}` : "";
            updateRoomEditSeatCounter();
        });
        return button;
    }));
    seatMap.appendChild(renderRoomEditHelp());
    editor.appendChild(seatMap);
    updateRoomEditSeatCounter();
}

function collectRoomEditSeats() {
    return [...document.querySelectorAll("#roomEditMatrixEditor .matrix-seat.active")]
        .map(button => Number(button.dataset.id));
}

function renderRoomEditHelp() {
    const help = document.createElement("div");
    help.className = "room-editor-help";
    help.innerHTML = `
        <strong id="roomEditSeatCounter">0 butacas activas</strong>
        <span>Tocá una butaca para quitarla de la sala. Si la tocás otra vez, vuelve a quedar activa.</span>
    `;
    return help;
}

function updateRoomEditSeatCounter() {
    const counter = $("#roomEditSeatCounter");
    const form = $("#roomEditForm");

    if (!counter || !form) {
        return;
    }

    const activeSeats = collectRoomEditSeats().length;
    counter.textContent = `${activeSeats} butaca${activeSeats === 1 ? "" : "s"} activa${activeSeats === 1 ? "" : "s"}`;
    form.capacidad.value = activeSeats;
}

function compareSeats(a, b) {
    const filaComparison = String(a.fila).localeCompare(String(b.fila), "es", { numeric: true });

    if (filaComparison !== 0) {
        return filaComparison;
    }

    return Number(a.numero) - Number(b.numero);
}

async function createFunction(event) {
    event.preventDefault();
    const form = event.currentTarget;

    if (dateIsBeforeToday(form.fecha.value)) {
        setOutput({ error: "La fecha de la función debe ser de hoy o posterior." }, true);
        return;
    }

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

async function updateFunction(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const id = Number(form.elements.id.value);

    if (dateIsBeforeToday(form.fecha.value)) {
        setOutput({ error: "La fecha de la función debe ser de hoy o posterior." }, true);
        return;
    }

    await adminPut(`${api.funciones}/${id}`, {
        fecha: form.fecha.value,
        horario: normalizeTime(form.horario.value),
        peliculaId: Number(form.peliculaId.value),
        salaId: Number(form.salaId.value),
        formato: form.formato.value,
        idioma: form.idioma.value,
        precioEntrada: Number(form.precioEntrada.value)
    }, "#functionEditForm");
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

async function adminPut(url, body, formSelector) {
    try {
        const response = await request(url, { method: "PUT", body });
        setOutput(response);
        cancelEdit(formSelector);
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
        ["Butacas", state.data.butacas, ["id", "butacaNombre", "estado", "salaNombre"]],
        ["Funciones", state.data.funciones, ["id", "peliculaTitulo", "salaNombre", "fecha", "horario", "formato", "idioma", "precioEntrada"]],
        ["Espectadores", state.data.espectadores, ["id", "nombre", "apellido", "email", "metodosDePago"]],
        ["Métodos de pago", state.data.metodosPago, ["id", "ultimosNumeros", "nombre", "apellido", "espectadorNombre"]],
        ["Entradas", state.data.entradas, ["id", "estado", "peliculaTitulo", "funcionDetalle", "salaNombre", "butacaNombre", "espectadorNombre", "precio", "ticketId"]],
        ["Tickets", state.data.tickets, ["id", "espectadorNombre", "metodoDePagoResumen", "total", "entradas", "itemsConsumo"]],
        ["Productos", state.data.productos, ["id", "nombre", "precio", "tipo", "tamano"]],
        ["Consumos", state.data.items, ["id", "productoNombre", "cantidad", "ticketId", "subtotal"]]
    ];

    renderDataGroup("#catalogData", allLists.slice(0, 2));
    renderDataGroup("#roomData", allLists.slice(2, 4));
    renderDataGroup("#functionData", [allLists[4]]);
    renderDataGroup("#spectatorData", allLists.slice(5, 9));
    renderDataGroup("#productData", allLists.slice(9, 11));
    renderDataGroup("#adminData", [
        ...allLists.slice(0, 5),
        ...allLists.slice(9, 11)
    ]);
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
            <div class="data-list">${renderTable(rows, columns, title)}</div>
        </article>
    `).join("");

    container.querySelectorAll("[data-table-filter]").forEach(input => {
        input.addEventListener("input", () => filterTable(input));
    });

    container.querySelectorAll("[data-edit-type]").forEach(button => {
        button.addEventListener("click", () => startEdit(button.dataset.editType, Number(button.dataset.editId)));
    });
}

function renderTable(rows, columns, title) {
    if (!rows.length) {
        return `<p class="muted">Sin datos</p>`;
    }

    const editType = editTypeForTitle(title);
    const actionHeader = editType ? `<th>Acciones</th>` : "";

    return `
        <table>
            <thead><tr>${columns.map(column => `<th>${labelForColumn(column)}</th>`).join("")}${actionHeader}</tr></thead>
            <tbody>
                ${rows.map(row => `
                    <tr>
                        ${columns.map(column => `<td>${escapeHtml(formatValue(row[column]))}</td>`).join("")}
                        ${editType ? `<td><button class="table-action" type="button" data-edit-type="${editType}" data-edit-id="${row.id}">Editar</button></td>` : ""}
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function editTypeForTitle(title) {
    const types = {
        "Películas": "movie",
        "Salas": "room",
        "Funciones": "function"
    };

    return types[title] || "";
}

function startEdit(type, id) {
    if (type === "movie") {
        startMovieEdit(id);
    }

    if (type === "room") {
        startRoomEdit(id);
    }

    if (type === "function") {
        startFunctionEdit(id);
    }
}

function startMovieEdit(id) {
    const pelicula = state.data.peliculas.find(item => item.id === id);
    const form = $("#movieEditForm");

    if (!pelicula || !form) {
        return;
    }

    state.adminTab = "catalogo";
    renderAdminTabs();
    form.classList.remove("hidden");
    form.elements.id.value = pelicula.id;
    form.titulo.value = pelicula.titulo || "";
    form.duracion.value = pelicula.duracion || "";
    form.descripcion.value = pelicula.descripcion || "";
    form.categoriaId.value = pelicula.categoriaId;
    form.scrollIntoView({ behavior: "smooth", block: "start" });
}

function startRoomEdit(id) {
    const sala = state.data.salas.find(item => item.id === id);
    const form = $("#roomEditForm");

    if (!sala || !form) {
        return;
    }

    state.adminTab = "salas";
    renderAdminTabs();
    form.classList.remove("hidden");
    form.elements.id.value = sala.id;
    form.nombre.value = sala.nombre || "";
    form.capacidad.value = sala.capacidad || "";
    renderRoomEditMatrixEditor(sala.id);
    form.scrollIntoView({ behavior: "smooth", block: "start" });
}

function startFunctionEdit(id) {
    const funcion = state.data.funciones.find(item => item.id === id);
    const form = $("#functionEditForm");

    if (!funcion || !form) {
        return;
    }

    state.adminTab = "funciones";
    renderAdminTabs();
    form.classList.remove("hidden");
    form.elements.id.value = funcion.id;
    form.peliculaId.value = funcion.peliculaId;
    form.salaId.value = funcion.salaId;
    form.fecha.value = funcion.fecha || "";
    form.horario.value = shortTime(funcion.horario);
    form.formato.value = funcion.formato || "DOS_D";
    form.idioma.value = funcion.idioma || "ESPANIOL";
    form.precioEntrada.value = funcion.precioEntrada || "";
    form.scrollIntoView({ behavior: "smooth", block: "start" });
}

function cancelEdit(selector) {
    const form = $(selector);

    if (!form) {
        return;
    }

    form.reset();
    form.classList.add("hidden");

    if (selector === "#roomEditForm") {
        const editor = $("#roomEditMatrixEditor");

        if (editor) {
            editor.innerHTML = "";
        }
    }
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

function dateIsBeforeToday(value) {
    if (!value) {
        return true;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const selected = new Date(`${value}T00:00:00`);
    return selected < today;
}

function todayValue() {
    return formatDateInputValue(new Date());
}

function isAdminPath() {
    return window.location.pathname.replace(/\/$/, "") === "/admin";
}

function movieMatchesSearch(pelicula, funciones, query) {
    if (!query) {
        return true;
    }

    const searchable = [
        pelicula.titulo,
        pelicula.categoriaNombre
    ].join(" ").toLowerCase();

    return searchable.includes(query);
}

function renderDateStrip(selector, selectedDate, onSelect) {
    const container = $(selector);

    if (!container) {
        return;
    }

    const activeDate = selectedDate || todayValue();
    const dates = availableFunctionDates();

    if (dates.length === 0) {
        container.innerHTML = "";
        return;
    }

    container.innerHTML = `
        <button class="date-nav" type="button" aria-label="Fechas anteriores">‹</button>
        <div class="date-strip-scroll">
            ${dates.map(date => {
                const parts = formatDateCard(date);
                return `
                    <button class="date-card ${activeDate === date ? "active" : ""}" type="button" data-date-value="${date}">
                        <span>${parts.weekday}</span>
                        <strong>${parts.month}</strong>
                        <em>${parts.day}</em>
                    </button>
                `;
            }).join("")}
        </div>
        <button class="date-nav" type="button" aria-label="Fechas siguientes">›</button>
    `;

    const scroll = container.querySelector(".date-strip-scroll");
    const navButtons = container.querySelectorAll(".date-nav");

    navButtons[0]?.addEventListener("click", () => scroll?.scrollBy({ left: -360, behavior: "smooth" }));
    navButtons[1]?.addEventListener("click", () => scroll?.scrollBy({ left: 360, behavior: "smooth" }));

    container.querySelectorAll("[data-date-value]").forEach(button => {
        button.addEventListener("click", () => onSelect(button.dataset.dateValue));
    });
}

function availableFunctionDates() {
    return [...new Set([todayValue(), ...state.data.funciones.map(funcion => funcion.fecha)])]
        .filter(Boolean)
        .sort();
}

function formatDateCard(value) {
    const [year, month, day] = String(value).split("-").map(Number);
    const date = new Date(year, month - 1, day);
    const today = new Date();
    const isToday = date.toDateString() === today.toDateString();
    const weekday = isToday
        ? "HOY"
        : new Intl.DateTimeFormat("es-AR", { weekday: "long" }).format(date).toUpperCase();
    const monthName = new Intl.DateTimeFormat("es-AR", { month: "short" })
        .format(date)
        .replace(".", "")
        .toUpperCase();

    return { weekday, month: monthName, day };
}

function formatDateShort(value) {
    if (!value) {
        return "-";
    }

    const [year, month, day] = String(value).split("-").map(Number);

    if (!year || !month || !day) {
        return value;
    }

    const date = new Date(year, month - 1, day);
    const weekday = new Intl.DateTimeFormat("es-AR", { weekday: "long" }).format(date);
    return `${weekday} ${day}/${month}`;
}

function formatDateRange(functions) {
    const dates = [...new Set(functions.map(funcion => funcion.fecha))].filter(Boolean).sort();

    if (dates.length === 0) {
        return "Sin fechas";
    }

    if (dates.length === 1) {
        return formatDateShort(dates[0]);
    }

    return `${formatDateShort(dates[0])} a ${formatDateShort(dates[dates.length - 1])}`;
}

function shortDescription(value) {
    const text = String(value || "Sin descripción cargada.").trim();

    if (text.length <= 120) {
        return text;
    }

    return `${text.slice(0, 117)}...`;
}

function nextMonthValue() {
    const date = new Date();
    date.setMonth(date.getMonth() + 1);
    return formatDateInputValue(date).slice(0, 7);
}

function formatDateInputValue(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function validatePaymentExpiration(value) {
    if (paymentIsExpired(value)) {
        throw { error: "La fecha de vencimiento debe ser posterior al mes actual." };
    }
}

function paymentIsExpired(value) {
    if (!value) {
        return true;
    }

    const [year, month] = value.split("-").map(Number);
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth() + 1;

    return year < currentYear || (year === currentYear && month <= currentMonth);
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
        butacaNombre: "Butaca",
        estado: "Estado",
        salaId: "Sala ID",
        salaNombre: "Sala",
        peliculaTitulo: "Título",
        funcionDetalle: "Función",
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
        espectadorNombre: "Espectador",
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

    if (typeof value === "string" && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
        return formatDateShort(value);
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
