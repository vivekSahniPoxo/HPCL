package com.example.hpcl.identification.data


import com.google.gson.annotations.SerializedName

data class ManageVehicleAudit(
    @SerializedName("createdby")
    val createdby: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("rfidTag")
    val rfidTag: String,
    @SerializedName("status")
    val status: Int
)