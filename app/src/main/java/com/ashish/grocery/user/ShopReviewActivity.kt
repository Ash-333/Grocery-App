package com.ashish.grocery.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.user.adapters.ReviewAdapter
import com.ashish.grocery.user.models.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShopReviewActivity : AppCompatActivity() {
    private lateinit var shopUid:String
    private lateinit var shopImage:ImageView
    private lateinit var backBtn:ImageButton
    private lateinit var shopNameTv:TextView
    private lateinit var ratingBar:RatingBar
    private lateinit var ratingTv:TextView
    private lateinit var reviewRv:RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private var reviewItem: ArrayList<ReviewModel>? = null
    private var reviewAdapter: ReviewAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_review)
        init()
        shopUid=intent.getStringExtra("shopUid")!!
        firebaseAuth= FirebaseAuth.getInstance()
        loadShopDetails()
        loadReviews()
        backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private var ratingSum:Float= 0.0F
    private fun loadReviews() {
        reviewItem=ArrayList()
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid).child("Ratings").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
              reviewItem?.clear()
                ratingSum=0F
                for (ds in snapshot.children){
                    val rating=(""+ds.child("ratings").value).toFloat()
                    ratingSum+=rating
                    val reviewModel=ds.getValue(ReviewModel::class.java)
                    reviewItem?.add(reviewModel!!)
                }
                reviewAdapter= ReviewAdapter(this@ShopReviewActivity,reviewItem!!)
                reviewRv.adapter=reviewAdapter
                val numberOfReview=snapshot.childrenCount
                val avg=ratingSum/numberOfReview
                ratingTv.text=avg.toString()
                ratingBar.rating=avg

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun loadShopDetails() {
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name=""+snapshot.child("shopName").value
                val img=""+snapshot.child("profileImage").value
                shopNameTv.text=name
                shopImage.load(img){
                    placeholder(R.drawable.ic_shop)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun init(){
        shopImage=findViewById(R.id.shopImg)
        shopNameTv=findViewById(R.id.shopName)
        ratingBar=findViewById(R.id.ratingBar)
        ratingTv=findViewById(R.id.rating)
        reviewRv=findViewById(R.id.reviewRv)
        backBtn=findViewById(R.id.backBtn)
    }
}