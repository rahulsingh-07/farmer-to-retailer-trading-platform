import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'

export default function ScrollToSection() {
  const { hash } = useLocation()

  useEffect(() => {
    if (hash) {
      const el = document.querySelector(hash)
      el?.scrollIntoView({ behavior: 'smooth' })
    }
  }, [hash])

  return null
}
