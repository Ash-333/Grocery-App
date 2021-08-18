package com.ashish.grocery.user.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.user.cart.cartDb.Cart

class CartItemAdapter(var context: Context, var listener:ICartAdapter) :
    RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {
    private val item=ArrayList<Cart>()

    fun update(newList:List<Cart>){
        item.clear()
        item.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val inflater = CartItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_cart_item, parent, false))
        inflater.removeTv.setOnClickListener {
            listener.onItemClicked(item[inflater.adapterPosition])
        }
        return inflater

    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val itemPosition = item[position]
        Log.d("Cart",itemPosition.name)
        holder.titleTv.text = itemPosition.name
        holder.quantityTv.text = "  [" + itemPosition.quantity + "]"
        holder.eachPriceTv.text = itemPosition.priceEach
        holder.priceTv.text = itemPosition.price
        holder.productImg.load(itemPosition.image)

    }


    override fun getItemCount() = item.size



    class CartItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTv: TextView = view.findViewById(R.id.itemTitle)
        val priceTv: TextView = view.findViewById(R.id.itemPrice)
        val eachPriceTv: TextView = view.findViewById(R.id.eachItemPrice)
        val quantityTv: TextView = view.findViewById(R.id.itemQuantity)
        val removeTv: TextView = view.findViewById(R.id.removeItem)
        val productImg: ImageView =view.findViewById(R.id.productImg)
    }
}
interface ICartAdapter{
    fun onItemClicked(car: Cart)
}