export default function ErrorMessage({ message }) {
  if (!message) return null
  return (
    <div className="bg-red-900/30 border border-red-700 rounded-lg p-3 text-sm text-red-400">
      {message}
    </div>
  )
}
