package com.example.photopay

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class SendMessage : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etAmount: EditText
    private lateinit var transferBtn: Button

    private lateinit var smsPermissions: Array<String>

    private companion object{
        private const val SMS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        var phoneNum:String? = intent.getStringExtra("PhoneNum")

        smsPermissions = arrayOf(android.Manifest.permission.SEND_SMS)

        etPhone = findViewById(R.id.recipientPhone)
        etPhone.setText(phoneNum)

        etAmount = findViewById(R.id.transferAmount)
        transferBtn = findViewById(R.id.transferBtn)

        transferBtn.setOnClickListener{
            Log.d("usersms", "Button clicked")
            if (checkSMSPermissions()){
                Log.d("usersms", "Checked permissions")
                sendMessage()
            }
            else{
                requestSMSPermissions()
            }
        }
    }

    private fun sendMessage() {

        val sPhone = etPhone.text.toString().trim()
        val sAmount = etAmount.text.toString().trim()
        val sMessage = "Перевод " + sPhone + " " + sAmount
        etAmount.setText(sMessage)
        try {
            val smsManager:SmsManager
            if (Build.VERSION.SDK_INT>=23) {
                smsManager = this.getSystemService(SmsManager::class.java)
            }
            else{
                smsManager = SmsManager.getDefault()
            }

            smsManager.sendTextMessage("900", null, sMessage, null, null)

            Toast.makeText(applicationContext, "Message sent", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {

            Toast.makeText(applicationContext, "Please enter all the data.."+e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }

    }

    private fun checkSMSPermissions():Boolean{
        val smsResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        return smsResult
    }

    private fun requestSMSPermissions(){
        ActivityCompat.requestPermissions(this, smsPermissions,
            SendMessage.SMS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            SendMessage.SMS_REQUEST_CODE -> {
                Log.d("usersms", "Checking permissions")
                if (grantResults.isNotEmpty()){

                    val smsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (smsAccepted) {
                        sendMessage()
                    }
                    else{
                        showToast("SMS permissions requiered")
                    }
                }
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}