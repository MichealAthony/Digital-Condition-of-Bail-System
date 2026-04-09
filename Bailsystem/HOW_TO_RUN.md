# How to Run the Full DCBS Swing GUI

## Prerequisites
Java JDK 21+  →  check with: `java -version`

## Compile
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

## Run
```bash
java -cp out com.hunts.bail.ui.swing.BailSwingApp
```

## To Run with DB
java -cp "out:lib/sqlite-jdbc-3.36.0.3.jar" com.hunts.bail.ui.swing.BailSwingApp 


## Demo Accounts

| Username          | Password        | Role           | Can do                                  |
|-------------------|-----------------|----------------|-----------------------------------------|
| `officer.brown`   | `Officer#2026`  | Police Officer | Create, View, Update records            |
| `sup.hamilton`    | `Super#2026`    | Supervisor     | + Change status, Audit Report           |
| `manager.johnson` | `Manager#2026`  | Manager        | + Delete records, Audit Report          |
| `admin.james`     | `Admin#2026`    | Administrator  | Everything + Manage Users               |

