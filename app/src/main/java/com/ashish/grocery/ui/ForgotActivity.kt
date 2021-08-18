package com.ashish.grocery.ui

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.ashish.grocery.R
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {
    lateinit var emailEt:EditText
    lateinit var backBtn:ImageButton
    lateinit var recover:Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        init()
        firebaseAuth= FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        backBtn.setOnClickListener {
            onBackPressed()
        }
        recover.setOnClickListener {
            recoverPassword()
        }
    }
    private lateinit var email:String
    private fun recoverPassword() {
        email=emailEt.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid Email pattern", Toast.LENGTH_SHORT).show()
            return
        }
        progressDialog.setMessage("Please wait sending instruction to reset Password")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Password reset instruction sent to your email",Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this,it.message.toString(),Toast.LENGTH_LONG).show()
            }
    }

    private fun init(){
        emailEt=findViewById(R.id.emailEt)
        backBtn=findViewById(R.id.backBtn)
        recover=findViewById(R.id.recoverBtn)
    }
}