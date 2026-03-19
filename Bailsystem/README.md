# Digitalized Condition of Bail System (DCBS) for the Hunts Bay Police Station
## In fullfilment of COMP2171 – University of West Indies, Mona

---

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
Five-layer MVC design. All data access sits behind repository interfaces — swap the in-memory store for JDBC/PostgreSQL by changing three classes and three lines in bootstrap, nothing else.

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

## OOD Principles Used

**Encapsulation**  All domain entities expose private fields via getters only. AccusedPerson is fully immutable via Builder. 
**Single Responsibility** Each class has one reason to change: ValidationService validates, AuditService logs, IDGeneratorService generates IDs. 
**Open/Closed** New use cases added as new methods in BailRecordService; existing methods unchanged. Repository interface allows DB swap. 
**Interface Segregation**  Separate interfaces for BailRecordRepository, AuditLogRepository, UserRepository. 
**Dependency Inversion** Services depend on repository *interfaces*, never on InMemory concrete classes. 
**Factory Pattern (GoF)**  BailRecordFactory controls BailRecord construction, enforcing aggregate invariants. 
**Builder Pattern (GoF)** AccusedPerson.Builder manages the many constructor parameters cleanly. 
**MVC**  UI → Controller → Service → Domain → Repository separation. 
**Immutability** AccusedPerson and AuditLog have no setters; created once and never changed. 



