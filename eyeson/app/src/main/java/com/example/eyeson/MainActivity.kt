package com.example.eyeson

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.eyeson.classFile.UniqueID
import com.example.eyeson.dataFile.UUID_Parcelable
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var uuid = UniqueID.getAndroidID(this)
        var objIntent = Intent(this, BusActivity::class.java)
        var obj = UUID_Parcelable()
        obj.uu_id = uuid

        objIntent.putExtra("uuidObj", obj)
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(objIntent)
        }, 2000)
    }
}