package com.ashish.grocery.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.ashish.grocery.Constants
import com.ashish.grocery.Constants.Companion.FCM_TOPIC
import com.ashish.grocery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


//const val FCM_TOPIC="PUSH_NOTIFICATION"
class SettingsActivity : AppCompatActivity() {
    private lateinit var backBtn:ImageButton
    private lateinit var fcmSwitch:SwitchCompat
    private lateinit var notificationStatus:TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sp:SharedPreferences
    private lateinit var spEditor: SharedPreferences.Editor
    private var isChecked:Boolean=false
    private val enabledMessage:String="Notification are enabled"
    private val disabledMessage:String="Notification are disabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        init()
        firebaseAuth= FirebaseAuth.getInstance()
        sp=getSharedPreferences("SETTINGS_SP", MODE_PRIVATE)
        isChecked=sp.getBoolean("FCM_ENABLED",false)
        fcmSwitch.isChecked = isChecked
        if (isChecked){
            notificationStatus.text=enabledMessage
        }
        else{
            notificationStatus.text=disabledMessage
        }
        backBtn.setOnClickListener {
            onBackPressed()
        }
        fcmSwitch.setOnCheckedChangeListener { _, b ->
            if(b){
                subscribeToTopic()
            }
            else{
                unSubscribeToTopic()
            }
        }
    }
    private fun subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC)
            .addOnSuccessListener {
                spEditor=sp.edit()
                spEditor.putBoolean("FCM_ENABLED",true)
                spEditor.apply()
                showSuccessToast(enabledMessage)
                notificationStatus.text=enabledMessage
            }.addOnFailureListener {
                Toast.makeText(this,it.message.toString(),Toast.LENGTH_SHORT).show()
            }
    }
    private fun unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC)
            .addOnSuccessListener {
                spEditor=sp.edit()
                spEditor.putBoolean("FCM_ENABLED",false)
                spEditor.apply()
                Toast.makeText(this,disabledMessage,Toast.LENGTH_SHORT).show()
                notificationStatus.text=disabledMessage
            }.addOnFailureListener {
                showErrorToast(it.message.toString())
            }
    }

    private fun showSuccessToast(message: String?) {
        val toast = Toast(this@SettingsActivity)
        val view: View = LayoutInflater.from(this@SettingsActivity)
            .inflate(R.layout.success_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }

    private fun showErrorToast(message: String?) {
        val toast = Toast(this@SettingsActivity)
        val view: View = LayoutInflater.from(this@SettingsActivity)
            .inflate(R.layout.error_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }
    private fun init(){
        backBtn=findViewById(R.id.backBtn)
        fcmSwitch=findViewById(R.id.fcmSwitch)
        notificationStatus=findViewById(R.id.notificationStatus)
    }
}