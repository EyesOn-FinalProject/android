package com.example.eyeson.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList
//
//class voice(val context: Context) {
//    var recognizer: SpeechRecognizer? = null  // 음성인식기능 객체 선언
//    var stt_intent: Intent? = null //음성인식기능 객체담을 객체 선언
//    var ttsObj: TextToSpeech? = null // tts객체 선언(텍스트를 음성으로 변환)
    //음성기능 상태에따른 설정
    //음성기능 객체 설정

//
//    var listener = (object : RecognitionListener {
//        //startListening()호출 후 음성이 입력되기 전 상태
//        override fun onReadyForSpeech(params: Bundle?) {
//            //화면에 잠깐 나왔다가 꺼지는 Toast실행
//            printToast("음성인식을 시작합니다.")
//        }
//
//        //음성이 입력되고 있는 상태
//        override fun onBeginningOfSpeech() {
//            //Logcat에 찍어보기
//            Log.d("recog", "onBeginningOfSpeech")
//        }
//
//        //사운드 레벨이 변경된 상태
//        override fun onRmsChanged(rmsdB: Float) {
//            Log.d("recog", "onRmsChanged")
//        }
//
//        //소리가 수신된 상태
//        override fun onBufferReceived(buffer: ByteArray?) {
//            Log.d("recog", "onBufferReceived")
//        }
//
//        //부분적으로 인식 결과를 사용하기 위한 상태
//        override fun onPartialResults(partialResults: Bundle?) {
//            Log.d("recog", "onPartialResults")
//        }
//
//        //향후 이벤트를 추가하기 위해 예약된 상태
//        override fun onEvent(eventType: Int, params: Bundle?) {
//            Log.d("recog", "onEvent")
//        }
//
//        //음성 인식을 마친 상태
//        override fun onEndOfSpeech() {
//            Log.d("recog", "onEndOfSpeech")
//        }
//
//        //네트워크 혹은 인식 오류가 발생한 상태
//        override fun onError(error: Int) {
//            var message = ""
//            when (error) {
//                SpeechRecognizer.ERROR_AUDIO -> message = "오디오 에러"
//                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "퍼미션이 설정되지 않음";
//                SpeechRecognizer.ERROR_CLIENT -> message = "클라이언트 에러"
//                SpeechRecognizer.ERROR_NETWORK -> message = "네트워크 에러"
//                SpeechRecognizer.ERROR_NO_MATCH -> {
//                    message = "적당한 결과를 찾지 못함"
//                    ttsObj?.speak("다시 시도해주십시오", TextToSpeech.QUEUE_FLUSH, null,
//                            this.hashCode().toString() + "0")
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        recognizer?.startListening(stt_intent)
//                    }, 2000)
//                }
//                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "다른 작업 처리 중이라 바쁨"
//                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "말을 너무 길게 해서 시간초과"
//            }
//            Log.d("recog", message)
//        }
//
//        //음성 인식을 마치고 결과가 나온 상태
//        override fun onResults(results: Bundle?) {
//            //현재 데이터가 있을 경우 비우기
//            if(data != null){
//                data?.clear()
//                voiceMsg = ""
//            }
//            //음성 인식된 것을 data변수에 담고 edittool에 text 셋팅
//            data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
//            for (i in data!!.indices) {
//                edittool?.setText(data!!.get(i))
//            }
//            //edittool에 셋팅된 텍스트를 스트링으로 변환시켜 voiceMsg 변수에 담음
//            voiceMsg = edittool?.text.toString()
//
//
//            //음성이 발생되면 처리하고 싶은 기능을 구현
//            val utteranceId = this.hashCode().toString() + "0"
//            ttsObj?.setPitch(1f) //음성톤을 기본보다 2배 올려준다.
//            ttsObj?.setSpeechRate(1f) //읽는 속도 설정
//
//            //버스번호를 안받았을때
//            if(busNum == "") {
//                //정규식을 통한 버스번호 구분구간
//                val reg = """[-,0-9]{1,6}""".toRegex()
//                var check : MatchResult? = reg.find("$voiceMsg")
//                voiceMsg = ""
//                while(check!=null){
//                    val value : String = check!!.value
//                    voiceMsg += value
//                    check = check?.next()
//                }
//                if(voiceMsg == ""){
//                    ttsObj?.speak("버스번호를 불러주세요", TextToSpeech.QUEUE_FLUSH, null,
//                            this.hashCode().toString() + "0")
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        recognizer?.startListening(stt_intent)
//                    }, 2000)
//                }else {
//                    //음성인식된게 있으면
//                    busNum = voiceMsg
//                    Log.d("recog", "$voiceMsg")
//                    ttsObj?.speak("${busNum}번호가 맞습니까 예 아니오로 대답해주십시오", TextToSpeech.QUEUE_FLUSH, null,
//                            utteranceId)
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        recognizer?.startListening(stt_intent)
//                    }, 5000)
//                }
//            }else{ //버스번호를 받았을때
//                if (voiceMsg == "예") { //음성인식된게 "예"이면
//                    ttsObj?.speak("${busNum}번호를 승차예약합니다.", TextToSpeech.QUEUE_FLUSH, null,
//                            utteranceId)
//                    buttonId.text = "탑승 완료"
//                    publish("id01/" + "$busNum/" +"$latitude/" + "${longitude}")
//                    status = 1
//                } else if(voiceMsg in "아니오" .. "아니요") { //음성인식된게 "아니오"이면
//                    ttsObj?.speak("승차예약을 취소합니다.", TextToSpeech.QUEUE_FLUSH, null,
//                            utteranceId)
//                    data?.clear()
//                    voiceMsg = ""
//                    busNum = ""
//                }else {
//                    ttsObj?.speak("다시한번 말씀해주십시오", TextToSpeech.QUEUE_FLUSH, null,
//                            utteranceId)
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        recognizer?.startListening(stt_intent)
//                    }, 2000)
//                }
//            }
//
////                else if (voiceMsg == "음악") {
////                    val launchIntent =
////                            packageManager.getLaunchIntentForPackage("com.iloen.melon")
////                    startActivity(launchIntent)
////                    onDestroy()
////                }
//            Log.d("recog", "onResults")
//
//        }
//    })
//
//}