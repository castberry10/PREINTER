// InterviewRoomPage.jsx
import React, { useEffect, useRef, useState } from "react";
import styled from "styled-components";
import { Canvas, useFrame } from "@react-three/fiber";
import { OrbitControls, useGLTF } from "@react-three/drei";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import axios from "axios";
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

export default function InterviewRoomPage() {

  const navigate = useNavigate();
  const { sessionId } = useParams();
  const { interviewDuration = 10 } = useLocation().state || {};
  const [subtitle, setSubtitle] = useState("질문을 불러오는 중…");
  const [speaking, setSpeaking]     = useState(false);
  const [input, setInput]           = useState("");
  const [loading, setLoading]       = useState(false);    
  const timerRef   = useRef(null);
  const speakTimer = useRef(null);
  const END_KEY = `interviewEndTime_${sessionId}`;
  // const now = Date.now();
  // const endTime = now + interviewDuration * 60 * 1000;
  // localStorage.setItem("interviewEndTime", endTime.toString());
  const savedEnd = Number(localStorage.getItem(END_KEY));
  const initialEnd =
    savedEnd && savedEnd > Date.now()
      ? savedEnd
      : Date.now() + interviewDuration * 60 * 1000;
  
  const endTimeRef = useRef(initialEnd);
  localStorage.setItem(END_KEY, initialEnd.toString());
  const finishInterview = () => {
    setSubtitle("수고하셨습니다.");
    setSpeaking(false);
    // 3초 뒤 결과 페이지로 이동
    setTimeout(() => navigate(`/interview/${sessionId}/result`), 3000);
};

  const startSpeaking = (ms = 4500) => {
    setSpeaking(true);
    speakTimer.current && clearTimeout(speakTimer.current);
    speakTimer.current = setTimeout(() => setSpeaking(false), ms);
  };

  useEffect(() => {
    fetchQuestion();
    return () => clearTimeout(speakTimer.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchQuestion = async () => {
    if (Date.now() >= endTimeRef.current) {
      finishInterview();
      return;
    }

    try {
      const { data: question } = await axios.post(
        "/interview/question/text",
        { sessionId }
      );

      // if (!question || question === "END") {
      //   finishInterview();
      //   return;
      // }

      setSubtitle(question);
      startSpeaking();
    } catch (err) {
      console.error(err);
      setSubtitle("질문을 가져오지 못했습니다. 다시 시도해 주세요.");
    }
  };


    const handleSend = async (e) => {
      e.preventDefault();
      if (!input.trim() || loading) return;

      setLoading(true);
      try {
        await axios.post("/interview/answer/text", {
          sessionId,
          answer: input.trim(),
        });

        setInput("");
        setSubtitle("다음 질문을 불러오는 중…");
        await fetchQuestion(); 
      } catch (err) {
        console.error(err);
        alert("답변 전송에 실패했습니다. 다시 시도해 주세요.");
      } finally {
        setLoading(false);
      }
    };

  return (
    <Page>
        <ExitButton onClick={() => {
          const confirmExit = window.confirm("정말 면접을 종료하시겠습니까?");
          if (confirmExit) {
            navigate(`/interview/${sessionId}/result`);
          }
        }}>
          면접 종료
        </ExitButton>
      <Stage>
        <Canvas camera={{ position: [0, 1.4, 3.2], fov: 35 }}>
          <ambientLight intensity={0.7} />
          <directionalLight position={[1, 2, 3]} intensity={1} />
          <Avatar speaking={speaking} />
          <OrbitControls
            enablePan={false}
            enableRotate={false}
            enableZoom={false}
            target={[0, 1.2, 0]}
          />
        </Canvas>
      </Stage>

      <Subtitles>{subtitle}</Subtitles>

      <ChatBox onSubmit={handleSend}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="답변을 입력하세요…"
        />
        <button type="submit" disabled={loading}>
          {loading ? "전송 중…" : "전송"}
        </button>
      </ChatBox>
    </Page>
  );
}
