package com.ashish.grocery.seller

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashish.grocery.Constants
import com.ashish.grocery.Constants.Companion.SERVER_KEY
import com.ashish.grocery.R
import com.ashish.grocery.user.adapters.OrderItemAdapter
import com.ashish.grocery.user.models.OrderItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.util.*

class OrderDetailSellerActivity : AppCompatActivity() {
    private lateinit var orderId:String
    private lateinit var orderBy:String
    private lateinit var destinationLatitude:String
    private lateinit var destinationLongitude:String
    private lateinit var orderDate: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var mapBtn: ImageButton
    private lateinit var editBtn: ImageButton
    private lateinit var orderIdTv: TextView
    private lateinit var orderStatusTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var itemCountTv: TextView
    private lateinit var amountTv: TextView
    private lateinit var deliveryAddressTv: TextView
    private lateinit var deliveryFee: String
    private lateinit var itemRv: RecyclerView
    private var orderItem: ArrayList<OrderItemModel> = ArrayList()
    private var orderItemAdapter: OrderItemAdapter? = null
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail_seller)
        init()
        orderId=intent.getStringExtra("orderId")!!
        orderBy=intent.getStringExtra("orderBy")!!
        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()
        loadBuyerInfo()
        loadOrderDetail()
        loadOrderedItems()
        backBtn.setOnClickListener {
            onBackPressed()
        }
        mapBtn.setOnClickListener {
            openMap()
        }
        editBtn.setOnClickListener {
            editOrderStatusDialog()
        }
    }

    private fun editOrderStatusDialog() {
        val option= arrayOf("In progress","Completed","Cancelled")
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Change order Status")
            .setItems(option) { _, i ->
                val selectedOption=option[i]
                editOrderStatus(selectedOption)
            }.show()
    }

    private fun editOrderStatus(selectedOption: String) {
        val message="Order is now $selectedOption"
        val hashMap = HashMap<String, String>()
        hashMap["orderStatus"]=""+selectedOption
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Orders").child(orderId).updateChildren(hashMap as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
                prepareNotificationMessage(orderId,message)
            }.addOnFailureListener {
                Toast.makeText(this,it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadOrderedItems() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Orders").child(orderId).child("Items")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderItem.clear()
                    for (ds in snapshot.children) {
                        val orderItemModel = ds.getValue(OrderItemModel::class.java)
                        orderItem.add(orderItemModel!!)
                    }
                    orderItemAdapter = OrderItemAdapter(this@OrderDetailSellerActivity, orderItem)
                    itemRv.adapter = orderItemAdapter
                    val count=snapshot.childrenCount
                    itemCountTv.text=count.toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadOrderDetail() {
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Orders").child(orderId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderStatus=""+snapshot.child("orderStatus").value
                val orderBy=""+snapshot.child("orderBy").value
                val date=""+snapshot.child("orderTime").value
                val orderCost=""+snapshot.child("orderCost").value
                val orderId=""+snapshot.child("orderId").value
                val latitude=""+snapshot.child("latitude").value
                val longitude=""+snapshot.child("longitude").value

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = date.toLong()
                val dateFormat = android.text.format.DateFormat.format("dd-MMM-yyyy", calendar).toString()
                orderDate.text=dateFormat
                when (orderStatus) {
                    "In progress" -> {
                        orderStatusTv.setTextColor(this@OrderDetailSellerActivity.resources.getColor(R.color.inProgress))
                    }
                    "Completed" -> {
                        orderStatusTv.setTextColor(this@OrderDetailSellerActivity.resources.getColor(R.color.green))
                    }
                    "Cancelled" -> {
                        orderStatusTv.setTextColor(this@OrderDetailSellerActivity.resources.getColor(R.color.red))
                    }
                }
                orderStatusTv.text=orderStatus
                orderIdTv.text=orderId
                amountTv.text=getString(R.string.rs_400, orderCost, deliveryFee)
                findAddress(latitude, longitude)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun findAddress(latitude: String, longitude: String) {
        val lat = latitude.toDouble()
        val lon = longitude.toDouble()
        val addresses: List<Address>
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1)
            val address = addresses[0].getAddressLine(0)
            deliveryAddressTv.text = address
        } catch (e: Exception) {
            deliveryAddressTv.text = e.toString()
        }
    }

    private fun openMap() {
        val googleMapsUrl = "google.navigation:q=$destinationLatitude,$destinationLongitude"
        val uri = Uri.parse(googleMapsUrl)

        val googleMapsPackage = "com.google.android.apps.maps"
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(googleMapsPackage)
        }
        startActivity(intent)
    }

    private fun loadMyInfo() {
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //sourceLatitude=(""+snapshot.child("latitude").value).toDouble()
                //sourceLongitude=(""+snapshot.child("longitude").value).toDouble()
                deliveryFee = "" + snapshot.child("deliveryFee").value
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadBuyerInfo() {
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(orderBy).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                destinationLatitude=""+snapshot.child("latitude").value
                destinationLongitude=""+snapshot.child("longitude").value
                val email=""+snapshot.child("name").value
                val phone=""+snapshot.child("phone").value
                emailTv.text=email
                phoneTv.text=phone
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun prepareNotificationMessage(orderId: String,message:String) {
        val NOTIFICATION_TOPIC = "/topics/" + Constants().FCM_KEY
        val NOTIFICATION_TITLE = "Your order$orderId"
        val NOTIFICATI0N_MESSAGE = ""+message
        val NOTIFICATIN_TYPE = "OrderStatusChanged"

        val notificationJo = JSONObject()
        val notificationBodyJo = JSONObject()
        try {
            notificationBodyJo.put("notificationType", NOTIFICATIN_TYPE)
            notificationBodyJo.put("buyerUid", orderBy)
            notificationBodyJo.put("sellerUid", firebaseAuth.uid)
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE)
            notificationBodyJo.put("notificationMessage", NOTIFICATI0N_MESSAGE)
            notificationJo.put("to", NOTIFICATION_TOPIC)
            notificationJo.put("data", notificationBodyJo)
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
        sendNotification(notificationJo)
    }
    private fun sendNotification(notificationJo: JSONObject) {
        // url to post our data
        val url = "https://fcm.googleapis.com/fcm/send"

        val jsonObject = object : JsonObjectRequest(url,
            notificationJo,
            Response.Listener { response ->

            }, Response.ErrorListener { error -> // method to handle errors.

            }) {
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Content-Type"] = "application-json"
                header["Authorization"] = "key$SERVER_KEY"
                return header
            }
        }
        Volley.newRequestQueue(this).add(jsonObject)
    }

    private fun init() {
        backBtn = findViewById(R.id.backBtn)
        editBtn = findViewById(R.id.editBtn)
        mapBtn = findViewById(R.id.mapBtn)
        orderIdTv = findViewById(R.id.orderId)
        orderStatusTv = findViewById(R.id.orderStatus)
        emailTv = findViewById(R.id.email)
        phoneTv = findViewById(R.id.phone)
        orderDate = findViewById(R.id.date)
        itemCountTv = findViewById(R.id.itemCount)
        amountTv = findViewById(R.id.amount)
        deliveryAddressTv = findViewById(R.id.deliveryAddress)
        itemRv = findViewById(R.id.itemRv)
    }
}