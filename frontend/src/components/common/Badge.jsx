import { getCategoryLabel } from '../../utils/formatters'

const COLORS = {
  FOOD_DINING: 'bg-indigo-900/50 text-indigo-300',
  TRANSPORTATION: 'bg-sky-900/50 text-sky-300',
  HOUSING: 'bg-violet-900/50 text-violet-300',
  UTILITIES: 'bg-amber-900/50 text-amber-300',
  HEALTHCARE: 'bg-emerald-900/50 text-emerald-300',
  ENTERTAINMENT: 'bg-pink-900/50 text-pink-300',
  SHOPPING: 'bg-orange-900/50 text-orange-300',
  EDUCATION: 'bg-teal-900/50 text-teal-300',
  TRAVEL: 'bg-cyan-900/50 text-cyan-300',
  PERSONAL_CARE: 'bg-rose-900/50 text-rose-300',
  INSURANCE: 'bg-lime-900/50 text-lime-300',
  SAVINGS: 'bg-green-900/50 text-green-300',
  OTHER: 'bg-gray-800 text-gray-400',
  SALARY: 'bg-green-900/50 text-green-300',
  FREELANCE: 'bg-teal-900/50 text-teal-300',
  INVESTMENT: 'bg-indigo-900/50 text-indigo-300',
  BUSINESS: 'bg-violet-900/50 text-violet-300',
  RENTAL: 'bg-amber-900/50 text-amber-300',
}

export default function Badge({ value }) {
  const cls = COLORS[value] || 'bg-gray-800 text-gray-400'
  return (
    <span className={`badge ${cls}`}>
      {getCategoryLabel(value)}
    </span>
  )
}
