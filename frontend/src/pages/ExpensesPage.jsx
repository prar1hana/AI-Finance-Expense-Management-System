import { useState, useEffect, useCallback } from 'react'
import { getExpenses, createExpense, updateExpense, deleteExpense } from '../api/expenses'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import ExpenseForm from '../components/forms/ExpenseForm'
import Badge from '../components/common/Badge'
import Pagination from '../components/common/Pagination'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { CATEGORIES } from '../utils/constants'
import { formatCurrency, formatDate } from '../utils/formatters'
import toast from 'react-hot-toast'

export default function ExpensesPage() {
  const [expenses, setExpenses] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({ category: '', startDate: '', endDate: '' })
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [saving, setSaving] = useState(false)
  const [confirmLoading, setConfirmLoading] = useState(false)

  const load = useCallback(async (p = 0) => {
    setLoading(true)
    try {
      const params = { page: p, size: 10, sortBy: 'date', sortDir: 'desc' }
      if (filters.category) params.category = filters.category
      if (filters.startDate) params.startDate = filters.startDate
      if (filters.endDate) params.endDate = filters.endDate
      const { data } = await getExpenses(params)
      setExpenses(data.content)
      setTotalPages(data.totalPages)
      setTotal(data.totalElements)
      setPage(p)
    } catch { toast.error('Failed to load expenses') }
    finally { setLoading(false) }
  }, [filters])

  useEffect(() => { load(0) }, [load])

  async function handleSave(formData) {
    setSaving(true)
    try {
      if (editing) { await updateExpense(editing.id, formData); toast.success('Expense updated') }
      else { await createExpense(formData); toast.success('Expense added') }
      setShowForm(false); setEditing(null); load(page)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save')
    } finally { setSaving(false) }
  }

  async function handleDelete() {
    setConfirmLoading(true)
    try {
      await deleteExpense(deleting.id)
      toast.success('Expense deleted')
      setDeleting(null)
      load(page)
    } catch { toast.error('Failed to delete') }
    finally { setConfirmLoading(false) }
  }

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="card">
        <div className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="label">Category</label>
            <select className="input w-44" value={filters.category}
              onChange={e => setFilters(f => ({ ...f, category: e.target.value }))}>
              <option value="">All categories</option>
              {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
            </select>
          </div>
          <div>
            <label className="label">From</label>
            <input type="date" className="input w-40" value={filters.startDate}
              onChange={e => setFilters(f => ({ ...f, startDate: e.target.value }))} />
          </div>
          <div>
            <label className="label">To</label>
            <input type="date" className="input w-40" value={filters.endDate}
              onChange={e => setFilters(f => ({ ...f, endDate: e.target.value }))} />
          </div>
          <button onClick={() => setFilters({ category: '', startDate: '', endDate: '' })}
            className="btn-secondary text-sm">Clear</button>
          <div className="ml-auto">
            <button onClick={() => { setEditing(null); setShowForm(true) }} className="btn-primary">
              + Add Expense
            </button>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800">
          <h3 className="font-medium text-gray-200">Expenses</h3>
          <span className="text-sm text-gray-500">{total} total</span>
        </div>
        {loading ? <LoadingSpinner className="py-12" /> : expenses.length === 0 ? (
          <p className="text-center text-gray-600 py-12">No expenses found</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-800 text-gray-500">
                  <th className="text-left px-6 py-3 font-medium">Description</th>
                  <th className="text-left px-4 py-3 font-medium">Category</th>
                  <th className="text-left px-4 py-3 font-medium">Date</th>
                  <th className="text-right px-6 py-3 font-medium">Amount</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody>
                {expenses.map(exp => (
                  <tr key={exp.id} className="border-b border-gray-800/50 hover:bg-gray-800/30">
                    <td className="px-6 py-3 text-gray-200">{exp.description}</td>
                    <td className="px-4 py-3"><Badge value={exp.category} /></td>
                    <td className="px-4 py-3 text-gray-400">{formatDate(exp.date)}</td>
                    <td className="px-6 py-3 text-right font-medium text-red-400">{formatCurrency(exp.amount)}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2 justify-end">
                        <button onClick={() => { setEditing(exp); setShowForm(true) }}
                          className="text-gray-500 hover:text-indigo-400 transition-colors text-xs">Edit</button>
                        <button onClick={() => setDeleting(exp)}
                          className="text-gray-500 hover:text-red-400 transition-colors text-xs">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="px-6 pb-4">
          <Pagination page={page} totalPages={totalPages} onPageChange={p => load(p)} />
        </div>
      </div>

      <Modal isOpen={showForm} onClose={() => { setShowForm(false); setEditing(null) }}
        title={editing ? 'Edit Expense' : 'Add Expense'}>
        <ExpenseForm initial={editing} onSubmit={handleSave} loading={saving} />
      </Modal>

      <ConfirmDialog isOpen={!!deleting} onClose={() => setDeleting(null)}
        onConfirm={handleDelete} loading={confirmLoading}
        title="Delete Expense"
        message={`Delete "${deleting?.description}" (${formatCurrency(deleting?.amount)})?`} />
    </div>
  )
}
