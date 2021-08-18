@file:Suppress("DEPRECATION")

package com.ashish.grocery.user

import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashish.grocery.R
import com.ashish.grocery.seller.adpaters.ProductAdapter
import com.ashish.grocery.seller.models.ProductsModel
import com.ashish.grocery.user.adapters.CartItemAdapter
import com.ashish.grocery.user.adapters.UserProductAdapter
import com.ashish.grocery.user.models.CartItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject
import p32929.androideasysql_library.Column
import p32929.androideasysql_library.EasyDB


class ShopDetailActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageButton
    private lateinit var cartBtn: ImageButton
    private lateinit var callBtn: ImageButton
    private lateinit var mapBtn: ImageButton
    private lateinit var shopImg: ImageView
    private lateinit var reviewBtn: ImageView
    private lateinit var shopNameTv: TextView
    private lateinit var cartCountTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var addressTv: TextView
    private lateinit var openTv: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var deliveryFeesTv: TextView
    private lateinit var searchBar: EditText
    private lateinit var productRv: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private var shopUid: String? = null
    private lateinit var myLat: String
    private lateinit var myPhone: String
    private lateinit var shopLat: String
    private lateinit var myLong: String
    private lateinit var shopLong: String
    private lateinit var shopEmail: String
    private lateinit var shopName: String
    private lateinit var shopPhone: String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var shopAddress: String
    lateinit var deliveryFee: String
    private lateinit var easyDB: EasyDB

    //cart
    private var cartItem: ArrayList<CartItemModel>? = null
    private var cartItemAdapter: CartItemAdapter? = null

    //private var productList: ArrayList<ProductsModel> = ArrayList()
    private var adapter: ProductAdapter? = null
    private var productAdapter: UserProductAdapter? = null
    private var listData: ArrayList<ProductsModel> = ArrayList()
    val FCM_KEY="AAAAEP5drBM:APA91bE9bAVoPXhzPiVvOGnLXfP2q5cp0toEhf6_xrDxUdYnQfDEcLCnrQXmjTfFtZp48evEttEILsESCdxsteZfOp3TvoQfvHchnqxY14I7ubMwJMP1oXGxOT9-TpWX44N75E4z0eB8"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_detail)
        init()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCancelable(false)
        firebaseAuth = FirebaseAuth.getInstance()
        shopUid = "mRlqasSfeIRBmCCeXYumuGYC9vE2"
        Log.d("shopId", shopUid!!)
        easyDB = EasyDB.init(this, "ITEM_DB")
            .setTableName("ITEM_TABLE")
            .addColumn(Column("Item_Id", "text", "unique"))
            .addColumn(Column("Item_PID", "text", "not null"))
            .addColumn(Column("Item_Name", "text", "not null"))
            .addColumn(Column("Item_Price_Each", "text", "not null"))
            .addColumn(Column("Item_Price", "text", "not null"))
            .addColumn(Column("Item_Quantity", "text", "not null"))
            .doneTableColumn()
        loadMyInfo()
        loadShopDetails()
        loadShopProducts()
        loadReviews()
        deleteCart()
        cartCount()
        backBtn.setOnClickListener {
            onBackPressed()
        }
        cartBtn.setOnClickListener {
            showCartDialog()
        }
        callBtn.setOnClickListener {
            dailPhone()
        }
        mapBtn.setOnClickListener {
            openMap()
        }
        reviewBtn.setOnClickListener {
            val intent = Intent(this, ShopReviewActivity::class.java)
            intent.putExtra("shopUid", shopUid)
            startActivity(intent)
        }
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
                filter(text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })
    }

    private fun deleteCart() {

        easyDB.deleteAllDataFromTable()
    }

    fun cartCount() {

        val count = easyDB.allData.count
        if (count <= 0) {
            cartCountTv.visibility = View.GONE
        } else {
            cartCountTv.visibility = View.VISIBLE
            cartCountTv.text = count.toString()
        }
    }

    private var ratingSum: Float = 0.0F
    private fun loadReviews() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(shopUid!!).child("Ratings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ratingSum = 0F
                for (ds in snapshot.children) {
                    val rating = ("" + ds.child("ratings").value).toFloat()
                    ratingSum += rating
                }
                val numberOfReview = snapshot.childrenCount
                val avg = ratingSum / numberOfReview
                ratingBar.rating = avg

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    lateinit var dFeeTv: TextView
    lateinit var sTotalTv: TextView
    var allTotalPriceTv: TextView? = null
    var allTotalPrice: Double = 0.0
    fun showCartDialog() {
        cartItem = ArrayList()
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null)
        val shopNameTv: TextView = view.findViewById(R.id.shopName)
        val cartRv: RecyclerView = view.findViewById(R.id.cartItemRv)
        sTotalTv = view.findViewById(R.id.stotalAmt)
        dFeeTv = view.findViewById(R.id.deliveryFeeAmt)
        allTotalPriceTv = view.findViewById(R.id.allTotalPriceTv)
        val confirmBtn: Button = view.findViewById(R.id.confirm)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        shopNameTv.text = shopName

        val easyDB = EasyDB.init(this, "ITEM_DB")
            .setTableName("ITEM_TABLE")
            .addColumn(Column("Item_Id", "text", "unique"))
            .addColumn(Column("Item_PID", "text", "not null"))
            .addColumn(Column("Item_Name", "text", "not null"))
            .addColumn(Column("Item_Price_Each", "text", "not null"))
            .addColumn(Column("Item_Price", "text", "not null"))
            .addColumn(Column("Item_Quantity", "text", "not null"))
            .addColumn(Column("Item_Image", "text", "not null"))
            .doneTableColumn()

        val res: Cursor = easyDB.allData
        while (res.moveToNext()) {
            val id: String = res.getString(1)
            val pId: String = res.getString(2)
            val name: String = res.getString(3)
            val price: String = res.getString(4)
            val cost: String = res.getString(5)
            val quantity: String = res.getString(6)
            //val image: String = res.getString(7)

            allTotalPrice += cost.toDouble()
            val cartItemModel = CartItemModel(
                "" + id, "" + pId, "" + name, "" + price, "" + cost, "" + quantity
            )
            //cartItem.clear()
            cartItem!!.add(cartItemModel)
        }
        //cartItemAdapter = CartItemAdapter(this, cartItem!!)
        cartRv.adapter = cartItemAdapter
        dFeeTv.text = getString(R.string.rs, deliveryFee)
        sTotalTv.text = getString(R.string.rs, String.format("%.2f", allTotalPrice))
        allTotalPriceTv?.text = getString(
            R.string.rs,
            (allTotalPrice + deliveryFee.replace("Rs", "").toDouble()).toString()
        )

        val dialog = builder.create()
        dialog.show()
        dialog.setOnCancelListener {
            allTotalPrice = 0.0
        }

        confirmBtn.setOnClickListener {
            if (myLat == "" || myLat == "null" || myLong == "" || myLong == "null") {
                Toast.makeText(
                    this,
                    "Please enter address in your profile before placing order",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (myPhone == "" || myPhone == "null") {
                Toast.makeText(
                    this,
                    "Please enter phone in your profile before placing order",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (cartItem!!.size == 0) {
                Toast.makeText(this, "No item in cart", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (allTotalPrice < 100) {
                Toast.makeText(this, "Minimum order should be Rs 100", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitOrder()
        }
    }

    private fun submitOrder() {
        progressDialog.setMessage("Placing order...")
        progressDialog.show()
        val timeStamp = "" + System.currentTimeMillis()
        val cost = allTotalPriceTv?.text.toString().trim().replace("Rs", "")
        val hashMap = HashMap<String, String>()
        hashMap["orderId"] = "" + timeStamp
        hashMap["orderTime"] = "" + timeStamp
        hashMap["orderStatus"] = "In progress"
        hashMap["orderBy"] = "" + firebaseAuth.uid
        hashMap["orderTo"] = "" + shopUid
        hashMap["orderCost"] = "" + cost
        hashMap["latitude"] = "" + myLat
        hashMap["longitude"] = "" + myLong

        val ref =
            FirebaseDatabase.getInstance().getReference("Users").child(shopUid!!).child("Orders")
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener {
            for (i in cartItem!!.indices) {
                val pId = cartItem!![i].pId
                //val id = cartItem!![i].id
                val cost = cartItem!![i].cost
                val name = cartItem!![i].name
                val price = cartItem!![i].price
                val quantity = cartItem!![i].quantity

                val hashMap = HashMap<String, String>()
                hashMap["pId"] = "" + name
                //hashMap["id"] = "" + id
                hashMap["cost"] = "" + cost
                hashMap["name"] = "" + pId
                hashMap["price"] = "" + price
                hashMap["quantity"] = "" + quantity
                ref.child(timeStamp).child("Items").child(pId).setValue(hashMap)
                progressDialog.dismiss()
                Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show()
                prepareNotificationMessage(timeStamp)

            }

        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMap() {
        val googleMapsUrl = "google.navigation:q=$shopLat,$shopLong"
        val uri = Uri.parse(googleMapsUrl)

        val googleMapsPackage = "com.google.android.apps.maps"
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(googleMapsPackage)
        }
        startActivity(intent)
    }

    private fun dailPhone() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:" + Uri.encode(shopPhone))
        startActivity(intent)
    }

    private fun loadShopProducts() {
        val productList = ArrayList<ProductsModel>()
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(shopUid!!).child("Products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (ds in snapshot.children) {
                    val productsModel = ds.getValue(ProductsModel::class.java)
                    productList.add(productsModel!!)
                }
                productAdapter = UserProductAdapter(this@ShopDetailActivity, productList)
                productRv.adapter = productAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadShopDetails() {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild(shopUid!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val name = "" + ds.child("name").value
                    shopName = "" + ds.child("shopName").value
                    shopEmail = "" + ds.child("email").value
                    shopAddress = "" + ds.child("address").value
                    shopPhone = "" + ds.child("phone").value
                    shopLong = "" + ds.child("longitude").value
                    shopLat = "" + ds.child("latitude").value
                    deliveryFee = "" + ds.child("deliveryFee").value
                    val profileImg = "" + ds.child("profileImage").value
                    val shopOpen = "" + ds.child("shopOpen").value
                    shopNameTv.text = shopName
                    emailTv.text = shopEmail
                    phoneTv.text = shopPhone
                    addressTv.text = shopAddress
                    phoneTv.text = shopPhone
                    deliveryFeesTv.text = getString(R.string.delivery, deliveryFee)
                    if (shopOpen == "true") {
                        openTv.text = "Open"
                    } else {
                        openTv.text = "Closed"
                    }
                    shopImg.load(profileImg) {
                        placeholder(R.drawable.ic_shop)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadMyInfo() {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild("uid").equalTo(firebaseAuth.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val name = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    myPhone = "" + ds.child("phone").value
                    val profileImage = "" + ds.child("profileImage").value
                    val city = "" + ds.child("city").value
                    myLat = "" + ds.child("latitude").value
                    myLong = "" + ds.child("longitude").value
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun prepareNotificationMessage(orderId: String) {
        val NOTIFICATION_TOPIC = "/topics/$FCM_KEY"
        val NOTIFICATION_TITLE = "New order$orderId"
        val NOTIFICATI0N_MESSAGE = "Congratulations...! You have  new order"
        val NOTIFICATIN_TYPE = "New order"

        val notificationJo = JSONObject()
        val notificationBodyJo = JSONObject()
        try {
            notificationBodyJo.put("notificationType", NOTIFICATIN_TYPE)
            notificationBodyJo.put("buyerUid", firebaseAuth.uid)
            notificationBodyJo.put("sellerUid", shopUid)
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE)
            notificationBodyJo.put("notificationMessage", NOTIFICATI0N_MESSAGE)
            notificationJo.put("to", NOTIFICATION_TOPIC)
            notificationJo.put("data", notificationBodyJo)
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
        sendNotification(notificationJo, orderId)
    }
    private fun sendNotification(notificationJo: JSONObject, orderId: String) {
        // url to post our data
        val url = "https://fcm.googleapis.com/fcm/send"

        val jsonObject = object : JsonObjectRequest(url,
            notificationJo,
            Response.Listener { _ ->
                val intent = Intent(this, OrderDetailUserActivity::class.java)
                intent.putExtra("orderId", orderId)
                intent.putExtra("orderTo", shopUid)
                startActivity(intent)
            }, Response.ErrorListener { _ -> // method to handle errors.
                val intent = Intent(this, OrderDetailUserActivity::class.java)
                intent.putExtra("orderId", orderId)
                intent.putExtra("orderTo", shopUid)
                startActivity(intent)
            }) {
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Content-Type"] = "application-json"
                header["Authorization"] = "key$FCM_KEY"
                return header
            }
        }
        Volley.newRequestQueue(this).add(jsonObject)
    }

    fun filter(text: String) {
        val filterLists: ArrayList<ProductsModel> = ArrayList()
        for (item in listData) {
            if (item.productName.lowercase().contains(text.toLowerCase())
                or item.productCategory.lowercase().contains(text.toLowerCase())
            ) {
                filterLists.add(item)
            }
        }
        adapter?.filterList(filterLists)
    }

    private fun init() {
        backBtn = findViewById(R.id.backBtn)
        cartBtn = findViewById(R.id.cartBtn)
        callBtn = findViewById(R.id.callBtn)
        mapBtn = findViewById(R.id.mapBtn)
        shopImg = findViewById(R.id.shopImg)
        shopNameTv = findViewById(R.id.shopNameTv)
        shopNameTv = findViewById(R.id.shopNameTv)
        phoneTv = findViewById(R.id.phoneTv)
        emailTv = findViewById(R.id.emailTv)
        addressTv = findViewById(R.id.addressTv)
        openTv = findViewById(R.id.openTv)
        deliveryFeesTv = findViewById(R.id.deliveryFeeTv)
        searchBar = findViewById(R.id.searchEt)
        productRv = findViewById(R.id.productsRv)
        cartCountTv = findViewById(R.id.cartCount)
        reviewBtn = findViewById(R.id.starBtn)
        ratingBar = findViewById(R.id.ratingBar)
    }
}