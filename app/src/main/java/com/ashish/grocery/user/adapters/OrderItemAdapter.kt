package com.ashish.grocery.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashish.grocery.R
import com.ashish.grocery.user.models.OrderItemModel

class OrderItemAdapter(var context:Context,var orderList:ArrayList<OrderItemModel>):RecyclerView.Adapter<OrderItemAdapter.OrderItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemHolder {
        val inflater=LayoutInflater.from(context)
        val view=inflater.inflate(R.layout.row_ordered_item,parent,false)
        return OrderItemHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemHolder, position: Int) {
        val item=orderList[position]
        holder.itemTitleTv.text=item.name
        holder.itemPriceTv.text="Rs"+item.cost
        holder.itemPriceEachTv.text="Rs"+item.price
        holder.quantityTv.text="["+item.quantity+"]"
    }

    override fun getItemCount()=orderList.size
    class OrderItemHolder(view:View):RecyclerView.ViewHolder(view){
        var itemTitleTv:TextView=view.findViewById(R.id.itemTitle)
        var itemPriceTv:TextView=view.findViewById(R.id.itemPrice)
        var itemPriceEachTv:TextView=view.findViewById(R.id.itemPriceEach)
        var quantityTv:TextView=view.findViewById(R.id.quantity)
    }
}