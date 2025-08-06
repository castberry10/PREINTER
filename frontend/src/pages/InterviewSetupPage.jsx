import React, { useState, useEffect } from 'react';
import styled, { keyframes } from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence  } from 'framer-motion';
import { Sparkles, PlayCircle, Zap } from 'lucide-react';
import axios from "axios";   
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

const spin = keyframes`
  to { transform: rotate(360deg); }
`;

const LoaderOverlay = styled(motion.div)`
  position: fixed;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 1.2rem;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(3px);
  color: #f1f5f9;
  font-size: 0.95rem;
  z-index: 9999;      /* 화면 맨 위 */
`;

const Spinner = styled.div`
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.25);
  border-top-color: #f8fafc;
  border-radius: 50%;
  animation: ${spin} 0.8s linear infinite;
`;
import * as pdfjsLib from "pdfjs-dist";
import pdfWorker from "pdfjs-dist/build/pdf.worker?url";

pdfjsLib.GlobalWorkerOptions.workerSrc = pdfWorker;

const extractTextFromPDF = async (file) => {
  const arrayBuffer = await file.arrayBuffer();
  const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;

  let text = "";
  for (let i = 1; i <= pdf.numPages; i++) {
    const page = await pdf.getPage(i);
    const content = await page.getTextContent();
    const pageText = content.items.map((item) => item.str).join(" ");
    text += pageText + "\n";
  }

  return text;
};

export default function InterviewSetupPage() {
  const navigate = useNavigate();
  const [resume, setResume] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingMessage, setLoadingMessage] = useState("");
  const [interviewDuration, setInterviewDuration] = useState(10);
  const [interviewMode, setInterviewMode] = useState("voice");
  useEffect(() => {
    if (!loading) {
      setLoadingMessage(""); 
      return;
    }

    setLoadingMessage("이력서 AI 정밀 분석 중...");

    const timeout = setTimeout(() => {
      setLoadingMessage("면접 장소로 이동하는 중...");
    }, 3000);

    return () => clearTimeout(timeout);
  }, [loading]);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type !== 'application/pdf') {
      alert('PDF 파일을 업로드해주세요.');
      e.target.value = '';
      return;
    }
    setResume(file);
  };

  const handleStartInterview = async () => {
    if (!resume) {
      alert("이력서를 먼저 업로드해 주세요.");
      return;
    }
    


    try {
      setLoading(true); 
        const extractedText = await extractTextFromPDF(resume); 
      const res = await axios.post(
        "/interview/start",
        {
          resumeFile: extractedText
        },
        { headers: { "Content-Type": "application/json" } }
      );

      const { sessionId } = res.data;
      if(!sessionId) {
        alert("면접 세션을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.");
        setLoading(false);
        return;
      }

      if (interviewMode === "voice") {
        navigate(`/interview/${sessionId}/real`, {
          state: { resumeName: resume.name, interviewDuration: interviewDuration }
        });
      } else {
        navigate(`/interview/${sessionId}/text`, {
          state: { resumeName: resume.name, interviewDuration: interviewDuration }
        });
      }
    } catch (err) {
      console.error(err);
      alert("면접 세션을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }finally {
      setLoading(false);                    
    }
  };

  return (
    <InterviewSetupPageBlock
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
        면접 시간 선택
        <select
          value={interviewDuration}
          onChange={(e) => setInterviewDuration(parseInt(e.target.value))}
        >
          <option value={0.5}>30초 (테스트/시연용)</option>
          <option value={5}>5분</option>
          <option value={10}>10분</option>
          <option value={15}>15분</option>
          <option value={20}>20분</option>
        </select>
      </label>

      <label>
        면접 방식 선택
        <select
          value={interviewMode}
          onChange={(e) => setInterviewMode(e.target.value)}
        >
          <option value="chat">채팅</option>
          <option value="voice">음성</option>
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
      <AnimatePresence>
        {loading && (
          <LoaderOverlay
            key="loader"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.25 }}
          >
            <Spinner />  
            <span>{loadingMessage}</span>
          </LoaderOverlay>
        )}
      </AnimatePresence>
    </InterviewSetupPageBlock>
  );
}
