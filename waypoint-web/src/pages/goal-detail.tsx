import { useState, useCallback, useRef, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
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
} from 'lucide-react'
import type { Goal, Milestone, Deposit, Completion } from '@/types/api'

function FlipValue({ value, className }: { value: number; className?: string }) {
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

  const wide = animating ? (oldVal > value ? oldVal : value) : display

  return (
    <span className={`${className} inline-grid`} style={{ perspective: '300px' }}>
      <span
        className={`col-start-1 row-start-1 ${animating ? 'animate-flip-out' : ''}`}
        style={{ backfaceVisibility: 'hidden', transformOrigin: 'bottom' }}
      >
        {animating ? oldVal : display}
      </span>
      <span
        className={`col-start-1 row-start-1 ${animating ? 'animate-flip-in' : 'invisible'}`}
        style={{ backfaceVisibility: 'hidden', transformOrigin: 'top' }}
      >
        {value}
      </span>
      <span className="col-start-1 row-start-1 invisible">{wide}</span>
    </span>
  )
}

function SortableMilestoneItem({
  milestone,
  goalId,
}: {
  milestone: Milestone
  goalId: string
}) {
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

  const allocateMutation = useMutation({
    mutationFn: (amount: number) =>
      api.post(`/goals/${goalId}/transfers/allocate`, {
        milestoneId: milestone.id,
        amount,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      setAllocateOpen(false)
    },
  })

  const withdrawMutation = useMutation({
    mutationFn: (amount: number) =>
      api.post(`/goals/${goalId}/transfers/withdraw`, {
        milestoneId: milestone.id,
        amount,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', goalId] })
      setWithdrawOpen(false)
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

  const coveragePercent = milestone.cost > 0
    ? Math.round((milestone.balance / milestone.cost) * 100)
    : 0

  return (
    <div ref={setNodeRef} style={style}>
      <Card className={!milestone.enabled ? 'opacity-50' : ''}>
        <CardContent className="flex gap-4 p-4">
          <button
            className="cursor-grab touch-none self-start mt-1"
            {...attributes}
            {...listeners}
            aria-label="Drag to reorder"
          >
            <GripVertical className="h-5 w-5 shrink-0 text-muted-foreground" />
          </button>
          <div className="flex-1 space-y-2">
            <div className="flex items-center gap-2">
              <span className="font-medium">{milestone.title}</span>
              {milestone.completed && <Badge variant="secondary">Done</Badge>}
              {!milestone.enabled && <Badge variant="outline">Disabled</Badge>}
            </div>
            {milestone.details && (
              <p className="text-sm text-muted-foreground">{milestone.details}</p>
            )}
            <div className="flex items-center gap-4 text-sm">
              <span>
                Cost: <strong>{milestone.cost}</strong>
              </span>
              <span>
                Balance: <strong>{milestone.balance}</strong>
              </span>
              <Progress value={coveragePercent} className="h-2 w-24" />
              <span className="text-xs text-muted-foreground">{coveragePercent}%</span>
            </div>
            <div className="flex flex-wrap gap-2">
              <Dialog open={allocateOpen} onOpenChange={setAllocateOpen}>
                <DialogTrigger asChild>
                  <Button size="sm" variant="outline" disabled={!milestone.enabled}>
                    <ArrowDownToLine className="mr-1 h-4 w-4" />
                    Allocate
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader><DialogTitle>Allocate to {milestone.title}</DialogTitle></DialogHeader>
                  <form onSubmit={(e) => {
                    e.preventDefault()
                    allocateMutation.mutate(Number(allocateAmount))
                  }} className="space-y-4">
                    <div className="space-y-2">
                      <Label>Amount</Label>
                      <Input
                        type="number"
                        value={allocateAmount}
                        onChange={(e) => setAllocateAmount(e.target.value)}
                        required
                        min={1}
                      />
                    </div>
                    <Button type="submit" className="w-full">
                      Allocate
                    </Button>
                  </form>
                </DialogContent>
              </Dialog>
              <Dialog open={withdrawOpen} onOpenChange={setWithdrawOpen}>
                <DialogTrigger asChild>
                  <Button size="sm" variant="outline" disabled={!milestone.enabled || milestone.balance === 0}>
                    <ArrowUpFromLine className="mr-1 h-4 w-4" />
                    Withdraw
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader><DialogTitle>Withdraw from {milestone.title}</DialogTitle></DialogHeader>
                  <form onSubmit={(e) => {
                    e.preventDefault()
                    withdrawMutation.mutate(Number(withdrawAmount))
                  }} className="space-y-4">
                    <div className="space-y-2">
                      <Label>Amount (max {milestone.balance})</Label>
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
                      Withdraw
                    </Button>
                  </form>
                </DialogContent>
              </Dialog>
              {!milestone.completed && milestone.enabled && (
                <Button size="sm" variant="default" onClick={() => completeMutation.mutate()}>
                  <CheckCircle2 className="mr-1 h-4 w-4" />
                  Complete
                </Button>
              )}
            </div>
          </div>
          <Switch
            checked={milestone.enabled}
            onCheckedChange={() => toggleMutation.mutate()}
            className="self-end shrink-0"
          />
        </CardContent>
      </Card>
    </div>
  )
}

export default function GoalDetailPage() {
  const { goalId } = useParams<{ goalId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [createOpen, setCreateOpen] = useState(false)
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
    queryFn: () => api.get<{ walletBalance: number; totalMilestoneCost: number; progressPercent: number }>(`/goals/${goalId}/analytics`),
    enabled: !!goalId,
  })

  const createMilestone = useMutation({
    mutationFn: () =>
      api.post(`/goals/${goalId}/milestones`, {
        title,
        cost: cost ? Number(cost) : undefined,
        details: details || undefined,
        enabled: true,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', goalId] })
      setTitle('')
      setCost('')
      setDetails('')
      setCreateOpen(false)
    },
  })

  const createDeposit = useMutation({
    mutationFn: () =>
      api.post(`/goals/${goalId}/deposits`, {
        amount: Number(depositAmount),
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
          <Button variant="ghost" className="mb-2" onClick={() => navigate('/goals')}>
            ← Back
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

      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Wallet Balance</CardTitle>
          </CardHeader>
          <CardContent>
            <FlipValue value={analytics?.walletBalance ?? 0} className="text-2xl font-bold" />
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Target</CardTitle>
          </CardHeader>
          <CardContent>
            <FlipValue value={analytics?.totalMilestoneCost ?? 0} className="text-2xl font-bold" />
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Completion</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              <FlipValue value={analytics?.progressPercent ?? 0} />%
            </p>
          </CardContent>
        </Card>
      </div>

      <Separator />

      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Milestones</h2>
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
                Add Funds
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Add Funds</DialogTitle></DialogHeader>
              <form onSubmit={(e) => {
                e.preventDefault()
                createDeposit.mutate()
              }} className="space-y-4">
                <div className="space-y-2">
                  <Label>Amount</Label>
                  <Input
                    type="number"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                    required
                    min={1}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Note (optional)</Label>
                  <Input
                    value={depositNote}
                    onChange={(e) => setDepositNote(e.target.value)}
                  />
                </div>
                <Button type="submit" className="w-full">
                  Deposit
                </Button>
              </form>
            </DialogContent>
          </Dialog>
          <Dialog open={createOpen} onOpenChange={setCreateOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                Milestone
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Add Milestone</DialogTitle></DialogHeader>
              <form onSubmit={(e) => {
                e.preventDefault()
                createMilestone.mutate()
              }} className="space-y-4">
                <div className="space-y-2">
                  <Label>Title</Label>
                  <Input value={title} onChange={(e) => setTitle(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label>Cost</Label>
                  <Input type="number" value={cost} onChange={(e) => setCost(e.target.value)} min={0} />
                </div>
                <div className="space-y-2">
                  <Label>Details (optional)</Label>
                  <Input value={details} onChange={(e) => setDetails(e.target.value)} />
                </div>
                <Button type="submit" className="w-full" disabled={createMilestone.isPending}>
                  Create
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
              <SortableMilestoneItem key={ms.id} milestone={ms} goalId={goal.id} />
            ))}
            {milestones?.length === 0 && (
              <p className="py-8 text-center text-muted-foreground">
                No milestones yet. Add one to get started.
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
        <h2 className="mb-4 text-xl font-semibold">Deposits</h2>
        {deposits?.length === 0 ? (
          <p className="text-sm text-muted-foreground">No deposits yet</p>
        ) : (
          <div className="space-y-2">
            {deposits?.map((deposit) => (
              <Card key={deposit.id}>
                <CardContent className="flex items-center justify-between p-4">
                  <div>
                    <p className="font-medium">+{deposit.amount}</p>
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
        <h2 className="mb-4 text-xl font-semibold">Completions</h2>
        {completions?.length === 0 ? (
          <p className="text-sm text-muted-foreground">No completions yet</p>
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
