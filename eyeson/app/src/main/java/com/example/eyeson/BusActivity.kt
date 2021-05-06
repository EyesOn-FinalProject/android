package com.example.eyeson

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.bus_notification.*
import java.util.*

class BusActivity : AppCompatActivity() {
    private var permission_state = false //음성인식 권한 상태
    var stt_intent: Intent? = null //음성인식기능 객체담을 객체 선언
    var recognizer: SpeechRecognizer? = null  // 음성인식기능 객체 선언
    var edittool: EditText? = null // 안드로이드 xml 텍스트박스 선언
    var ttsObj: TextToSpeech? = null // tts객체 선언(텍스트를 음성으로 변환)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bus_notification)
        edittool = voiceText
        ttsObj = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                ttsObj?.language = Locale.KOREAN
            }
        })
        //1. Permission을 먼저 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            permission_state = true
            printToast("권한이 설정되었습니다.")
        } else {
            permission_state = false
            printToast("권한을 설정해야 합니다.")
            //2. 권한이 없는 경우 권한을 설정하는 메시지를 띄운다.
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    1000
            )
        }
        var status = 0 //버튼 상태 변화 변수
        var busNum = "" //버스번호 담을 변수
        var data: ArrayList<String> ?= null // 음성데이터 담기
        var voiceMsg: String = "" // 음성데이터 스트링 형태

        //음성기능 객체 설정
        stt_intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        stt_intent?.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        stt_intent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        //음성기능 상태에따른 설정
        var listener = (object : RecognitionListener {
            //startListening()호출 후 음성이 입력되기 전 상태
            override fun onReadyForSpeech(params: Bundle?) {
                printToast("음성인식을 시작합니다.")
            }

            //음성이 입력되고 있는 상태
            override fun onBeginningOfSpeech() {
                Log.d("recog", "onBeginningOfSpeech")
            }

            //사운드 레벨이 변경된 상태
            override fun onRmsChanged(rmsdB: Float) {
                Log.d("recog", "onRmsChanged")
            }

            //소리가 수신된 상태
            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("recog", "onBufferReceived")
            }

            //부분적으로 인식 결과를 사용하기 위한 상태
            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("recog", "onPartialResults")
            }

            //향후 이벤트를 추가하기 위해 예약된 상태
            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("recog", "onEvent")
            }

            //음성 인식을 마친 상태
            override fun onEndOfSpeech() {
                Log.d("recog", "onEndOfSpeech")
            }

            //네트워크 혹은 인식 오류가 발생한 상태
            override fun onError(error: Int) {
                var message = ""
                when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> message = "오디오 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "퍼미션이 설정되지 않음";
                    SpeechRecognizer.ERROR_CLIENT -> message = "클라이언트 에러"
                    SpeechRecognizer.ERROR_NETWORK -> message = "네트워크 에러"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "다른 작업 처리 중이라 바쁨"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "말을 너무 길게 해서 시간초과"
                }
                Log.d("recog", message)
            }

            //음성 인식을 마치고 결과가 나온 상태
            override fun onResults(results: Bundle?) {
                if(data != null){
                    data?.clear()
                    voiceMsg = ""
                }
                data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                for (i in data!!.indices) {
                    edittool?.setText(data!!.get(i))
                }
                voiceMsg = edittool?.text.toString()
                //음성이 발생되면 처리하고 싶은 기능을 구현
                val utteranceId = this.hashCode().toString() + "0"
                ttsObj?.setPitch(1f) //음성톤을 기본보다 2배 올려준다.
                ttsObj?.setSpeechRate(1f)

                if(busNum == "") {
                    busNum = voiceMsg
                    if(voiceMsg == "") {
                        ttsObj?.speak("다시 시도해주십시오", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                    } else {
                        ttsObj?.speak("${busNum}번호가 맞습니까 예 아니오로 대답해주십시오", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                    }
                }else{
                    ttsObj?.speak("${voiceMsg}입력", TextToSpeech.QUEUE_FLUSH, null,
                            utteranceId)
                    if (voiceMsg == "예") {
                        ttsObj?.speak("${busNum}번호를 승차예약합니다.", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                    } else if(voiceMsg == "아니오") {
                        ttsObj?.speak("승차예약을 취소합니다.", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                    }
                }

//                else if (voiceMsg == "음악") {
//                    val launchIntent =
//                            packageManager.getLaunchIntentForPackage("com.iloen.melon")
//                    startActivity(launchIntent)
//                    onDestroy()
//                }
                Log.d("recog", "onResults")

            }
        })

        buttonId.setOnClickListener {
//            if (status == 0)
//
//            {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizer?.setRecognitionListener(listener)
            recognizer?.startListening(stt_intent)
            Handler(Looper.myLooper()!!).postDelayed({
                if (busNum==""){

                }else{
                    recognizer?.startListening(stt_intent)
                }
            }, 7000)


//            buttonId.text = "탑승 완료"
//            status = 1
//            } else if (status == 1) {
//                buttonId.text = "하차"
//                status = 2
//            } else {
//                buttonId.text = "승차"
//                status = 0
//            }
//        }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty()) { //권한의 성공 설정에 대한 결과가 있다는 의미
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permission_state = true
                printToast("권한 설정 마무리 완료")
            } else {
                printToast("권한 설정을 하지 않았으므로 기능을 사용할 수 없습니다.")
            }
        }
    }
    private fun printToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        if (ttsObj != null) {
            ttsObj?.stop()
            ttsObj?.shutdown()
            ttsObj = null
        }
        if (recognizer != null) {
            recognizer?.destroy()
            recognizer?.cancel()
            recognizer = null
        }
    }
}