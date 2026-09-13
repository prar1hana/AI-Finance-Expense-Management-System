import { NavLink } from 'react-router-dom'

const nav = [
  { to: '/dashboard', label: 'Dashboard',  icon: '📊' },
  { to: '/expenses',  label: 'Expenses',   icon: '💳' },
  { to: '/income',    label: 'Income',     icon: '💰' },
  { to: '/budgets',   label: 'Budgets',    icon: '🎯' },
  { to: '/ai-chat',   label: 'AI Assistant', icon: '🤖' },
]

export default function Sidebar() {
  return (
    <aside className="w-64 bg-gray-900 border-r border-gray-800 flex flex-col">
      <div className="p-6 border-b border-gray-800">
        <h1 className="text-lg font-bold text-indigo-400">AI Finance</h1>
        <p className="text-xs text-gray-500 mt-0.5">Smart Money Manager</p>
      </div>
      <nav className="flex-1 p-4 space-y-1">
        {nav.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-indigo-600 text-white'
                  : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'
              }`
            }
          >
            <span>{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t border-gray-800">
        <p className="text-xs text-gray-600 text-center">Simulated data only</p>
      </div>
    </aside>
  )
}
