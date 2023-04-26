package com.example.hpcl.registration

import com.google.gson.annotations.SerializedName

data class GetDepartmentItem(
    @SerializedName("departmentName")
    val departmentName: String,
    @SerializedName("id")
    val id: Int
)