# rapid-crud-generator

Generate a ready-to-use RESTful API and Admin Dashboard from your JSON Schema — in seconds.

---

## 🚀 What is this?

**rapid-crud-generator** is a developer-friendly tool that automatically generates a backend (Spring Boot) and admin frontend (Angular) based on a user-provided JSON schema.

No boilerplate. No setup. Just schema in, code out.

---

## ✨ Features (MVP)

- 🔁 Upload or edit your JSON schema
- ⚙️ Auto-generate RESTful API (Spring Boot + JPA)
- 🎛️ Auto-generate Angular Admin Dashboard (CRUD)
- 📦 One-click zip download of the generated code
- 🛠️ Easily extend, run, or deploy the code

---

## 📦 Output Structure
```
output/
├── controller/
│   ├── UserController.java
│   └── PostController.java
├── entity/
│   ├── User.java
│   └── Post.java
├── repository/
│   ├── UserRepository.java
│   └── PostRepository.java
└── frontend/
└── src/app/
├── user/
└── post/
```

---

## 🚀 How to Use (Developer Mode)
### 1. Start backend (Spring Boot)
`cd backend
./mvnw spring-boot:run`

### 2. Send your JSON schema via Postman or curl

Make sure your backend service is running at `http://localhost:8080`.

You can send your schema using `curl`:

```bash
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d @../example-schema.json
```
📝 Note: 
* Make sure the path ../example-schema.json is correct. Adjust the path as needed based on your working directory.
* Alternatively, you can use Postman to send a POST request to /api/generate, with the schema in the request body and Content-Type: application/json.

### 🧪 Example Input (JSON Schema: example-schema.json)

```json
{
  "User": {
    "id": "Long",
    "username": "String",
    "email": "String"
  },
  "Product": {
    "id": "Long",
    "title": "String",
    "price": "Double"
  }
}
```
### 3. A zip file will be generated in your project root

---
## 🧩 Customize Templates
All templates are located in `backend/src/main/resources/templates/:`

```
├── springboot/
│   └── Entity.ftl, Controller.ftl, ...
└── angular/
    └── table.component.ts.ftl, ...
```

You can add new code templates and adapt naming conventions, folder structure, or application layers.

Examples of custom templates you can add:

- ✅ **DTOs**: Generate data-transfer objects that hide sensitive fields.
- ✅ **Service layer**: Generate business logic classes to decouple controller and repository.
- ✅ **Pagination**: Generate REST APIs that support pagination parameters.
- ✅ **Permissions**: Add role-based access control annotations like `@PreAuthorize`.
- ✅ **i18n**: Output message keys for internationalization support.

You can define your own templates to meet your project needs.

---
## 🔮 Roadmap Ideas

- ☁️ **Web UI for Schema Editing & Live Preview**  
  Build an intuitive web interface where users can edit schema and see real-time code previews or generate live projects online.

- 🧠 **AI-assisted Schema Generation** ← ✨ New  
  Use AI to help users generate JSON schema based on natural language prompts like:  
  “I want a user table with email, password, and signup date.”

- 🌍 **OpenAPI / Swagger Support**  
  Accept `.yaml` or `.json` OpenAPI files and generate backend & frontend automatically.

- 🧱 **MongoDB / GraphQL Support**  
  Extend backend generators to support document databases and GraphQL endpoints.

- 🧩 **Plugin-based Template Extensions**  
  Allow developers to add or override templates easily (e.g., pagination, auth module, custom validation).

- 🎨 **Angular Themes & Dark Mode**  
  Let users pick UI themes for the generated frontend, including dark mode and layout styles.
