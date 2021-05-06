package com.example.eyeson

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var objIntent = Intent(this, BusActivity::class.java)
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(objIntent)
        }, 2000)
    }
}