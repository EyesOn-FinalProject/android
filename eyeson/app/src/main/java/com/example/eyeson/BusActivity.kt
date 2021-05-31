package com.example.eyeson

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.eyeson.classFile.MyMqtt
import com.example.eyeson.classFile.MyTableDB
import com.example.eyeson.classFile.RaspberryId
import com.example.eyeson.dataFile.UUID_Parcelable
import com.google.zxing.integration.android.IntentIntegrator
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.IOException
import java.util.*

class BusActivity : AppCompatActivity(), LocationListener {

    private var permission_state = false //음성인식 권한 상태
    var stt_intent: Intent? = null //음성인식기능 객체담을 객체 선언
    var recognizer: SpeechRecognizer? = null  // 음성인식기능 객체 선언
    var edittool: EditText? = null // 안드로이드 xml 텍스트박스형식으로 변수 선언
    var ttsObj: TextToSpeech? = null // tts객체 선언(텍스트를 음성으로 변환)
    var locationMgr: LocationManager? = null //위치기능 객체 선언
    //위도,경도 담을 변수
    var latitude: Double? = null
    var longitude: Double? = null

    //전역변수
    var reservation = "" //버스번호 담을 변수
    var data: ArrayList<String> ?= null // 음성데이터 담기
    var voiceMsg: String = "" // 음성데이터 스트링 형태
    var btnStatus = "riding" //버튼 상태 변화 변수(승차[riding],탑승완료[busTime],하차[getOff])
    var busStation = ""
    var busLicenseNum = "" //버스차량번호
    var ruuid = "ruuid" //라즈베리파이 고유번호
    var androidDb:MyTableDB? = null //db객체 담을 변수

    lateinit var obj: UUID_Parcelable
    lateinit var uuid: String
    lateinit  var objintent:Intent //인텐드 변수 선언
    lateinit var mqttClient: MyMqtt //mqtt클래스 변수 선언

    //남은시간안내하기위한 전역변수
    var target_stId = ""
    var target_busRouteId = ""
    var target_ord = ""

    //음성이 발생되면 처리하고 싶은 기능을 구현
    lateinit var utteranceId:String

    // 화면 생성 부분
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.bus_notification)

        //db객체 생성
        androidDb = MyTableDB(this)
        //id값 찾기
        var buttonId = findViewById<Button>(R.id.buttonId)
        var buttonId2 = findViewById<Button>(R.id.buttonId2)
        var qrcode = findViewById<Button>(R.id.QRcode)
        var voiceText = findViewById<EditText>(R.id.voiceText)

        utteranceId = this.hashCode().toString() + "0"
        objintent = intent //인텐드 변수 선언
        obj = objintent.getParcelableExtra<UUID_Parcelable>("uuidObj")!! //UUID_Parcelable 형태값 받아오기
        uuid = obj?.uu_id.toString() //uuid 가져오기
        mqttClient = MyMqtt(applicationContext, "tcp://15.164.46.54:1883")
        locationMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager //위치서비스 쓸 변수 설정
        try {
            mqttClient.setCallback(::onReceived)
            mqttClient.connect(arrayOf<String>("eyeson/$uuid"))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        edittool = voiceText //voiceText라는 editText를 edittool에 담음.
        // tts 언어 설정(한국어 설정)
        ttsObj = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                ttsObj?.language = Locale.KOREAN
                ttsObj?.setPitch(1f) //음성톤을 기본보다 2배 올려준다.
                ttsObj?.setSpeechRate(1f) //읽는 속도 설정
            }
        })
        //1. Permission(권한)을 먼저 체크 - 음성기능권한(RECORD_AUDIO), 위치기능권한(ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
            //2. 권한이 없는 경우 권한을 설정하는 메시지를 띄운다.
            permission_state = false
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECORD_AUDIO),
                    1000)
        } else {
            permission_state = true
//            printToast("권한 확인완료")
            //provider 를 가져올 수 있는 function call
            //location을 가져올 수 있는 funcion call
            Log.d("Location", "getLocation 실행")
            getLocation()
        }



        //음성기능 객체 설정
        stt_intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        stt_intent?.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        stt_intent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")


        //음성기능 상태에따른 설정
        var listener = (object : RecognitionListener {
            //startListening()호출 후 음성이 입력되기 전 상태

            override fun onReadyForSpeech(params: Bundle?) {
                //화면에 잠깐 나왔다가 꺼지는 Toast실행
                printToast("음성인식을 시작합니다.")
            }

            //음성이 입력되고 있는 상태
            override fun onBeginningOfSpeech() {
                //Logcat에 찍어보기
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
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        message = "적당한 결과를 찾지 못함"
                        ttsObj?.speak("다시 시도해주십시오", TextToSpeech.QUEUE_FLUSH, null,
                                this.hashCode().toString() + "0")
                        Handler(Looper.myLooper()!!).postDelayed({
                            recognizer?.startListening(stt_intent)
                        }, 2000)
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "다른 작업 처리 중이라 바쁨"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "말을 너무 길게 해서 시간초과"
                }
                Log.d("recog", message)
            }


            //음성 인식을 마치고 결과가 나온 상태
            override fun onResults(results: Bundle?) {
                //현재 데이터가 있을 경우 비우기
                if(data != null){
                    data?.clear()
                    voiceMsg = ""
                }
                //음성 인식된 것을 data변수에 담고 edittool에 text 셋팅
                data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                for (i in data!!.indices) {
                    edittool?.setText(data!!.get(i))
                }
                //edittool에 셋팅된 텍스트를 스트링으로 변환시켜 voiceMsg 변수에 담음
                voiceMsg = edittool?.text.toString()



                //버스번호를 안받았을때
                if(reservation == "") {
                    //정규식을 통한 버스번호 구분구간
//                    val reg = """[-,0-9]{1,6}""".toRegex()
//                    var check : MatchResult? = reg.find("$voiceMsg")
//                    voiceMsg = ""
//                    while(check!=null){
//                        val value : String = check!!.value
//                        voiceMsg += value
//                        check = check?.next()
//                    }
                    if(voiceMsg == ""){
                        ttsObj?.speak("다시한번 말씀해주십시오", TextToSpeech.QUEUE_FLUSH, null,
                                this.hashCode().toString() + "0")
                        Handler(Looper.myLooper()!!).postDelayed({
                            recognizer?.startListening(stt_intent)
                        }, 2000)
                    }else {
                        //음성인식된게 있으면
                        reservation = voiceMsg
                        Log.d("recog", "$voiceMsg")
                        ttsObj?.speak("${reservation}가 맞습니까 예 아니오로 대답해주십시오", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                        Handler(Looper.myLooper()!!).postDelayed({
                            recognizer?.startListening(stt_intent)
                        }, 4500)
                    }
                }else{ //버스번호나 목적지를 받았을때
                    if (voiceMsg == "예") { //음성인식된게 "예"이면
                        Log.d("mqtt", "onResults")
                        publish("android/" +  "$btnStatus/" + "$reservation/" +"$latitude/" + "${longitude}")
                    } else if(voiceMsg in "아니오" .. "아니요") { //음성인식된게 "아니오"이면
                        ttsObj?.speak("승차예약을 취소합니다.", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                        data?.clear()
                        voiceMsg = ""
                        reservation = ""
                        busStation = ""
                        busLicenseNum = ""
                    }else {
                        ttsObj?.speak("다시한번 말씀해주십시오", TextToSpeech.QUEUE_FLUSH, null,
                                utteranceId)
                        Handler(Looper.myLooper()!!).postDelayed({
                            recognizer?.startListening(stt_intent)
                        }, 2000)
                    }
                }

                Log.d("recog", "onResults")

            }
        })
        // 음성인식 인스턴스 얻기
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        // 해당 인스턴스에 콜백 리스너 등록
        recognizer?.setRecognitionListener(listener)

        //QR코드 실행
        qrcode.setOnClickListener {
            printToast(androidDb?.select().toString())
            if (androidDb?.select().toString() == "[]" ) {
                startBarcodeReader(it)
            }else{
                androidDb?.delete("아이즈온")
            }
        }
        //승차 버튼 클릭 시 실행
        buttonId.setOnClickListener {

            if (btnStatus == "riding")
            {
                publish("android/busStation/$latitude/$longitude")

            } else if (btnStatus == "busTime") {
                publish("android/busTime/$target_stId/$target_busRouteId/$target_ord")
            } else if (btnStatus == "getOff") {
                buttonId.text = "승차"
                publish("android/driver/$busLicenseNum/$btnStatus/$busStation")
                data?.clear()
                voiceMsg = ""
                reservation = ""
                busStation = ""
                busLicenseNum = ""
                btnStatus = "riding"
            }
        }
        buttonId2.setOnClickListener {
            if (btnStatus=="busTime"){
                ttsObj?.speak("승차예약을 취소합니다.", TextToSpeech.QUEUE_FLUSH, null,
                        utteranceId)
                data?.clear()
                btnStatus = "riding"
                voiceMsg = ""
                reservation = ""
                busStation = ""
                busLicenseNum = ""
                buttonId.text = "승차"
            }else{
                ttsObj?.speak("취소할 예약이 없습니다.", TextToSpeech.QUEUE_FLUSH, null,
                    utteranceId)
            }
        }

    }
    //mqtt publish
    fun publish(data: String) {
        //mqttClient 의 publish기능의의 메소드를 호출
        mqttClient.publish("eyeson/$uuid", data)
    }
    fun rpublish(data: String) {
        //mqttClient 의 publish기능의의 메소드를 호출
        mqttClient.publish("eyeson/$ruuid", data)
    }
    //mqtt subscribe
    fun onReceived(topic: String, message: MqttMessage) {
        //id값 찾기
        var buttonId = findViewById<Button>(R.id.buttonId)
        var buttonId2 = findViewById<Button>(R.id.buttonId2)
        val msg = String(message.payload)
        var msgList = msg.split("/")
        if (msgList[0] == "bigData"){
            if (msgList[1] == "error"){
                ttsObj?.speak("오류로인해 예약취소합니다.", TextToSpeech.QUEUE_FLUSH, null,
                        utteranceId)
                data?.clear()
                btnStatus = "riding"
                voiceMsg = ""
                reservation = ""
                busStation = ""
                busLicenseNum = ""
                buttonId.text = "승차"
            }else if(msgList[1] == "ok"){
                buttonId.text = "버스남은시간안내"
                reservation = msgList[2]
                var arrival = msgList[3]
                busLicenseNum = msgList[4]
                busStation = msgList[5]
                target_stId = msgList[6]
                target_busRouteId = msgList[7]
                target_ord = msgList[8]
                ttsObj?.speak("${reservation}번호를 승차예약합니다. $arrival", TextToSpeech.QUEUE_FLUSH, null,
                        utteranceId)
                Log.d("mqtt", "$btnStatus")
                publish("android/driver/$busLicenseNum/$btnStatus/$busStation")
                btnStatus = "busTime"
            }else if(msgList[1] == "last"){
                rpublish("android/camera/on/$uuid")
                var arrival = msgList[2]
                ttsObj?.speak("${reservation} $arrival 구조물로 이동해주세요.", TextToSpeech.QUEUE_FLUSH, null,
                        utteranceId)
                publish("android/ai/$reservation/$busLicenseNum")
            }else if(msgList[1] == "busStation"){
                Log.d("mqtt", "busStation 잘 넘어옴")
                busStation = msgList[2]
                //음성인식 시작(stt_intent 설정한대로)
                ttsObj?.speak("현재 정류소는 $busStation 입니다. 버스번호나 목적지정류소를 불러주세요", TextToSpeech.QUEUE_FLUSH, null,
                    utteranceId)
                Handler(Looper.myLooper()!!).postDelayed({
                    recognizer?.startListening(stt_intent)
                }, 6500)
            }else if(msgList[1] == "busTime"){
                var arrival = msgList[2]
                ttsObj?.speak("${reservation} 버스는 $arrival", TextToSpeech.QUEUE_FLUSH, null,
                    utteranceId)
            }
        }else if(msgList[0] == "ai"){
            Log.d("mqtt", "$btnStatus")
            if(msgList[1] == "noticeInfo"){
                if(msgList[2] == ""){
                    
                }else if(msgList[2] == ""){

                }else if(msgList[2] == ""){

                }
            }else if(msgList[1] == "targetBus"){
                ttsObj?.speak("해당버스는 ${reservation} 번 버스입니다. 탑승해주십시오.", TextToSpeech.QUEUE_FLUSH, null,
                        utteranceId)
            }else if(msgList[1] == "doorInfo"){
                Log.d("mqtt", "${msgList[1]}")
                if(msgList[2] == "R"){
                    ttsObj?.speak("오른쪽에 문이 있습니다.", TextToSpeech.QUEUE_FLUSH, null,
                            utteranceId)
                }else if(msgList[2] == "L"){
                    ttsObj?.speak("왼쪽에 문이 있습니다.", TextToSpeech.QUEUE_FLUSH, null,
                            utteranceId)

                }else if(msgList[2] == "C"){
                    ttsObj?.speak("정면에 문이 있습니다.", TextToSpeech.QUEUE_FLUSH, null,
                            utteranceId)

                }else if(msgList[2] == "X"){
                    ttsObj?.speak("문이 인식되지 않았습니다. 주의를 둘러봐주세요.", TextToSpeech.QUEUE_FLUSH, null,
                            utteranceId)

                }else if(msgList[2] == "correctC"){
                    ttsObj?.speak("정면에 문이 있습니다. 이 방향으로 다가가주세요.", TextToSpeech.QUEUE_FLUSH, null,
                            utteranceId)
                    publish("android/driver/$busLicenseNum/boarding/$busStation")
                    buttonId.text = "하차버튼"
                    rpublish("android/camera/off")
                    btnStatus = "getOff"
                }
            }
        }
    }

    //위치 데이터 가져오기
    fun getLocation() {
        var currentLatLng: Location? = null
        //권한 확인
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }
        //GPS PROVIDER 이용
        currentLatLng = locationMgr?.getLastKnownLocation(LocationManager.GPS_PROVIDER) // GPS_PROVIDER를 이용한 마지막 위치 데이터 넣기
        if (currentLatLng != null) { //위치데이터가 존재할시
            //3초마다 거리 0 바뀔때마다 GPS_PROVIDER를 업데이트
            locationMgr?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0.0f, this)
            latitude = currentLatLng.latitude //위도
            longitude = currentLatLng.longitude // 경도
            Log.d("Location", "$currentLatLng 현재 내 위치 값: ${latitude}, ${longitude}") //로그 찍기
//            printToast("gps데이터") // 토스트 출력(앱내 화면에 출력되었다가 사라지는 기능)
        } else {
            //GPS안될시 NETWORK
            currentLatLng = locationMgr?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (currentLatLng != null) {
                Log.d("Location", "network인..")
                locationMgr?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0.0f, this)
                latitude = currentLatLng.latitude
                longitude = currentLatLng.longitude
                Log.d("Location", "$currentLatLng 현재 내 위치 값: ${latitude}, ${longitude}")
//                printToast("net데이터")
            } else {
                //NETWORK안될시 PASSIVE
                currentLatLng = locationMgr?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                if (currentLatLng != null) {
                    Log.d("Location", "passive인..")
                    locationMgr?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 3000, 0.0f, this)
                    latitude = currentLatLng.latitude
                    longitude = currentLatLng.longitude
                    Log.d("Location", "$currentLatLng 현재 내 위치 값: ${latitude}, ${longitude}")
//                    printToast("passive데이터")
                }
            }
        }
        //위도,경도를 바탕으로 주소찾기
        var mGeoCoder = Geocoder(applicationContext, Locale.KOREAN)
        var mResultList: List<Address>? = null
        try { // 성공 시
            mResultList = mGeoCoder.getFromLocation(
                    latitude!!, longitude!!, 1
            )
        } catch (e: IOException) { //실패시
            e.printStackTrace()
        }
        if (mResultList != null) { //성공했을 시 데이터 출력
            Log.d("Location", mResultList[0].getAddressLine(0))
//            printToast("${mResultList[0].getAddressLine(0)}")
        }
    }

    //location 업데이트(시간,위치 등 변환시 )
    //locationMgr?.requestLocationUpdates(LocationManager.PROVIDER, 3000, 0.0f, this) 을 통해 작동
    override fun onLocationChanged(location: Location) {
        Log.d("Location","체인지 진입")
        latitude = location.latitude
        longitude = location.longitude
        Log.d("Location", "$location 체인지 내 위치 값: ${latitude}, ${longitude}")
        var mGeoCoder = Geocoder(applicationContext, Locale.KOREAN)
        var mResultList: List<Address>? = null
        try {
            mResultList = mGeoCoder.getFromLocation(
                    latitude!!, longitude!!, 1
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (mResultList != null) {
            Log.d("Location", mResultList[0].getAddressLine(0))
        }
    }


    //권한 설정창이 끝나면
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.size > 0) { //권한 처리 결과를 확인하고 요청한 요청 코드가 맞으면
            var check_result = true
            permission_state = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result != true) {
                Toast.makeText(this, "권한 설정이 거부되었습니다.\n설정에서 권한을 허용해야 합니다..", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "권한 설정이 되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 화면에 띄워지는 toast를 메소드로 따로 뺌
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
    //QR코드스캐너 실행
    fun startBarcodeReader(view: View) {
        IntentIntegrator(this).initiateScan()
    }
    //돌아왔을때
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)

        if(result != null){

            if(result.contents != null){
                var raspberryId = RaspberryId(result.contents)
                androidDb?.insert(raspberryId)
                Toast.makeText(this,"Scanned: " + result.contents, Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"failed",Toast.LENGTH_SHORT).show()
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}