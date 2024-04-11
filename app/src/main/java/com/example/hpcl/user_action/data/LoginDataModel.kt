package com.example.hpcl.user_action.data


import com.google.gson.annotations.SerializedName

data class LoginDataModel(
    @SerializedName("password")
    val password: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("userTypeId")
    val userTypeId: Int
)