package com.example.hpcl.user_action.data


import com.google.gson.annotations.SerializedName

data class RefreshToken(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)