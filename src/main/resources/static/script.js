 let currentUser = null;

window.switchTab = function(tab) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const tabBtns = document.querySelectorAll('.tab-btn');

    loginForm.style.opacity = '0';
    registerForm.style.opacity = '0';

    setTimeout(() => {
        if (tab === 'login') {
            loginForm.classList.remove('hidden');
            registerForm.classList.add('hidden');
            if(tabBtns[0]) tabBtns[0].classList.add('active');
            if(tabBtns[1]) tabBtns[1].classList.remove('active');
        } else {
            loginForm.classList.add('hidden');
            registerForm.classList.remove('hidden');
            if(tabBtns[0]) tabBtns[0].classList.remove('active');
            if(tabBtns[1]) tabBtns[1].classList.add('active');
        }

        setTimeout(() => {
            loginForm.style.opacity = '1';
            registerForm.style.opacity = '1';
        }, 50);
    }, 200);
};

window.login = function() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    if (!email || !password) {
        shakeElement(document.getElementById('loginForm'));
        document.getElementById('loginError').textContent = 'Email and password required!';
        return;
    }

    fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email, password: password })
    })
    .then(res => res.json())
    .then(data => {
        if (data && data.id) {
            currentUser = data;
            document.getElementById('authSection').style.display = 'none';
            document.getElementById('todoSection').classList.remove('hidden');
            document.getElementById('pageTitle').innerHTML = `Hi, <span style="color:#6e8efb">${data.firstname}</span>!`;
            loadTasks();
        } else {
            shakeElement(document.getElementById('loginForm'));
            document.getElementById('loginError').textContent = 'Invalid Credentials';
        }
    })
    .catch(err => {
        console.error(err);
        document.getElementById('loginError').textContent = 'Server Error (Is Spring Boot running?)';
    });
};

// --- 3. Register Logic ---
window.register = function() {
    const user = {
        firstname: document.getElementById('regFirstname').value,
        lastname: document.getElementById('regLastname').value,
        username: document.getElementById('regUsername').value,
        email: document.getElementById('regEmail').value,
        pw: document.getElementById('regPassword').value
    };

    if (!user.firstname || !user.lastname || !user.email || !user.pw) {
        shakeElement(document.getElementById('registerForm'));
        document.getElementById('regError').textContent = 'All fields are required!';
        return;
    }

    fetch('/api/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    })
    .then(res => {
        if (!res.ok) throw new Error('Registration failed');
        return res.json();
    })
    .then(() => {
        alert('Registration successful! Please login.');
        window.switchTab('login');
    })
    .catch(err => {
        document.getElementById('regError').textContent = err.message;
    });
};

window.addTask = function() {
    const title = document.getElementById('taskTitle').value;
    const priority = document.getElementById('taskPriority').value;

    if (!title) return;

    const task = {
        userId: currentUser.id,
        title: title,
        priority: parseInt(priority),
        categoryId: 2,
        expense: 5,
        done: 0
    };

    fetch('/api/task', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(task)
    })
    .then(res => res.json())
    .then(() => {
        document.getElementById('taskTitle').value = '';
        loadTasks();
    });
};

window.loadTasks = function() {
    if (!currentUser) return;

    fetch(`/api/tasks/${currentUser.id}`)
    .then(res => res.json())
    .then(tasks => {
        const list = document.getElementById('tasksList');
        list.innerHTML = '';

        if (tasks.length === 0) {
            list.innerHTML = '<p style="text-align:center; color:#888; margin-top:20px">No tasks found 🌴</p>';
            return;
        }

        tasks.forEach((task, index) => {
            const div = document.createElement('div');
            div.className = 'task-item';
            div.style.animationDelay = `${index * 0.1}s`;

            let pClass = 'p-low';
            if(task.priority >= 4) pClass = 'p-high';
            else if(task.priority === 3) pClass = 'p-med';

            div.innerHTML = `
                <div class="task-info">
                    <div class="task-title">${task.title}</div>
                    <div class="priority-badge ${pClass}">Priority ${task.priority}</div>
                </div>
                <button class="delete-btn" onclick="deleteTaskWithAnimation(this, ${task.id})">✕</button>
            `;
            list.appendChild(div);
        });
    })
    .catch(err => console.error("Error loading tasks:", err));
};

window.deleteTaskWithAnimation = function(btnElement, taskId) {
    const taskItem = btnElement.closest('.task-item');
    taskItem.classList.add('removing');
    setTimeout(() => {
        fetch(`/api/task/${taskId}`, { method: 'DELETE' })
        .then(() => loadTasks())
        .catch(err => console.error(err));
    }, 400);
};

window.logout = function() {
    location.reload();
};

function shakeElement(element) {
    element.style.animation = 'shake 0.5s';
    setTimeout(() => element.style.animation = '', 500);
}

const styleSheet = document.createElement("style");
styleSheet.innerText = `
@keyframes shake {
  0% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  50% { transform: translateX(10px); }
  75% { transform: translateX(-10px); }
  100% { transform: translateX(0); }
}`;
document.head.appendChild(styleSheet);