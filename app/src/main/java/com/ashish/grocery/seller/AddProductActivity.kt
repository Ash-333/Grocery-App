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
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.ashish.grocery.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

@Suppress("DEPRECATION")
class AddProductActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageButton
    private lateinit var productImage: ImageView
    private lateinit var productNameEt: EditText
    private lateinit var productDescriptionEt: EditText
    private lateinit var productCategoryEt: EditText
    private lateinit var productSubCategoryEt: EditText
    private lateinit var productQuantityEt: EditText
    private lateinit var productPriceEt: EditText
    private lateinit var discountAmountEt: EditText
    private lateinit var discountPercentEt: EditText
    private lateinit var discountSwitch: SwitchCompat
    private lateinit var addProductBtn: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var downloadUrl: String

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val REQ = 1
    private var imageUri: Uri? = null
    private val productCategories = arrayOf(
        "Beverages",
        "Biscuit and snacks",
        "Atta,oil & grains",
        "Personal care",
        "Laundry",
        "Breakfast and Dairy",
        "Spices,Salt & Sugar",
        "Noodles & Pasta",
        "Tea and coffee",
        "Fruits and Vegetables"
    )
    private val productSubCategories= arrayOf("Popular","Daily need","none")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        init()
        discountAmountEt.visibility = View.GONE
        discountPercentEt.visibility = View.GONE
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        discountSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discountAmountEt.visibility = View.VISIBLE
                discountPercentEt.visibility = View.VISIBLE
            } else {
                discountAmountEt.visibility = View.GONE
                discountPercentEt.visibility = View.GONE
            }
        }
        productImage.setOnClickListener {
            openGallery()
        }
        productCategoryEt.setOnClickListener {
            categoryDialog()
        }
        productSubCategoryEt.setOnClickListener {
            subCategoryDialog()
        }
        addProductBtn.setOnClickListener {
            inputData()
        }
        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private lateinit var productName: String
    private lateinit var productDescription: String
    private lateinit var productCategory: String
    private lateinit var productSubCategory: String
    private lateinit var productQuantity: String
    private lateinit var originalPrice: String
    private lateinit var discountPrice: String
    private lateinit var discountNote: String
    private var discountAvailable: Boolean = false

    private fun inputData() {
        productName = productNameEt.text.toString()
        productDescription = productDescriptionEt.text.toString()
        productCategory = productCategoryEt.text.toString()
        productSubCategory = productSubCategoryEt.text.toString()
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
        addProduct()
    }

    private fun addProduct() {
        progressDialog.setMessage("Adding product...")
        progressDialog.show()
        val timeStamp: String = "" + System.currentTimeMillis()
        if (imageUri == null) {
            val hashMap = java.util.HashMap<String, String>()

            hashMap["productName"] = "" + productName
            hashMap["productDescription"] = "" + productDescription
            hashMap["productCategory"] = "" + productCategory
            hashMap["productSubCategory"] = "" + productSubCategory
            hashMap["productQuantity"] = "" + productQuantity
            hashMap["originalPrice"] = "" + originalPrice
            hashMap["productId"] = "" + firebaseAuth.uid
            hashMap["discountAvailable"] = "" + discountAvailable
            hashMap["discountPrice"] = "" + discountPrice
            hashMap["discountNote"] = "" + discountNote
            hashMap["timestamp"] = "" + timeStamp
            hashMap["productImage"] = ""

            //save to db
            val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
            dbRef.child(firebaseAuth.uid!!).child("Products").child(timeStamp).setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    showSuccessToast("Product Added..")
                    clearData()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    showErrorToast(it.message.toString())
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
                hashMap["productSubCategory"] = "" + productSubCategory
                hashMap["productQuantity"] = "" + productQuantity
                hashMap["originalPrice"] = "" + originalPrice
                hashMap["productId"] = "" + firebaseAuth.uid
                hashMap["discountAvailable"] = "" + discountAvailable
                hashMap["discountPrice"] = "" + discountPrice
                hashMap["discountNote"] = "" + discountNote
                hashMap["timestamp"] = "" + timeStamp
                hashMap["productImage"] = "" + downloadUrl
                //save to db
                val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(firebaseAuth.uid!!).child("Products").child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        showSuccessToast("Product Added..")
                        clearData()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        showErrorToast(it.message.toString())
                    }
            }
        }.addOnFailureListener {
            showErrorToast(it.message.toString())
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
        productImage.setImageResource(R.drawable.ic_cart_purple)
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

    private fun subCategoryDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Product Sub Category")
            .setItems(productSubCategories) { _, i ->
                val category = productSubCategories[i]
                productSubCategoryEt.text = category.toEditable()
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
            productImage.setImageURI(imageUri)
        }
    }

    private fun showSuccessToast(message: String?) {
        val toast = Toast(this@AddProductActivity)
        val view: View = LayoutInflater.from(this@AddProductActivity)
            .inflate(R.layout.success_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }

    private fun showErrorToast(message: String?) {
        val toast = Toast(this@AddProductActivity)
        val view: View = LayoutInflater.from(this@AddProductActivity)
            .inflate(R.layout.error_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }

    private fun init() {
        backBtn = findViewById(R.id.backBtn)
        productImage = findViewById(R.id.productImg)
        productNameEt = findViewById(R.id.productName)
        productDescriptionEt = findViewById(R.id.productDescription)
        productCategoryEt = findViewById(R.id.category)
        productSubCategoryEt = findViewById(R.id.subCategory)
        productQuantityEt = findViewById(R.id.quantity)
        productPriceEt = findViewById(R.id.price)
        discountAmountEt = findViewById(R.id.discountAmount)
        discountPercentEt = findViewById(R.id.discountPercent)
        discountSwitch = findViewById(R.id.dicountSwitch)
        addProductBtn = findViewById(R.id.addProduct)
    }

}