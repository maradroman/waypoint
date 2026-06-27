import { useState, useEffect, Fragment, useMemo, useCallback } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { api, plannedFunds as plannedFundsApi } from '@/lib/api'
import { useFormatMoney } from '@/lib/format'
import type { Goal, Deposit, GoalAnalytics } from '@/types/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { CalendarIcon, InfoIcon, LayoutGrid, ArrowLeft } from 'lucide-react'
import { uk, pl, enUS } from 'date-fns/locale'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

export default function PlanningPage() {
  const { t, i18n } = useTranslation()
  const formatMoney = useFormatMoney()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [selectedGoalId, setSelectedGoalId] = useState<string>('')
  const [plannedFunds, setPlannedFunds] = useState<Record<string, number>>({}) // date -> amount in cents
  const [popoverDate, setPopoverDate] = useState<string | null>(null)
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(() => {
    const today = new Date()
    return new Date(today.getFullYear(), today.getMonth() + 1, today.getDate())
  })
  const [isCalendarOpen, setIsCalendarOpen] = useState(false)
  const [mobileView, setMobileView] = useState<'scroll' | 'grid'>(() => {
    // Always load from localStorage if available
    const saved = localStorage.getItem('planning-mobile-view')
    return (saved === 'grid' || saved === 'scroll') ? saved : 'scroll'
  })
  const [selectedMonthIndex, setSelectedMonthIndex] = useState<number | null>(null)
  const [isMobile, setIsMobile] = useState(() => window.innerWidth < 768)

  // Track screen size changes
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768)
    }
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  // Save mobile view preference to localStorage (only when on mobile screen)
  useEffect(() => {
    if (isMobile) {
      localStorage.setItem('planning-mobile-view', mobileView)
    }
  }, [mobileView, isMobile])

  const getLocale = () => {
    switch (i18n.language) {
      case 'uk':
        return uk
      case 'pl':
        return pl
      default:
        return enUS
    }
  }

  const { data: goals } = useQuery({
    queryKey: ['goals'],
    queryFn: () => api.get<Goal[]>('/goals'),
  })

  // Auto-select goal from URL param or if only one is available
  useEffect(() => {
    const goalParam = searchParams.get('goal')
    if (goalParam && goals?.some(g => g.id === goalParam)) {
      setSelectedGoalId(goalParam)
    } else if (goals && goals.length === 1 && !selectedGoalId) {
      setSelectedGoalId(goals[0].id)
    }
  }, [goals, selectedGoalId, searchParams])

  const { data: analytics } = useQuery({
    queryKey: ['analytics', selectedGoalId],
    queryFn: () => api.get<GoalAnalytics>(`/goals/${selectedGoalId}/analytics`),
    enabled: !!selectedGoalId,
  })

  const { data: deposits } = useQuery({
    queryKey: ['deposits', selectedGoalId],
    queryFn: () => api.get<Deposit[]>(`/goals/${selectedGoalId}/deposits`),
    enabled: !!selectedGoalId,
  })

  // Fetch planned funds from backend
  const { data: plannedFundsData } = useQuery({
    queryKey: ['plannedFunds', selectedGoalId],
    queryFn: () => plannedFundsApi.list(selectedGoalId),
    enabled: !!selectedGoalId,
  })

  // Sync plannedFundsData with local state
  useEffect(() => {
    if (plannedFundsData) {
      const fundsMap: Record<string, number> = {}
      plannedFundsData.forEach(fund => {
        fundsMap[fund.date] = fund.amount
      })
      setPlannedFunds(fundsMap)
    }
  }, [plannedFundsData])

  // Mutation for upserting planned funds
  const upsertMutation = useMutation({
    mutationFn: ({ date, amount }: { date: string; amount: number }) =>
      plannedFundsApi.upsert(selectedGoalId, date, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plannedFunds', selectedGoalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', selectedGoalId] })
    },
  })

  // Mutation for deleting planned funds
  const deleteMutation = useMutation({
    mutationFn: (date: string) => plannedFundsApi.delete(selectedGoalId, date),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plannedFunds', selectedGoalId] })
      queryClient.invalidateQueries({ queryKey: ['analytics', selectedGoalId] })
    },
  })

  const calculateProjectedBalance = (targetDate: Date) => {
    const currentBalance = analytics?.walletBalance ?? 0
    let projectedBalance = currentBalance

    // Sum all planned funds up to and including the target date
    Object.entries(plannedFunds).forEach(([dateKey, amount]) => {
      const fundDate = new Date(dateKey)
      if (fundDate <= targetDate) {
        projectedBalance += amount
      }
    })

    return projectedBalance
  }

  const projectedBalance = selectedDate ? calculateProjectedBalance(selectedDate) : 0

  // Generate 12 months starting from current month
  const generateMonths = () => {
    const months = []
    const today = new Date()
    for (let i = 0; i < 12; i++) {
      const date = new Date(today.getFullYear(), today.getMonth() + i, 1)
      months.push(date)
    }
    return months
  }

  const months = useMemo(() => generateMonths(), [])

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear()
    const month = date.getMonth()
    const firstDay = new Date(year, month, 1)
    const lastDay = new Date(year, month + 1, 0)
    const daysInMonth = lastDay.getDate()
    const startingDayOfWeek = firstDay.getDay()

    return { daysInMonth, startingDayOfWeek }
  }

  const formatDateKey = (year: number, month: number, day: number) => {
    return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }


  // Reset popover when switching months in grid view
  useEffect(() => {
    setPopoverDate(null)
  }, [selectedMonthIndex])

  const getMonthTranslationKey = (month: number) => {
    const monthKeys = [
      'january', 'february', 'march', 'april', 'may', 'june',
      'july', 'august', 'september', 'october', 'november', 'december'
    ]
    return monthKeys[month]
  }

  const MonthCalendar = ({
    monthDate,
    plannedFunds,
    deposits,
    popoverDate,
    setPopoverDate,
    analytics,
  }: {
    monthDate: Date;
    plannedFunds: Record<string, number>;
    deposits: Deposit[] | undefined;
    popoverDate: string | null;
    setPopoverDate: (date: string | null) => void;
    analytics: GoalAnalytics | undefined;
  }) => {
    const [tempAmount, setTempAmount] = useState('')

    // Sync tempAmount when popover opens
    useEffect(() => {
      if (popoverDate) {
        const existingAmount = plannedFunds[popoverDate]
        setTempAmount(existingAmount ? String(existingAmount / 100) : '')
      }
    }, [popoverDate, plannedFunds])

    const handleSaveFund = useCallback((dateKey: string) => {
      const amount = parseFloat(tempAmount)
      if (amount > 0) {
        const amountInCents = Math.round(amount * 100)
        // Optimistic update
        setPlannedFunds(prev => ({
          ...prev,
          [dateKey]: amountInCents
        }))
        // Save to backend
        upsertMutation.mutate({ date: dateKey, amount: amountInCents })
      } else {
        // Remove if amount is 0 or invalid
        setPlannedFunds(prev => {
          const newFunds = { ...prev }
          delete newFunds[dateKey]
          return newFunds
        })
        // Delete from backend
        deleteMutation.mutate(dateKey)
      }
      setPopoverDate(null)
      setTempAmount('')
    }, [tempAmount])

    const year = monthDate.getFullYear()
    const month = monthDate.getMonth()
    const { daysInMonth, startingDayOfWeek } = getDaysInMonth(monthDate)

    const monthName = `${t(`planning.months.${getMonthTranslationKey(month)}`)} ${year}`
    const weekDays = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat']

    const days = []
    const today = new Date()
    today.setHours(0, 0, 0, 0) // Reset time to start of day for accurate comparison

    // Empty cells for days before month starts
    for (let i = 0; i < startingDayOfWeek; i++) {
      days.push(<div key={`empty-${i}`} className="aspect-square" />)
    }

    // Days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const dateKey = formatDateKey(year, month, day)
      const currentDate = new Date(year, month, day)
      const isPast = currentDate < today
      const hasPlannedFund = plannedFunds[dateKey] !== undefined
      const isToday = today.toDateString() === currentDate.toDateString()

      // Check if this is the completion date
      const isCompletionDate = analytics?.potentialCompletionDate === dateKey

      // Find deposits for this date
      const dateDeposits = deposits?.filter(d => {
        const depositDate = new Date(d.createdAt)
        return depositDate.getFullYear() === year &&
               depositDate.getMonth() === month &&
               depositDate.getDate() === day
      }) || []

      const totalDeposited = dateDeposits.reduce((sum, d) => sum + d.amount, 0)
      const hasDeposits = dateDeposits.length > 0

      days.push(
        <Fragment key={dateKey}>
          {isPast && !hasDeposits ? (
            // Past dates with no deposits - not clickable
            <div className="aspect-square rounded-md text-sm flex items-center justify-center relative">
              {day}
            </div>
          ) : (
            <Popover
              open={popoverDate === dateKey}
              onOpenChange={(open) => {
                if (open) {
                  setPopoverDate(dateKey)
                  const existingAmount = plannedFunds[dateKey]
                  setTempAmount(existingAmount ? String(existingAmount / 100) : '')
                } else {
                  setPopoverDate(null)
                  setTempAmount('')
                }
              }}
            >
              <PopoverTrigger asChild>
                <button
                  type="button"
                  className={`
                    aspect-square rounded-md text-sm flex items-center justify-center relative cursor-pointer
                    hover:bg-accent hover:text-accent-foreground
                    ${isToday ? 'ring-2 ring-ring' : ''}
                    ${!isPast && isCompletionDate ? 'bg-accent/20 text-primary font-semibold ring-2 ring-accent ring-offset-0' : !isPast && hasPlannedFund ? 'bg-primary text-primary-foreground font-semibold' : ''}
                  `}
                >
                  {day}
                  {hasDeposits && (
                    <span className="absolute -top-1 -right-1 h-2 w-2 sm:h-2.5 sm:w-2.5 rounded-full bg-primary" />
                  )}
                </button>
              </PopoverTrigger>
              <PopoverContent className="w-64" side="top">
                <div className="space-y-4">
                  <div>
                    <h4 className="font-semibold mb-2">
                      {day} {t(`planning.months.${getMonthTranslationKey(month)}`)} {year}
                    </h4>
                    {hasDeposits && (
                      <p className="text-sm text-muted-foreground">
                        {t('planning.deposited', { amount: formatMoney(totalDeposited) })}
                      </p>
                    )}
                  </div>
                  {!isPast && (
                    <>
                      <div className="space-y-2">
                        <Label>{t('planning.expectedFunds')}</Label>
                        <Input
                          type="number"
                          value={tempAmount}
                          onChange={(e) => setTempAmount(e.target.value)}
                          placeholder="0.00"
                          step="0.01"
                          min="0"
                        />
                        {plannedFunds[dateKey] && (
                          <p className="text-xs text-muted-foreground">
                            {t('planning.current', { amount: formatMoney(plannedFunds[dateKey]) })}
                          </p>
                        )}
                      </div>
                      <div className="flex gap-2">
                        <Button onClick={() => handleSaveFund(dateKey)} className="flex-1">
                          {t('planning.save')}
                        </Button>
                        {plannedFunds[dateKey] && (
                          <Button
                            variant="outline"
                            onClick={() => {
                              setPlannedFunds(prev => {
                                const newFunds = { ...prev }
                                delete newFunds[dateKey]
                                return newFunds
                              })
                              deleteMutation.mutate(dateKey)
                              setPopoverDate(null)
                              setTempAmount('')
                            }}
                          >
                            {t('planning.remove')}
                          </Button>
                        )}
                      </div>
                    </>
                  )}
                </div>
              </PopoverContent>
            </Popover>
          )}
        </Fragment>
      )
    }

    return (
      <Card key={monthName} className="h-full flex flex-col">
        <CardHeader className="pb-3">
          <CardTitle className="text-base">{monthName}</CardTitle>
        </CardHeader>
        <CardContent className="flex-1">
          <div className="grid grid-cols-7 gap-1 mb-2">
            {weekDays.map(day => (
              <div key={day} className="text-center text-xs font-medium text-muted-foreground py-1">
                {t(`planning.weekDays.${day}`)}
              </div>
            ))}
          </div>
          <div className="grid grid-cols-7 gap-1">
            {days}
          </div>
        </CardContent>
      </Card>
    )
  }

  const goalParam = searchParams.get('goal')

  return (
    <div className="space-y-6 pb-8">
      <div>
        {goalParam && (
          <Button
            variant="ghost"
            size="sm"
            className="mb-4 -ml-2"
            onClick={() => navigate(`/goals/${goalParam}`)}
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            {t('common.back')}
          </Button>
        )}
        <h1 className="text-3xl font-bold tracking-tight">{t('planning.title')}</h1>
        <p className="text-muted-foreground mt-2">{t('planning.description')}</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>
            {t('planning.projectedBalance')} {t('planning.forGoal')}{' '}
            {goals && goals.length > 1 ? (
              (() => {
                const selectedGoal = goals.find(g => g.id === selectedGoalId)
                return (
                  <span className="inline-block ml-2 py-1">
                    <Select value={selectedGoalId} onValueChange={setSelectedGoalId}>
                      <SelectTrigger className="inline-flex w-auto align-middle">
                        {selectedGoal ? (
                          <span className="flex items-center">
                            {selectedGoal.icon && <span className="mr-2">{selectedGoal.icon}</span>}
                            {selectedGoal.title}
                          </span>
                        ) : (
                          <SelectValue placeholder={t('planning.selectGoalPlaceholder')} />
                        )}
                      </SelectTrigger>
                      <SelectContent>
                        {goals.map((goal) => (
                          <SelectItem key={goal.id} value={goal.id}>
                            {goal.icon && <span className="mr-2">{goal.icon}</span>}
                            {goal.title}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </span>
                )
              })()
            ) : (
              goals && goals[0] && (
                <>
                  {goals[0].icon && <span className="mr-2">{goals[0].icon}</span>}
                  {goals[0].title}
                </>
              )
            )}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {selectedGoalId && (
            <>
              <div className="flex flex-wrap items-center gap-3">
                <span className="whitespace-nowrap">{t('planning.forDate')}</span>
                <Popover open={isCalendarOpen} onOpenChange={setIsCalendarOpen}>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      className="w-auto justify-start text-left font-normal"
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {selectedDate ? (
                        `${selectedDate.getDate()} ${t(`planning.months.${getMonthTranslationKey(selectedDate.getMonth())}`)} ${selectedDate.getFullYear()}`
                      ) : (
                        <span>{t('planning.pickDate')}</span>
                      )}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0">
                    <Calendar
                      mode="single"
                      selected={selectedDate}
                      onSelect={(date) => {
                        setSelectedDate(date)
                        setIsCalendarOpen(false)
                      }}
                      disabled={(date) => {
                        const today = new Date()
                        today.setHours(0, 0, 0, 0)
                        return date < today
                      }}
                      className="rounded-lg border min-w-[240px]"
                      locale={getLocale()}
                    />
                  </PopoverContent>
                </Popover>
                <span className="font-bold text-2xl whitespace-nowrap">{formatMoney(projectedBalance)}</span>
                <TooltipProvider delayDuration={200}>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <InfoIcon className="h-4 w-4 text-muted-foreground cursor-help flex-shrink-0" />
                    </TooltipTrigger>
                    <TooltipContent className="p-4">
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between gap-6">
                          <span className="text-muted-foreground">{t('planning.currentBalance')}</span>
                          <span className="font-medium">{formatMoney(analytics?.walletBalance ?? 0)}</span>
                        </div>
                        <div className="flex justify-between gap-6">
                          <span className="text-muted-foreground">{t('planning.plannedFunds')}</span>
                          <span className="font-medium">{formatMoney(projectedBalance - (analytics?.walletBalance ?? 0))}</span>
                        </div>
                        <div className="border-t pt-2 mt-2 flex justify-between gap-6">
                          <span className="font-semibold">{t('planning.projectedTotal')}</span>
                          <span className="font-bold">{formatMoney(projectedBalance)}</span>
                        </div>
                      </div>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
              {analytics && (
                <div className="text-sm">
                  {analytics.totalMilestoneCost > 0 && projectedBalance >= analytics.totalMilestoneCost ? (
                    <span className="text-accent font-medium">
                      {t('planning.goalCanBeCompleted')}
                    </span>
                  ) : analytics.totalMilestoneCost > 0 ? (
                    <span className="text-muted-foreground">
                      {t('planning.remaining')}: {formatMoney(analytics.totalMilestoneCost - projectedBalance)}
                    </span>
                  ) : null}
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {selectedGoalId && (
        <>
          {isMobile ? (
            <>
              {/* Mobile view toggle */}
              <div className="flex justify-between items-center">
                {mobileView === 'grid' && selectedMonthIndex !== null ? (
                  <Button
                    variant="ghost"
                    onClick={() => setSelectedMonthIndex(null)}
                  >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    {t('common.back')}
                  </Button>
                ) : (
                  <div />
                )}
                <Tabs value={mobileView} onValueChange={(v) => setMobileView(v as 'scroll' | 'grid')}>
                  <TabsList>
                    <TabsTrigger value="scroll">
                      <CalendarIcon className="h-4 w-4" />
                    </TabsTrigger>
                    <TabsTrigger value="grid">
                      <LayoutGrid className="h-4 w-4" />
                    </TabsTrigger>
                  </TabsList>
                </Tabs>
              </div>

              {mobileView === 'grid' ? (
                selectedMonthIndex === null ? (
                  /* Mobile grid view - month selection */
                  <div className="grid grid-cols-3 gap-4">
                    {months.map((month, index) => (
                      <Button
                        key={month.toISOString()}
                        variant="outline"
                        onClick={() => setSelectedMonthIndex(index)}
                        className="h-20 text-sm whitespace-normal"
                      >
                        {t(`planning.months.${getMonthTranslationKey(month.getMonth())}`)} {month.getFullYear()}
                      </Button>
                    ))}
                  </div>
                ) : (
                  /* Mobile grid view - single month calendar */
                  <div style={{ isolation: 'isolate' }}>
                    <MonthCalendar
                      monthDate={months[selectedMonthIndex]}
                      plannedFunds={plannedFunds}
                      deposits={deposits}
                      popoverDate={popoverDate}
                      setPopoverDate={setPopoverDate}
                      analytics={analytics}
                    />
                  </div>
                )
              ) : (
                /* Mobile: horizontal scroll */
                <div className="overflow-x-auto pb-4">
                  <div className="flex gap-4">
                    {months.map(month => (
                      <div key={month.toISOString()} className="flex-shrink-0 w-[300px]">
                        <MonthCalendar
                          monthDate={month}
                          plannedFunds={plannedFunds}
                          deposits={deposits}
                          popoverDate={popoverDate}
                          setPopoverDate={setPopoverDate}
                          analytics={analytics}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          ) : (
            /* Desktop/Tablet: always show full grid (2 cols on md, 4 cols on lg) */
            <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
              {months.map(month => (
                <div key={month.toISOString()} style={{ isolation: 'isolate' }}>
                  <MonthCalendar
                    monthDate={month}
                    plannedFunds={plannedFunds}
                    deposits={deposits}
                    popoverDate={popoverDate}
                    setPopoverDate={setPopoverDate}
                    analytics={analytics}
                  />
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
