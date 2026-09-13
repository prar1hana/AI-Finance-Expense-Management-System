import { useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

const titles = {
  '/dashboard': 'Dashboard',
  '/expenses':  'Expenses',
  '/income':    'Income',
  '/budgets':   'Budgets',
  '/ai-chat':   'AI Assistant',
}

export default function TopBar() {
  const { currentUser, logout } = useAuth()
  const { pathname } = useLocation()

  return (
    <header className="bg-gray-900 border-b border-gray-800 px-6 py-4 flex items-center justify-between">
      <h2 className="text-lg font-semibold text-gray-100">{titles[pathname] || 'Finance'}</h2>
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-400">
          {currentUser?.name}
        </span>
        <button
          onClick={logout}
          className="text-sm text-gray-400 hover:text-red-400 transition-colors"
        >
          Sign out
        </button>
      </div>
    </header>
  )
}
