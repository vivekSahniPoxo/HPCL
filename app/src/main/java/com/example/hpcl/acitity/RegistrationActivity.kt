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
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hpcl.MainActivity
import com.example.hpcl.ManageAuditTempList
import com.example.hpcl.R
import com.example.hpcl.acitity.data.RegisterModel
import com.example.hpcl.databinding.ActivityRegistrationBinding
import com.example.hpcl.localdatabase.RegisteredDataItem
import com.example.hpcl.localdatabase.TempList
import com.example.hpcl.localdatabase.UserImage
import com.example.hpcl.localdatabase.dbViewmodel.VehicleViewModel
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.GetDepartmentName
import com.example.hpcl.registration.RegistrationDataModel
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class RegistrationActivity : AppCompatActivity() {
    var countDownTimer: CountDownTimer?=null
    var isTwentySecoud=0
    private var isTimerRunning = false

    val maxSizeBytes: Long = 512 * 512 // 1 MB in bytes.
    lateinit var binding: ActivityRegistrationBinding
    lateinit var dialog: Dialog
    var imageUri: Uri?=null
    var depId = 0
    var validDate = ""
    var vehicleActive = 0
    var rfidNo =  ""
    private var selectedDep: Int = 0
    lateinit var mActivity: MainActivity
    lateinit var iuhfService: IUHFService

    private var selectedFirstAidType: Int = 0
    private var selectType= 0

    lateinit var departmentlist:ArrayList<GetDepartment>
    private var isOpening = false
    private val CLOSE_DELAY = 20000 // 20 seconds
    private var lastKeyPressTime = 0L
    private var reopenTimer: Timer? = null

    private val openRunnable = Runnable {
        closeDevice()
    }


    var departmentId = 0

    var department = arrayListOf<String>()

    lateinit var getDepartment:GetDepartmentAdapter
    private val vehicleInfoViewModel: VehicleViewModel by viewModels()
    private val addVehicleInfoViewModel: VehicleViewModel by viewModels()

    lateinit var getDepartmentName: ArrayList<GetDepartmentName>

    lateinit var  handler: Handler



    val requestOptions = RequestOptions()
        .centerCrop()
        .override(180, 180)

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        binding.imUser.setImageURI(null)
        Glide.with(this)
            .load(imageUri)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imUser)

//        Glide.with(this).load(imageUri).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
//            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imUser)
    }

    private val contractVehicle = registerForActivityResult(ActivityResultContracts.TakePicture()){
        binding.imVehicle.setImageURI(null)
        Glide.with(this)
            .load(imageUri)
//            .apply(requestOptions)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imVehicle)
//        Glide.with(this).load(imageUri).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
//            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imVehicle)
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



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler()

        department.add("Please choose department")
        getDepartmentName = arrayListOf()
        getDepartmentName.add(GetDepartmentName(0,"Choose department",0))
       val  getDepartmentAdapter = GetDepartmentAdapter(this,getDepartmentName)



        addVehicleInfoViewModel.readDertmentName.observe(this@RegistrationActivity, androidx.lifecycle.Observer {
            it.forEach {
                getDepartmentName.add(GetDepartmentName(0,it.departmentName,it.id))
            }
            GetDepartmentAdapter(this,getDepartmentName)
            binding.spType.adapter = getDepartmentAdapter
        })
        //getDepartment()
        //setTypeSpinner()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getDepartmentName.map { it.departmentName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spType.adapter = getDepartmentAdapter


        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val department = getDepartmentName[position]
                departmentId = department.id
                selectedDep = departmentId



                try {


                    val departmentName = binding.spType.selectedItem.toString().substringAfter("departmentName=").substringBefore(",")
                    Toast.makeText(this@RegistrationActivity, departmentName, Toast.LENGTH_SHORT).show()
                } catch (e:Exception){

                }
                // Do something with departmentId
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }




        departmentlist = arrayListOf()




        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30

        } catch (e:Exception){

        }





        binding.apply {
            mcardView.setOnClickListener {
                showDialogForGalleryAndCamera()
            }
            mCardVisitor.setOnClickListener {
                showVehicleDialogForGalleryAndCamera()
            }

            idBtnPickDate.setOnClickListener {
                pickDateTime()
            }

            btnSubmit.setOnClickListener {
                if (validate()) {
//                    binding.btnSubmit.isVisible = false
//                    binding.imSyncProgress.isVisible = true
//                    registrationVehicle()

                    // Converting user image into byteArray
//                    val bitmap = (binding.imUser.drawable as BitmapDrawable).bitmap
//                    val stream = ByteArrayOutputStream()
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
//                    val userImage = stream.toByteArray()


//                    val streamVehicle = ByteArrayOutputStream()
//                    bitmapVehicle.compress(Bitmap.CompressFormat.PNG, 90, streamVehicle)
//                    val vehicleImage = streamVehicle.toByteArray()

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
                        val scaleFactor = Math.sqrt(image.size.toDouble() / targetSize)
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width / scaleFactor).toInt(), (bitmap.height / scaleFactor).toInt(), true)
                        val scaledStream = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, scaledStream) // Compress the scaled image again
                        image = scaledStream.toByteArray()
                    }

                    val user = image.toBase64()
                    val vehicle = vehicleImage.toBase64()

                    val rfidTag = binding.tvRfidNo.text.toString()
                    val currentTime = LocalDateTime.now()
                    val departmentName = binding.spType.selectedItem.toString().substringAfter("departmentName=").substringBefore(",")
                    addVehicleInfoViewModel.registerVehicle(
                        RegisterModel(0,departmentId,binding.etName.text.toString(),
                        user,vehicleActive,rfidTag,binding.idTVSelectedDate.text.toString(),vehicle,binding.etVehicleNo.text.toString(),binding.etVehicleRegistratrion.text.toString(),binding.etVehicleColor.text.toString(),binding.etVehicleName.text.toString(),binding.etDriveLinecesNo.text.toString(),binding.etCreatedBy.text.toString())
                    )
                    Toast.makeText(this@RegistrationActivity, "SuccessFully Registered", Toast.LENGTH_SHORT).show()



                    addVehicleInfoViewModel.userImage(UserImage(0,binding.tvRfidNo.text.toString(),user,binding.etName.text.toString(),binding.etVehicleNo.text.toString(),departmentName,binding.etCreatedBy.text.toString(),binding.etDriveLinecesNo.text.toString(),binding.etVehicleName.text.toString(),binding.etVehicleColor.text.toString(),binding.etVehicleRegistratrion.text.toString(),vehicleActive,binding.idTVSelectedDate.text.toString()))
                    addVehicleInfoViewModel.tempList(TempList(0,binding.tvRfidNo.text.toString(),vehicle,binding.etVehicleName.text.toString(),binding.etName.text.toString()))
                    val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                     startActivity(intent)
                     finish()


                }
            }
        }

        imageUri = createImageUri()!!


        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            vehicleActive = if (isChecked) {
                1
                //Do Whatever you want in isChecked
            } else{
                0
            }
        }

    }

    fun compressBitmap(bitmap: Bitmap, maxSizeBytes: Long): Bitmap {
        var quality = 100
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        // Keep reducing the quality until the image size is below maxSizeBytes.
        while (stream.toByteArray().size > maxSizeBytes && quality > 0) {
            stream.reset()
            quality -= 5 // Adjust the decrement value as needed.
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    private fun setTypeSpinner() {
        //department.forEach {
//            val typeList = arrayOf("Choose department", it.departmentName)
        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, androidx.appcompat.R.layout.select_dialog_item_material, department) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView

                if (position == binding.spType.selectedItemPosition && position != 0) {
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
        binding.spType.adapter = adapter
        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                departmentId =  binding.spType.getItemIdAtPosition(position).toInt()

                if (position > 0) {
                    selectedFirstAidType = if (position == 1) 1 else 0
                }

                selectType = try {
                    // selectType = parent?.getItemAtPosition(position) as Int
                    Integer.parseInt(position.toString())
                } catch (e:Exception){
                    parent?.getItemAtPosition(position) as Int
                }




            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        //}
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode==KeyEvent.KEYCODE_BUTTON_R2 ) {
//            if (isOpening) {
//                lastKeyPressTime = System.currentTimeMillis()
//                reopenTimer?.cancel()

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
                        Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
                        rfidNo = readHexString
                        binding.tvRfidNo.text = rfidNo

                    } else {
                        stringBuilder.append(this.resources.getString(R.string.read_fail))
                            .append(":")
                            .append(
                                ErrorStatus.getErrorStatus(var1.status)
                            ).append("\n")
                    }
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))
                    // Toast.makeText(this,stringBuilder,Toast.LENGTH_SHORT).show()
                }
                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
                if (readArea != 0) {
                    val err: String =
                        this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(
                            readArea
                        ) + "\n"
                    handler.sendMessage(handler.obtainMessage(1, err))
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                }


                try {

                    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                } catch (e: Exception) {

                }


//
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

//
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
//        val userImage = stream.toByteArray()
////        if (stream.toByteArray().size / 1000 > 50) {
////            resizePhoto(bitmap)
////
////
////
////        }
//
//
//        // Converting vehicle image into byteArray
//        val bitmapVehicle = (binding.imVehicle.drawable as BitmapDrawable).bitmap
//        val streamVehicle = ByteArrayOutputStream()
//        bitmapVehicle.compress(Bitmap.CompressFormat.PNG, 90, streamVehicle)
//        val vehicleImage = streamVehicle.toByteArray()
//
//        Log.d("userImage",userImage.toBase64())
//        Log.d("vehicleImage",vehicleImage.toBase64())
//
//        val rfidTag = binding.tvRfidNo.text.toString()
//        val registrationItem = RegisterModel(0,selectedDep,binding.etName.text.toString(),userImage.toBase64(),vehicleActive,rfidTag,binding.idTVSelectedDate.text.toString(),vehicleImage.toBase64(),binding.etVehicleNo.text.toString(),binding.etVehicleRegistratrion.text.toString(),binding.etVehicleColor.text.toString(),binding.etVehicleName.text.toString()
//            ,binding.etDriveLinecesNo.text.toString(),binding.etCreatedBy.text.toString())
//        RetrofitClient.getResponseFromApi().vehicleRegistration(registrationItem).enqueue(object :
//            Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                binding.imSyncProgress.isVisible = false
//                binding.btnSubmit.isVisible = true
//               Toast.makeText(this@RegistrationActivity,response.body(), Toast.LENGTH_SHORT).show()
//               // FragMover.replaceFrag(activity?.supportFragmentManager, HomeFragment(), R.id.fragment_container_view_tag)
//                Log.d("response",response.body().toString())
//
//                val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                binding.imSyncProgress.isVisible = false
//                binding.btnSubmit.isVisible = true
//                Toast.makeText(this@RegistrationActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
//            }
//
//        })
//
//    }

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
//                        Log.d("dep", it.departmentName)
//                        departmentName = it.departmentName
//                        department.add(it.departmentName)
//
//                        getDepartmentName  = GetDepartmentAdapter(this@RegistrationActivity,response.body() as kotlin.collections.ArrayList<GetDepartmentName>)
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
//             Toast.makeText(this@RegistrationActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
//            }
//
//        })
//    }


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
                val formatter = SimpleDateFormat("MM/dd/yyyy")
                val output = parser.parse(ldt.toString())?.let { formatter.format(it) }
                binding.idTVSelectedDate.text = output.toString()
                validDate = ldt.toString()
                Log.d("dateTime",output.toString())
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }




    private fun createImageUri(): Uri? {
        val cImage = File(this.filesDir,"camera_photos.png")
        return FileProvider.getUriForFile(this,"com.example.hpcl.fileProvider",cImage)
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

    private fun onClickRequestPermissionForVehicle(view: View) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                try {
                    contractVehicle.launch(imageUri)
                } catch (e:Exception){

                }

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

    private fun onClickRequestGalleryPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                gContract.launch("image/*")

            }

            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                binding.root.showSnackbar(view, getString(R.string.permission_Storage_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }


    private fun onClickRequestGalleryForVehiclePermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {



            }

            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                binding.root.showSnackbar(view, getString(R.string.permission_Storage_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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


    private fun validate(): Boolean {
        val selectedPosition = binding.spType.selectedItemPosition
        if (selectedPosition==0){
        //if (binding.spType.selectedItem.toString().trim { it <= ' ' } == "Choose department") {
            Snackbar.make(binding.root, "Please choose department", Snackbar.LENGTH_SHORT).show()
           // Toast.makeText(this,"Please choose department",Toast.LENGTH_SHORT).show()
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
        } else if(binding.tvRfidNo.text.toString().length<8){
            Snackbar.make(binding.root,"Invalid Rfid Tag",Toast.LENGTH_SHORT).show()
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


    fun resizePhoto(bitmap: Bitmap): Bitmap {


        val w = bitmap.width
        val h = bitmap.height
        val aspRat = w / h
        val W = 400
        val H = W * aspRat
        val b = Bitmap.createScaledBitmap(bitmap, W, H, false)

        return b


    }


        @SuppressLint("SuspiciousIndentation")
        override fun onBackPressed() {
        super.onBackPressed()
//        iuhfService.closeDev()
        val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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