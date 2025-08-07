import React, { useEffect, useRef, useState } from "react";
import styled from "styled-components";
import { Canvas, useFrame } from "@react-three/fiber";
import { OrbitControls, useGLTF } from "@react-three/drei";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import axios from "axios";
  import MicRecorder from 'mic-recorder-to-mp3-fixed';
import * as THREE from "three";

const Page = styled.div`
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  overflow: hidden;
  background: url('/interview_back.jpg') center/cover no-repeat;
  filter: brightness(0.85);
`;

const Stage = styled.div`
  position: absolute;
  inset: 0;
  pointer-events: none;
`;

const Subtitles = styled.div`
  position: absolute;
  bottom: 120px;
  left: 50%;
  transform: translateX(-50%);
  max-width: 90%;
  padding: 1rem 2rem;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 1rem;
  backdrop-filter: blur(6px);
  font-size: 1.5rem;
  font-weight: 600;
  color: #f8fafc;
  text-align: center;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.5);
  pointer-events: none;
`;

const ChatBox = styled.form`
  width: 100%;
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);

  input {
    flex: 1;
    padding: 0.75rem 1rem;
    border-radius: 0.75rem;
    border: none;
    font-size: 1rem;
  }
  button {
  font-size: 1rem;
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 0.75rem;
    background: #4f46e5;
    color: #f8fafc;
    font-weight: 600;
    cursor: pointer;
  }
`;

const ExitButton = styled.button`
  position: absolute;
  top: 2rem;
  right: 2rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 0, 0, 0.1);
  color: #f8fafc;
  border: 1px solid rgba(255, 0, 0, 0.2);
  border-radius: 0.75rem;
  font-weight: 600;
  font-size: 1.2rem;
  backdrop-filter: blur(6px);
  cursor: pointer;
  transition: background 0.2s;
  z-index: 10;

  &:hover {
    background: rgba(255, 0, 0, 0.25);
  }
`;
const RecControls = styled.div`
  width: 100%;
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);

  button {
    flex: 1;
    padding: 0.9rem 1rem;
    border-radius: 0.75rem;
    border: none;
    font-size: 1rem;
    color: #f8fafc;
    font-weight: 600;
    cursor: pointer;
  }
  .rec      { background:#ef4444;}   /* 빨간 REC */
  .stop     { background:#4f46e5;}   /* 보라 STOP */
  .disabled { opacity:0.5; cursor:not-allowed;}
`;
function Avatar({ speaking }) {
  const { scene } = useGLTF("/models/avatar.glb");

  const mouthMeshes = useRef([]);
  useEffect(() => {
    mouthMeshes.current = [];
    scene.traverse((o) => {
      const idx = o.isMesh && o.morphTargetDictionary?.mouthOpen;
      if (idx !== undefined) {
        mouthMeshes.current.push({ mesh: o, idx });
      }
    });
  }, [scene]);

  const t = useRef(0);

  useFrame((_, delta) => {
    if (!mouthMeshes.current.length) return;

    const SPEAKING_SPEED = 6;          // speaking=true일 때 시간 증가 속도
    const IDLE_SPEED     = 2;          // speaking=false일 때 시간 증가 속도
    const BASE_OPEN      = 0.8;        // 기본 입 벌림 정도 (0~1)
    const SHAKE_AMPLITUDE = 0.5;       // 입 진동 크기 (0~1)
    const SHAKE_FREQUENCY = 2;         // 진동 주파수 (값이 클수록 빠름)

    t.current += delta * (speaking ? SPEAKING_SPEED : IDLE_SPEED);

    const base   = speaking ? BASE_OPEN : 0;
    const shake  = speaking ? Math.sin(t.current * SHAKE_FREQUENCY) * SHAKE_AMPLITUDE : 0; 
    const target = THREE.MathUtils.clamp(base + shake, 0, 1);

    mouthMeshes.current.forEach(({ mesh, idx }) => {
      mesh.morphTargetInfluences[idx] = THREE.MathUtils.lerp(
        mesh.morphTargetInfluences[idx],
        target,
        0.2 
      );
    });
  });

  return (
    <primitive
      object={scene}
      scale={2.3}
      position={[0, -2.4, 0]}
    />
  );
}

export default function InterviewRealRoomPage() {
  const navigate               = useNavigate();
  const { sessionId }          = useParams();
  const { interviewDuration=10 } = useLocation().state || {};

  const [subtitle, setSubtitle] = useState("질문을 불러오는 중…");
  const [speaking, setSpeaking] = useState(false);

  const [isRecording, setIsRecording] = useState(false);
  const [loading,     setLoading]     = useState(false);
  const [mouthVal, setMouthVal] = useState(0);

  const recorder   = useRef(new MicRecorder({ bitRate: 128 }));
  const speakTimer = useRef(null);
  const END_KEY    = `interviewEndTime_${sessionId}`;
  const savedEnd   = Number(localStorage.getItem(END_KEY));
  const initialEnd = savedEnd && savedEnd > Date.now()
      ? savedEnd
      : Date.now() + interviewDuration*60*1000;
  const endTimeRef = useRef(initialEnd);  
  localStorage.setItem(END_KEY, initialEnd.toString());

  const finishInterview = () => {
    setSubtitle("수고하셨습니다.");
    setSpeaking(false);
    setTimeout(() => navigate(`/interview/${sessionId}/result`), 3000);
  };

  const startSpeaking = (ms=4500) => {
    setSpeaking(true);
    clearTimeout(speakTimer.current);
    speakTimer.current = setTimeout(() => setSpeaking(false), ms);
  };

  const fetchQuestion = async () => {
    
    if (Date.now() >= endTimeRef.current) {
      finishInterview();
      return;
    }
    try {
      const { data } = await axios.post(
        "/interview/question/audio",
        { sessionId },
        { responseType: "arraybuffer" }
      );

      const blob = new Blob([data], { type: "audio/mpeg" });
      const url  = URL.createObjectURL(blob);
      const audio = new Audio(url);
      const ctx       = new (window.AudioContext || window.webkitAudioContext)();
      const srcNode   = ctx.createMediaElementSource(audio);
      const analyser  = ctx.createAnalyser();
      analyser.fftSize = 1024;
      const dataArray = new Uint8Array(analyser.fftSize);
      srcNode.connect(analyser);
      srcNode.connect(ctx.destination);

      const pump = () => {
        analyser.getByteTimeDomainData(dataArray);
        let sum = 0;
        for (const v of dataArray) { const x = (v-128)/128; sum += x*x; }
        const rms = Math.sqrt(sum / dataArray.length); 
        const mapped = Math.min(1, rms * 3);    
        setMouthVal(mapped);  
        if (!audio.paused) requestAnimationFrame(pump);
      };
      requestAnimationFrame(pump);

      audio.play();
      startSpeaking(audio.duration * 1000);

      setSubtitle("질문을 듣고 답변을 녹음하세요.");
    } catch (err) {
      console.error(err);
      setSubtitle("질문 재생에 실패했습니다.");
    }
  };

  const uploadAnswer = async (mp3Blob) => {
    const form = new FormData();
    form.append("file", mp3Blob, "answer.mp3");
    try {
      await axios.post(
        `/interview/answer/audio`,
        form,
        {
          params: { sessionId },
          headers: { "Content-Type": "multipart/form-data" }
        }
      );
      setSubtitle("다음 질문을 불러오는 중…");
      await fetchQuestion();
    } catch (err) {
      console.error(err);
      alert("답변 업로드에 실패했습니다. 다시 시도해 주세요.");
    }
  };

  const startRecording = async () => {
    try {
      await recorder.current.start();
      setIsRecording(true);
    } catch (e) {
      alert("마이크 권한이 필요합니다.");
    }
  };

  const stopRecording = async () => {
    setLoading(true);
    setIsRecording(false);
    try {
      const [, blob] = await recorder.current
        .stop()
        .getMp3();        
      await uploadAnswer(blob);
    } catch (e) {
      console.error(e);
      alert("녹음 처리 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuestion();
    return () => clearTimeout(speakTimer.current);
  }, []);

  return (
    <Page>
      <ExitButton onClick={()=>{
        if (window.confirm("정말 면접을 종료하시겠습니까?"))
          navigate(`/interview/${sessionId}/result`);
      }}>
        면접 종료
      </ExitButton>

      <Stage>
        <Canvas camera={{ position: [0,1.4,3.2], fov:35 }}>
          <ambientLight intensity={0.7}/>
          <directionalLight position={[1,2,3]} intensity={1}/>
          <Avatar speaking={speaking}/>
          <OrbitControls enablePan={false} enableRotate={false} enableZoom={false} target={[0,1.2,0]}/>
        </Canvas>
      </Stage>

      <Subtitles>{subtitle}</Subtitles>

      <RecControls>
        <button
          className={`rec ${isRecording || loading ? "disabled" : ""}`}
          disabled={isRecording || loading}
          onClick={startRecording}
        >
          {isRecording ? "REC…" : "녹음 시작"}
        </button>
        <button
          className={`stop ${!isRecording || loading ? "disabled" : ""}`}
          disabled={!isRecording || loading}
          onClick={stopRecording}
        >
          {loading ? "업로드 중…" : "녹음 종료 & 전송"}
        </button>
      </RecControls>
    </Page>
  );
}