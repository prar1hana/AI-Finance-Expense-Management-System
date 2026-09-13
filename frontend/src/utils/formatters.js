export function formatCurrency(amount) {
  if (amount == null) return '$0.00'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(amount)
}

export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  })
}

export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  })
}

export function currentMonthYear() {
  const now = new Date()
  return { month: now.getMonth() + 1, year: now.getFullYear() }
}

export function getCategoryLabel(value) {
  const map = {
    FOOD_DINING: 'Food & Dining', TRANSPORTATION: 'Transportation',
    HOUSING: 'Housing', UTILITIES: 'Utilities', HEALTHCARE: 'Healthcare',
    ENTERTAINMENT: 'Entertainment', SHOPPING: 'Shopping', EDUCATION: 'Education',
    TRAVEL: 'Travel', PERSONAL_CARE: 'Personal Care', INSURANCE: 'Insurance',
    SAVINGS: 'Savings', OTHER: 'Other',
    SALARY: 'Salary', FREELANCE: 'Freelance', INVESTMENT: 'Investment',
    BUSINESS: 'Business', RENTAL: 'Rental',
  }
  return map[value] || value
}
