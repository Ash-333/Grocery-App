package com.ashish.grocery.user

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ashish.grocery.databinding.ActivityCategoryBinding
import com.ashish.grocery.seller.models.ProductsModel
import com.ashish.grocery.user.adapters.UserProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCategoryBinding
    private lateinit var category: String
    private lateinit var shopUid:String
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var progressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        category= intent.getStringExtra("category").toString()
        Log.d("Category",category)
        binding.cat.text=category
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCancelable(false)
        firebaseAuth = FirebaseAuth.getInstance()
        shopUid = "mRlqasSfeIRBmCCeXYumuGYC9vE2"
        loadProduct()
    }
   private fun loadProduct(){
       val productList = ArrayList<ProductsModel>()
       val dbRef = FirebaseDatabase.getInstance().getReference("Users")
       dbRef.child(shopUid!!).child("Products").addValueEventListener(object : ValueEventListener {
           override fun onDataChange(snapshot: DataSnapshot) {
               productList.clear()
               for (ds in snapshot.children) {
                   val subCat=""+ds.child("productCategory").value
                   if (subCat==category){
                       val productsModel = ds.getValue(ProductsModel::class.java)
                       productList.add(productsModel!!)
                   }
               }
               val itemAdapter= UserProductAdapter(this@CategoryActivity,productList)
               binding.productsRv.adapter=itemAdapter

           }

           override fun onCancelled(error: DatabaseError) {
           }
       })
   }
}