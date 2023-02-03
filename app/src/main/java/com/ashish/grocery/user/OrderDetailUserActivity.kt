package com.ashish.grocery.user

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashish.grocery.R
import com.ashish.grocery.user.adapters.OrderItemAdapter
import com.ashish.grocery.user.models.OrderItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class OrderDetailUserActivity : AppCompatActivity() {
    private lateinit var orderId: String
    private lateinit var orderTo: String
    private lateinit var orderDate: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var reviewBtn: ImageButton
    private lateinit var orderIdTv: TextView
    private lateinit var orderStatusTv: TextView
    private lateinit var shopNameTv: TextView
    private lateinit var itemCountTv: TextView
    private lateinit var amountTv: TextView
    private lateinit var deliveryAddressTv: TextView
    private lateinit var deliveryFee: String
    private lateinit var itemRv: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private var orderItem: ArrayList<OrderItemModel> = ArrayList()
    private var orderItemAdapter: OrderItemAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail_user)
        init()
        orderId = intent.getStringExtra("orderId")!!
        orderTo = intent.getStringExtra("orderTo")!!
        firebaseAuth = FirebaseAuth.getInstance()
        loadShopInfo()
        loadOrderDetails()
        loadOrderedItems()
        backBtn.setOnClickListener {
            onBackPressed()
        }
        reviewBtn.setOnClickListener {
            val intent=Intent(this,WriteReviewActivity::class.java)
            intent.putExtra("shopUid",orderTo)
            startActivity(intent)
        }
    }

    private fun loadOrderedItems() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderItem.clear()
                    for (ds in snapshot.children) {
                        val orderItemModel = ds.getValue(OrderItemModel::class.java)
                        orderItem.add(orderItemModel!!)
                    }
                    orderItemAdapter = OrderItemAdapter(this@OrderDetailUserActivity, orderItem)
                    itemRv.adapter = orderItemAdapter
                    val count=snapshot.childrenCount
                    itemCountTv.text=count.toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadOrderDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(orderTo).child("Orders").child(orderId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orderBy = "" + snapshot.child("orderBy").value
                    val orderCost = "" + snapshot.child("orderCost").value
                    val orderId = "" + snapshot.child("orderId").value
                    val orderTo = "" + snapshot.child("orderTo").value
                    //val deliveryFee=""+snapshot.child("deliveryFee").value
                    val longitude = "" + snapshot.child("longitude").value
                    val latitude = "" + snapshot.child("latitude").value
                    val orderStatus = "" + snapshot.child("orderStatus").value
                    val orderTime = "" + snapshot.child("orderTime").value
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = orderTime.toLong()
                    val dateFormat =
                        android.text.format.DateFormat.format("dd/MM/yyyy hh:mm a", calendar)
                            .toString()
                    when (orderStatus) {
                        "In progress" -> {
                            orderStatusTv.setTextColor(resources.getColor(R.color.inProgress))
                        }
                        "Completed" -> {
                            orderStatusTv.setTextColor(resources.getColor(R.color.green))
                        }
                        "Cancelled" -> {
                            orderStatusTv.setTextColor(resources.getColor(R.color.red))
                        }
                    }
                    orderIdTv.text = orderId
                    orderDate.text = dateFormat
                    orderStatusTv.text = orderStatus
                    amountTv.text = getString(R.string.rs_400, orderCost, deliveryFee)
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
            addresses =
                geocoder.getFromLocation(lat, lon, 1) as List<Address?>
            val address = addresses[0]?.getAddressLine(0)
            deliveryAddressTv.text = address
        } catch (e: Exception) {
            deliveryAddressTv.text = e.toString()
        }
    }

    private fun loadShopInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(orderTo).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shopName = "" + snapshot.child("shopName").value
                deliveryFee = "" + snapshot.child("deliveryFee").value
                shopNameTv.text = shopName
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun init() {
        backBtn = findViewById(R.id.backBtn)
        reviewBtn = findViewById(R.id.reviewBtn)
        orderIdTv = findViewById(R.id.orderId)
        orderStatusTv = findViewById(R.id.orderStatus)
        shopNameTv = findViewById(R.id.shopName)
        orderDate = findViewById(R.id.date)
        itemCountTv = findViewById(R.id.itemCount)
        amountTv = findViewById(R.id.amount)
        deliveryAddressTv = findViewById(R.id.deliveryAddress)
        itemRv = findViewById(R.id.itemRv)
    }
}