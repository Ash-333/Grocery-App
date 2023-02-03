package com.ashish.grocery

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import coil.load
import com.ashish.grocery.databinding.ActivityMainShopBinding
import com.ashish.grocery.seller.models.ProductsModel
import com.ashish.grocery.ui.LoginActivity
import com.ashish.grocery.ui.SettingsActivity
import com.ashish.grocery.user.CartActivity
import com.ashish.grocery.user.EditProfileUserActivity
import com.ashish.grocery.user.OrdersActivity
import com.ashish.grocery.user.adapters.CategoryAdapter
import com.ashish.grocery.user.adapters.PopularItemAdapter
import com.ashish.grocery.user.adapters.UserProductAdapter
import com.ashish.grocery.user.models.CategoryModel
import com.ashish.grocery.user.models.PopularItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainShopActivity : AppCompatActivity() {
    private lateinit var shopUid: String
    private lateinit var binding: ActivityMainShopBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private val list = ArrayList<CategoryModel>()
    private lateinit var mAdapter: CategoryAdapter
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var name: TextView
    private lateinit var img: ImageView
    private lateinit var email: TextView
    private lateinit var phNumber: TextView
    private var productAdapter: UserProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainShopBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCancelable(false)
        firebaseAuth = FirebaseAuth.getInstance()
        shopUid = "mRlqasSfeIRBmCCeXYumuGYC9vE2"
        mAdapter = CategoryAdapter(this, list)
        binding.recView.adapter = mAdapter
        prepareCategory()
        loadPopularProducts()
        loadShopProducts()


        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.open, R.string.close)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()
        val headerView = binding.navView.getHeaderView(0)
        name = headerView.findViewById(R.id.name)
        email = headerView.findViewById(R.id.email)
        phNumber = headerView.findViewById(R.id.phNumber)
        img = headerView.findViewById(R.id.profileImg)
        loadMyInfo()
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.home -> startActivity(Intent(applicationContext, MainShopActivity::class.java))
                R.id.editProfile -> startActivity(
                    Intent(
                        applicationContext,
                        EditProfileUserActivity::class.java
                    )
                )
                R.id.setting -> startActivity(
                    Intent(
                        applicationContext,
                        SettingsActivity::class.java
                    )
                )
                R.id.orders -> startActivity(Intent(applicationContext, OrdersActivity::class.java))
                R.id.logout -> logoutMe()

            }
            true
        }

        binding.navView.itemIconTintList = null
        binding.menu.setOnClickListener {
            binding.drawer.openDrawer(GravityCompat.START)
        }
        binding.cartBtn.setOnClickListener {
            val intent=Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }


    private fun logoutMe() {
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
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
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
        db.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val userName = "" + ds.child("name").value
                        val userEmail = "" + ds.child("email").value
                        val userPh = "" + ds.child("phone").value
                        val userImage = "" + ds.child("profileImage").value
                        val city = "" + ds.child("city").value
                        name.text = userName
                        email.text = userEmail
                        phNumber.text = userPh
                        img.load(userImage){
                            placeholder(R.drawable.ic_person_gray)
                        }

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun prepareCategory() {
        var cate = CategoryModel("Atta,oil & grains", R.drawable.atta_oil, R.color.one)
        list.add(cate)
        cate = CategoryModel("Biscuit and snacks", R.drawable.snacks, R.color.two)
        list.add(cate)
        cate = CategoryModel("Personal care", R.drawable.personal_care, R.color.three)
        list.add(cate)
        cate = CategoryModel("Laundry", R.drawable.laundry, R.color.four)
        list.add(cate)
        cate = CategoryModel("Breakfast and Dairy", R.drawable.milk, R.color.five)
        list.add(cate)
        cate = CategoryModel("Spices,Salt & Sugar", R.drawable.masala, R.color.six)
        list.add(cate)
        cate = CategoryModel("Noodles & Pasta", R.drawable.noodles, R.color.seven)
        list.add(cate)
        cate = CategoryModel("Tea and coffee", R.drawable.tea, R.color.eight)
        list.add(cate)
        cate = CategoryModel("Beverages", R.drawable.cold, R.color.nine)
        list.add(cate)
        cate = CategoryModel("Fruits and Vegetables", R.drawable.fruit_veg, R.color.nine)
        list.add(cate)
    }

    private fun loadPopularProducts() {
        val productList = ArrayList<PopularItemModel>()
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(shopUid!!).child("Products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //productList.clear()
                for (ds in snapshot.children) {
                    val subCat = "" + ds.child("productCategory").value
                    if (subCat == "Bread & Bakery") {
                        val productsModel = ds.getValue(PopularItemModel::class.java)
                        productList.add(productsModel!!)
                    }
                }
                val itemAdapter = PopularItemAdapter(this@MainShopActivity, productList)
                binding.popularRV.adapter = itemAdapter

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
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
                productAdapter = UserProductAdapter(this@MainShopActivity, productList)
                binding.productRv.adapter = productAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}