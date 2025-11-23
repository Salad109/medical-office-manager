# Medical Office Manager

[![codecov](https://codecov.io/gh/Salad109/medical-office-manager/graph/badge.svg?token=67KJW0FPCR)](https://codecov.io/gh/Salad109/medical-office-manager)
[![CI](https://github.com/Salad109/medical-office-manager/workflows/CI/badge.svg)](https://github.com/Salad109/medical-office-manager/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Kotlin](https://img.shields.io/badge/dynamic/xml?url=https://raw.githubusercontent.com/Salad109/medical-office-manager/master/backend/pom.xml&query=//*[local-name()='kotlin.version']/text()&label=Kotlin&color=purple)
![Java](https://img.shields.io/badge/dynamic/xml?url=https://raw.githubusercontent.com/Salad109/medical-office-manager/master/backend/pom.xml&query=//*[local-name()='java.version']/text()&label=Java&color=orange)
![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https://raw.githubusercontent.com/Salad109/medical-office-manager/master/backend/pom.xml&query=//*[local-name()='parent']/*[local-name()='version']/text()&label=Spring%20Boot&color=brightgreen)
![React](https://img.shields.io/badge/dynamic/json?url=https://raw.githubusercontent.com/Salad109/medical-office-manager/master/frontend/package.json&query=$.dependencies.react&label=React&color=blue)

A comprehensive medical office management system with role-based access control for patients, doctors, and
receptionists. Built with Spring Boot and React in a monorepo architecture.

## Features

- **Role-Based Access Control**: Separate interfaces for patients, doctors, and receptionists
- **Appointment Management**: Schedule, modify, and track medical appointments
- **Visit Records**: Maintain comprehensive patient visit history
- **JWT Authentication**: Secure stateless authentication with token-based sessions
- **Responsive Design**: Modern React UI with Tailwind CSS

## Architecture

### Technology Stack

**Backend:**

- Kotlin + Java
- Spring Boot
- Spring Security with JWT
- MySQL/MariaDB
- Flyway for database migrations
- Maven

**Frontend:**

- React
- Vite
- Tailwind CSS
- JavaScript

**Infrastructure:**

- Docker Compose for containerized deployment
- GitHub Actions for CI/CD

### Project Structure

```
medical-office-manager/
├── backend/                  # Spring Boot application
│   ├── src/main/java/io/salad109/medicalofficemanager/
│   │   ├── appointments/    # Appointment scheduling
│   │   ├── audit/           # Audit logging
│   │   ├── auth/            # Authentication & JWT services
│   │   ├── exception/       # Exception classes & handling
│   │   ├── config/          # Security & CORS configuration
│   │   ├── users/           # User management
│   │   └──visits/           # Medical visit records
│   └── src/main/resources/
│       └── db/migration/    # Flyway database migrations
├── frontend/                 # React application
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── contexts/        # React contexts (Auth, etc.)
│   │   └── pages/           # Application pages
│   └── vite.config.js       # Vite configuration with API proxy
├── compose.yaml             # Docker Compose configuration
└── .github/workflows/       # CI/CD pipelines
```

## Getting Started

```bash
cp backend/.env.example .env
# Edit .env with your database credentials and JWT secret
docker compose up -d
```

Access at `http://localhost`
