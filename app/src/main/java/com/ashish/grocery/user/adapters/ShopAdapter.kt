package com.ashish.grocery.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashish.grocery.R
import com.ashish.grocery.user.ShopDetailActivity
import com.ashish.grocery.user.models.ShopModel
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShopAdapter(val context: Context, var shops: ArrayList<ShopModel>) :
    RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shopNo = shops[position]
        holder.shopName.text = shopNo.shopName
        holder.email.text = shopNo.email
        holder.phone.text = shopNo.phone
        val uid = shopNo.uid
        loadReview(holder, shopNo)
        try {
            Glide.with(context).load(shopNo.profileImage).timeout(6000).into(holder.shopImg)
        } catch (e: Exception) {
            holder.shopImg.setImageResource(R.drawable.ic_shop)
        }
        if (shopNo.online == "true") {
            holder.onlineStatus.visibility = View.VISIBLE
        } else
            holder.onlineStatus.visibility = View.GONE
        if (shopNo.shopOpen == "false") {
            holder.close.visibility = View.VISIBLE
        } else
            holder.close.visibility = View.GONE
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent(context, ShopDetailActivity::class.java))
            intent.putExtra("shopId", uid)
            context.startActivity(intent)
        }
    }

    private var ratingSum: Float = 0.0F
    private fun loadReview(holder: ShopViewHolder, shopNo: ShopModel) {
        val shopUid = shopNo.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid).child("Ratings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ratingSum = 0F
                for (ds in snapshot.children) {
                    val rating = ("" + ds.child("ratings").value).toFloat()
                    ratingSum += rating
                }
                val numberOfReview = snapshot.childrenCount
                val avg = ratingSum / numberOfReview
                holder.ratingBar.rating = avg
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount() = shops.size

    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var shopImg: ImageView = view.findViewById(R.id.shopImg)
        var shopName: TextView = view.findViewById(R.id.shopName)
        var email: TextView = view.findViewById(R.id.email)
        var phone: TextView = view.findViewById(R.id.phone)
        var ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        var close: TextView = view.findViewById(R.id.closed)
        var nextBtn: ImageView = view.findViewById(R.id.next)
        var onlineStatus: ImageView = view.findViewById(R.id.onlineStatus)

    }
}