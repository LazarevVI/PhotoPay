package com.example.photopay

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class SendMessage : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etAmount: EditText
    private lateinit var transferBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        etPhone = findViewById(R.id.recipientPhone)
        etAmount = findViewById(R.id.transferAmount)
        transferBtn = findViewById(R.id.transferBtn)

        transferBtn.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this@SendMessage, android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED){
                sendMessage()
            }
        }


    }

    private fun sendMessage() {
        val sPhone = etPhone.text.toString().trim()
        val sAmount = etAmount.text.toString().trim()
        val sMessage = "Перевод " + sPhone + " " + sAmount
        etAmount.setText(sMessage)
//        try {
//            val smsManager:SmsManager
//            if (Build.VERSION.SDK_INT>=23) {
//                smsManager = this.getSystemService(SmsManager::class.java)
//            }
//            else{
//                smsManager = SmsManager.getDefault()
//            }
//
//            smsManager.sendTextMessage(sPhone, null, sMessage, null, null)
//
//            Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
//
//        } catch (e: Exception) {
//
//            Toast.makeText(applicationContext, "Please enter all the data.."+e.message.toString(), Toast.LENGTH_LONG)
//                .show()
//        }


    }
}