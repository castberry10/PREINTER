import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import { Route, Routes } from 'react-router-dom';
import NotFoundErrorPage from './pages/NotFoundErrorPage';
import IntroPage from './pages/IntroPage';
import InterviewSetupPage from './pages/InterviewSetupPage';
import InterviewRoomPage from './pages/InterviewRoomPage';
import InterviewResultPage from './pages/InterviewResultPage';
import InterviewReplayPage from './pages/InterviewReplayPage';
import InterviewRealRoomPage from './pages/InterviewRealRoomPage';
import LoginPage from './pages/LoginPage';
import axios from 'axios';

function App() {
  // axios.defaults.baseURL = "http://preinter.castberry.kr:8443/api";
  axios.defaults.baseURL = "https://aoaoaoqq.com:8443/api";
  // axios.defaults.baseURL = "http://aoaoaoqq.com:8443/api";
  // axios.defaults.withCredentials = true;
  return (
    <div>
      <Routes>
        <Route path="/" element={<IntroPage />} />
          <Route path="/interview">
          <Route path="setup" element={<InterviewSetupPage />} />
          <Route path=":sessionId/text" element={<InterviewRoomPage />} />
          <Route path=":sessionId/real" element={<InterviewRealRoomPage />} />
          <Route path=":sessionId/result" element={<InterviewResultPage />} />
          <Route path=":sessionId/replay" element={<InterviewReplayPage />} />
        </Route>
        <Route path="login" element={<LoginPage />} />
        <Route path="/*" element={<NotFoundErrorPage />} />
      </Routes>
    </div>
  )
}

export default App
