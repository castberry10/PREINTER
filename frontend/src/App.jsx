import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import { Route, Routes } from 'react-router-dom';
import NotFoundErrorPage from './pages/NotFoundErrorPage';
import IntroPage from './pages/IntroPage';
function App() {

  return (
    <div>
      <Routes>
        <Route path="/" element={<IntroPage />} />

      <Route path="/*" element={<NotFoundErrorPage />} />
      </Routes>
    </div>
  )
}

export default App
