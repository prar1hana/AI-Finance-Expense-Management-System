import { useState, useEffect } from 'react'
import StatCard from '../components/common/StatCard'
import MonthlySpendingChart from '../components/charts/MonthlySpendingChart'
import CategoryBreakdownChart from '../components/charts/CategoryBreakdownChart'
import BudgetUtilizationChart from '../components/charts/BudgetUtilizationChart'
import Badge from '../components/common/Badge'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { getDashboardSummary, getMonthlySpending, getRecentTransactions } from '../api/dashboard'
import { getBudgets } from '../api/budgets'
import { formatCurrency, formatDate, currentMonthYear } from '../utils/formatters'

export default function DashboardPage() {
  const { month, year } = currentMonthYear()
  const [summary, setSummary]       = useState(null)
  const [monthly, setMonthly]       = useState([])
  const [recent, setRecent]         = useState([])
  const [budgets, setBudgets]       = useState([])
  const [loading, setLoading]       = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [s, m, r, b] = await Promise.all([
          getDashboardSummary({ month, year }),
          getMonthlySpending({ months: 6 }),
          getRecentTransactions({ limit: 8 }),
          getBudgets({ month, year }),
        ])
        setSummary(s.data)
        setMonthly(m.data)
        setRecent(r.data)
        setBudgets(b.data)
      } catch { /* errors shown empty */ }
      finally { setLoading(false) }
    }
    load()
  }, [])

  if (loading) return <LoadingSpinner className="h-64" />

  const balance = summary?.netBalance ?? 0
  const exceeded = budgets.filter(b => b.isExceeded)

  return (
    <div className="space-y-6">
      {exceeded.length > 0 && (
        <div className="bg-red-900/30 border border-red-700 rounded-xl p-4 flex items-center gap-3">
          <span className="text-red-400 text-lg">⚠️</span>
          <p className="text-sm text-red-300">
            <strong>Budget exceeded</strong> in {exceeded.length} categor{exceeded.length > 1 ? 'ies' : 'y'}:{' '}
            {exceeded.map(b => b.category.replace(/_/g, ' ')).join(', ')}
          </p>
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard title="Total Income" value={formatCurrency(summary?.totalIncome)} color="green"
          subtitle={`${new Date().toLocaleString('default',{month:'long'})} ${year}`} />
        <StatCard title="Total Expenses" value={formatCurrency(summary?.totalExpenses)} color="red"
          subtitle={`${new Date().toLocaleString('default',{month:'long'})} ${year}`} />
        <StatCard title="Net Balance" value={formatCurrency(balance)}
          color={balance >= 0 ? 'green' : 'red'}
          subtitle={balance >= 0 ? 'In the black' : 'Over budget'} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <MonthlySpendingChart data={monthly} />
        <CategoryBreakdownChart data={summary?.categoryBreakdown ?? []} />
      </div>

      <BudgetUtilizationChart data={budgets} />

      <div className="card">
        <h3 className="text-sm font-semibold text-gray-400 mb-4">Recent Transactions</h3>
        {recent.length === 0 ? (
          <p className="text-gray-600 text-sm text-center py-8">No transactions yet</p>
        ) : (
          <div className="space-y-2">
            {recent.map((tx, i) => (
              <div key={i} className="flex items-center justify-between py-2 border-b border-gray-800 last:border-0">
                <div className="flex items-center gap-3">
                  <span className="text-lg">{tx.type === 'INCOME' ? '⬆️' : '⬇️'}</span>
                  <div>
                    <p className="text-sm text-gray-200">{tx.description}</p>
                    <p className="text-xs text-gray-500">{formatDate(tx.date)}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Badge value={tx.category} />
                  <span className={`text-sm font-medium ${tx.type === 'INCOME' ? 'text-emerald-400' : 'text-red-400'}`}>
                    {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
