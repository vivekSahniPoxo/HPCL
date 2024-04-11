package com.example.hpcl.identification

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.*
import android.service.autofill.UserData
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.hpcl.MainActivity
import com.example.hpcl.R
import com.example.hpcl.VehicleHistoryModelClass
import com.example.hpcl.databinding.ActivityVehicleBinding
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.identification.data.VehicleIdentification
import com.example.hpcl.localdatabase.TempList
import com.example.hpcl.localdatabase.UserImage
import com.example.hpcl.localdatabase.VehicleDao
import com.example.hpcl.localdatabase.VehicleDatabase
import com.example.hpcl.localdatabase.dbViewmodel.VehicleViewModel
import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.retrofit.RetrofitClient
import com.example.hpcl.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*


class VehicleActivity : AppCompatActivity() {
    lateinit var binding:ActivityVehicleBinding
    private lateinit var rfidLogger: RfidLogger

      var countDownTimer: CountDownTimer?=null
    private var isTimerRunning = false

    lateinit var  vehicleDao: VehicleDao
    lateinit var iuhfService: IUHFService
    lateinit var dialog:Dialog
    lateinit var dialogTag:Dialog
    var RfidNo: String = ""
    var depId = 0
    var isVehicleActive = 0
    var getRfidNo = ""
    var count = 0

    private var isOpening = false
    lateinit var  handler: Handler
    private var lastKeyPressTime = 0L
    private var reopenTimer: Timer? = null

    var isTwentySecoud=0

    private val openRunnable = Runnable {
        closeDevice()
    }

    private val CLOSE_DELAY = 20000 // 20 seconds

    private val vehicleInfoViewModel: VehicleViewModel by viewModels()



    private lateinit var database: VehicleDatabase
    private lateinit var dao: VehicleDao
    var isInventoryRunning = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rfidLogger = RfidLogger(this)


        val database: VehicleDatabase = VehicleDatabase.getDatabase(this)
        vehicleDao = database.VehicleDao()






        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()


            //iuhfService.antennaPower = 30



        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }

        dialog = Dialog(this)

        handler = Handler()


        binding.radioGroup.isVisible = false



//        binding.btnUHF.setOnClickListener {
//            count++
//            if (count==1) {
//                vehicleInfoViewModel.getUserImage("34161FA820328EE818319760")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328EE818319760")
//                bindVehicleImage1()
//            } else if (count==2){
//                vehicleInfoViewModel.getUserImage("34161FA820328E4025DEBCC0")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328E4025DEBCC0")
//                bindVehicleImage1()
//            } else if (count==3){
//                vehicleInfoViewModel.getUserImage("34161FA820328EE81F60CD80")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328EE81F60CD80")
//                bindVehicleImage1()
//            } else if (count==4){
//                vehicleInfoViewModel.getUserImage("34161FA820328E4028342540")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328E4028342540")
//                bindVehicleImage1()
//            } else if (count==5){
//                vehicleInfoViewModel.getUserImage("34161FA82033E6EE03ABF4C0")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA82033E6EE03ABF4C0")
//                bindVehicleImage1()
//            }else if (count==6){
//                vehicleInfoViewModel.getUserImage("34161FA82032897227CE40A0")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA82032897227CE40A0")
//                bindVehicleImage1()
//            }else if (count==7){
//                vehicleInfoViewModel.getUserImage("34161FA82033E6EE0332ECA0")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                    bindUserImage1()
//                    vehicleInfoViewModel.getVehicleImage("34161FA82033E6EE0332ECA0")
//                    bindVehicleImage1()
//                } else if (count==8){
//                    vehicleInfoViewModel.getUserImage("00A18647A503006047AD3210")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("00A18647A503006047AD3210")
//                    bindVehicleImage1()
//                }else if (count==9){
//                    vehicleInfoViewModel.getUserImage("34161FA820328E401B9BEA80")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("34161FA820328E401B9BEA80")
//                    bindVehicleImage1()
//                } else if (count==10){
//                    vehicleInfoViewModel.getUserImage("34161FA8203289723D78DC40")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("34161FA8203289723D78DC40")
//                    bindVehicleImage1()
//                } else if (count==11){
//                    vehicleInfoViewModel.getUserImage("34161FA82032861403209A80")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("34161FA82032861403209A80")
//                    bindVehicleImage1()
//                } else if (count==12){
//                    vehicleInfoViewModel.getUserImage("34161FA82032897223003660")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("34161FA82032897223003660")
//                    bindVehicleImage1()
//                }else if (count==13){
//                    vehicleInfoViewModel.getUserImage("34161FA8203289DA0E4BB740")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("34161FA8203289DA0E4BB740")
//                    bindVehicleImage1()
//                }else if (count==14){
//                    vehicleInfoViewModel.getUserImage("34161FA820328E400ECC7E20")
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        bindItemFromRoomDb()
//                        bindUserImage1()
//                    }
//
//                    vehicleInfoViewModel.getVehicleImage("34161FA820328E400ECC7E20")
//                    bindVehicleImage1()
//                }
//
//            }else if (count==15){
//                vehicleInfoViewModel.getUserImage("34161FA820328E4026DABEE0")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328E4026DABEE0")
//                bindVehicleImage1()
//            }else if (count==16){
//                vehicleInfoViewModel.getUserImage("34161FA820328E4027E99560")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328E4027E99560")
//                bindVehicleImage1()
//            }else if (count==17){
//                vehicleInfoViewModel.getUserImage("34161FA820328EE828A66420")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328EE828A66420")
//                bindVehicleImage1()
//            }else if (count==18){
//                vehicleInfoViewModel.getUserImage("34161FA8203289723B374120")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA8203289723B374120")
//                bindVehicleImage1()
//            }else if (count==19){
//                vehicleInfoViewModel.getUserImage("34161FA820328AA2C8455580")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328AA2C8455580")
//                bindVehicleImage1()
//            }else if (count==20){
//                vehicleInfoViewModel.getUserImage("34161FA820328EE80C8D8F20")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    bindUserImage1()
//                }
//
//                vehicleInfoViewModel.getVehicleImage("34161FA820328EE80C8D8F20")
//                bindVehicleImage1()
//            }
//
//            Log.d("count",count.toString())
//
//
//        }







        binding.btnErase.setOnClickListener {
            binding.apply {
                imEmpImg.setImageDrawable(ContextCompat.getDrawable(this@VehicleActivity, R.drawable.priest))
                ll3.setBackgroundResource(R.drawable.bg_box)
                imVehicle.setImageDrawable(ContextCompat.getDrawable(this@VehicleActivity, R.drawable.car))
                linearLayout.setBackgroundResource(R.drawable.bg_box)
                imTick.isVisible=false
                tvName.text = ""
                tvDepartment.text = ""
                tvVehicaleNo.text = ""
                tvActivit.text = ""
                tvValidity.text =""
                tvCreatedBy.text = ""
                tvDrivingLincesNo.text = ""
                tvVehicaleNo.text = ""
                tvVehicleColor.text = ""
                tvVehicleName.text = ""
                tvRegistration.text = ""
                binding.radioGroup.isVisible = false

            }
        }


        binding.btnReject.setOnClickListener {
            dialogForReject()


        }


        binding.btnOk.setOnClickListener {
            binding.btnReject.isEnabled = false
            binding.progressBarOk.isVisible=true
            binding.btnOk.isVisible = false
            binding.tvActivit.isVisible = false
           // isVehicleActive = 1
            //registrationVehicle()
//            manageVehicleAudit(binding.tvCreatedBy.text.toString(),"",RfidNo,1)
            val vehicleAudit = ManageVehicleAudit(0,binding.tvCreatedBy.text.toString(),"",binding.tvRfid.text.toString(),1)
            vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)



            val vehicleInTime = VehicleHistoryModelClass(0,binding.tvRfid.text.toString(),LocalDateTime.now().toString())
            vehicleInfoViewModel.addVehicleInTime(vehicleInTime)

//            val vehicleHistory  = VehicleHistoryModelClass(0,binding.tvCreatedBy.text.toString(),getIPAddress().toString(),LocalDateTime.now().toString())
//           vehicleInfoViewModel.vehicleHistoryInsert(vehicleHistory)
            Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
            //manageVehicleAudit()
            dialog.dismiss()
            binding.btnReject.isEnabled = true
            binding.progressBarOk.isVisible=false
            binding.btnOk.isVisible = true
            binding.radioGroup.isVisible = false

           // Toast.makeText(this@VehicleActivity,response.body(),Toast.LENGTH_SHORT).show()
            binding.apply { imEmpImg.setImageDrawable(ContextCompat.getDrawable(this@VehicleActivity, R.drawable.priest))
                ll3.setBackgroundResource(R.drawable.bg_box)
                imVehicle.setImageDrawable(
                    ContextCompat.getDrawable(this@VehicleActivity, R.drawable.car))
                linearLayout.setBackgroundResource(R.drawable.bg_box)
                imTick.isVisible = false
                tvName.text = ""
                tvDepartment.text = ""
                tvVehicaleNo.text = ""
                tvActivit.text = ""
                tvValidity.text = ""
                tvCreatedBy.text = ""
                tvDrivingLincesNo.text = ""
                tvVehicaleNo.text = ""
                tvVehicleColor.text = ""
                tvRegistration.text = ""
                tvVehicleName.text = ""

            }
        }

    }

    fun getIPAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun  getVehicleDetails(rfid:String) {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getVehicleDetails(rfid)
            .enqueue(object : Callback<VehicleIdentification> {
                @SuppressLint("SimpleDateFormat")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<VehicleIdentification>, response: Response<VehicleIdentification>) {
                    binding.progressBar.isVisible = false
                    try {
                        val toneGen1 = ToneGenerator(AudioManager.STREAM_RING, 100)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)
                        val mColors = arrayOf(ColorDrawable(Color.GREEN),ColorDrawable(Color.WHITE))
                        val mTransition = TransitionDrawable(mColors)
                        binding.rootLayout.background = mTransition
                        mTransition.startTransition(1000)
                        val vehicleDetails = response.body()
                        if (response.code() == 200) {
                            if (vehicleDetails != null) {
                                binding.apply {
                                    vehicleDetails.forEach {

                                        tvName.text = vehicleDetails[0].empName
                                        tvDepartment.text = vehicleDetails[0].department
                                        tvVehicaleNo.text = vehicleDetails[0].vehicleNumber
                                        tvValidity.text = vehicleDetails[0].validity
                                        tvCreatedBy.text = vehicleDetails[0].createdBy
                                        tvRegistration.text = vehicleDetails[0].registrationNo
                                        tvDrivingLincesNo.text = vehicleDetails[0].drivingLicenceNo
                                        tvVehicleName.text = vehicleDetails[0].vehicleName
                                        tvVehicleColor.text = vehicleDetails[0].vehicleColor

                                    }
                                }


                                if (vehicleDetails[0].isActive==0){
                                    binding.tvActivit.text = "Not Active"
                                } else{
                                    if (vehicleDetails[0].isActive==1){
                                        binding.tvActivit.text = "Active"
                                    }
                                }



                                val current = LocalDateTime.now()
                                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                val formatter = SimpleDateFormat("MM/dd/yyyy")
                                val output = formatter.format(parser.parse(current.toString()))
                                if (vehicleDetails[0].validity >= output) {
                                    binding.imTick.isVisible = true
                                    binding.imTick.setBackgroundResource(R.drawable.greenicon)
                                } else if (vehicleDetails[0].validity < output) {
                                    binding.imTick.setBackgroundResource(R.drawable.cross_icon)
                                    binding.imTick.isVisible = true
                                    alertDialog()
                                }


                                val vehicleImage = vehicleDetails[0].vehicleImage
                                val userImage = vehicleDetails[0].image

                                // used for user image
                                val imageBytes = android.util.Base64.decode(userImage, 0)
                                val img = convertCompressedByteArrayToBitmap(imageBytes)
                                binding.imEmpImg.setImageBitmap(img)



                                val vImageBytes = android.util.Base64.decode(vehicleImage, 0)
                                val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
                                binding.imVehicle.setImageBitmap(vImg)


                                // storing data in globle variable

                                depId = vehicleDetails[0].departMentId
                                getRfidNo = vehicleDetails[0].rfidTag

                                binding.radioGroup.isVisible = true


                            } else{
                                vibratePhone()
                                binding.tvActivit.isVisible = false
                            }

                        }


                    } catch (e:Exception){
                        binding.tvActivit.isVisible = false
                        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)

                        val toneGen2 = ToneGenerator(AudioManager.STREAM_ALARM, 500)
                        toneGen2.startTone(ToneGenerator.TONE_CDMA_ANSWER, 150)
                        Log.d("exception",e.toString())
                        vibratePhone()
                        dialogForTag()
                        val mColors = arrayOf(ColorDrawable(Color.GREEN), ColorDrawable(Color.RED))
                        val mTransition = TransitionDrawable(mColors)
                        binding.rootLayout.background = mTransition
                        mTransition.startTransition(2000)
                        binding.radioGroup.isVisible = false
                        manageVehicleAudit(binding.tvCreatedBy.text.toString(),"Rfid Tag does not exits",binding.tvRfid.text.toString(),0)

                    }
                }

                override fun onFailure(call: Call<VehicleIdentification>, t: Throwable) {
                    binding.progressBar.isVisible= false
                    Log.d("Error",t.localizedMessage)
                }
            })
    }








    @RequiresApi(Build.VERSION_CODES.O)
    fun ByteArray.toBase64(): String = String(Base64.getEncoder().encode(this))




    fun convertCompressedByteArrayToBitmap(src: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(src, 0, src.size)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode==KeyEvent.KEYCODE_BUTTON_R2 ) {
            if (isTwentySecoud==20){
                isTwentySecoud=0
                iuhfService.openDev()
                startTimer()
                Log.d("isTwentySecoud", isTwentySecoud.toString())
            } else{
//           Log.d("UhfService", iuhfService.openDev().toString())
//            iuhfService = UHFManager.getUHFService(this)
//            if (isOpening) {
////                startSearching()
//                lastKeyPressTime = System.currentTimeMillis()
//                reopenTimer?.cancel()

                cancelTimer()
                Log.d("isTwentySecoud", isTwentySecoud.toString())

                //Log.d("isTwentySecoud", isTwentySecoud.toString())
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
                    RfidNo = readHexString
                    //rfidLogger.logRfidNumber(RfidNo)


                } else {
                    stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(ErrorStatus.getErrorStatus(var1.status)).append("\n")
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
           // RfidNo = iuhfService.read_area(1, "2", "6", "00000000").toString()

            Toast.makeText(this,RfidNo, Toast.LENGTH_SHORT).show()
//            Log.d("ScannedRfid",RfidNo.toString())
            if (RfidNo.length<8){
                Toast.makeText(this@VehicleActivity,"Invalid Tag",Toast.LENGTH_SHORT).show()
            } else {
                binding.tvRfid.text = RfidNo


                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vehicleInfoViewModel.getUserImage(RfidNo)
                        bindUserImage1()

                    }

                    vehicleInfoViewModel.getVehicleImage(RfidNo)
                    bindVehicleImage1()

                    rfidLogger.logRfidNumber(RfidNo)
                    writeToFileExternal("ScannedLog.txt",RfidNo,LocalDateTime.now().toString())

                } catch (e: Exception) {
                    Log.d("Error", e.toString())
                }
            }

                return true
            }
//            } else {
//                 openDevice()
//            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()

            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onPause() {
        super.onPause()

        iuhfService.closeDev()
    }

    override fun onStop() {
        super.onStop()
        iuhfService.closeDev()
    }




    private fun alertDialog() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_alert)
        dialog.setCancelable(true)
        dialog.show()



        val stay_diag: MaterialButton? = dialog.findViewById(R.id.stay_diag)
        stay_diag?.setOnClickListener {
            dialog.dismiss()
        }

    }


    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Deprecated in API 26
            vibrator.vibrate(500)
        }
    }




    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun dialogForReject() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.layout_remark)
        dialog.setCancelable(false)
        dialog.show()


        val description:EditText = dialog.findViewById(R.id.et_description)
        val progressBar:ProgressBar = dialog.findViewById(R.id.progressBar_remark)
        val send:MaterialButton = dialog.findViewById(R.id.btn_send)



        progressBar.isVisible = false
        send.isVisible = true



        send.setOnClickListener {
            if (description.text.isNotEmpty()) {
                progressBar.isVisible = true
                send.isVisible = false
                val vehicleAudit = ManageVehicleAudit(1,binding.tvCreatedBy.text.toString(),description.text.toString(),binding.tvRfid.text.toString(),1)
                vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)
                dialog.dismiss()
                binding.btnReject.isEnabled = true
                binding.progressBarOk.isVisible=false
                binding.btnOk.isVisible = true
                binding.radioGroup.isVisible = false
                binding.tvActivit.isVisible = false
                binding.apply { imEmpImg.setImageDrawable(ContextCompat.getDrawable(
                    this@VehicleActivity,
                    R.drawable.priest
                )
                )
                    ll3.setBackgroundResource(R.drawable.bg_box)
                    imVehicle.setImageDrawable(
                        ContextCompat.getDrawable(this@VehicleActivity, R.drawable.car))
                    linearLayout.setBackgroundResource(R.drawable.bg_box)
                    imTick.isVisible = false
                    tvName.text = ""
                    tvDepartment.text = ""
                    tvVehicaleNo.text = ""
                    tvActivit.text = ""
                    tvValidity.text = ""
                    tvCreatedBy.text = ""
                    tvDrivingLincesNo.text = ""
                    tvVehicaleNo.text = ""
                    tvVehicleColor.text = ""
                    tvRegistration.text = ""
                    tvVehicleName.text = ""

                }
                //manageVehicleAudit()
                //manageVehicleAudit(binding.tvCreatedBy.text.toString(), description.text.toString(), RfidNo, 0)
            } else{
                Snackbar.make(binding.root,"Remark description is required",Snackbar.LENGTH_SHORT).show()
            }
        }

    }



    @SuppressLint("SetTextI18n")
    private fun dialogForTag() {
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.layout_tags_exites)
        dialogTag.setCancelable(true)
        dialogTag.show()


        val send:MaterialButton = dialogTag.findViewById(R.id.bt_ok)

        send.setOnClickListener {
            dialogTag.dismiss()
            val mColors = arrayOf(ColorDrawable(Color.WHITE),ColorDrawable(Color.WHITE))
            val mTransition = TransitionDrawable(mColors)
            binding.rootLayout.background = mTransition
            mTransition.startTransition(1000)

        }



    }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun manageVehicleAudit(createdBy:String, remark:String, RfidNo:String, Status:Int){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        val vehicleAudit = ManageVehicleAudit(1,createdBy,remark,binding.tvRfid.text.toString(),Status)
        RetrofitClient.getResponseFromApi().manageVehicleAuditWithoutList(vehicleAudit)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    dialog.dismiss()
                    binding.btnReject.isEnabled = true
                    binding.progressBarOk.isVisible=false
                    binding.btnOk.isVisible = true
                    binding.radioGroup.isVisible = false
                    Toast.makeText(this@VehicleActivity,response.body(),Toast.LENGTH_SHORT).show()
                    binding.apply { imEmpImg.setImageDrawable(ContextCompat.getDrawable(
                                this@VehicleActivity,
                                R.drawable.priest
                            )
                        )
                        ll3.setBackgroundResource(R.drawable.bg_box)
                        imVehicle.setImageDrawable(
                            ContextCompat.getDrawable(this@VehicleActivity, R.drawable.car))
                        linearLayout.setBackgroundResource(R.drawable.bg_box)
                        imTick.isVisible = false
                        tvName.text = ""
                        tvDepartment.text = ""
                        tvVehicaleNo.text = ""
                        tvActivit.text = ""
                        tvValidity.text = ""
                        tvCreatedBy.text = ""
                        tvDrivingLincesNo.text = ""
                        tvVehicaleNo.text = ""
                        tvVehicleColor.text = ""
                        tvRegistration.text = ""
                        tvVehicleName.text = ""

                    }

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@VehicleActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
                }

            })
    }




    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindItemFromRoomDb() {
     vehicleInfoViewModel.getResponseFromDb.observe(this, androidx.lifecycle.Observer {
         binding.progressBar.isVisible = false
         when (it) {
             is NetworkResult.Success->{
                 //iuhfService.closeDev()
//                 vehicleInfoViewModel.getVehicleImage(binding.tvRfid.text.toString())
//                 bindVehicleImage()
//                 vehicleInfoViewModel.getUserImage(binding.tvRfid.text.toString())
//                 bindUserImage()

                 try {
                     val toneGen1 = ToneGenerator(AudioManager.STREAM_RING, 100)
                     toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)
                     val mColors = arrayOf(ColorDrawable(Color.GREEN), ColorDrawable(Color.WHITE))
                     val mTransition = TransitionDrawable(mColors)
                     binding.rootLayout.background = mTransition
                     mTransition.startTransition(1000)
                 } catch (e:Exception){

                 }

                 binding.apply {
                    try {
                        if (it.data?.empName?.isNotEmpty() == true) {
                            tvName.text = it.data?.empName
                             tvDepartment.text = it.data.department
                            tvVehicaleNo.text = it.data?.vehicleNumber
                            tvValidity.text = it.data?.validity
                            tvCreatedBy.text = it.data?.createdBy
                            tvRegistration.text = it.data?.registrationNo
                            tvDrivingLincesNo.text = it.data?.drivingLicenceNo
                            tvVehicleName.text = it.data?.vehicleName
                            tvVehicleColor.text = it.data?.vehicleColor
                            RfidNo = it.data?.rfidTag.toString()
                            if (it.data?.isActive == 1) {
                                tvActivit.text = "Active"
                            } else {
                                tvActivit.text = "Not Active"
                                showAlertDialog()
                            }

                            val current = LocalDateTime.now()
                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            val formatter = SimpleDateFormat("MM/dd/yyyy")
                            val output = formatter.format(parser.parse(current.toString()))
                            if (it.data.validity >= output) {
                                binding.imTick.isVisible = true
                                binding.imTick.setBackgroundResource(R.drawable.greenicon)
                            } else if (it.data.validity < output) {
                                binding.imTick.setBackgroundResource(R.drawable.cross_icon)
                                binding.imTick.isVisible = true
                                alertDialog()
                            }


                            val vehicleImage = it.data.vehicleImage
                            val userImage = it.data.image

                            // used for user image
                            val imageBytes = android.util.Base64.decode(userImage, 0)
                            val img = convertCompressedByteArrayToBitmap(imageBytes)
                            binding.imEmpImg.setImageBitmap(img)



                            val vImageBytes = android.util.Base64.decode(vehicleImage, 0)
                            val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
                            binding.imVehicle.setImageBitmap(vImg)


                            // storing data in globle variable

                            depId = it.data.departMentId
                            getRfidNo = it.data.rfidTag

                            binding.radioGroup.isVisible = true







                        } else{
                            try {

                                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)

                                val toneGen2 = ToneGenerator(AudioManager.STREAM_ALARM, 500)
                                toneGen2.startTone(ToneGenerator.TONE_CDMA_ANSWER, 150)
                                vibratePhone()
                                dialogForTag()
                                val mColors = arrayOf(ColorDrawable(Color.TRANSPARENT), ColorDrawable(Color.RED))
                                val mTransition = TransitionDrawable(mColors)
                                binding.rootLayout.background = mTransition
                                mTransition.startTransition(2000)
                            } catch (e:Exception){
                                Log.d("Exce",e.toString())
                            }

                            val vehicleAudit = ManageVehicleAudit(0,binding.tvCreatedBy.text.toString(),"Rfid Tag does not exits",binding.tvRfid.text.toString(),0)
                            vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)
                            binding.radioGroup.isVisible = false

                        }

                    // binding.radioGroup.isVisible = true


                     } catch (e:Exception){

                        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)

                        val toneGen2 = ToneGenerator(AudioManager.STREAM_ALARM, 500)
                        toneGen2.startTone(ToneGenerator.TONE_CDMA_ANSWER, 150)
                        Log.d("exception",e.toString())
                        vibratePhone()
                        dialogForTag()
                        val mColors = arrayOf(ColorDrawable(Color.TRANSPARENT), ColorDrawable(Color.RED))
                        val mTransition = TransitionDrawable(mColors)
                        binding.rootLayout.background = mTransition
                        mTransition.startTransition(2000)
                        val vehicleAudit = ManageVehicleAudit(0,binding.tvCreatedBy.text.toString(),"Rfid Tag does not exits",binding.tvRfid.text.toString().toString(),0)
                        vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)
                    }
                 }
             }
             is NetworkResult.Error->{
                 Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()

             }
             is NetworkResult.Loading->{
                 binding.progressBar.isVisible = true
             }

         }
     })


    }


    private fun bindVehicleImage(){
        vehicleInfoViewModel.getVehicleImage.observe(this, androidx.lifecycle.Observer {
            when(it){
                is NetworkResult.Success->{
                    try {
                        if (it.data?.vehicleImage != null) {
                            val vImageBytes = android.util.Base64.decode(it.data.vehicleImage, 0)
                            val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
                            binding.imVehicle.setImageBitmap(vImg)
                        }
                    } catch (e:Exception){
                        Log.d("exception1",e.toString())

                    }
                }
                is NetworkResult.Error->{

                }
                is NetworkResult.Loading->{

                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindUserImage(){
        vehicleInfoViewModel.getUserImage.observe(this, androidx.lifecycle.Observer {
            when(it){
                is NetworkResult.Success->{

                        try {
                            val toneGen1 = ToneGenerator(AudioManager.STREAM_RING, 100)
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)
                            val mColors =
                                arrayOf(ColorDrawable(Color.GREEN), ColorDrawable(Color.WHITE))
                            val mTransition = TransitionDrawable(mColors)
                            binding.rootLayout.background = mTransition
                            mTransition.startTransition(1000)
                        } catch (e: Exception) {
                            Log.d("exception2",e.toString())
                        }

                        binding.apply {
                            try {
                                if (it.data?.userName?.isNotEmpty() == true) {
                                    tvName.text = it.data?.userName
                                    tvDepartment.text = it.data.departmentName
                                    tvVehicaleNo.text = it.data?.vehicleNumber
                                    val validity = it.data.validity
                                    tvValidity.text = validity
                                    tvCreatedBy.text = it.data?.createdBy
                                    tvRegistration.text = it.data?.RNo
                                    tvDrivingLincesNo.text = it.data?.dl
                                    tvVehicleName.text = it.data?.vehicleName
                                    tvVehicleColor.text = it.data?.vehicleColor
                                    RfidNo = it.data?.rfidTag.toString()
                                    val userImage = it.data.userImage
                                    val imageBytes = android.util.Base64.decode(userImage, 0)
                                    val img = convertCompressedByteArrayToBitmap(imageBytes)
                                    binding.imEmpImg.setImageBitmap(img)


                                    try {
                                        val validity =
                                            it.data.validity // Replace this with your actual validity date string
                                        val formatter = DateTimeFormatter.ofPattern(
                                            "M/d/yyyy h:mm:ss a",
                                            Locale.ENGLISH
                                        )
                                        val parsedDate = LocalDateTime.parse(validity, formatter)
                                        val current = LocalDate.now()
                                        if (current.isBefore(parsedDate.toLocalDate())) {
                                            binding.imTick.isVisible = true
                                            binding.imTick.setBackgroundResource(R.drawable.greenicon)
                                        } else {
                                            binding.imTick.setBackgroundResource(R.drawable.cross_icon)
                                            binding.imTick.isVisible = true
                                            alertDialog()
                                        }
                                    } catch (e: DateTimeParseException) {
                                        // Handle date parsing error
                                        Log.d("exception", e.toString())
                                        // Optionally, you can show an error message or handle the error in another way.
                                    }
                                    if (it.data?.Active == 1) {
                                        tvActivit.text = "Active"
                                        tvActivit.isVisible = true
                                        tvActivit.setBackgroundResource(R.drawable.active_bg)
                                    } else {
                                        tvActivit.text = "Blacklisted"
                                        tvActivit.isVisible = true
                                        tvActivit.setBackgroundResource(R.drawable.not_active_bg)
                                        showAlertDialog()
                                    }



                                    getRfidNo = it.data.rfidTag
                                    binding.radioGroup.isVisible = true
                                } else {
                                    try {
                                        binding.tvActivit.isVisible = false
                                        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)
                                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)

                                        val toneGen2 = ToneGenerator(AudioManager.STREAM_ALARM, 500)
                                        toneGen2.startTone(ToneGenerator.TONE_CDMA_ANSWER, 150)
                                        vibratePhone()
                                        dialogForTag()
                                        val mColors = arrayOf(
                                            ColorDrawable(Color.TRANSPARENT),
                                            ColorDrawable(Color.RED)
                                        )
                                        val mTransition = TransitionDrawable(mColors)
                                        binding.rootLayout.background = mTransition
                                        mTransition.startTransition(2000)
                                    } catch (e: Exception) {
                                        Log.d("Exce", e.toString())
                                    }

                                    val vehicleAudit = ManageVehicleAudit(
                                        0,
                                        binding.tvCreatedBy.text.toString(),
                                        "Rfid Tag does not exits",
                                        binding.tvRfid.text.toString(),
                                        0
                                    )
                                    vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)
                                    binding.radioGroup.isVisible = false

                                    // clearing data when rfid tag is not exits in table
                                    binding.apply {
                                        imEmpImg.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                this@VehicleActivity,
                                                R.drawable.priest
                                            )
                                        )
                                        ll3.setBackgroundResource(R.drawable.bg_box)
                                        imVehicle.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                this@VehicleActivity,
                                                R.drawable.car
                                            )
                                        )
                                        linearLayout.setBackgroundResource(R.drawable.bg_box)
                                        imTick.isVisible = false
                                        tvName.text = ""
                                        tvDepartment.text = ""
                                        tvVehicaleNo.text = ""
                                        tvActivit.text = ""
                                        tvValidity.text = ""
                                        tvCreatedBy.text = ""
                                        tvDrivingLincesNo.text = ""
                                        tvVehicaleNo.text = ""
                                        tvVehicleColor.text = ""
                                        tvVehicleName.text = ""
                                        tvRegistration.text = ""
                                        binding.radioGroup.isVisible = false

                                    }

                                }


                            } catch (e: Exception) {
                                Log.d("exception3",e.toString())
                                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)

                                val toneGen2 = ToneGenerator(AudioManager.STREAM_ALARM, 500)
                                toneGen2.startTone(ToneGenerator.TONE_CDMA_ANSWER, 150)
                                Log.d("exception", e.toString())
                                vibratePhone()
                                dialogForTag()
                                val mColors = arrayOf(
                                    ColorDrawable(Color.TRANSPARENT),
                                    ColorDrawable(Color.RED)
                                )
                                val mTransition = TransitionDrawable(mColors)
                                binding.rootLayout.background = mTransition
                                mTransition.startTransition(2000)
                                val vehicleAudit = ManageVehicleAudit(
                                    0,
                                    binding.tvCreatedBy.text.toString(),
                                    "Rfid Tag does not exits",
                                    binding.tvRfid.text.toString().toString(),
                                    0
                                )
                                vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)
                            }
                        }
                    }


                is NetworkResult.Error->{

                }
                is NetworkResult.Loading->{

                }
            }
        })
    }


    override fun onBackPressed() {
        super.onBackPressed()
       // iuhfService.closeDev()
        val i = Intent(applicationContext, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

//    private fun fetchVehicleImage(RfidNo: String) {
//        Thread {
//            val myEntity = vehicleDao.getAllVehicleImage(binding.tvRfid.text.toString())
//            val vImageBytes = android.util.Base64.decode(myEntity.vehicleImage, 0)
//            val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
//            binding.imVehicle.setImageBitmap(vImg)
//
//        }.start()
//    }


    fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)

        // Set the custom title layout
        val inflater = layoutInflater
        val customTitleView = inflater.inflate(R.layout.layout_alert, null)
        builder.setCustomTitle(customTitleView)

        // Set the dialog message
        builder.setMessage("This vehicle has been blacklisted.")

        // Add buttons and their actions
        builder.setPositiveButton("OK") { dialog, _ ->
            // Handle the positive button click (e.g., perform an action)
            // Dismiss the dialog if necessary: dialog.dismiss()
        }

//        builder.setNegativeButton("Cancel") { dialog, _ ->
//            // Handle the negative button click (e.g., cancel the operation)
//            // Dismiss the dialog if necessary: dialog.dismiss()
//        }

        // Create and show the dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }



    private fun bindVehicleImage1() {
        vehicleInfoViewModel.getVehicleImage.observe(this, androidx.lifecycle.Observer {
            when(it){
                is NetworkResult.Success->{
                    handleVehicleImageSuccess(it.data)
                }
                is NetworkResult.Error->{

                }
                is NetworkResult.Loading->{

                }
            }
        })
    }

    private fun handleVehicleImageSuccess(data: TempList?) {
        try {
            data?.vehicleImage?.let { image ->
                val vImageBytes = android.util.Base64.decode(image, 0)
                val vImg = convertCompressedByteArrayToBitmap(vImageBytes)
                binding.imVehicle.setImageBitmap(vImg)
            }
        } catch (e: Exception) {
            Log.e("exception1", "Error processing vehicle image", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindUserImage1() {
        vehicleInfoViewModel.getUserImage.observe(this, androidx.lifecycle.Observer { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data?.let {
                        handleUserImageSuccess(it)

                    }

                }
                is NetworkResult.Error -> {

                }
                is NetworkResult.Loading -> {

                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleUserImageSuccess(data: UserImage) {
        try {
            val toneGen1 = ToneGenerator(AudioManager.STREAM_RING, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)

            val mColors = arrayOf(ColorDrawable(Color.GREEN), ColorDrawable(Color.WHITE))
            val mTransition = TransitionDrawable(mColors)
            binding.rootLayout.background = mTransition
            mTransition.startTransition(1000)

            binding.apply {
                if (data?.userName?.isNotEmpty() == true) {
                    updateUIWithData(data)
                } else {
                    handleEmptyUserData()
                }
            }
        } catch (e: Exception) {
            Log.e("exception2", "Error processing user image", e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUIWithData(data: UserImage) {
        binding.tvName.text = data.userName
        binding.tvDepartment.text = data.departmentName
        binding.tvVehicaleNo.text = data?.vehicleNumber
        val validity = data.validity
        binding.tvValidity.text = validity
        binding.tvCreatedBy.text = data?.createdBy
        binding.tvRegistration.text = data?.RNo
        binding.tvDrivingLincesNo.text = data?.dl
        binding.tvVehicleName.text = data?.vehicleName
        binding.tvVehicleColor.text = data?.vehicleColor
        RfidNo = data?.rfidTag.toString()

        val userImage = data.userImage
        val imageBytes = android.util.Base64.decode(userImage, 0)
        val img = convertCompressedByteArrayToBitmap(imageBytes)
        binding.imEmpImg.setImageBitmap(img)

        // ... (other UI updates)
        writeToFileExternal("VarifiedVehicle.txt",RfidNo,LocalDateTime.now().toString())


        try {
            val validity = data.validity
            val formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.ENGLISH)
            val parsedDate = LocalDateTime.parse(validity, formatter)
            val current = LocalDate.now()

            if (current.isBefore(parsedDate.toLocalDate())) {
                binding.imTick.isVisible = true
                binding.imTick.setBackgroundResource(R.drawable.greenicon)
            } else {
                binding.imTick.setBackgroundResource(R.drawable.cross_icon)
                binding.imTick.isVisible = true
                alertDialog()
            }
        } catch (e: DateTimeParseException) {
            Log.e("exception", e.toString())
        }

        if (data.Active == 1) {
            binding.tvActivit.text = "Active"
            binding.tvActivit.isVisible = true
            binding.tvActivit.setBackgroundResource(R.drawable.active_bg)
        } else {
            binding.tvActivit.text = "Blacklisted"
            binding.tvActivit.isVisible = true
            binding.tvActivit.setBackgroundResource(R.drawable.not_active_bg)
            showAlertDialog()
        }

        binding.tvRfid.text = data.rfidTag
        binding.radioGroup.isVisible = true
    }

    private fun handleEmptyUserData() {
        binding.tvActivit.isVisible = false
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)

        val toneGen2 = ToneGenerator(AudioManager.STREAM_ALARM, 500)
        toneGen2.startTone(ToneGenerator.TONE_CDMA_ANSWER, 150)

        vibratePhone()
        dialogForTag()

        val mColors = arrayOf(ColorDrawable(Color.TRANSPARENT), ColorDrawable(Color.RED))
        val mTransition = TransitionDrawable(mColors)
        binding.rootLayout.background = mTransition
        mTransition.startTransition(2000)

        val vehicleAudit = ManageVehicleAudit(
            0,
            binding.tvCreatedBy.text.toString(),
            "Rfid Tag does not exist",
            binding.tvRfid.text.toString(),
            0
        )
        vehicleInfoViewModel.mangeVehicleAuditDB(vehicleAudit)

        binding.radioGroup.isVisible = false

        binding.apply {
            imEmpImg.setImageDrawable(ContextCompat.getDrawable(this@VehicleActivity, R.drawable.priest))
            ll3.setBackgroundResource(R.drawable.bg_box)
            radioGroup.isVisible = false
        }
    }

    private fun handleError(exception: Exception?) {
        // Handle error, e.g., show an error message or log the exception
    }

    private fun handleLoading() {
        // Handle loading if needed
    }


@SuppressLint("ResourceAsColor")
fun stopSearching(){
    isInventoryRunning = false
    iuhfService.inventoryStop()
    iuhfService.closeDev()
}


@SuppressLint("ResourceAsColor")
fun  startSearching(){
    isInventoryRunning = true

    try {
        iuhfService = UHFManager.getUHFService(this)
        iuhfService.openDev()
        iuhfService.antennaPower = 15


    } catch (e:Exception){
        Log.d("Exception",e.toString())
    }

    iuhfService.inventoryStart()
}



    fun writeToFileExternal(fileName: String, data: String, time: String) {
        try {
            val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(externalDir, fileName)

            // Use BufferedWriter for better performance
            val bufferedWriter = BufferedWriter(FileWriter(file, true))

            // No need to check file length for duplicates

            // Write the data
            bufferedWriter.write(time)
            bufferedWriter.write(" ")
            bufferedWriter.write(data)
            bufferedWriter.newLine() // Use newLine() for appending a newline
            bufferedWriter.flush() // Flush to ensure the data is written immediately

            // Close the writer
            bufferedWriter.close()
        } catch (e: Exception) {
            Log.d("exception", e.toString())
            e.printStackTrace()
        }
    }











//            iuhfService.setOnReadListener { var1 ->
//                val stringBuilder = StringBuilder()
//                val epcData = var1.epcData
//                val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
//                if (!TextUtils.isEmpty(hexString)) {
//                    stringBuilder.append("EPC：").append(hexString).append("\n")
//                }
//                if (var1.status == 0) {
//                    val readData = var1.readData
//                    val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
//
//                    stringBuilder.append("ReadData:").append(readHexString).append("\n")
//                    Toast.makeText(this,readHexString,Toast.LENGTH_SHORT).show()
//                } else {
//                    stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(ErrorStatus.getErrorStatus(var1.status)).append("\n")
//                }
//                handler.sendMessage(handler.obtainMessage(1, stringBuilder))
//            }
//            val readArea = iuhfService.readArea(1, 2, 6, "00000000")
//            if (readArea != 0) {
//                val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(readArea) + "\n"
//                handler.sendMessage(handler.obtainMessage(1, err))
//            }







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
        iuhfService.closeDev()
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


    fun handleUIData(var1:SpdInventoryData): Boolean {
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
                RfidNo = readHexString
                //rfidLogger.logRfidNumber(RfidNo)


            } else {
                stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(ErrorStatus.getErrorStatus(var1.status)).append("\n")
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


        Toast.makeText(this,RfidNo, Toast.LENGTH_SHORT).show()
//            Log.d("ScannedRfid",RfidNo.toString())
        if (RfidNo.length<8){
            Toast.makeText(this@VehicleActivity,"Invalid Tag",Toast.LENGTH_SHORT).show()
        } else {
            binding.tvRfid.text = RfidNo


            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vehicleInfoViewModel.getUserImage(RfidNo)
                    bindUserImage1()

                }

                vehicleInfoViewModel.getVehicleImage(RfidNo)
                bindVehicleImage1()

                rfidLogger.logRfidNumber(RfidNo)
                writeToFileExternal("ScannedLog.txt",RfidNo,LocalDateTime.now().toString())

            } catch (e: Exception) {
                Log.d("Error", e.toString())
            }
        }

        return true
    }

    }

