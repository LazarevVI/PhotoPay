package com.example.photopay

import android.R.attr
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.phone.SmsRetriever
import java.util.regex.Pattern


class SendMessage : AppCompatActivity() {
    private val REQ_USER_CONSENT = 200
    var smsBroadcastReceiver:SmsBroadcastReceiver? = null
    var etOtp:EditText? = null

    private lateinit var etPhone: EditText
    private lateinit var etAmount: EditText
    private lateinit var transferBtn: Button
    private lateinit var confirmBtn: Button

    private lateinit var smsSendPermissions: Array<String>
    private lateinit var progressDialog: ProgressDialog

    private companion object{
        private const val SMS_REQUEST_CODE = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        var sMessage:String = ""

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        etOtp = findViewById(R.id.etOtp)

        startSmartUserConsent()

        var phoneNum:String? = intent.getStringExtra("PhoneNum")!!.trim()

        if (phoneNum != null) {
            if (phoneNum.length == 11){
                phoneNum = phoneNum.substring(1)
            }

                smsSendPermissions = arrayOf(android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS)
        }

        etPhone = findViewById(R.id.recipientPhone)
        etPhone.setText(phoneNum)

        etAmount = findViewById(R.id.transferAmount)
        transferBtn = findViewById(R.id.transferBtn)
        confirmBtn = findViewById(R.id.confirmBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        confirmBtn.setOnClickListener{
            sMessage = etOtp!!.text.toString()
                  sendMessage(sMessage)
        }

        transferBtn.setOnClickListener{
            Log.d("usersms", "Button clicked")

            if (checkSMSPermissions()){
                Log.d("usersms", "Checked permissions")
                val sPhone = etPhone.text.toString().trim()
                val sAmount = etAmount.text.toString().trim()
                Log.d("samount", sAmount.isEmpty().toString())
                if (sPhone.isNotEmpty() && sAmount.isNotEmpty()){
                    sMessage = "Перевод " + sPhone + " " + sAmount
                    sendMessage(sMessage)
                }
                else {
                    Toast.makeText(applicationContext, "Please enter all data...", Toast.LENGTH_LONG)
                        .show()
                }


            }
            else{
                requestSMSPermissions()
            }
        }
    }

    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT){
            if (resultCode == RESULT_OK && data != null){
                val phoneNum = data.data.toString()
                Log.d("EXTRA_STATUS", phoneNum)
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        }
    }

    private fun getOtpFromMessage(message: String?) {
        val otpPattern = Pattern.compile("(|^)\\d{5}")
        val matcher = otpPattern.matcher(message)
        if (matcher.find()){
            etOtp!!.setText(matcher.group(0))
        }
    }

    private fun registerBroadcastReceiver(){
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener = object: SmsBroadcastReceiver.SmsBroadcastRecieverListener{
            override fun onSuccess(intent: Intent?){
               startActivityForResult(intent,REQ_USER_CONSENT)
            }

            override fun onFailure() {
                //  showToast("BroadCastReciever Failure")
            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)

    }

    override fun onStart(){
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop(){
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    private fun sendMessage(sMessage:String) {
        try {
            val smsManager:SmsManager

            smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage("900", null, sMessage, null, null)

            Toast.makeText(applicationContext, "Message sent", Toast.LENGTH_LONG).show()


        } catch (e: Exception) {

            Toast.makeText(applicationContext, "Exception: "
                    + e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun checkSMSPermissions():Boolean{
        val smsSendResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val smsReadResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val smsRecieveResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED

        return smsSendResult && smsReadResult && smsRecieveResult
    }

    private fun requestSMSPermissions() {
        ActivityCompat.requestPermissions(
            this, smsSendPermissions,
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
                Log.d("usersms", "Checking permissions 2")
                if (grantResults.isNotEmpty()){

                    val smsSendAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val smsReadAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    val smsRecieveAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED

                    if (smsSendAccepted && smsReadAccepted && smsRecieveAccepted) {
                        var sMessage:String = ""
                        val sPhone = etPhone.text.toString().trim()
                        val sAmount = etAmount.text.toString().trim()
                        if (sPhone.isNotEmpty() and sAmount.isNotEmpty()){
                            sMessage = "Перевод " + sPhone + " " + sAmount
                            sendMessage(sMessage)
                        }
                        else {
                            Toast.makeText(applicationContext, "Please enter all data..", Toast.LENGTH_LONG)
                                .show()
                        }

                    }
                    else{
                        showToast("SMS send and read permissions requiered")
                    }
                }
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}