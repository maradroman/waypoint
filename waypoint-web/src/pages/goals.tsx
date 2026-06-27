import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Skeleton } from '@/components/ui/skeleton'
import { Plus, MoreHorizontal, Pencil, Trash2, FolderOpen } from 'lucide-react'
import type { Goal } from '@/types/api'
import { useNavigate } from 'react-router-dom'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'

function GoalCard({ goal, onEdit }: { goal: Goal; onEdit: (goal: Goal) => void }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false)

  const deleteMutation = useMutation({
    mutationFn: () => api.delete(`/goals/${goal.id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] })
    },
  })

  return (
    <>
      <Card
        className="cursor-pointer transition-shadow hover:shadow-md"
        onClick={() => navigate(`/goals/${goal.id}`)}
      >
        <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
          <CardTitle className="text-lg font-medium">
            {goal.icon && <span className="mr-2">{goal.icon}</span>}
            {goal.title}
          </CardTitle>
          <DropdownMenu>
            <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation()
                onEdit(goal)
              }}>
                <Pencil className="mr-2 h-4 w-4" />
                {t('common.edit')}
              </DropdownMenuItem>
              <DropdownMenuItem
                className="text-destructive"
                onClick={(e) => {
                  e.stopPropagation()
                  setConfirmDeleteOpen(true)
                }}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                {t('common.delete')}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </CardHeader>
        <CardContent>
          {goal.description && (
            <p className="text-sm text-muted-foreground line-clamp-2">
              {goal.description}
            </p>
          )}
        </CardContent>
      </Card>
      <ConfirmDialog
        open={confirmDeleteOpen}
        onOpenChange={setConfirmDeleteOpen}
        title={t('goals.deleteConfirm')}
        confirmText={t('common.delete')}
        cancelText={t('common.cancel')}
        onConfirm={() => deleteMutation.mutate()}
      />
    </>
  )
}

export default function GoalsPage() {
  const [open, setOpen] = useState(false)
  const [editingGoal, setEditingGoal] = useState<Goal | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [icon, setIcon] = useState('')
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const { data: goals, isLoading } = useQuery({
    queryKey: ['goals'],
    queryFn: () => api.get<Goal[]>('/goals'),
  })

  const handleEdit = (goal: Goal) => {
    setEditingGoal(goal)
    setTitle(goal.title)
    setDescription(goal.description || '')
    setIcon(goal.icon || '')
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
    setTimeout(() => {
      setEditingGoal(null)
      setTitle('')
      setDescription('')
      setIcon('')
    }, 200)
  }

  const createMutation = useMutation({
    mutationFn: () =>
      api.post('/goals', { title, description: description || undefined, icon: icon || undefined }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] })
      handleClose()
    },
  })

  const updateMutation = useMutation({
    mutationFn: () =>
      api.patch(`/goals/${editingGoal?.id}`, { title, description: description || undefined, icon: icon || undefined }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] })
      handleClose()
    },
  })

  const isEditing = editingGoal !== null
  const mutation = isEditing ? updateMutation : createMutation

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">{t('goals.title')}</h1>
        <Dialog open={open} onOpenChange={(isOpen) => {
          if (isOpen) {
            setEditingGoal(null)
            setTitle('')
            setDescription('')
            setIcon('')
          }
          setOpen(isOpen)
        }}>
          <DialogTrigger asChild>
            <Button variant="accent">
              <Plus className="mr-2 h-4 w-4" />
              {t('goals.newGoal')}
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{isEditing ? t('goals.editGoal') : t('goals.createGoal')}</DialogTitle>
            </DialogHeader>
            <form
              onSubmit={(e) => {
                e.preventDefault()
                mutation.mutate()
              }}
              className="space-y-4"
            >
              <div className="space-y-2">
                <Label htmlFor="title">{t('goals.titleLabel')}</Label>
                <Input
                  id="title"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                  maxLength={70}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="icon">{t('goals.iconLabel')}</Label>
                <Input
                  id="icon"
                  value={icon}
                  onChange={(e) => setIcon(e.target.value)}
                  placeholder="🎯"
                  className="placeholder:opacity-30"
                  maxLength={10}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="desc">{t('goals.descriptionLabel')}</Label>
                <Textarea
                  id="desc"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  maxLength={200}
                />
              </div>
              <Button type="submit" disabled={mutation.isPending} className="w-full">
                {mutation.isPending ? (isEditing ? t('common.updating') : t('common.creating')) : (isEditing ? t('common.update') : t('common.create'))}
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-32 rounded-xl" />
          ))}
        </div>
      ) : goals?.length === 0 ? (
        <div className="flex flex-col items-center gap-4 py-20 text-muted-foreground">
          <FolderOpen className="h-16 w-16" />
          <p className="text-lg">{t('goals.noGoals')}</p>
          <p className="text-sm">{t('goals.createFirstGoal')}</p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {goals?.map((goal) => (
            <GoalCard key={goal.id} goal={goal} onEdit={handleEdit} />
          ))}
        </div>
      )}
    </div>
  )
}
