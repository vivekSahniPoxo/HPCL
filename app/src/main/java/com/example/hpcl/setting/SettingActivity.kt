package com.example.hpcl.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hpcl.MainActivity
import com.example.hpcl.databinding.ActivitySettingBinding
import com.example.hpcl.utils.Cons

class SettingActivity : AppCompatActivity() {
  private lateinit var binding:ActivitySettingBinding
    var updateBaseUrl:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
           binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

      binding.buttonSubmitUrl.setOnClickListener {
          updateBaseUrl =  binding.baseUrlConfig.text.toString()
        Cons.BASE_URL = "http://$updateBaseUrl"
        binding.ipconfigForm.visibility = View.GONE
        Toast.makeText(this@SettingActivity,  Cons.BASE_URL, Toast.LENGTH_SHORT).show()
        //Toast.makeText(SettingActivity.this,UpdateBaseUrl,Toast.LENGTH_SHORT).show();
          val i  = Intent(this,MainActivity::class.java)
          startActivity(i)
      }

        binding.ipconfig.setOnClickListener(View.OnClickListener {
            binding.ipconfigForm.visibility = View.VISIBLE
        })


    }
}