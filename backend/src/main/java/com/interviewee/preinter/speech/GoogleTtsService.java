package com.interviewee.preinter.speech;

import com.google.cloud.texttospeech.v1.*;

public class GoogleTtsService {
    private final TextToSpeechClient ttsClient;

    public GoogleTtsService() throws Exception {
        this.ttsClient = TextToSpeechClient.create();
    }

    /**
     * 텍스트를 MP3 오디오 바이트로 변환합니다.
     *
     * @param text 인터뷰 로직이 생성한 답변 텍스트
     * @return 합성된 MP3 바이트 배열
     */
    public byte[] synthesize(String text) throws Exception {
        // 합성할 텍스트 입력
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();

        // 한국어, 중립 성별 음성 선택
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build();

        // MP3 포맷으로 설정
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        // 합성 요청
        com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse response =
                ttsClient.synthesizeSpeech(input, voice, audioConfig);

        // 오디오 바이트 반환
        return response.getAudioContent().toByteArray();
    }
}
