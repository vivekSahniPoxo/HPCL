package com.example.hpcl.localdatabase

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "temoList_table",indices = [Index(value = ["rfidTag"], unique = true)])
data class TempList(@PrimaryKey(autoGenerate = true,)
                    val ID: Int,
                    var rfidTag:String,
                    var vehicleImage: String,
                    var vehicleNumber:String,
                    var userName:String)

@Entity(tableName = "user_image",indices = [Index(value = ["rfidTag"], unique = true)])
data class UserImage(@PrimaryKey(autoGenerate = true,)
                    val ID: Int,
                    var rfidTag:String,
                    var userImage: String,
                    var userName:String,
                     var vehicleNumber:String,
                     var departmentName:String,
                     var createdBy:String,
                     var dl:String,
                     var vehicleName:String,
                     var vehicleColor:String,
                      var RNo:String,
                      var Active:Int,
                       var validity:String)
