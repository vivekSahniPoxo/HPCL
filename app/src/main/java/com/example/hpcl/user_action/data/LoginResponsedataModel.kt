package com.example.hpcl.user_action.data


import com.google.gson.annotations.SerializedName

data class LoginResponsedataModel(
    @SerializedName("expiration")
    val expiration: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("userRole")
    val userRole: String
)