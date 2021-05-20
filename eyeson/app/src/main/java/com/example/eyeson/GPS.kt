//package com.example.eyeson
//
//import android.content.Context
//import android.location.Location
//import android.location.LocationManager
//import android.util.Log
//import androidx.core.content.ContextCompat.getSystemService
//
//
//class GPS {
//    fun getLocation(){
//        var locatioNManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
//        var userLocation: Location = getLatLng()
//        if(userLocation != null){
//            latitude = userLocation.latitude
//            longitude = userLocation.longitude
//            Log.d("CheckCurrentLocation", "현재 내 위치 값: ${latitude}, ${longitude}")
//
//            var mGeoCoder =  Geocoder(applicationContext, Locale.KOREAN)
//            var mResultList: List<Address>? = null
//            try{
//                mResultList = mGeoCoder.getFromLocation(
//                    latitude!!, longitude!!, 1
//                )
//            }catch(e: IOException){
//                e.printStackTrace()
//            }
//            if(mResultList != null){
//                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
//            }
//        }
//    }
//
//    fun getLatLng(): Location{
//        var currentLatLng: Location? = null
//        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.ACCESS_FINE_LOCATION)
//        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
//            Manifest.permission.ACCESS_COARSE_LOCATION)
//
//        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
//            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
//            val locatioNProvider = LocationManager.GPS_PROVIDER
//            currentLatLng = locatioNManager?.getLastKnownLocation(locatioNProvider)
//        }else{
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
//                Toast.makeText(this, "앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
//                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
//            }else{
//                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
//            }
//            currentLatLng = getLatLng()
//        }
//        return currentLatLng!!
//    }
//}