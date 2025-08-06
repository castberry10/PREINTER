import React, { useEffect, useState } from 'react';
import styled, { keyframes } from 'styled-components';
import { useNavigate, useParams } from 'react-router-dom';
import { Sparkles, PlayCircle } from 'lucide-react';
import { motion, AnimatePresence  } from 'framer-motion';
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

export default function InterviewResultPage() {
  const navigate = useNavigate();
  const { sessionId } = useParams();

  const [summary, setSummary] = useState(null);  
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await axios.post("/interview/result", { sessionId });

        const parsed = JSON.parse(data.evaluationSummary || "{}");
        setSummary(parsed);                    
      } catch (e) {
        console.error(e);
        alert("결과를 가져오지 못했습니다.");
        navigate("/");
      } finally {
        setLoading(false);
      }
    })();
  }, [sessionId, navigate]);

  if (loading || !summary)
    return (
      <Wrapper>
        <LoaderOverlay initial={{ opacity: 1 }} animate={{ opacity: 1 }}>
          <Spinner />
          <span>결과 분석 중…</span>
        </LoaderOverlay>
      </Wrapper>
    );

  const status       = summary["면접결과"] ?? "";
  const comment      = summary["면접관의 평가"] ?? "";
  const feedback     = summary["면접관의 피드백"] ?? "";
  const tip          = summary["면접관의 면접 팁"] ?? "";
  const score        = summary["면접관의 점수"] ?? 0;
  const detailScores = summary["면접관의 상세 점수"] ?? {};

  return (
    <Wrapper>
      <header>
        <a href="/" className="logo">
          <Sparkles /> PREINTER
        </a>
        <nav>
          <a href="/login">Login</a>
        </nav>
        <button className="cta" onClick={() => navigate("/")}>
          <PlayCircle size={16} /> Home
        </button>
      </header>

      <motion.section
        className="card"
        initial={{ opacity: 0, y: 40 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: "easeOut" }}
      >
        <h2>면접 결과</h2>
        <div className="status">
          {status} – <span>{score}점</span>
        </div>

        <div className="chart-wrapper">
          <svg width="100%" height="100%" viewBox="0 0 400 400">
            {[1, 0.8, 0.6, 0.4, 0.2].map((r, idx) => {
              const n = Object.keys(detailScores).length;
              const ring = Array.from({ length: n }, (_, i) => {
                const angle = (2 * Math.PI * i) / n - Math.PI / 2;
                return [
                  200 + Math.cos(angle) * 140 * r,
                  200 + Math.sin(angle) * 140 * r,
                ].join(",");
              }).join(" ");
              return (
                <polygon
                  key={idx}
                  points={ring}
                  fill={idx ? "none" : "#e3f2fd"}
                  stroke="#90caf9"
                  strokeWidth={1}
                />
              );
            })}

            <polygon
              points={getPolygonPoints(detailScores)}
              fill="rgba(33,150,243,.4)"
              stroke="#1976d2"
              strokeWidth={2}
            />

            {Object.keys(detailScores).map((label, i, arr) => {
              const angle = (2 * Math.PI * i) / arr.length - Math.PI / 2;
              return (
                <text
                  key={label}
                  x={200 + Math.cos(angle) * 170}
                  y={200 + Math.sin(angle) * 170}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  fontSize="17"
                  fill="#fff"
                >
                  {label}
                </text>
              );
            })}
          </svg>
        </div>

        <div className="feedback-block">
          <p>
            <strong>면접관의 평가</strong>
            <br />
            {comment}
          </p>
          <p>
            <strong>면접관의 피드백</strong>
            <br />
            {feedback}
          </p>
          <p>
            <strong>면접관의 면접 팁</strong>
            <br />
            {tip}
          </p>
        </div>

        <div className="actions">
          <button onClick={() => navigate("/")}>홈으로</button>
          <button onClick={() => navigate(`/interview/${sessionId}/replay`)}>
            리플레이
          </button>
        </div>
      </motion.section>
    </Wrapper>
  );
}

function getPolygonPoints(scores, radius = 140, cx = 200, cy = 200) {
  const entries = Object.entries(scores);
  return entries
    .map(([_, v], i) => {
      const angle = (2 * Math.PI * i) / entries.length - Math.PI / 2;
      const r = (Number(v) / 100) * radius;
      return `${cx + Math.cos(angle) * r},${cy + Math.sin(angle) * r}`;
    })
    .join(" ");
}