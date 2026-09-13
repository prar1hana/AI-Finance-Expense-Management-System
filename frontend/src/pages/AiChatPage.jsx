import { useState, useRef, useEffect } from 'react'
import { chatWithAI } from '../api/ai'
import { useAuth } from '../hooks/useAuth'

const SUGGESTIONS = [
  'Summarize my spending this month',
  'Which category did I overspend in?',
  'How does my income compare to my expenses?',
  'Give me budgeting tips based on my spending',
]

function Message({ msg }) {
  const isUser = msg.role === 'user'
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} mb-3`}>
      {!isUser && (
        <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-sm mr-2 flex-shrink-0 mt-0.5">
          🤖
        </div>
      )}
      <div className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm whitespace-pre-wrap ${
        isUser
          ? 'bg-indigo-600 text-white rounded-br-sm'
          : 'bg-gray-800 text-gray-200 rounded-bl-sm'
      }`}>
        {msg.content}
      </div>
    </div>
  )
}

function TypingIndicator() {
  return (
    <div className="flex justify-start mb-3">
      <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-sm mr-2 flex-shrink-0">🤖</div>
      <div className="bg-gray-800 rounded-2xl rounded-bl-sm px-4 py-3">
        <div className="flex gap-1 items-center h-4">
          {[0, 1, 2].map(i => (
            <div key={i} className="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce"
              style={{ animationDelay: `${i * 150}ms` }} />
          ))}
        </div>
      </div>
    </div>
  )
}

export default function AiChatPage() {
  const { currentUser } = useAuth()
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)
  const inputRef = useRef(null)

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages, loading])

  async function sendMessage(text) {
    const userMsg = text || input.trim()
    if (!userMsg || loading) return
    setInput('')
    const newMessages = [...messages, { role: 'user', content: userMsg }]
    setMessages(newMessages)
    setLoading(true)
    try {
      const history = newMessages.slice(0, -1).map(m => ({ role: m.role, content: m.content }))
      const { data } = await chatWithAI(userMsg, history)
      setMessages(prev => [...prev, { role: 'assistant', content: data.message }])
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Sorry, I ran into an error. Please try again.' }])
    } finally {
      setLoading(false)
      inputRef.current?.focus()
    }
  }

  function handleKey(e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage() }
  }

  return (
    <div className="flex flex-col h-[calc(100vh-130px)]">
      <div className="card mb-4 py-3 px-4 flex items-center gap-3">
        <span className="text-2xl">🤖</span>
        <div>
          <h3 className="font-medium text-gray-200 text-sm">AI Finance Assistant</h3>
          <p className="text-xs text-gray-500">Powered by Gemini — answers based on your real data</p>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto card py-4 px-4 mb-4">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-center space-y-4">
            <div className="text-5xl">💬</div>
            <p className="text-gray-400 font-medium">Ask about your finances</p>
            <p className="text-gray-600 text-sm">I have access to your real transaction data</p>
            <div className="flex flex-wrap gap-2 justify-center mt-2">
              {SUGGESTIONS.map(s => (
                <button key={s} onClick={() => sendMessage(s)}
                  className="bg-gray-800 hover:bg-gray-700 text-gray-300 text-xs px-3 py-2 rounded-full transition-colors border border-gray-700">
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <>
            {messages.map((msg, i) => <Message key={i} msg={msg} />)}
            {loading && <TypingIndicator />}
          </>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="card py-3 px-4">
        <div className="flex gap-3 items-end">
          <textarea
            ref={inputRef}
            rows={1}
            className="input flex-1 resize-none py-2.5"
            placeholder="Ask about your spending, budgets, or get financial insights..."
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKey}
          />
          <button onClick={() => sendMessage()} disabled={!input.trim() || loading}
            className="btn-primary px-5 py-2.5 flex-shrink-0">
            {loading ? '...' : 'Send'}
          </button>
        </div>
        {messages.length > 0 && (
          <button onClick={() => setMessages([])} className="text-xs text-gray-600 hover:text-gray-400 mt-2 transition-colors">
            Clear conversation
          </button>
        )}
      </div>
    </div>
  )
}
