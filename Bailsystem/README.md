# Digitalized Condition of Bail System (DCBS)
## COMP2171 – Object-Oriented Design and Implementation

---

## Project Overview
## Project Overview
A desktop bail management application built for Hunts Bay Police Station as part of COMP2171 (Object-Oriented Design and Implementation) at UWI. It replaces the physical Condition of Bail Book with a role-secured, fully audited Java Swing application.

Features

Create, search, update, and delete bail records with accused photo upload
Log reporting compliance visits (Present / Absent / Late) per bail condition
Role-based access control — Police Officer, Supervisor, Manager, Administrator
Immutable audit trail with filterable report generation
Administrator user management

---

## Architecture — Layered MVC + Domain Model

```
┌─────────────────────────────────────────────┐
│  UI Layer          com.hunts.bail.ui         │  ConsoleUI (→ Web/Desktop in production)
├─────────────────────────────────────────────┤
│  Controller Layer  com.hunts.bail.controller │  BailRecordController
├─────────────────────────────────────────────┤
│  Service Layer     com.hunts.bail.service    │  BailRecordService, AuthenticationService,
│                                             │  ValidationService, IDGeneratorService,
│                                             │  AuditService, NotificationService
├─────────────────────────────────────────────┤
│  Domain Layer      com.hunts.bail.domain     │  BailRecord (aggregate root), AccusedPerson,
│                                             │  BailCondition, AuditLog, User
│                                             │  BailRecordFactory (Factory Pattern)
├─────────────────────────────────────────────┤
│  Repository Layer  com.hunts.bail.repository │  Interfaces + InMemory implementations
├─────────────────────────────────────────────┤
│  Cross-cutting     com.hunts.bail.enums      │  Role, BailStatus, ConditionType
│                    com.hunts.bail.exception  │  ValidationException, AuthenticationException,
│                                             │  RecordPersistenceException
└─────────────────────────────────────────────┘
```

---

## OOD Principles Applied

Encapsulation: All domain entities expose private fields via getters only. AccusedPerson is fully immutable via Builder. 
Single Responsibility: Each class has one reason to change: ValidationService validates, AuditService logs, IDGeneratorService generates IDs. 
Open/Closed: New use cases added as new methods in BailRecordService; existing methods unchanged. Repository interface allows DB swap. 
Interface Segregation: Separate interfaces for BailRecordRepository, AuditLogRepository, UserRepository. 
Dependency Inversion: Services depend on repository *interfaces*, never on InMemory concrete classes. 
Factory Pattern (GoF):  BailRecordFactory controls BailRecord construction, enforcing aggregate invariants. 
Builder Pattern (GoF): AccusedPerson.Builder manages the many constructor parameters cleanly. 
MVC:  UI → Controller → Service → Domain → Repository separation. 
Immutability: AccusedPerson and AuditLog have no setters; created once and never changed. 



---

## How to Compile and Run

### Prerequisites
- Java 21+

### Compile
```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
```

### Run
```bash
java -cp "out:lib/sqlite-jdbc-3.36.0.3.jar" com.hunts.bail.ui.swing.BailSwingApp 
```

---

## Demo Scenarios

The `BailSystemApplication` runs three scenarios automatically:

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Officer logs in with valid credentials and submits a complete bail form | ✔ Record created, audit logged, bail ID printed |
| 2 | Officer submits a form with missing/invalid fields | ✘ All validation errors listed, record NOT created |
| 3 | Supervisor (wrong role) attempts to create a bail record | ✘ Access denied, error displayed |

---

## Package Structure

```
src/main/java/com/hunts/bail/
├── BailSystemApplication.java          ← Entry point / DI wiring
├── controller/
│   └── BailRecordController.java
├── domain/
│   ├── AccusedPerson.java              ← Immutable value object 
│   ├── AuditLog.java                   ← Immutable audit entry
│   ├── BailCondition.java
│   ├── BailRecord.java                 ← Aggregate root
│   ├── BailRecordFactory.java          ← Factory Pattern
│   └── User.java
├── enums/
│   ├── BailStatus.java
│   ├── ConditionType.java
│   └── Role.java
├── exception/
│   ├── AuthenticationException.java
│   ├── RecordPersistenceException.java
│   └── ValidationException.java
├── repository/
│   ├── AuditLogRepository.java         ← Interface
│   ├── BailRecordRepository.java       ← Interface
│   ├── UserRepository.java             ← Interface
│   ├── InMemoryAuditLogRepository.java ← Implementation
│   ├── InMemoryBailRecordRepository.java
│   └── InMemoryUserRepository.java
├── service/
│   ├── AuditService.java
│   ├── AuthenticationService.java
│   ├── BailRecordService.java          ← Core UC1 orchestrator
│   ├── IDGeneratorService.java
│   ├── NotificationService.java
│   └── ValidationService.java
└── ui/
    └── ConsoleUI.java                  ← Demo UI layer
```
