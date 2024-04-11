package com.example.hpcl.localdatabase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.hpcl.VehicleHistoryModelClass
import com.example.hpcl.VehicleInTimeDataModel
import com.example.hpcl.acitity.data.RegisterModel
import com.example.hpcl.acitity.data.UpdateVehicleItem
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.GetDepartmentName
import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.utils.DataAdditionListener

@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(addVehicleDetails: RegisteredDataItem)

    @Insert (onConflict = OnConflictStrategy.IGNORE)
     suspend fun addVehicleInTime(vehicleInTimeDataModel: VehicleHistoryModelClass)

   @Query("SELECT * FROM History_table ORDER BY id ASC")
    fun getAllVehicleInTime():LiveData<List<VehicleHistoryModelClass>>

    @Query("DELETE FROM History_table")
    suspend fun deleteVehicleIntime()

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun vehicleHistoryInsert(vehicleHistoryModelClass: VehicleHistoryModelClass)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(addVehicleDetails: List<RegisteredDataItem>)

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    fun insertData(data: List<RegisteredDataItem>, callback: DataAdditionListener)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addDepartment(getDepartment: GetDepartmentName)

    @Query("DELETE FROM department_table")
    suspend fun deleteDepartment()





    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registeredVehicle(addVehicleDetails: RegisterModel)

    @Query("SELECT * FROM offline_registration_table ORDER BY id ASC")
    fun readAllResigeteredVehicle(): LiveData<List<RegisterModel>>

    @Query("SELECT * FROM offline_update_table WHERE rfidTag = :rfidTag")
    fun getFromOfflineRegistrationTable(rfidTag: String):UpdateVehicleItem



    @Update(onConflict = OnConflictStrategy.REPLACE)
   suspend fun updateMainTable(registerModel:RegisteredDataItem)

    @Query("SELECT * FROM department_table ORDER BY id ASC")
    fun readAllDepartmentName(): LiveData<List<GetDepartmentName>>


    @Query("DELETE FROM  offline_registration_table ")
    fun deleteRegisteredVehicle()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUpdateData(addVehicleDetails: UpdateVehicleItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRegistration(registrationModel: RegisterModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun tempAdd(number: TempList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun userImage(userImage:UserImage)



    @Query("SELECT * FROM user_image WHERE rfidTag = :rfidTag")
    fun getUserImage(rfidTag: String): UserImage

    @Query("SELECT * FROM user_image WHERE ID = :ID")
    fun getUserImageByInt(ID: Int): UserImage

    @Update(onConflict = OnConflictStrategy.REPLACE)
     fun updateVehicleImage(number: TempList)



    @Query("SELECT * FROM temoList_table ORDER BY id ASC")
    fun readAllTempList(): LiveData<List<TempList>>

    @Query("SELECT * FROM temoList_table WHERE rfidTag = :rfidTag")
    fun getAllVehicleImage(rfidTag: String): TempList

    @Query("SELECT * FROM temoList_table WHERE ID = :ID")
    fun getAllVehicleImageByID(ID: Int): TempList


    // Add the @Query for counting data
    @Query("SELECT COUNT(*) FROM temoList_table")
    suspend fun getCountOfVehicleImage(): Int

    // Get the count of rows in the database table
    @Query("SELECT COUNT(*) FROM temoList_table")
    suspend fun getRowCount(): Int





    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun manageVehicleAudit(manageVehicleAudit: ManageVehicleAudit)


    @Query("DELETE FROM ManageVehicleAudit_Table")
    suspend fun deleteManageAudit()




    @Query("DELETE FROM managevehicleaudit_table")
    suspend fun deleteManageVehicleAudit()


    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateUserImageTable(entity: UserImage)

    @Query("SELECT * FROM user_image WHERE vehicleNumber = :vehicleNumber")
    fun getDataFromUserImage(vehicleNumber: String): UserImage



    @Query("SELECT * FROM registration_table WHERE rfidTag = :rfidTag")
     fun getVehicleDetails(rfidTag: String): RegisteredDataItem

    @Query("SELECT * FROM registration_table WHERE vehicleNumber = :vehicleNumber")
    fun getVehicleDetailsByNumber(vehicleNumber: String): RegisteredDataItem

    @Query("DELETE FROM registration_table")
    suspend fun deleteMainTable()

    @Query("SELECT * FROM ManageVehicleAudit_Table ORDER BY id ASC")
    fun readAllManageVehicleAuditData(): LiveData<List<ManageVehicleAudit>>


//    @Query("SELECT * FROM History_table ORDER BY id ASC")
//    fun getAllVehicleHistory(): LiveData<List<VehicleHistoryModelClass>>

    @Query("SELECT * FROM offline_update_table ORDER BY id ASC")
    fun readAllUpdateItem(): LiveData<List<UpdateVehicleItem>>

    @Query("DELETE FROM  offline_update_table ")
    fun deleteUpdateItem()







}