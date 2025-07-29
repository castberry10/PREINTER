import React from 'react';
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
  from { opacity: .3; }
  50%  { opacity: .7; }
  to   { opacity: .3; }
`;

const IntroPageBlock = styled.div`
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  color: #f8fafc; /* slate‑50 */
  overflow: hidden;

  /* animated cosmic gradient */
  background: radial-gradient(circle at 25% 30%, rgba(99,102,241,0.35) 0%, rgba(2,6,23,0) 55%),
              radial-gradient(circle at 75% 70%, rgba(236,72,153,0.30) 0%, rgba(2,6,23,0) 50%),
              linear-gradient(-60deg, #020617 0%, #0f172a 100%);
  background-size: 400% 400%;
  animation: ${drift} 22s ease-in-out infinite;


  &::before{ opacity:.25; animation:${drift} 80s linear infinite; }

  &::after{
    opacity:.4; mix-blend-mode:screen;
    animation:${drift} 120s linear infinite reverse, ${twinkle} 5s steps(60) infinite;
  }

  > * { position: relative; z-index: 1; }

  header{
    position:absolute; top:0; left:0; width:100%; display:flex; justify-content:space-between; align-items:center;
    padding:1rem 2rem; background:rgba(255,255,255,0.04); backdrop-filter:blur(8px); border-bottom-left-radius:1rem; border-bottom-right-radius:1rem;

    a.logo{ display:flex; align-items:center; gap:.5rem; font-size:1.25rem; font-weight:700; color:#e2e8f0; text-decoration:none; }
    nav{ display:none; gap:1.5rem; @media(min-width:768px){ display:flex; }
      a{ color:#e2e8f0; text-decoration:none; font-weight:500; &:hover{ opacity:.8; } }
    }
    button.cta{ display:inline-flex; align-items:center; gap:.3rem; background:rgba(255,255,255,0.18); padding:.5rem 1rem; border:none; border-radius:.75rem; cursor:pointer; font-size:.875rem; color:#f8fafc; @media(min-width:768px){ display:none; } }
  }

  .hero{
    text-align:center; max-width:42rem; padding-top:6rem;
    h1{ font-size:2.5rem; font-weight:700; margin-bottom:1rem; line-height:1.2; }
    p{ font-size:1.125rem; font-weight:300; margin-bottom:2rem; color:#cbd5e1; }
    button.start{ display:inline-flex; align-items:center; gap:.5rem; background:#f8fafc; color:#4f46e5; padding:.75rem 2rem; border:none; border-radius:1.25rem; font-weight:600; cursor:pointer; transition:transform .15s ease-out; &:active{ transform:scale(.95); } }
  }

  .features{
    display:grid; gap:1.5rem; margin-top:4rem; width:100%; max-width:64rem; grid-template-columns:1fr;
    @media(min-width:640px){ grid-template-columns:repeat(2,1fr); }
    @media(min-width:1024px){ grid-template-columns:repeat(3,1fr); }
    .feature{ background:rgba(255,255,255,0.05); backdrop-filter:blur(6px); border-radius:1.5rem; padding:1.5rem; text-align:center; transition:transform .2s; &:hover{ transform:translateY(-.25rem); }
      svg{ color:#a5b4fc; margin-bottom:.75rem; }
      h3{ font-size:1.25rem; margin-bottom:.5rem; }
      p{ font-size:.875rem; font-weight:300; color:#dbeafe; }
    }
  }

  footer{ position:absolute; bottom:1rem; font-size:.875rem; color:rgba(255,255,255,0.6); }
`;

export default function LoginPage(){
  const navigate=useNavigate();
  const goInterview=()=>navigate('/interview/setup');

  return(
    <IntroPageBlock>
      <header>
        <a href="/" className="logo"><Sparkles/> PREINTER</a>
        <button className="cta" onClick={goInterview}><PlayCircle/> Start</button>
      </header>

      <motion.section className="hero" initial={{opacity:0,y:50}} animate={{opacity:1,y:0}} transition={{duration:0.8}}>
        <p>로그인/회원가입 기능은 추후 추가 예정입니다</p>
      </motion.section>

      <footer>© {new Date().getFullYear()} PREINTER. All rights reserved.</footer>
    </IntroPageBlock>
  );
}
