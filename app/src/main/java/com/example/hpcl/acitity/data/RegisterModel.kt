package com.example.hpcl.acitity.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "offline_registration_table",indices = [Index(value = ["rfidTag"], unique = true)])
data class RegisterModel(@PrimaryKey(autoGenerate = true)
                                 val Id:Int,
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