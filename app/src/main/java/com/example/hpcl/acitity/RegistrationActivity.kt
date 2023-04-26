package com.example.hpcl.acitity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hpcl.MainActivity
import com.example.hpcl.R
import com.example.hpcl.databinding.ActivityRegistrationBinding
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.retrofit.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class RegistrationActivity : AppCompatActivity() {

    lateinit var binding: ActivityRegistrationBinding
    lateinit var dialog: Dialog
    var imageUri: Uri?=null
    var depId = 0
    var validDate = ""
    var vehicleActive = 0
    var rfidNo =  ""
    lateinit var mActivity: MainActivity
    lateinit var iuhfService: IUHFService

    private var selectedFirstAidType: Int = 0
    private var selectType= 0

    lateinit var departmentlist:ArrayList<GetDepartment>

    var departmentName =  ""
    var departmentId = 0

    var department = arrayListOf<String>()


    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        binding.imUser.setImageURI(null)
        Glide.with(this).load(imageUri).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imUser)
    }

    private val contractVehicle = registerForActivityResult(ActivityResultContracts.TakePicture()){
        binding.imVehicle.setImageURI(null)
        Glide.with(this).load(imageUri).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
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



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        department.add("Please choose department")
        getDepartment()
        setTypeSpinner()

        departmentlist = arrayListOf()

        iuhfService = UHFManager.getUHFService(this)


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
                    binding.btnSubmit.isVisible = false
                    binding.imSyncProgress.isVisible = true
                    registrationVehicle()
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



                if (position > 0) {
                    selectedFirstAidType = if (position == 1) 1 else 0
                }

                try {
                   // selectType = parent?.getItemAtPosition(position) as Int
                    selectType =  Integer.parseInt(position.toString())
                } catch (e:Exception){
                    selectType = parent?.getItemAtPosition(position) as Int
                }




            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        //}
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1) {

            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)

            Thread {
                Handler(Looper.getMainLooper()).post {

                    iuhfService.openDev()
                    iuhfService.antennaPower = 30
                    iuhfService.inventoryStart()

                }

            }.start()

            rfidNo = iuhfService.read_area(1, "2", "6", "00000000").toString()

            binding.tvRfidNo.text = rfidNo

            return true
        }
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun registrationVehicle(){

        // Converting user image into byteArray
        val bitmap = (binding.imUser.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val image = stream.toByteArray()

        // Converting vehicle image into byteArray
        val bitmapVehicle = (binding.imUser.drawable as BitmapDrawable).bitmap
        val streamVehicle = ByteArrayOutputStream()
        bitmapVehicle.compress(Bitmap.CompressFormat.PNG, 90, streamVehicle)
        val vehicleImage = stream.toByteArray()

        val rfidTag = binding.tvRfidNo.text.toString()
        val registrationItem = RegistrationDataModel(selectType,binding.etName.text.toString(),image.toBase64(),vehicleActive,rfidTag,binding.idTVSelectedDate.text.toString(),vehicleImage.toBase64(),binding.etVehicleNo.text.toString(),binding.etVehicleRegistratrion.text.toString(),binding.etVehicleColor.text.toString(),binding.etVehicleName.text.toString()
            ,binding.etDriveLinecesNo.text.toString(),binding.etCreatedBy.text.toString())
        RetrofitClient.getResponseFromApi().vehicleRegistration(registrationItem).enqueue(object :
            Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                binding.imSyncProgress.isVisible = false
                binding.btnSubmit.isVisible = true
                Toast.makeText(this@RegistrationActivity,response.body(), Toast.LENGTH_SHORT).show()
               // FragMover.replaceFrag(activity?.supportFragmentManager, HomeFragment(), R.id.fragment_container_view_tag)

                val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                startActivity(intent)

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                binding.imSyncProgress.isVisible = false
                binding.btnSubmit.isVisible = true
                Toast.makeText(this@RegistrationActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun getDepartment(){
        RetrofitClient.getResponseFromApi().getDepartment().enqueue(object :Callback<GetDepartment> {
            override fun onResponse(call: Call<GetDepartment>, response: Response<GetDepartment>) {
                response.body()?.forEach {
                    try {

                        Log.d("dep", it.departmentName)
                        departmentName = it.departmentName
                        department.add(it.departmentName)



                    } catch (e:Exception){
                        Log.d("exception",e.toString())
                    }
                }
            }

            override fun onFailure(call: Call<GetDepartment>, t: Throwable) {
             Toast.makeText(this@RegistrationActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
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
                val formatter = SimpleDateFormat("yyy-MM-dd")
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
        if (binding.spType.selectedItem.toString().trim { it <= ' ' } == "Please choose department") {
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
    
    
    
}