package com.ashish.grocery.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ashish.grocery.databinding.ActivityMainShopBinding
import com.ashish.grocery.databinding.ActivityOrdersBinding
import com.ashish.grocery.user.adapters.OrderUserAdapter
import com.ashish.grocery.user.models.OrderUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrdersActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding:ActivityOrdersBinding
    private var orderList: ArrayList<OrderUserModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        firebaseAuth = FirebaseAuth.getInstance()
        loadOrder()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }
    private fun loadOrder() {
        val ref= FirebaseDatabase.getInstance().getReference("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (ds in snapshot.children){
                    val uid=""+ds.ref.key
                    val dbRef= FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders")
                    dbRef.orderByChild("orderBy").equalTo(firebaseAuth.uid).addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                for (dp in snapshot.children){
                                    val order=dp.getValue(OrderUserModel::class.java)
                                    orderList.add(order!!)
                                }
                                val orderAdapter= OrderUserAdapter(this@OrdersActivity,orderList)
                                binding.ordersRv.adapter=orderAdapter
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}