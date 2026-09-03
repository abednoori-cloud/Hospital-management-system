-- =========================================================
-- Hospital Management System - MySQL Schema
-- =========================================================
-- This script is provided for reference / manual setup.
-- The application also creates/updates this schema
-- automatically on startup via Hibernate (ddl-auto=update)
-- when the "mysql" Spring profile is active.
-- =========================================================

CREATE DATABASE IF NOT EXISTS hospital_management_db;
USE hospital_management_db;

-- ---------------------------------------------------------
-- users (application accounts: ADMIN, DOCTOR, RECEPTIONIST)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME
);

-- ---------------------------------------------------------
-- patients
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address VARCHAR(255),
    blood_group VARCHAR(5),
    emergency_contact VARCHAR(20),
    created_at DATETIME
);

-- ---------------------------------------------------------
-- doctors
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    specialization VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    years_of_experience INT,
    consultation_fee DECIMAL(10,2),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME
);

-- ---------------------------------------------------------
-- appointments (Many-to-One -> patients, Many-to-One -> doctors)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date_time DATETIME NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(1000),
    created_at DATETIME,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- medical_records (Many-to-One -> patients, Many-to-One -> doctors)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    diagnosis VARCHAR(1000) NOT NULL,
    prescription VARCHAR(1000),
    doctor_notes VARCHAR(2000),
    created_at DATETIME,
    CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- invoices (Many-to-One -> patients, Many-to-One -> doctors)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT,
    invoice_date DATE NOT NULL,
    treatment_description VARCHAR(500) NOT NULL,
    treatment_cost DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME,
    CONSTRAINT fk_invoice_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL
);

CREATE INDEX idx_appointments_datetime ON appointments(appointment_date_time);
CREATE INDEX idx_records_visit_date ON medical_records(visit_date);
CREATE INDEX idx_invoices_date ON invoices(invoice_date);

-- ---------------------------------------------------------
-- Link a DOCTOR-role user account to its clinical Doctor profile.
-- Added via ALTER TABLE since it references doctors(id), which is
-- created after users. Nullable — only DOCTOR-role accounts use it,
-- and only once an admin links them from the Users management screen.
-- Powers "My Patients" / "My Appointments" scoping for that doctor.
-- ---------------------------------------------------------
ALTER TABLE users ADD COLUMN doctor_id BIGINT NULL;
ALTER TABLE users ADD CONSTRAINT fk_user_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL;
