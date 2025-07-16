import React, { useState } from 'react';
import styled, { keyframes } from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Sparkles, PlayCircle } from 'lucide-react';

const drift = keyframes`
  0%   { background-position:   0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position:   0% 50%; }
`;

const twinkle = keyframes`
  0%   { opacity:.35; }
  50%  { opacity:.65; }
  100% { opacity:.35; }
`;

const InterviewSetupPageBlock = styled(motion.main)`
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #f8fafc;
  overflow: hidden;

  /* drifting aurora background */
  background: radial-gradient(circle at 20% 30%, rgba(99,102,241,0.35) 0%, rgba(2,6,23,0) 55%),
              radial-gradient(circle at 80% 75%, rgba(236,72,153,0.30) 0%, rgba(2,6,23,0) 55%),
              linear-gradient(-60deg, #020617 0%, #0f172a 100%);
  background-size: 400% 400%;
  animation: ${drift} 22s ease-in-out infinite;

  &::before, &::after { content:''; position:absolute; inset:0; pointer-events:none; }
  &::before{
    background-image:
      radial-gradient(2px 2px at 25% 40%, #fff 55%, transparent 56%),
      radial-gradient(1.5px 1.5px at 40% 60%, #fff 55%, transparent 56%),
      radial-gradient(2px 2px at 55% 15%, #fff 55%, transparent 56%),
      radial-gradient(1.5px 1.5px at 70% 82%, #fff 55%, transparent 56%),
      radial-gradient(2px 2px at 85% 28%, #fff 55%, transparent 56%);
    background-size: 200% 200%;
    opacity:.15;
    animation:${drift} 80s linear infinite;
  }
  &::after{
    background-image:
      radial-gradient(2px 2px at 15% 25%, #fff 55%, transparent 56%),
      radial-gradient(1.5px 1.5px at 35% 75%, #fff 55%, transparent 56%),
      radial-gradient(2px 2px at 60% 40%, #fff 55%, transparent 56%),
      radial-gradient(1.5px 1.5px at 80% 60%, #fff 55%, transparent 56%),
      radial-gradient(2px 2px at 90% 20%, #fff 55%, transparent 56%);
    background-size: 200% 200%;
    mix-blend-mode:screen;
    opacity:.35;
    animation:${drift} 120s linear infinite reverse, ${twinkle} 5s steps(60) infinite;
  }

  > * { position:relative; z-index:1; }

  header{
    position:absolute; top:0; left:0; width:100%;
    display:flex; justify-content:space-between; align-items:center;
    padding:1rem 2rem;
    background:rgba(255,255,255,0.04);
    backdrop-filter:blur(8px);
    border-bottom-left-radius:1rem;
    border-bottom-right-radius:1rem;

    a.logo{
      display:flex; align-items:center; gap:.5rem;
      font-size:1.25rem; font-weight:700; color:#e2e8f0;
      text-decoration:none;
    }
    nav{ display:none; gap:1.5rem;
      @media(min-width:768px){ display:flex; }
      a{ color:#e2e8f0; text-decoration:none; font-weight:500; &:hover{ opacity:.8; } }
    }
    button.cta{
      display:inline-flex; align-items:center; gap:.3rem;
      background:rgba(255,255,255,0.18);
      padding:.5rem 1rem; border:none; border-radius:.75rem;
      cursor:pointer; font-size:.875rem; color:#f8fafc;
      @media(min-width:768px){ display:none; }
    }
  }

  h1{ font-size:2rem; font-weight:700; margin-bottom:2rem; text-align:center; }

  form{
    display:grid; gap:1.5rem; width:100%; max-width:28rem;
    background:rgba(255,255,255,0.05);
    backdrop-filter:blur(6px);
    padding:2rem; border-radius:1.5rem;
  }
  label{ display:flex; flex-direction:column; gap:.5rem; font-size:.875rem; }

  .fileinput{
    background:rgba(0,0,0,0.35); border:1px solid rgba(255,255,255,.15);
    padding:.65rem .9rem; border-radius:.5rem; color:#f8fafc; font-size:.9rem;
    cursor:pointer;
  }
  select, input[type="file"]{
    background:rgba(0,0,0,0.35); border:1px solid rgba(255,255,255,.15);
    padding:.65rem .9rem; border-radius:.5rem; color:#f8fafc; font-size:.9rem;
  }
  small{ color:#cbd5e1; font-size:.75rem; margin-top:.25rem; }

  button.start{
    display:inline-flex; align-items:center; gap:.5rem; justify-content:center;
    background:#f8fafc; color:#4f46e5; padding:.75rem 2rem;
    border:none; border-radius:1.25rem; font-weight:600; cursor:pointer;
    transition:transform .15s ease-out;
    &:active{ transform:scale(.95); }
  }
`;

export default function InterviewSetupPage() {
  const navigate = useNavigate();
  const [jobRole, setJobRole]       = useState('developer');
  const [difficulty, setDifficulty] = useState('medium');
  const [resume, setResume]         = useState(null);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type !== 'application/pdf') {
      alert('PDF 파일만 업로드할 수 있습니다.');
      e.target.value = '';
      return;
    }
    setResume(file);
  };

  const handleStartInterview = () => {
    const sessionId = Math.random().toString(36).substring(2, 15);
    navigate(`/interview/${sessionId}`, { state: { jobRole, difficulty, resumeName: resume?.name } });
  };

  return (
    <InterviewSetupPageBlock
      // initial={{ opacity:0, y:30 }}
      animate={{ opacity:1, y:0 }}
      transition={{ duration:0.8 }}
    >
      <header>
        <a href="/" className="logo"><Sparkles /> PREINTER</a>
        <nav><a href="/login">Login</a></nav>
        <button className="cta" onClick={handleStartInterview}>
          <PlayCircle size={16} /> Start
        </button>
      </header>

      <h1>인터뷰 세팅</h1>
      <form onSubmit={e => { e.preventDefault(); handleStartInterview(); }}>
        <label>
          난이도
          <select value={difficulty} onChange={e => setDifficulty(e.target.value)}>
            <option value="easy">Easy</option>
            <option value="medium">Medium</option>
            <option value="hard">Hard</option>
          </select>
        </label>

        <label>
          이력서 (PDF)
          <input className="fileinput"type="file" accept=".pdf" onChange={handleFileChange} />
          {resume && <small>{resume.name} 선택됨</small>}
        </label>

        <button type="submit" className="start">
          <PlayCircle size={18} />&nbsp;면접 시작하기
        </button>
      </form>
    </InterviewSetupPageBlock>
  );
}
