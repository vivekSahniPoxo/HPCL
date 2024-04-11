package com.example.hpcl.utils

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

class CustomHandler(looper: Looper): Handler(looper) {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        Log.d(TAG, "Inside handle message " + msg.what)
    }
}