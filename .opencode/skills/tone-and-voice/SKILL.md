---
name: tone-and-voice
description: Use when writing or editing user-facing text in the Waypoint app — labels, descriptions, placeholders, status messages, tooltips, and empty states. Covers the capability-focused, empowering tone the app uses.
---

# Tone & Voice: Waypoint

Waypoint uses a **capability-focused, empowering tone**. The app never tells users what to do — it tells them what they're able to do.

## Core Principle

Write about **what the user can do with the app**, not what the app wants from them or what they must do.

| Instead of this (imperative) | Use this (capability-focused) |
|---|---|
| "Define your financial goal" | "Set the financial goal you're working toward" |
| "You'll break it into milestones" | "You'll be able to break it into milestones" |
| "Enter a valid goal title" | "Goal title is required" |
| "Add a deposit to get started" | "You can add deposits to build your balance" |
| "Create a goal first" | "You'll need a goal before adding milestones" |

## Guidelines

### 1. Describe capabilities, not instructions
Frame every message as what's possible or available, not as a command or requirement.

### 2. Use "you'll be able to" over "you'll"
This small shift changes the tone from predictive/instructional to empowering.

### 3. Keep it concise
Even with a softer tone, messages should be short — under 60 chars for status, under 3 lines for descriptions.

### 4. Empty states
Describe what the section is for and what the user can do there, rather than saying there's nothing there.
- **Good**: "No deposits yet. Add your first deposit to start tracking your balance."
- **Avoid**: "No deposits. Add a deposit."

### 5. Error messages
Explain the constraint, not the action.
- **Good**: "Deposit amount must be greater than 0."
- **Avoid**: "Enter a deposit amount greater than 0."

### 6. Confirmation dialogs
Present as a question, not a warning.
- **Good**: "Delete this milestone and all related history?"
- **Avoid**: "You must confirm to delete this milestone."

## Application Areas

This tone applies to:
- Modal descriptions (goal creation, milestone editing)
- Status messages (`#statusText`)
- Placeholder text in inputs
- Empty state messages
- Confirmation dialogs
- Tooltips and help text
- Button labels and aria-labels
