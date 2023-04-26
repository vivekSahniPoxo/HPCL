//package com.example.hpcl
//
//import android.annotation.SuppressLint
//import android.app.DatePickerDialog
//import android.app.Dialog
//import android.app.TimePickerDialog
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.graphics.drawable.BitmapDrawable
//import android.graphics.drawable.ColorDrawable
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.provider.OpenableColumns
//import android.util.Log
//import android.view.*
//import androidx.fragment.app.Fragment
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.RequiresApi
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.core.view.isVisible
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.example.hpcl.databinding.FragmentRegistrationBinding
//import com.example.hpcl.registration.RegistrationDataModel
//import com.example.hpcl.retrofit.PassRfid
//import com.example.hpcl.retrofit.RetrofitClient
//import com.google.android.material.snackbar.Snackbar
//import com.google.gson.JsonObject
//import com.speedata.libuhf.IUHFService
//import com.speedata.libuhf.UHFManager
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//
//class RegistrationFragment : Fragment(),PassRfid {
//    lateinit var binding:FragmentRegistrationBinding
//    lateinit var dialog:Dialog
//    var imageUri: Uri?=null
//    var depId = 0
//    var validDate = ""
//    var vehicleActive = 0
//    var rfidNo =  ""
//    lateinit var mActivity:MainActivity
//    lateinit var iuhfService:IUHFService
//
//
//    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
//        binding.imUser.setImageURI(null)
//        Glide.with(this).load(imageUri).centerCrop()
//            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imUser)
//    }
//
//    private val contractVehicle = registerForActivityResult(ActivityResultContracts.TakePicture()){
//        binding.imVehicle.setImageURI(null)
//        Glide.with(this).load(imageUri).centerCrop()
//            .apply(RequestOptions.skipMemoryCacheOf(true)).into(binding.imVehicle)
//    }
//
//
//
//    @SuppressLint("Range")
//    private val gContract = registerForActivityResult(ActivityResultContracts.GetContent()){
//        if (it != null) {
//            imageUri = it
//        }
//        val uri = imageUri
//        val cursor =  uri?.let { requireActivity().contentResolver.query(it, null, null, null, null) }
//        if (cursor != null && cursor.moveToNext()){
//            val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//            Log.d("nameee",name)
//        }
//        binding.imUser.setImageURI(null)
//        binding.imUser.setImageURI(it)
//        Log.d("uri",it.toString())
//    }
//
//
//    @SuppressLint("Range")
//    private val gContractVehicle = registerForActivityResult(ActivityResultContracts.GetContent()){
//        if (it != null) {
//            imageUri = it
//        }
//        val uri = imageUri
//        val cursor =  uri?.let { requireActivity().contentResolver.query(it, null, null, null, null) }
//        if (cursor != null && cursor.moveToNext()){
//            val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//            Log.d("nameee",name)
//        }
//        binding.imUser.setImageURI(null)
//        binding.imUser.setImageURI(it)
//        Log.d("uri",it.toString())
//    }
//
//
//
//
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view =  inflater.inflate(R.layout.fragment_registration, container, false)
//        binding = FragmentRegistrationBinding.bind(view)
//        return view
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//
//
//
//
//        binding.apply {
//            mcardView.setOnClickListener {
//                showDialogForGalleryAndCamera()
//            }
//            mCardVisitor.setOnClickListener {
//                showVehicleDialogForGalleryAndCamera()
//            }
//
//            idBtnPickDate.setOnClickListener {
//                pickDateTime()
//            }
//
//            btnSubmit.setOnClickListener {
//                binding.imSyncProgress.isVisible = true
//                registrationVehicle()
//            }
//        }
//
//        imageUri = createImageUri()!!
//
//
//        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
//            vehicleActive = if (isChecked) {
//                1
//                //Do Whatever you want in isChecked
//            } else{
//                0
//            }
//        }
//
//
//
//
//    }
//
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun registrationVehicle(){
//
//        // Converting user image into byteArray
//        val bitmap = (binding.imUser.drawable as BitmapDrawable).bitmap
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
//        val image = stream.toByteArray()
//
//        // Converting vehicle image into byteArray
//        val bitmapVehicle = (binding.imUser.drawable as BitmapDrawable).bitmap
//        val streamVehicle = ByteArrayOutputStream()
//        bitmapVehicle.compress(Bitmap.CompressFormat.PNG, 90, streamVehicle)
//        val vehicleImage = stream.toByteArray()
//
//        val registrationItem = RegistrationDataModel(depId,binding.etName.text.toString(),image.toBase64(),vehicleActive,binding.tvRfidNo.text.toString(),validDate,vehicleImage.toBase64(),binding.etVehicleNo.text.toString(),"","","","","")
//        RetrofitClient.getResponseFromApi().vehicleRegistration(registrationItem).enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                binding.imSyncProgress.isVisible = false
//                Toast.makeText(requireActivity(),"Successfully submitted",Toast.LENGTH_SHORT).show()
//               // FragMover.replaceFrag(activity?.supportFragmentManager, HomeFragment(), R.id.fragment_container_view_tag)
//
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                binding.imSyncProgress.isVisible = false
//                Toast.makeText(requireActivity(),t.localizedMessage,Toast.LENGTH_SHORT).show()
//            }
//
//        })
//
//    }
//
//    private fun getDepartment(){
//
//    }
//
//
//    @SuppressLint("SimpleDateFormat")
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun pickDateTime() {
//        val currentDateTime = Calendar.getInstance()
//        val startYear = currentDateTime.get(Calendar.YEAR)
//        val startMonth = currentDateTime.get(Calendar.MONTH)
//        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
//        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
//        val startMinute = currentDateTime.get(Calendar.MINUTE)
//
//
//
//        DatePickerDialog(requireActivity(), DatePickerDialog.OnDateSetListener { _, year, month, day ->
//            TimePickerDialog(requireActivity(), TimePickerDialog.OnTimeSetListener { _, hour, minute ->
//                val pickedDateTime = Calendar.getInstance()
//                pickedDateTime.set(year, month, day, hour, minute)
//                val gc = GregorianCalendar(year, month, day, hour, minute)
//                val ldt = gc.toZonedDateTime().toLocalDateTime()
//                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
//                val formatter = SimpleDateFormat("dd-MM-yyyy")
//                val output = parser.parse(ldt.toString())?.let { formatter.format(it) }
//                binding.idTVSelectedDate.text = output.toString()
//                validDate = ldt.toString()
//                Log.d("dateTime",output.toString())
//            }, startHour, startMinute, false).show()
//        }, startYear, startMonth, startDay).show()
//    }
//
//
//
//
//    private fun createImageUri(): Uri? {
//        val cImage = File(requireActivity().filesDir,"camera_photos.png")
//        return FileProvider.getUriForFile(requireActivity(),"com.example.hpcl.fileProvider",cImage)
//    }
//
//    fun View.showSnackbar(view: View, msg: String, length: Int, actionMessage: CharSequence?, action: (View) -> Unit) {
//        val snackbar = Snackbar.make(view, msg, length)
//        if (actionMessage != null) {
//            snackbar.setAction(actionMessage) {
//                action(this)
//            }.show()
//        } else {
//            snackbar.show()
//        }
//    }
//
//    private fun onClickRequestPermissionForVehicle(view: View) {
//        when {
//            ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
//                contractVehicle.launch(imageUri)
//
//            }
//
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                requireActivity(),
//                android.Manifest.permission.CAMERA
//            ) -> {
//                binding.root.showSnackbar(view, getString(R.string.permission_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
//                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//                }
//            }
//
//            else -> {
//                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//            }
//        }
//    }
//
//
//    private fun onClickRequestPermission(view: View) {
//        when {
//            ContextCompat.checkSelfPermission(
//                requireActivity(),
//                android.Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED -> {
//
//                contract.launch(imageUri)
//
//            }
//
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                requireActivity(),
//                android.Manifest.permission.CAMERA
//            ) -> {
//                binding.root.showSnackbar(
//                    view,
//                    getString(R.string.permission_required),
//                    Snackbar.LENGTH_INDEFINITE,
//                    getString(R.string.ok)
//                ) {
//                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//                }
//            }
//
//            else -> {
//                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//            }
//        }
//    }
//
//        private fun onClickRequestGalleryPermission(view: View) {
//            when {
//                ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
//
//                    gContract.launch("image/*")
//
//                }
//
//                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
//                    binding.root.showSnackbar(view, getString(R.string.permission_Storage_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
//                        requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                    }
//                }
//
//                else -> {
//                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                }
//            }
//        }
//
//
//    private fun onClickRequestGalleryForVehiclePermission(view: View) {
//        when {
//            ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
//
//                gContractVehicle.launch("image/*")
//
//            }
//
//            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
//                binding.root.showSnackbar(view, getString(R.string.permission_Storage_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {
//                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                }
//            }
//
//            else -> {
//                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//            }
//        }
//    }
//
//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                Log.i("Permission: ", "Granted")
//            } else {
//                Log.i("Permission: ", "Denied")
//            }
//        }
//
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun ByteArray.toBase64(): String = String(Base64.getEncoder().encode(this))
//
//    private fun showDialogForGalleryAndCamera() {
//        dialog = Dialog(requireActivity())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.layout_select_camera_storage)
//        dialog.setCancelable(true)
//        dialog.show()
//
//        val choose_gallery: LinearLayout? = dialog.findViewById(R.id.choose_gallery)
//        choose_gallery?.setOnClickListener {
//            dialog.dismiss()
//            onClickRequestGalleryPermission(binding.root)
//
//        }
//        val choose_camera: LinearLayout? = dialog.findViewById(R.id.choose_camera)
//        choose_camera?.setOnClickListener {
//            dialog.dismiss()
//            onClickRequestPermission(binding.root)
//
//        }
//    }
//
//
//
//    private fun showVehicleDialogForGalleryAndCamera() {
//        dialog = Dialog(requireActivity())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.layout_select_camera_storage)
//        dialog.setCancelable(true)
//        dialog.show()
//
//        val choose_gallery: LinearLayout? = dialog.findViewById(R.id.choose_gallery)
//        choose_gallery?.setOnClickListener {
//            dialog.dismiss()
//            onClickRequestGalleryForVehiclePermission(binding.root)
//
//
//        }
//        val choose_camera: LinearLayout? = dialog.findViewById(R.id.choose_camera)
//        choose_camera?.setOnClickListener {
//            dialog.dismiss()
//            onClickRequestPermissionForVehicle(binding.root)
//
//        }
//    }
//
//    override fun passRfidTag(item: String) {
//        binding.tvRfidNo.text = item
//    }
//
//
//}