package com.ashish.grocery.seller

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.seller.models.ProductsModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

@Suppress("DEPRECATION")
class EditProductActivity : AppCompatActivity() {
    private lateinit var productIds: String
    private lateinit var backBtn: ImageButton
    private lateinit var productImg: ImageView
    private lateinit var productNameEt: EditText
    private lateinit var productDescriptionEt: EditText
    private lateinit var productCategoryEt: EditText
    private lateinit var productQuantityEt: EditText
    private lateinit var productPriceEt: EditText
    private lateinit var discountAmountEt: EditText
    private lateinit var discountPercentEt: EditText
    private lateinit var discountSwitch: SwitchCompat
    private lateinit var updateProductBtn: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var productList: ArrayList<ProductsModel> = ArrayList()
    private lateinit var downloadUrl: String

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val REQ = 1
    private var imageUri: Uri? = null
    private val productCategories = arrayOf(
        "Beverages",
        "Biscuit and snacks",
        "Vegetables",
        "Cooking essentials",
        "Personal care",
        "Beauty & groom",
        "Laundry",
        "Bread & Bakery",
        "Spices & masala",
        "Noodles",
        "Tea and coffee"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)
        init()
        discountAmountEt.visibility = View.GONE
        discountPercentEt.visibility = View.GONE
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        productIds = intent.getStringExtra("productId").toString()
        Log.d("ProductId", productIds)
        loadProductDetail()
        discountSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discountAmountEt.visibility = View.VISIBLE
                discountPercentEt.visibility = View.VISIBLE
            } else {
                discountAmountEt.visibility = View.GONE
                discountPercentEt.visibility = View.GONE
            }
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }
        productImg.setOnClickListener {
            openGallery()
        }
        productCategoryEt.setOnClickListener {
            categoryDialog()
        }
        updateProductBtn.setOnClickListener {
            inputData()
        }

    }

    private fun loadProductDetail() {
        val auth = FirebaseAuth.getInstance()
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(firebaseAuth.uid!!).child("Products").child(productIds)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(ds: DataSnapshot) {

                        val productIds = "" + ds.child("productId").value
                        val productName = "" + ds.child("productName").value
                        val productCategory = "" + ds.child("productCategory").value
                        val productQuantity = "" + ds.child("productQuantity").value
                        val productDescription = "" + ds.child("productDescription").value
                        val originalPrice = "" + ds.child("originalPrice").value
                        val discountPrice = "" + ds.child("discountPrice").value
                        val discountNote = "" + ds.child("discountNote").value
                        val discountAvailable = "" + ds.child("discountAvailable").value
                        val timestamp = "" + ds.child("timestamp").value
                        val productImages = "" + ds.child("productImage").value

                        if (discountAvailable == "true") {
                            discountSwitch.isChecked = true
                            discountAmountEt.visibility = View.VISIBLE
                            discountPercentEt.visibility = View.VISIBLE
                        } else {
                            discountAmountEt.visibility = View.GONE
                            discountPercentEt.visibility = View.GONE
                        }

                        productNameEt.text = productName.toEditable()
                        productDescriptionEt.text = productDescription.toEditable()
                        productCategoryEt.text = productCategory.toEditable()
                        productQuantityEt.text = productQuantity.toEditable()
                        productPriceEt.text = originalPrice.toEditable()
                        discountPercentEt.text = discountNote.toEditable()
                        discountAmountEt.text = discountPrice.toEditable()
                        productImg.load(productImages) {
                            placeholder(R.drawable.ic_cart_purple)
                        }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private lateinit var productName: String
    private lateinit var productDescription: String
    private lateinit var productCategory: String
    private lateinit var productQuantity: String
    private lateinit var originalPrice: String
    private lateinit var discountPrice: String
    private lateinit var discountNote: String
    private var discountAvailable: Boolean = false

    private fun inputData() {
        productName = productNameEt.text.toString()
        productDescription = productDescriptionEt.text.toString()
        productCategory = productCategoryEt.text.toString()
        productQuantity = productQuantityEt.text.toString()
        originalPrice = productPriceEt.text.toString()
        discountAvailable = discountSwitch.isChecked
        discountPrice = discountAmountEt.text.toString()
        discountNote = discountPercentEt.text.toString()

        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, "Enter Product Name..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(productDescription)) {
            Toast.makeText(this, "Enter Product description", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(productQuantity)) {
            Toast.makeText(this, "Enter Product quantity", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Enter Product original price", Toast.LENGTH_SHORT).show()
            return
        }
        if (discountAvailable) {
            discountPrice = "0.0"
            discountNote = ""
            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount price is necessary", Toast.LENGTH_SHORT).show()
                return
            } else {
                discountPrice = discountAmountEt.text.toString()
                discountNote = discountPercentEt.text.toString()
            }
        }
        updateProduct()
    }

    private fun updateProduct() {
        progressDialog.setMessage("Updating product...")
        progressDialog.show()
        val timeStamp: String = "" + System.currentTimeMillis()
        if (imageUri == null) {
            val hashMap = java.util.HashMap<String, String>()

            hashMap["productName"] = "" + productName
            hashMap["productDescription"] = "" + productDescription
            hashMap["productCategory"] = "" + productCategory
            hashMap["productQuantity"] = "" + productQuantity
            hashMap["productId"] = "" + productIds
            hashMap["originalPrice"] = "" + originalPrice
            hashMap["discountAvailable"] = "" + discountAvailable
            hashMap["discountPrice"] = "" + discountPrice
            hashMap["discountNote"] = "" + discountNote
            hashMap["timestamp"] = "" + timeStamp

            //save to db
            val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
            dbRef.child(firebaseAuth.uid!!).child("Products").child(productIds)
                .updateChildren(hashMap as Map<String, Any>)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Product Updated..", Toast.LENGTH_SHORT).show()
                    clearData()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
        } else {
            uploadImage(imageUri!!)
        }
    }

    private fun uploadImage(it: Uri) {
        val timeStamp: String = "" + System.currentTimeMillis()
        val ref = storage.reference.child("product_img/$productName")
        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressDialog.dismiss()
                downloadUrl = task.result.toString()
                Log.i("URL", "DownloadUrl $downloadUrl")

                val hashMap = java.util.HashMap<String, String>()
                hashMap["productName"] = "" + productName
                hashMap["productDescription"] = "" + productDescription
                hashMap["productCategory"] = "" + productCategory
                hashMap["productQuantity"] = "" + productQuantity
                hashMap["originalPrice"] = "" + originalPrice
                hashMap["discountAvailable"] = "" + discountAvailable
                hashMap["discountPrice"] = "" + discountPrice
                hashMap["discountNote"] = "" + discountNote
                hashMap["timestamp"] = "" + timeStamp
                hashMap["productImage"] = "" + downloadUrl
                //save to db
                val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(auth.uid!!).child("Products").child(productIds)
                    .updateChildren(hashMap as Map<String, Any>)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Product Added..", Toast.LENGTH_SHORT).show()
                        clearData()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearData() {
        productNameEt.text = "".toEditable()
        productDescriptionEt.text = "".toEditable()
        productCategoryEt.text = "".toEditable()
        productQuantityEt.text = "".toEditable()
        productPriceEt.text = "".toEditable()
        productPriceEt.text = "".toEditable()
        productPriceEt.text = "".toEditable()
        productImg.setImageResource(R.drawable.ic_cart_purple)
        discountPercentEt.text = "".toEditable()
        discountAmountEt.text = "".toEditable()
    }

    private fun categoryDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Product Category")
            .setItems(productCategories) { _, i ->
                val category = productCategories[i]
                productCategoryEt.text = category.toEditable()
            }.show()
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun openGallery() {
        val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImage, REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ && resultCode == RESULT_OK && data != null && data.data !== null) {
            imageUri = data.data
            productImg.setImageURI(imageUri)
        }
    }

    private fun init() {
        backBtn = findViewById(R.id.backBtn)
        productImg = findViewById(R.id.productImg)
        productNameEt = findViewById(R.id.productName)
        productDescriptionEt = findViewById(R.id.productDescription)
        productCategoryEt = findViewById(R.id.category)
        productQuantityEt = findViewById(R.id.quantity)
        productPriceEt = findViewById(R.id.price)
        discountAmountEt = findViewById(R.id.discountAmount)
        discountPercentEt = findViewById(R.id.discountPercent)
        discountSwitch = findViewById(R.id.dicountSwitch)
        updateProductBtn = findViewById(R.id.updateProduct)
    }
}