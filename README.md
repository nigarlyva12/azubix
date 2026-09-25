# LearnHub — Learning Platform

A full-stack learning platform built with **Spring Boot** and **Thymeleaf**, similar in concept to W3Schools or GeeksforGeeks. Supports categories, topics, articles with a WYSIWYG editor, user registration, role-based access, and progress tracking.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Backend      | Spring Boot 3.2 (Java 17)          |
| Frontend     | Thymeleaf (server-side rendering)   |
| Database     | PostgreSQL                          |
| ORM          | Spring Data JPA (Hibernate)         |
| Security     | Spring Security                     |
| Editor       | Quill.js (WYSIWYG rich text)        |
| Code Highlighting | Prism.js (client-side)         |
| Build Tool   | Maven                               |

---

## Prerequisites

Before running this project, make sure you have installed:

1. **Java 17** (or higher)
   ```bash
   java -version
   ```

2. **Maven 3.8+**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 14+**
   ```bash
   psql --version
   ```

---

## Step-by-Step Setup

### Step 1: Create the PostgreSQL Database

Open your terminal or pgAdmin and create a new database:

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create the database
CREATE DATABASE learnhub;

-- Verify it was created
\l
```

### Step 2: Configure Database Connection

Open `src/main/resources/application.properties` and update these values to match your PostgreSQL setup:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/learnhub
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Step 3: Build and Run

```bash
# Navigate to the project directory
cd learnhub

# Build the project (downloads dependencies, compiles code)
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at: **http://localhost:8080**

### Step 4: Log In

A default admin account is created automatically on first run:

| Field    | Value               |
|----------|---------------------|
| Email    | admin@learnhub.com  |
| Password | admin123            |

> **Important:** Change the admin password in production! See `DataInitializer.java`.

---

## Project Structure

```
learnhub/
├── pom.xml                              # Maven dependencies
├── src/main/java/com/learnhub/
│   ├── LearnHubApplication.java         # Main entry point
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security rules
│   │   └── DataInitializer.java         # Creates default admin
│   ├── entity/                          # JPA entities (database tables)
│   │   ├── Role.java                    # ADMIN / USER enum
│   │   ├── User.java
│   │   ├── Category.java
│   │   ├── Topic.java
│   │   ├── Article.java
│   │   └── Progress.java
│   ├── repository/                      # Spring Data JPA interfaces
│   │   ├── UserRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── TopicRepository.java
│   │   ├── ArticleRepository.java
│   │   └── ProgressRepository.java
│   ├── dto/                             # Data Transfer Objects
│   │   ├── UserRegistrationDto.java
│   │   ├── CategoryDto.java
│   │   ├── TopicDto.java
│   │   └── ArticleDto.java
│   ├── service/                         # Business logic layer
│   │   ├── UserService.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── CategoryService.java
│   │   ├── TopicService.java
│   │   ├── ArticleService.java
│   │   └── ProgressService.java
│   └── controller/                      # HTTP request handlers
│       ├── AuthController.java          # Login & registration
│       ├── HomeController.java          # Public pages & progress
│       └── AdminController.java         # Admin CRUD operations
├── src/main/resources/
│   ├── application.properties           # App configuration
│   ├── static/css/style.css             # Custom styles
│   └── templates/                       # Thymeleaf HTML templates
│       ├── fragments/
│       │   ├── navbar.html
│       │   ├── sidebar.html
│       │   └── footer.html
│       ├── home.html                    # Home page (category list)
│       ├── login.html
│       ├── register.html
│       ├── topics.html                  # Topics in a category
│       ├── topic-detail.html            # Articles in a topic
│       ├── article.html                 # Article reader view
│       ├── error.html                   # Error page
│       └── admin/
│           ├── dashboard.html
│           ├── categories.html
│           ├── topics.html
│           ├── articles.html
│           └── article-form.html        # WYSIWYG editor page
└── README.md
```

---

## Architecture Overview

This project follows the **layered architecture** pattern:

```
[Browser] → [Controller] → [Service] → [Repository] → [Database]
              (HTTP)        (Logic)      (SQL/JPA)     (PostgreSQL)
```

- **Controller Layer**: Receives HTTP requests, calls services, returns Thymeleaf views
- **Service Layer**: Contains business logic, validation, data transformation
- **Repository Layer**: JPA interfaces that generate SQL queries automatically
- **Entity Layer**: Java classes mapped to database tables via Hibernate

---

## Features

### For Users
- Browse categories, topics, and articles
- Read articles with syntax-highlighted code blocks
- Register and log in
- Mark topics as completed
- Track progress with per-category progress bars

### For Admins
- Dashboard with content overview stats
- Full CRUD for categories, topics, and articles
- WYSIWYG rich text editor (Quill.js) with:
  - Headings (H1, H2, H3)
  - Bold, italic, underline, strikethrough
  - Ordered and unordered lists
  - Code blocks with syntax highlighting
  - Blockquotes
  - Links and images
  - Clean formatting button

### Security
- BCrypt password hashing
- Role-based access control (ADMIN / USER)
- CSRF protection (enabled by default)
- Protected admin routes (`/admin/**`)

---

## Data Model Relationships

```
User ──────< Progress >────── Topic
                                │
Category ──────< Topic ──────< Article
```

- **User** → has many **Progress** records
- **Category** → has many **Topics**
- **Topic** → belongs to one **Category**, has many **Articles**
- **Article** → belongs to one **Topic**
- **Progress** → links one **User** to one **Topic** (completed: true/false)

---

## API Routes

### Public Routes
| Method | URL                | Description              |
|--------|--------------------|--------------------------|
| GET    | `/`                | Home page (categories)   |
| GET    | `/login`           | Login page               |
| GET    | `/register`        | Registration page        |
| POST   | `/register`        | Handle registration      |
| GET    | `/category/{id}`   | Topics in category       |
| GET    | `/topic/{id}`      | Articles in topic        |
| GET    | `/article/{id}`    | Read an article          |

### Authenticated Routes
| Method | URL                        | Description           |
|--------|----------------------------|-----------------------|
| POST   | `/progress/toggle/{topicId}` | Toggle completion   |

### Admin Routes (ADMIN role only)
| Method | URL                            | Description          |
|--------|--------------------------------|----------------------|
| GET    | `/admin`                       | Dashboard            |
| GET    | `/admin/categories`            | List categories      |
| POST   | `/admin/categories/create`     | Create category      |
| GET    | `/admin/categories/edit/{id}`  | Edit form            |
| POST   | `/admin/categories/update/{id}`| Update category      |
| POST   | `/admin/categories/delete/{id}`| Delete category      |
| GET    | `/admin/topics`                | List topics          |
| POST   | `/admin/topics/create`         | Create topic         |
| GET    | `/admin/topics/edit/{id}`      | Edit form            |
| POST   | `/admin/topics/update/{id}`    | Update topic         |
| POST   | `/admin/topics/delete/{id}`    | Delete topic         |
| GET    | `/admin/articles`              | List articles        |
| GET    | `/admin/articles/create`       | Create form          |
| POST   | `/admin/articles/create`       | Save new article     |
| GET    | `/admin/articles/edit/{id}`    | Edit form            |
| POST   | `/admin/articles/update/{id}`  | Update article       |
| POST   | `/admin/articles/delete/{id}`  | Delete article       |

---

## Customization

### Changing the Default Admin Credentials
Edit `src/main/java/com/learnhub/config/DataInitializer.java`:
```java
admin.setEmail("your-email@example.com");
admin.setPassword(passwordEncoder.encode("your-secure-password"));
```

### Adding More Prism.js Languages
Edit `src/main/resources/templates/article.html` and add more `<script>` tags:
```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-go.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-rust.min.js"></script>
```

### Changing Colors / Theme
Edit `src/main/resources/static/css/style.css` and modify the CSS variables at the top:
```css
:root {
    --primary: #4f46e5;      /* Change to your brand color */
    --primary-dark: #4338ca;
    ...
}
```

---

## Troubleshooting

| Problem                        | Solution                                          |
|--------------------------------|---------------------------------------------------|
| Port 8080 already in use       | Change `server.port` in application.properties     |
| Cannot connect to database     | Check PostgreSQL is running and credentials match   |
| Tables not created             | Ensure `spring.jpa.hibernate.ddl-auto=update`      |
| Login always fails             | Check password encoding — never store plain text   |
| Quill editor content not saving| Check browser console for JavaScript errors         |

---

## Future Enhancements (Planned)

- AI-powered question & answer system
- Search functionality across articles
- User profile page with detailed progress
- Comments / discussion on articles
- PDF export of articles
- Dark mode toggle

---

## License

This project is for educational purposes. Built with Spring Boot and Thymeleaf.
