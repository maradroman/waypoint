import { useState, useCallback, useRef, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import {
  DndContext,
  DragOverlay,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core'
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { api } from '@/lib/api'
import { useFormatMoney } from '@/lib/format'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Progress } from '@/components/ui/progress'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  Plus,
  ArrowDownToLine,
  ArrowUpFromLine,
  Undo2,
  GripVertical,
  CheckCircle2,
  Pencil,
  Trash2,
  ExternalLink,
} from 'lucide-react'
import type { Goal, Milestone, Deposit, Completion, GoalAnalytics } from '@/types/api'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'

function FlipValue({ value, className, formatValue }: { value: number; className?: string; formatValue?: (v: number) => string }) {
  const [display, setDisplay] = useState(value)
  const [animating, setAnimating] = useState(false)
  const [oldVal, setOldVal] = useState(value)
  const prevRef = useRef(value)

  useEffect(() => {
    if (prevRef.current !== value) {
      setOldVal(prevRef.current)
      setAnimating(true)
      const timer = setTimeout(() => {
        setDisplay(value)
        setAnimating(false)
        prevRef.current = value
      }, 500)
      return () => clearTimeout(timer)
    }
  }, [value])

  const format = formatValue || ((v: number) => String(v))
  const wide = animating ? (oldVal > value ? oldVal : value) : display

  return (
    <span className={`${className} inline-grid`} style={{ perspective: '300px' }}>
      <span
        className={`col-start-1 row-start-1 ${animating ? 'animate-flip-out' : ''}`}
        style={{ backfaceVisibility: 'hidden', transformOrigin: 'bottom' }}
      >
        {format(animating ? oldVal : display)}
      </span>
      <span
        className={`col-start-1 row-start-1 ${animating ? 'animate-flip-in' : 'invisible'}`}
        style={{ backfaceVisibility: 'hidden', transformOrigin: 'top' }}
      >
        {format(value)}
      </span>
      <span className="col-start-1 row-start-1 invisible">{format(wide)}</span>
    </span>
  )
}

function SortableMilestoneItem({
  milestone,
  goalId,
  onEdit,
}: {
  milestone: Milestone
  goalId: string
  onEdit: (milestone: Milestone) => void
}) {
  const formatMoney = useFormatMoney()
  const { t } = useTranslation()
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: milestone.id })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  }

  const queryClient = useQueryClient()
  const [allocateOpen, setAllocateOpen] = useState(false)
  const [allocateAmount, setAllocateAmount] = useState('')
  const [withdrawOpen, setWithdrawOpen] = useState(false)
  const [withdrawAmount, setWithdrawAmount] = useState('')
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false)

  const allocateMutation = useMutation({
    mutationFn: (amount: number) =>
      api.post(`/goals/${goalId}/transfers/allocate`, {
        milestoneId: milestone.id,
        amount: Math.round(amount * 100),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      setAllocateOpen(false)
      setAllocateAmount('')
    },
  })

  const withdrawMutation = useMutation({
    mutationFn: (amount: number) =>
      api.post(`/goals/${goalId}/transfers/withdraw`, {
        milestoneId: milestone.id,
        amount: Math.round(amount * 100),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      setWithdrawOpen(false)
      setWithdrawAmount('')
    },
  })

  const toggleMutation = useMutation({
    mutationFn: () =>
      api.patch(`/goals/${goalId}/milestones/${milestone.id}`, {
        enabled: !milestone.enabled,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
    },
  })

  const completeMutation = useMutation({
    mutationFn: () =>
      api.post(`/goals/${goalId}/milestones/${milestone.id}/complete`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['completions', goalId] })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => api.delete(`/goals/${goalId}/milestones/${milestone.id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
    },
  })

  const coveragePercent = milestone.cost > 0
    ? Math.round((milestone.balance / milestone.cost) * 100)
    : 0

  return (
    <div ref={setNodeRef} style={style}>
      <Card className={!milestone.enabled ? 'opacity-50' : ''}>
        <CardContent className="flex gap-2 p-4 sm:gap-4">
          <button
            className="cursor-grab touch-none self-start mt-1 shrink-0"
            {...attributes}
            {...listeners}
            aria-label="Drag to reorder"
          >
            <GripVertical className="h-5 w-5 text-muted-foreground" />
          </button>
          <div className="min-w-0 flex-1 space-y-2">
            <div className="flex items-center gap-2">
              <span className="font-medium">{milestone.title}</span>
              {milestone.completed && <Badge variant="secondary">{t('milestone.done')}</Badge>}
              {!milestone.enabled && <Badge variant="outline">{t('milestone.disabled')}</Badge>}
            </div>
            {milestone.details && (
              <p className="text-sm text-muted-foreground">{milestone.details}</p>
            )}
            <div className="flex items-center gap-4 text-sm">
              <span>
                {t('milestone.cost')}: <strong>{formatMoney(milestone.cost)}</strong>
              </span>
              <span>
                {t('milestone.balance')}: <strong>{formatMoney(milestone.balance)}</strong>
              </span>
              <Progress value={coveragePercent} className="h-2 w-24" />
              <span className="text-xs text-muted-foreground">{coveragePercent}%</span>
            </div>
            <div className="flex flex-wrap gap-2">
              <Dialog open={allocateOpen} onOpenChange={setAllocateOpen}>
                <DialogTrigger asChild>
                  <Button size="sm" variant="outline" disabled={!milestone.enabled}>
                    <ArrowDownToLine className="mr-1 h-4 w-4" />
                    {t('milestone.allocate')}
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader><DialogTitle>{t('milestone.allocateTo', { name: milestone.title })}</DialogTitle></DialogHeader>
                  <form onSubmit={(e) => {
                    e.preventDefault()
                    allocateMutation.mutate(Number(allocateAmount))
                  }} className="space-y-4">
                    <div className="space-y-2">
                      <Label>{t('milestone.amount')}</Label>
                      <Input
                        type="number"
                        value={allocateAmount}
                        onChange={(e) => setAllocateAmount(e.target.value)}
                        required
                        min={1}
                      />
                    </div>
                    <Button type="submit" className="w-full">
                      {t('milestone.allocate')}
                    </Button>
                  </form>
                </DialogContent>
              </Dialog>
              <Dialog open={withdrawOpen} onOpenChange={setWithdrawOpen}>
                <DialogTrigger asChild>
                  <Button size="sm" variant="outline" disabled={!milestone.enabled || milestone.balance === 0}>
                    <ArrowUpFromLine className="mr-1 h-4 w-4" />
                    {t('milestone.withdraw')}
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader><DialogTitle>{t('milestone.withdrawFrom', { name: milestone.title })}</DialogTitle></DialogHeader>
                  <form onSubmit={(e) => {
                    e.preventDefault()
                    withdrawMutation.mutate(Number(withdrawAmount))
                  }} className="space-y-4">
                    <div className="space-y-2">
                      <Label>{t('milestone.amountMax', { max: formatMoney(milestone.balance) })}</Label>
                      <Input
                        type="number"
                        value={withdrawAmount}
                        onChange={(e) => setWithdrawAmount(e.target.value)}
                        required
                        min={1}
                        max={milestone.balance}
                      />
                    </div>
                    <Button type="submit" className="w-full">
                      {t('milestone.withdraw')}
                    </Button>
                  </form>
                </DialogContent>
              </Dialog>
              {!milestone.completed && milestone.enabled && (
                <Button size="sm" variant="default" onClick={() => completeMutation.mutate()}>
                  <CheckCircle2 className="mr-1 h-4 w-4" />
                  {t('milestone.complete')}
                </Button>
              )}
            </div>
          </div>
          <div className="flex shrink-0 flex-col items-end justify-between self-stretch">
            <div className="flex gap-0.5">
              <Button
                size="icon"
                variant="ghost"
                className="h-7 w-7"
                onClick={() => onEdit(milestone)}
              >
                <Pencil className="h-3.5 w-3.5" />
              </Button>
              <Button
                size="icon"
                variant="ghost"
                className="h-7 w-7 text-destructive hover:text-destructive"
                onClick={() => setConfirmDeleteOpen(true)}
              >
                <Trash2 className="h-3.5 w-3.5" />
              </Button>
            </div>
            <Switch
              checked={milestone.enabled}
              onCheckedChange={() => toggleMutation.mutate()}
            />
          </div>
        </CardContent>
      </Card>

      <ConfirmDialog
        open={confirmDeleteOpen}
        onOpenChange={setConfirmDeleteOpen}
        title={t('milestone.delete')}
        confirmText={t('common.delete')}
        cancelText={t('common.cancel')}
        onConfirm={() => deleteMutation.mutate()}
      />
    </div>
  )
}

export default function GoalDetailPage() {
  const { goalId } = useParams<{ goalId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const formatMoney = useFormatMoney()
  const { t, i18n } = useTranslation()
  const [milestoneDialogOpen, setMilestoneDialogOpen] = useState(false)
  const [editingMilestone, setEditingMilestone] = useState<Milestone | null>(null)
  const [title, setTitle] = useState('')
  const [cost, setCost] = useState('')
  const [details, setDetails] = useState('')
  const [depositOpen, setDepositOpen] = useState(false)
  const [depositAmount, setDepositAmount] = useState('')
  const [depositNote, setDepositNote] = useState('')
  const [activeDragId, setActiveDragId] = useState<string | null>(null)

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
  )

  const { data: goal, isLoading: goalLoading } = useQuery({
    queryKey: ['goal', goalId],
    queryFn: () => api.get<Goal>(`/goals/${goalId}`),
    enabled: !!goalId,
  })

  const { data: milestones } = useQuery({
    queryKey: ['milestones', goalId],
    queryFn: () => api.get<Milestone[]>(`/goals/${goalId}/milestones`),
    enabled: !!goalId,
  })

  const { data: deposits } = useQuery({
    queryKey: ['deposits', goalId],
    queryFn: () => api.get<Deposit[]>(`/goals/${goalId}/deposits`),
    enabled: !!goalId,
  })

  const { data: completions } = useQuery({
    queryKey: ['completions', goalId],
    queryFn: () => api.get<Completion[]>(`/goals/${goalId}/completions`),
    enabled: !!goalId,
  })

  const { data: analytics } = useQuery({
    queryKey: ['analytics', goalId],
    queryFn: () => api.get<GoalAnalytics>(`/goals/${goalId}/analytics`),
    enabled: !!goalId,
  })

  const handleEditMilestone = (milestone: Milestone) => {
    setEditingMilestone(milestone)
    setTitle(milestone.title)
    setCost(String(milestone.cost / 100))
    setDetails(milestone.details || '')
    setMilestoneDialogOpen(true)
  }

  const handleCloseMilestoneDialog = () => {
    setMilestoneDialogOpen(false)
    setTimeout(() => {
      setEditingMilestone(null)
      setTitle('')
      setCost('')
      setDetails('')
    }, 200)
  }

  const createMilestone = useMutation({
    mutationFn: () =>
      api.post(`/goals/${goalId}/milestones`, {
        title,
        cost: cost ? Math.round(Number(cost) * 100) : undefined,
        details: details || undefined,
        enabled: true,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      handleCloseMilestoneDialog()
    },
  })

  const updateMilestone = useMutation({
    mutationFn: () =>
      api.patch(`/goals/${goalId}/milestones/${editingMilestone?.id}`, {
        title,
        cost: cost ? Math.round(Number(cost) * 100) : undefined,
        details: details || undefined,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      handleCloseMilestoneDialog()
    },
  })

  const createDeposit = useMutation({
    mutationFn: () =>
      api.post(`/goals/${goalId}/deposits`, {
        amount: Math.round(Number(depositAmount) * 100),
        note: depositNote || undefined,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deposits', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      setDepositAmount('')
      setDepositNote('')
      setDepositOpen(false)
    },
  })

  const undoCompletion = useMutation({
    mutationFn: (completionId: string) =>
      api.delete(`/goals/${goalId}/completions/${completionId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['completions', goalId] })
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
    },
  })

  const reorderMutation = useMutation({
    mutationFn: (milestoneIds: string[]) =>
      api.patch(`/goals/${goalId}/milestones/reorder`, { milestoneIds }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
    },
  })

  const toggleAllMutation = useMutation({
    mutationFn: (enabled: boolean) =>
      api.patch(`/goals/${goalId}/milestones/toggle-all`, { enabled }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
    },
  })

  const hasDisabled = milestones?.some((m) => !m.enabled)

  const handleDragStart = useCallback((event: DragStartEvent) => {
    setActiveDragId(String(event.active.id))
  }, [])

  const handleDragEnd = useCallback((event: DragEndEvent) => {
    setActiveDragId(null)
    const { active, over } = event
    if (!over || active.id === over.id) return

    const oldIndex = milestones?.findIndex((m) => m.id === active.id) ?? -1
    const newIndex = milestones?.findIndex((m) => m.id === over.id) ?? -1
    if (oldIndex === -1 || newIndex === -1) return

    const reordered = [...milestones!]
    const [moved] = reordered.splice(oldIndex, 1)
    reordered.splice(newIndex, 0, moved)

    reorderMutation.mutate(reordered.map((m) => m.id))
  }, [milestones, reorderMutation])

  if (goalLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-64" />
      </div>
    )
  }

  if (!goal) {
    return (
      <div className="flex flex-col items-center gap-4 py-20">
        <p className="text-lg text-muted-foreground">Goal not found</p>
        <Button onClick={() => navigate('/goals')}>Back to goals</Button>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <Button variant="ghost" className="-ml-4 mb-2" onClick={() => navigate('/goals')}>
            ← {t('common.back')}
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">
            {goal.icon && <span className="mr-2">{goal.icon}</span>}
            {goal.title}
          </h1>
          {goal.description && (
            <p className="text-muted-foreground">{goal.description}</p>
          )}
        </div>
      </div>

      <div className="space-y-3">
        <div className="grid gap-4 sm:grid-cols-2">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{t('goalDetail.walletBalance')}</CardTitle>
            </CardHeader>
            <CardContent>
              <FlipValue value={analytics?.walletBalance ?? 0} formatValue={formatMoney} className="text-2xl font-bold" />
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{t('goalDetail.target')}</CardTitle>
            </CardHeader>
            <CardContent>
              <FlipValue value={analytics?.totalMilestoneCost ?? 0} formatValue={formatMoney} className="text-2xl font-bold" />
            </CardContent>
          </Card>
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">{t('planning.remaining')}: {formatMoney((analytics?.totalMilestoneCost ?? 0) - (analytics?.walletBalance ?? 0))}</span>
            <span className="font-medium">
              <FlipValue value={analytics?.progressPercent ?? 0} />%
            </span>
          </div>
          <Progress value={analytics?.progressPercent ?? 0} className="h-2" />
          {analytics?.potentialCompletionDate ? (
            <p className="text-sm text-muted-foreground">
              {t('goalDetail.potentialCompletionDate', {
                date: new Date(analytics.potentialCompletionDate).toLocaleDateString(i18n.language, {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })
              })}{' '}
              <Link to={`/planning?goal=${goalId}`} className="inline-flex items-center gap-1 underline hover:text-foreground">
                {t('goalDetail.seePlanningLink')}
                <ExternalLink className="h-3 w-3" />
              </Link>
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              <Link to={`/planning?goal=${goalId}`} className="inline-flex items-center gap-1 underline hover:text-foreground">
                {t('goalDetail.planLink')}
                <ExternalLink className="h-3 w-3" />
              </Link>{' '}
              {t('goalDetail.planAheadPromptPrefix')}
            </p>
          )}
        </div>
      </div>

      <Separator />

      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">{t('goalDetail.milestones')}</h2>
        <div className="flex gap-2">
          {hasDisabled && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => toggleAllMutation.mutate(true)}
            >
              Enable All
            </Button>
          )}
          <Dialog open={depositOpen} onOpenChange={setDepositOpen}>
            <DialogTrigger asChild>
              <Button variant="outline">
                <Plus className="mr-2 h-4 w-4" />
                {t('goalDetail.addFunds')}
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>{t('deposit.add')}</DialogTitle></DialogHeader>
              <form onSubmit={(e) => {
                e.preventDefault()
                createDeposit.mutate()
              }} className="space-y-4">
                <div className="space-y-2">
                  <Label>{t('deposit.amount')}</Label>
                  <Input
                    type="number"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                    required
                    min={1}
                  />
                </div>
                <div className="space-y-2">
                  <Label>{t('deposit.note')}</Label>
                  <Input
                    value={depositNote}
                    onChange={(e) => setDepositNote(e.target.value)}
                  />
                </div>
                <Button type="submit" className="w-full">
                  {t('deposit.deposit')}
                </Button>
              </form>
            </DialogContent>
          </Dialog>
          <Dialog open={milestoneDialogOpen} onOpenChange={(isOpen) => {
            if (isOpen) {
              setEditingMilestone(null)
              setTitle('')
              setCost('')
              setDetails('')
            }
            setMilestoneDialogOpen(isOpen)
          }}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                {t('goalDetail.milestone')}
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>{editingMilestone ? t('milestone.edit') : t('milestone.add')}</DialogTitle>
              </DialogHeader>
              <form onSubmit={(e) => {
                e.preventDefault()
                if (editingMilestone) {
                  updateMilestone.mutate()
                } else {
                  createMilestone.mutate()
                }
              }} className="space-y-4">
                <div className="space-y-2">
                  <Label>{t('milestone.title')}</Label>
                  <Input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={70} />
                </div>
                <div className="space-y-2">
                  <Label>{t('milestone.cost')}</Label>
                  <Input type="number" value={cost} onChange={(e) => setCost(e.target.value)} min={0} step="0.01" />
                </div>
                <div className="space-y-2">
                  <Label>{t('milestone.details')}</Label>
                  <Input value={details} onChange={(e) => setDetails(e.target.value)} maxLength={200} />
                </div>
                <Button type="submit" className="w-full" disabled={createMilestone.isPending || updateMilestone.isPending}>
                  {editingMilestone ? (updateMilestone.isPending ? t('common.updating') : t('common.update')) : (createMilestone.isPending ? t('common.creating') : t('common.create'))}
                </Button>
              </form>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={milestones?.map((m) => m.id) ?? []}
          strategy={verticalListSortingStrategy}
        >
          <div className="space-y-3">
            {milestones?.map((ms) => (
              <SortableMilestoneItem key={ms.id} milestone={ms} goalId={goal.id} onEdit={handleEditMilestone} />
            ))}
            {milestones?.length === 0 && (
              <p className="py-8 text-center text-muted-foreground">
                {t('milestone.noMilestones')}
              </p>
            )}
          </div>
        </SortableContext>
        <DragOverlay>
          {activeDragId && milestones ? (
            <Card className="opacity-80 shadow-lg">
              <CardContent className="p-4">
                <p className="font-medium">
                  {milestones.find((m) => m.id === activeDragId)?.title}
                </p>
              </CardContent>
            </Card>
          ) : null}
        </DragOverlay>
      </DndContext>

      <Separator />

      <div>
        <h2 className="mb-4 text-xl font-semibold">{t('goalDetail.deposits')}</h2>
        {deposits?.length === 0 ? (
          <p className="text-sm text-muted-foreground">{t('goalDetail.noDeposits')}</p>
        ) : (
          <div className="space-y-2">
            {deposits?.map((deposit) => (
              <Card key={deposit.id}>
                <CardContent className="flex items-center justify-between p-4">
                  <div>
                    <p className="font-medium">+{formatMoney(deposit.amount)}</p>
                    {deposit.note && (
                      <p className="text-sm text-muted-foreground">{deposit.note}</p>
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {new Date(deposit.createdAt).toLocaleDateString()}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>

      <Separator />

      <div>
        <h2 className="mb-4 text-xl font-semibold">{t('goalDetail.completions')}</h2>
        {completions?.length === 0 ? (
          <p className="text-sm text-muted-foreground">{t('goalDetail.noCompletions')}</p>
        ) : (
          <div className="space-y-2">
            {completions?.map((comp) => (
              <Card key={comp.id}>
                <CardContent className="flex items-center justify-between p-4">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-5 w-5 text-green-500" />
                    <span>{comp.milestoneTitle}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-muted-foreground">
                      {new Date(comp.createdAt).toLocaleDateString()}
                    </span>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => undoCompletion.mutate(comp.id)}
                    >
                      <Undo2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
