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
import com.ashish.grocery.R
import com.ashish.grocery.notification.NotificationData
import com.ashish.grocery.notification.PushNotificationData
import com.ashish.grocery.notification.api.ApiUtilities
import com.ashish.grocery.user.adapters.OrderItemAdapter
import com.ashish.grocery.user.models.OrderItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Response
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
    private lateinit var messageToken: String
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
                val pushNotificationData=PushNotificationData(NotificationData("Your order status Changed",message),messageToken)
                ApiUtilities.getInstance().sendNotification(pushNotificationData).enqueue(object :retrofit2.Callback<PushNotificationData>{
                    override fun onResponse(
                        call: Call<PushNotificationData>,
                        response: Response<PushNotificationData>
                    ) {
                        Toast.makeText(this@OrderDetailSellerActivity,response.toString(),Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<PushNotificationData>, t: Throwable) {
                        Toast.makeText(this@OrderDetailSellerActivity,"Something went wrong",Toast.LENGTH_SHORT).show()
                    }

                })
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
                ""+snapshot.child("orderBy").value
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
        val addresses: List<Address?>
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1) as List<Address?>
            val address = addresses[0]?.getAddressLine(0)
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
                messageToken = "" + snapshot.child("messageToken").value
                emailTv.text=email
                phoneTv.text=phone
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
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