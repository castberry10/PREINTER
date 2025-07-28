import React, { useEffect, useState } from 'react';
import styled, { keyframes } from 'styled-components';
import { useNavigate, useParams } from 'react-router-dom';
import { Sparkles, PlayCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import axios from 'axios';

const drift = keyframes`
  0% {background-position:0% 50%;}
  50% {background-position:100% 50%;}
  100% {background-position:0% 50%;}
`;
const twinkle = keyframes`
  0%{opacity:.35;}50%{opacity:.65;}100%{opacity:.35;}
`;

const Wrapper = styled.main`
  position:relative;min-height:100vh;padding:2rem;color:#f8fafc;overflow-x:hidden;
  background:radial-gradient(circle at 20% 30%,rgba(99,102,241,.35)0%,rgba(2,6,23,0)55%),
             radial-gradient(circle at 80% 75%,rgba(236,72,153,.30)0%,rgba(2,6,23,0)55%),
             linear-gradient(-60deg,#020617 0%,#0f172a 100%);
  background-size:400% 400%;animation:${drift} 22s ease-in-out infinite;

  &::before,&::after{content:'';position:absolute;inset:0;pointer-events:none;}
  &::before{
    background-image:radial-gradient(2px 2px at 25% 40%,#fff 55%,transparent 56%),
                     radial-gradient(1.5px 1.5px at 40% 60%,#fff 55%,transparent 56%),
                     radial-gradient(2px 2px at 55% 15%,#fff 55%,transparent 56%),
                     radial-gradient(1.5px 1.5px at 70% 82%,#fff 55%,transparent 56%),
                     radial-gradient(2px 2px at 85% 28%,#fff 55%,transparent 56%);
    background-size:200% 200%;opacity:.15;animation:${drift} 80s linear infinite;
  }
  &::after{
    background-image:radial-gradient(2px 2px at 15% 25%,#fff 55%,transparent 56%),
                     radial-gradient(1.5px 1.5px at 35% 75%,#fff 55%,transparent 56%),
                     radial-gradient(2px 2px at 60% 40%,#fff 55%,transparent 56%),
                     radial-gradient(1.5px 1.5px at 80% 60%,#fff 55%,transparent 56%),
                     radial-gradient(2px 2px at 90% 20%,#fff 55%,transparent 56%);
    background-size:200% 200%;mix-blend-mode:screen;opacity:.35;
    animation:${drift} 120s linear infinite reverse,${twinkle} 5s steps(60) infinite;
  }

  header{
    position:absolute;top:0;left:0;width:100%;display:flex;justify-content:space-between;align-items:center;
    padding:1rem 2rem;background:rgba(255,255,255,.04);backdrop-filter:blur(8px);
    border-bottom-left-radius:1rem;border-bottom-right-radius:1rem;
    a.logo{display:flex;align-items:center;gap:.5rem;font-size:1.25rem;font-weight:700;color:#e2e8f0;text-decoration:none;}
    nav{display:none;gap:1.5rem;@media(min-width:768px){display:flex;}
      a{color:#e2e8f0;text-decoration:none;font-weight:500;&:hover{opacity:.8;}}
    }
    button.cta{display:inline-flex;align-items:center;gap:.3rem;background:rgba(255,255,255,.18);
      padding:.5rem 1rem;border:none;border-radius:.75rem;cursor:pointer;font-size:.875rem;color:#f8fafc;
      @media(min-width:768px){display:none;}
    }
  }

  .card{
    position:relative;z-index:1;max-width:56rem;margin:7rem auto 3rem;
    background:rgba(255,255,255,.06);backdrop-filter:blur(6px);
    padding:3rem 2rem;border-radius:1.5rem;display:grid;gap:2rem;
  }
  h2{font-size:1.75rem;font-weight:700;margin-bottom:.5rem;text-align:center;}
  .status{font-size:1.25rem;font-weight:600;margin-bottom:1rem;text-align:center;}
  .score{font-size:2.5rem;font-weight:700;color:#f59e0b;text-align:center;}
  .feedback-block p{margin-bottom:1rem;line-height:1.6;font-size:.95rem;color:#dbeafe;}
  .chart-wrapper{width:350px;height:350px; margin:30px auto;}
  .actions{display:flex;justify-content:center;gap:1.5rem;margin-top:1rem;
    button{background:#f8fafc;color:#4f46e5;padding:.6rem 1.5rem;border:none;border-radius:1rem;font-weight:600;cursor:pointer;}
  }
`;

const getPolygonPoints = (scores, radius = 140, centerX = 200, centerY = 200) =>
  Object.entries(scores)
    .map(([_, v], i, arr) => {
      const angle = (2 * Math.PI * i) / arr.length - Math.PI / 2;
      const r = (v / 100) * radius;
      const x = centerX + Math.cos(angle) * r;
      const y = centerY + Math.sin(angle) * r;
      return `${x},${y}`;
    })
    .join(' ');

export default function InterviewResultPage(){
  const navigate=useNavigate();
  const{sessionId}=useParams();
  const[data,setData]=useState(null);

  /* 실제 API 호출
  useEffect(()=>{
    axios.get(`/api/interviews/${sessionId}/result`)
         .then(res=>setData(res.data))
         .catch(console.error);
  },[sessionId]);
  */

  useEffect(()=>{
    setData({
      '면접결과':'불합격',
      '면접관의 평가':'지원자는 질문에 대해 구체적인 답변을 제공하지 못하고, 자신이 생각하는 운동의 중요성만 강조했습니다. 응답 내용이 면접과 관련된 질문에 적절히 연결되지 않아 면접에서 원하는 정보를 효과적으로 전달하지 못했습니다.',
      '면접관의 피드백':'질문에 대한 명확하고 구체적인 답변이 필요합니다. 특히 당신의 경험과 관련된 성과나 구체적인 사례를 들어 설명하는 것이 중요합니다. 문제를 해결하는 방법이나 접근 방식을 구체적으로 설명해 주세요.',
      '면접관의 면접 팁':'인터뷰 시 주어진 질문에 직접적으로 답변하는 것에 집중하세요. 경험 기반으로 구체적인 사례를 제시하고 질문자의 관심에 맞게 답변하는 것이 중요합니다.',
      '면접관의 점수':45,
      '면접관의 상세 점수':{논리성:40,전문성:50,소통력:35,인성:60,창의성:50}
    });
  },[]);

  if(!data) return null;
  const detail=data['면접관의 상세 점수'];

  return(
    <Wrapper>
      <header>
        <a href="/" className="logo"><Sparkles/> PREINTER</a>
        <nav><a href="/login">Login</a></nav>
        <button className="cta" onClick={()=>navigate('/')}><PlayCircle size={16}/> Home</button>
      </header>

      <motion.section
        className="card"
        initial={{opacity:0,y:40}}
        animate={{opacity:1,y:0}}
        transition={{duration:.8,ease:'easeOut'}}
      >
        <h2>면접 결과</h2>
        <div className="status">{data['면접결과']} – <span>{data['면접관의 점수']}점</span></div>

<div className="chart-wrapper">
  <svg width="100%" height="100%" viewBox="0 0 400 400">
    {[1, 0.8, 0.6, 0.4, 0.2].map((r, idx) => {
      const n = Object.keys(detail).length;
      const centerX = 200; 
      const centerY = 200;
      const pts = Array.from({ length: n }, (_, i) => {
        const angle = (2 * Math.PI * i) / n - Math.PI / 2;
        const x = centerX + Math.cos(angle) * 140 * r;
        const y = centerY + Math.sin(angle) * 140 * r;
        return `${x},${y}`;
      }).join(' ');
      return (
        <polygon
          key={idx}
          points={pts}
          fill={idx ? 'none' : '#e3f2fd'}
          stroke="#90caf9"
          strokeWidth={1}
        />
      );
    })}

    <polygon
      points={getPolygonPoints(detail, 140, 200, 200)} 
      fill="rgba(33,150,243,.4)"
      stroke="#1976d2"
      strokeWidth={2}
    />

    {Object.keys(detail).map((label, i) => {
      const n = Object.keys(detail).length;
      const angle = (2 * Math.PI * i) / n - Math.PI / 2;
      const x = 200 + Math.cos(angle) * 170;
      const y = 200 + Math.sin(angle) * 170;
      return (
        <text
          key={label}
          x={x}
          y={y}
          textAnchor="middle"
          dominantBaseline="middle"
          fontSize="17"
          fill="#ffffffff"
        >
          {label}
        </text>
      );
    })}
  </svg>
</div>


        <div className="feedback-block">
          <p><strong>면접관의 평가</strong><br/>{data['면접관의 평가']}</p>
          <p><strong>면접관의 피드백</strong><br/>{data['면접관의 피드백']}</p>
          <p><strong>면접관의 면접 팁</strong><br/>{data['면접관의 면접 팁']}</p>
        </div>

        <div className="actions">
          <button onClick={()=>navigate('/')}>홈으로</button>
          <button onClick={()=>navigate(`/interview/${sessionId}/replay`)}>리플레이</button>
        </div>
      </motion.section>
    </Wrapper>
  );
}

