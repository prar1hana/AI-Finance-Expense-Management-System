import { useState } from 'react'
import { CATEGORIES } from '../../utils/constants'

export default function BudgetForm({ initial, onSubmit, loading }) {
  const now = new Date()
  const [form, setForm] = useState({
    category: initial?.category || '',
    limitAmount: initial?.limitAmount || '',
    month: initial?.month || now.getMonth() + 1,
    year: initial?.year || now.getFullYear(),
  })
  function set(field, val) { setForm(f => ({ ...f, [field]: val })) }

  function handleSubmit(e) {
    e.preventDefault()
    onSubmit({ ...form, limitAmount: parseFloat(form.limitAmount), month: parseInt(form.month), year: parseInt(form.year) })
  }

  const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label">Category</label>
        <select className="input" required value={form.category} onChange={e => set('category', e.target.value)}>
          <option value="">Select category</option>
          {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
        </select>
      </div>
      <div>
        <label className="label">Monthly Limit ($)</label>
        <input type="number" className="input" required min="0.01" step="0.01" placeholder="0.00"
          value={form.limitAmount} onChange={e => set('limitAmount', e.target.value)} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="label">Month</label>
          <select className="input" required value={form.month} onChange={e => set('month', e.target.value)}>
            {monthNames.map((m, i) => <option key={i+1} value={i+1}>{m}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Year</label>
          <input type="number" className="input" required min="2000" max="2100"
            value={form.year} onChange={e => set('year', e.target.value)} />
        </div>
      </div>
      <div className="flex justify-end pt-2">
        <button type="submit" disabled={loading} className="btn-primary px-6">
          {loading ? 'Saving...' : 'Save Budget'}
        </button>
      </div>
    </form>
  )
}
