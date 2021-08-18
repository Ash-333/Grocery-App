package com.ashish.grocery.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import coil.load
import com.ashish.grocery.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WriteReviewActivity : AppCompatActivity() {
    private lateinit var shopUid: String
    private lateinit var backBtn: ImageButton
    private lateinit var shopImg: ImageView
    private lateinit var shopNameTv: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewEt: EditText
    private lateinit var submitBtn: FloatingActionButton
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)
        init()
        shopUid = intent.getStringExtra("shopUid")!!
        firebaseAuth = FirebaseAuth.getInstance()
        loadShopInfo()
        loadReview()
        backBtn.setOnClickListener {
            onBackPressed()
        }
        submitBtn.setOnClickListener {
            inputData()
        }

    }

    private fun loadShopInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shopName = "" + snapshot.child("shopName").value
                val shopImage = "" + snapshot.child("profileImage").value
                shopNameTv.text = shopName
                Log.d("shopImage", shopImage)
                shopImg.load(shopImage) {
                    placeholder(R.drawable.ic_shop)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadReview() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid).child("Ratings").child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        //val uid=""+snapshot.child("uid").value
                        val ratings = "" + snapshot.child("ratings").value
                        //val timeStamp=""+snapshot.child("timeStamp").value

                        val myRating = ratings.toFloat()
                        ratingBar.rating = myRating
                        reviewEt.text = ("" + snapshot.child("review").value).toEditable()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun inputData() {
        val ratings = "" + ratingBar.rating
        val review = "" + reviewEt.text.toString().trim()
        val timeStamp = "" + System.currentTimeMillis()

        val hashMap = HashMap<String, String>()
        hashMap["timeStamp"] = "" + timeStamp
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["ratings"] = "" + ratings
        hashMap["review"] = "" + review

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid).child("Ratings").child(firebaseAuth.uid!!)
            .updateChildren(hashMap as Map<String, Any>)
            .addOnSuccessListener {
                showSuccessToast("Review published successfully")
            }.addOnFailureListener {
                showErrorToast(it.message.toString())
            }
    }
    private fun showSuccessToast(message: String?) {
        val toast = Toast(this@WriteReviewActivity)
        val view: View = LayoutInflater.from(this@WriteReviewActivity)
            .inflate(R.layout.success_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }

    private fun showErrorToast(message: String?) {
        val toast = Toast(this@WriteReviewActivity)
        val view: View = LayoutInflater.from(this@WriteReviewActivity)
            .inflate(R.layout.error_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }
    private fun init() {
        backBtn = findViewById(R.id.backBtn)
        shopImg = findViewById(R.id.shopImg)
        shopNameTv = findViewById(R.id.shopName)
        ratingBar = findViewById(R.id.ratingBar)
        reviewEt = findViewById(R.id.reviewEt)
        submitBtn = findViewById(R.id.submitBtn)
    }
}