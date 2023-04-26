package com.example.hpcl



import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.hpcl.acitity.RegistrationActivity
import com.example.hpcl.databinding.ActivityMainBinding
import com.example.hpcl.identification.VehicleActivity
import com.example.hpcl.retrofit.PassRfid
import com.example.hpcl.setting.SettingActivity
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager


class MainActivity: AppCompatActivity() {
    lateinit var iuhfService: IUHFService
    lateinit var binding: ActivityMainBinding
    var rfidNo = ""
    var passRfid: PassRfid? = null
    private var pressedTime: Long = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        }
        pressedTime = System.currentTimeMillis()
    }

}