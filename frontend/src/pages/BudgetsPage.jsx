import { useState, useEffect, useCallback } from 'react'
import { getBudgets, createBudget, updateBudget, deleteBudget } from '../api/budgets'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import BudgetForm from '../components/forms/BudgetForm'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { formatCurrency, getCategoryLabel } from '../utils/formatters'
import { currentMonthYear } from '../utils/formatters'
import toast from 'react-hot-toast'

function BudgetCard({ budget, onEdit, onDelete }) {
  const pct = Math.min(budget.utilizationPercent, 100)
  const exceeded = budget.isExceeded
  return (
    <div className={`card border ${exceeded ? 'border-red-700/50' : 'border-gray-800'}`}>
      <div className="flex items-start justify-between mb-3">
        <div>
          <h4 className="font-medium text-gray-200">{getCategoryLabel(budget.category)}</h4>
          <p className="text-xs text-gray-500 mt-0.5">
            {budget.spentAmount != null ? formatCurrency(budget.spentAmount) : '$0.00'} / {formatCurrency(budget.limitAmount)}
          </p>
        </div>
        <div className="flex gap-2">
          <button onClick={() => onEdit(budget)} className="text-gray-500 hover:text-indigo-400 text-xs">Edit</button>
          <button onClick={() => onDelete(budget)} className="text-gray-500 hover:text-red-400 text-xs">Delete</button>
        </div>
      </div>
      <div className="w-full bg-gray-800 rounded-full h-2 overflow-hidden">
        <div
          className={`h-2 rounded-full transition-all ${exceeded ? 'bg-red-500' : pct >= 80 ? 'bg-amber-500' : 'bg-indigo-500'}`}
          style={{ width: `${pct}%` }}
        />
      </div>
      <div className="flex items-center justify-between mt-2">
        <span className={`text-xs ${exceeded ? 'text-red-400' : 'text-gray-500'}`}>
          {exceeded ? '⚠ Exceeded' : `${budget.utilizationPercent?.toFixed(1)}% used`}
        </span>
        <span className="text-xs text-gray-500">
          Remaining: {formatCurrency(Math.max(0, budget.limitAmount - (budget.spentAmount ?? 0)))}
        </span>
      </div>
    </div>
  )
}

export default function BudgetsPage() {
  const { month: curMonth, year: curYear } = currentMonthYear()
  const [budgets, setBudgets] = useState([])
  const [loading, setLoading] = useState(true)
  const [month, setMonth] = useState(curMonth)
  const [year, setYear] = useState(curYear)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [saving, setSaving] = useState(false)
  const [confirmLoading, setConfirmLoading] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const { data } = await getBudgets({ month, year })
      setBudgets(data)
    } catch { toast.error('Failed to load budgets') }
    finally { setLoading(false) }
  }, [month, year])

  useEffect(() => { load() }, [load])

  async function handleSave(formData) {
    setSaving(true)
    try {
      if (editing) { await updateBudget(editing.id, formData); toast.success('Budget updated') }
      else { await createBudget(formData); toast.success('Budget created') }
      setShowForm(false); setEditing(null); load()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to save') }
    finally { setSaving(false) }
  }

  async function handleDelete() {
    setConfirmLoading(true)
    try {
      await deleteBudget(deleting.id); toast.success('Budget deleted'); setDeleting(null); load()
    } catch { toast.error('Failed to delete') }
    finally { setConfirmLoading(false) }
  }

  const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
  const exceeded = budgets.filter(b => b.isExceeded).length

  return (
    <div className="space-y-4">
      <div className="card">
        <div className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="label">Month</label>
            <select className="input w-32" value={month} onChange={e => setMonth(parseInt(e.target.value))}>
              {monthNames.map((m, i) => <option key={i+1} value={i+1}>{m}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Year</label>
            <input type="number" className="input w-28" min="2000" value={year}
              onChange={e => setYear(parseInt(e.target.value))} />
          </div>
          <div className="ml-auto flex items-center gap-4">
            {exceeded > 0 && (
              <span className="text-sm text-red-400">⚠ {exceeded} budget{exceeded > 1 ? 's' : ''} exceeded</span>
            )}
            <button onClick={() => { setEditing(null); setShowForm(true) }} className="btn-primary">+ Add Budget</button>
          </div>
        </div>
      </div>

      {loading ? <LoadingSpinner className="py-12" /> : budgets.length === 0 ? (
        <div className="card text-center py-12 text-gray-600">
          <p>No budgets for {monthNames[month-1]} {year}</p>
          <button onClick={() => setShowForm(true)} className="btn-primary mt-4 text-sm">Create your first budget</button>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {budgets.map(b => (
            <BudgetCard key={b.id} budget={b}
              onEdit={b => { setEditing(b); setShowForm(true) }}
              onDelete={setDeleting} />
          ))}
        </div>
      )}

      <Modal isOpen={showForm} onClose={() => { setShowForm(false); setEditing(null) }}
        title={editing ? 'Edit Budget' : 'Add Budget'}>
        <BudgetForm initial={editing} onSubmit={handleSave} loading={saving} />
      </Modal>

      <ConfirmDialog isOpen={!!deleting} onClose={() => setDeleting(null)}
        onConfirm={handleDelete} loading={confirmLoading}
        title="Delete Budget"
        message={`Delete budget for ${getCategoryLabel(deleting?.category)}?`} />
    </div>
  )
}
