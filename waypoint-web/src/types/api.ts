export interface User {
  id: string
  email: string
  name: string
  locale: string
  currency: string
  role: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface Goal {
  id: string
  title: string
  description: string | null
  icon: string | null
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface CreateGoalRequest {
  title: string
  description?: string
  icon?: string
}

export interface UpdateGoalRequest {
  title?: string
  description?: string
  icon?: string
}

export interface Milestone {
  id: string
  goalId: string
  title: string
  cost: number
  details: string | null
  enabled: boolean
  completed: boolean
  sortOrder: number
  balance: number
  createdAt: string
  updatedAt: string
}

export interface CreateMilestoneRequest {
  title: string
  cost?: number
  details?: string
  enabled?: boolean
}

export interface UpdateMilestoneRequest {
  title?: string
  cost?: number
  details?: string
  enabled?: boolean
}

export interface ReorderMilestonesRequest {
  milestoneIds: string[]
}

export interface Deposit {
  id: string
  goalId: string
  amount: number
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateDepositRequest {
  amount: number
  note?: string
}

export interface UpdateDepositRequest {
  amount?: number
  note?: string
}

export interface Transfer {
  id: string
  goalId: string
  milestoneId: string
  amount: number
  type: 'ALLOCATE' | 'WITHDRAW'
  createdAt: string
  updatedAt: string
}

export interface AllocateRequest {
  milestoneId: string
  amount: number
}

export interface WithdrawRequest {
  milestoneId: string
  amount: number
}

export interface Completion {
  id: string
  goalId: string
  milestoneId: string
  milestoneTitle: string
  createdAt: string
}

export interface GoalAnalytics {
  goalId: string
  depositsTotal: number
  completionsTotal: number
  allocatedTotal: number
  balance: number
  targetAmount: number
  remainingAmount: number
  completionRatio: number
  completionPercent: number
  fundedMilestonesCount: number
  totalMilestonesCount: number
  averageCoverageRatio: number
  nextMilestone: {
    id: string
    title: string
    cost: number
    balance: number
  } | null
  milestones: Array<{
    id: string
    title: string
    cost: number
    balance: number
    coverageRatio: number
    completed: boolean
    enabled: boolean
  }>
}

export interface SummaryResponse {
  totalSaved: number
  totalTargets: number
  activeGoals: number
  completedMilestones: number
}

export interface ErrorResponse {
  error: {
    code: string
    message: string
    details?: Record<string, string>
  }
}

export interface BugReportMetadata {
  url: string
  pathname: string
  route?: { goalId?: string }
  userAgent: string
  viewport: { width: number; height: number }
  pixelRatio: number
  language: string
  timezone: string
  appVersion: string
  timestamp: string
  user: { id: string; email: string; name: string } | null
  [key: string]: unknown
}

export interface CreateBugReportRequest {
  description: string
  metadata?: Record<string, unknown>
}

export interface BugReport {
  id: string
  description: string
  metadata: Record<string, unknown>
  createdAt: string
}

export interface BugReportAttachment {
  id: string
  filename: string
  contentType: string
  sizeBytes: number
  createdAt: string
}

export interface AdminBugReportListItem {
  id: string
  description: string
  createdAt: string
  user: {
    id: string
    email: string
    displayName: string
  }
  attachmentCount: number
}

export interface AdminBugReportDetail {
  id: string
  description: string
  metadata: Record<string, unknown>
  createdAt: string
  user: {
    id: string
    email: string
    displayName: string
  }
  attachments: Array<{
    id: string
    filename: string
    contentType: string
    sizeBytes: number
    createdAt: string
    downloadUrl: string
  }>
}
