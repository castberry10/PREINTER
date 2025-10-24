// import React, { useEffect, useState,useRef } from 'react';
// import styled, { keyframes } from 'styled-components';
// import { useNavigate, useParams } from 'react-router-dom';
// import { Sparkles, PlayCircle } from 'lucide-react';
// import { motion, AnimatePresence  } from 'framer-motion';
// // import axios from 'axios'; 
// import jsPDF from 'jspdf';
// import html2canvas from 'html2canvas';

// const drift = keyframes`
//   0% {background-position:0% 50%;}
//   50% {background-position:100% 50%;}
//   100% {background-position:0% 50%;}
// `;
// const twinkle = keyframes`
//   0%{opacity:.35;}50%{opacity:.65;}100%{opacity:.35;}
// `;

// const Wrapper = styled.main`
//   position:relative;min-height:100vh;padding:2rem;color:#f8fafc;overflow-x:hidden;
//   background:radial-gradient(circle at 20% 30%,rgba(99,102,241,.35)0%,rgba(2,6,23,0)55%),
//              radial-gradient(circle at 80% 75%,rgba(236,72,153,.30)0%,rgba(2,6,23,0)55%),
//              linear-gradient(-60deg,#020617 0%,#0f172a 100%);
//   background-size:400% 400%;animation:${drift} 22s ease-in-out infinite;

//   &::before,&::after{content:'';position:absolute;inset:0;pointer-events:none;}
//   &::before{
//     background-image:radial-gradient(2px 2px at 25% 40%,#fff 55%,transparent 56%),
//                      radial-gradient(1.5px 1.5px at 40% 60%,#fff 55%,transparent 56%),
//                      radial-gradient(2px 2px at 55% 15%,#fff 55%,transparent 56%),
//                      radial-gradient(1.5px 1.5px at 70% 82%,#fff 55%,transparent 56%),
//                      radial-gradient(2px 2px at 85% 28%,#fff 55%,transparent 56%);
//     background-size:200% 200%;opacity:.15;animation:${drift} 80s linear infinite;
//   }
//   &::after{
//     background-image:radial-gradient(2px 2px at 15% 25%,#fff 55%,transparent 56%),
//                      radial-gradient(1.5px 1.5px at 35% 75%,#fff 55%,transparent 56%),
//                      radial-gradient(2px 2px at 60% 40%,#fff 55%,transparent 56%),
//                      radial-gradient(1.5px 1.5px at 80% 60%,#fff 55%,transparent 56%),
//                      radial-gradient(2px 2px at 90% 20%,#fff 55%,transparent 56%);
//     background-size:200% 200%;mix-blend-mode:screen;opacity:.35;
//     animation:${drift} 120s linear infinite reverse,${twinkle} 5s steps(60) infinite;
//   }

//   header{
//     position:absolute;top:0;left:0;width:100%;display:flex;justify-content:space-between;align-items:center;
//     padding:1rem 2rem;background:rgba(255,255,255,.04);backdrop-filter:blur(8px);
//     border-bottom-left-radius:1rem;border-bottom-right-radius:1rem;
//     a.logo{display:flex;align-items:center;gap:.5rem;font-size:1.25rem;font-weight:700;color:#e2e8f0;text-decoration:none;}
//     nav{display:none;gap:1.5rem;@media(min-width:768px){display:flex;}
//       a{color:#e2e8f0;text-decoration:none;font-weight:500;&:hover{opacity:.8;}}
//     }
//     button.cta{display:inline-flex;align-items:center;gap:.3rem;background:rgba(255,255,255,.18);
//       padding:.5rem 1rem;border:none;border-radius:.75rem;cursor:pointer;font-size:.875rem;color:#f8fafc;
//       @media(min-width:768px){display:none;}
//     }
//   }

//   .card{
//     position:relative;z-index:1;max-width:56rem;margin:7rem auto 3rem;
//     background:rgba(255,255,255,.06);backdrop-filter:blur(6px);
//     padding:3rem 2rem;border-radius:1.5rem;display:grid;gap:2rem;
//   }
//   h2{font-size:1.75rem;font-weight:700;margin-bottom:.5rem;text-align:center;}
//   .status{font-size:1.25rem;font-weight:600;margin-bottom:1rem;text-align:center;}
//   .score{font-size:2.5rem;font-weight:700;color:#f59e0b;text-align:center;}
//   .feedback-block p{margin-bottom:1rem;line-height:1.6;font-size:.95rem;color:#dbeafe;}
//   .chart-wrapper{width:350px;height:350px; margin:30px auto;}
//   .actions{display:flex;justify-content:center;gap:1.5rem;margin-top:1rem;
//     button{background:#f8fafc;color:#4f46e5;padding:.6rem 1.5rem;border:none;border-radius:1rem;font-weight:600;cursor:pointer;}
//   }
// `;

// const spin = keyframes`
//   to { transform: rotate(360deg); }
// `;

// const LoaderOverlay = styled(motion.div)`
//   position: fixed;
//   inset: 0;
//   display: flex;
//   flex-direction: column;
//   justify-content: center;
//   align-items: center;
//   gap: 1.2rem;
//   background: rgba(0, 0, 0, 0.55);
//   backdrop-filter: blur(3px);
//   color: #f1f5f9;
//   font-size: 0.95rem;
//   z-index: 9999;
// `;

// const Spinner = styled.div`
//   width: 48px;
//   height: 48px;
//   border: 4px solid rgba(255, 255, 255, 0.25);
//   border-top-color: #f8fafc;
//   border-radius: 50%;
//   animation: ${spin} 0.8s linear infinite;
// `;

// const Printable = styled.div`
//   position: absolute; left: -99999px; top: 0;
//   width: 800px; background: #ffffff; color: #0f172a; padding: 40px;
//   font-family: system-ui, -apple-system, Segoe UI, Roboto, 'Noto Sans', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;

//   h1 { font-size: 24px; margin: 0 0 10px; }
//   h2 { font-size: 18px; margin: 24px 0 10px; text-align: left; }
//   .meta { font-size: 12px; color: #475569; margin-bottom: 16px; }
//   .row { display: flex; gap: 24px; align-items: flex-start; }
//   .scoreBox { min-width: 200px; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 16px; }
//   .scoreValue { font-size: 28px; font-weight: 800; color: #1d4ed8; }
//   .table { width: 100%; border-collapse: collapse; margin-top: 10px; }
//   .table th, .table td { border: 1px solid #e2e8f0; padding: 8px 10px; font-size: 12px; }
//   .table th { background: #f8fafc; text-align: left; }
//   .section { margin-top: 18px; line-height: 1.65; font-size: 13px; white-space: pre-wrap; text-align: left; }
//   .chartWrap { width: 360px; height: 360px; }
// `;

// const DUMMY_SUMMARY = {
//   "면접결과": "합격 유력",
//   "면접관의 평가": "핵심 역량에 대한 이해가 높고, 답변 구조가 명확합니다. 지원 직무와의 연관성을 잘 설명했습니다.",
//   "면접관의 피드백": "예시가 아주 좋았습니다. 다만, 성능 최적화 파트에서 수치(지연시간, 메모리 사용량 등)를 더 구체적으로 제시하면 설득력이 올라갑니다.",
//   "면접관의 면접 팁": "STAR 기법(상황-과제-행동-결과)으로 사례를 정리하고, 예상 꼬리질문에 대한 짧은 백업 근거를 준비하세요.",
//   "면접관의 점수": 87,
//   "면접관의 상세 점수": {
//     "문제해결력": 90,
//     "커뮤니케이션": 82,
//     "논리성": 85,
//     "태도/매너": 88,
//     "직무지식": 84
//   }
// };

// export default function InterviewResultPage() {
//   const navigate = useNavigate();
//   const { sessionId } = useParams();
//   const safeSessionId = sessionId || 'DEMO-SESSION-001';

//   const [summary, setSummary] = useState(null);  
//   const [loading, setLoading] = useState(true);

//   const [exporting, setExporting] = useState(false);
//   const pdfTopRef = useRef(null);
//   const pdfBottomRef = useRef(null);

//   useEffect(() => {
//     /* ✅ 데모 모드: API 호출 대신 더미 데이터 사용 */
//     setSummary(DUMMY_SUMMARY);
//     setLoading(false);

//     /* 실제 API 모드는 아래를 사용
//     (async () => {
//       try {
//         const { data } = await axios.post("/interview/result", { sessionId });
//         const parsed = JSON.parse(data.evaluationSummary || "{}");
//         setSummary(parsed);
//       } catch (e) {
//         console.error(e);
//         alert("결과를 가져오지 못했습니다.");
//         navigate("/");
//       } finally {
//         setLoading(false);
//       }
//     })();
//     */
//   }, [/* sessionId, navigate */]);

//   if (loading || !summary)
//     return (
//       <Wrapper>
//         <LoaderOverlay initial={{ opacity: 1 }} animate={{ opacity: 1 }}>
//           <Spinner />
//           <span>결과 분석 중…</span>
//         </LoaderOverlay>
//       </Wrapper>
//     );

//   const status       = summary["면접결과"] ?? "";
//   const comment      = summary["면접관의 평가"] ?? "";
//   const feedback     = summary["면접관의 피드백"] ?? "";
//   const tip          = summary["면접관의 면접 팁"] ?? "";
//   const score        = summary["면접관의 점수"] ?? 0;
//   const detailScores = summary["면접관의 상세 점수"] ?? {};

//   const handleExportPDF = async () => {
//     try {
//       setExporting(true);

//       const makeImage = async (el) => {
//         const canvas = await html2canvas(el, {
//           scale: 2,
//           backgroundColor: '#ffffff',
//           useCORS: true,
//         });
//         return canvas.toDataURL('image/png');
//       };

//       const imgTop = await makeImage(pdfTopRef.current);
//       const imgBottom = await makeImage(pdfBottomRef.current);

//       const doc = new jsPDF('p', 'mm', 'a4');
//       const pageWidth = doc.internal.pageSize.getWidth();
//       const pageHeight = doc.internal.pageSize.getHeight();
//       const margin = 10;

//       const addFullWidthImage = (dataUrl) => {
//         const img = new Image();
//         return new Promise((resolve) => {
//           img.onload = () => {
//             const w = pageWidth - margin * 2;
//             const h = (img.height / img.width) * w;
//             doc.addImage(dataUrl, 'PNG', margin, margin, w, h);
//             resolve(h);
//           };
//           img.src = dataUrl;
//         });
//       };

//       let usedHeight = await addFullWidthImage(imgTop);
//       const remaining = pageHeight - margin - usedHeight - margin;
//       const bottomImg = new Image();
//       await new Promise((resolve) => {
//         bottomImg.onload = () => {
//           const w = pageWidth - margin * 2;
//           const h = (bottomImg.height / bottomImg.width) * w;

//           if (h <= remaining) {
//             doc.addImage(bottomImg, 'PNG', margin, margin + usedHeight + 5, w, h);
//           } else {
//             doc.addPage();
//             doc.addImage(bottomImg, 'PNG', margin, margin, w, h);
//           }
//           resolve();
//         };
//         bottomImg.src = imgBottom;
//       });

//       const pages = doc.getNumberOfPages();

//       const makeWatermarkDataURL = (text) => {
//         const size = 500;
//         const angle = -Math.PI / 4;
//         const canvas = document.createElement('canvas');
//         canvas.width = size;
//         canvas.height = size;
//         const ctx = canvas.getContext('2d');

//         ctx.clearRect(0, 0, size, size);
//         ctx.save();
//         ctx.translate(size / 2, size / 2);
//         ctx.rotate(angle);

//         ctx.font = 'normal 30px Helvetica, Arial, sans-serif';
//         ctx.fillStyle = 'rgba(150,150,150,0.15)';
//         ctx.textAlign = 'center';
//         ctx.textBaseline = 'middle';
//         ctx.fillText(text, 0, 0);
//         ctx.restore();

//         return canvas.toDataURL('image/png');
//       };
//       const makeWatermarkDataURL2 = (text = 'PREINTER') => {
//         const size = 500;
//         const angle = -Math.PI / 4;
//         const canvas = document.createElement('canvas');
//         canvas.width = size;
//         canvas.height = size;
//         const ctx = canvas.getContext('2d');

//         ctx.clearRect(0, 0, size, size);
//         ctx.save();
//         ctx.translate(size / 2, size / 2);
//         ctx.rotate(angle);

//         ctx.font = 'normal 120px Helvetica, Arial, sans-serif';
//         ctx.fillStyle = 'rgba(150,150,150,0.15)';
//         ctx.textAlign = 'center';
//         ctx.textBaseline = 'middle';
//         ctx.fillText(text, 0, 0);
//         ctx.restore();

//         return canvas.toDataURL('image/png');
//       };
//       const watermarkUrl = makeWatermarkDataURL(safeSessionId);
//       const watermarkUrl2 = makeWatermarkDataURL2('PREINTER');
//       for (let i = 1; i <= pages; i++) {
//         doc.setPage(i);
//         const wmWidth = pageWidth * 0.6;
//         const img = new Image();
//         await new Promise((resolve) => {
//           img.onload = () => {
//             const ratio = img.height / img.width;
//             const wmHeight = wmWidth * ratio;
//             const x = (pageWidth - wmWidth) / 2;
//             const y = (pageHeight - wmHeight) / 2;
//             doc.addImage(watermarkUrl2, 'PNG', x-7, y-7, wmWidth, wmHeight);
//             doc.addImage(watermarkUrl, 'PNG', x+7, y+7, wmWidth, wmHeight);
//             resolve();
//           };
//           img.src = watermarkUrl2;
//         });

//         doc.setFontSize(10);
//         doc.setTextColor(0, 0, 0);
//         doc.text(`Page ${i} / ${pages}`, pageWidth / 2, pageHeight - 6, { align: 'center' });
//      }

//       const filename = `PREINTER_${safeSessionId}_면접결과.pdf`;
//       doc.save(filename);
//     } catch (err) {
//       console.error(err);
//       alert('PDF 생성에 실패했어요. 다시 시도해 주세요.');
//     } finally {
//       setExporting(false);
//     }
//   };

//   return (
//     <Wrapper>
//       <header>
//         <a href="/" className="logo">
//           <Sparkles /> PREINTER
//         </a>
//         <nav>
//           <a href="/login">Login</a>
//         </nav>
//         <button className="cta" onClick={() => navigate("/")}>
//           <PlayCircle size={16} /> Home
//         </button>
//       </header>

//       <motion.section
//         className="card"
//         initial={{ opacity: 0, y: 40 }}
//         animate={{ opacity: 1, y: 0 }}
//         transition={{ duration: 0.8, ease: "easeOut" }}
//       >
//         <h2>면접 결과</h2>
//         <div className="status">
//           {status} – <span>{score}점</span>
//         </div>

//         <div className="chart-wrapper">
//           <svg width="100%" height="100%" viewBox="0 0 400 400">
//             {[1, 0.8, 0.6, 0.4, 0.2].map((r, idx) => {
//               const n = Object.keys(detailScores).length;
//               const ring = Array.from({ length: n }, (_, i) => {
//                 const angle = (2 * Math.PI * i) / n - Math.PI / 2;
//                 return [
//                   200 + Math.cos(angle) * 140 * r,
//                   200 + Math.sin(angle) * 140 * r,
//                 ].join(",");
//               }).join(" ");
//               return (
//                 <polygon
//                   key={idx}
//                   points={ring}
//                   fill={idx ? "none" : "#e3f2fd"}
//                   stroke="#90caf9"
//                   strokeWidth={1}
//                 />
//               );
//             })}

//             <polygon
//               points={getPolygonPoints(detailScores)}
//               fill="rgba(33,150,243,.4)"
//               stroke="#1976d2"
//               strokeWidth={2}
//             />

//             {Object.keys(detailScores).map((label, i, arr) => {
//               const angle = (2 * Math.PI * i) / arr.length - Math.PI / 2;
//               return (
//                 <text
//                   key={label}
//                   x={200 + Math.cos(angle) * 170}
//                   y={200 + Math.sin(angle) * 170}
//                   textAnchor="middle"
//                   dominantBaseline="middle"
//                   fontSize="17"
//                   fill="#fff"
//                 >
//                   {label}
//                 </text>
//               );
//             })}
//           </svg>
//         </div>

//         <div className="feedback-block">
//           <p>
//             <strong>면접관의 평가</strong>
//             <br />
//             {comment}
//           </p>
//           <p>
//             <strong>면접관의 피드백</strong>
//             <br />
//             {feedback}
//           </p>
//           <p>
//             <strong>면접관의 면접 팁</strong>
//             <br />
//             {tip}
//           </p>
//         </div>

//         <div className="actions">
//           <button onClick={() => navigate("/")}>홈으로</button>
//           <button onClick={() => navigate(`/interview/${safeSessionId}/replay`)}>
//             리플레이
//           </button>
//           <button onClick={handleExportPDF} disabled={exporting}>
//             {exporting ? 'PDF 생성 중…' : '상세 리포트 (PDF) 출력'}
//           </button>
//         </div>
//       </motion.section>

//       <Printable aria-hidden>
//         <div ref={pdfTopRef}>
//           <h1>PREINTER 면접 결과 리포트</h1>
//           <div className="meta">
//             세션 ID: {safeSessionId} · 생성일시: {new Date().toLocaleString('ko-KR')}
//           </div>

//           <div className="row">
//             <div className="scoreBox">
//               <div style={{fontSize:14, fontWeight:700, marginBottom:6}}>면접 결과</div>
//               <div style={{fontSize:13, marginBottom:10}}>{status}</div>
//               <div className="scoreValue">{score}점</div>
//             </div>

//             <div className="chartWrap">
//               <svg width="100%" height="100%" viewBox="0 0 400 400">
//                 {[1, 0.8, 0.6, 0.4, 0.2].map((r, idx) => {
//                   const n = Object.keys(detailScores).length;
//                   const ring = Array.from({ length: n }, (_, i) => {
//                     const angle = (2 * Math.PI * i) / n - Math.PI / 2;
//                     return [
//                       200 + Math.cos(angle) * 140 * r,
//                       200 + Math.sin(angle) * 140 * r,
//                     ].join(",");
//                   }).join(" ");
//                   return (
//                     <polygon
//                       key={idx}
//                       points={ring}
//                       fill={idx ? "none" : "#eef2ff"}
//                       stroke="#c7d2fe"
//                       strokeWidth={1}
//                     />
//                   );
//                 })}

//                 <polygon
//                   points={getPolygonPoints(detailScores)}
//                   fill="rgba(59,130,246,.35)"
//                   stroke="#1d4ed8"
//                   strokeWidth={2}
//                 />

//                 {Object.keys(detailScores).map((label, i, arr) => {
//                   const angle = (2 * Math.PI * i) / arr.length - Math.PI / 2;
//                   return (
//                     <text
//                       key={label}
//                       x={200 + Math.cos(angle) * 170}
//                       y={200 + Math.sin(angle) * 170}
//                       textAnchor="middle"
//                       dominantBaseline="middle"
//                       fontSize="12"
//                       fill="#0f172a"
//                     >
//                       {label}
//                     </text>
//                   );
//                 })}
//               </svg>
//             </div>
//           </div>

//           <h2>상세 점수</h2>
//           <table className="table">
//             <thead>
//               <tr>
//                 <th>항목</th>
//                 <th>점수(0~100)</th>
//               </tr>
//             </thead>
//             <tbody>
//               {Object.entries(detailScores).map(([k, v]) => (
//                 <tr key={k}>
//                   <td>{k}</td>
//                   <td>{v}</td>
//                 </tr>
//               ))}
//             </tbody>
//           </table>
//         </div>

//         <div ref={pdfBottomRef} style={{marginTop: 16}}>
//           <h2>면접관의 평가</h2>
//           <div className="section">{comment}</div>

//           <h2>면접관의 피드백</h2>
//           <div className="section">{feedback}</div>
//         </div>
//       </Printable>

//       {exporting && (
//         <LoaderOverlay initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
//           <Spinner />
//           <span>PDF 생성 중…</span>
//         </LoaderOverlay>
//       )}
//     </Wrapper>
//   );
// }

// function getPolygonPoints(scores, radius = 140, cx = 200, cy = 200) {
//   const entries = Object.entries(scores);
//   return entries
//     .map(([_, v], i) => {
//       const angle = (2 * Math.PI * i) / entries.length - Math.PI / 2;
//       const r = (Number(v) / 100) * radius;
//       return `${cx + Math.cos(angle) * r},${cy + Math.sin(angle) * r}`;
//     })
//     .join(" ");
// }

import React, { useEffect, useState, useRef } from 'react';
import styled, { keyframes } from 'styled-components';
import { useNavigate, useParams } from 'react-router-dom';
import { Sparkles, PlayCircle } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
// import axios from 'axios';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

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

  /* ====== 추가된 화면 전용 분석 섹션 스타일 ====== */
  .analysis { display:grid; gap:1.25rem; }
  .mini-cards {
    display:grid; gap:1rem;
    grid-template-columns: 1fr;
    @media(min-width:768px){ grid-template-columns: repeat(3, 1fr); }
  }
  .mini-card {
    background:rgba(255,255,255,.06);
    border:1px solid rgba(255,255,255,.08);
    border-radius:1rem; padding:1rem;
  }
  .mini-card h3 { margin:0 0 .5rem; font-size:1rem; font-weight:700; color:#e5e7eb; }
  .kv { display:flex; justify-content:space-between; font-size:.95rem; margin:.25rem 0; }
  .subtle { color:#cbd5e1; font-size:.9rem; }
  .tag {
    display:inline-flex; align-items:center; gap:.3rem;
    padding:.25rem .5rem; border-radius:.5rem; font-size:.8rem;
    background:rgba(255,255,255,.08); color:#f1f5f9;
  }
  .list { display:flex; flex-wrap:wrap; gap:.4rem; margin-top:.35rem; }
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
  z-index: 9999;
`;

const Spinner = styled.div`
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.25);
  border-top-color: #f8fafc;
  border-radius: 50%;
  animation: ${spin} 0.8s linear infinite;
`;

const Printable = styled.div`
  position: absolute; left: -99999px; top: 0;
  width: 800px; background: #ffffff; color: #0f172a; padding: 40px;
  font-family: system-ui, -apple-system, Segoe UI, Roboto, 'Noto Sans', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;

  h1 { font-size: 24px; margin: 0 0 10px; }
  h2 { font-size: 18px; margin: 24px 0 10px; text-align: left; }
  .meta { font-size: 12px; color: #475569; margin-bottom: 16px; }
  .row { display: flex; gap: 24px; align-items: flex-start; }
  .scoreBox { min-width: 200px; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 16px; }
  .scoreValue { font-size: 28px; font-weight: 800; color: #1d4ed8; }
  .table { width: 100%; border-collapse: collapse; margin-top: 10px; }
  .table th, .table td { border: 1px solid #e2e8f0; padding: 8px 10px; font-size: 12px; }
  .table th { background: #f8fafc; text-align: left; }
  .section { margin-top: 18px; line-height: 1.65; font-size: 13px; white-space: pre-wrap; text-align: left; }
  .chartWrap { width: 360px; height: 360px; }
`;

/* 기존 더미 요약 */
const DUMMY_SUMMARY = {
  "면접결과": "합격 유력",
  "면접관의 평가": "핵심 역량에 대한 이해가 높고, 답변 구조가 명확합니다. 지원 직무와의 연관성을 잘 설명했습니다.",
  "면접관의 피드백": "예시가 아주 좋았습니다. 다만, 성능 최적화 파트에서 수치(지연시간, 메모리 사용량 등)를 더 구체적으로 제시하면 설득력이 올라갑니다.",
  "면접관의 면접 팁": "STAR 기법(상황-과제-행동-결과)으로 사례를 정리하고, 예상 꼬리질문에 대한 짧은 백업 근거를 준비하세요.",
  "면접관의 점수": 87,
  "면접관의 상세 점수": {
    "문제해결력": 90,
    "커뮤니케이션": 82,
    "논리성": 85,
    "태도/매너": 88,
    "직무지식": 84
  }
};

/* ✅ 추가: 화면 전용 분석 메트릭(네가 준 JSON 그대로) — PDF에는 미포함 */
const DUMMY_METRICS = {
  "sessionId": "exp:b1b98d8e-9db7-44f0-9ef8-a6dfbd67b46a",
  "thinkingTime": {
    "available": true,
    "answerCount": 1,
    "minSec": 2.14,
    "maxSec": 2.14,
    "avgSec": 2.14,
    "perAnswers": [
      {
        "questionNumber": 1,
        "thinkingSec": 2.14
      }
    ]
  },
  "fillerPositions": {
    "total": 5,
    "beginCount": 2,
    "middleCount": 3,
    "endCount": 0
  },
  "fillerFrequency": {
    "totalCount": 10,
    "topFillers": [
      { "token": "아", "count": 4 },
      { "token": "음", "count": 4 },
      { "token": "어", "count": 2 }
    ],
    "ratios": {
      "어": 20.0,
      "아": 40.0,
      "음": 40.0
    }
  },
  "topWords": null,
  "AR": 2.4630541871921188
};

export default function InterviewResultPage() {
  const navigate = useNavigate();
  const { sessionId } = useParams();
  const safeSessionId = sessionId || 'DEMO-SESSION-001';

  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  const [exporting, setExporting] = useState(false);
  const pdfTopRef = useRef(null);
  const pdfBottomRef = useRef(null);

  /* ✅ 추가: 화면 전용 메트릭 상태 */
  const [metrics, setMetrics] = useState(null);

  useEffect(() => {
    /* ✅ 데모 모드: API 호출 대신 더미 데이터 사용 */
    setSummary(DUMMY_SUMMARY);
    /* ✅ 추가: 메트릭 주입 (PDF는 그대로) */
    setMetrics(DUMMY_METRICS);
    setLoading(false);

    /* 실제 API 모드는 아래를 사용
    (async () => {
      try {
        const { data } = await axios.post("/interview/result", { sessionId });
        const parsed = JSON.parse(data.evaluationSummary || "{}");
        setSummary(parsed);

        // 메트릭도 함께 가져올 경우
        const { data: m } = await axios.post("/interview/metrics", { sessionId });
        setMetrics(m);

      } catch (e) {
        console.error(e);
        alert("결과를 가져오지 못했습니다.");
        navigate("/");
      } finally {
        setLoading(false);
      }
    })();
    */
  }, [/* sessionId, navigate */]);

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

  const handleExportPDF = async () => {
    try {
      setExporting(true);

      const makeImage = async (el) => {
        const canvas = await html2canvas(el, {
          scale: 2,
          backgroundColor: '#ffffff',
          useCORS: true,
        });
        return canvas.toDataURL('image/png');
      };

      const imgTop = await makeImage(pdfTopRef.current);
      const imgBottom = await makeImage(pdfBottomRef.current);

      const doc = new jsPDF('p', 'mm', 'a4');
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();
      const margin = 10;

      const addFullWidthImage = (dataUrl) => {
        const img = new Image();
        return new Promise((resolve) => {
          img.onload = () => {
            const w = pageWidth - margin * 2;
            const h = (img.height / img.width) * w;
            doc.addImage(dataUrl, 'PNG', margin, margin, w, h);
            resolve(h);
          };
          img.src = dataUrl;
        });
      };

      let usedHeight = await addFullWidthImage(imgTop);
      const remaining = pageHeight - margin - usedHeight - margin;
      const bottomImg = new Image();
      await new Promise((resolve) => {
        bottomImg.onload = () => {
          const w = pageWidth - margin * 2;
          const h = (bottomImg.height / bottomImg.width) * w;

          if (h <= remaining) {
            doc.addImage(bottomImg, 'PNG', margin, margin + usedHeight + 5, w, h);
          } else {
            doc.addPage();
            doc.addImage(bottomImg, 'PNG', margin, margin, w, h);
          }
          resolve();
        };
        bottomImg.src = imgBottom;
      });

      const pages = doc.getNumberOfPages();

      const makeWatermarkDataURL = (text) => {
        const size = 500;
        const angle = -Math.PI / 4;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        ctx.clearRect(0, 0, size, size);
        ctx.save();
        ctx.translate(size / 2, size / 2);
        ctx.rotate(angle);

        ctx.font = 'normal 30px Helvetica, Arial, sans-serif';
        ctx.fillStyle = 'rgba(150,150,150,0.15)';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(text, 0, 0);
        ctx.restore();

        return canvas.toDataURL('image/png');
      };
      const makeWatermarkDataURL2 = (text = 'PREINTER') => {
        const size = 500;
        const angle = -Math.PI / 4;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        ctx.clearRect(0, 0, size, size);
        ctx.save();
        ctx.translate(size / 2, size / 2);
        ctx.rotate(angle);

        ctx.font = 'normal 120px Helvetica, Arial, sans-serif';
        ctx.fillStyle = 'rgba(150,150,150,0.15)';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(text, 0, 0);
        ctx.restore();

        return canvas.toDataURL('image/png');
      };
      const watermarkUrl = makeWatermarkDataURL(safeSessionId);
      const watermarkUrl2 = makeWatermarkDataURL2('PREINTER');
      for (let i = 1; i <= pages; i++) {
        doc.setPage(i);
        const wmWidth = pageWidth * 0.6;
        const img = new Image();
        await new Promise((resolve) => {
          img.onload = () => {
            const ratio = img.height / img.width;
            const wmHeight = wmWidth * ratio;
            const x = (pageWidth - wmWidth) / 2;
            const y = (pageHeight - wmHeight) / 2;
            doc.addImage(watermarkUrl2, 'PNG', x-7, y-7, wmWidth, wmHeight);
            doc.addImage(watermarkUrl, 'PNG', x+7, y+7, wmWidth, wmHeight);
            resolve();
          };
          img.src = watermarkUrl2;
        });

        doc.setFontSize(10);
        doc.setTextColor(0, 0, 0);
        doc.text(`Page ${i} / ${pages}`, pageWidth / 2, pageHeight - 6, { align: 'center' });
     }

      const filename = `PREINTER_${safeSessionId}_면접결과.pdf`;
      doc.save(filename);
    } catch (err) {
      console.error(err);
      alert('PDF 생성에 실패했어요. 다시 시도해 주세요.');
    } finally {
      setExporting(false);
    }
  };

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

        {/* ✅ 추가: 화면 전용 발화/간투사 분석 섹션 (PDF 미포함) */}
        {metrics && (
          <section className="analysis">
            <h2>발화 패턴 분석</h2>

            <div className="mini-cards">
              {/* 1) 생각 시간(Thinking Time) */}
              <div className="mini-card">
                <h3>생각 시간</h3>
                <div className="kv"><span>답변 개수</span><span>{metrics.thinkingTime?.answerCount ?? 0} 개</span></div>
                <div className="kv"><span>평균</span><span>{(metrics.thinkingTime?.avgSec ?? 0).toFixed(2)} s</span></div>
                <div className="kv"><span>최소 ~ 최대</span><span>{(metrics.thinkingTime?.minSec ?? 0).toFixed(2)} ~ {(metrics.thinkingTime?.maxSec ?? 0).toFixed(2)} s</span></div>
                {Array.isArray(metrics.thinkingTime?.perAnswers) && metrics.thinkingTime.perAnswers.length > 0 && (
                  <>
                    <div className="subtle" style={{marginTop:'.5rem'}}>질문별</div>
                    <div className="list">
                      {metrics.thinkingTime.perAnswers.map((p) => (
                        <span key={p.questionNumber} className="tag">Q{p.questionNumber}("지원 동기가 무엇인가요?"): {p.thinkingSec.toFixed(2)}s</span>
                      ))}
                    </div>
                  </>
                )}
              </div>

              {/* 2) 간투사 위치(Positions) */}
              <div className="mini-card">
                <h3>간투사 위치</h3>
                <div className="kv"><span>총 발생</span><span>{metrics.fillerPositions?.total ?? 0} 회</span></div>
                <div className="kv"><span>초반</span><span>{metrics.fillerPositions?.beginCount ?? 0} 회</span></div>
                <div className="kv"><span>중반</span><span>{metrics.fillerPositions?.middleCount ?? 0} 회</span></div>
                <div className="kv"><span>후반</span><span>{metrics.fillerPositions?.endCount ?? 0} 회</span></div>
              </div>

              {/* 3) 간투사 빈도(Frequency) */}
              <div className="mini-card">
                <h3>간투사 빈도</h3>
                <div className="kv"><span>총 개수</span><span>{metrics.fillerFrequency?.totalCount ?? 0} 개</span></div>
                {Array.isArray(metrics.fillerFrequency?.topFillers) && metrics.fillerFrequency.topFillers.length > 0 && (
                  <>
                    <div className="subtle" style={{marginTop:'.5rem'}}>상위 토큰</div>
                    <div className="list">
                      {metrics.fillerFrequency.topFillers.map((t) => (
                        <span key={t.token} className="tag">{t.token} × {t.count}</span>
                      ))}
                    </div>
                  </>
                )}
                {metrics.fillerFrequency?.ratios && (
                  <>
                    <div className="subtle" style={{marginTop:'.5rem'}}>비율(%)</div>
                    <div className="list">
                      {Object.entries(metrics.fillerFrequency.ratios).map(([tok, pct]) => (
                        <span key={tok} className="tag">{tok}: {Number(pct).toFixed(1)}%</span>
                      ))}
                    </div>
                  </>
                )}
              </div>
            </div>

            {/* 4) 발화 속도(AR) */}
            <div className="mini-card" style={{marginTop:'.25rem'}}>
              <h3>발화 속도</h3>
              <div className="kv">
                <span>AR (침묵 제외 초당 음절)</span>
                <span>{(metrics.AR ?? 0).toFixed(3)} syl/s</span>
              </div>
              <div className="subtle" style={{marginTop:'.35rem'}}>
                * AR은 침묵 구간을 제외하고 초당 말한 음절 수를 의미합니다.
              </div>
            </div>
          </section>
        )}

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
          <button onClick={() => navigate(`/interview/${safeSessionId}/replay`)}>
            리플레이
          </button>
          <button onClick={handleExportPDF} disabled={exporting}>
            {exporting ? 'PDF 생성 중…' : '상세 리포트 (PDF) 출력'}
          </button>
        </div>
      </motion.section>

      {/* ====== PDF 영역: 수정 없음 ====== */}
      <Printable aria-hidden>
        <div ref={pdfTopRef}>
          <h1>PREINTER 면접 결과 리포트</h1>
          <div className="meta">
            세션 ID: {safeSessionId} · 생성일시: {new Date().toLocaleString('ko-KR')}
          </div>

          <div className="row">
            <div className="scoreBox">
              <div style={{fontSize:14, fontWeight:700, marginBottom:6}}>면접 결과</div>
              <div style={{fontSize:13, marginBottom:10}}>{status}</div>
              <div className="scoreValue">{score}점</div>
            </div>

            <div className="chartWrap">
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
                      fill={idx ? "none" : "#eef2ff"}
                      stroke="#c7d2fe"
                      strokeWidth={1}
                    />
                  );
                })}

                <polygon
                  points={getPolygonPoints(detailScores)}
                  fill="rgba(59,130,246,.35)"
                  stroke="#1d4ed8"
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
                      fontSize="12"
                      fill="#0f172a"
                    >
                      {label}
                    </text>
                  );
                })}
              </svg>
            </div>
          </div>

          <h2>상세 점수</h2>
          <table className="table">
            <thead>
              <tr>
                <th>항목</th>
                <th>점수(0~100)</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(detailScores).map(([k, v]) => (
                <tr key={k}>
                  <td>{k}</td>
                  <td>{v}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div ref={pdfBottomRef} style={{marginTop: 16}}>
          <h2>면접관의 평가</h2>
          <div className="section">{comment}</div>

          <h2>면접관의 피드백</h2>
          <div className="section">{feedback}</div>
        </div>
      </Printable>

      {exporting && (
        <LoaderOverlay initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
          <Spinner />
          <span>PDF 생성 중…</span>
        </LoaderOverlay>
      )}
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
