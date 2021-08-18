@file:Suppress("DEPRECATION")

package com.ashish.grocery.user
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ashish.grocery.MainShopActivity
import com.ashish.grocery.R
import com.ashish.grocery.ui.LoginActivity
import com.ashish.grocery.ui.SettingsActivity
import com.ashish.grocery.user.adapters.OrderUserAdapter
import com.ashish.grocery.user.adapters.ShopAdapter
import com.ashish.grocery.user.models.OrderUserModel
import com.ashish.grocery.user.models.ShopModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainUserActivity : AppCompatActivity() {
    private lateinit var nameTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var shopsTv: TextView
    private lateinit var ordersTv: TextView
    private lateinit var profileImg: ImageView
    private lateinit var logoutBtn: ImageButton
    private lateinit var settingsBtn: ImageButton
    private lateinit var editBtn: ImageButton
    private lateinit var shopRv: RecyclerView
    private lateinit var shopRl: RelativeLayout
    private lateinit var orderRv: RecyclerView
    private lateinit var orderRl: RelativeLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var shopAdapter: ShopAdapter
    private lateinit var orderAdapter: OrderUserAdapter
    private var listData: ArrayList<ShopModel> = ArrayList()
    private var shopList: ArrayList<ShopModel> = ArrayList()
    private var orderList: ArrayList<OrderUserModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_user)
        init()
        val intent =Intent(this,MainShopActivity::class.java)
        startActivity(intent)
        finish()
        firebaseAuth= FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        checkUser()
        showShop()
        logoutBtn.setOnClickListener {
            makeMeOffline()
        }

        editBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileUserActivity::class.java))
        }

        settingsBtn.setOnClickListener {
            val intent=Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        shopsTv.setOnClickListener {
            showShop()
        }
        ordersTv.setOnClickListener {
            showOrders()
        }
    }

    private fun showOrders() {
        shopRl.visibility = View.GONE
        orderRl.visibility = View.VISIBLE
        ordersTv.setTextColor(resources.getColor(R.color.black))
        shopsTv.setTextColor(resources.getColor(R.color.white))
        ordersTv.setBackgroundResource(R.drawable.shape_rect03)
        shopsTv.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun showShop() {
        shopRl.visibility = View.VISIBLE
        orderRl.visibility = View.GONE
        shopsTv.setTextColor(resources.getColor(R.color.black))
        ordersTv.setTextColor(resources.getColor(R.color.white))
        shopsTv.setBackgroundResource(R.drawable.shape_rect03)
        ordersTv.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun makeMeOffline() {
        progressDialog.setMessage("Logging Out...")
        val hashMap = hashMapOf<String,String>()
        hashMap["online"] = "false"
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(firebaseAuth.uid!!).updateChildren(hashMap as Map<String, Any>)
            .addOnSuccessListener {
                firebaseAuth.signOut()
                checkUser()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this,it.message.toString(),Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val user:FirebaseUser?=firebaseAuth.currentUser
        if (user==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else{
            loadMyInfo()
        }
    }

    private fun loadMyInfo() {
        val db:DatabaseReference= FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild("uid").equalTo(firebaseAuth.uid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    val name=""+ds.child("name").value
                    val email=""+ds.child("email").value
                    val phone=""+ds.child("phone").value
                    val profileImage=""+ds.child("profileImage").value
                    val city=""+ds.child("city").value
                    nameTv.text=name
                    emailTv.text=email
                    phoneTv.text=phone
                    profileImg.load(profileImage){
                        placeholder(R.drawable.ic_person_gray)
                    }
                    loadShop(city)
                    loadOrder()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadOrder() {
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (ds in snapshot.children){
                    val uid=""+ds.ref.key
                    val dbRef=FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders")
                    dbRef.orderByChild("orderBy").equalTo(firebaseAuth.uid).addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                for (dp in snapshot.children){
                                    val order=dp.getValue(OrderUserModel::class.java)
                                    orderList.add(order!!)
                                }
                                orderAdapter= OrderUserAdapter(this@MainUserActivity,orderList)
                                orderRv.adapter=orderAdapter
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

    private fun loadShop(myCity: String) {
        val dbRef=FirebaseDatabase.getInstance().getReference("Users")
        dbRef.orderByChild("accountType").equalTo("seller").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                shopList.clear()
                for (ds in snapshot.children){
                    val shops=ds.getValue(ShopModel::class.java)!!
                    val shopCity=""+ds.child("city").value
                    if (shopCity==myCity){
                        shopList.add(shops)
                    }
                    //For displaying all shops
                    //shopList.add(shops)
                }
                shopAdapter= ShopAdapter(this@MainUserActivity,shopList)
                shopRv.adapter=shopAdapter
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun init(){
        nameTv=findViewById(R.id.nameUser)
        logoutBtn=findViewById(R.id.logoutBtn)
        editBtn=findViewById(R.id.editBtn)
        emailTv=findViewById(R.id.emailTv)
        phoneTv=findViewById(R.id.phoneTv)
        shopsTv=findViewById(R.id.shops)
        ordersTv=findViewById(R.id.orders)
        profileImg=findViewById(R.id.profileImg)
        shopRv=findViewById(R.id.shopsRv)
        shopRl=findViewById(R.id.shopsRl)
        orderRv=findViewById(R.id.ordersRv)
        orderRl=findViewById(R.id.ordersRl)
        settingsBtn=findViewById(R.id.settingsBtn)

    }
}