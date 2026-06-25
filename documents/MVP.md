# Waypoint MVP — Complete Functional Specification

This document describes **every feature, interaction, and data flow** implemented in the Waypoint MVP (v48). Use it as the single source of truth when rewriting the application.

---

## 1. Application Overview

Waypoint is a single-page, offline-capable PWA for tracking progress toward a financial goal broken down into milestones. Users create goals, fund them with deposits, allocate money to specific milestones, mark milestones complete, and visualize overall progress.

### Architecture

| Layer | Implementation |
|---|---|
| UI | Vanilla HTML + DOM manipulation (no framework) |
| Styling | Vanilla CSS with custom properties (dark amber theme) |
| State | Mutable store object, IIFE module pattern on `window.WaypointApp` |
| Persistence | `localStorage` (primary) + optional Firebase Firestore |
| PWA | Cache-first service worker + Web App Manifest |
| i18n | JSON locale files fetched at runtime, `data-i18n` attribute binding |

### Bootstrap Order (index.html)

1. `config.js` — App config, default state shape, available locales/currencies
2. `utils.js` — `uid()`, `nowIso()`, `slugify()`, `clamp()`
3. `formatters.js` — `formatMoney()`, `formatDate()`, `formatPercent()` (locale-aware via `Intl`)
4. `i18n.js` — Internationalization engine, locale loading, settings persistence
5. `storage.js` — localStorage adapter + optional Firebase Firestore adapter
6. `calculations.js` — `getGoalAnalytics()` (all derived state)
7. `state.js` — `createStore()` (mutable state + persistence + all mutations)
8. `ui.js` — DOM rendering + event binding
9. `app.js` — Bootstrap, wires store to UI, registers SW

---

## 2. Data Model

### State Shape

```typescript
interface State {
  version: number;          // Schema version (currently 1)
  activeGoalId: string;     // ID of the currently active goal
  goals: Goal[];
}

interface Goal {
  id: string;
  icon: string;             // Always "target"
  title: string;
  description: string;
  targetAmount: number;     // Sum of enabled milestone costs
  milestones: Milestone[];
  deposits: Deposit[];
  completions: Completion[];
  transfers: Transfer[];
  createdAt: string;        // ISO 8601
}

interface Milestone {
  id: string;
  title: string;
  cost: number;
  details: string;          // Optional notes
  completed: boolean;
  completedAt: string | null;
  enabled: boolean;         // Can be toggled off to exclude from target
}

interface Deposit {
  id: string;
  amount: number;
  timestamp: string;        // ISO 8601
}

interface Completion {
  id: string;
  milestoneId: string;
  amount: number;
  timestamp: string;
}

interface Transfer {
  id: string;
  milestoneId: string;
  amount: number;           // Positive = allocate, Negative = withdraw
  timestamp: string;
  type: "allocate" | "withdraw" | "legacy_completion";
  comment: string;          // Optional note
}
```

### Persistence

- Primary: `localStorage` key `"waypoint_v1"`
- Settings (locale + currency): `localStorage` key `"waypoint_settings"`
- Firebase config: `localStorage` key `"waypoint_firebase_config"`
- Merge flag: `localStorage` key `"waypoint_merged_v1"` (tracks one-time Firebase import)
- `storage.js` creates either a `localStorage` adapter or a Firebase adapter (which also saves locally)

---

## 3. UI Layout & Sections

### 3.1 Hero Card (Header Section)

**Elements:**
- Brand label ("Waypoint" in purple accent)
- Hamburger menu button (☰) — opens goal selector dropdown
- Edit goal button (pencil icon) — opens goal edit modal
- Settings button (gear icon) — opens settings modal
- Hero tagline / goal title (uppercase)
- Active goal title (below tagline)
- Total target amount (with warning icon if milestones are excluded)
- Progress bar (amber fill)
- Remaining amount + completion percentage

**Behavior:**
- If no goal exists, tagline shows "Every big goal starts with the next step." and "Set a goal to start tracking"
- Warning icon appears next to target when some milestones are disabled

### 3.2 Goal Dropdown

**Elements:**
- List of all goals with active state highlighted
- Edit button per goal (opens goal modal in edit mode)
- "New Goal" button at bottom

**Behavior:**
- Click a goal → switches active goal, re-renders entire UI
- Click edit icon on a goal → switches to that goal AND opens edit modal
- Click outside dropdown → closes it
- Opened via hamburger button (aria-expanded toggled)

### 3.3 Wallet Panel

**Elements:**
- Current balance display (formatted money)
- Deposit form: number input + "Add Deposit" button (disabled when empty/invalid)
- "Savings Journal" accordion (`<details>`)

**Behavior:**
- Balance = sum of all deposits − sum of all transfers (net allocated)
- Submit deposit → pushes to `goal.deposits` array with timestamp + auto-generated ID
- Savings Journal lists deposits in reverse chronological order
- Each deposit row has Edit and Delete buttons
- Edit opens a `prompt()` dialog to change amount
- Delete removes deposit after `confirm()` dialog

### 3.4 Milestones Section

**Elements:**
- Select/deselect all toggle (checkbox icon, cycles all-enabled / mixed / all-disabled)
- "Add Milestone" toggle button
- Add milestone form (initially hidden): title, cost, details (optional), submit
- Milestone list (ordered array, reorderable via drag-and-drop)

**Per-Milestone Row:**
- Drag handle (6 dots) — draggable for reorder (mouse + touch)
- Enable/disable toggle (amber checkbox)
- Milestone title (with optional "i" details tooltip)
- Status pill: ✅ Completed / 🟢 Ready / 🟡 Next / ⚪ Future
- Cost display (editable inline via pencil icon)
- Action buttons: Edit, Mark Complete, Delete
- Transfer row: number input + Add button + Withdraw button
- Transfer note input (optional text field)
- Progress bar showing allocated vs. target
- "Milestone Journal" accordion showing per-transfer history

**Status Logic:**
- Completed — if `milestone.completed === true`
- Ready — not completed, fully funded (allocated >= cost)
- Next — the first pending milestone that isn't fully funded
- Future — all remaining pending milestones

**Milestone Mutations:**
- Add: form submit, title + cost required
- Edit: opens milestone modal (title, cost, details)
- Delete: removes milestone + all associated completions + transfers (with confirm)
- Toggle enabled: excludes/includes milestone from target total calculation
- Toggle all: cycles all-enabled → all-disabled → all-enabled
- Inline cost edit: pencil icon → inline input → save/cancel
- Mark complete: sets `completed: true`, pushes to completions journal, disables the complete button
- Reorder: drag-and-drop (mouse: dragstart/dragover/drop, touch: touchstart/touchmove/touchend)

**Transfer System:**
- Allocate: user enters amount + optional note, clicks "Add" → transfers money from wallet to milestone
  - Validated against: wallet balance, milestone remaining need
  - If amount exceeds limits, partial allocation occurs with notification
  - If milestone already fully funded, allocation is rejected with status message
- Withdraw: user enters amount + optional note, clicks "Withdraw" → moves money back to wallet
  - Validated against: milestone's current allocated balance
  - Creates a transfer with negative amount
- Transfer input shows tooltip on validation errors (exceeds wallet, exceeds milestone remaining)
- Allocate/Withdraw buttons disable/enable based on input validity in real time

### 3.5 Completion Journal

**Elements:**
- Section heading "Completion Journal"
- Reverse-chronological list of completed milestones

**Behavior:**
- Each entry shows: milestone name + formatted amount · date
- Undo button reverses the completion:
  - Removes completion entry
  - Sets `milestone.completed = false`, `milestone.completedAt = null`
  - Does NOT remove transfers (funds stay allocated)

### 3.6 Status Bar

- Bottom of main content, `aria-live="polite"`
- Displays feedback messages for all operations (see `en.json` `status.*` and `errors.*`)
- Initial: "Loading..." → Ready

### 3.7 Toast Notifications

- Transient overlay toast (auto-dismisses after 2500ms)
- Used for Firebase sync status (start/end/error)
- Shows with amber dot + message
- Smooth entry/exit animation

---

## 4. Modals

### 4.1 Goal Modal (Create / Edit)

- Triggered by: "New Goal" button, edit icon in hero, edit button in goal dropdown
- Title dynamically reads "Create Goal" or "Edit Goal"
- Fields: Title (required), Description
- On first load with no goals, opens automatically in create mode
- Submit calls `createGoal()` or `updateGoalMeta()` depending on mode
- Close via X button, backdrop click, or form submit
- `uiState.creatingGoal` flag tracks mode

### 4.2 Milestone Modal (Edit)

- Triggered by: Edit button on milestone row
- Fields: Title (required), Cost (required), Details (optional)
- Hidden input stores milestone ID
- Submit calls `updateMilestone()` → re-renders

### 4.3 Settings Modal

- Triggered by: Gear icon in hero card
- **Language**: Dropdown populated from `config.availableLocales` (en, uk)
- **Currency**: Dropdown populated from `config.availableCurrencies` (USD, EUR, UAH, GBP, JPY, CAD, AUD, CHF, PLN, SEK, NOK, CZK, HUF, RON, BGN, ISK)
- **Backup section**:
  - Export JSON: downloads `waypoint-backup-YYYY-MM-DD.json` with full state
  - Import JSON: file picker → parse → overwrite state (with migration)
  - Reset Data: confirm dialog → wipe everything → start fresh
- **Firebase Sync section**:
  - Textarea to paste Firebase config JSON
  - "Save & Reload" button → validates config → stores in localStorage → reloads page
  - "Clear" button → removes config → reloads page
  - Firebase config must include `apiKey`, `projectId`, `appId` at minimum
- Close via X button, backdrop click, or form submit
- Locale and currency settings persist independently of goal data

---

## 5. Calculations & Derived State (`getGoalAnalytics()`)

For a given goal, returns:

| Field | Description |
|---|---|
| `depositsTotal` | Sum of all deposits |
| `completionsTotal` | Sum of all completion amounts |
| `allocatedTotal` | Sum of all transfer amounts (allocated to milestones) |
| `balance` | `depositsTotal - allocatedTotal` (money in wallet) |
| `targetAmount` | Sum of costs of all enabled milestones |
| `remainingAmount` | Money still needed: `max(0, pendingNeeded - balance)` |
| `completionRatio` | `clamp((completedAmount + allocatedTotal + max(balance, 0)) / targetAmount, 0, 1)` |
| `completionPercent` | `Math.round(completionRatio * 100)` |
| `fundedMilestonesCount` | Milestones with allocated >= cost |
| `totalMilestonesCount` | Total milestone count |
| `averageCoverageRatio` | Mean funding ratio across all milestones |
| `nextMilestone` | The first pending non-funded milestone (with balance, remaining, progress) |
| `milestones` | Array of `{ milestone, milestoneBalance, status, icon, tone, amountRemaining, unlockRatio }` |

---

## 6. Store Methods (`state.js`)

| Method | Description |
|---|---|
| `init()` | Load state from storage, mark initialized, emit to listeners |
| `getState()` | Return current state object |
| `getActiveGoal()` | Return the currently active goal (or first goal) |
| `switchGoal(id)` | Change `activeGoalId`, persist |
| `createGoal(payload)` | Create new goal with title + description, set as active |
| `updateGoalMeta(payload)` | Update active goal's title + description |
| `addMilestone(payload)` | Add milestone to active goal |
| `deleteMilestone(id)` | Remove milestone + associated completions + transfers |
| `updateMilestoneCost(id, cost)` | Inline cost edit |
| `updateMilestone(id, payload)` | Full milestone edit (modal) |
| `markMilestoneCompleted(id)` | Mark completed, push completion entry, timestamp |
| `transferToMilestone(id, amount, comment)` | Allocate wallet funds to milestone (with validation) |
| `withdrawFromMilestone(id, amount, comment)` | Move funds back from milestone to wallet |
| `toggleMilestoneEnabled(id)` | Toggle milestone enabled/disabled |
| `toggleAllMilestones(enabled)` | Set all milestones to enabled/disabled |
| `reorderMilestones(sourceId, targetId)` | Reorder via splice |
| `addDeposit(amount)` | Add a deposit entry |
| `updateDeposit(id, amount)` | Edit a deposit amount |
| `deleteDeposit(id)` | Remove a deposit |
| `updateTransfer(id, amount)` | Edit a transfer amount |
| `deleteTransfer(id)` | Remove a transfer |
| `undoCompletion(id)` | Undo a milestone completion (removes entry, sets completed=false) |
| `exportJson()` | Stringify current state |
| `importJson(raw)` | Parse JSON, migrate, persist |
| `resetAll()` | Reset to default state |
| `subscribe(listener)` | Register render listener (returns unsubscribe) |

---

## 7. Firebase Integration

- Optional; disabled by default (requires config paste)
- Anonymous authentication via `firebase.auth().signInAnonymously()`
- Single document per user: `{collection}/{documentId}`
- On first load with Firebase configured:
  1. Check if Firestore doc exists
  2. If not → push local state to Firestore
  3. If exists but no merge flag → merge local + remote (`mergeStates()`) with conflict renaming (" (imported)" suffix)
  4. If already merged → remote is source of truth, override local
- Save: writes to both Firestore AND localStorage (dual write)
- Firebase activity triggers CustomEvent `"waypoint:firebase-activity"` → toast notifications
- `config.firebase.useAnonymousAuth` can be set to `false` for unauthenticated access

---

## 8. PWA / Service Worker

- **Cache-first strategy**: `caches.match()` → fallback to `fetch()`
- **CACHE_KEY**: `"waypoint-static-v48"` (must be bumped on any asset change)
- **Precached assets** (18 files): index.html, styles.css, all JS files, manifest, locales, icon.svg
- On install: cache all assets, `skipWaiting()`
- On activate: delete old caches, `clients.claim()`
- Offline support: all static assets serve from cache; dynamic data fails gracefully
- Web Manifest: `display: standalone`, SVG icon (512x512, maskable), theme color `#101216`

---

## 9. Internationalization

**Supported locales**: `en` (English), `uk` (Ukrainian)

**Files**: `assets/locales/en.json`, `assets/locales/uk.json`

**Mechanism**:
- Locale files fetched via `fetch()` on demand, cached in `i18n.cache`
- HTML attributes: `data-i18n` (text), `data-i18n-placeholder` (placeholder), `data-i18n-aria` (aria-label), `data-i18n-title` (title), `data-i18n-value` (value)
- Template interpolation: `{key}` placeholders replaced at runtime
- Fallback chain: current locale → `en` → raw key string
- Auto-detection: browser's `navigator.language` checked on first load (`uk` → Ukrainian, everything else → English)
- Settings override persisted in `localStorage` key `"waypoint_settings"`
- Currency formatting uses `Intl.NumberFormat` with locale and currency from settings
- `document.documentElement.lang` set on locale change

---

## 10. Design System

| Token | Value |
|---|---|
| Background | `#0A0A0F` |
| Surface | `rgba(26,26,36,0.6)` with `backdrop-filter: blur(8px)` |
| Accent | `#F59E0B` (amber) |
| Text | `#FAFAFA` primary, `#71717A` muted |
| Borders | `rgba(255,255,255,0.08)`, hover `rgba(255,255,255,0.15)` |
| Radius | 8px buttons/inputs, 12px panels, 16px hero |
| Fonts | Space Grotesk (headings/metrics), Inter (body), JetBrains Mono (labels) |
| Background | Radial amber glows (top-left + bottom-right) + dotted SVG noise |

**All corners are rounded. No pure black. No gradient text.**

---

## 11. Edge Cases & Constraints

- **No goal exists on load**: Create-goal modal opens automatically
- **Milestone cost = 0**: Allowed. Marking complete still works, progress bar handles it
- **Negative amounts**: Deposit validation rejects `<= 0`. Transfer validation rejects `<= 0`
- **All milestones disabled**: Target shows 0, warning icon appears, progress shows 100% (division by zero guard)
- **Duplicate names**: Milestone IDs generated via `slugify(title)` with numeric suffix deduplication
- **Data migration**: `migrateState()` normalizes legacy schema on every load (handles missing fields, old field names, date formats)
- **Empty states**: Each list shows contextual empty message ("No milestones yet...", "No deposits yet...", etc.)
- **Mobile**: Touch-based drag-and-drop, tooltip workaround for `title` attribute, responsive layout
- **Concurrent Firebase + local edits**: Merge strategy favors Firebase, renames conflicting goals with "(imported)" suffix

---

## 12. Version Bumping Protocol

When any cached asset changes, these files must be updated with matching version numbers:

1. `index.html` — `?v=` query params on all asset references (currently `48`)
2. `assets/styles.css` — Same `?v=` param in index.html
3. `waypoint-sw.js` — `CACHE_KEY` constant (currently `"waypoint-static-v48"`)
4. `assets/manifest.webmanifest` — No version field, but `start_url` points to `index.html`
