import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { formatCurrency } from '../../utils/formatters'
import { CHART_COLORS } from '../../utils/constants'
import { getCategoryLabel } from '../../utils/formatters'

const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-gray-800 border border-gray-700 rounded-lg p-3 text-sm">
      <p className="text-gray-300">{getCategoryLabel(payload[0].name)}</p>
      <p style={{ color: payload[0].payload.fill }}>{formatCurrency(payload[0].value)}</p>
      <p className="text-gray-500">{payload[0].payload.percentage?.toFixed(1)}%</p>
    </div>
  )
}

export default function CategoryBreakdownChart({ data }) {
  if (!data?.length) return (
    <div className="card flex items-center justify-center h-64 text-gray-600">No expenses this month</div>
  )

  const chartData = data.slice(0, 8).map((d, i) => ({
    name: d.category,
    value: d.amount,
    percentage: d.percentage,
    fill: CHART_COLORS[i % CHART_COLORS.length],
  }))

  return (
    <div className="card">
      <h3 className="text-sm font-semibold text-gray-400 mb-4">Spending by Category</h3>
      <ResponsiveContainer width="100%" height={220}>
        <PieChart>
          <Pie data={chartData} cx="50%" cy="50%" innerRadius={55} outerRadius={85}
            dataKey="value" paddingAngle={2}>
            {chartData.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
          <Legend
            formatter={(value) => <span className="text-xs text-gray-400">{getCategoryLabel(value)}</span>}
            iconType="circle" iconSize={8}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
