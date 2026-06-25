# Waypoint — Additional Functionality Ideas

This document catalogs potential features beyond the MVP scope. Each idea is categorized and described at a high level for review and prioritization.

---

## 1. Authentication & Users

### Social Login
- **Description**: Sign in with Google, Apple, or GitHub in addition to email/password
- **Why**: Lowers friction, especially on mobile where platform auth is expected (Apple Sign-In for iOS, Google for Android)
- **Effort**: Medium — standard OAuth2 integration on backend and with platform SDKs

### Profile & Avatar
- **Description**: Display name, avatar upload, timezone, preferred locale/currency in user profile
- **Why**: Personalization, improves UX for multi-region users
- **Effort**: Low

### Account Deletion
- **Description**: GDPR-compliant self-service account deletion with data wipe
- **Why**: Legal requirement for production apps
- **Effort**: Low

---

## 2. Goal Enhancements

### Goal Templates
- **Description**: Pre-built templates (e.g., "Emergency Fund — 6 months expenses", "New Laptop", "Vacation to Japan") with suggested milestones and amounts
- **Why**: Reduces setup friction, educates new users on how to structure goals
- **Effort**: Medium — create template system in DB + UI gallery

### Time-Bound Goals (Target Date)
- **Description**: Optional target completion date per goal. Progress bar shows pace (on-track / behind / ahead)
- **Why**: Adds urgency, enables projections, answers "will I make it on time?"
- **Effort**: Medium

  ```
  Projected completion: December 2026 (on track)
  ───▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰░░░░░░░░░───
  Time elapsed: 40%  |  Savings progress: 55%
  ```

### Goal Categories / Tags
- **Description**: Group goals by category (Savings, Debt, Investment, Purchase) with optional color coding
- **Why**: Organizational layer for users with many goals
- **Effort**: Low

### Goal Archiving
- **Description**: Archive completed or abandoned goals instead of deleting them. Keep history visible in an "Archived" section
- **Why**: Preserve accomplishment records without cluttering active list
- **Effort**: Low

### Goal Sharing (Collaborative)
- **Description**: Invite other users to contribute to a shared goal (couples, roommates, families). Each contributor sees their own deposits + shared progress
- **Why**: High-value social feature. Enables use cases like "Saving for a wedding" or "Group trip fund"
- **Effort**: High — permissions, real-time sync, conflict resolution
- **Architecture note**: Add `goal_members` join table with roles (owner/contributor/viewer)

### Public Goal Profiles
- **Description**: Opt-in public goal page (e.g., `waypoint.app/@username/goal-id`) for accountability with friends/followers
- **Why**: Motivation through social accountability, potential viral growth
- **Effort**: Medium

---

## 3. Deposits & Funding

### Recurring / Scheduled Deposits
- **Description**: Set up recurring deposits (daily, weekly, monthly) that auto-create deposit entries on schedule
- **Why**: Core behavior for consistent savers. MVP manual deposit entry is tedious for regular contributions
- **Effort**: High — requires job scheduler (Spring `@Scheduled` / cron), push notification reminders, or both

### Auto-Allocate Rules
- **Description**: Define rules like "When a deposit arrives, distribute X% to milestone A, Y% to milestone B". Funds are automatically allocated according to the rule set
- **Why**: Eliminates manual allocation step for users with predictable saving patterns
- **Effort**: Medium-high

### Deposit Notes / Memo
- **Description**: Free-text note per deposit (e.g., "Freelance payment", "Birthday money")
- **Why**: Adds context to savings history, improves journal usefulness
- **Effort**: Low

### Attachments / Receipts
- **Description**: Upload a photo or document per deposit (receipt, screenshot of transfer confirmation)
- **Why**: Audit trail, especially for shared or business goals
- **Effort**: Medium — file upload, storage (S3/Cloudflare R2), thumbnails

---

## 4. Milestones & Progress

### Milestone Dependencies
- **Description**: Require milestone A to be completed before milestone B can be funded. Visual dependency graph
- **Why**: Useful for sequential goals (e.g., "Pay off credit card" before "Save for house down payment")
- **Effort**: Medium-high — DAG management, validation on allocation

### Milestone Sub-Milestones
- **Description**: Nested breakdown of a milestone into smaller steps (with optional costs)
- **Why**: Decomposition for very large milestones, better granularity
- **Effort**: Medium

### Progress Snapshots / Timeline
- **Description**: Periodic snapshots of goal state (balance, completion %). Rendered as a timeline or sparkline chart showing progress over time
- **Why**: Visual motivation, answers "how far have I come?" beyond current state
- **Effort**: Medium — store periodic snapshots or derive from deposit/transfer history dates

### Milestone Due Dates
- **Description**: Per-milestone optional due date. Show overdue / upcoming indicators
- **Why**: More granular timeline tracking than just goal-level target date
- **Effort**: Low

---

## 5. Analytics & Insights

### Dashboard / Overview Page
- **Description**: Cross-goal summary page showing total saved across all goals, total targets, active vs achieved, recent activity feed
- **Why**: Birds-eye view for users with multiple goals. MVP only shows one goal at a time
- **Effort**: Medium

### Savings Velocity Chart
- **Description**: Line chart of cumulative deposits over time, with projection line toward goal target. Daily/weekly/monthly aggregation
- **Why**: Powerful motivation tool, answers "at this rate, I'll finish by date X"
- **Effort**: Medium

### Category Breakdown (Spending by Milestone)
- **Description**: Pie chart or stacked bar showing how allocated funds distribute across milestones
- **Why**: Visual understanding of fund allocation
- **Effort**: Low

### Streak Tracking
- **Description**: Track consecutive weeks/months with at least one deposit. Display current streak + longest streak
- **Why**: Gamification, habit formation
- **Effort**: Low

### Export Reports (PDF / CSV)
- **Description**: Generate a formatted report of goal progress (PDF for printing, CSV for spreadsheet analysis)
- **Why**: Users may need records for financial planning, sharing with advisors
- **Effort**: Medium — PDF generation library on backend (JasperReports / PDFBox)

---

## 6. Notifications

### Push Notifications
- **Description**: Daily/weekly reminders to add deposits. Alerts when a milestone is fully funded. Achievement celebrations
- **Why**: Re-engagement, habit formation. Essential for mobile apps
- **Effort**: Medium — FCM for Android, APNs for iOS, Web Push API for PWA

### Email Notifications
- **Description**: Weekly progress summary email. Goal completion celebration. Inactivity reminders
- **Why**: Re-engagement channel beyond push
- **Effort**: Medium — Spring Mail + background job + email template

### Milestone Celebration Animation
- **Description**: When a milestone is marked complete, show a confetti/celebration animation
- **Why**: Dopamine hit, positive reinforcement. Makes the app feel rewarding
- **Effort**: Low (frontend only)

---

## 7. Internationalization & UX

### RTL Support
- **Description**: Right-to-left layout for Arabic, Hebrew, etc.
- **Why**: Opens Middle Eastern market
- **Effort**: Medium

### Additional Languages
- **Description**: Community-driven translations via Crowdin / Locize
- **Why**: Global reach
- **Effort**: Variable per language

### Accessibility (a11y) Pass
- **Description**: WCAG 2.1 AA compliance — keyboard navigation, screen reader support, color contrast, focus management
- **Why**: Legal compliance, broader user base
- **Effort**: Medium-high

### Dark / Light Theme Toggle
- **Description**: User-selectable theme preference. MVP has dark-only. Light theme for daytime use
- **Why**: User preference, reduces eye strain
- **Effort**: Low (CSS variables swap)

### Mobile-First Redesign
- **Description**: Native-feeling mobile UI with bottom navigation, swipe actions, pull-to-refresh. Separate mobile layout from desktop
- **Why**: Most users will access via phone
- **Effort**: Medium (frontend only)

---

## 8. Data & Backup

### Automatic Cloud Sync (Always-On)
- **Description**: Data is always synced to backend (replacing optional Firebase). Offline writes are queued and synced when connection restores
- **Why**: MVP's optional Firebase was unreliable. Always-on sync is table stakes for a cloud app
- **Effort**: Part of core architecture (covered in ARCHITECTURE.md)

### Data Export (All Formats)
- **Description**: Export all user data in JSON, CSV (per table), and PDF summary. Full GDPR data portability
- **Why**: Legal compliance, user trust
- **Effort**: Low-medium

### Undo / Trash System
- **Description**: Soft-delete with 30-day trash. Support undo for any destructive action (delete goal, delete deposit, etc.)
- **Why**: Safety net, reduces anxiety around data loss
- **Effort**: Medium-high — `deleted_at` columns, background cleanup job

### Version History / Audit Log
- **Description**: Full audit trail of every change (who did what, when). Useful for shared goals
- **Why**: Trust and transparency in collaborative scenarios
- **Effort**: High — event sourcing or separate audit table

---

## 9. Integrations

### Bank Account Linking (Plaid / Yapily / GoCardless)
- **Description**: Link real bank accounts to automatically import transactions as deposits. Read-only access
- **Why**: Eliminates manual entry entirely. Game-changer for user retention
- **Effort**: Very high — third-party integration (Plaid for US/CA, Yapily/GoCardless for EU/UK), OAuth linking, transaction categorization
- **Note**: Regulatory compliance required (PSD2 in EU, screen scraping legality)

### Currency Conversion
- **Description**: Support deposits in multiple currencies with automatic conversion to goal currency at current rate
- **Why**: Useful for freelancers, international users, travelers
- **Effort**: Medium — exchange rate API (exchangerate-api.com / OpenExchangeRates), store amount + currency + converted amount

### Spreadsheet Import (CSV/Excel)
- **Description**: Import deposits or milestones from a CSV/Excel file
- **Why**: Bulk setup or migration from personal spreadsheets
- **Effort**: Low-medium

### Calendar Integration
- **Description**: Sync target dates / due milestones to Google Calendar / Apple Calendar / Outlook
- **Why**: Deadline visibility in user's existing calendar workflow
- **Effort**: Medium — OAuth calendar API integration

### IFTTT / Zapier Webhooks
- **Description**: Trigger actions on goal events (e.g., "When a milestone completes, send a Telegram message" or "When a deposit is added, log to Google Sheets")
- **Why**: Power user extensibility without building every integration ourselves
- **Effort**: Low — webhook endpoint + event system

---

## 10. Community & Social

### Public Leaderboard (Opt-in)
- **Description**: Users can opt into a public leaderboard showing "Most goals completed this month" or "Longest savings streak"
- **Why**: Gamification, community building
- **Effort**: Medium

### Goal Journals / Notes
- **Description**: Free-form diary entries per goal ("Today I felt motivated because..."). Private or public
- **Why**: Emotional connection to the saving journey, blog-like engagement
- **Effort**: Low-medium

### Milestone Completion Badges
- **Description**: Earn badges for completing milestones (first milestone, 5 milestones, all milestones in a goal, 30-day streak)
- **Why**: Gamification, sense of achievement
- **Effort**: Low

---

## Prioritization Framework

When evaluating ideas, consider:

| Factor | Weight | Notes |
|---|---|---|
| User value | High | Does this solve a real user problem? |
| Development cost | Medium | Backend + frontend + mobile effort |
| Maintenance cost | Medium | Ongoing burden vs. value |
| Differentiation | Medium | Does this set us apart from competitors? |
| Revenue potential | Low | Not currently monetized, but future-proof |
| Mobile necessity | Medium | Needed for iOS/Android parity? |

### Quick Wins (Low Effort, High Value)

1. Deposit notes / memo
2. Goal archiving
3. Goal categories / tags
4. Streak tracking
5. Dashboard / overview page
6. Export CSV / PDF reports
7. Milestone due dates
8. Profile & avatar settings
9. Dark / light theme toggle
10. Undo / toast confirmation on destructive actions

### Phase 2 (Medium Effort)

1. Time-bound goals (target dates)
2. Recurring / scheduled deposits
3. Progress snapshots timeline chart
4. Savings velocity chart
5. Push notifications
6. Email reports
7. Goal templates
8. Auto-allocate rules
9. Spreadsheet import

### Phase 3 (High Effort, Strategic)

1. Bank account linking
2. Goal sharing (collaborative)
3. Public goal profiles
4. Currency conversion
5. Full audit log with undo history
6. Milestone dependencies
7. Calendar integration
8. IFTTT / webhooks
