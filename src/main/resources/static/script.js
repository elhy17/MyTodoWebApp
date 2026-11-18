let currentUser = null;

window.switchTab = function (tab) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const buttons = document.querySelectorAll('.tab-btn');
    loginForm.classList.toggle('hidden', tab !== 'login');
    registerForm.classList.toggle('hidden', tab !== 'register');
    buttons[0].classList.toggle('active', tab === 'login');
    buttons[1].classList.toggle('active', tab === 'register');
};

async function csrfToken() {
    if (!document.cookie.split('; ').some(cookie => cookie.startsWith('XSRF-TOKEN='))) {
        await fetch('/api/csrf');
    }
    const tokenCookie = document.cookie.split('; ').find(cookie => cookie.startsWith('XSRF-TOKEN='));
    return tokenCookie ? decodeURIComponent(tokenCookie.substring('XSRF-TOKEN='.length)) : '';
}

async function apiRequest(url, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = new Headers(options.headers || {});
    if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
        headers.set('X-XSRF-TOKEN', await csrfToken());
    }
    const response = await fetch(url, { ...options, headers, credentials: 'same-origin' });
    if (!response.ok) {
        const detail = await response.json().catch(() => ({}));
        throw new Error(detail.detail || `Request failed (${response.status})`);
    }
    if (response.status === 204) return null;
    return response.json();
}

function showUser(user) {
    currentUser = user;
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('todoSection').classList.remove('hidden');
    document.getElementById('pageTitle').textContent = `Hi, ${user.firstname}!`;
    loadTasks();
}

window.login = async function () {
    const error = document.getElementById('loginError');
    try {
        const user = await apiRequest('/api/login', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: document.getElementById('loginEmail').value,
                password: document.getElementById('loginPassword').value
            })
        });
        error.textContent = '';
        showUser(user);
    } catch (exception) {
        error.textContent = exception.message;
    }
};

window.register = async function () {
    const error = document.getElementById('regError');
    try {
        await apiRequest('/api/register', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                firstname: document.getElementById('regFirstname').value,
                lastname: document.getElementById('regLastname').value,
                username: document.getElementById('regUsername').value,
                email: document.getElementById('regEmail').value,
                pw: document.getElementById('regPassword').value
            })
        });
        error.textContent = 'Registration successful. Please log in.';
        window.switchTab('login');
    } catch (exception) {
        error.textContent = exception.message;
    }
};

window.addTask = async function () {
    const title = document.getElementById('taskTitle').value;
    if (!title.trim()) return;
    try {
        await apiRequest('/api/task', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, priority: Number(document.getElementById('taskPriority').value), categoryId: 2, expense: 5, done: 0 })
        });
        document.getElementById('taskTitle').value = '';
        await loadTasks();
    } catch (exception) {
        console.error(exception);
    }
};

window.loadTasks = async function () {
    if (!currentUser) return;
    try {
        const tasks = await apiRequest('/api/tasks');
        const list = document.getElementById('tasksList');
        list.replaceChildren();
        if (!tasks.length) {
            const empty = document.createElement('p');
            empty.textContent = 'No tasks found yet.';
            list.appendChild(empty);
            return;
        }
        tasks.forEach(task => {
            const item = document.createElement('div');
            item.className = 'task-item';
            const info = document.createElement('div');
            info.className = 'task-info';
            const title = document.createElement('div');
            title.className = 'task-title';
            title.textContent = task.title;
            const priority = document.createElement('div');
            priority.className = `priority-badge ${task.priority >= 4 ? 'p-high' : task.priority === 3 ? 'p-med' : 'p-low'}`;
            priority.textContent = `Priority ${task.priority}`;
            info.append(title, priority);
            const remove = document.createElement('button');
            remove.className = 'delete-btn';
            remove.textContent = 'Delete';
            remove.addEventListener('click', () => window.deleteTaskWithAnimation(remove, task.id));
            item.append(info, remove);
            list.appendChild(item);
        });
    } catch (exception) {
        if (exception.message.includes('401')) window.logout();
        else console.error(exception);
    }
};

window.deleteTaskWithAnimation = async function (button, taskId) {
    button.closest('.task-item').classList.add('removing');
    window.setTimeout(async () => {
        try {
            await apiRequest(`/api/task/${taskId}`, { method: 'DELETE' });
            await loadTasks();
        } catch (exception) {
            console.error(exception);
        }
    }, 400);
};

window.logout = async function () {
    try { await apiRequest('/api/logout', { method: 'POST' }); } catch (exception) { console.error(exception); }
    currentUser = null;
    document.getElementById('taskTitle').value = '';
    document.getElementById('tasksList').replaceChildren();
    document.getElementById('todoSection').classList.add('hidden');
    document.getElementById('authSection').style.display = 'block';
    document.getElementById('pageTitle').textContent = 'TODO App';
    window.switchTab('login');
};

fetch('/api/me').then(response => response.ok ? response.json() : null).then(user => {
    if (user) showUser(user);
}).catch(() => {});
