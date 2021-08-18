@file:Suppress("DEPRECATION")

package com.ashish.grocery.seller

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.seller.adpaters.OrderShopAdapter
import com.ashish.grocery.ui.LoginActivity
import com.ashish.grocery.seller.adpaters.ProductAdapter
import com.ashish.grocery.seller.models.OrderShopModel
import com.ashish.grocery.seller.models.ProductsModel
import com.ashish.grocery.ui.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class MainSellerActivity : AppCompatActivity() {
    private lateinit var nameTv: TextView
    private lateinit var shopNameTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var productsTv: TextView
    private lateinit var ordersTv: TextView
    private lateinit var logoutBtn: ImageButton
    private lateinit var editBtn: ImageButton
    private lateinit var addProductBtn: ImageButton
    private lateinit var settingsBtn: ImageButton
    private lateinit var filterBtn: ImageButton
    private lateinit var filterOrderBtn: ImageButton
    private lateinit var profileImg: ImageView
    private lateinit var searchBar: EditText
    private lateinit var searchOrder: EditText
    private lateinit var productsUI: RelativeLayout
    private lateinit var ordersUI: RelativeLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var ordersRv: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var listData: ArrayList<ProductsModel> = ArrayList()
    private var productList: ArrayList<ProductsModel> = ArrayList()
    private var adapter: ProductAdapter? = null
    private var productAdapter: ProductAdapter? = null
    private lateinit var orderList: ArrayList<OrderShopModel>
    private lateinit var orderShopAdapter: OrderShopAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_seller)

        init()
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        checkUser()
        showProductsUI()
        loadAllProducts()
        loadAllOrders()
        logoutBtn.setOnClickListener {
            makeMeOffline()
        }

        editBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileSellerActivity::class.java))
        }

        addProductBtn.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }
        settingsBtn.setOnClickListener {
            val intent=Intent(this,SettingsActivity::class.java)
            startActivity(intent)
        }
        productsTv.setOnClickListener {
            showProductsUI()
        }
        ordersTv.setOnClickListener {
            showOrdersUI()
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                filter(text.toString())
            }

            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })
    }

    private fun loadAllOrders() {
        orderList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Orders")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderList.clear()
                    for (ds in snapshot.children) {
                        val orderModel = ds.getValue(OrderShopModel::class.java)
                        orderList.add(orderModel!!)
                    }
                    orderShopAdapter = OrderShopAdapter(this@MainSellerActivity, orderList)
                    ordersRv.adapter = orderShopAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadAllProducts() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(firebaseAuth.uid!!).child("Products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productList.clear()
                    for (ds in snapshot.children) {
                        val productModel = ds.getValue(ProductsModel::class.java)!!
                        productList.add(productModel)
                    }
                    productAdapter = ProductAdapter(this@MainSellerActivity, productList)
                    recyclerView.adapter = productAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainSellerActivity, error.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun showOrdersUI() {
        productsUI.visibility = View.GONE
        ordersUI.visibility = View.VISIBLE
        ordersTv.setTextColor(resources.getColor(R.color.black))
        productsTv.setTextColor(resources.getColor(R.color.white))
        ordersTv.setBackgroundResource(R.drawable.shape_rect03)
        productsTv.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun showProductsUI() {
        productsUI.visibility = View.VISIBLE
        ordersUI.visibility = View.GONE
        productsTv.setTextColor(resources.getColor(R.color.black))
        ordersTv.setTextColor(resources.getColor(R.color.white))
        productsTv.setBackgroundResource(R.drawable.shape_rect03)
        ordersTv.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun makeMeOffline() {
        progressDialog.setMessage("Logging Out...")
        val hashMap = hashMapOf<String, String>()
        hashMap["online"] = "false"
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(firebaseAuth.uid!!).updateChildren(hashMap as Map<String, Any>)
            .addOnSuccessListener {
                firebaseAuth.signOut()
                checkUser()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                //Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            loadMyInfo()
        }
    }

    private fun loadMyInfo() {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild("uid").equalTo(firebaseAuth.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val name: String = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    val shopName = "" + ds.child("shopName").value
                    val img = "" + ds.child("profileImage").value
                    nameTv.text = name
                    emailTv.text = email
                    shopNameTv.text = shopName
                    profileImg.load(img) {
                        placeholder(R.drawable.ic_person_gray)
                    }

                    try {
                        Picasso.get().load(img).placeholder(R.drawable.ic_person_gray)
                            .into(profileImg)
                    } catch (e: Exception) {
                        profileImg.setImageResource(R.drawable.ic_person_gray)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
        nameTv = findViewById(R.id.nameSeller)
        logoutBtn = findViewById(R.id.logoutBtn)
        editBtn = findViewById(R.id.editBtn)
        addProductBtn = findViewById(R.id.addCartBtn)
        shopNameTv = findViewById(R.id.shopNameTv)
        emailTv = findViewById(R.id.emailTv)
        profileImg = findViewById(R.id.profileImg)
        ordersTv = findViewById(R.id.orders)
        productsTv = findViewById(R.id.products)
        ordersUI = findViewById(R.id.ordersUI)
        productsUI = findViewById(R.id.productsUI)
        searchBar = findViewById(R.id.searchBar)
        filterBtn = findViewById(R.id.filterBar)
        recyclerView = findViewById(R.id.productRv)
        ordersRv = findViewById(R.id.ordersRv)
        filterOrderBtn = findViewById(R.id.filterOrder)
        searchOrder = findViewById(R.id.searchOrder)
        settingsBtn = findViewById(R.id.settingsBtn)
    }
}