package com.example.hpcl

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "History_table",indices = [Index(value = ["rfidTag"], unique = true)])
data class VehicleHistoryModelClass(@PrimaryKey(autoGenerate = true) val Id:Int,
                                    val rfidTag:String,val vehicleIntime:String)