package com.ashish.grocery.user

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashish.grocery.Constants
import com.ashish.grocery.Constants.Companion.SERVER_KEY
import com.ashish.grocery.R
import com.ashish.grocery.databinding.ActivityCartBinding
import com.ashish.grocery.notification.NotificationData
import com.ashish.grocery.notification.PushNotification
import com.ashish.grocery.notification.RetrofitInstance
import com.ashish.grocery.user.adapters.CartItemAdapter
import com.ashish.grocery.user.adapters.ICartAdapter
import com.ashish.grocery.user.cart.cartDb.Cart
import com.ashish.grocery.user.cart.cartDb.CartDatabase
import com.ashish.grocery.user.cart.cartDb.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

const val TOPIC="topic/PUSH_NOTIFICATION"
class CartActivity : AppCompatActivity(), ICartAdapter {

    val TAG="MainActivity"

    private lateinit var viewModel: CartViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityCartBinding
    private var cartItem: ArrayList<Cart> = ArrayList()
    private lateinit var cartItemAdapter: CartItemAdapter
    private var allTotalPrice: Double = 0.0
    private lateinit var shopUid: String
    lateinit var deliveryFee: String
    private lateinit var myLat: String
    private lateinit var myPhone: String
    private lateinit var name: String
    private lateinit var myLong: String
    private var aTotal: Double = 0.0
    private lateinit var list: List<Cart>
    //private var cart: ArrayList<Cart> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCancelable(false)
        firebaseAuth = FirebaseAuth.getInstance()
        shopUid = "mRlqasSfeIRBmCCeXYumuGYC9vE2"

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(CartViewModel::class.java)
        deliveryFee = "100"
        loadMyInfo()
        loadShopDetails()
        showCart()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }


    private fun submitOrder() {
        progressDialog.setMessage("Placing order...")
        progressDialog.show()
        viewModel.list.observe(this, Observer { cartItem ->

            for (i in cartItem.indices) {
                val cost = cartItem[i].price
                aTotal += cost.replace("Rs", "").toDouble()
            }
        })


        val timeStamp = "" + System.currentTimeMillis()
        val costs = aTotal + deliveryFee.toDouble()
        val hashMap = HashMap<String, String>()
        hashMap["orderId"] = "" + timeStamp
        hashMap["orderTime"] = "" + timeStamp
        hashMap["orderStatus"] = "In progress"
        hashMap["orderBy"] = "" + firebaseAuth.uid
        hashMap["userName"] = "" + name
        hashMap["orderTo"] = "" + shopUid
        hashMap["orderCost"] = "" + costs
        hashMap["latitude"] = "" + myLat
        hashMap["longitude"] = "" + myLong

        val ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid)
            .child("Orders")
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener {

            viewModel.list.observe(this, Observer { cartItem ->
                list = cartItem
            })

            for (i in list.indices) {
                val pId = list[i].pId
                val id = list[i].id
                val cost = list[i].price
                val name = list[i].name
                val price = list[i].priceEach
                val quantity = list[i].quantity


                val hashMap = HashMap<String, String>()
                hashMap["pId"] = "" + pId
                hashMap["id"] = "" + id
                hashMap["cost"] = "" + cost
                hashMap["name"] = "" + name
                hashMap["price"] = "" + price
                hashMap["quantity"] = "" + quantity

                ref.child(timeStamp).child("Items").child(name).setValue(hashMap)
                progressDialog.dismiss()


                makeText(this, "Order Placed Successfully", LENGTH_SHORT).show()
                deleteAllFromDb()

            }


        }.addOnFailureListener {
            progressDialog.dismiss()
            makeText(this, it.message.toString(), LENGTH_SHORT).show()
        }
    }

    fun deleteAllFromDb(){
       val thread = Thread {
           CartDatabase.getDatabase(applicationContext).cartDao().delAllItem()
       }
       thread.start()

    }

    private fun loadShopDetails() {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild(shopUid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    deliveryFee = "" + ds.child("deliveryFee").value
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun showCart() {
        cartItemAdapter = CartItemAdapter(this, this)
        binding.cartRv.adapter = cartItemAdapter


        viewModel.list.observe(this, Observer { list ->
            Log.d("Size", list?.size.toString())
            list?.let {
                cartItemAdapter.update(it)
            }

        })

        binding.confirmBtn.setOnClickListener {

            takeOrder()
        }

    }

    fun takeOrder() {
        val cart = ArrayList<Cart>()
        val dialog = Dialog(this)
//        if (dialog.window!=null){
//            dialog.window!!.setBackgroundDrawableResource(0)
//        }
        dialog.setContentView(R.layout.dialog_confirm_order)
        val sTotal: TextView = dialog.findViewById(R.id.sTotalAmt)
        val allTotalPriceTv: TextView = dialog.findViewById(R.id.allTotalPriceTv)
        val deliveryFeeAmt: TextView = dialog.findViewById(R.id.deliveryFeeAmt)
        val okBtn: TextView = dialog.findViewById(R.id.OkBtn)
//        Log.d("Items",cart.size.toString())

        //viewModel=ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(CartViewModel::class.java)
        viewModel.list.observe(this, Observer { list ->
            //Log.d("Size", list?.size.toString())
            for (i in list.indices) {
                val prices = list[i].price
                Log.d("Price", prices)
                allTotalPrice += (prices.replace("Rs", "")).toDouble()

            }

        })


        for (i in cart.indices) {
            val prices = cart[i].price
            //Log.d("Price", prices)
            allTotalPrice += (prices.replace("Rs", "")).toDouble()

        }
        Log.d("AllTotal", allTotalPrice.toString())
        sTotal.text = getString(R.string.rs, String.format("%.2f", allTotalPrice))
        deliveryFeeAmt.text = getString(R.string.rs, deliveryFee)
        allTotalPriceTv.text =
            getString(R.string.rs, (allTotalPrice + deliveryFee.toDouble()).toString())
        okBtn.setOnClickListener {
            if (myLat == "" || myLat == "null" || myLong == "" || myLong == "null") {
                Toast.makeText(
                    this,
                    "Please enter address in your profile before placing order",
                    LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (myPhone == "" || myPhone == "null") {
                Toast.makeText(
                    this,
                    "Please enter phone in your profile before placing order",
                    LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.list.observe(this, Observer { list ->
                if (list.isEmpty()) {
                    Toast.makeText(this, "No item in cart", LENGTH_SHORT).show()
                    return@Observer
                }
            })
//            if (list.size == 0) {
//                Toast.makeText(this, "No item in cart", LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
            if (allTotalPrice < 100) {
                Toast.makeText(this, "Minimum order should be of Rs 100", LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitOrder()
            dialog.dismiss()
        }

        dialog.show()

    }


    private fun loadMyInfo() {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild("uid").equalTo(firebaseAuth.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    name = "" + ds.child("name").value
                    myPhone = "" + ds.child("phone").value
                    myLat = "" + ds.child("latitude").value
                    myLong = "" + ds.child("longitude").value
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onItemClicked(car: Cart) {
        viewModel.delete(car)
        Toast.makeText(this, "Item deleted from Cart", Toast.LENGTH_SHORT).show()
    }

    fun cart() {
        val thread = Thread {
            val todoList: LiveData<List<Cart>> = CartDatabase.getDatabase(
                applicationContext
            )
                .cartDao()
                .getAllItem()
            Log.d("TAG", "run: $todoList")
        }
        thread.start()
    }

//    fun sendNotification(notification: PushNotification)= CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val response=RetrofitInstance.api.pushNotification(notification)
//            if (response.isSuccessful){
////                Log.d{TAG,"Response: ${Gson().toJson(response)}"}
//            }else{
////                Log.e{TAG, response.errorBody().toString()}
//            }
//        }catch (e:Exception){
////            Log.e{TAG,e.toString()}
//        }
//    }

    fun prepareNotification(orderId:String){
        val NOTIFICATION_TITLE="Congratulation you have new Order!!"
        val NOTIFICATION_TOPIC=TOPIC
        val NOTIFICATION_MESSAGE="New order $orderId"
        val NOTIFICATION_TYPE="New Order"

        var notificationJo= JSONObject()
        var notificationBodyJo= JSONObject()

        try {
            //Notification Data
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE)
            notificationBodyJo.put("buyerUid",firebaseAuth.uid)
            notificationBodyJo.put("sellerUid",shopUid)

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC)
            notificationJo.put("data",notificationBodyJo)
        }catch (e:Exception){
            Toast.makeText(this,e.message.toString(),Toast.LENGTH_SHORT).show()
        }
        sendNotification(notificationJo,orderId)
    }

    private fun sendNotification(notificationJo: JSONObject, orderId: String) {
        val url = "https://fcm.googleapis.com/fcm/send"

        val jsonObject = object : JsonObjectRequest(url,
            notificationJo,
            Response.Listener { response ->

            }, Response.ErrorListener { error -> // method to handle errors.

            }) {
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = java.util.HashMap()
                header["Content-Type"] = "application-json"
                header["Authorization"] = "key$SERVER_KEY"
                return header
            }
        }
        Volley.newRequestQueue(this).add(jsonObject)
    }
}