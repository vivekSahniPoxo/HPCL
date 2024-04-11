package com.example.hpcl.localdatabase.dbRepos

import androidx.lifecycle.LiveData
import com.example.hpcl.VehicleHistoryModelClass
import com.example.hpcl.VehicleInTimeDataModel
import com.example.hpcl.acitity.data.RegisterModel
import com.example.hpcl.acitity.data.UpdateVehicleItem
import com.example.hpcl.identification.data.ManageVehicleAudit

import com.example.hpcl.localdatabase.RegisteredDataItem
import com.example.hpcl.localdatabase.TempList
import com.example.hpcl.localdatabase.UserImage
import com.example.hpcl.localdatabase.VehicleDao
import com.example.hpcl.registration.GetDepartmentName

import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.utils.DataAdditionListener
import com.example.hpcl.utils.NetworkResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class VehicleRepository(private val vehicleDao: VehicleDao)  {

    val getALlVehicleInHistory: LiveData<List<VehicleHistoryModelClass>> = vehicleDao.getAllVehicleInTime()

    val readAllManageVehicleAuditData: LiveData<List<ManageVehicleAudit>> = vehicleDao.readAllManageVehicleAuditData()

    val readAllUpdateItem: LiveData<List<UpdateVehicleItem>> = vehicleDao.readAllUpdateItem()


    val readAllVehicleRegisterationItem: LiveData<List<RegisterModel>> = vehicleDao.readAllResigeteredVehicle()


    val readAllDepartmentName: LiveData<List<GetDepartmentName>> = vehicleDao.readAllDepartmentName()

  val readAllTempList:LiveData<List<TempList>>  = vehicleDao.readAllTempList()

    suspend fun updateMainTable(registerModel: RegisteredDataItem){
        vehicleDao.updateMainTable(registerModel)
    }

    suspend fun deleteRegisteredVehicle(){
        vehicleDao.deleteRegisteredVehicle()
    }

    suspend fun deleteManageVehicleAudit(){
        vehicleDao.deleteManageVehicleAudit()
    }

    suspend fun deleteUpdateItem(){
        vehicleDao.deleteUpdateItem()
    }

    suspend fun deleteMainTable(){
        vehicleDao.deleteMainTable()
    }

    suspend fun addVehicle(addRegisteredVehicle: RegisteredDataItem){
        vehicleDao.insert(addRegisteredVehicle)
    }


    suspend fun addVehicleInTime(vehicleInTimeDataModel: VehicleHistoryModelClass){
        vehicleDao.addVehicleInTime(vehicleInTimeDataModel)
    }

//    suspend fun vehicleHistoryInsert(vehicleHistoryInsert:VehicleHistoryModelClass){
//        vehicleDao.vehicleHistoryInsert(vehicleHistoryInsert)
//    }

    suspend fun addVehicles(addRegisteredVehicles: List<RegisteredDataItem>) {
        vehicleDao.insertAll(addRegisteredVehicles)
    }

//    suspend fun insertionCalculation(data: List<RegisteredDataItem>, callback: DataAdditionListener){
//        vehicleDao.insertData(data,callback)
//    }

    suspend fun registeredVehicle(addRegisteredVehicle: RegisterModel){
        vehicleDao.registeredVehicle(addRegisteredVehicle)
    }

    suspend fun addDepartment(getDepartmentName: GetDepartmentName){
        vehicleDao.addDepartment(getDepartmentName)
    }


    suspend fun deletDepartment(){
        vehicleDao.deleteDepartment()
    }

    suspend fun deleteInTime(){
        vehicleDao.deleteVehicleIntime()
    }

    suspend fun updateVehicleItem(updateVehicleItem: UpdateVehicleItem){
        vehicleDao.addUpdateData(updateVehicleItem)
    }

    suspend fun updateRegistration(registrationModel: RegisterModel){
        vehicleDao. updateRegistration(registrationModel)
    }

    suspend fun updateVehicle(tempList: TempList){
        vehicleDao.updateVehicleImage(tempList)
    }

    suspend fun tempAdd(number: TempList){
        vehicleDao.tempAdd(number)
    }

    suspend fun userImage(userImage: UserImage){
        vehicleDao.userImage(userImage)
    }

    suspend  fun manageVehicleAudit(manageVehicleAudit: ManageVehicleAudit){
        vehicleDao.manageVehicleAudit(manageVehicleAudit)
    }



    suspend fun deleteManageAudit(){
        vehicleDao.deleteManageAudit()
    }

//    fun manageVehicleAudit(manageVehicleAudit: ManageVehicleAudit) = flow{
//        emit(NetworkResult.Loading())
//        val response = vehicleDao.manageVehicleAudit(manageVehicleAudit)
//        emit(NetworkResult.Success(response))
//    }.catch { e->
//        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
//    }


     fun getVehicleDetails(rfidTagNo: String) = flow{
        emit(NetworkResult.Loading())
        val response = vehicleDao.getVehicleDetails(rfidTagNo)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
    }


    fun getVehicleDetailsByNumber(vehicleNumber: String) = flow{
        emit(NetworkResult.Loading())
        val response = vehicleDao.getVehicleDetailsByNumber(vehicleNumber)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
    }

    fun getDataFromUserImageTable(vehicleNumber: String) = flow{
        emit(NetworkResult.Loading())
        val response = vehicleDao.getDataFromUserImage(vehicleNumber)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
    }

    fun getVehicleImage(vehicleImage: String) = flow{
        emit(NetworkResult.Loading())
        val response = vehicleDao.getAllVehicleImage(vehicleImage)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
    }


    fun getUserImage(userImage: String) = flow{
        emit(NetworkResult.Loading())
        val response = vehicleDao.getUserImage(userImage)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
    }

    suspend fun getCountOfInsertedData(): Int {
        return vehicleDao.getCountOfVehicleImage()
    }

}