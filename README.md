# MediCore — Hospital Management System

A complete, production-style **Hospital Management System** built with **Java 21 and Spring Boot 3**, designed as a portfolio-quality, layered enterprise web application (Controller → Service → Repository → Entity → Database).

> Built for: internship/portfolio submissions, GitHub showcase, university final projects, and junior Java backend developer CVs.

---

## 🔐 Security & Role-Based Access Control

This isn't just "login required" — every role sees a genuinely different application, enforced at three layers:

| Layer | How it's enforced |
|---|---|
| **URL-level** | `SecurityConfig` restricts whole modules per role (e.g. `/medical-records/**` → ADMIN + DOCTOR only, RECEPTIONIST gets a 403) |
| **Action-level** | `@PreAuthorize` on individual controller methods (e.g. only ADMIN can delete a doctor; only RECEPTIONIST can create an invoice) |
| **Object-level** | A DOCTOR can only view/edit *their own* patients, appointments, and medical records — verified against the data, not just the role |
| **UI-level** | `sec:authorize` in Thymeleaf hides buttons/menu items a user isn't allowed to use, so no one even sees a "Delete Doctor" button they can't click |

**Permission matrix:**

| Role | Can | Cannot |
|---|---|---|
| **ADMIN** | Manage users, doctors, receptionists, patients; view all appointments/invoices/medical records; reports; hospital settings | Write diagnoses or prescriptions |
| **DOCTOR** | View assigned patients & own appointments; create/update medical records, diagnoses, prescriptions; complete appointments | Register/delete patients; manage doctors/users; create invoices; view reports/settings |
| **RECEPTIONIST** | Register/edit patients; schedule/cancel appointments; create/print invoices; search patients | Manage doctors/users; medical records; diagnoses/prescriptions; reports/settings |

Each role also gets its own dashboard (e.g. ADMIN sees hospital-wide revenue and an appointment-status chart; DOCTOR sees today's schedule and recent records; RECEPTIONIST sees today's bookings and waiting patients) and its own sidebar navigation.

---

## ✨ Features

### Authentication & Security
- Login / logout / registration with **Spring Security** session-based auth
- **BCrypt** password hashing
- Full role-based access control: **ADMIN**, **DOCTOR**, **RECEPTIONIST** (see above)
- Custom `UserDetailsService`, global access-denied and error pages

### Dashboard
- Three distinct dashboards, one per role (see Security section above)

### Patient Management
- Add / edit / delete / view patients, with pagination and keyword search
- Duplicate email & phone number prevention, field validation, computed age

### Doctor Management
- Add / edit / delete / view doctors
- Specialization, department, years of experience, consultation fee, availability toggle

### Appointment Management
- Book / edit / cancel / delete appointments
- Conflict detection (prevents double-booking a doctor within the same time slot)
- Filter by status (Scheduled / Completed / Cancelled / No-show) and free-text search

### Medical Records
- Diagnosis, prescription, doctor notes, and visit date per patient
- Full history displayed on each patient's profile page

### Billing / Invoices
- Generate invoices tied to a patient (and optionally a doctor)
- Payment status tracking (Pending / Paid / Partially Paid / Refunded)
- Printable receipt view

### Global Search
- Single search box in the navbar that queries patients, doctors, and appointments at once (results scoped per role)

### Validation & Error Handling
- Bean Validation (`jakarta.validation`) on every form
- Centralized `@ControllerAdvice` with dedicated 403 / 404 / general error pages
- No stack traces ever shown to the user

---

## 🛠️ Tech Stack

| Layer            | Technology                                  |
|-------------------|---------------------------------------------|
| Language          | Java 21                                      |
| Framework         | Spring Boot 3 (Spring MVC, Spring Data JPA)  |
| ORM               | Hibernate                                    |
| Security          | Spring Security 6 + BCrypt                   |
| Build Tool        | Maven                                        |
| Database          | MySQL (production) / H2 (zero-setup default) |
| Templating        | Thymeleaf                                    |
| Frontend          | Bootstrap 5, HTML5, CSS3, JavaScript         |
| Validation        | Spring Validation (Jakarta Bean Validation)  |
| Boilerplate       | Lombok                                       |
| Testing           | JUnit 5, Mockito, AssertJ                    |

---

## 🏗️ Architecture

Classic layered / clean architecture:

```
Controller  →  Service (interface)  →  Service.impl  →  Repository  →  Entity  →  Database
```

```
com.hospital.hms
├── config          → SecurityConfig, DataInitializer
├── controller       → Auth, Dashboard, Patient, Doctor, Appointment,
│                      MedicalRecord, Invoice, GlobalSearch, Home
├── service           → Interfaces (PatientService, DoctorService, ...)
├── service.impl       → Implementations
├── repository         → Spring Data JPA repositories
├── entity              → Patient, Doctor, Appointment, MedicalRecord, Invoice, User (+ enums)
├── dto                  → Request/response DTOs with Bean Validation
├── exception              → Custom exceptions + GlobalExceptionHandler
└── security                 → CustomUserDetails, CustomUserDetailsService
```

Design principles applied: SOLID, constructor injection (`@RequiredArgsConstructor`), DTO pattern, repository pattern, service-layer transaction boundaries (`@Transactional`), and a single global exception handler.

---

## 🚀 How to Run

### Option A — Zero setup (H2 in-memory database)

The project ships with an H2 in-memory database active **by default**, so it runs immediately with no external database required — ideal for demos and grading.

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** — you'll be redirected to the login page.

The app automatically seeds sample data on first run (admin/doctor/receptionist accounts, 4 doctors, 4 patients, appointments, medical records, and invoices).

**Demo credentials:**

| Role         | Username    | Password        |
|--------------|-------------|------------------|
| Admin        | `admin`     | `Admin@123`      |
| Doctor       | `doctor`    | `Doctor@123`     |
| Receptionist | `reception` | `Reception@123`  |

The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:hospitaldb`, user `sa`, blank password) once logged in.

### Option B — MySQL (production-style setup)

1. Make sure MySQL is running locally and create the schema (optional — Hibernate will also auto-create it):
   ```sql
   CREATE DATABASE hospital_management_db;
   ```
   Or run the provided reference script: `database/schema.sql`.

2. Edit `src/main/resources/application-mysql.properties` with your MySQL username/password.

3. Run with the `mysql` profile active:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Running in IntelliJ IDEA

1. **File → Open** → select the project's root folder (the one containing `pom.xml`).
2. Let Maven finish importing dependencies.
3. Run `HospitalManagementApplication.java` (right-click → Run).
4. By default this uses the H2 profile and needs no further configuration.
   To use MySQL instead, add `-Dspring-boot.run.profiles=mysql` to the run configuration's VM/program arguments, or set `spring.profiles.active=mysql` in `application.properties`.

### Running Tests

```bash
mvn test
```

---

## 🗄️ Database

- `database/schema.sql` — reference MySQL DDL script (tables, foreign keys, indexes) matching the JPA entity model.
- `database/sample-data.sql` — optional reference sample-data script for manual MySQL setup (the app already seeds this automatically via `DataInitializer.java`).
- Entity relationships:
  - `Patient` 1 —— * `Appointment` * —— 1 `Doctor`
  - `Patient` 1 —— * `MedicalRecord` * —— 1 `Doctor`
  - `Patient` 1 —— * `Invoice` *(optional)*—— 1 `Doctor`
  - `User` holds application login credentials and a `Role` (ADMIN / DOCTOR / RECEPTIONIST)

---

## 📁 Folder Structure

```
hospital-management-system/
├── pom.xml
├── .gitignore
├── README.md
├── database/
│   ├── schema.sql
│   └── sample-data.sql
├── src/
│   ├── main/
│   │   ├── java/com/hospital/hms/...
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-h2.properties
│   │       ├── application-mysql.properties
│   │       ├── static/{css,js}
│   │       └── templates/{auth,patients,doctors,appointments,medical-records,invoices,fragments,error}
│   └── test/java/com/hospital/hms/...
```

---

## 📸 Screenshots

_Add screenshots of the Dashboard, Patients list, Appointment booking form, and Invoice receipt here once you run the app locally — this section is intentionally left for you to fill in with your own captures for your portfolio/GitHub README._

---

## 🔭 Future Improvements

- REST API layer (separate from the MVC/Thymeleaf UI) for a mobile client or SPA frontend
- Email/SMS appointment reminders
- File uploads for lab reports / scanned documents attached to medical records
- Audit logging (who changed what, and when)
- Doctor working-hours/availability calendar instead of a simple boolean flag
- Multi-tenancy for supporting multiple hospital branches

---

## 📄 License

This project is provided under the MIT License — free to use, modify, and distribute for personal, academic, or portfolio purposes.
