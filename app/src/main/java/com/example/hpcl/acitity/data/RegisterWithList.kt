package com.example.hpcl.acitity.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "registration_tableList", indices = [Index(value = ["rfidTag"], unique = true)])
data class RegisteredDataItem(
    @PrimaryKey(autoGenerate = true) val Id: Int?,
    @SerializedName("authorizedBy") val authorizedBy: Int,
    @SerializedName("createdBy") val createdBy: String,
    @SerializedName("createdDate") val createdDate: String,
    @SerializedName("departMentId") val departMentId: Int,
    @SerializedName("department") val department: String,
    @SerializedName("drivingLicenceNo") val drivingLicenceNo: String,
    @SerializedName("empName") val empName: String,
    @SerializedName("empid") val empid: Int,
    @SerializedName("image") val image: String,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("registrationNo") val registrationNo: String,
    @SerializedName("rfidTag") val rfidTag: String,
    @SerializedName("validity") val validity: String,
    @SerializedName("vehicleColor") val vehicleColor: String,
    @SerializedName("vehicleImage") val vehicleImage: String,
    @SerializedName("vehicleName") val vehicleName: String,
    @SerializedName("vehicleNumber") val vehicleNumber: String
)

