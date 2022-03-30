package com.example.pluginapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : Activity() {

    val TAG = "finish"

    var startTime:Long = 0L
    var finishTime:Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        jump_btn.setOnClickListener{
            startActivity(Intent(this,SecondActivity::class.java))
            startTime = System.currentTimeMillis()
        }
    }

    override fun finish() {
        super.finish()
        finishTime = System.currentTimeMillis();
        Log.e(TAG,"finish()-startTime == "+ (System.currentTimeMillis() - startTime))
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG,"onPause()-startTime = "+ (System.currentTimeMillis() - startTime))
        if(finishTime != 0L) {
            Log.e(TAG, "onPause()-finishTime  = " + (System.currentTimeMillis() - finishTime))
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"onStop()-startTime = "+ (System.currentTimeMillis() - startTime))
        if(finishTime != 0L) {
            Log.e(TAG, "onStop()-finishTime  = " + (System.currentTimeMillis() - finishTime))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"onDestroy()-startTime = "+ (System.currentTimeMillis() - startTime))
        if(finishTime != 0L) {
            Log.e(TAG, "onDestroy()-finishTime  = " + (System.currentTimeMillis() - finishTime))
        }
    }
}