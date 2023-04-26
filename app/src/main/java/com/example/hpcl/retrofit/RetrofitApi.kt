package com.example.hpcl.retrofit

import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.identification.data.VehicleIdentification
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.RegistrationDataModel
import com.google.gson.JsonObject

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RetrofitApi {

    @POST("/api/register-vehicles")
    fun vehicleRegistration(@Body registrationDataModel: RegistrationDataModel): Call<String>

    @GET("/api/search-registerd-vehicles")
    fun getVehicleDetails(@Query("rfidTag") rfidTag: String):Call<VehicleIdentification>

    @GET("/api/get-department")
    fun getDepartment():Call<GetDepartment>

    @POST("/api/update-register-vehicles")
    fun postRequest(@Body registrationDataModel: RegistrationDataModel):Call<String>

    @POST("/api/manage-vehicle-audit")
    fun manageVehicleAudit(@Body manageVehicleAudit: ManageVehicleAudit):Call<String>

}