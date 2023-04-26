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
import android.os.*
import android.speech.RecognizerIntent
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.hpcl.R
import com.example.hpcl.databinding.ActivityVehicleBinding
import com.example.hpcl.identification.data.ManageVehicleAudit
import com.example.hpcl.identification.data.VehicleIdentification
import com.example.hpcl.registration.RegistrationDataModel
import com.example.hpcl.retrofit.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class VehicleActivity : AppCompatActivity() {
    lateinit var binding:ActivityVehicleBinding
    lateinit var iuhfService: IUHFService
    lateinit var dialog:Dialog
    var RfidNo: String = ""
    var depId = 0
    var isVehicleActive = 0
    var getRfidNo = ""

    // Languages included
    var languages = arrayOf(
        "English", "Tamil", "Hindi", "Spanish", "French",
        "Arabic", "Chinese", "Japanese", "German"
    )

    // Language codes
    var lCodes = arrayOf(
        "en-US", "ta-IN", "hi-IN", "es-CL", "fr-FR",
        "ar-SA", "zh-TW", "jp-JP", "de-DE","raj_IN")


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)






        Thread {
            Handler(Looper.getMainLooper()).post {
                iuhfService = UHFManager.getUHFService(this)
                iuhfService.openDev()
                iuhfService.antennaPower = 30
                iuhfService.inventoryStart()

            }

        }.start()


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
            }
        }


        binding.btnReject.setOnClickListener {
//            binding.btnOk.isEnabled = false
//            binding.progressBarReject.isVisible=true
//            binding.btnReject.isVisible = false
//            isVehicleActive = 0
            dialogForReject()

//            if (binding.tvName.text.isEmpty()){
//                Snackbar.make(binding.root,"Please scan the tag",Snackbar.LENGTH_SHORT).show()
//            } else {
//                dialogForReject()
//            }
        }


        binding.btnOk.setOnClickListener {
            binding.btnReject.isEnabled = false
            binding.progressBarOk.isVisible=true
            binding.btnOk.isVisible = false
           // isVehicleActive = 1
            //registrationVehicle()
            manageVehicleAudit(binding.tvCreatedBy.text.toString(),"",RfidNo,1)
        }

    }



    private fun  getVehicleDetails(rfid:String) {
        RetrofitClient.getResponseFromApi().getVehicleDetails(rfid)
            .enqueue(object : Callback<VehicleIdentification> {
                @SuppressLint("SimpleDateFormat")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<VehicleIdentification>, response: Response<VehicleIdentification>) {
                    binding.progressBar.isVisible = false
                    try {
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
                            }

                        }


                    } catch (e:Exception){
                        Log.d("exception",e.toString())
                        vibratePhone()
                        val mColors = arrayOf(ColorDrawable(Color.GREEN), ColorDrawable(Color.RED))
                        val mTransition = TransitionDrawable(mColors)
                        binding.rootLayout.background = mTransition
                        mTransition.startTransition(2000)
                        manageVehicleAudit(binding.tvCreatedBy.text.toString(),"Rfid Tag does not exits",RfidNo,0)
                        dialogForTag()


                    }
                }

                override fun onFailure(call: Call<VehicleIdentification>, t: Throwable) {
                    binding.progressBar.isVisible= false
                    Log.d("Error",t.localizedMessage)
                }
            })
    }




//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun registrationVehicle(){
//
//        // Converting user image into byteArray
//        val bitmap = (binding.imEmpImg.drawable as BitmapDrawable).bitmap
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
//        val image = stream.toByteArray()
//
//        // Converting vehicle image into byteArray
//        val bitmapVehicle = (binding.imVehicle.drawable as BitmapDrawable).bitmap
//        val streamVehicle = ByteArrayOutputStream()
//        bitmapVehicle.compress(Bitmap.CompressFormat.PNG, 90, streamVehicle)
//        val vehicleImage = stream.toByteArray()
//
//        val registrationItem = RegistrationDataModel(depId,binding.tvName.text.toString(),image.toBase64(),isVehicleActive,getRfidNo,binding.tvValidity.text.toString(),vehicleImage.toBase64(),binding.tvVehicaleNo.text.toString())
//        RetrofitClient.getResponseFromApi().postRequest(registrationItem).enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                binding.progressBarOk.isVisible = false
//                binding.progressBarReject.isVisible = false
//                binding.btnReject.isEnabled = true
//                binding.btnOk.isEnabled = true
//                binding.btnOk.isVisible = true
//                binding.btnReject.isVisible=true
//                Toast.makeText(this@VehicleActivity,response.body(),Toast.LENGTH_SHORT).show()
//
//                // clearing displayed items after successfully response
//                binding.apply {
//                    imEmpImg.setImageDrawable(ContextCompat.getDrawable(this@VehicleActivity, R.drawable.priest))
//                    ll3.setBackgroundResource(R.drawable.bg_box)
//                    imVehicle.setImageDrawable(ContextCompat.getDrawable(this@VehicleActivity, R.drawable.car))
//                    linearLayout.setBackgroundResource(R.drawable.bg_box)
//                    imTick.isVisible = false
//                    tvName.text = ""
//                    tvDepartment.text = ""
//                    tvVehicaleNo.text = ""
//                    tvActivit.text = ""
//                    tvValidity.text =""
//                }
//
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                binding.progressBarOk.isVisible = false
//                binding.progressBarReject.isVisible = false
//                binding.btnReject.isEnabled = true
//                binding.btnOk.isEnabled = true
//                binding.btnOk.isVisible = true
//                binding.btnReject.isVisible=true
//                Log.d("Error",t.localizedMessage)
//                Toast.makeText(this@VehicleActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
//            }
//
//        })
//
//    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun ByteArray.toBase64(): String = String(Base64.getEncoder().encode(this))




    fun convertCompressedByteArrayToBitmap(src: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(src, 0, src.size)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1) {
            RfidNo = iuhfService.read_area(1, "2", "6", "00000000").toString()

            Toast.makeText(this,RfidNo, Toast.LENGTH_SHORT).show()
            Log.d("ScannedRfid",RfidNo.toString())
            binding.tvRfid.text = RfidNo
            try {
                binding.progressBar.isVisible= true
                getVehicleDetails(RfidNo)

            } catch (e:Exception){
                Log.d("Error",e.toString())
            }
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


    fun Activity.vibratePhone() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }



    @SuppressLint("SetTextI18n")
    private fun dialogForReject() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.layout_remark)
        dialog.setCancelable(true)
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
                manageVehicleAudit(
                    binding.tvCreatedBy.text.toString(),
                    description.text.toString(),
                    RfidNo,
                    0
                )
            } else{
                Snackbar.make(binding.root,"Remark description is required",Snackbar.LENGTH_SHORT).show()
            }
        }

    }



    @SuppressLint("SetTextI18n")
    private fun dialogForTag() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.layout_tags_exites)
        dialog.setCancelable(true)
        dialog.show()


        val send:MaterialButton = dialog.findViewById(R.id.bt_ok)

        send.setOnClickListener {
           dialog.dismiss()

        }



    }




    private fun manageVehicleAudit(createdBy:String,remark:String,RfidNo:String,Status:Int){
        val vehicleAudit = ManageVehicleAudit(createdBy,remark,RfidNo,Status)
        RetrofitClient.getResponseFromApi().manageVehicleAudit(vehicleAudit)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    dialog.dismiss()
                    binding.btnReject.isEnabled = true
                    binding.progressBarOk.isVisible=false
                    binding.btnOk.isVisible = true
                    Toast.makeText(this@VehicleActivity,response.body(),Toast.LENGTH_SHORT).show()

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@VehicleActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
                }

            })
    }






}