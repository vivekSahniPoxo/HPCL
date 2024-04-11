package com.example.hpcl.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import com.example.hpcl.user_action.LoginActivity


@SuppressLint("HandlerLeak")
class MyBaseActivity : Activity() {
    private val disconnectHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {

        }
    }
    private val disconnectCallback = Runnable {

        // Perform any required operation on disconnect

        // Logout from app
        val int = Intent(this,LoginActivity::class.java)
        startActivity(int)


    }

    fun resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback)
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT)
    }

    fun stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback)
    }

    override fun onUserInteraction() {
        resetDisconnectTimer()
    }

     override fun onResume() {
        super.onResume()
        resetDisconnectTimer()
    }

     override fun onStop() {
        super.onStop()
        stopDisconnectTimer()
    }

    companion object {
        const val DISCONNECT_TIMEOUT: Long = 900000 // 15 min = 15 * 60 * 1000 ms
    }
}