import React, { useEffect, useState, useRef } from 'react';
import styled, { keyframes } from 'styled-components';
import { useNavigate, useParams } from 'react-router-dom';
import { Sparkles, PlayCircle } from 'lucide-react';
import { motion } from 'framer-motion';
// import axios from 'axios';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

/* ==== Animations ==== */
const drift = keyframes`
  0% {background-position:0% 50%;}
  50% {background-position:100% 50%;}
  100% {background-position:0% 50%;}
`;
const twinkle = keyframes`
  0%{opacity:.35;}50%{opacity:.65;}100%{opacity:.35;}
`;
const spin = keyframes` to { transform: rotate(360deg); }`;

/* ==== Layout ==== */
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
`;

/* === Landscape A4-ish Board === */
const Board = styled.section`
  position:relative;z-index:1;margin:7rem auto 3rem;
  max-width: 1400px;
  padding: 1.5rem;
  background:rgba(255,255,255,.06);backdrop-filter:blur(6px);
  border:1px solid rgba(255,255,255,.08);border-radius:1.25rem;

  display:grid;gap:1.25rem;
  grid-template-columns: minmax(440px, 640px) 1fr;

  @media(max-width:1024px){
    grid-template-columns: 1fr;
  }
`;

/* === Top Title Bar === */
const TopBar = styled.div`
  grid-column: 1 / -1;
  display:flex;align-items:center;justify-content:space-between;gap:1rem;
  background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.10);
  border-radius:1rem;padding:1rem 1.25rem;

  .title{font-weight:900;font-size:1.25rem;letter-spacing:.2px;}
  .meta{display:flex;align-items:center;gap:1rem;flex-wrap:wrap;}
  .chip{
    background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);
    border-radius:.65rem;padding:.35rem .6rem;font-size:.85rem;color:#e5e7eb;
  }
  .scoreBig{
    font-weight:900;font-size:1.9rem;line-height:1;color:#fde68a; /* 점수 강조 */
  }
`;

const Pane = styled.div`
  display:flex;flex-direction:column;gap:1rem;
`;

const Panel = styled.div`
  background:rgba(255,255,255,.06);
  border:1px solid rgba(255,255,255,.10);
  border-radius:1rem;
  padding:1rem;
`;

const PanelHeader = styled.div`
  display:flex;justify-content:space-between;align-items:center;margin-bottom:.5rem;
  h3{margin:0;font-size:1.05rem;font-weight:800;color:#e5e7eb;}
  .subtle{color:#cbd5e1;font-size:.85rem;}
`;

/* === Right cards grid === */
const CardsGrid = styled.div`
  display:grid;gap:1rem;
  grid-template-columns: 1fr 1fr;
  @media(max-width:1200px){
    grid-template-columns: 1fr;
  }
`;

/* shared UI bits */
const KeyValue = styled.div`
  display:flex;justify-content:space-between;font-size:.95rem;margin:.25rem 0;
`;
const Tag = styled.span`
  display:inline-flex;align-items:center;gap:.3rem;
  padding:.25rem .5rem;border-radius:.5rem;font-size:.8rem;
  background:rgba(255,255,255,.08);color:#f1f5f9;
`;
const Pills = styled.div`display:flex;flex-wrap:wrap;gap:.4rem;margin-top:.35rem;`;

/* 예시 테이블 스타일 */
const SentenceTable = styled.div`
  width:100%;display:grid;gap:.5rem;margin-top:.6rem;
`;
const SectionTitle = styled.div`
  margin-top:.35rem;padding:.25rem .5rem;border-radius:.4rem;
  font-weight:800;font-size:.9rem;color:#e5e7eb;background:rgba(255,255,255,.08);
  display:inline-block;
`;
const Row = styled.div`
  display:grid;gap:.5rem;align-items:center;
  grid-template-columns: 90px 1fr 2.5fr; /* 유형 | 토큰 | 사용 예시 */
  padding:.5rem .6rem;border:1px solid rgba(255,255,255,.08);border-radius:.6rem;
  background:rgba(255,255,255,.04);
  @media(max-width:900px){ grid-template-columns: 1fr; }
`;
const CellHeading = styled.div`
  font-size:.8rem;color:#94a3b8;margin-bottom:.2rem;
`;
const TypeBadge = styled.span`
  display:inline-block;padding:.2rem .5rem;border-radius:.45rem;font-size:.8rem;
  color:#0f172a;background:${p=>p.$kind==='compound' ? '#fde68a' : '#bfdbfe'};
`;
const Quote = styled.blockquote`
  margin:0;padding:.5rem .7rem;border-left:3px solid rgba(255,255,255,.25);background:rgba(255,255,255,.03);
  border-radius:.25rem;color:#eaf2ff;font-size:.92rem;white-space:pre-wrap;
`;

/* Loader */
const LoaderOverlay = styled(motion.div)`
  position: fixed;inset: 0;display:flex;flex-direction:column;justify-content:center;align-items:center;gap:1.2rem;
  background: rgba(0, 0, 0, 0.55);backdrop-filter: blur(3px);color:#f1f5f9;font-size: 0.95rem;z-index: 9999;
`;
const Spinner = styled.div`
  width: 48px;height: 48px;border: 4px solid rgba(255, 255, 255, 0.25);
  border-top-color: #f8fafc;border-radius: 50%;animation: ${spin} 0.8s linear infinite;
`;

/* PDF 영역은 기존 유지 */
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

/* ==== Dummy Data: 상세 점수 5항목 ==== */
const DUMMY_SUMMARY = {
  "면접결과": "합격 유력",
  "면접관의 평가": "핵심 역량에 대한 이해가 높고, 답변 구조가 명확합니다. 지원 직무와의 연관성을 잘 설명했습니다.",
  "면접관의 피드백": "예시가 아주 좋았습니다. 다만, 성능 최적화 파트에서 수치(지연시간, 메모리 사용량 등)를 더 구체적으로 제시하면 설득력이 올라갑니다.",
  "면접관의 면접 팁": "STAR 기법(상황-과제-행동-결과)으로 사례를 정리하고, 예상 꼬리질문에 대한 짧은 백업 근거를 준비하세요.",
  "면접관의 점수": 87,
  "면접관의 상세 점수": {
    "논리성": 85,
    "창의성": 88,
    "전문성": 84,
    "인성": 90,
    "소통력": 82
  }
};

const DUMMY_METRICS = {
  "sessionId": "exp:b1b98d8e-9db7-44f0-9ef8-a6dfbd67b46a",
  "thinkingTime": { "available": true, "answerCount": 1, "minSec": 2.14, "maxSec": 2.14, "avgSec": 2.14, "perAnswers": [{ "questionNumber": 1, "thinkingSec": 2.14 }] },
  "fillerPositions": { "total": 5, "beginCount": 2, "middleCount": 3, "endCount": 0 },
  "fillerFrequency": {
    "totalCount": 10,
    "topFillers": [
      { "token": "아", "count": 4 },
      { "token": "음", "count": 4 },
      { "token": "어", "count": 2 }
    ],
    "ratios": { "어": 20.0, "아": 40.0, "음": 40.0 }
  },
  "topWords": null,
  "AR": 2.4630541871921188,
  "fillerSentences": {
    "sentenceCount": 7,
    "sentenceSingleCount": 5,
    "sentenceCompoundCount": 2,
    "examples": [
      { "text": "음… 지원 동기는, 제가 학부 때부터 보안 연구를 해왔기 때문입니다.", "type": "compound", "fillers": ["음…"] },
      { "text": "어 사실, 해당 프로젝트는 제가 리딩했습니다.", "type": "single", "fillers": ["어"] },
      { "text": "그… 뭐랄까, 사용자 경험을 더 우선했습니다.", "type": "compound", "fillers": ["그…", "뭐랄까"] },
      { "text": "아 네, 성능 수치는 레이턴시 45ms 정도였습니다.", "type": "single", "fillers": ["아"] },
      { "text": "저 먼저 간단히 구조를 설명드릴게요.", "type": "single", "fillers": ["저"] }
    ]
  }
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

  const [metrics, setMetrics] = useState(null);

  useEffect(() => {
    setSummary(DUMMY_SUMMARY);
    setMetrics(DUMMY_METRICS);
    setLoading(false);
  }, []);

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
        const canvas = await html2canvas(el, { scale: 2, backgroundColor: '#ffffff', useCORS: true });
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
        const size = 500; const angle = -Math.PI / 4;
        const canvas = document.createElement('canvas');
        canvas.width = size; canvas.height = size;
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0,0,size,size); ctx.save(); ctx.translate(size/2,size/2); ctx.rotate(angle);
        ctx.font = 'normal 30px Helvetica, Arial, sans-serif'; ctx.fillStyle = 'rgba(150,150,150,0.15)';
        ctx.textAlign='center'; ctx.textBaseline='middle'; ctx.fillText(text,0,0); ctx.restore();
        return canvas.toDataURL('image/png');
      };
      const makeWatermarkDataURL2 = (text='PREINTER')=>{
        const size=500; const angle=-Math.PI/4;
        const canvas=document.createElement('canvas'); canvas.width=size; canvas.height=size;
        const ctx=canvas.getContext('2d');
        ctx.clearRect(0,0,size,size); ctx.save(); ctx.translate(size/2,size/2); ctx.rotate(angle);
        ctx.font='normal 120px Helvetica, Arial, sans-serif'; ctx.fillStyle='rgba(150,150,150,0.15)';
        ctx.textAlign='center'; ctx.textBaseline='middle'; ctx.fillText(text,0,0); ctx.restore();
        return canvas.toDataURL('image/png');
      };
      const watermarkUrl = makeWatermarkDataURL(safeSessionId);
      const watermarkUrl2 = makeWatermarkDataURL2('PREINTER');
      for (let i=1;i<=pages;i++){
        doc.setPage(i);
        const wmWidth = pageWidth * 0.6;
        const img = new Image();
        await new Promise((resolve)=>{
          img.onload=()=>{
            const ratio = img.height / img.width;
            const wmHeight = wmWidth * ratio;
            const x = (pageWidth - wmWidth) / 2;
            const y = (pageHeight - wmHeight) / 2;
            doc.addImage(watermarkUrl2,'PNG',x-7,y-7,wmWidth,wmHeight);
            doc.addImage(watermarkUrl,'PNG',x+7,y+7,wmWidth,wmHeight);
            resolve();
          };
          img.src=watermarkUrl2;
        });
        doc.setFontSize(10); doc.setTextColor(0,0,0);
        doc.text(`Page ${i} / ${pages}`, pageWidth/2, pageHeight-6, {align:'center'});
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

  /* === 스코어 라벨 순서 고정 === */
  const orderedLabels = ["논리성","창의성","전문성","인성","소통력"];
  const orderedScores = orderedLabels.reduce((obj, key) => {
    obj[key] = Number(detailScores[key] ?? 0);
    return obj;
  }, {});

  return (
    <Wrapper>
      <header>
        <a href="/" className="logo"><Sparkles /> PREINTER</a>
        <nav><a href="/login">Login</a></nav>
        <button className="cta" onClick={() => navigate("/")}><PlayCircle size={16} /> Home</button>
      </header>

      <Board as={motion.section} initial={{ opacity: 0, y: 40 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8, ease: "easeOut" }}>
        {/* === Top Title/Score Bar (제목+상태+점수) === */}
        <TopBar>
          <div className="title">면접 결과</div>
          <div className="meta">
            <span className="chip">세션: {safeSessionId}</span>
            <span className="chip">상태: {status}</span>
            <span className="scoreBig">{score}점</span>
          </div>
        </TopBar>

        {/* Left Pane: Radar + Filler Frequency + Speech Rate */}
        <Pane>
          <Panel>
            <PanelHeader>
              <h3>핵심 역량 레이더</h3>
              <div className="subtle">논리성 · 창의성 · 전문성 · 인성 · 소통력</div>
            </PanelHeader>
            <div style={{width:'100%', height: 420, maxHeight: '55vh', margin:'0 auto'}}>
              <Radar scores={orderedScores} />
            </div>
          </Panel>

          <Panel>
            <PanelHeader>
              <h3>간투사 빈도 그래프</h3>
              <div className="subtle">빈도 상위 토큰</div>
            </PanelHeader>
            <div style={{width:'100%', height: 280}}>
              <FillerBarChart topFillers={metrics?.fillerFrequency?.topFillers || []} total={metrics?.fillerFrequency?.totalCount || 0} />
            </div>
          </Panel>

          {/* 발화 속도: 좌측 하단 */}
          <Panel>
            <PanelHeader><h3>발화 속도</h3></PanelHeader>
            <KeyValue><span>AR (침묵 제외 초당 음절)</span><span>{(metrics.AR ?? 0).toFixed(3)} syl/s</span></KeyValue>
            <div className="subtle" style={{marginTop:'.35rem'}}>* AR은 침묵 구간을 제외하고 초당 말한 음절 수를 의미합니다.</div>
          </Panel>
        </Pane>

        {/* Right Pane: Cards */}
        <Pane>
          {/* 면접관 요약 카드 */}
          <Panel>
            <PanelHeader>
              <h3>면접관 요약</h3>
              <div className="subtle">상세 점수</div>
            </PanelHeader>
            <div style={{display:'grid',gridTemplateColumns:'repeat(5, minmax(0,1fr))',gap:'.5rem',marginBottom:'.75rem'}}>
              {orderedLabels.map(k=>(
                <div key={k} style={{background:'rgba(255,255,255,.05)',border:'1px solid rgba(255,255,255,.08)',borderRadius:8,padding:10,textAlign:'center'}}>
                  <div style={{fontSize:12, color:'#cbd5e1', marginBottom:6}}>{k}</div>
                  <div style={{fontSize:20, fontWeight:800}}>{orderedScores[k]}</div>
                </div>
              ))}
            </div>
            <div style={{fontSize:14, color:'#e2e8f0'}}>
              <b>평가</b> — {comment}
            </div>
          </Panel>

          {/* 생각 시간 */}
          <CardsGrid>
            <Panel>
              <PanelHeader><h3>생각 시간</h3></PanelHeader>
              <KeyValue><span>답변 개수</span><span>{metrics.thinkingTime?.answerCount ?? 0} 개</span></KeyValue>
              <KeyValue><span>평균</span><span>{(metrics.thinkingTime?.avgSec ?? 0).toFixed(2)} s</span></KeyValue>
              <KeyValue><span>최소 ~ 최대</span><span>{(metrics.thinkingTime?.minSec ?? 0).toFixed(2)} ~ {(metrics.thinkingTime?.maxSec ?? 0).toFixed(2)} s</span></KeyValue>
              {Array.isArray(metrics.thinkingTime?.perAnswers) && metrics.thinkingTime.perAnswers.length > 0 && (
                <>
                  <div className="subtle" style={{marginTop:'.5rem'}}>질문별</div>
                  <Pills>
                    {metrics.thinkingTime.perAnswers.map((p) => (
                      <Tag key={p.questionNumber}>Q{p.questionNumber}("지원 동기가 무엇인가요?"): {p.thinkingSec.toFixed(2)}s</Tag>
                    ))}
                  </Pills>
                </>
              )}
            </Panel>

            {/* 간투사 위치 */}
            <Panel>
              <PanelHeader><h3>간투사 위치</h3></PanelHeader>
              <KeyValue><span>총 발생</span><span>{metrics.fillerPositions?.total ?? 0} 회</span></KeyValue>
              <KeyValue><span>초반</span><span>{metrics.fillerPositions?.beginCount ?? 0} 회</span></KeyValue>
              <KeyValue><span>중반</span><span>{metrics.fillerPositions?.middleCount ?? 0} 회</span></KeyValue>
              <KeyValue><span>후반</span><span>{metrics.fillerPositions?.endCount ?? 0} 회</span></KeyValue>
            </Panel>

            {/* === 가로로 길게: 간투사 사용 문장 (우측 그리드 2칸 span) === */}
            <Panel style={{gridColumn:'1 / -1'}}>
              <PanelHeader><h3>간투사 사용 문장</h3></PanelHeader>
              <KeyValue><span>문장 총 개수</span><span>{metrics.fillerSentences?.sentenceCount ?? 0} 문장</span></KeyValue>
              <KeyValue><span>단일 간투사 문장</span><span>{metrics.fillerSentences?.sentenceSingleCount ?? 0} 문장</span></KeyValue>
              <KeyValue><span>복합 간투사 문장</span><span>{metrics.fillerSentences?.sentenceCompoundCount ?? 0} 문장</span></KeyValue>

              {/* 예시: 단일 섹션 */}
              {Array.isArray(metrics.fillerSentences?.examples) && (
                <>
                  <SectionTitle>단일</SectionTitle>
                  <SentenceTable>
                    {metrics.fillerSentences.examples.filter(e=>e.type==='single').map((ex, idx)=>(
                      <Row key={`single-${idx}`}>
                        <div>
                          <CellHeading>유형</CellHeading>
                          <TypeBadge $kind="single">단일</TypeBadge>
                        </div>
                        <div>
                          <CellHeading>사용 토큰</CellHeading>
                          <Pills>{(ex.fillers||[]).map((f,i)=>(<Tag key={i}>{f}</Tag>))}</Pills>
                        </div>
                        <div>
                          <CellHeading>사용 예시</CellHeading>
                          <Quote>{ex.text}</Quote>
                        </div>
                      </Row>
                    ))}
                  </SentenceTable>

                  {/* 예시: 복합 섹션 */}
                  <SectionTitle>복합</SectionTitle>
                  <SentenceTable>
                    {metrics.fillerSentences.examples.filter(e=>e.type==='compound').map((ex, idx)=>(
                      <Row key={`compound-${idx}`}>
                        <div>
                          <CellHeading>유형</CellHeading>
                          <TypeBadge $kind="compound">복합</TypeBadge>
                        </div>
                        <div>
                          <CellHeading>사용 토큰</CellHeading>
                          <Pills>{(ex.fillers||[]).map((f,i)=>(<Tag key={i}>{f}</Tag>))}</Pills>
                        </div>
                        <div>
                          <CellHeading>사용 예시</CellHeading>
                          <Quote>{ex.text}</Quote>
                        </div>
                      </Row>
                    ))}
                  </SentenceTable>
                </>
              )}
            </Panel>
          </CardsGrid>

          {/* 피드백/팁 + 액션 */}
          <Panel>
            <PanelHeader><h3>면접관 피드백 & 팁</h3></PanelHeader>
            <div style={{display:'grid', gap:'.75rem', fontSize:'.95rem', color:'#dbeafe'}}>
              <div><b>평가</b><br/>{comment}</div>
              <div><b>피드백</b><br/>{feedback}</div>
              <div><b>면접 팁</b><br/>{tip}</div>
            </div>
            <div style={{display:'flex', justifyContent:'flex-end', gap:'.6rem', marginTop:'1rem'}}>
              <button style={btnStyle} onClick={() => navigate("/")}>홈으로</button>
              <button style={btnStyle} onClick={() => navigate(`/interview/${safeSessionId}/replay`)}>리플레이</button>
              <button style={btnStyle} onClick={handleExportPDF} disabled={exporting}>{exporting ? 'PDF 생성 중…' : '상세 리포트 (PDF) 출력'}</button>
            </div>
          </Panel>
        </Pane>
      </Board>

      {/* ====== PDF 유지 ====== */}
      <Printable aria-hidden>
        <div ref={pdfTopRef}>
          <h1>PREINTER 면접 결과 리포트</h1>
          <div className="meta">세션 ID: {safeSessionId} · 생성일시: {new Date().toLocaleString('ko-KR')}</div>

          <div className="row">
            <div className="scoreBox">
              <div style={{fontSize:14, fontWeight:700, marginBottom:6}}>면접 결과</div>
              <div style={{fontSize:13, marginBottom:10}}>{status}</div>
              <div className="scoreValue">{score}점</div>
            </div>

            <div className="chartWrap">
              <svg width="100%" height="100%" viewBox="0 0 400 400">
                {[1, 0.8, 0.6, 0.4, 0.2].map((r, idx) => {
                  const n = Object.keys(orderedScores).length;
                  const ring = Array.from({ length: n }, (_, i) => {
                    const angle = (2 * Math.PI * i) / n - Math.PI / 2;
                    return [200 + Math.cos(angle) * 140 * r, 200 + Math.sin(angle) * 140 * r].join(",");
                  }).join(" ");
                  return (
                    <polygon key={idx} points={ring} fill={idx ? "none" : "#eef2ff"} stroke="#c7d2fe" strokeWidth={1}/>
                  );
                })}
                <polygon points={getPolygonPoints(orderedScores)} fill="rgba(59,130,246,.35)" stroke="#1d4ed8" strokeWidth={2}/>
                {Object.keys(orderedScores).map((label, i, arr) => {
                  const angle = (2 * Math.PI * i) / arr.length - Math.PI / 2;
                  return (
                    <text key={label} x={200 + Math.cos(angle) * 170} y={200 + Math.sin(angle) * 170}
                      textAnchor="middle" dominantBaseline="middle" fontSize="12" fill="#0f172a">{label}</text>
                  );
                })}
              </svg>
            </div>
          </div>

          <h2>상세 점수</h2>
          <table className="table">
            <thead><tr><th>항목</th><th>점수(0~100)</th></tr></thead>
            <tbody>
              {Object.entries(orderedScores).map(([k, v]) => (<tr key={k}><td>{k}</td><td>{v}</td></tr>))}
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

/* ===== Radar Component (SVG) ===== */
function Radar({ scores }) {
  const labels = Object.keys(scores);
  const width = 700, height = 420;
  const cx = width/2, cy = height/2 + 10;
  const radius = 160;
  const rings = [1, 0.8, 0.6, 0.4, 0.2];

  return (
    <svg width="100%" height="100%" viewBox={`0 0 ${width} ${height}`}>
      {rings.map((r, idx) => {
        const ring = labels.map((_, i) => {
          const angle = (2 * Math.PI * i) / labels.length - Math.PI / 2;
          return `${cx + Math.cos(angle) * radius * r},${cy + Math.sin(angle) * radius * r}`;
        }).join(" ");
        return (
          <polygon key={idx} points={ring} fill={idx ? "none" : "#e3f2fd"} stroke="#90caf9" strokeWidth={1}/>
        );
      })}
      <polygon points={getPolygonPoints(scores, radius, cx, cy)} fill="rgba(33,150,243,.4)" stroke="#1976d2" strokeWidth={2}/>
      {labels.map((label, i, arr) => {
        const angle = (2 * Math.PI * i) / arr.length - Math.PI / 2;
        return (
          <text key={label} x={cx + Math.cos(angle) * (radius + 28)} y={cy + Math.sin(angle) * (radius + 28)}
            textAnchor="middle" dominantBaseline="middle" fontSize="15" fill="#fff">{label}</text>
        );
      })}
    </svg>
  );
}

/* ===== Filler Frequency Bar Chart (SVG) ===== */
function FillerBarChart({ topFillers, total }) {
  const data = Array.isArray(topFillers) ? topFillers : [];
  const width = 700, height = 280, padding = { top: 10, right: 20, bottom: 40, left: 40 };
  const innerW = width - padding.left - padding.right;
  const innerH = height - padding.top - padding.bottom;
  const maxVal = Math.max(1, ...data.map(d => d.count || 0));
  const step = data.length ? innerW / data.length : innerW;
  const barW = step * 0.7;

  return (
    <svg width="100%" height="100%" viewBox={`0 0 ${width} ${height}`}>
      <line x1={padding.left} y1={padding.top} x2={padding.left} y2={padding.top + innerH} stroke="#94a3b8" strokeOpacity=".5"/>
      <line x1={padding.left} y1={padding.top + innerH} x2={padding.left + innerW} y2={padding.top + innerH} stroke="#94a3b8" strokeOpacity=".5"/>

      {data.map((d, i) => {
        const x = padding.left + i * step + (step - barW)/2;
        const h = (d.count / maxVal) * (innerH - 10);
        const y = padding.top + innerH - h;
        return (
          <g key={i}>
            <rect x={x} y={y} width={barW} height={h} fill="rgba(59,130,246,.6)" stroke="#1d4ed8"/>
            <text x={x + barW/2} y={padding.top + innerH + 18} fontSize="14" textAnchor="middle" fill="#e2e8f0">{d.token}</text>
            <text x={x + barW/2} y={y - 6} fontSize="12" textAnchor="middle" fill="#f8fafc">{d.count}</text>
          </g>
        );
      })}

      <text x={width - 10} y={padding.top + 10} textAnchor="end" fontSize="12" fill="#cbd5e1">총 {total} 개</text>
    </svg>
  );
}

/* ===== Shared Utils ===== */
function getPolygonPoints(scores, radius = 140, cx = 200, cy = 200) {
  const entries = Object.entries(scores);
  return entries
    .map(([, v], i) => {
      const angle = (2 * Math.PI * i) / entries.length - Math.PI / 2;
      const r = (Number(v) / 100) * radius;
      return `${cx + Math.cos(angle) * r},${cy + Math.sin(angle) * r}`;
    })
    .join(" ");
}

const btnStyle = {
  background:'#f8fafc', color:'#4f46e5', padding:'.6rem 1.0rem', border:'none', borderRadius:'1rem', fontWeight:600, cursor:'pointer'
};
