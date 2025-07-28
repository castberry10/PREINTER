// InterviewRoomPage.jsx
import React, { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, useGLTF } from '@react-three/drei';
import { useNavigate, useParams } from 'react-router-dom';

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
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 0.75rem;
    background: #4f46e5;
    color: #f8fafc;
    font-weight: 600;
    cursor: pointer;
  }
`;

function Avatar({ speaking }) {
  const { scene } = useGLTF('/models/avatar.glb');
  const tRef = useRef(0);
  const mouthRef = useRef(null);
  const jawIndexRef = useRef(null);

  useEffect(() => {
    scene.traverse((obj) => {
      if (obj.isMesh && obj.morphTargetDictionary?.jawOpen !== undefined) {
        mouthRef.current = obj;
        jawIndexRef.current = obj.morphTargetDictionary.jawOpen;
      }
    });
  }, [scene]);

  useFrame((_, delta) => {
    const mesh = mouthRef.current;
    const idx = jawIndexRef.current;
    if (!mesh || idx === undefined) return;

    tRef.current += delta * (speaking ? 6 : 2);
    const target = speaking ? 1 : 0;
    mesh.morphTargetInfluences[idx] +=
      (target - mesh.morphTargetInfluences[idx]) * 0.1;
    if (speaking) {
      mesh.morphTargetInfluences[idx] +=
        Math.sin(tRef.current * 20) * 0.03;
    }
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

  const QUESTIONS = [
    '저희 회사에 왜 지원하셨죠?',
    '앞으로 5년 뒤 목표는 무엇인가요?',
    '가장 큰 실패 경험과 배운 점은 무엇인가요?',
  ];

  const [idx, setIdx] = useState(0);
  const [subtitle, setSubtitle] = useState(QUESTIONS[0]);
  const [speaking, setSpeaking] = useState(true);
  const [input, setInput] = useState('');

  const timerRef = useRef(null);

  const startSpeaking = () => {
    setSpeaking(true);
    timerRef.current = setTimeout(() => setSpeaking(false), 5000);
  };

  useEffect(() => {
    startSpeaking();
    return () => clearTimeout(timerRef.current);
  }, []);

  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    console.log(`Q${idx + 1} 답변:`, input);
    setInput('');
    setSubtitle('응답 전송 중…');

    setTimeout(() => {
      const nextIdx = idx + 1;
      if (nextIdx < QUESTIONS.length) {
        setIdx(nextIdx);
        setSubtitle(QUESTIONS[nextIdx]);
        startSpeaking();
      } else {
        navigate(`/interview/${sessionId}/result`);
      }
    }, 500);
  };

  return (
    <Page>
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
        <button type="submit">Send</button>
      </ChatBox>
    </Page>
  );
}
