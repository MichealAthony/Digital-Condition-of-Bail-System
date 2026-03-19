# How to Run the system using the Swing GUI

## The machine needs to have
Java JDK 21+  →  check with: `java -version`

## Before running, compile using the following commands
```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
```
Windows:
```cmd
mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
```

## To run GUI using the following command
```bash
java -cp out com.hunts.bail.ui.swing.BailSwingApp
```

## The following are demo Accounts which can be used to access system

| Username          | Password        | Role           | Can do                                  |
|-------------------|-----------------|----------------|-----------------------------------------|
| `officer.brown`   | `Officer#2026`  | Police Officer | Create, View, Update records            |
| `sup.hamilton`    | `Super#2026`    | Supervisor     | + Change status, Audit Report           |
| `manager.johnson` | `Manager#2026`  | Manager        | + Delete records, Audit Report          |
| `admin.james`     | `Admin#2026`    | Administrator  | Everything + Manage Users               |


