
package com.example.eyeson

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

class MainActivity2 : AppCompatActivity(), LocationListener {
    var locationMgr: LocationManager? = null //위치기능 객체 선언
    var latitude: Double? = null
    var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Log.d("recog", "뷰생성")

        locationMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //1. Permission(권한)을 먼저 체크 -
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            printToast("권한을 설정해야 합니다.")
            //2. 권한이 없는 경우 권한을 설정하는 메시지를 띄운다.
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    1000)
        } else {
            printToast("권한 확인완료")
            //provider 를 가져올 수 있는 function call
            //location을 가져올 수 있는 funcion call
            Log.d("Location", "getLocation 실행")
            getLocation()
        }

    }

    fun getLocation() {
        var currentLatLng: Location? = null
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }
            Log.d("Location", "gps시작전..")
            currentLatLng = locationMgr?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (currentLatLng != null) {
                Log.d("Location", "gps인..")
                locationMgr?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0.0f, this)
                Log.d("Location", "gps업데이트.....")
                latitude = currentLatLng.latitude
                longitude = currentLatLng.longitude
                Log.d("Location", "$currentLatLng 현재 내 위치 값: ${latitude}, ${longitude}")
                printToast("gps데이터")
            } else {
                currentLatLng = locationMgr?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (currentLatLng != null) {
                    Log.d("Location", "network인..")
                    locationMgr?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0.0f, this)
                    latitude = currentLatLng.latitude
                    longitude = currentLatLng.longitude
                    Log.d("Location", "$currentLatLng 현재 내 위치 값: ${latitude}, ${longitude}")
                    printToast("net데이터")
                } else {
                    currentLatLng = locationMgr?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    if (currentLatLng != null) {
                        Log.d("Location", "passive인..")
                        locationMgr?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 3000, 0.0f, this)
                        latitude = currentLatLng.latitude
                        longitude = currentLatLng.longitude
                        Log.d("Location", "$currentLatLng 현재 내 위치 값: ${latitude}, ${longitude}")
                        printToast("passive데이터")
                    }
                }
            }
        Log.d("Location", "geocoder시작..")
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
            printToast("${mResultList[0].getAddressLine(0)}")
        }
    }

    fun getProvider():String?{
        //provider목록을 저장할 변수를 변수로 선언하고 사용가능한 provider 변수에 저장
        Log.d("Location","겟프로바이더 진입")
        var providerData : String ? = null
        try {
            if (LocationManager.GPS_PROVIDER != null) {
                providerData = LocationManager.GPS_PROVIDER
            } else if (LocationManager.NETWORK_PROVIDER != null) {
                providerData = LocationManager.NETWORK_PROVIDER

            } else if (LocationManager.PASSIVE_PROVIDER != null){
                providerData = LocationManager.NETWORK_PROVIDER
            }
        }catch (e:SecurityException){
            Log.d("msg",e.message+"")
        }
        return providerData
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1000 && grantResults.size > 0) { //권한 처리 결과를 확인하고 요청한 요청 코드가 맞으면 작
            var check_result = true
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
    private fun printToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location) {
        Log.d("Location","체인지 진입")
        latitude = location.latitude
        longitude = location.longitude
        Log.d("Location", "$location 체인지 내 위치 값: ${latitude}, ${longitude}")
        printToast("$location 체인지 내 위치 값: ${latitude}, ${longitude}")
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
            printToast("${mResultList[0].getAddressLine(0)}")
        }
    }


}
