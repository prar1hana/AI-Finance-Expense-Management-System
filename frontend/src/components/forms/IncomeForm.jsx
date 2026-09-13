import { useState } from 'react'
import { INCOME_SOURCES } from '../../utils/constants'

export default function IncomeForm({ initial, onSubmit, loading }) {
  const today = new Date().toISOString().split('T')[0]
  const [form, setForm] = useState({
    description: initial?.description || '',
    amount: initial?.amount || '',
    source: initial?.source || '',
    date: initial?.date || today,
    notes: initial?.notes || '',
  })
  function set(field, val) { setForm(f => ({ ...f, [field]: val })) }

  function handleSubmit(e) {
    e.preventDefault()
    onSubmit({ ...form, amount: parseFloat(form.amount) })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label">Description</label>
        <input type="text" className="input" required placeholder="e.g. Monthly salary"
          value={form.description} onChange={e => set('description', e.target.value)} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="label">Amount ($)</label>
          <input type="number" className="input" required min="0.01" step="0.01" placeholder="0.00"
            value={form.amount} onChange={e => set('amount', e.target.value)} />
        </div>
        <div>
          <label className="label">Date</label>
          <input type="date" className="input" required
            value={form.date} onChange={e => set('date', e.target.value)} />
        </div>
      </div>
      <div>
        <label className="label">Source</label>
        <select className="input" required value={form.source} onChange={e => set('source', e.target.value)}>
          <option value="">Select source</option>
          {INCOME_SOURCES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
        </select>
      </div>
      <div>
        <label className="label">Notes (optional)</label>
        <textarea className="input resize-none" rows={2}
          value={form.notes} onChange={e => set('notes', e.target.value)} />
      </div>
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={loading} className="btn-primary px-6">
          {loading ? 'Saving...' : 'Save Income'}
        </button>
      </div>
    </form>
  )
}
