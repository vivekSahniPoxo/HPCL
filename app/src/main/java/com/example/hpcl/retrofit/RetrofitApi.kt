package com.example.hpcl.retrofit

import com.example.hpcl.ManageAuditTempList
import com.example.hpcl.VehicleInTimeDataModel
import com.example.hpcl.acitity.data.RegisterModel
import com.example.hpcl.acitity.data.UpdateModel
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.identification.data.VehicleIdentification
import com.example.hpcl.localdatabase.GetRegisteredData
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.user_action.data.GetUserType
import com.example.hpcl.user_action.data.LoginDataModel
import com.example.hpcl.user_action.data.LoginResponsedataModel
import com.example.hpcl.user_action.data.RefreshToken
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
    fun manageVehicleAuditWithoutList(@Body manageVehicleAudit: ManageVehicleAudit):Call<String>

    @POST("/api/manage-vehicle-audit")
    fun manageVehicleAudit(@Body manageVehicleAudit: ManageAuditTempList):Call<String>

    @GET("/api/usertype")
    fun getUserType():Call<GetUserType>

    @POST("/api/login")
    fun userLogin(@Body loginDataModel: LoginDataModel):Call<LoginResponsedataModel>

    @POST("/api/refresh-token")
    fun refreshToken(@Body refreshToken: RefreshToken):Call<RefreshToken>

    @GET("/api/search-vehicles-by-vehicleno")
    fun getVehicleInfo(@Query("vehicleno")vehicleno:String):Call<UpdateModel>

    @POST("/api/update-register-vehicles")
    fun updateVehicleInfo(@Body registrationDataModel: RegistrationDataModel):Call<String>

    @GET("/api/get-registers-vehicles")
    fun getAllRegisteredData():Call<GetRegisteredData>

    @GET("/api/get-registerd-vehicles")
    fun getAllRegisterDataByPagingApi(@Query("PageNumber")PageNumber:Int,@Query("PageSize")PageSize:Int):Call<GetRegisteredData>

    @POST("/api/create-vehicle-transactions")
    fun vehicleInTime(@Body vehicleInTimeDataModel:ArrayList<VehicleInTimeDataModel>):Call<String>

    @GET("api/get-latest-registers-vehicles")
    fun getLatestData(@Query("Days")Days:Int):Call<GetRegisteredData>



}