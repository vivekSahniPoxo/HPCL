package com.example.hpcl.identification.data


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ManageVehicleAudit_Table",indices = [Index(value = ["rfidTag"], unique = true)])
data class ManageVehicleAudit(@PrimaryKey(autoGenerate = true)
     val id:Int,
    @SerializedName("createdby")
    val createdby: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("rfidTag")
    val rfidTag: String,
    @SerializedName("status")
    val status: Int
)