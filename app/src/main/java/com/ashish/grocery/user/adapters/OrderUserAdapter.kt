package com.ashish.grocery.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashish.grocery.R
import com.ashish.grocery.user.OrderDetailUserActivity
import com.ashish.grocery.user.models.OrderUserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class OrderUserAdapter(var context: Context, var orderList: ArrayList<OrderUserModel>) :
    RecyclerView.Adapter<OrderUserAdapter.OrderUserHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderUserHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_order_user, parent, false)
        return OrderUserHolder(view)
    }

    override fun onBindViewHolder(holder: OrderUserHolder, position: Int) {
        val order = orderList[position]
        loadShopInfo(order, holder)
        holder.orderIdTv.text = "Order Id:" + order.orderId
        holder.shopNameTv.text = order.orderTo
        holder.amountTv.text = "Total Amount: Rs" + order.orderCost
        when (order.orderStatus) {
            "In progress" -> {
                holder.statusTv.setTextColor(context.resources.getColor(R.color.inProgress))
                holder.statusTv.text="In progress"
            }
            "Completed" -> {
                holder.statusTv.setTextColor(context.resources.getColor(R.color.green))
                holder.statusTv.text="Completed"
            }
            "Cancelled" -> {
                holder.statusTv.setTextColor(context.resources.getColor(R.color.red))
                holder.statusTv.text="Cancelled"
            }
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = order.orderTime.toLong()
        val dateFormat = android.text.format.DateFormat.format("dd-MMM-yyyy", calendar).toString()
        holder.dateTv.text = dateFormat
        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetailUserActivity::class.java)
            intent.putExtra("orderId", order.orderId)
            intent.putExtra("orderTo", order.orderTo)
            context.startActivity(intent)
        }
    }

    private fun loadShopInfo(order: OrderUserModel, holder: OrderUserHolder) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(order.orderTo).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shopName = "" + snapshot.child("shopName").value
                holder.shopNameTv.text = shopName
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount() = orderList.size
    class OrderUserHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderIdTv: TextView = view.findViewById(R.id.orderId)
        val dateTv: TextView = view.findViewById(R.id.date)
        val shopNameTv: TextView = view.findViewById(R.id.shopName)
        val amountTv: TextView = view.findViewById(R.id.amount)
        val statusTv: TextView = view.findViewById(R.id.status)
        val nextBtn: ImageView = view.findViewById(R.id.next)
    }
}