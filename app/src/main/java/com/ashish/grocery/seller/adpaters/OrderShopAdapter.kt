package com.ashish.grocery.seller.adpaters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashish.grocery.R
import com.ashish.grocery.seller.OrderDetailSellerActivity
import com.ashish.grocery.seller.models.OrderShopModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class OrderShopAdapter(var context: Context, var orderList: ArrayList<OrderShopModel>) :
    RecyclerView.Adapter<OrderShopAdapter.OrderShopHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderShopHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_order_seller, parent, false)
        return OrderShopHolder(view)
    }

    override fun onBindViewHolder(holder: OrderShopHolder, position: Int) {
        val item = orderList[position]
        loadUserInfo(item, holder)
        holder.orderIdTv.text = "Order Id: " + item.orderId
        holder.orderStatusTv.text = item.orderStatus
        holder.totalAmtTv.text = "Amount: Rs" + item.orderCost
        when (item.orderStatus) {
            "In progress" -> {
                holder.orderStatusTv.setTextColor(context.resources.getColor(R.color.inProgress))
            }
            "Completed" -> {
                holder.orderStatusTv.setTextColor(context.resources.getColor(R.color.green))
            }
            "Cancelled" -> {
                holder.orderStatusTv.setTextColor(context.resources.getColor(R.color.red))
            }
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = item.orderTime.toLong()
        val dateFormat = android.text.format.DateFormat.format("dd-MMM-yyyy", calendar).toString()
        holder.orderDateTv.text = dateFormat
        holder.itemView.setOnClickListener {
            val intent=Intent(context, OrderDetailSellerActivity::class.java)
            intent.putExtra("orderId",item.orderId)
            intent.putExtra("orderBy",item.orderBy)
            context.startActivity(intent)
        }
    }

    private fun loadUserInfo(item: OrderShopModel, holder: OrderShopHolder) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(item.orderBy).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val email = "" + snapshot.child("email").value
                holder.emailTv.text = email
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    override fun getItemCount() = orderList.size
    class OrderShopHolder(view: View) : RecyclerView.ViewHolder(view) {
        var orderIdTv: TextView = view.findViewById(R.id.orderId)
        var orderDateTv: TextView = view.findViewById(R.id.orderDate)
        var emailTv: TextView = view.findViewById(R.id.email)
        var totalAmtTv: TextView = view.findViewById(R.id.totalAmt)
        var orderStatusTv: TextView = view.findViewById(R.id.orderStatus)
        var nextBtn: ImageView = view.findViewById(R.id.nextBtn)
    }
}