"""
MedQueue DSA Core
=================
Implements the three core data structures for the hospital queue system:
  1. PriorityQueue   — min-heap by (priority_level, arrival_time)
  2. LinkedList      — doubly-linked list of patient nodes
  3. GreedyScheduler — always dequeues the most urgent patient first
"""

import heapq
import time
from dataclasses import dataclass, field
from typing import Optional


# ── 1. PRIORITY QUEUE ────────────────────────────────────────────────────────
PRIORITY_ORDER = {"emergency": 0, "urgent": 1, "normal": 2}


@dataclass(order=True)
class QueueItem:
    """Heap-comparable item: sorted by (priority_level, arrival_time)."""
    priority_level: int
    arrival_time: float
    patient_id: str = field(compare=False)
    patient_name: str = field(compare=False)
    priority: str = field(compare=False)


class PriorityQueue:
    """
    Min-heap priority queue.
    Emergency (0) < Urgent (1) < Normal (2).
    Ties broken by FIFO arrival time.
    """
    def __init__(self):
        self._heap: list[QueueItem] = []
        self._index: dict[str, QueueItem] = {}   # id → item for O(1) lookup
        self._removed: set[str] = set()           # lazy-deletion set

    def push(self, patient_id: str, patient_name: str, priority: str) -> QueueItem:
        item = QueueItem(
            priority_level=PRIORITY_ORDER.get(priority, 2),
            arrival_time=time.time(),
            patient_id=patient_id,
            patient_name=patient_name,
            priority=priority,
        )
        heapq.heappush(self._heap, item)
        self._index[patient_id] = item
        return item

    def peek(self) -> Optional[QueueItem]:
        """Return highest-priority item without removing."""
        self._clean()
        return self._heap[0] if self._heap else None

    def pop(self) -> Optional[QueueItem]:
        """Remove and return highest-priority item (greedy choice)."""
        while self._heap:
            item = heapq.heappop(self._heap)
            if item.patient_id not in self._removed:
                self._index.pop(item.patient_id, None)
                return item
        return None

    def remove(self, patient_id: str) -> bool:
        """Lazy-delete a patient (e.g. discharged, walked out)."""
        if patient_id in self._index:
            self._removed.add(patient_id)
            self._index.pop(patient_id, None)
            return True
        return False

    def update_priority(self, patient_id: str, new_priority: str) -> bool:
        """Re-insert with new priority (escalate/de-escalate)."""
        if patient_id not in self._index:
            return False
        old = self._index[patient_id]
        self.remove(patient_id)
        self.push(patient_id, old.patient_name, new_priority)
        return True

    def sorted_list(self) -> list[QueueItem]:
        """Return all active items in priority order (non-destructive)."""
        self._clean()
        return sorted(
            [item for item in self._heap if item.patient_id not in self._removed]
        )

    def size(self) -> int:
        self._clean()
        return len([i for i in self._heap if i.patient_id not in self._removed])

    def _clean(self):
        """Remove lazily-deleted items from top of heap."""
        while self._heap and self._heap[0].patient_id in self._removed:
            item = heapq.heappop(self._heap)
            self._removed.discard(item.patient_id)


# ── 2. LINKED LIST ───────────────────────────────────────────────────────────

class PatientNode:
    """Doubly-linked list node holding full patient record."""
    def __init__(self, patient_id: str, data: dict):
        self.patient_id = patient_id
        self.data = data                # mutable dict — update in place
        self.prev: Optional["PatientNode"] = None
        self.next: Optional["PatientNode"] = None


class LinkedList:
    """
    Doubly-linked list of patient records.
    - O(1) insert at head/tail
    - O(1) delete by node reference (via index)
    - O(n) traversal for search
    Used to maintain the full ordered patient registry.
    """
    def __init__(self):
        self.head: Optional[PatientNode] = None
        self.tail: Optional[PatientNode] = None
        self._index: dict[str, PatientNode] = {}
        self._size = 0

    def append(self, patient_id: str, data: dict) -> PatientNode:
        """Add patient record to tail — O(1)."""
        node = PatientNode(patient_id, data)
        if self.tail:
            self.tail.next = node
            node.prev = self.tail
            self.tail = node
        else:
            self.head = self.tail = node
        self._index[patient_id] = node
        self._size += 1
        return node

    def prepend(self, patient_id: str, data: dict) -> PatientNode:
        """Add patient record to head (emergency fast-track) — O(1)."""
        node = PatientNode(patient_id, data)
        if self.head:
            node.next = self.head
            self.head.prev = node
            self.head = node
        else:
            self.head = self.tail = node
        self._index[patient_id] = node
        self._size += 1
        return node

    def find(self, patient_id: str) -> Optional[PatientNode]:
        """Find by ID — O(1) via index."""
        return self._index.get(patient_id)

    def update(self, patient_id: str, updates: dict) -> bool:
        """Update patient data in place — O(1)."""
        node = self._index.get(patient_id)
        if not node:
            return False
        node.data.update(updates)
        return True

    def delete(self, patient_id: str) -> Optional[dict]:
        """Unlink and remove node — O(1)."""
        node = self._index.pop(patient_id, None)
        if not node:
            return None
        if node.prev:
            node.prev.next = node.next
        else:
            self.head = node.next
        if node.next:
            node.next.prev = node.prev
        else:
            self.tail = node.prev
        self._size -= 1
        return node.data

    def to_list(self) -> list[dict]:
        """Traverse and return all records — O(n)."""
        result, cur = [], self.head
        while cur:
            result.append({"id": cur.patient_id, **cur.data})
            cur = cur.next
        return result

    def search(self, query: str) -> list[dict]:
        """Linear search by name or complaint — O(n)."""
        q = query.lower()
        result, cur = [], self.head
        while cur:
            if q in cur.data.get("name", "").lower() or \
               q in cur.data.get("complaint", "").lower():
                result.append({"id": cur.patient_id, **cur.data})
            cur = cur.next
        return result

    def __len__(self) -> int:
        return self._size


# ── 3. GREEDY SCHEDULER ──────────────────────────────────────────────────────

class GreedyScheduler:
    """
    Greedy scheduling algorithm.

    Greedy choice: at every scheduling step, always serve the patient with
    the highest priority (lowest priority_level integer).  Within the same
    priority tier, serve by FIFO arrival order.

    This is provably optimal for minimising the maximum waiting time of
    critical (emergency) patients — the greedy proof:
      Suppose we swap a lower-priority patient ahead of an emergency patient.
      The emergency patient's wait increases, making the schedule worse.
      Therefore the greedy order is always optimal.
    """
    def __init__(self, queue: PriorityQueue, registry: LinkedList):
        self.queue = queue
        self.registry = registry

    def call_next(self) -> Optional[dict]:
        """
        Greedy dequeue: pop highest-priority patient, update registry.
        Returns the full patient record or None if queue empty.
        """
        item = self.queue.pop()
        if not item:
            return None
        self.registry.update(item.patient_id, {
            "status": "seen",
            "called_at": time.strftime("%Y-%m-%dT%H:%M:%S")
        })
        node = self.registry.find(item.patient_id)
        return {"id": item.patient_id, **(node.data if node else {})}

    def estimate_wait(self, patient_id: str) -> int:
        """
        Estimate wait time in minutes for a given patient.
        Counts patients ahead in the queue × average consultation time (10 min).
        """
        AVG_CONSULT_MINS = 10
        sorted_q = self.queue.sorted_list()
        for pos, item in enumerate(sorted_q):
            if item.patient_id == patient_id:
                return pos * AVG_CONSULT_MINS
        return -1  # not in queue

    def queue_summary(self) -> dict:
        """Return stats snapshot."""
        items = self.queue.sorted_list()
        return {
            "total": len(items),
            "emergency": sum(1 for i in items if i.priority == "emergency"),
            "urgent":    sum(1 for i in items if i.priority == "urgent"),
            "normal":    sum(1 for i in items if i.priority == "normal"),
            "next": {
                "id": items[0].patient_id,
                "name": items[0].patient_name,
                "priority": items[0].priority,
            } if items else None,
        }
