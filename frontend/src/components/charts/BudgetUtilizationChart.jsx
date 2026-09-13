import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts'
import { formatCurrency } from '../../utils/formatters'
import { getCategoryLabel } from '../../utils/formatters'

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-gray-800 border border-gray-700 rounded-lg p-3 text-sm space-y-1">
      <p className="text-gray-300 font-medium">{getCategoryLabel(label)}</p>
      {payload.map(p => (
        <p key={p.name} style={{ color: p.color }}>
          {p.name === 'limitAmount' ? 'Budget' : 'Spent'}: {formatCurrency(p.value)}
        </p>
      ))}
    </div>
  )
}

export default function BudgetUtilizationChart({ data }) {
  if (!data?.length) return (
    <div className="card flex items-center justify-center h-48 text-gray-600">No budgets set</div>
  )
  return (
    <div className="card">
      <h3 className="text-sm font-semibold text-gray-400 mb-4">Budget Utilization</h3>
      <ResponsiveContainer width="100%" height={220}>
        <BarChart data={data} margin={{ top: 4, right: 4, left: 0, bottom: 4 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
          <XAxis dataKey="category" tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false}
            tickFormatter={v => getCategoryLabel(v).split(' ')[0]} />
          <YAxis tick={{ fill: '#6b7280', fontSize: 11 }} axisLine={false} tickLine={false}
            tickFormatter={v => `$${v >= 1000 ? (v/1000).toFixed(1)+'k' : v}`} />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: '#1f2937' }} />
          <Legend formatter={v => <span className="text-xs text-gray-400">{v === 'limitAmount' ? 'Budget' : 'Spent'}</span>} />
          <Bar dataKey="limitAmount" fill="#374151" radius={[4, 4, 0, 0]} />
          <Bar dataKey="spentAmount" radius={[4, 4, 0, 0]}>
            {data.map((entry, i) => (
              <Cell key={i} fill={entry.isExceeded ? '#ef4444' : '#6366f1'} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
