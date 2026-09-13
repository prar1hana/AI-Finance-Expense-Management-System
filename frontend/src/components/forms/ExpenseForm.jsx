import { useState } from 'react'
import { CATEGORIES } from '../../utils/constants'
import { categorizeExpense } from '../../api/ai'
import toast from 'react-hot-toast'

export default function ExpenseForm({ initial, onSubmit, loading }) {
  const today = new Date().toISOString().split('T')[0]
  const [form, setForm] = useState({
    description: initial?.description || '',
    amount: initial?.amount || '',
    category: initial?.category || '',
    date: initial?.date || today,
    notes: initial?.notes || '',
  })
  const [suggesting, setSuggesting] = useState(false)

  function set(field, val) { setForm(f => ({ ...f, [field]: val })) }

  async function suggestCategory() {
    if (!form.description.trim()) { toast.error('Enter a description first'); return }
    setSuggesting(true)
    try {
      const { data } = await categorizeExpense(form.description)
      set('category', data.suggestedCategory)
      toast.success(`Suggested: ${data.suggestedCategory.replace(/_/g, ' ')}`)
    } catch {
      toast.error('Could not suggest category')
    } finally {
      setSuggesting(false)
    }
  }

  function handleSubmit(e) {
    e.preventDefault()
    onSubmit({ ...form, amount: parseFloat(form.amount) })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label">Description</label>
        <div className="flex gap-2">
          <input type="text" className="input" required placeholder="e.g. Uber to office"
            value={form.description} onChange={e => set('description', e.target.value)} />
          <button type="button" onClick={suggestCategory} disabled={suggesting}
            className="btn-secondary whitespace-nowrap text-xs px-3">
            {suggesting ? '...' : '🤖 AI'}
          </button>
        </div>
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
        <label className="label">Category</label>
        <select className="input" required value={form.category} onChange={e => set('category', e.target.value)}>
          <option value="">Select category</option>
          {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
        </select>
      </div>
      <div>
        <label className="label">Notes (optional)</label>
        <textarea className="input resize-none" rows={2} placeholder="Any additional notes"
          value={form.notes} onChange={e => set('notes', e.target.value)} />
      </div>
      <div className="flex gap-3 justify-end pt-2">
        <button type="submit" disabled={loading} className="btn-primary px-6">
          {loading ? 'Saving...' : 'Save Expense'}
        </button>
      </div>
    </form>
  )
}
