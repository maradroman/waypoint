import { useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useLocation, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { toast } from 'sonner'
import { api, uploadWithProgress } from '@/lib/api'
import { useAuth } from '@/stores/auth'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Progress } from '@/components/ui/progress'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Bug, Paperclip, X, FileVideo, FileImage } from 'lucide-react'
import type { BugReport, BugReportMetadata, BugReportAttachment, User } from '@/types/api'

const MAX_FILE_SIZE = 100 * 1024 * 1024
const ACCEPTED_TYPES = ['image/', 'video/']

function buildMetadata(user: User | null, pathname: string, goalId?: string): BugReportMetadata {
  return {
    url: window.location.href,
    pathname,
    route: goalId ? { goalId } : undefined,
    userAgent: navigator.userAgent,
    viewport: { width: window.innerWidth, height: window.innerHeight },
    pixelRatio: window.devicePixelRatio,
    language: navigator.language,
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    appVersion: import.meta.env.VITE_APP_VERSION ?? 'unknown',
    timestamp: new Date().toISOString(),
    user: user ? { id: user.id, email: user.email, name: user.name } : null,
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function isImage(file: File): boolean {
  return file.type.startsWith('image/')
}

export function ReportBugButton() {
  const [open, setOpen] = useState(false)
  const [description, setDescription] = useState('')
  const [files, setFiles] = useState<File[]>([])
  const [uploadProgress, setUploadProgress] = useState<number | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const user = useAuth((s) => s.user)
  const location = useLocation()
  const params = useParams()
  const { t } = useTranslation()

  const createMutation = useMutation({
    mutationFn: (vars: { description: string; metadata: Record<string, unknown> }) =>
      api.post<BugReport>('/bug-reports', vars),
  })

  const resetState = () => {
    setDescription('')
    setFiles([])
    setUploadProgress(null)
  }

  const addFiles = (incoming: File[]) => {
    const valid: File[] = []
    for (const file of incoming) {
      if (file.size > MAX_FILE_SIZE) {
        toast.error(t('bugReport.fileTooLarge', { name: file.name }))
        continue
      }
      if (!ACCEPTED_TYPES.some((t) => file.type.startsWith(t))) {
        toast.error(t('bugReport.invalidFileType', { name: file.name }))
        continue
      }
      valid.push(file)
    }
    if (valid.length > 0) {
      setFiles((prev) => [...prev, ...valid])
    }
  }

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    addFiles(Array.from(e.target.files ?? []))
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handlePaste = (e: React.ClipboardEvent) => {
    const items = Array.from(e.clipboardData.items)
    const imageItems = items.filter((item) => item.type.startsWith('image/'))
    if (imageItems.length === 0) return

    e.preventDefault()
    const pasted: File[] = []
    for (const item of imageItems) {
      const file = item.getAsFile()
      if (!file) continue
      const ext = file.type.split('/')[1] ?? 'png'
      const renamed = new File([file], `screenshot-${Date.now()}.${ext}`, { type: file.type })
      pasted.push(renamed)
    }
    addFiles(pasted)
  }

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      const bugReport = await createMutation.mutateAsync({
        description,
        metadata: buildMetadata(user, location.pathname, params.goalId),
      })

      if (files.length > 0) {
        setUploadProgress(0)
        const formData = new FormData()
        for (const file of files) {
          formData.append('files', file)
        }
        await uploadWithProgress<BugReportAttachment[]>(
          `/bug-reports/${bugReport.id}/attachments`,
          formData,
          setUploadProgress,
        )
      }

      toast.success(t('bugReport.success'))
      resetState()
      setOpen(false)
    } catch {
      setUploadProgress(null)
      toast.error(t('bugReport.error'))
    }
  }

  const isSubmitting = createMutation.isPending || uploadProgress !== null

  return (
    <Dialog
      open={open}
      onOpenChange={(v) => {
        setOpen(v)
        if (!v) resetState()
      }}
    >
      <DialogTrigger asChild>
        <Button variant="ghost" className="w-full justify-start gap-2">
          <Bug className="h-4 w-4" />
          <span className="text-sm">{t('bugReport.reportBug')}</span>
        </Button>
      </DialogTrigger>
      <DialogContent onPaste={handlePaste}>
        <DialogHeader>
          <DialogTitle>{t('bugReport.title')}</DialogTitle>
          <DialogDescription>
            {t('bugReport.description')}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="bug-description">{t('bugReport.whatHappened')}</Label>
            <Textarea
              id="bug-description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder={t('bugReport.placeholder')}
              required
              className="min-h-[120px]"
            />
          </div>

          <div className="space-y-2">
            <Label>{t('bugReport.attachments')}</Label>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*,video/*"
              multiple
              onChange={handleFileSelect}
              className="hidden"
              data-testid="file-input"
            />
            <Button
              type="button"
              variant="outline"
              className="w-full"
              onClick={() => fileInputRef.current?.click()}
              disabled={isSubmitting}
            >
              <Paperclip className="h-4 w-4" />
              {t('bugReport.addAttachment')}
            </Button>
            <p className="text-xs text-muted-foreground">
              {t('bugReport.pasteHint')} {navigator.platform.toLowerCase().includes('mac') ? '⌘V' : 'Ctrl+V'}
            </p>
            {files.length > 0 && (
              <div className="space-y-2">
                {files.map((file, index) => (
                  <div
                    key={`${file.name}-${index}`}
                    className="flex items-center gap-2 rounded-md border p-2"
                  >
                    {isImage(file) ? (
                      <FileImage className="h-4 w-4 shrink-0 text-muted-foreground" />
                    ) : (
                      <FileVideo className="h-4 w-4 shrink-0 text-muted-foreground" />
                    )}
                    <span className="flex-1 truncate text-sm">{file.name}</span>
                    <span className="text-xs text-muted-foreground">{formatSize(file.size)}</span>
                    {!isSubmitting && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={() => removeFile(index)}
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {uploadProgress !== null && (
            <div className="space-y-1">
              <Progress value={uploadProgress} />
              <p className="text-center text-xs text-muted-foreground">
                {t('bugReport.uploadingProgress', { progress: uploadProgress })}
              </p>
            </div>
          )}

          <Button type="submit" disabled={isSubmitting} className="w-full">
            {isSubmitting
              ? uploadProgress !== null
                ? t('bugReport.uploading')
                : t('bugReport.submitting')
              : t('bugReport.submitReport')}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  )
}
