import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '@/stores/auth'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import {
  Sheet,
  SheetContent,
  SheetTrigger,
} from '@/components/ui/sheet'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'
import { ReportBugButton } from '@/components/report-bug-dialog'
import {
  Target,
  LayoutDashboard,
  Menu,
  User,
  LogOut,
  Settings,
  Bug,
  Calendar,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react'

const navItems = [
  { href: '/dashboard', labelKey: 'nav.dashboard' as const, icon: LayoutDashboard },
  { href: '/goals', labelKey: 'nav.goals' as const, icon: Target },
  { href: '/planning', labelKey: 'nav.planning' as const, icon: Calendar },
]

function SidebarContent({ onNavigate, isCollapsed }: { onNavigate?: () => void; isCollapsed?: boolean }) {
  const location = useLocation()
  const navigate = useNavigate()
  const logout = useAuth((s) => s.logout)
  const user = useAuth((s) => s.user)
  const isAdmin = useAuth((s) => s.isAdmin)
  const { t } = useTranslation()

  const handleNavClick = () => {
    onNavigate?.()
  }

  return (
    <div className="flex h-full flex-col">
      <div className="p-4">
        <Link to="/dashboard" className={`flex items-center gap-2 font-semibold ${isCollapsed ? 'justify-center' : ''}`} onClick={handleNavClick}>
          <Target className="h-5 w-5" />
          {!isCollapsed && <span>Waypoint</span>}
        </Link>
      </div>
      <Separator />
      <nav className="flex-1 space-y-1 p-2">
        {navItems.map((item) => {
          const Icon = item.icon
          const active = location.pathname === item.href
          return (
            <Link
              key={item.href}
              to={item.href}
              onClick={handleNavClick}
              className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${
                active
                  ? 'bg-primary text-primary-foreground'
                  : 'hover:bg-muted'
              } ${isCollapsed ? 'justify-center' : ''}`}
              title={isCollapsed ? t(item.labelKey) : undefined}
            >
              <Icon className="h-4 w-4" />
              {!isCollapsed && t(item.labelKey)}
            </Link>
          )
        })}
      </nav>
      {isAdmin && (
        <>
          <Separator />
          <nav className="space-y-1 p-2">
            <Link
              to="/admin"
              onClick={handleNavClick}
              className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${
                location.pathname === '/admin'
                  ? 'bg-primary text-primary-foreground'
                  : 'hover:bg-muted'
              } ${isCollapsed ? 'justify-center' : ''}`}
              title={isCollapsed ? t('nav.bugReports') : undefined}
            >
              <Bug className="h-4 w-4" />
              {!isCollapsed && t('nav.bugReports')}
            </Link>
          </nav>
        </>
      )}
      <Separator />
      <div className="space-y-1 p-2">
        {!isCollapsed && <ReportBugButton />}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className={`w-full gap-2 ${isCollapsed ? 'justify-center px-0' : 'justify-start'}`} title={isCollapsed ? (user?.name ?? t('nav.user')) : undefined}>
              <User className="h-4 w-4" />
              {!isCollapsed && <span className="text-sm">{user?.name ?? t('nav.user')}</span>}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuItem onClick={() => { navigate('/settings'); handleNavClick() }}>
              <Settings className="mr-2 h-4 w-4" />
              {t('nav.settings')}
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => { logout(); navigate('/login'); handleNavClick() }}>
              <LogOut className="mr-2 h-4 w-4" />
              {t('nav.signOut')}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  )
}

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => {
    const saved = localStorage.getItem('sidebar-collapsed')
    return saved === 'true'
  })

  const toggleSidebar = () => {
    setSidebarCollapsed(prev => {
      const newValue = !prev
      localStorage.setItem('sidebar-collapsed', String(newValue))
      return newValue
    })
  }

  return (
    <div className="flex h-screen overflow-hidden">
      <aside className={`hidden h-full border-r bg-background md:block transition-all relative ${sidebarCollapsed ? 'w-16' : 'w-60'}`}>
        <SidebarContent isCollapsed={sidebarCollapsed} />
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleSidebar}
          className="absolute -right-3 top-[14px] z-50 h-6 w-6 rounded-full border bg-background shadow-sm hover:bg-accent"
        >
          {sidebarCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
        </Button>
      </aside>
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="z-40 shrink-0 border-b bg-background md:hidden">
          <div className="flex h-12 items-center px-3">
            <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-60 p-0">
                <SidebarContent onNavigate={() => setMobileMenuOpen(false)} />
              </SheetContent>
            </Sheet>
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-3 md:p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
