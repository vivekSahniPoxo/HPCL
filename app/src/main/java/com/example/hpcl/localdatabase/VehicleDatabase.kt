package com.example.hpcl.localdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hpcl.VehicleHistoryModelClass
import com.example.hpcl.acitity.data.RegisterModel
import com.example.hpcl.acitity.data.UpdateVehicleItem
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.registration.GetDepartmentName

import com.example.hpcl.registration.RegistrationDataModel


@Database(entities = [RegisteredDataItem::class, ManageVehicleAudit::class,UpdateVehicleItem::class, RegisterModel::class,TempList::class,UserImage::class,GetDepartmentName::class,VehicleHistoryModelClass::class], version = 12, exportSchema = false)
abstract class VehicleDatabase : RoomDatabase() {

    abstract fun VehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: VehicleDatabase? = null

        fun getDatabase(context: Context): VehicleDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VehicleDatabase::class.java,
                    "vehicle_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }


}