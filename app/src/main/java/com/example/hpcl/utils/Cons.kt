package com.example.hpcl.utils

import android.os.Handler
import android.os.Looper
import com.example.hpcl.user_action.data.RefreshToken

class Cons {
    companion object {
       // var BASE_URL = "http://164.52.223.163:4562/api/"
         //var BASE_URL= "http://10.5.50.150:4558"
        var BASE_URL = "http://164.52.223.163:4562"
        const val NO_ACTION = -1
        const val Token = "token"
        const val ACCESSTOKEN = "access_token"
        const val REFRESHTOKEN = "refresh_token"
        const val USER_ROLE = "user_role"
        const val TOKEN_EXPIRE_TIME = "token_expire_time"
        private const val DEFAULT_BUFFER_SIZE = 4096
        private val handler = Handler(Looper.getMainLooper())
    }
}