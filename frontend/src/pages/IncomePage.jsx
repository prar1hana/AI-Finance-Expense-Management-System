import { useState, useEffect, useCallback } from 'react'
import { getIncomes, createIncome, updateIncome, deleteIncome } from '../api/income'
import Modal from '../components/common/Modal'
import ConfirmDialog from '../components/common/ConfirmDialog'
import IncomeForm from '../components/forms/IncomeForm'
import Badge from '../components/common/Badge'
import Pagination from '../components/common/Pagination'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { INCOME_SOURCES } from '../utils/constants'
import { formatCurrency, formatDate } from '../utils/formatters'
import toast from 'react-hot-toast'

export default function IncomePage() {
  const [incomes, setIncomes] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({ source: '' })
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [saving, setSaving] = useState(false)
  const [confirmLoading, setConfirmLoading] = useState(false)

  const load = useCallback(async (p = 0) => {
    setLoading(true)
    try {
      const params = { page: p, size: 10, sortDir: 'desc' }
      if (filters.source) params.source = filters.source
      const { data } = await getIncomes(params)
      setIncomes(data.content); setTotalPages(data.totalPages); setTotal(data.totalElements); setPage(p)
    } catch { toast.error('Failed to load income') }
    finally { setLoading(false) }
  }, [filters])

  useEffect(() => { load(0) }, [load])

  async function handleSave(formData) {
    setSaving(true)
    try {
      if (editing) { await updateIncome(editing.id, formData); toast.success('Income updated') }
      else { await createIncome(formData); toast.success('Income added') }
      setShowForm(false); setEditing(null); load(page)
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to save') }
    finally { setSaving(false) }
  }

  async function handleDelete() {
    setConfirmLoading(true)
    try {
      await deleteIncome(deleting.id); toast.success('Income deleted'); setDeleting(null); load(page)
    } catch { toast.error('Failed to delete') }
    finally { setConfirmLoading(false) }
  }

  return (
    <div className="space-y-4">
      <div className="card">
        <div className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="label">Source</label>
            <select className="input w-44" value={filters.source}
              onChange={e => setFilters({ source: e.target.value })}>
              <option value="">All sources</option>
              {INCOME_SOURCES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
            </select>
          </div>
          <button onClick={() => setFilters({ source: '' })} className="btn-secondary text-sm">Clear</button>
          <div className="ml-auto">
            <button onClick={() => { setEditing(null); setShowForm(true) }} className="btn-primary">+ Add Income</button>
          </div>
        </div>
      </div>

      <div className="card p-0 overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800">
          <h3 className="font-medium text-gray-200">Income</h3>
          <span className="text-sm text-gray-500">{total} total</span>
        </div>
        {loading ? <LoadingSpinner className="py-12" /> : incomes.length === 0 ? (
          <p className="text-center text-gray-600 py-12">No income records found</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-800 text-gray-500">
                  <th className="text-left px-6 py-3 font-medium">Description</th>
                  <th className="text-left px-4 py-3 font-medium">Source</th>
                  <th className="text-left px-4 py-3 font-medium">Date</th>
                  <th className="text-right px-6 py-3 font-medium">Amount</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody>
                {incomes.map(inc => (
                  <tr key={inc.id} className="border-b border-gray-800/50 hover:bg-gray-800/30">
                    <td className="px-6 py-3 text-gray-200">{inc.description}</td>
                    <td className="px-4 py-3"><Badge value={inc.source} /></td>
                    <td className="px-4 py-3 text-gray-400">{formatDate(inc.date)}</td>
                    <td className="px-6 py-3 text-right font-medium text-emerald-400">{formatCurrency(inc.amount)}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2 justify-end">
                        <button onClick={() => { setEditing(inc); setShowForm(true) }}
                          className="text-gray-500 hover:text-indigo-400 text-xs">Edit</button>
                        <button onClick={() => setDeleting(inc)}
                          className="text-gray-500 hover:text-red-400 text-xs">Delete</button>
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
        title={editing ? 'Edit Income' : 'Add Income'}>
        <IncomeForm initial={editing} onSubmit={handleSave} loading={saving} />
      </Modal>

      <ConfirmDialog isOpen={!!deleting} onClose={() => setDeleting(null)}
        onConfirm={handleDelete} loading={confirmLoading}
        title="Delete Income"
        message={`Delete "${deleting?.description}" (${formatCurrency(deleting?.amount)})?`} />
    </div>
  )
}
