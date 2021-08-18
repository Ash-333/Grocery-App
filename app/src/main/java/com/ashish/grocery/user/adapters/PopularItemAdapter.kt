package com.ashish.grocery.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.user.ProductDetailActivity
import com.ashish.grocery.user.models.PopularItemModel

class PopularItemAdapter(var context: Context,var list:ArrayList<PopularItemModel>):RecyclerView.Adapter<PopularItemAdapter.ItemHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.popular_product_row, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item=list[position]

        holder.img.load(item.productImage)
        holder.name.text=item.productName
        holder.price.text="Rs "+item.originalPrice
        holder.itemView.setOnClickListener {
            val intent=Intent(context,ProductDetailActivity::class.java)
            intent.putExtra("productName",item.productName)
            intent.putExtra("productImage",item.productImage)
            intent.putExtra("productDescription",item.productDescription)
            intent.putExtra("productPrice",item.originalPrice)
            intent.putExtra("discountNote",item.discountNote)
            intent.putExtra("discountPrice",item.discountPrice)
            intent.putExtra("discountAvailable",item.discountAvailable)
            intent.putExtra("productQuantity",item.productQuantity)
            intent.putExtra("productId",item.productId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int =list.size
    class ItemHolder(view:View):RecyclerView.ViewHolder(view){
        val img:ImageView=view.findViewById(R.id.productImg)
        val name:TextView=view.findViewById(R.id.productName)
        val price:TextView=view.findViewById(R.id.price)
    }
}