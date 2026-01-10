let currentUser = null;

    function switchTab(tab) {
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const tabBtns = document.querySelectorAll('.tab-btn');

        if (tab === 'login') {
            loginForm.classList.remove('hidden');
            registerForm.classList.add('hidden');
            tabBtns[0].classList.add('active');
            tabBtns[1].classList.remove('active');
        } else {
            loginForm.classList.add('hidden');
            registerForm.classList.remove('hidden');
            tabBtns[0].classList.remove('active');
            tabBtns[1].classList.add('active');
        }
    }

    function register() {
        const user = {
            firstname: document.getElementById('regFirstname').value,
            lastname: document.getElementById('regLastname').value,
            username: document.getElementById('regUsername').value,
            email: document.getElementById('regEmail').value,
            pw: document.getElementById('regPassword').value
        };

        if (!user.firstname || !user.lastname || !user.username || !user.email || !user.pw) {
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
        .then(data => {
            alert('Registration successful! Please login.');
            document.getElementById('regFirstname').value = '';
            document.getElementById('regLastname').value = '';
            document.getElementById('regUsername').value = '';
            document.getElementById('regEmail').value = '';
            document.getElementById('regPassword').value = '';
            document.getElementById('regError').textContent = '';
            switchTab('login');
        })
        .catch(err => {
            document.getElementById('regError').textContent = 'Registration failed: ' + err.message;
        });
    }

    function login() {
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;

        if (!email || !password) {
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
            if (data && data.id != null) {
                currentUser = data;
                document.getElementById('authSection').classList.add('hidden');
                document.getElementById('todoSection').classList.remove('hidden');
                document.getElementById('pageTitle').textContent = `Welcome, ${data.firstname}!`;
                document.getElementById('loginError').textContent = '';

                console.log(data.id)
                loadTasks();
            } else {
                document.getElementById('loginError').textContent = 'Invalid email or password!';
                console.log("error in stocking data");
            }
        })
        .catch(err => {
            document.getElementById('loginError').textContent = 'Login error: ' + err.message;
        });
    }

    function logout() {
        currentUser = null;
        document.getElementById('authSection').classList.remove('hidden');
        document.getElementById('todoSection').classList.add('hidden');
        document.getElementById('pageTitle').textContent = 'TODO App';
        document.getElementById('loginEmail').value = '';
        document.getElementById('loginPassword').value = '';
    }

    function addTask() {
        const title = document.getElementById('taskTitle').value;
        const priority = document.getElementById('taskPriority').value;

        if (!title) {
            alert('Task title is required!');
            return;
        }

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
        .then(res => {
            if (!res.ok) throw new Error('Failed to add task');
            return res.json();
        })
        .then(data => {
            document.getElementById('taskTitle').value = '';
            document.getElementById('taskPriority').value = '3';
            loadTasks();
        })
        .catch(err => alert('Error adding task: ' + err.message));
    }

    function loadTasks() {
        fetch(`/api/tasks/${currentUser.id}`)
        .then(res => res.json())
        .then(tasks => {
            const tasksList = document.getElementById('tasksList');
            tasksList.innerHTML = '';

            if (tasks.length === 0) {
                tasksList.innerHTML = '<p style="color: #999; text-align: center;">No tasks yet. Add one!</p>';
                return;
            }

            tasks.forEach(task => {
                tasksList.innerHTML += `
                    <div class="task-item">
                        <div class="task-info">
                            <div class="task-title">${task.title}</div>
                            <div class="task-priority">Priority: ${task.priority}/5</div>
                        </div>
                        <button class="delete-btn" onclick="deleteTask(${task.id})">Delete</button>
                    </div>
                `;
            });
        })
        .catch(err => alert('Error loading tasks: ' + err.message));
    }

    function deleteTask(taskId) {
        fetch(`/api/task/${taskId}`, { method: 'DELETE' })
        .then(() => loadTasks())
        .catch(err => alert('Error deleting task: ' + err.message));
    }
