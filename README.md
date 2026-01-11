# MyTodoWebApp ✅

A full-stack **Todo Web Application** with user authentication and task management.  
Built using **Spring Boot**, **MySQL**, and a simple **HTML / CSS / JavaScript** frontend.

---

## Features

### Authentication
- User registration
- User login with email and password
- Passwords are securely hashed using **BCrypt**

### Tasks
- Create tasks
- View tasks per user
- Update tasks
- Delete tasks
- Task attributes:
  - Title
  - Priority
  - Category
  - Expense
  - Done status
  - Creation date

---

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Web (REST API)
- Spring Data JPA (Hibernate)
- MySQL
- BCrypt Password Encoder

**Frontend**
- HTML
- CSS
- Vanilla JavaScript (Fetch API)

---

## API Endpoints

Base path: `/api`

### User
- `POST /register` – Register a new user  
- `POST /login` – Login user  
- `GET /user/{id}` – Get user by ID  

### Tasks
- `POST /task` – Create task  
- `GET /tasks/{userId}` – Get all tasks for a user  
- `PUT /task/{id}` – Update task  
- `DELETE /task/{id}` – Delete task  

---

## Database Configuration

```properties
spring.datasource.url=jdbc:mysql://localhost:3305/ToDoList
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
# MyTodoWebApp
