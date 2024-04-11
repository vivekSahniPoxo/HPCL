package com.example.hpcl.localdatabase


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName



@Entity(tableName = "registration_table",indices = [Index(value = ["rfidTag"], unique = true)])
data class RegisteredDataItem(@PrimaryKey(autoGenerate = true)
                              val Id:Int?,
    @SerializedName("authorizedBy")
    var authorizedBy: Int,
    @SerializedName("createdBy")
    var createdBy: String,
    @SerializedName("createdDate")
    var createdDate: String,
    @SerializedName("departMentId")
    var departMentId: Int,
    @SerializedName("department")
    var department: String,
    @SerializedName("drivingLicenceNo")
    var drivingLicenceNo: String,
    @SerializedName("empName")
    var empName: String,
    @SerializedName("empid")
    var empid: Int,
    @SerializedName("image")
    var image: String,
    @SerializedName("isActive")
    var isActive: Int,
    @SerializedName("registrationNo")
    var registrationNo: String,
    @SerializedName("rfidTag")
    var rfidTag: String,
    @SerializedName("validity")
    var validity: String,
    @SerializedName("vehicleColor")
    var vehicleColor: String,
    @SerializedName("vehicleImage")
    var vehicleImage: String,
    @SerializedName("vehicleName")
    var vehicleName: String,
    @SerializedName("vehicleNumber")
    var vehicleNumber: String
)







class GetRegisteredData : ArrayList<GetRegisteredData.GetRegisteredDataItem>(){
    data class GetRegisteredDataItem(
        @SerializedName("authorizedBy")
        var authorizedBy: Int,
        @SerializedName("createdBy")
        var createdBy: String,
        @SerializedName("createdDate")
        var createdDate: String,
        @SerializedName("departMentId")
        var departMentId: Int,
        @SerializedName("department")
        var department: String,
        @SerializedName("drivingLicenceNo")
        var drivingLicenceNo: String,
        @SerializedName("empName")
        var empName: String,
        @SerializedName("empid")
        var empid: Int,
        @SerializedName("image")
        var image: String,
        @SerializedName("isActive")
        var isActive: Int,
        @SerializedName("registrationNo")
        var registrationNo: String,
        @SerializedName("rfidTag")
        var rfidTag: String,
        @SerializedName("validity")
        var validity: String,
        @SerializedName("vehicleColor")
        var vehicleColor: String,
        @SerializedName("vehicleImage")
        var vehicleImage: String,
        @SerializedName("vehicleName")
        var vehicleName: String,
        @SerializedName("vehicleNumber")
        var vehicleNumber: String
    )
}