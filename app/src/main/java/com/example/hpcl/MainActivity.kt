package com.example.hpcl



import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.hpcl.acitity.RegistrationActivity
import com.example.hpcl.acitity.UpdateActivity
import com.example.hpcl.databinding.ActivityMainBinding
import com.example.hpcl.identification.VehicleActivity
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.internetspeed.SpeedTester

import com.example.hpcl.localdatabase.*
import com.example.hpcl.localdatabase.dbViewmodel.VehicleViewModel
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.GetDepartmentName
import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.retrofit.PassRfid
import com.example.hpcl.retrofit.RetrofitClient
import com.example.hpcl.setting.SettingActivity
import com.example.hpcl.sharePreference.SharePref
import com.example.hpcl.utils.*
import com.google.android.material.button.MaterialButton
import com.speedata.libuhf.IUHFService
import kotlinx.android.synthetic.main.activity_vehicle.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class MainActivity: AppCompatActivity(),DataAdditionListener {
    lateinit var iuhfService: IUHFService
    lateinit var binding: ActivityMainBinding
    val updateList = arrayListOf<RegistrationDataModel>()
    var rfidNo = ""
    var passRfid: PassRfid? = null
    private var pressedTime: Long = 0

    lateinit var sharePref: SharePref
    lateinit var dialog: Dialog

    lateinit var dialogTag:Dialog
    var selectData = 0



    private lateinit var vehicleDatabase: VehicleDatabase

    private val addVehicleInfoViewModel: VehicleViewModel by viewModels()
    lateinit var submitVehicleHistory:ArrayList<ManageAuditTempList>

    lateinit var vehicleInTime:ArrayList<VehicleInTimeDataModel>

    val maxImageSize = 1024 * 1024

    var userImage = ""

    var byteArray:ByteArray?=null
    private val vehicleInfoViewModel: VehicleViewModel by viewModels()




    private lateinit var internetSpeedTest: SpeedTester
    private var isMonitoring = false

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialogTag = Dialog(this)
        dialog = Dialog(this)
        vehicleInTime = arrayListOf()

        vehicleDatabase = VehicleDatabase.getDatabase(this)




        internetSpeedTest = SpeedTester(applicationContext)

        internetSpeedTest = SpeedTester(applicationContext)


            checkInternetSpeed()







        sharePref = SharePref()
        submitVehicleHistory = arrayListOf()

        lifecycleScope.launch {
            val rowCount = vehicleDatabase.VehicleDao().getRowCount()
            binding.tvStatus.text = "Total Registered Vehicle $rowCount"
        }

        Log.d("token",sharePref.getData(Cons.ACCESSTOKEN).toString())
        binding.btnSync.setOnClickListener {
//            if (binding.tvStatus.text == "Total Registered Vehicle $0") {
//                binding.imSyncProgress.isVisible = true
//                binding.btnSync.isVisible = false
//                //getVehicleDetails()
//                getDepartment()
//                getAll()
//            } else{
                selectSyncTypeData()
           // }
           // getAllDataWithout()
         }

        binding.refresh.setOnClickListener {
            lifecycleScope.launch {
                val rowCount = vehicleDatabase.VehicleDao().getRowCount()
                binding.tvStatus.text = "Total Registered Vehicle $rowCount"
                Log.d("DataCount", "Number of rows in the table: $rowCount")


            }

        }



        fun writeToFileExternal(fileName: String, data: String) {
            try {
                val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(externalDir, fileName)

                val fileOutputStream = FileOutputStream(file, true)
                val outputStreamWriter = OutputStreamWriter(fileOutputStream)

                // Append a newline character before adding new content
                if (file.length() > 0) {
                    outputStreamWriter.append('\n')
                }

                outputStreamWriter.write(data)
                outputStreamWriter.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }







//        for(i in 0..1000) {
//            addVehicleInfoViewModel.tempList(TempList(0, i.toString()))
//        }


        binding.btnVehicleHistrory.setOnClickListener {
            binding.imSyncHistory.isVisible = true
            binding.btnVehicleHistrory.isVisible = false
            dialog.show()

            addVehicleInfoViewModel.getAllVehicleHistory.observe(this, Observer {
                it.forEach {
                    try {
                        if (it.rfidTag.isNotEmpty()){
                            vehicleInTime.add(VehicleInTimeDataModel(it.rfidTag,it.vehicleIntime))
                            Log.d("vehicleInTime", vehicleInTime.toString())


                           // val vehicleInTime = VehicleInTimeDataModel(it.rfidTag,it.vehicleIntime)


                        }
                        submitVehicleInTime(vehicleInTime)
                    } catch (e:Exception){
                        Log.d("Exception",e.toString())
                    }
                }
            })


            addVehicleInfoViewModel.readAllData.observe(this, Observer {
                it.forEach {
                    try {
                        if (it.rfidTag.isNotEmpty()){
                            submitVehicleHistory.add(ManageAuditTempList(it.createdby, it.rfidTag, it.remark, it.status))

                        val manageAuditTempList = ManageAuditTempList(it.createdby, it.rfidTag, it.remark, it.status)
                        manageVehicleAudit(manageAuditTempList)

                            }
                    } catch (e:Exception){
                      Log.d("vvvvvv",e.toString())
                    }

                }
            })

            addVehicleInfoViewModel.readAllVehicleRegisteredItem.observe(
                this@MainActivity,
                Observer {
                    it.forEach {
                        val vehicleRegistration = RegistrationDataModel(
                            it.departMentId,
                            it.empName,
                            it.image,
                            it.isActive,
                            it.rfidTag,
                            it.validity,
                            it.vehicleImage,
                            it.vehicleNumber,
                            it.registrationNo,
                            it.vehicleColor,
                            it.vehicleName,
                            it.drivingLicenceNo,
                            it.createdBy)

                        registration(vehicleRegistration)


                    }


                })

            addVehicleInfoViewModel.readAllUpdateItem.observe(this@MainActivity, Observer {

                it.forEach {
                    val updateItem = RegistrationDataModel(
                        it.departMentId, it.empName, it.image, it.isActive, it.rfidTag, it.validity, it.vehicleImage, it.vehicleNumber, it.registrationNo, it.vehicleColor, it.vehicleName, it.drivingLicenceNo,it.createdBy)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updateVehicleItem(updateItem)
                    }
                }

            })


            binding.imSyncHistory.isVisible = false
            binding.btnVehicleHistrory.isVisible = true
            dialog.dismiss()

        }

        binding.mcardView.setOnClickListener {
            val i  = Intent(this, RegistrationActivity::class.java)
            startActivity(i)
        }

        binding.mCardVisitor.setOnClickListener {
            val i  = Intent(this, VehicleActivity::class.java)
            startActivity(i)
        }

        binding.mCardSettings.setOnClickListener {
            val i  = Intent(this, SettingActivity::class.java)
            startActivity(i)
        }

        binding.mCardUpdate.setOnClickListener {
            val i  = Intent(this, UpdateActivity::class.java)
            startActivity(i)
        }


//        val fragment: Fragment = HomeFragment()
//        val fragmentManager: FragmentManager = supportFragmentManager
//        fragmentManager.beginTransaction().replace(R.id.fragment_container_view_tag, fragment)
//            .addToBackStack(null)
//            .commit()


//        Thread {
//            Handler(Looper.getMainLooper()).post {
//                iuhfService = UHFManager.getUHFService(this)
//                iuhfService.openDev()
//                iuhfService.antennaPower = 30
//                iuhfService.inventoryStart()
//                rfidNo = iuhfService.read_area(1, "2", "6", "00000000").toString()
//
//            }
//        }.start()
//
//    }


//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_F1) {
//
//
//            Toast.makeText(this,rfidNo, Toast.LENGTH_SHORT).show()
//
//            passRfid?.passRfidTag(rfidNo)
//            binding.tvRfid.text = rfidNo
//
//            return true
//        }
//        else {
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                // startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }
    }



    override fun onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()

        } else {
            Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
            //finish()
        }
        pressedTime = System.currentTimeMillis()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getData() {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getAllRegisteredData()
            .enqueue(object : Callback<GetRegisteredData> {
                @SuppressLint("SimpleDateFormat")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<GetRegisteredData>,
                    response: Response<GetRegisteredData>
                ) {
                    if (response.code() == 200) {
                        addVehicleInfoViewModel.deleteMainTable()
                        val vehicleDetails = response.body()

                        if (vehicleDetails != null) {
                            val totalImages = vehicleDetails.size
                            var imagesProcessed = 0

                            for (i in vehicleDetails) {
                                try {
                                    var vehicleImage = ""
                                    var byteArray: ByteArray? = null

                                    runBlocking {
                                        try {
                                            // Process and add vehicleImage to the local database
                                            // ... (existing code)

                                            imagesProcessed++

                                            // Update progress bar and status TextView
                                            val progress = (imagesProcessed * 100) / totalImages
                                            runOnUiThread {
                                                progressBar.progress = progress
                                                //binding.tvStatus.text = "Processing image $imagesProcessed of $totalImages"
                                            }

                                        } catch (e: Exception) {
                                            Log.d("Exception", e.toString())
                                        }
                                    }

                                    // Rest of the code for adding other data to the local database
                                    // ... (existing code)

                                } catch (e: Exception) {
                                    Log.d("InnerException", e.toString())
                                }
                            }

                            // All images are processed
                            runOnUiThread {
                                progressBar.progress = 100
                              Toast.makeText(this@MainActivity,"All images added to local database",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    binding.imSyncProgress.isVisible = false
                    binding.btnSync.isVisible = true
                }

                override fun onFailure(call: Call<GetRegisteredData>, t: Throwable) {
                    binding.imSyncProgress.isVisible = false
                    binding.btnSync.isVisible = true
                    Log.d("Error", t.localizedMessage)
                    Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
    }


    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun  getVehicleDetails() {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getAllRegisteredData()
            .enqueue(object : Callback<GetRegisteredData> {
                @SuppressLint("SimpleDateFormat")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<GetRegisteredData>,
                    response: Response<GetRegisteredData>
                ) {


                    if (response.code() == 200) {

                        var vehicleImage= ""
                        addVehicleInfoViewModel.deleteMainTable()

                        val vehicleDetails = response.body()


                        if (vehicleDetails != null) {
                            for (i in vehicleDetails){

                                try {
                                  val job =   GlobalScope.launch {
                                      try {
                                          GlobalScope.launch {
                                          val vehicleUrl = i.vehicleImage
                                          val vehicleBitmap = BitmapFactory.decodeStream(URL(vehicleUrl).openStream())
                                          val byteArrayOutputStreamVehicle = ByteArrayOutputStream()
                                          vehicleBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStreamVehicle)

                                          var byteArrayVehicle = byteArrayOutputStreamVehicle.toByteArray()
                                          //vehicleImage = Base64.encodeToString(byteArrayVehicle, Base64.DEFAULT)

                                          runOnUiThread {
                                          if (byteArrayVehicle.size > maxImageSize) {
                                              val compressionRatio = maxImageSize.toFloat() / byteArrayVehicle.size
                                              byteArrayVehicle = if (compressionRatio < 1) {
                                                  vehicleBitmap.compress(Bitmap.CompressFormat.JPEG, (compressionRatio * 100).toInt(), byteArrayOutputStreamVehicle)
                                                  byteArrayOutputStreamVehicle.toByteArray()
                                              } else {
                                                  byteArrayVehicle
                                              }
                                          }
                                              vehicleImage =  Base64.encodeToString(byteArrayVehicle, Base64.DEFAULT)
                                              addVehicleInfoViewModel.tempList(TempList(0,i.rfidTag,vehicleImage,i.vehicleNumber,i.empName))
                                          }
                                          }


                                      } catch (e:Exception){
                                          Log.d("Exception",e.toString())
                                      }


                                      try {
                                          GlobalScope.launch {
                                              val imageUrl = i.image
                                              val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                                              var byteArrayOutputStream = ByteArrayOutputStream()
                                              bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                                               byteArray = byteArrayOutputStream.toByteArray()
                                              // userImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
                                              if (byteArray?.size!! > maxImageSize) {
                                                  val compressionRatio = maxImageSize.toFloat() / byteArray!!.size
                                                  byteArray = if (compressionRatio < 1) {
                                                      bitmap.compress(Bitmap.CompressFormat.JPEG, (compressionRatio * 100).toInt(), byteArrayOutputStream)
                                                      byteArrayOutputStream.toByteArray()
                                                  } else {
                                                      byteArray
                                                  }
                                              }
                                              userImage =  Base64.encodeToString(byteArray, Base64.DEFAULT)
                                              addVehicleInfoViewModel.userImage(UserImage(0,i.rfidTag,userImage,i.empName,i.vehicleNumber,i.department,i.createdBy,i.drivingLicenceNo,i.vehicleName,i.vehicleColor,i.registrationNo,i.isActive,i.validity))
                                              //Log.d("userImage",userImage)
                                          }

                                      } catch (e:Exception){
                                          Log.d("userImage",e.toString())
                                      }

                                          try {

                                              addVehicleInfoViewModel.addVehicle(
                                                  RegisteredDataItem(
                                                      0,
                                                      i.authorizedBy,
                                                      i.createdBy,
                                                      i.createdDate,
                                                      i.departMentId,
                                                      i.department,
                                                      i.drivingLicenceNo,
                                                      i.empName,
                                                      i.empid,
                                                      Base64.encodeToString(byteArray, Base64.DEFAULT),
                                                      i.isActive,
                                                      i.registrationNo,
                                                      i.rfidTag,
                                                      i.validity,
                                                      i.vehicleColor,
                                                      vehicleImage ,
                                                      i.vehicleName,
                                                      i.vehicleNumber
                                                  )
                                              )



                                              binding.userImage.text = ""
                                              vehicleImage  = ""



                                         // Log.d("vehicleImage",vehicleImage)

                                      } catch (e:Exception){
                                          Log.d("vehicleExce",e.toString())
                                      }
                                    }






                                    if (job.isActive) {
                                        Log.d("isAllive","Background work is still in progress")
                                    } else {
                                        binding.imSyncProgress.isVisible = false
                                        binding.btnSync.isVisible = true
                                        Log.d("not Alive","Background work has completed")
                                    }


                                } catch (e: Exception) {
                                    Log.d("InnerException",e.toString())
                                }



                        }
                   }
                    }
                }


                override fun onFailure(call: Call<GetRegisteredData>, t: Throwable) {
                    binding.imSyncProgress.isVisible= false
                    binding.btnSync.isVisible = true
                    Log.d("Error",t.localizedMessage)
                    Toast.makeText(this@MainActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
                }
            })
    }





    @RequiresApi(Build.VERSION_CODES.M)
    private fun manageVehicleAudit(submitVehicleHistory:ManageAuditTempList){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        RetrofitClient.getResponseFromApi().manageVehicleAudit(submitVehicleHistory)
            .enqueue(object : Callback<String> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    dialog.dismiss()
                    binding.imSyncHistory.isVisible = false
                    binding.btnVehicleHistrory.isVisible = true
                    if (response.code()==200) {
                        addVehicleInfoViewModel.deleteManageVehicleAudit()
//                        addVehicleInfoViewModel.readAllVehicleRegisteredItem.observe(
//                            this@MainActivity,
//                            Observer {
//                                it.forEach {
//                                    val vehicleRegistration = RegistrationDataModel(
//                                        it.departMentId,
//                                        it.empName,
//                                        it.image,
//                                        it.isActive,
//                                        it.rfidTag,
//                                        it.validity,
//                                        it.vehicleImage,
//                                        it.vehicleNumber,
//                                        it.registrationNo,
//                                        it.vehicleColor,
//                                        it.vehicleName,
//                                        it.drivingLicenceNo,
//                                        it.createdBy)
//                                    registration(vehicleRegistration)
//
//
//                                }
//
//                            })
                        Toast.makeText(this@MainActivity, "Vehicle History Submitted", Toast.LENGTH_SHORT).show()



                    } else if (response.code()==400){
                        Toast.makeText(this@MainActivity, response.message(), Toast.LENGTH_SHORT).show()
                        binding.imSyncHistory.isVisible = false
                        binding.btnVehicleHistrory.isVisible = true
                    }else if (response.code()==404){
                        Toast.makeText(this@MainActivity, response.message(), Toast.LENGTH_SHORT).show()
                        binding.imSyncHistory.isVisible = false
                        binding.btnVehicleHistrory.isVisible = true
                    }

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    binding.imSyncHistory.isVisible = false
                    binding.btnVehicleHistrory.isVisible = true
                    Toast.makeText(this@MainActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                }

            })
    }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateVehicleItem(registrationItem:RegistrationDataModel){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }


        RetrofitClient.getResponseFromApi().updateVehicleInfo(registrationItem).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.code()==200) {
                    dialog.dismiss()
                    addVehicleInfoViewModel.deleteUpdateItem()
                    Toast.makeText(this@MainActivity, "Updated SuccessFully", Toast.LENGTH_SHORT).show()

                } else if (response.code()==400){
                    binding.imSyncProgress.isVisible = false
                    binding.btnSync.isVisible = true
                    Toast.makeText(this@MainActivity,response.message(), Toast.LENGTH_SHORT).show()
                }


            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                binding.imSyncProgress.isVisible = false
                binding.btnSync.isVisible = true
                dialog.dismiss()
                Toast.makeText(this@MainActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun registration(registrationItem:RegistrationDataModel){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        RetrofitClient.getResponseFromApi().vehicleRegistration(registrationItem).enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                dialog.dismiss()

                if (response.code()==200){
                    addVehicleInfoViewModel.deleteRegisteredVehicle()


//                    addVehicleInfoViewModel.readAllUpdateItem.observe(this@MainActivity, Observer {
//
//                        it.forEach {
//                            val updateItem = RegistrationDataModel(
//                                it.departMentId, it.empName, it.image, it.isActive, it.rfidTag, it.validity, it.vehicleImage, it.vehicleNumber, it.registrationNo, it.vehicleColor, it.vehicleName, it.drivingLicenceNo,it.createdBy)
//                            updateVehicleItem(updateItem)
//                        }
//
////                        val updateItem = RegistrationDataModel(
////                            it.departMentId, it.empName, it.image, it.isActive, it.rfidTag, it.validity, it.vehicleImage, it.vehicleNumber, it.registrationNo, it.vehicleColor, it.vehicleName, it.drivingLicenceNo,it.createdBy)
////                        updateVehicleItem(updateItem)
//                    })

                    Toast.makeText(this@MainActivity, "Registered Successfully", Toast.LENGTH_SHORT).show()

                }else if (response.code()==400){
                    binding.imSyncHistory.isVisible = false
                    binding.btnVehicleHistrory.isVisible = true
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity,response.message(), Toast.LENGTH_SHORT).show()
                }



            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                binding.imSyncHistory.isVisible = false
                binding.btnVehicleHistrory.isVisible = true
                dialog.dismiss()
                Toast.makeText(this@MainActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getDepartment(){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getDepartment().enqueue(object :Callback<GetDepartment> {
            override fun onResponse(call: Call<GetDepartment>, response: Response<GetDepartment>) {
                dialog.dismiss()
                if (response.code()==200) {

                    addVehicleInfoViewModel.deletDepartment()

                    response.body()?.forEach {
                       // val thread = Thread {
                        try {
                            val getDepartment = GetDepartmentName(0,it.departmentName,it.id)
                        addVehicleInfoViewModel.addDepartment(getDepartment)
                            //Toast.makeText(this@MainActivity,"Adding Department",Toast.LENGTH_SHORT).show()


                        } catch (e: Exception) {
                            Log.d("exception", e.toString())
                        }

//                        }
//
//                        thread.start()


                    }
                }
            }

            override fun onFailure(call: Call<GetDepartment>, t: Throwable) {
                Toast.makeText(this@MainActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }


    fun insertData(data: List<RegisteredDataItem>, callback: DataAdditionListener) {
        // Perform database insertion for each data item
        for ((index, item) in data.withIndex()) {
            // Insert the data item into the database

            // Calculate the progress
            val progress = ((index + 1).toFloat() / data.size * 100).toInt()

            // Update the progress callback
            callback.onProgressUpdated(progress)
        }

        // Notify the completion of data insertion
        callback.onInsertionComplete()
    }

    override fun onProgressUpdated(progress: Int) {
        runOnUiThread {
           // Toast.makeText(this,"Data is Adding",Toast.LENGTH_SHORT).show()
            // Update your progress UI element
            // e.g., binding.progressBar.progress = progress
        }
    }

    override fun onInsertionComplete() {

    }


    private fun compressBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        var byteArray = byteArrayOutputStream.toByteArray()

        if (byteArray.size > maxImageSize) {
            val compressionRatio = maxImageSize.toFloat() / byteArray.size
            byteArray = if (compressionRatio < 1) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, (compressionRatio * 100).toInt(), byteArrayOutputStream)
                byteArrayOutputStream.toByteArray()
            } else {
                byteArray
            }
        }

        return byteArray
    }





    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAll(){

        if (!App.get().isConnected()) {
            InternetConnectionDialog(this@MainActivity, null).show()
            return
        }

        RetrofitClient.getResponseFromApi().getAllRegisteredData()
            .enqueue(object : Callback<GetRegisteredData> { @SuppressLint("SetTextI18n")
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(call: Call<GetRegisteredData>, response: Response<GetRegisteredData>) {


                CoroutineScope(Dispatchers.IO).launch {
                    try {

                        if (response.isSuccessful) {
                            val vehicleDetails = response.body()


                            vehicleDetails?.let {
                                addVehicleInfoViewModel.deleteMainTable()
                                for (userI in it) {
                                    writeToFileExternal("AllSyncData.txt",userI.rfidTag,userI.vehicleNumber)
//                                    val imageUrl = userI.image
//                                    val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
//                                    val byteArray = compressBitmapToByteArray(bitmap)
//
                                    val byteArray = fetchStringFromUrl(userI.image)
//                                    val vehicleUrl = userI.vehicleImage
//                                    val vehicleBitmap = BitmapFactory.decodeStream(URL(vehicleUrl).openStream())
//                                    val byteArrayVehicle = compressBitmapToByteArray(vehicleBitmap)
                                     val byteArrayVehicle = fetchStringFromUrl(userI.vehicleImage)
//                                    withContext(Dispatchers.Main) {
//                                        addVehicleInfoViewModel.addVehicle(
//                                            RegisteredDataItem(
//                                                0,
//                                                0,
//                                                userI.createdBy,
//                                                userI.createdDate,
//                                                userI.departMentId,
//                                                "userI.department",
//                                                "userI.drivingLicenceNo",
//                                                "userI.empName",
//                                                userI.empid,
//                                                "Base64.encodeToString(byteArray, Base64.DEFAULT)",
//                                                userI.isActive,
//                                                userI.registrationNo,
//                                                userI.rfidTag,
//                                                userI.validity,
//                                                userI.vehicleColor,
//                                                "Base64.encodeToString(byteArrayVehicle, Base64.DEFAULT)",
//                                                userI.vehicleName,
//                                                userI.vehicleNumber
//                                            )
//                                        )
//                                        lifecycleScope.launch {
//                                            val rowCount =
//                                                vehicleDatabase.VehicleDao().getRowCount()
//                                            binding.tvStatus.text = "Total Registered Vehicle $rowCount"
//                                        }
//                                    }

                                    withContext(Dispatchers.Main) {
                                        addVehicleInfoViewModel.userImage(
                                            UserImage(
                                                0,
                                                userI.rfidTag,
                                                byteArray,
                                                userI.empName,
                                                userI.vehicleNumber,
                                                userI.department,userI.createdBy,userI.drivingLicenceNo,userI.vehicleName,userI.vehicleColor,userI.registrationNo,userI.isActive,userI.validity
                                            )
                                        )
                                       // Base64.encodeToString(byteArray, Base64.DEFAULT)
                                        addVehicleInfoViewModel.tempList(
                                            TempList(
                                                0,
                                                userI.rfidTag,
                                                byteArrayVehicle,
                                                userI.vehicleNumber,
                                                userI.empName
                                            )
                                        )
                                       // Base64.encodeToString(byteArrayVehicle, Base64.DEFAULT),

                                        lifecycleScope.launch {
                                            val rowCount =
                                                vehicleDatabase.VehicleDao().getRowCount()
                                            binding.tvStatus.text = "Total Registered Vehicle $rowCount"
                                        }

                                        binding.userImage.text = ""

                                    }

                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    } finally {
                        runOnUiThread {
                            binding.imSyncProgress.isVisible = false
                            binding.btnSync.isVisible = true


                        }
                    }
                }
            }

                override fun onFailure(call: Call<GetRegisteredData>, t: Throwable) {
                    binding.imSyncProgress.isVisible = false
                    binding.btnSync.isVisible = true
                    Toast.makeText(this@MainActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                }


            })
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAllDataWithout(){

        if (!App.get().isConnected()) {
            InternetConnectionDialog(this@MainActivity, null).show()
            return
        }

        RetrofitClient.getResponseFromApi().getAllRegisteredData()
            .enqueue(object : Callback<GetRegisteredData> { @SuppressLint("SetTextI18n")
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(call: Call<GetRegisteredData>, response: Response<GetRegisteredData>) {


                CoroutineScope(Dispatchers.IO).launch {
                    try {

                        if (response.isSuccessful) {
                            val vehicleDetails = response.body()


                            vehicleDetails?.let {
                                addVehicleInfoViewModel.deleteMainTable()
                                for (userI in it) {
                                    withContext(Dispatchers.Main) {
                                        addVehicleInfoViewModel.addVehicle(
                                            RegisteredDataItem(
                                                0,
                                                0,
                                                "userI.createdBy",
                                                "userI.createdDate",
                                                0,
                                                "userI.department",
                                                "userI.drivingLicenceNo",
                                                "userI.empName",
                                                0,
                                                "userI.image",
                                                1,
                                                "userI.registrationNo",
                                                userI.rfidTag,
                                                "userI.validity",
                                                "userI.vehicleColor",
                                                "userI.vehicleImage",
                                                "userI.vehicleName",
                                                "userI.vehicleNumber"
                                            )
                                        )
//                                        lifecycleScope.launch {
//                                            val rowCount =
//                                                vehicleDatabase.VehicleDao().getRowCount()
//                                            binding.tvStatus.text = "Total Registered Vehicle $rowCount"
//                                        }
                                    }



                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    } finally {
                        runOnUiThread {
                            binding.imSyncProgress.isVisible = false
                            binding.btnSync.isVisible = true

                            lifecycleScope.launch {
                                val rowCount = vehicleDatabase.VehicleDao().getRowCount()
                                binding.tvStatus.text = "Total Registered Vehicle $rowCount"
                            }
                        }
                    }
                }
            }

                override fun onFailure(call: Call<GetRegisteredData>, t: Throwable) {
                    binding.imSyncProgress.isVisible = false
                    binding.btnSync.isVisible = true
                    Toast.makeText(this@MainActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                }


            })
    }



    fun clearDatabase(context: Context) {
        try {
            // Close any open database connections
            VehicleDatabase.getDatabase(context)?.close()

            // Delete the database file
            val databaseFile = context.getDatabasePath("vehicle_database.db")
            databaseFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun dialogForTag() {
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.layout_for_sync)
        dialogTag.setCancelable(true)
        dialogTag.show()


        val send: MaterialButton = dialogTag.findViewById(R.id.bt_ok)

        send.setOnClickListener {
            dialogTag.dismiss()

            binding.imSyncProgress.isVisible = true
            binding.btnSync.isVisible = false
            //getVehicleDetails()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getDepartment()
            }
            getAll()



        }

        val btCancel: MaterialButton = dialogTag.findViewById(R.id.bt_cancel)

        btCancel.setOnClickListener {
            dialogTag.dismiss()
        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun getSyncLatest() {
        val numbersList = (1..30).toList()
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.choose_days)
        dialogTag.setCancelable(true)
        dialogTag.show()


        val send: MaterialButton = dialogTag.findViewById(R.id.bt_latest_data)
        val spType:Spinner = dialogTag.findViewById(R.id.sp_type)

            val adapter: ArrayAdapter<Int> = object : ArrayAdapter<Int>(this, androidx.appcompat.R.layout.select_dialog_item_material, numbersList ) {
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: TextView =
                        super.getDropDownView(position, convertView, parent) as TextView

                    if (position == spType.selectedItemPosition && position != 0) {
                        view.setTextColor(Color.parseColor("#000000"))
                    }
                    if (position == 0) {
                        view.setTextColor(Color.parseColor("#999999"))
                    }


                    return view
                }

                override fun isEnabled(position: Int): Boolean {
                    return position != 0
                }
            }
            spType.adapter = adapter

            spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    parent.getItemAtPosition(position).toString()
                    if (parent.getItemAtPosition(position).toString() != "Choose user role") {

                        selectData = spType.getItemIdAtPosition(position).toInt()
                        //Toast.makeText(this@LoginActivity, "position" + binding.spType.getItemIdAtPosition(position), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        send.setOnClickListener {
            dialogTag.dismiss()

            binding.imSyncProgress.isVisible = true
            binding.btnSync.isVisible = false
            //getVehicleDetails()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getDepartment()
            }
            getLatestData(selectData)


        }

//        val btCancel: MaterialButton = dialogTag.findViewById(R.id.bt_cancel)
//
//        btCancel.setOnClickListener {
//            dialogTag.dismiss()
//        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun selectSyncTypeData() {
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.choose_sync_type)
        dialogTag.setCancelable(true)
        dialogTag.show()



        val getAllData:MaterialButton = dialogTag.findViewById(R.id.bt_sync_all)
         val getLatestData:MaterialButton = dialogTag.findViewById(R.id.bt_latest_data)


        getAllData.setOnClickListener{
            dialogTag.dismiss()
            dialogForTag()
        }

        getLatestData.setOnClickListener {
            dialogTag.dismiss()
            getSyncLatest()
        }





//        val btCancel: MaterialButton = dialogTag.findViewById(R.id.bt_cancel)
//
//        btCancel.setOnClickListener {
//            dialogTag.dismiss()
//        }




    }

    private suspend fun fetchImageAndConvertToBase64(imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            connection.disconnect()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }


    private fun checkInternetSpeed() {
        internetSpeedTest.measureSpeed(object : SpeedTester.SpeedTestListener {
            override fun onDownloadSpeedChanged(speedMbps: Double) {
                val speedText = String.format("Internet Speed: %.2f Mbps", speedMbps)
                binding.textViewSpeed.text = speedText
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAllRegisterDataByPaging(page:Int){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        RetrofitClient.getResponseFromApi().getAllRegisterDataByPagingApi(page,25).enqueue(object :Callback<GetRegisteredData>{
            override fun onResponse(call: Call<GetRegisteredData>, response: Response<GetRegisteredData>) {
                if (response.code() == 200) {
                    dialog.dismiss()
                    val vehicleDetails = response.body()
                    vehicleDetails?.let {
                        addVehicleInfoViewModel.deleteMainTable()
                        for (userI in it) {

                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()


                    }
                }
            }

            override fun onFailure(call: Call<GetRegisteredData>, t: Throwable) {
              Toast.makeText(this@MainActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun submitVehicleInTime(vehicleInTimeDataModel:ArrayList<VehicleInTimeDataModel>){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        dialog.show()

        RetrofitClient.getResponseFromApi().vehicleInTime(vehicleInTimeDataModel).enqueue(object :Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    dialog.dismiss()
                    vehicleInfoViewModel.deleteInTime()
                    Toast.makeText(this@MainActivity,response.body(),Toast.LENGTH_SHORT).show()

                } else if (response.code()==400){
                    Toast.makeText(this@MainActivity,response.body(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    Toast.makeText(this@MainActivity,response.body(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    Toast.makeText(this@MainActivity,response.body(),Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
              Toast.makeText(this@MainActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }


    fun fetchStringFromUrl(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            // Check if the response code is OK (200)
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line).append("\n")
                }
                return response.toString()
            }
        } finally {
            connection.disconnect()
        }
        return ""
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLatestData(Days:Int){

        if (!App.get().isConnected()) {
            InternetConnectionDialog(this@MainActivity, null).show()
            return
        }

        RetrofitClient.getResponseFromApi().getLatestData(Days)
            .enqueue(object : Callback<GetRegisteredData> { @SuppressLint("SetTextI18n")
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(call: Call<GetRegisteredData>, response: Response<GetRegisteredData>) {


                CoroutineScope(Dispatchers.IO).launch {
                    try {

                        if (response.isSuccessful) {
                            val vehicleDetails = response.body()


                            vehicleDetails?.let {
                                addVehicleInfoViewModel.deleteMainTable()
                                for (userI in it) {

                                    val byteArray = fetchStringFromUrl(userI.image)
                                    val byteArrayVehicle = fetchStringFromUrl(userI.vehicleImage)

                                    withContext(Dispatchers.Main) {
                                        addVehicleInfoViewModel.userImage(
                                            UserImage(
                                                0,
                                                userI.rfidTag,
                                                byteArray,
                                                userI.empName,
                                                userI.vehicleNumber,
                                                userI.department,userI.createdBy,userI.drivingLicenceNo,userI.vehicleName,userI.vehicleColor,userI.registrationNo,userI.isActive,userI.validity
                                            )
                                        )
                                        // Base64.encodeToString(byteArray, Base64.DEFAULT)
                                        addVehicleInfoViewModel.tempList(
                                            TempList(
                                                0,
                                                userI.rfidTag,
                                                byteArrayVehicle,
                                                userI.vehicleNumber,
                                                userI.empName
                                            )
                                        )


                                        // Base64.encodeToString(byteArrayVehicle, Base64.DEFAULT),

                                        lifecycleScope.launch {
                                            val rowCount =
                                                vehicleDatabase.VehicleDao().getRowCount()
                                            binding.tvStatus.text = "Total Registered Vehicle $rowCount"
                                        }





                                        binding.userImage.text = ""
                                        writeToFileExternal("LatestData.txt",userI.rfidTag,userI.vehicleNumber)
                                    }

                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    } finally {
                        runOnUiThread {
                            binding.imSyncProgress.isVisible = false
                            binding.btnSync.isVisible = true


                        }
                    }
                }
            }

                override fun onFailure(call: Call<GetRegisteredData>, t: Throwable) {
                    binding.imSyncProgress.isVisible = false
                    binding.btnSync.isVisible = true
                    Toast.makeText(this@MainActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                }


            })



    }



    fun writeToFileExternal(fileName: String, data: String,time:String) {
        try {

            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED != state) {
                Log.d("writeToFileExternal", "External storage is not available")
                return
            }

            val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(externalDir, fileName)

            val fileOutputStream = FileOutputStream(file, true)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)

            // Append a newline character before adding new content
            if (file.length() > 0) {
                outputStreamWriter.append('\n')
            }

            outputStreamWriter.write(time)
            outputStreamWriter.write(" ")
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }










}