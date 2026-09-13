export default function StatCard({ title, value, subtitle, color = 'indigo' }) {
  const colors = {
    indigo: 'text-indigo-400',
    green:  'text-emerald-400',
    red:    'text-red-400',
    amber:  'text-amber-400',
  }
  return (
    <div className="card">
      <p className="text-sm text-gray-400">{title}</p>
      <p className={`text-2xl font-bold mt-1 ${colors[color] || colors.indigo}`}>{value}</p>
      {subtitle && <p className="text-xs text-gray-500 mt-1">{subtitle}</p>}
    </div>
  )
}
