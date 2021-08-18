package com.ashish.grocery.user.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.seller.models.ProductsModel
import com.ashish.grocery.user.ProductDetailActivity

class UserProductAdapter(var context: Context, var product: ArrayList<ProductsModel>) :
    RecyclerView.Adapter<UserProductAdapter.UserProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_product_user, parent, false)
        return UserProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserProductViewHolder, position: Int) {
        //Log.d("pos",position.toString())
        val products = product[position]
        holder.productNameTv.text = products.productName
        holder.productDescriptionTv.text = products.productQuantity
        holder.originalPriceTv.text = "Rs " + products.originalPrice
        holder.discountedPriceTv.text = "Rs " + products.discountPrice
        holder.discountNoteTv.text = products.discountNote
        if (products.discountAvailable == "true") {
            Log.d("pos",position.toString())
            holder.discountNoteTv.visibility = View.VISIBLE
            holder.discountedPriceTv.visibility = View.VISIBLE
            holder.discountedPriceTv.text = "Rs " + products.discountPrice
            holder.discountNoteTv.text = products.discountNote
            holder.originalPriceTv.paintFlags =
                holder.originalPriceTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.discountNoteTv.visibility = View.GONE
            holder.discountedPriceTv.visibility = View.GONE
        }
        holder.productImg.load(products.productImage) {
            placeholder(R.drawable.ic_cart_purple)
        }

//        holder.addToCart.setOnClickListener {
//            showQuantityDialog(products)
//        }
        holder.nextBtn.setOnClickListener {
            val intent= Intent(context,ProductDetailActivity::class.java)
            intent.putExtra("productName",products.productName)
            intent.putExtra("productImage",products.productImage)
            intent.putExtra("productDescription",products.productDescription)
            intent.putExtra("productPrice",products.originalPrice)
            intent.putExtra("discountNote",products.discountNote)
            intent.putExtra("discountPrice",products.discountPrice)
            intent.putExtra("discountAvailable",products.discountAvailable)
            intent.putExtra("productQuantity",products.productQuantity)
            intent.putExtra("productId",products.productId)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent= Intent(context,ProductDetailActivity::class.java)
            intent.putExtra("productName",products.productName)
            intent.putExtra("productImage",products.productImage)
            intent.putExtra("productDescription",products.productDescription)
            intent.putExtra("productPrice",products.originalPrice)
            intent.putExtra("discountNote",products.discountNote)
            intent.putExtra("discountPrice",products.discountPrice)
            intent.putExtra("discountAvailable",products.discountAvailable)
            intent.putExtra("productQuantity",products.productQuantity)
            intent.putExtra("productId",products.productId)
            context.startActivity(intent)
        }
    }

    private var cost: Double = 0.0
    private var finalCost: Double = 0.0
    private var quantities: Int = 1
    private fun showQuantityDialog(product: ProductsModel) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null)
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productNameTv)
        val productQuantity: TextView = view.findViewById(R.id.quantity)
        val productDescription: TextView = view.findViewById(R.id.productDescription)
        val discountedPrice: TextView = view.findViewById(R.id.discountedPrice)
        val originalPriceTv: TextView = view.findViewById(R.id.originalPrice)
        val discountNoteTv: TextView = view.findViewById(R.id.discountNoteTv)
        val addCartBtn: Button = view.findViewById(R.id.addToCartBtn)
        val removeBtn: ImageButton = view.findViewById(R.id.removeBtn)
        val addBtn: ImageButton = view.findViewById(R.id.addBtn)
        val quantityTv: TextView = view.findViewById(R.id.quantityTv)
        val finalPrice: TextView = view.findViewById(R.id.finalPrice)

        val productId = product.productId
        lateinit var price: String
        if (product.discountAvailable == "true") {
            price = "Rs" + product.discountPrice
            discountNoteTv.visibility = View.VISIBLE
            originalPriceTv.paintFlags = originalPriceTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            discountNoteTv.visibility = View.GONE
            discountedPrice.visibility = View.GONE
            price = "Rs" + product.originalPrice
        }
        cost = price.replace("Rs", "").toDouble()
        finalCost = price.replace("Rs", "").toDouble()

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        productImg.load(product.productImage) {
            placeholder(R.drawable.ic_shopping_cart)
        }
        productName.text = product.productName
        productQuantity.text = product.productQuantity
        productDescription.text = product.productDescription
        discountedPrice.text = product.discountPrice
        originalPriceTv.text = product.originalPrice
        discountNoteTv.text = product.discountNote
        finalPrice.text = finalCost.toString()
        val dialog = builder.create()
        dialog.show()
        addBtn.setOnClickListener {
            finalCost += cost
            quantities++
            finalPrice.text = "Rs$finalCost"
            quantityTv.text = quantities.toString()
        }
        removeBtn.setOnClickListener {
            if (quantities > 1) {
                finalCost -= cost
                quantities--
                finalPrice.text = finalCost.toString()
                quantityTv.text = quantities.toString()
            }
        }
        addCartBtn.setOnClickListener {
            val title = productName.text.toString().trim()
            val priceEach = price
            val totalPrice = finalPrice.text.toString().trim().replace("Rs", "")
            val quantity = quantityTv.text.toString().trim()
            val image = product.productImage.trim()
            //cartItemModel=cart.add(CartItemModel(title,productId,priceEach,totalPrice,quantity,image))

            addToCart(productId, priceEach, totalPrice, quantity,title,image)
            dialog.dismiss()
        }
    }
//    val shopDetail=ShopDetailActivity()

//    private var itemId = 1
    private fun addToCart(productId: String, priceEach: String, totalPrice: String, quantity: String,title: String,image:String) {

    //viewModel=
        //ViewModelProvider(context, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(CartViewModel::class.java)
//        itemId++
//        val easyDB = EasyDB.init(context, "ITEM_DB")
//            .setTableName("ITEM_TABLE")
//            .addColumn(Column("Item_Id", "text", "unique"))//1
//            .addColumn(Column("Item_Name", "text", "not null"))//2
//            .addColumn(Column("Item_PID", "text", "not null"))//3
//            .addColumn(Column("Item_Price_Each", "text", "not null"))//4
//            .addColumn(Column("Item_Price", "text", "not null"))//5
//            .addColumn(Column("Item_Quantity", "text", "not null"))//6
//            .addColumn(Column("Item_Image", "text", "not null"))//7
//            .doneTableColumn()
//
//        val done: Boolean = easyDB.addData("Item_Id", itemId)//1
//            .addData("Item_PID", productId)//2
//            .addData("Item_Name", title)//3
//            .addData("Item_Price_Each", priceEach)//4
//            .addData("Item_Price", totalPrice)//5
//            .addData("Item_Quantity", quantity)//6
//            .addData("Item_Image", image)//7
//            .doneDataAdding()
        Toast.makeText(context, "Product added to cart", Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount() = product.size

    class UserProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var productImg: ImageView = view.findViewById(R.id.productImg)
        val nextBtn: ImageView = view.findViewById(R.id.nextImg)
        val productNameTv: TextView = view.findViewById(R.id.titleTv)
        val productDescriptionTv: TextView = view.findViewById(R.id.descriptionTv)
       // val addToCart: TextView = view.findViewById(R.id.addToCartTv)
        val discountedPriceTv: TextView = view.findViewById(R.id.discountedPriceTv)
        val originalPriceTv: TextView = view.findViewById(R.id.originalPriceTv)
        val discountNoteTv: TextView = view.findViewById(R.id.discountNoteTv)
    }
}