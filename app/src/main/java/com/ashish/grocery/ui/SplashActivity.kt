package com.ashish.grocery.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ashish.grocery.R
import com.ashish.grocery.seller.MainSellerActivity
import com.ashish.grocery.user.MainUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        firebaseAuth = FirebaseAuth.getInstance()

        CoroutineScope(Dispatchers.IO).launch {
            delay(java.util.concurrent.TimeUnit.SECONDS.toMillis(1))
            withContext(Dispatchers.Main) {
                val user: FirebaseUser? = firebaseAuth.currentUser
                if (user == null) {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Log.d("Success", " $user")
                    checkUserType()
                }
            }
        }

    }

    private fun checkUserType() {
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.orderByChild("uid").equalTo(firebaseAuth.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //val ds: DataSnapshot
                for (ds in snapshot.children) {
                    val accountType: String = "" + ds.child("accountType").value
                    if (accountType == "seller") {
                        startActivity(Intent(this@SplashActivity, MainSellerActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this@SplashActivity, MainUserActivity::class.java))

                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}