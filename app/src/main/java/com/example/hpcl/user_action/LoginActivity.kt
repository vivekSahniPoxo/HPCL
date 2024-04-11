package com.example.hpcl.user_action

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.hpcl.MainActivity
import com.example.hpcl.databinding.ActivityLoginBinding
import com.example.hpcl.identification.VehicleActivity
import com.example.hpcl.retrofit.RetrofitClient
import com.example.hpcl.sharePreference.SharePref
import com.example.hpcl.user_action.data.GetUserType
import com.example.hpcl.user_action.data.LoginDataModel
import com.example.hpcl.user_action.data.LoginResponsedataModel
import com.example.hpcl.utils.App
import com.example.hpcl.utils.Cons
import com.example.hpcl.utils.InternetConnectionDialog
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.experimental.and


class LoginActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginBinding
    lateinit var sharedPreferences: SharePref

    var userTypeList = arrayListOf<String>()
    var selected = 0

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userTypeList.add("Choose user role")
        sharedPreferences = SharePref()


//        val wifiManager =
//            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo = wifiManager.connectionInfo
//        val macAddress = wifiInfo.macAddress
//        Toast.makeText(this,macAddress,Toast.LENGTH_SHORT).show()

        selectUserType()
        getUserType()

        binding.btnLogin.setOnClickListener {
            if (valid()){
                binding.btnLogin.isVisible = false
                binding.imSyncProgress.isVisible = true
                userAction()

            }

        }

    }


    private fun selectUserType(){

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, androidx.appcompat.R.layout.select_dialog_item_material, userTypeList ) {
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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                parent.getItemAtPosition(position).toString()
                if (parent.getItemAtPosition(position).toString() != "Choose user role") {

                    selected = binding.spType.getItemIdAtPosition(position).toInt()
                    //Toast.makeText(this@LoginActivity, "position" + binding.spType.getItemIdAtPosition(position), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getUserType(){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getUserType().enqueue(object:Callback<GetUserType>{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<GetUserType>, response: Response<GetUserType>) {

                response.body()?.forEach {
                    try {

                        userTypeList.add(it.userType)



                    } catch (e:Exception){
                        Log.d("exception",e.toString())
                    }
                }


            }

            override fun onFailure(call: Call<GetUserType>, t: Throwable) {
                Toast.makeText(this@LoginActivity,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })

    }


    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun userAction() {

        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        val userData = LoginDataModel(binding.etPassword.text.toString(),binding.etUserId.text.toString(),selected)
            RetrofitClient.getResponseFromApi().userLogin(userData).enqueue(object : Callback<LoginResponsedataModel> {
            override fun onResponse(call: Call<LoginResponsedataModel>, response: Response<LoginResponsedataModel>) {
               // Toast.makeText(this@LoginActivity,response.body().toString(), Toast.LENGTH_SHORT).show()
                binding.btnLogin.isVisible = true
                binding.imSyncProgress.isVisible = false
                try {
                    if (response.code()==401){
                        Toast.makeText(this@LoginActivity,"Invalid user",Toast.LENGTH_SHORT).show()
                    } else if (binding.spType.selectedItem.toString().trim { it <= ' ' } == "Admin" && response.body().toString().isNotEmpty()) {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            sharedPreferences.saveData(
                                Cons.ACCESSTOKEN,
                                response.body()?.token.toString()
                            )
                            sharedPreferences.saveData(
                                Cons.REFRESHTOKEN,
                                response.body()?.refreshToken.toString()
                            )
                            sharedPreferences.saveData(
                                Cons.USER_ROLE,
                                response.body()?.userRole.toString()
                            )
                            sharedPreferences.expireTime(
                                Cons.TOKEN_EXPIRE_TIME,
                                        System.currentTimeMillis() + TimeUnit.HOURS.toMillis(4)
                            )
                       // System.currentTimeMillis() + TimeUnit.HOURS.toMillis(30)
                        } else {
                            val intent = Intent(this@LoginActivity, VehicleActivity::class.java)
                            startActivity(intent)
                            finish()
                            sharedPreferences.saveData(
                                Cons.ACCESSTOKEN,
                                response.body()?.token.toString()
                            )
                            sharedPreferences.saveData(
                                Cons.REFRESHTOKEN,
                                response.body()?.refreshToken.toString()
                            )
                            sharedPreferences.saveData(
                                Cons.USER_ROLE,
                                response.body()?.userRole.toString()
                            )
                            sharedPreferences.expireTime(
                                Cons.TOKEN_EXPIRE_TIME,
                                System.currentTimeMillis() + TimeUnit.HOURS.toMillis(4)
                            )
                       // System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)
                    }
                } catch (e:Exception){
                    Toast.makeText(this@LoginActivity,"Invalid user",Toast.LENGTH_SHORT).show()
                }
                    }



            override fun onFailure(call: Call<LoginResponsedataModel>, t: Throwable) {
                Toast.makeText(this@LoginActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                binding.btnLogin.isVisible = true
                binding.imSyncProgress.isVisible = false
            }

        })
    }

    private fun valid():Boolean{
        if (binding.spType.selectedItem.toString().trim { it <= ' ' } == "Choose user role") {
            Snackbar.make(binding.root, "Choose user role", Snackbar.LENGTH_SHORT).show()
            return false
        }else if (binding.etUserId.text.toString().isEmpty()){
            binding.etUserId.error = "UserID is required"
            return false
        }  else if (binding.etPassword.text.toString().isEmpty()){
            binding.etPassword.error = "Password is required"
            return false
        }

        return true
    }


    fun getMacAddress(): String {
        try {
            val networkInterfaceList: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            var stringMac = ""
            for (networkInterface in networkInterfaceList) {
                if (networkInterface.name.equals("wlon0"));
                run {
                    for (i in 0 until networkInterface.hardwareAddress.size) {
                        var stringMacByte = Integer.toHexString(
                            (networkInterface.hardwareAddress[i] and 0xFF.toByte()).toInt()
                        )
                        if (stringMacByte.length == 1) {
                            stringMacByte = "0$stringMacByte"
                        }
                        stringMac =
                            stringMac + stringMacByte.uppercase(Locale.getDefault()) + ":"

                    }
                    //break
                    Toast.makeText(this@LoginActivity,stringMac,Toast.LENGTH_SHORT).show()
                }
            }
            //Toast.makeText(this@LoginActivity,stringMac,Toast.LENGTH_SHORT).show()
            return stringMac
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }


}