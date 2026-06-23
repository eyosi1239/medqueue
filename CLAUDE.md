# MedQueue — Project Guide

## What This Is
A hospital management system with three independent parts that all run together:
- **`frontend/`** — Single `index.html` file (vanilla JS + CSS, no framework)
- **`java-backend/`** — Pure Java HTTP server on port 8080 (no Maven, no Spring)
- **`python-dsa/`** — Flask API on port 5000 for the patient priority queue

## How to Run

### Windows (all at once)
```
start.bat
```

### Manual
```bash
# Python (port 5000)
cd python-dsa
pip install flask
python app.py

# Java (port 8080)
cd java-backend
run.bat              # compiles + runs on Windows
# or manually:
mkdir out
javac -d out src/com/medqueue/**/*.java src/com/medqueue/Main.java
java -cp out com.medqueue.Main
```

Then open `frontend/index.html` directly in a browser (no dev server needed).

## Architecture

```
frontend/index.html
  ├── calls /api/patients/*     → python-dsa (port 5000)   [queue management]
  └── calls /api/doctors/*      → java-backend (port 8080)  [hospital records]
       /api/appointments/*
       /api/records/*
       /api/invoices/*
```

Data is stored in-memory at runtime. Python persists to `python-dsa/patients.json` on every write. Java uses an in-memory `DataRegistry` singleton (no file persistence).

---

## Standards

### General
- No new external dependencies without a good reason — this project intentionally has minimal deps
- Keep the three-service split: Python owns the queue, Java owns hospital records, frontend ties them together
- API responses are always JSON; status codes follow HTTP conventions (200, 201, 400, 404, 409, 500)

### Java (`java-backend/`)
- **Classes**: `PascalCase` — `AppointmentController`, `DoctorRepository`
- **Methods / fields**: `camelCase` — `findById`, `maxDailyPatients`
- **Package structure**: `model/` for entities, `repository/` for data access, `controller/` for HTTP handlers, `server/` for shared infrastructure
- **Controllers**: always extend `BaseHandler`, implement `route()`, use `sendResponse()` for all output
- **Models**: always include a `toJson()` method that manually serializes to a JSON string
- **No libraries**: no Gson, no Jackson, no external JARs — use `JsonParser` in `server/` for parsing
- **Compilation**: add new `.java` files to the `javac` command in `run.bat`

### Python (`python-dsa/`)
- **Functions / variables**: `snake_case`
- **Classes**: `PascalCase`
- **DSA structures**: stay in `dsa_core.py` — `PriorityQueue`, `LinkedList`, `GreedyScheduler`
- **Routes**: stay in `app.py` — use Flask decorators (`@app.get`, `@app.post`, `@app.patch`, `@app.delete`)
- **Type hints**: use them on function signatures where the original code already does
- **Persistence**: call `save_patients()` after any mutation (already wired through `log()`)

### Frontend (`frontend/`)
- Everything stays in `index.html` — one file, no build step, no framework
- **CSS**: use the existing CSS custom properties (`--teal`, `--coral`, `--amber`, etc.) — don't hardcode hex values
- **JS**: vanilla JS only, no jQuery, no React; use `fetch()` for API calls
- **Styling patterns**: use existing classes (`btn`, `btn-primary`, `card`, `badge-*`, `stat-card`) before writing new ones
- **API base URLs**: Python API is `http://localhost:5000`, Java API is `http://localhost:8080` — defined at the top of the script section

### Naming (cross-cutting)
| Thing | Convention |
|---|---|
| Java classes | `PascalCase` |
| Java methods/fields | `camelCase` |
| Python functions/vars | `snake_case` |
| Python classes | `PascalCase` |
| API endpoints | `kebab-case` (`/call-next`, `/java-stats`) |
| CSS classes | `kebab-case` (`btn-primary`, `stat-card`) |
| JSON keys (API) | `camelCase` (`patientName`, `doctorId`) |

---

## Adding Things

### New Java endpoint
1. Create model in `model/` with a `toJson()` method
2. Create repository in `repository/` extending `Repository<T>`
3. Register it in `DataRegistry`
4. Create controller in `controller/` extending `BaseHandler`
5. Register the route in `Main.java`
6. Add the new `.java` files to `run.bat`

### New Python route
1. Add the route function to `app.py` with a Flask decorator
2. If it needs a new data structure, add it to `dsa_core.py`

### New frontend section
1. Add a nav item in the sidebar
2. Add a `<div class="page" id="page-name">` section
3. Wire it up in the `showPage()` JS function
