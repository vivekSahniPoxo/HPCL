package com.example.hpcl.registration


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "department_table")
data class GetDepartmentName( @PrimaryKey(autoGenerate = true)
    val idd:Int,
    @SerializedName("departmentName")
    val departmentName: String,
    @SerializedName("id")
    val id: Int
)

class GetDepartment : ArrayList<GetDepartment.GetDepartmentItem>(){
    data class GetDepartmentItem(
        @SerializedName("departmentName")
        val departmentName: String,
        @SerializedName("id")
        val id: Int
    )
}