package com.ashish.grocery.user

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.databinding.ActivityProductDetailBinding
import com.ashish.grocery.user.cart.cartDb.Cart
import com.ashish.grocery.user.cart.cartDb.CartViewModel
import com.ashish.grocery.user.models.CartItemModel


class ProductDetailActivity : AppCompatActivity() {
    private lateinit var viewModel: CartViewModel
    private lateinit var productName: String
    private lateinit var productImage: String
    private lateinit var productDescription: String
    private lateinit var productPrice: String
    private lateinit var discountNote: String
    private lateinit var discountPrice: String
    private lateinit var discountAvailable: String
    private lateinit var productQuantity: String
    private lateinit var productId: String
    private lateinit var binding:ActivityProductDetailBinding
    private var cartItem:ArrayList<CartItemModel> = ArrayList()
    private lateinit var cart:CartItemModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel= ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(CartViewModel::class.java)
        productName=intent.getStringExtra("productName")!!
        productImage=intent.getStringExtra("productImage")!!
        productDescription=intent.getStringExtra("productDescription")!!
        productPrice=intent.getStringExtra("productPrice")!!
        discountNote=intent.getStringExtra("discountNote")!!
        discountPrice=intent.getStringExtra("discountPrice")!!
        discountAvailable=intent.getStringExtra("discountAvailable")!!
        productQuantity=intent.getStringExtra("productQuantity")!!
        productId=intent.getStringExtra("productId")!!
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.addBtn.setOnClickListener{
            showQuantityDialog()
        }
        binding.productImg.load(productImage)
        binding.productDescription.text=productDescription
        binding.productName.text=productName
        binding.productPrice.text=productPrice
    }

    private var cost: Double = 0.0
    private var finalCost: Double = 0.0
    private var quantities: Int = 1
    private fun showQuantityDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productNameTv: TextView = view.findViewById(R.id.productNameTv)
        val productQuantityTv: TextView = view.findViewById(R.id.quantity)
        val productDescriptionTv: TextView = view.findViewById(R.id.productDescription)
        val discountedPriceTv: TextView = view.findViewById(R.id.discountedPrice)
        val originalPriceTv: TextView = view.findViewById(R.id.originalPrice)
        val discountNoteTv: TextView = view.findViewById(R.id.discountNoteTv)
        val addCartBtn: Button = view.findViewById(R.id.addToCartBtn)
        val removeBtn: ImageButton = view.findViewById(R.id.removeBtn)
        val addBtn: ImageButton = view.findViewById(R.id.addBtn)
        val quantityTv: TextView = view.findViewById(R.id.quantityTv)
        val finalPrice: TextView = view.findViewById(R.id.finalPrice)

        val productId = productId
        lateinit var price: String
        if (discountAvailable == "true") {
            price = "Rs$discountPrice"
            discountNoteTv.visibility = View.VISIBLE
            originalPriceTv.paintFlags = originalPriceTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            discountNoteTv.visibility = View.GONE
            discountedPriceTv.visibility = View.GONE
            price = "Rs$productPrice"
        }
        cost = price.replace("Rs", "").toDouble()
        finalCost = price.replace("Rs", "").toDouble()

        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        productImg.load(productImage) {
            placeholder(R.drawable.ic_shopping_cart)
        }
        productNameTv.text = productName
        productQuantityTv.text = productQuantity
        productDescriptionTv.text = productDescription
        discountedPriceTv.text = discountPrice
        originalPriceTv.text = productPrice
        discountNoteTv.text = discountNote
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
            val title = productNameTv.text.toString().trim()
            val priceEach = price
            val totalPrice = finalPrice.text.toString().trim().replace("Rs", "")
            val quantity = quantityTv.text.toString().trim()
            val image = productImage.trim()
            cartItem.add(CartItemModel(title,productId,priceEach,totalPrice,quantity,image))
            addToCart(productId, priceEach, totalPrice, quantity,title,image)
//            //addToCart(productId, priceEach, totalPrice, quantity,title)

            dialog.dismiss()
        }
    }

   // private var itemId = 1
    private fun addToCart(productId: String, priceEach: String, totalPrice: String, quantity: String,title: String,image:String) {
       viewModel.insert(Cart(image,title,totalPrice,priceEach,quantity,productId))
//        itemId++
//        val easyDB = EasyDB.init(this, "ITEM_DB")
//            .setTableName("ITEM_TABLE")
//            .addColumn(Column("Item_Id", "text", "unique"))
//            .addColumn(Column("Item_Name", "text", "not null"))
//            .addColumn(Column("Item_PID", "text", "not null"))
//            .addColumn(Column("Item_Price_Each", "text", "not null"))
//            .addColumn(Column("Item_Price", "text", "not null"))
//            .addColumn(Column("Item_Quantity", "text", "not null"))
//            .addColumn(Column("Item_Image", "text", "not null"))
//            .doneTableColumn()
//
//        val done: Boolean = easyDB.addData("Item_Id", itemId)
//            .addData("Item_PID", productId)
//            .addData("Item_Name", title)
//            .addData("Item_Price_Each", priceEach)
//            .addData("Item_Price", totalPrice)
//            .addData("Item_Quantity", quantity)
//            .addData("Item_Image", image)
//            .doneDataAdding()
        Toast.makeText(this, "Product added to cart", Toast.LENGTH_SHORT).show()
    }


}