package com.example.hpcl.user_action.data


import com.google.gson.annotations.SerializedName

class GetUserType : ArrayList<GetUserType.GetUserTypeItem>(){
    data class GetUserTypeItem(
        @SerializedName("id")
        val id: Int,
        @SerializedName("userType")
        val userType: String
    )
}