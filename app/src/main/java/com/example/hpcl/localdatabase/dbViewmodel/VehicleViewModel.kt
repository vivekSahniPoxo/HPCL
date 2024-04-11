package com.example.hpcl.localdatabase.dbViewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hpcl.VehicleHistoryModelClass
import com.example.hpcl.VehicleInTimeDataModel
import com.example.hpcl.acitity.data.RegisterModel

import com.example.hpcl.acitity.data.UpdateVehicleItem
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.localdatabase.*

import com.example.hpcl.localdatabase.dbRepos.VehicleRepository
import com.example.hpcl.registration.GetDepartmentName
import com.example.hpcl.utils.NetworkResult
import com.example.hpcl.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehicleViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<ManageVehicleAudit>>
    val readAllUpdateItem: LiveData<List<UpdateVehicleItem>>
    val readAllVehicleRegisteredItem:LiveData<List<RegisterModel>>
    val readDertmentName:LiveData<List<GetDepartmentName>>
    val readTempList:LiveData<List<TempList>>
    val getAllVehicleHistory:LiveData<List<VehicleHistoryModelClass>>
    private var repository: VehicleRepository
    init {
        val userDao = VehicleDatabase.getDatabase(application).VehicleDao()
        repository = VehicleRepository(userDao)
        readAllData = repository.readAllManageVehicleAuditData
        readAllUpdateItem  =repository.readAllUpdateItem
        readAllVehicleRegisteredItem = repository.readAllVehicleRegisterationItem
        readDertmentName = repository.readAllDepartmentName
        readTempList = repository.readAllTempList
        getAllVehicleHistory = repository.getALlVehicleInHistory
//        readAllRegisteredData = repository.readAllRegisteredData
    }


    private val _dataInsertionProgress = MutableLiveData<Boolean>()
    val dataInsertionProgress: LiveData<Boolean>
        get() = _dataInsertionProgress




    fun updateMainTable(registerModel: RegisteredDataItem){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMainTable(registerModel)
        }
    }

    fun addVehicle(registeredVehicle: RegisteredDataItem){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addVehicle(registeredVehicle)
        }
    }

    fun addVehicleInTime(vehicleInTimeDataModel: VehicleHistoryModelClass){
        viewModelScope.launch {
            repository.addVehicleInTime(vehicleInTimeDataModel)
        }
    }


//    fun vehicleHistoryInsert(vehicleHistoryInsert:VehicleHistoryModelClass){
//        viewModelScope.launch {
//            repository.vehicleHistoryInsert(vehicleHistoryInsert)
//        }
//    }

    fun addVehicleAll(registeredVehicle: List<RegisteredDataItem>){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addVehicles(registeredVehicle)
        }
    }

//    fun insertionCalculation(data: List<RegisteredDataItem>, callback: DataAdditionListener){
//        viewModelScope.launch(Dispatchers.Main) {
//            repository.insertionCalculation(data,callback)
//        }
//    }

    fun updateVehicleItem(updateVehicleItem: UpdateVehicleItem){
        viewModelScope.launch(Dispatchers.Main) {
            repository.updateVehicleItem(updateVehicleItem)
        }
    }

    fun registerVehicle(registeredVehicle: RegisterModel){
        viewModelScope.launch(Dispatchers.Main) {
            repository.registeredVehicle(registeredVehicle)
        }
    }

    fun addDepartment(getDepartmentName: GetDepartmentName){
        viewModelScope.launch(Dispatchers.Main) {
            repository.addDepartment(getDepartmentName)
        }
    }

    fun updateRegistration(registrationModel: RegisterModel){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRegistration(registrationModel)
        }
    }

    fun updateVehicleImage(tempList: TempList){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVehicle(tempList)
        }
    }

    fun tempList(number:TempList){
        viewModelScope.launch(Dispatchers.Main) {
            repository.tempAdd(number)
        }
    }

    fun userImage(userImage:UserImage){
        viewModelScope.launch(Dispatchers.Main) {
            repository.userImage(userImage)
        }
    }

    fun mangeVehicleAuditDB(manageVehicleAudit:ManageVehicleAudit){
        viewModelScope.launch(Dispatchers.IO) {
            repository.manageVehicleAudit(manageVehicleAudit)
        }
    }


    fun deleteManageAudit(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteManageAudit()
        }
    }


    fun deleteRegisteredVehicle(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRegisteredVehicle()
        }

    }

    fun deletDepartment(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletDepartment()
        }

    }

    fun deleteInTime(){
        viewModelScope.launch {
            repository.deleteInTime()
        }
    }

    fun deleteManageVehicleAudit(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteManageVehicleAudit()
        }

    }

    fun deleteUpdateItem(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUpdateItem()
        }

    }

    fun deleteMainTable(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMainTable()
        }
    }


//    private var _manageVehicleAuditResponseFromDb = SingleLiveEvent<NetworkResult<ManageVehicleAudit>>()
//    val manageVehicleAuditResponseFromDb:SingleLiveEvent<NetworkResult<ManageVehicleAudit>> = _manageVehicleAuditResponseFromDb
//    fun mangeVehicleAudit(manageVehicleAudit: ManageVehicleAudit) {
//        viewModelScope.launch(Dispatchers.Main) {
//            repository.manageVehicleAudit(manageVehicleAudit).collect {
//                _manageVehicleAuditResponseFromDb.postValue(it)
//            }
//        }
//    }

    private var _getResponseFromDb = SingleLiveEvent<NetworkResult<RegisteredDataItem>>()
    val getResponseFromDb:SingleLiveEvent<NetworkResult<RegisteredDataItem>> = _getResponseFromDb
    fun getVisitInfo(rfidTagNo:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getVehicleDetails(rfidTagNo).collect{
                _getResponseFromDb.postValue(it)
            }
        }
    }

    private var _getVehicleImage = SingleLiveEvent<NetworkResult<TempList>>()
    val getVehicleImage:SingleLiveEvent<NetworkResult<TempList>> = _getVehicleImage
    fun getVehicleImage(rfidTagNo:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getVehicleImage(rfidTagNo).collect{
                _getVehicleImage.postValue(it)
            }
        }
    }


    private var _getUserImage = SingleLiveEvent<NetworkResult<UserImage>>()
    val getUserImage:SingleLiveEvent<NetworkResult<UserImage>> = _getUserImage
    fun getUserImage(rfidTagNo:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getUserImage(rfidTagNo).collect{
                _getUserImage.postValue(it)
            }
        }
    }



    private var _getItemByVehicleNumber = SingleLiveEvent<NetworkResult<RegisteredDataItem>>()
    val getItemByVehicleNumber:SingleLiveEvent<NetworkResult<RegisteredDataItem>> = _getItemByVehicleNumber
    fun getVisitInfoByVehicleNumber(vehicleNumber:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getVehicleDetailsByNumber(vehicleNumber).collect{
                _getItemByVehicleNumber.postValue(it)
            }
        }
    }


    private var _getDataFromUserImage = SingleLiveEvent<NetworkResult<UserImage>>()
    val getDataFromUserImage:SingleLiveEvent<NetworkResult<UserImage>> = _getDataFromUserImage
    fun getDataFromUserImage(vehicleNumber:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getDataFromUserImageTable(vehicleNumber).collect{
                _getDataFromUserImage.postValue(it)
            }
        }
    }


    private val _countLiveData = MutableLiveData<Int>()
    val countLiveData: LiveData<Int> = _countLiveData

    fun getCountOfInsertedData() {
        viewModelScope.launch {
            val count = repository.getCountOfInsertedData()
            _countLiveData.postValue(count)
        }
    }

}