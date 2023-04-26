package com.example.hpcl.identification.data


import com.google.gson.annotations.SerializedName

class VehicleIdentification : ArrayList<VehicleIdentification.VehicleIdentificationItem>(){
    data class VehicleIdentificationItem(
        @SerializedName("createdBy")
        val createdBy: String,
        @SerializedName("createdDate")
        val createdDate: String,
        @SerializedName("departMentId")
        val departMentId: Int,
        @SerializedName("department")
        val department: String,
        @SerializedName("drivingLicenceNo")
        val drivingLicenceNo: String,
        @SerializedName("empName")
        val empName: String,
        @SerializedName("empid")
        val empid: Int,
        @SerializedName("image")
        val image: String,
        @SerializedName("isActive")
        val isActive: Int,
        @SerializedName("registrationNo")
        val registrationNo: String,
        @SerializedName("rfidTag")
        val rfidTag: String,
        @SerializedName("validity")
        val validity: String,
        @SerializedName("vehicleColor")
        val vehicleColor: String,
        @SerializedName("vehicleImage")
        val vehicleImage: String,
        @SerializedName("vehicleName")
        val vehicleName: String,
        @SerializedName("vehicleNumber")
        val vehicleNumber: String
    )
}