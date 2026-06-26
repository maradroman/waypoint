import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import {
  ArrowLeft,
  Bug,
  FileImage,
  FileVideo,
  Mail,
  Clock,
  Paperclip,
} from 'lucide-react'
import type { AdminBugReportListItem, AdminBugReportDetail } from '@/types/api'
import { format } from 'date-fns'

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function isImage(contentType: string): boolean {
  return contentType.startsWith('image/')
}

function AttachmentViewer({
  attachment,
}: {
  attachment: AdminBugReportDetail['attachments'][0]
}) {
  if (isImage(attachment.contentType)) {
    return (
      <div className="space-y-2">
        <div className="overflow-hidden rounded-lg border bg-muted/30">
          <img
            src={attachment.downloadUrl}
            alt={attachment.filename}
            className="mx-auto max-h-[60vh] w-auto object-contain"
          />
        </div>
        <p className="text-xs text-muted-foreground">
          {attachment.filename} — {formatSize(attachment.sizeBytes)}
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <div className="overflow-hidden rounded-lg border bg-muted/30">
        <video
          src={attachment.downloadUrl}
          controls
          className="mx-auto max-h-[60vh] w-full"
        />
      </div>
      <p className="text-xs text-muted-foreground">
        {attachment.filename} — {formatSize(attachment.sizeBytes)}
      </p>
    </div>
  )
}

function MetadataDisplay({ metadata }: { metadata: Record<string, unknown> }) {
  const entries = Object.entries(metadata)
  if (entries.length === 0) return null

  return (
    <div className="space-y-2">
      <p className="text-sm font-medium">Reproduction metadata</p>
      <div className="rounded-lg border bg-muted/30 p-3">
        <pre className="text-xs whitespace-pre-wrap break-all font-mono text-muted-foreground">
          {JSON.stringify(metadata, null, 2)}
        </pre>
      </div>
    </div>
  )
}

function ReportDetail({
  reportId,
  onBack,
}: {
  reportId: string
  onBack: () => void
}) {
  const { data: report, isLoading } = useQuery({
    queryKey: ['admin-bug-report', reportId],
    queryFn: () => api.get<AdminBugReportDetail>(`/admin/bug-reports/${reportId}`),
  })

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-48 w-full" />
      </div>
    )
  }

  if (!report) return null

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={onBack} className="gap-2">
        <ArrowLeft className="h-4 w-4" />
        Back to list
      </Button>

      <Card>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="space-y-1">
              <CardTitle className="text-lg">{report.description}</CardTitle>
              <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Mail className="h-3 w-3" />
                  {report.user.email}
                </span>
                <span className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {format(new Date(report.createdAt), "MMM d, yyyy 'at' HH:mm")}
                </span>
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <MetadataDisplay metadata={report.metadata} />

          {report.attachments.length > 0 ? (
            <div className="space-y-4">
              <p className="text-sm font-medium">
                Attachments ({report.attachments.length})
              </p>
              <div className="grid gap-6">
                {report.attachments.map((attachment) => (
                  <div key={attachment.id} className="space-y-2">
                    <div className="flex items-center gap-2 text-sm">
                      {isImage(attachment.contentType) ? (
                        <FileImage className="h-4 w-4 text-muted-foreground" />
                      ) : (
                        <FileVideo className="h-4 w-4 text-muted-foreground" />
                      )}
                      <span className="font-medium">{attachment.filename}</span>
                      <span className="text-muted-foreground">
                        {formatSize(attachment.sizeBytes)}
                      </span>
                    </div>
                    <AttachmentViewer attachment={attachment} />
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No attachments</p>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

export default function AdminPage() {
  const [selectedId, setSelectedId] = useState<string | null>(null)

  const { data: reports, isLoading } = useQuery({
    queryKey: ['admin-bug-reports'],
    queryFn: () => api.get<AdminBugReportListItem[]>('/admin/bug-reports'),
    enabled: selectedId === null,
  })

  if (selectedId) {
    return <ReportDetail reportId={selectedId} onBack={() => setSelectedId(null)} />
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Bug Reports</h1>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-24 w-full rounded-xl" />
          ))}
        </div>
      ) : reports?.length === 0 ? (
        <div className="flex flex-col items-center gap-4 py-20 text-muted-foreground">
          <Bug className="h-16 w-16" />
          <p className="text-lg">No bug reports yet</p>
          <p className="text-sm">Bug reports submitted by users will appear here</p>
        </div>
      ) : (
        <div className="space-y-3">
          {reports?.map((report) => (
            <Card
              key={report.id}
              className="cursor-pointer transition-shadow hover:shadow-md"
              onClick={() => setSelectedId(report.id)}
            >
              <CardContent className="flex items-start gap-4 py-4">
                <div className="rounded-lg bg-destructive/10 p-2">
                  <Bug className="h-5 w-5 text-destructive" />
                </div>
                <div className="flex-1 space-y-1">
                  <p className="text-sm font-medium line-clamp-2">
                    {report.description}
                  </p>
                  <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <Mail className="h-3 w-3" />
                      {report.user.email}
                    </span>
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      {format(new Date(report.createdAt), "MMM d, yyyy 'at' HH:mm")}
                    </span>
                    {report.attachmentCount > 0 && (
                      <span className="flex items-center gap-1">
                        <Paperclip className="h-3 w-3" />
                        {report.attachmentCount}
                      </span>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
