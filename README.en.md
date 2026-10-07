# vibe-lishiyin

> **Who I am**: Li Shiying
> **Study Group**: Group 9
> **Goal of this course**: Ship independently with an AI pair — from an idea to a running full-stack mini system, and be able to explain every single AI-made change.

## Project: Teaching Equipment Reservation System (Day1 · Lab 1)

Turn "who reserved the equipment first" from a group-chat quarrel into an HTTP 409. The same equipment + the same date + the same time slot can only be booked once; the second submission is rejected on the spot with a concrete conflict reason.

### Core Features (Exactly Three)

1. **Browse** equipment cards (Projector / Speaker / Wireless Microphone) with usage notes
2. **Submit** a reservation and let the system decide: pick a date + period (AM/PM); a duplicate slot returns 409 with a persistent message like "Projector on 2026-10-08 AM has already been reserved, please pick another period"
3. **Query** my reservation history: enter a name and see the list (equipment / date / period / upcoming or finished / submitted-at), with a live success counter

### Tech Stack

| Layer | Choice | Notes |
|-------|--------|-------|
| Frontend | Vue3 + Element Plus + Vite | Component-based, `/api` proxy to backend |
| Backend | SpringBoot 3 + Spring Data JPA | Layered: Controller / Service / Repository |
| Database | H2 file storage | `UNIQUE(device_id, date, period)` index guards concurrent conflicts |

Three layers of conflict protection: Service pre-check → database unique index → global exception handler returning 409 with the reason.

### Quick Start (one command each)

```
run-backend.cmd     # backend on :8080, ready when "Started ReservationApplication" appears
run-frontend.cmd    # frontend on :5173, open http://localhost:5173
```

Health check: `http://localhost:8080/api/ping` returns `{"status":"ok"}`.

### Repository Layout

```
backend/                 SpringBoot backend (source + pom.xml)
frontend/                Vue3 frontend (source + vite.config.js)
docs/Reflections/       daily reflection notes (process archive)
想法电梯稿.md            T3 one-page elevator pitch
pitch.html               C1 challenge: accessible single-page pitch site
交作业自查.py            daily self-check script (four green lights)
```
