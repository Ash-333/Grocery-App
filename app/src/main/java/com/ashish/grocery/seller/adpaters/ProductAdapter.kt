package com.ashish.grocery.seller.adpaters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.seller.EditProductActivity
import com.ashish.grocery.seller.models.ProductsModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(val context: Context, var product: ArrayList<ProductsModel>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_product_seller, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val products = product[position]
        holder.productName.text = products.productName
        holder.productQuantity.text = products.productQuantity
        holder.originalPrice.text = "Rs " + products.originalPrice
        holder.discountedPrice.text = "Rs " + products.discountPrice
        holder.discountNote.text = products.discountNote
        if (products.discountAvailable == "true") {
            holder.discountNote.visibility = View.VISIBLE
            holder.discountedPrice.visibility = View.VISIBLE
            holder.discountedPrice.text = "Rs " + products.discountPrice
            holder.discountNote.text = products.discountNote
            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.discountNote.visibility = View.GONE
            holder.discountedPrice.visibility = View.GONE
        }

        holder.productImage.load(products.productImage) {
            placeholder(R.drawable.ic_cart_purple)
        }
        holder.itemView.setOnClickListener {
            detailBottomSheet(products)
        }
    }

    private fun detailBottomSheet(products: ProductsModel) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bs_product_detail_seller, null)
        bottomSheetDialog.setContentView(view)

        val backBtn: ImageButton = view.findViewById(R.id.backBtn)
        val editBtn: ImageButton = view.findViewById(R.id.editBtn)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteBtn)
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val discountNoteTv: TextView = view.findViewById(R.id.discountNoteTv)
        val productTitleTv: TextView = view.findViewById(R.id.productTitle)
        val productDescriptionTv: TextView = view.findViewById(R.id.productDescription)
        val productCategoryTv: TextView = view.findViewById(R.id.productCategory)
        val productQuantityTv: TextView = view.findViewById(R.id.productQuantity)
        val discountPriceTv: TextView = view.findViewById(R.id.discountPrice)
        val originalPriceTv: TextView = view.findViewById(R.id.originalPrice)

        val productIds = products.timestamp
        productTitleTv.text = products.productName
        productDescriptionTv.text = products.productDescription
        productCategoryTv.text = products.productCategory
        productQuantityTv.text = products.productQuantity
        originalPriceTv.text = "Rs " + products.originalPrice
        discountPriceTv.text = "Rs " + products.discountPrice
        discountNoteTv.text = products.discountNote
        if (products.discountAvailable == "true") {
            discountNoteTv.visibility = View.VISIBLE
            discountPriceTv.visibility = View.VISIBLE
            discountPriceTv.text = "Rs " + products.discountPrice
            discountNoteTv.text = products.discountNote
            originalPriceTv.paintFlags = originalPriceTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            discountNoteTv.visibility = View.GONE
            discountPriceTv.visibility = View.GONE
        }
        productImg.load(products.productImage) {
            placeholder(R.drawable.ic_cart_white)
        }

        bottomSheetDialog.show()
        backBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        editBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(context, EditProductActivity::class.java)
            intent.putExtra("productId", productIds)
            context.startActivity(intent)

        }
        deleteBtn.setOnClickListener {
            Log.d("ProductTitle", products.productName)
            bottomSheetDialog.dismiss()
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure you want to delete product " + products.productName + "?")
                .setTitle("Delete").setPositiveButton("Delete") { _, _ ->
                    deleteProduct(productIds)
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun deleteProduct(productId: String) {
        val auth = FirebaseAuth.getInstance()
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(auth.uid!!).child("Products").child(productId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }

    }

    override fun getItemCount() = product.size

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.titleTv)
        val productQuantity: TextView = view.findViewById(R.id.quantityTv)
        val discountedPrice: TextView = view.findViewById(R.id.discountedPriceTv)
        val originalPrice: TextView = view.findViewById(R.id.originalPriceTv)
        val discountNote: TextView = view.findViewById(R.id.discountNoteTv)
    }

    fun filterList(filterList: ArrayList<ProductsModel>) {
        product = filterList
        notifyDataSetChanged()
    }
}