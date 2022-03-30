package com.example.pluginapplication

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_first.*

class SecondActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        postMessage();
    }

    private fun postMessage(){
        Handler().post {
            Thread.sleep(1000)
//            postMessage()
        }
    }


}