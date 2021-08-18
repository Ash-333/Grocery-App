package com.ashish.grocery.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ashish.grocery.R
import com.ashish.grocery.seller.MainSellerActivity
import com.ashish.grocery.user.MainUserActivity
import com.ashish.grocery.user.RegisterUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {
    lateinit var emailEt: EditText
    lateinit var passwordEt: EditText
    lateinit var forgotTV: TextView
    lateinit var noAccount: TextView
    lateinit var errorMessageTv: TextView
    private lateinit var loginBtn: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)

        forgotTV.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotActivity::class.java))
        }

        noAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterUserActivity::class.java))
        }

        loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private lateinit var email: String
    private lateinit var password: String
    private fun loginUser() {
        email = emailEt.text.toString()
        password = passwordEt.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email pattern", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password..", Toast.LENGTH_SHORT).show()
            return
        }
        progressDialog.setMessage("Logging in...")
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                makeMeOnline()
            } else {
                // If sign in fails, display a message to the user.
                    progressDialog.dismiss()
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeMeOnline() {
        progressDialog.setMessage("Checking...")
        val hashMap = hashMapOf<String,String>()
        hashMap["online"] = "true"
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(firebaseAuth.uid!!).updateChildren(hashMap as Map<String, Any>)
            .addOnSuccessListener {
                checkUserType()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                //errorMessageTv.text = it.message.toString()
            }
    }

    private fun checkUserType() {
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //val ds:DataSnapshot
                    for (ds in snapshot.children) {
                        val accountType: String = "" + ds.child("accountType").value
                        if (accountType == "seller") {
                            progressDialog.dismiss()
                            startActivity(Intent(this@LoginActivity, MainSellerActivity::class.java))
                        } else {
                            progressDialog.dismiss()
                            startActivity(Intent(this@LoginActivity, MainUserActivity::class.java))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessageTv.text = error.message
                }
            })
    }

    private fun init() {
        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        forgotTV = findViewById(R.id.forgotTv)
        noAccount = findViewById(R.id.registerTv)
        loginBtn = findViewById(R.id.loginBtn)
        //errorMessageTv = findViewById(R.id.errorMessage)
    }
}