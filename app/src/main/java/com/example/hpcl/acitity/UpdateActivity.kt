package com.example.hpcl.acitity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hpcl.MainActivity
import com.example.hpcl.R
import com.example.hpcl.acitity.data.RegisterModel
import com.example.hpcl.acitity.data.UpdateModel
import com.example.hpcl.acitity.data.UpdateVehicleItem
import com.example.hpcl.databinding.ActivityUpdateBinding
import com.example.hpcl.localdatabase.RegisteredDataItem
import com.example.hpcl.localdatabase.UserImage
import com.example.hpcl.localdatabase.VehicleDao
import com.example.hpcl.localdatabase.VehicleDatabase
import com.example.hpcl.localdatabase.dbViewmodel.VehicleViewModel
import com.example.hpcl.registration.GetDepartmentName
import com.example.hpcl.registration.adapter.GetDepartmentAdapter
import com.example.hpcl.retrofit.RetrofitClient
import com.example.hpcl.utils.App
import com.example.hpcl.utils.ErrorStatus
import com.example.hpcl.utils.InternetConnectionDialog
import com.example.hpcl.utils.NetworkResult
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.utils.StringUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class UpdateActivity : AppCompatActivity() {
    var countDownTimer: CountDownTimer?=null
    var isTwentySecoud=0
    private var isTimerRunning = false
    lateinit var binding:ActivityUpdateBinding
    lateinit var getDepartment:GetDepartmentAdapter
    private val vehicleInfoViewModel: VehicleViewModel by viewModels()
    private var isOpening = false
    private val CLOSE_DELAY = 20000 // 20 seconds
    private var lastKeyPressTime = 0L
    private var reopenTimer: Timer? = null

    private val openRunnable = Runnable {
        closeDevice()
    }

    var vehicleActive = 0
    var Id = 0
    var gettingIDFromUpdateTable = 0

    lateinit var dialog: Dialog

    var imageUri: Uri?=null


    var validDate = ""
    var departmentId = 0

    var rfidNo =  ""

//    var empId = 0
    var authrose = 0



//    var createdDate = ""





   lateinit var iuhfService: IUHFService

    lateinit var getDepartmentName: ArrayList<GetDepartmentName>
    lateinit var  handler: Handler


    private val addVehicleInfoViewModel: VehicleViewModel by viewModels()


    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        binding.imUser.setImageURI(null)
        Glide.with(this).load(imageUri).fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imUser)
    }

    private val contractVehicle = registerForActivityResult(ActivityResultContracts.TakePicture()){
        binding.imVehicle.setImageURI(null)
        Glide.with(this).load(imageUri).fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imVehicle)
    }


    @SuppressLint("Range")
    private val gContract = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null) {
            imageUri = it
        }
        val uri = imageUri
        val cursor =  uri?.let { this.contentResolver.query(it, null, null, null, null) }
        if (cursor != null && cursor.moveToNext()){
            val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            Log.d("nameee",name)
        }
        binding.imUser.setImageURI(null)
        binding.imUser.setImageURI(it)
        Log.d("uri",it.toString())
    }


    @SuppressLint("Range")
    private val gContractVehicle = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null) {
            imageUri = it
        }
        val uri = imageUri
        val cursor =  uri?.let { this.contentResolver.query(it, null, null, null, null) }
        if (cursor != null && cursor.moveToNext()){
            val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            Log.d("nameee",name)
        }
        binding.imVehicle.setImageURI(null)
        binding.imVehicle.setImageURI(it)
        Log.d("uri",it.toString())
    }



    @SuppressLint("SuspiciousIndentation", "NewApi")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        try {
            Thread {
                Handler(Looper.getMainLooper()).post {
                    iuhfService = UHFManager.getUHFService(this)
                    iuhfService.openDev()
                    iuhfService.antennaPower = 30


                }

            }.start()
        } catch (e:Exception){

        }

        getDepartmentName = arrayListOf()
        handler = Handler()
        getDepartmentName.add(GetDepartmentName(0,"Choose department",0))
        val  getDepartmentAdapter = GetDepartmentAdapter(this,getDepartmentName)
//        val db = Room.databaseBuilder(applicationContext, VehicleDatabase::class.java, "my-db").build()
//        val departmentDao = db.VehicleDao()
//        val departmentName = departmentDao.readAllDepartmentName()

        addVehicleInfoViewModel.readDertmentName.observe(this@UpdateActivity, androidx.lifecycle.Observer {
            it.forEach {
                getDepartmentName.add(GetDepartmentName(0,it.departmentName,it.id))
            }

        })
        GetDepartmentAdapter(this,getDepartmentName)
        //binding.spType.adapter = getDepartmentAdapter


        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getDepartmentName.map { it.departmentName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spType.adapter = getDepartmentAdapter

        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val department = getDepartmentName[position]
                departmentId = department.id

                try {
                    Toast.makeText(this@UpdateActivity, departmentId.toString(), Toast.LENGTH_SHORT).show()
                } catch (e:Exception){

                }
                // Do something with departmentId
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }










        imageUri = createImageUri()
        //getDepartment()



        binding.idBtnPickDate.setOnClickListener {
            pickDateTime()
        }


        binding.btnSubmit.setOnClickListener {
            if (validate()) {
                // Converting user image into byteArray
                val bitmap = (binding.imUser.drawable as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream) // Use JPEG and reduce quality to 80%
                var image = stream.toByteArray()

                 // Converting vehicle image into byteArray
                val bitmapVehicle = (binding.imVehicle.drawable as BitmapDrawable).bitmap
                val streamVehicle = ByteArrayOutputStream()
                bitmapVehicle.compress(Bitmap.CompressFormat.JPEG, 80, streamVehicle) // Use JPEG and reduce quality to 80%
                val vehicleImage = streamVehicle.toByteArray()

                // Scale down the image if needed (e.g., target size less than 1MB)
                val targetSize = 1 * 512 * 512 // 1MB in bytes
                if (image.size > targetSize) {
                    val scaleFactor = sqrt(image.size.toDouble() / targetSize)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width / scaleFactor).toInt(), (bitmap.height / scaleFactor).toInt(), true)
                    val scaledStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, scaledStream) // Compress the scaled image again
                    image = scaledStream.toByteArray()
                }

//                val user = image.toBase64()
//                val vehicle = vehicleImage.toBase64()
//
//
//
//
//               // val departmentName = binding.spType.selectedItem.toString().substringAfter("departmentName=").substringBefore(",")


                val yourDatabase = VehicleDatabase.getDatabase(this@UpdateActivity)
                val vehicleDao = yourDatabase.VehicleDao()
                val rfidNumber = binding.tvRfidNo.text.toString()

                try {
                    val updateItem = vehicleDao.getFromOfflineRegistrationTable(rfidNumber)
                    updateItem.let { item ->
                        val rfid = item.rfidTag
                        Log.d("rfid", rfid)




                        if (rfid.isNotBlank() && rfid == rfidNumber) {
                            showYesNoDialog()
                        } else {
                            // Perform the update operation here, if needed.
                            // For example, you can call a function to update the item here.
                            updateItem()
                        }
                    } ?: run {
                        // Handle the case when the item with the provided RFID number is not found.
                        Log.d("rfid", "Item not found for RFID: $rfidNumber")
                    }
                } catch (e: Exception) {
                    updateItem()
                    // Handle any database-related exceptions here.
                    Log.e("UpdateActivity", "Error updating item: ${e.message}")
                }



                // 1. Modify the entityToUpdate inside the first coroutine
//                lifecycleScope.launch(Dispatchers.IO) {
//                    val yourDatabase = VehicleDatabase.getDatabase(this@UpdateActivity)
//                    val vehicleDao = yourDatabase.VehicleDao()
//
//                            val entityToUpdate = vehicleDao.getAllVehicleImage(binding.tvRfidNo.text.toString())
//                            val entityToUpdateInUserTable =
//                                vehicleDao.getUserImage(binding.tvRfidNo.text.toString())
//
//                            // Modify the fields you want to update in entityToUpdate
//                            entityToUpdate.let {
//                                it.vehicleImage = vehicle
//                                it.rfidTag = binding.tvRfidNo.text.toString()
//                                it.vehicleNumber = binding.etVehicleNo.text.toString()
//                                it.userName = binding.etName.text.toString()
//
//                                // Update entityToUpdate in the database
//                                withContext(Dispatchers.Main) {
//                                    vehicleDao.updateVehicleImage(it)
//                                }
//                            }
//
//                            // Modify the fields you want to update in entityToUpdateInUserTable
//                            entityToUpdateInUserTable.let {
//                                it.departmentName = departmentName
//                                it.userName = binding.etName.text.toString()
//                                it.vehicleNumber = binding.etVehicleNo.text.toString()
//                                it.RNo = binding.etVehicleRegistratrion.text.toString()
//                                it.Active = vehicleActive
//                                it.validity = binding.idTVSelectedDate.text.toString()
//                                it.createdBy = binding.etCreatedBy.text.toString()
//                                it.vehicleName = binding.etVehicleName.text.toString()
//                                it.vehicleColor = binding.etVehicleColor.text.toString()
//                                it.rfidTag = binding.tvRfidNo.text.toString()
//                                it.userImage = user
//
//                                // Update entityToUpdateInUserTable in the database
//                                withContext(Dispatchers.Main) {
//                                    vehicleDao.updateUserImageTable(it)
//                                }
//                            }
//
//                            // Prepare the updateVehicelIte object
//
//                            val updateVehicelIte = UpdateVehicleItem(
//                                0,
//                                0,
//                                binding.etCreatedBy.text.toString(),
//                                binding.tvCreatedDate.text.toString(),
//                                departmentId,
//                                departmentName,
//                                binding.etDriveLinecesNo.text.toString(),
//                                binding.etName.text.toString(),
//                                0,
//                                image.toBase64(),
//                                vehicleActive,
//                                binding.etVehicleRegistratrion.text.toString(),
//                                binding.tvRfidNo.text.toString(),
//                                binding.idTVSelectedDate.text.toString(),
//                                binding.etVehicleColor.text.toString(),
//                                vehicleImage.toBase64(),
//                                binding.etVehicleName.text.toString(),
//                                binding.etVehicleNo.text.toString()
//                            )
//
//                            // Update the updateVehicelIte object in the database
//                            vehicleInfoViewModel.updateVehicleItem(updateVehicelIte)
//                        }
                    }




            }
       // }



        binding.apply {
            mcardView.setOnClickListener {
                showDialogForGalleryAndCamera()
            }
            mCardVisitor.setOnClickListener {
                showVehicleDialogForGalleryAndCamera()
            }
        }


        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            vehicleActive = if (isChecked) {
                1
                //Do Whatever you want in isChecked
            } else{
                0
            }
        }

        binding.btnSearch.setOnClickListener {

            if (binding.etSearch.text?.isNotEmpty() == true) {
//                binding.imSearchProgress.isVisible = true
//                binding.btnSearch.isVisible = false
//                getVehicleDetails(binding.etSearch.text.toString())
                vehicleInfoViewModel.getDataFromUserImage(binding.etSearch.text.toString())
                bindItemFromRoomDb()
            }  else{
                binding.etSearch.error = "Please! provide vehicle no"
            }

        }





//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun getDepartment(){
//        if (!App.get().isConnected()) {
//            InternetConnectionDialog(this, null).show()
//            return
//        }
//        RetrofitClient.getResponseFromApi().getDepartment().enqueue(object :Callback<GetDepartment> {
//            override fun onResponse(call: Call<GetDepartment>, response: Response<GetDepartment>) {
//                response.body()?.forEach {
//                    try {
//
//
//                        getDepartment  = GetDepartmentAdapter(this@UpdateActivity,response.body() as List<GetDepartment.GetDepartmentItem>)
//                        binding.spType.adapter = getDepartment
//
//
//
//                    } catch (e:Exception){
//                        Log.d("exception",e.toString())
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<GetDepartment>, t: Throwable) {
//                Toast.makeText(this@UpdateActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
//            }
//
//        })
//    }

        }

    private fun createImageUri(): Uri? {
        val cImage = File(this.filesDir,"camera_photos.png")
        return FileProvider.getUriForFile(this,"com.example.hpcl.fileProvider",cImage)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun ByteArray.toBase64(): String = String(Base64.getEncoder().encode(this))

    private fun showDialogForGalleryAndCamera() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.layout_select_camera_storage)
        dialog.setCancelable(true)
        dialog.show()

        val choose_gallery: LinearLayout? = dialog.findViewById(R.id.choose_gallery)
        choose_gallery?.setOnClickListener {
            dialog.dismiss()
            gContract.launch("image/*")
            // onClickRequestGalleryPermission(binding.root)

        }
        val choose_camera: LinearLayout? = dialog.findViewById(R.id.choose_camera)
        choose_camera?.setOnClickListener {
            dialog.dismiss()
            onClickRequestPermission(binding.root)

        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    private fun onClickRequestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                contract.launch(imageUri)

            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> {
                binding.root.showSnackbar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }


    fun View.showSnackbar(view: View, msg: String, length: Int, actionMessage: CharSequence?, action: (View) -> Unit) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }


    private fun showVehicleDialogForGalleryAndCamera() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.layout_select_camera_storage)
        dialog.setCancelable(true)
        dialog.show()

        val choose_gallery: LinearLayout? = dialog.findViewById(R.id.choose_gallery)
        choose_gallery?.setOnClickListener {
            dialog.dismiss()
            gContractVehicle.launch("image/*")
            // onClickRequestGalleryForVehiclePermission(binding.root)


        }
        val choose_camera: LinearLayout? = dialog.findViewById(R.id.choose_camera)
        choose_camera?.setOnClickListener {
            dialog.dismiss()
            onClickRequestPermissionForVehicle(binding.root)

        }
    }

    private fun onClickRequestPermissionForVehicle(view: View) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {

                   contractVehicle.launch(imageUri)


            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            ) -> {
                binding.root.showSnackbar(view, getString(R.string.permission_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)



        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)
                val gc = GregorianCalendar(year, month, day, hour, minute)
                val ldt = gc.toZonedDateTime().toLocalDateTime()
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
               // val formatter = SimpleDateFormat("yyy-MM-dd")
               // val formatter = SimpleDateFormat("MM/dd/yyyy")
                val formatter = SimpleDateFormat("M/d/yyyy h:mm:ss a")
                val output = parser.parse(ldt.toString())?.let { formatter.format(it) }
                binding.idTVSelectedDate.text = output.toString()
                validDate = ldt.toString()
                Log.d("dateTime",output.toString())
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun  getVehicleDetails(vehicleNo:String) {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getVehicleInfo(vehicleNo)
            .enqueue(object : Callback<UpdateModel> {
                @SuppressLint("SimpleDateFormat")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<UpdateModel>, response: Response<UpdateModel>) {
                    binding.imSearchProgress.isVisible = false
                    binding.btnSearch.isVisible = true
                    try {
                        val toneGen1 = ToneGenerator(AudioManager.STREAM_RING, 100)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)


                        val vehicleDetails = response.body()
                        if (response.code() == 200) {
                            if (vehicleDetails != null) {
                                binding.apply {
                                    vehicleDetails.forEach {
                                        etName.setText(vehicleDetails[0].empName)
                                       // tvDepartment.text = vehicleDetails[0].department
                                        etVehicleNo.setText(vehicleDetails[0].vehicleNumber)
                                        idTVSelectedDate.text = vehicleDetails[0].validity
                                        etCreatedBy.setText(vehicleDetails[0].createdBy)
                                        etVehicleRegistratrion.setText(vehicleDetails[0].registrationNo)
                                        etDriveLinecesNo.setText(vehicleDetails[0].drivingLicenceNo)
                                        etVehicleName.setText(vehicleDetails[0].vehicleName)
                                        etVehicleColor.setText(vehicleDetails[0].vehicleColor)
                                        tvRfidNo.text = vehicleDetails[0].rfidTag



                                    }
                                }

                                 departmentId = vehicleDetails[0].departMentId







                                val vehicleImage = vehicleDetails[0].vehicleImage
                                val userImage = vehicleDetails[0].image

                                // used for user image
//                                if (userImage.isEmpty()){
//                                    binding.imUser.setBackgroundResource(R.drawable.priest)
//                                } else {
                                    val imageBytes = android.util.Base64.decode(userImage, 0)
                                    val img = convertCompressedByteArrayToBitmap(imageBytes)
                                    binding.imUser.setImageBitmap(img)
                               // }

//                                Glide.with(this@UpdateActivity).load(userImage).centerCrop().diskCacheStrategy(
//                                    DiskCacheStrategy.NONE)
//                                    .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imUser)
//                                Glide.with(this@UpdateActivity).load(vehicleImage).centerCrop().diskCacheStrategy(
//                                    DiskCacheStrategy.NONE)
//                                    .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imVehicle)



//                                if (vehicleImage.isEmpty()){
//                                    binding.imVehicle.setBackgroundResource(R.drawable.car)
//                                } else {
                                    val vImageBytes = android.util.Base64.decode(vehicleImage, 0)
                                    val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
                                    binding.imVehicle.setImageBitmap(vImg)
                              //  }







                            } else{

                            }

                        }


                    } catch (e:Exception){
                    Toast.makeText(this@UpdateActivity,"No Data Found",Toast.LENGTH_SHORT).show()


                    }
                }

                override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                    binding.imSearchProgress.isVisible= false
                    binding.btnSearch.isVisible = true
                    Log.d("Error",t.localizedMessage)
                }
            })
    }


    fun convertCompressedByteArrayToBitmap(src: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(src, 0, src.size)
    }



    private fun validate(): Boolean {
        val selectedPosition = binding.spType.selectedItemPosition
        if (selectedPosition==0){
        //if (binding.spType.selectedItem.toString().trim { it <= ' ' } == "Choose department") {
            Snackbar.make(binding.root, "Please choose department", Snackbar.LENGTH_SHORT).show()
            return false
        } else if(binding.etName.text?.isEmpty() == true){
            binding.etName.error = "Name field should not be empty"
            return false
        } else if (binding.etVehicleNo.text?.isEmpty()==true){
            binding.etVehicleNo.error = "Please provide vehicle no"
            return false
        } else if (binding.etCreatedBy.text?.isEmpty()==true){
            binding.etCreatedBy.error = "Please provide creator name"
            return false
        } else if (binding.etDriveLinecesNo.text?.isEmpty()==true){
            binding.etDriveLinecesNo.error = "Please provide driver license no"
            return false
        } else if (binding.etVehicleName.text?.isEmpty()==true){
            binding.etVehicleName.error = "Please provide vehicle name"
            return false
        } else if(binding.etVehicleColor.text?.isEmpty()==true){
            binding.etVehicleColor.error = "Please provide vehicle color"
            return false
        } else if (binding.etVehicleRegistratrion.text?.isEmpty()==true){
            binding.etVehicleRegistratrion.error = "Please provide vehicle registration no"
            return false
        } else if (binding.tvRfidNo.text.isEmpty()){
            binding.tvRfidNo.error = "Please scan the rfid tag"
            return false
        } else if (binding.idTVSelectedDate.text.isEmpty()){
            binding.idTVSelectedDate.error = "Please pick valid date"
            return false
        } else if (binding.imUser.drawable==null){
            Snackbar.make(binding.root,"Please click Driver Image",Snackbar.LENGTH_SHORT).show()
            return false
        } else if (binding.imVehicle.drawable==null){
            Snackbar.make(binding.root,"Please click vehicle Image",Snackbar.LENGTH_SHORT).show()
            return false
        }
        return true
    }



//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun registrationVehicle(){
//        if (!App.get().isConnected()) {
//            InternetConnectionDialog(this, null).show()
//            return
//        }
//
//        // Converting user image into byteArray
//        val bitmap = (binding.imUser.drawable as BitmapDrawable).bitmap
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
//        val image = stream.toByteArray()
//
//        // Converting vehicle image into byteArray
//        val bitmapVehicle = (binding.imVehicle.drawable as BitmapDrawable).bitmap
//        val streamVehicle = ByteArrayOutputStream()
//        bitmapVehicle.compress(Bitmap.CompressFormat.PNG, 90, streamVehicle)
//        val vehicleImage = streamVehicle.toByteArray()
//          val user = image.toBase64()
//        val vehicle = vehicleImage.toBase64()
//        Log.d("userImage",user)
//        Log.d("vehicleImage",vehicle)
//
//        val rfidTag = binding.tvRfidNo.text.toString()
//        val registrationItem = RegistrationDataModel(0,selectedDep,binding.etName.text.toString(),image.toBase64(),vehicleActive,rfidTag,binding.idTVSelectedDate.text.toString(),vehicleImage.toBase64(),binding.etVehicleNo.text.toString(),binding.etVehicleRegistratrion.text.toString(),binding.etVehicleColor.text.toString(),binding.etVehicleName.text.toString()
//            ,binding.etDriveLinecesNo.text.toString(),binding.etCreatedBy.text.toString())
//        RetrofitClient.getResponseFromApi().updateVehicleInfo(registrationItem).enqueue(object :
//            Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                binding.imSyncProgress.isVisible = false
//                binding.btnSubmit.isVisible = true
//                Toast.makeText(this@UpdateActivity,response.body(), Toast.LENGTH_SHORT).show()
//                // FragMover.replaceFrag(activity?.supportFragmentManager, HomeFragment(), R.id.fragment_container_view_tag)
//
//                val intent = Intent(this@UpdateActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                binding.imSyncProgress.isVisible = false
//                binding.btnSubmit.isVisible = true
//                Toast.makeText(this@UpdateActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
//            }
//
//        })
//
//    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode==KeyEvent.KEYCODE_BUTTON_R2 ) {

//            if (isOpening) {
//                lastKeyPressTime = System.currentTimeMillis()
            iuhfService.setOnReadListener { var1 ->
                val stringBuilder = StringBuilder()
                val epcData = var1.epcData
                val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：").append(hexString).append("\n")
                }
                if (var1.status == 0) {
                    val readData = var1.readData
                    val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)

                    stringBuilder.append("ReadData:").append(readHexString).append("\n")
                    Toast.makeText(this@UpdateActivity,readHexString,Toast.LENGTH_SHORT).show()
                    rfidNo = readHexString
                    binding.tvRfidNo.text = rfidNo
//                    vehicleInfoViewModel.getVisitInfo(readHexString)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        bindItemFromRoomDb()
//                    }
                } else {
                    stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(
                        ErrorStatus.getErrorStatus(var1.status)).append("\n")
                }
                handler.sendMessage(handler.obtainMessage(1, stringBuilder))
                // Toast.makeText(this,stringBuilder,Toast.LENGTH_SHORT).show()
            }
            val readArea = iuhfService.readArea(1, 2, 6, "00000000")
            if (readArea != 0) {
                val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(readArea) + "\n"
                handler.sendMessage(handler.obtainMessage(1, err))
                Toast.makeText(this,err,Toast.LENGTH_SHORT).show()
            }

            try {
                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            } catch (e:Exception){

            }


            return true
//            } else {
//                openDevice()
//            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                iuhfService.closeDev()
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }


    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindItemFromRoomDb() {
        vehicleInfoViewModel.getDataFromUserImage.observe(this, androidx.lifecycle.Observer {
            binding.imSearchProgress.isVisible = false
            when (it) {
                is NetworkResult.Success->{
                    try {
                    binding.apply {
                        vehicleInfoViewModel.getVehicleImage(it.data?.rfidTag.toString())
                        bindVehicleImage()
                        gettingIDFromUpdateTable = it.data?.ID!!
                           // binding.tvEmpiD.text = it.data?.empid?.toString()

                           // binding.tvCreatedDate.text = it.data?.createdDate.toString()
                            authrose = 0
                            etName.setText(it.data?.userName)
                            // tvDepartment.text = vehicleDetails[0].department
                            etVehicleNo.setText(it.data?.vehicleNumber)
                            idTVSelectedDate.text = it.data?.validity
                            etCreatedBy.setText(it.data?.createdBy)
                            etVehicleRegistratrion.setText(it.data?.RNo)
                            etDriveLinecesNo.setText(it.data?.dl)
                            etVehicleName.setText(it.data?.vehicleName)
                            etVehicleColor.setText(it.data?.vehicleColor)
                            tvRfidNo.text = it.data?.rfidTag


                        }

                        //departmentId = it.data?.departMentId!!


                       // val vehicleImage = it.data.vehicleImage
                        val userImage = it.data?.userImage


                        val imageBytes = android.util.Base64.decode(userImage, 0)
                        val img = convertCompressedByteArrayToBitmap(imageBytes)
                        binding.imUser.setImageBitmap(img)

//                        val vImageBytes = android.util.Base64.decode(vehicleImage, 0)
//                        val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
//                        binding.imVehicle.setImageBitmap(vImg)
                    } catch (e:Exception){
                        Toast.makeText(this,"No data found",Toast.LENGTH_SHORT).show()
                    }


                }
                is NetworkResult.Error->{
                    Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()

                }
                is NetworkResult.Loading->{
                    binding.imSearchProgress.isVisible = true
                }

            }
        })


    }

    override fun onBackPressed() {
        super.onBackPressed()
//        iuhfService.closeDev()
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }


    private fun bindVehicleImage(){
        vehicleInfoViewModel.getVehicleImage.observe(this, androidx.lifecycle.Observer {
            when(it){
                is NetworkResult.Success->{
                    try {
                        if (it.data?.vehicleImage !== null) {
                            val vImageBytes = android.util.Base64.decode(it.data?.vehicleImage, 0)
                            val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
                            binding.imVehicle.setImageBitmap(vImg)
                        }
                    } catch (e:Exception){

                    }
                }
                is NetworkResult.Error->{

                }
                is NetworkResult.Loading->{

                }
            }
        })
    }


    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to proceed?")
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            // Handle "Yes" button click
            // Implement the logic you want to execute when the user clicks "Yes"
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            // Handle "No" button click
            // Implement the logic you want to execute when the user clicks "No"
        }
        builder.setCancelable(false) // Prevent dialog from being dismissed when clicking outside
        val dialog = builder.create()
        dialog.show()
    }



    @SuppressLint("NewApi", "SuspiciousIndentation", "LongLogTag")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateItem(){

        try{
        val bitmap = (binding.imUser.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream) // Use JPEG and reduce quality to 80%
        var image = stream.toByteArray()

        // Converting vehicle image into byteArray
        val bitmapVehicle = (binding.imVehicle.drawable as BitmapDrawable).bitmap
        val streamVehicle = ByteArrayOutputStream()
        bitmapVehicle.compress(Bitmap.CompressFormat.JPEG, 80, streamVehicle) // Use JPEG and reduce quality to 80%
        val vehicleImage = streamVehicle.toByteArray()

        // Scale down the image if needed (e.g., target size less than 1MB)
        val targetSize = 1 * 512 * 512 // 1MB in bytes
        if (image.size > targetSize) {
            val scaleFactor = sqrt(image.size.toDouble() / targetSize)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width / scaleFactor).toInt(), (bitmap.height / scaleFactor).toInt(), true)
            val scaledStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, scaledStream) // Compress the scaled image again
            image = scaledStream.toByteArray()
        }





        val departmentName = binding.spType.selectedItem.toString().substringAfter("departmentName=").substringBefore(",")
            lifecycleScope.launch(Dispatchers.IO) {
                val yourDatabase = VehicleDatabase.getDatabase(this@UpdateActivity)
                val vehicleDao = yourDatabase.VehicleDao()

               // gettingIDFromUpdateTable
//                val entityToUpdate = vehicleDao.getAllVehicleImage(binding.tvRfidNo.text.toString())
//                val entityToUpdateInUserTable = vehicleDao.getUserImage(binding.tvRfidNo.text.toString())


                val entityToUpdate = vehicleDao.getAllVehicleImageByID(gettingIDFromUpdateTable)
                val entityToUpdateInUserTable = vehicleDao.getUserImageByInt(gettingIDFromUpdateTable)



                // Check if entityToUpdate is not null
                // if (entityToUpdate != null) {
                // Modify the fields you want to update in entityToUpdate
                try {
                    entityToUpdate.vehicleImage = vehicleImage.toBase64()
                    entityToUpdate.rfidTag = binding.tvRfidNo.text.toString()
                    entityToUpdate.vehicleNumber = binding.etVehicleNo.text.toString()
                    entityToUpdate.userName = binding.etName.text.toString()

                    // Update entityToUpdate in the database
                    withContext(Dispatchers.Main) {
                        vehicleDao.updateVehicleImage(entityToUpdate)
                    }
                } catch (e: Exception) {

                }


               // try {

                    // Modify the fields you want to update in entityToUpdateInUserTable
                    entityToUpdateInUserTable.let {
                        //if (entityToUpdateInUserTable!=null) {
                        it.departmentName = departmentName
                        it.userName = binding.etName.text.toString()
                        it.vehicleNumber = binding.etVehicleNo.text.toString()
                        it.RNo = binding.etVehicleRegistratrion.text.toString()
                        it.Active = vehicleActive
                        it.validity = binding.idTVSelectedDate.text.toString()
                        it.createdBy = binding.etCreatedBy.text.toString()
                        it.vehicleName = binding.etVehicleName.text.toString()
                        it.vehicleColor = binding.etVehicleColor.text.toString()
                        it.rfidTag = binding.tvRfidNo.text.toString()
                        it.userImage = image.toBase64()

                        // Update entityToUpdateInUserTable in the database
                        withContext(Dispatchers.Main) {
                            vehicleDao.updateUserImageTable(it)
                        }
//                    } else{
//
//                    }
                    }
//                } catch (e: Exception) {
//                    Log.d("exce1", e.toString())
//                }


                // Prepare the updateVehicelIte object
               // try {
                    val updateVehicelIte = UpdateVehicleItem(
                        0,
                        0,
                        binding.etCreatedBy.text.toString(),
                        binding.tvCreatedDate.text.toString(),
                        departmentId,
                        departmentName,
                        binding.etDriveLinecesNo.text.toString(),
                        binding.etName.text.toString(),
                        0,
                        image.toBase64(),
                        vehicleActive,
                        binding.etVehicleRegistratrion.text.toString(),
                        binding.tvRfidNo.text.toString(),
                        binding.idTVSelectedDate.text.toString(),
                        binding.etVehicleColor.text.toString(),
                        vehicleImage.toBase64(),
                        binding.etVehicleName.text.toString(),
                        binding.etVehicleNo.text.toString()
                    )

                    // Update the updateVehicelIte object in the database
                    vehicleInfoViewModel.updateVehicleItem(updateVehicelIte)
//                } catch (e:Exception){
//                    Log.d("exce2",e.toString())
//                }
            }

        Toast.makeText(this,"Update Successfully",Toast.LENGTH_SHORT).show()
        val intent = Intent(this@UpdateActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
        } catch (e:Exception){

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showYesNoDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Set the dialog title and message
        alertDialogBuilder.setTitle("Confirmation")
        alertDialogBuilder.setMessage("This vehicle is already updated,Are you sure you want to proceed?")

        // Set the positive button (Yes button) and its click listener
        alertDialogBuilder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
            // Handle the "Yes" button click here
            // Call any desired function or perform the necessary action
            dialogInterface.dismiss() // Dismiss the dialog
            updateItem()
        }

        // Set the negative button (No button) and its click listener
        alertDialogBuilder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            // Handle the "No" button click here
            // Call any desired function or perform the necessary action
            dialogInterface.dismiss() // Dismiss the dialog
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    override fun onPause() {
        super.onPause()

        iuhfService.closeDev()
    }

    override fun onStop() {
        super.onStop()
        iuhfService.closeDev()
    }
    private fun openDevice() {
        if (!isOpening) {
            iuhfService.openDev()
            isOpening = true
            reopenTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        closeDevice()
                    }
                }, CLOSE_DELAY.toLong())
            }
        }
    }



    private fun closeDevice() {
        if (isOpening) {
            iuhfService.closeDev()
            isOpening = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to remove the callbacks to avoid memory leaks
        handler.removeCallbacks(openRunnable)
    }


    private fun startTimer() {
        isTimerRunning = true
        countDownTimer = object : CountDownTimer(21000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Calculate remaining time in seconds
                isTwentySecoud = ((21000 - millisUntilFinished) / 1000).toInt()
                Log.d("secondsRemaining", isTwentySecoud.toString())

            }

            override fun onFinish() {
                // Perform actions when the timer finishes
                isTimerRunning = false
                iuhfService.closeDev()
            }
        }

        (countDownTimer as CountDownTimer).start()
    }

    private fun cancelTimer() {
        isTimerRunning = false
        isTwentySecoud=0
        countDownTimer?.cancel()
        startTimer()
    }



}