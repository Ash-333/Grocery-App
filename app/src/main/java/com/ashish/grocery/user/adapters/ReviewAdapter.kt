package com.ashish.grocery.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.user.models.ReviewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class ReviewAdapter(val context: Context, var list: ArrayList<ReviewModel>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_review, parent, false)
        return ReviewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        val listPosition = list[position]
        holder.ratingBar.rating = listPosition.ratings.toFloat()
        holder.reviewTv.text = listPosition.review
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = (listPosition.timeStamp).toLong()
        val date = android.text.format.DateFormat.format("dd-MMM-yyyy", calendar).toString()
        holder.dateTv.text = date
        loadUserDetails(listPosition, holder)
    }

    private fun loadUserDetails(position: ReviewModel, holder: ReviewHolder) {
        val uid = position.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = "" + snapshot.child("name").value
                val img = "" + snapshot.child("profileImage").value
                holder.userNameTv.text = name
                holder.profileImage.load(img) {
                    placeholder(R.drawable.ic_person_gray)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount() = list.size
    class ReviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val userNameTv: TextView = view.findViewById(R.id.userName)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val dateTv: TextView = view.findViewById(R.id.date)
        val reviewTv: TextView = view.findViewById(R.id.review)
    }
}