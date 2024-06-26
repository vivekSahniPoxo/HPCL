package com.example.hpcl.registration


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

//@Entity(tableName = "registration_table",indices = [Index(value = ["rfidTag"], unique = true)])
data class RegistrationDataModel(
    @SerializedName("departMentId")
    val departMentId: Int,
    @SerializedName("empName")
    val empName: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("isActive")
    val isActive: Int,
    @SerializedName("rfidTag")
    val rfidTag: String,
    @SerializedName("validity")
    val validity: String,
    @SerializedName("vehicleImage")
    val vehicleImage: String,
    @SerializedName("vehicleNumber")
    val vehicleNumber: String,
    @SerializedName("registrationNo")
    val registrationNo:String,
    @SerializedName("vehicleColor")
    val vehicleColor:String,
    @SerializedName("vehicleName")
    val vehicleName:String,
    @SerializedName("drivingLicenceNo")
    val drivingLicenceNo:String,
    @SerializedName("createdBy")
    val createdBy:String
)