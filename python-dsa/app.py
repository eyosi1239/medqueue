"""
MedQueue Python API — Port 5000
================================
Handles the patient queue using the DSA core:
  - PriorityQueue  (min-heap)
  - LinkedList     (patient registry)
  - GreedyScheduler (call-next)
"""

import json
import os
import time
import uuid
from datetime import datetime
from flask import Flask, request, jsonify, Response

from dsa_core import PriorityQueue, LinkedList, GreedyScheduler, PRIORITY_ORDER

app = Flask(__name__)

# ── CORS (manual, no flask-cors needed) ─────────────────────
@app.after_request
def add_cors(response):
    response.headers["Access-Control-Allow-Origin"]  = "*"
    response.headers["Access-Control-Allow-Headers"] = "Content-Type,Authorization"
    response.headers["Access-Control-Allow-Methods"] = "GET,POST,PATCH,DELETE,OPTIONS"
    return response

@app.route("/<path:p>", methods=["OPTIONS"])
@app.route("/", methods=["OPTIONS"])
def options_handler(p=""):
    return jsonify({}), 200

# ── In-memory DSA structures ─────────────────────────────────
pq       = PriorityQueue()
registry = LinkedList()
scheduler = GreedyScheduler(pq, registry)

# Audit log (simple list)
audit_log: list[dict] = []

# Persistence file
DATA_FILE = os.path.join(os.path.dirname(__file__), "patients.json")

def now_iso():
    return datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

def log(action: str, patient_id: str = None, details: dict = None):
    audit_log.insert(0, {
        "id": str(uuid.uuid4())[:8],
        "action": action,
        "patient_id": patient_id,
        "details": details or {},
        "timestamp": now_iso(),
    })
    if len(audit_log) > 300:
        audit_log.pop()
    save_patients()

def save_patients():
    patients = registry.to_list()
    with open(DATA_FILE, "w") as f:
        json.dump({"patients": patients, "audit": audit_log[:100]}, f, indent=2)

def load_patients():
    if not os.path.exists(DATA_FILE):
        return
    try:
        with open(DATA_FILE) as f:
            data = json.load(f)
        for p in data.get("patients", []):
            pid = p.pop("id")
            registry.append(pid, p)
            # Only re-queue waiting patients
            if p.get("status") == "waiting":
                pq.push(pid, p.get("name", ""), p.get("priority", "normal"))
        audit_log.extend(data.get("audit", []))
    except Exception as e:
        print(f"[WARN] Could not load patients: {e}")

load_patients()

# ── HELPERS ──────────────────────────────────────────────────
def patient_to_dict(pid: str) -> dict | None:
    node = registry.find(pid)
    if not node:
        return None
    return {"id": pid, **node.data}

def serialize_queue() -> list[dict]:
    items = pq.sorted_list()
    result = []
    for pos, item in enumerate(items):
        node = registry.find(item.patient_id)
        if node and node.data.get("status") == "waiting":
            result.append({
                "position": pos + 1,
                "id": item.patient_id,
                "estimated_wait_mins": pos * 10,
                **node.data,
            })
    return result

# ════════════════════════════════════════════════════════════
# ROUTES
# ════════════════════════════════════════════════════════════

# ── GET /api/patients?status=waiting|seen|dismissed ──────────
@app.get("/api/patients")
def get_patients():
    status = request.args.get("status", "waiting")
    if status == "waiting":
        return jsonify(serialize_queue())
    # For non-waiting, traverse linked list and filter
    all_p = registry.to_list()
    filtered = [p for p in all_p if p.get("status") == status]
    filtered.sort(key=lambda p: p.get("updated_at", ""), reverse=True)
    return jsonify(filtered)

# ── GET /api/patients/search?q= ──────────────────────────────
@app.get("/api/patients/search")
def search_patients():
    q = request.args.get("q", "")
    results = registry.search(q) if q else registry.to_list()
    return jsonify(results)

# ── GET /api/patients/<id> ───────────────────────────────────
@app.get("/api/patients/<pid>")
def get_patient(pid):
    p = patient_to_dict(pid)
    if not p:
        return jsonify({"error": "Not found"}), 404
    return jsonify(p)

# ── POST /api/patients ───────────────────────────────────────
@app.post("/api/patients")
def create_patient():
    body = request.get_json(force=True)
    name      = (body.get("name") or "").strip()
    complaint = (body.get("complaint") or "").strip()
    priority  = body.get("priority", "normal")

    if not name or not complaint:
        return jsonify({"error": "name and complaint are required"}), 400
    if priority not in PRIORITY_ORDER:
        return jsonify({"error": "priority must be emergency, urgent, or normal"}), 400

    pid = str(uuid.uuid4())
    data = {
        "name":        name,
        "complaint":   complaint,
        "priority":    priority,
        "age":         body.get("age"),
        "gender":      body.get("gender"),
        "phone":       body.get("phone"),
        "blood_type":  body.get("bloodType"),
        "allergies":   body.get("allergies"),
        "doctor":      body.get("doctor") or "Auto-assigned",
        "notes":       body.get("notes"),
        "status":      "waiting",
        "created_at":  now_iso(),
        "updated_at":  now_iso(),
    }

    # Use prepend for emergency (fast-track to head of linked list)
    if priority == "emergency":
        registry.prepend(pid, data)
    else:
        registry.append(pid, data)

    pq.push(pid, name, priority)
    log("REGISTER_PATIENT", pid, {"name": name, "priority": priority})
    return jsonify({"id": pid, **data}), 201

# ── PATCH /api/patients/<id> ─────────────────────────────────
@app.patch("/api/patients/<pid>")
def update_patient(pid):
    node = registry.find(pid)
    if not node:
        return jsonify({"error": "Not found"}), 404

    body = request.get_json(force=True)
    allowed = ["status", "priority", "doctor", "complaint", "notes", "phone", "blood_type", "allergies"]
    updates = {k: v for k, v in body.items() if k in allowed}
    updates["updated_at"] = now_iso()

    # If priority changed, update the heap
    if "priority" in updates:
        pq.update_priority(pid, updates["priority"])

    registry.update(pid, updates)
    log("UPDATE_PATIENT", pid, updates)
    return jsonify(patient_to_dict(pid))

# ── DELETE /api/patients/<id> (dismiss) ──────────────────────
@app.delete("/api/patients/<pid>")
def dismiss_patient(pid):
    node = registry.find(pid)
    if not node:
        return jsonify({"error": "Not found"}), 404
    pq.remove(pid)
    registry.update(pid, {"status": "dismissed", "updated_at": now_iso()})
    log("DISMISS_PATIENT", pid, {"name": node.data.get("name")})
    return jsonify({"message": "Dismissed", "id": pid})

# ── POST /api/patients/call-next (Greedy Scheduler) ──────────
@app.post("/api/patients/call-next")
def call_next():
    result = scheduler.call_next()
    if not result:
        return jsonify({"error": "Queue is empty"}), 404
    log("CALL_NEXT", result["id"], {"name": result.get("name"), "priority": result.get("priority")})
    return jsonify({"message": f"Calling {result.get('name')}", "patient": result})

# ── GET /api/stats ───────────────────────────────────────────
@app.get("/api/stats")
def get_stats():
    summary = scheduler.queue_summary()
    all_p = registry.to_list()
    seen = sum(1 for p in all_p if p.get("status") == "seen")
    return jsonify({
        "waiting":    summary["total"],
        "emergency":  summary["emergency"],
        "urgent":     summary["urgent"],
        "normal":     summary["normal"],
        "seen":       seen,
        "next":       summary["next"],
        "avg_wait_mins": summary["total"] * 10,
    })

# ── GET /api/queue/position/<id> ─────────────────────────────
@app.get("/api/queue/position/<pid>")
def queue_position(pid):
    wait = scheduler.estimate_wait(pid)
    if wait < 0:
        return jsonify({"error": "Patient not in queue"}), 404
    items = pq.sorted_list()
    pos = next((i+1 for i,x in enumerate(items) if x.patient_id == pid), None)
    return jsonify({"patient_id": pid, "position": pos, "estimated_wait_mins": wait})

# ── GET /api/audit ───────────────────────────────────────────
@app.get("/api/audit")
def get_audit():
    return jsonify(audit_log[:50])

# ── Health check ─────────────────────────────────────────────
@app.get("/api/health")
def health():
    return jsonify({"service": "python-dsa", "status": "ok", "port": 5000})

if __name__ == "__main__":
    print("✅ Python DSA API running at http://localhost:5000")
    print("   Structures: PriorityQueue (min-heap), LinkedList, GreedyScheduler")
    app.run(port=5000, debug=False)
