package com.example.hpcl

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.hpcl.databinding.ActivitySplashScreenBinding
import com.example.hpcl.identification.VehicleActivity
import com.example.hpcl.retrofit.RetrofitClient
import com.example.hpcl.sharePreference.SharePref
import com.example.hpcl.user_action.LoginActivity
import com.example.hpcl.user_action.data.RefreshToken
import com.example.hpcl.utils.App
import com.example.hpcl.utils.Cons
import com.example.hpcl.utils.InternetConnectionDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class SplashScreen : AppCompatActivity() {

    lateinit var sharePref: SharePref
    lateinit var binding: ActivitySplashScreenBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharePref =  SharePref()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.imSyncProgress.isVisible = true
        refreshToken()

//        Handler(Looper.getMainLooper()).postDelayed({
//        val sharedpreferences = getSharedPreferences("prefname", Context.MODE_PRIVATE)
//        if (sharedpreferences.getLong(Cons.TOKEN_EXPIRE_TIME, -1) > System.currentTimeMillis() && sharePref.getData(Cons.USER_ROLE).toString()=="Admin") {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }  else if (sharedpreferences.getLong(Cons.TOKEN_EXPIRE_TIME, -1) > System.currentTimeMillis()){
//            val intent = Intent(this, VehicleActivity::class.java)
//            startActivity(intent)
//            finish()
//
//        } else {
//            sharePref.clearAll()
//            sharePref.logOut()
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        }, 3000)


//        val locle_time = LocalDateTime.now()
//        if (sharePref.getData(Cons.TOKEN_EXPIRE_TIME)!! <= locle_time.toString() && sharePref.getData(Cons.USER_ROLE)=="Admin"){
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else if (sharePref.getData(Cons.TOKEN_EXPIRE_TIME)!! <= locle_time.toString()){
//            val intent = Intent(this, VehicleActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else if (sharePref.getData(Cons.TOKEN_EXPIRE_TIME)!! > locle_time.toString()){
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        //refreshToken()



//        Handler(Looper.getMainLooper()).postDelayed({
//            if (sharePref.getData(Cons.ACCESSTOKEN).toString().isEmpty()) {
//                val intent = Intent(this, LoginActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else if (sharePref.getData(Cons.ACCESSTOKEN).toString().isNotEmpty() && sharePref.getData(Cons.USER_ROLE).toString()=="Admin"){
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else if (sharePref.getData(Cons.ACCESSTOKEN).toString().isNotEmpty()){
//                val intent = Intent(this, VehicleActivity::class.java)
//                startActivity(intent)
//                finish()
//
//            }
//        }, 3000)

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun  refreshToken(){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        val refreshToken = RefreshToken(sharePref.getData(Cons.ACCESSTOKEN).toString(),sharePref.getData(Cons.REFRESHTOKEN).toString())
        RetrofitClient.getResponseFromApi().refreshToken(refreshToken).enqueue(object : Callback<RefreshToken> {
            override fun onResponse(call: Call<RefreshToken>, response: Response<RefreshToken>) {
                try {
                    binding.imSyncProgress.isVisible = false
                    sharePref.saveData(Cons.ACCESSTOKEN, response.body()?.accessToken.toString())
                    sharePref.saveData(Cons.REFRESHTOKEN, response.body()?.refreshToken.toString())

                    val sharedpreferences = getSharedPreferences("prefname", Context.MODE_PRIVATE)
                    if (sharedpreferences.getLong(Cons.TOKEN_EXPIRE_TIME, -1) > System.currentTimeMillis() && sharePref.getData(Cons.USER_ROLE).toString()=="Admin") {
                        val intent = Intent(this@SplashScreen, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }  else if (sharedpreferences.getLong(Cons.TOKEN_EXPIRE_TIME, -1) > System.currentTimeMillis()){
                        val intent = Intent(this@SplashScreen, VehicleActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        sharePref.clearAll()
                        sharePref. logOut()
                        val intent = Intent(this@SplashScreen, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                } catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<RefreshToken>, t: Throwable) {
                binding.imSyncProgress.isVisible = false
                Toast.makeText(this@SplashScreen,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }
}