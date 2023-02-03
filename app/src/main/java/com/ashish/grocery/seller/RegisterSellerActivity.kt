package com.ashish.grocery.seller

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ashish.grocery.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

class RegisterSellerActivity : AppCompatActivity(), LocationListener {

    private lateinit var backBtn: ImageButton
    private lateinit var gpsBtn: ImageButton
    private lateinit var profileImg: ImageView
    private lateinit var fullNameEt: EditText
    private lateinit var shopNameEt: EditText
    private lateinit var phoneNumberEt: EditText
    private lateinit var deliveryFeesEt: EditText
    private lateinit var countryEt: EditText
    private lateinit var stateEt: EditText
    private lateinit var cityEt: EditText
    private lateinit var addressEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var cPassword: EditText
    private lateinit var registerBtn: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var downloadUrl: String
    private lateinit var locationManager: LocationManager
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var p0: Long = 0
    private var p1: Float = 0f
    private var imageUri: Uri? = null

    //permission const
    private var LOCATION_REQUEST_CODE = 100
    private val REQ = 1
    private lateinit var locationPermission: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_seller)
        init()
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        locationPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        profileImg.setOnClickListener {
            //pick image
            openGallery()
        }

        gpsBtn.setOnClickListener {
            //detect user location
            if (checkLocationPermission()) {
                detectLocation()
            } else {
                requestLocationPermission()
            }
        }

        registerBtn.setOnClickListener {
            inputData()

        }
    }

    private lateinit var fullName: String
    private lateinit var phoneNumber: String
    private lateinit var shopName: String
    private lateinit var deliveryFees: String
    private lateinit var country: String
    private lateinit var state: String
    private lateinit var city: String
    private lateinit var address: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confirmPassword: String

    private fun inputData() {
        fullName = fullNameEt.text.toString().trim()
        shopName = shopNameEt.text.toString().trim()
        phoneNumber = phoneNumberEt.text.toString().trim()
        deliveryFees = deliveryFeesEt.text.toString().trim()
        country = countryEt.text.toString().trim()
        state = stateEt.text.toString().trim()
        city = cityEt.text.toString().trim()
        address = addressEt.text.toString().trim()
        email = emailEt.text.toString()
        password = passwordEt.text.toString()
        confirmPassword = cPassword.toString()

        //Validate input data
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Full Name..", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(shopName)) {
            Toast.makeText(this, "Enter Shop Name..", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(deliveryFees)) {
            Toast.makeText(this, "Enter DeliverFees..", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter PhoneNumber..", Toast.LENGTH_SHORT).show()
            return
        }

//        if (longitude == 0.0 || latitude == 0.0) {
//            Toast.makeText(this, "Please click on GPS for auto detect", Toast.LENGTH_SHORT).show()
//            return
//        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email pattern", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password length mus be 6 characters long", Toast.LENGTH_SHORT)
                .show()
            return
        }
//        if (password != confirmPassword){
//            Toast.makeText(this,"Password didn't matched",Toast.LENGTH_SHORT).show()
//            return
//        }

        createAccount()
    }

    private fun createAccount() {
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            saveFirebaseData()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFirebaseData() {
        progressDialog.setMessage("Saving Account info")
        val timeStamp: String = "" + System.currentTimeMillis()

        if (imageUri == null) {
            //saving data without img
            val hashMap = HashMap<String, String>()
            hashMap["uid"] = "" + firebaseAuth.uid
            hashMap["email"] = "" + email
            hashMap["password"] = "" + password
            hashMap["name"] = "" + fullName
            hashMap["shopName"] = "" + shopName
            hashMap["phone"] = "" + phoneNumber
            hashMap["deliveryFee"] = "" + deliveryFees
            hashMap["country"] = "" + country
            hashMap["state"] = "" + state
            hashMap["city"] = "" + city
            hashMap["address"] = "" + address
            hashMap["longitude"] = "" + longitude
            hashMap["latitude"] = "" + latitude
            hashMap["timestamp"] = "" + timeStamp
            hashMap["accountType"] = "seller"
            hashMap["online"] = "true"
            hashMap["shopOpen"] = "true"
            hashMap["profileImage"] = ""

            //save to db
            val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
            dbRef.child(firebaseAuth.uid!!).setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    startActivity(Intent(this, MainSellerActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    startActivity(Intent(this, MainSellerActivity::class.java))
                    finish()
                }
        }
        else {
            //save info with image

            uploadImage(imageUri!!)
        }
    }

    private fun uploadImage(it: Uri) {
        val timeStamp: String = "" + System.currentTimeMillis()
        val ref = storage.reference.child("profile_img/" + auth.uid.toString())
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

                val hashMap = HashMap<String, String>()
                hashMap["uid"] = "" + firebaseAuth.uid
                hashMap["email"] = "" + email
                hashMap["password"] = "" + password
                hashMap["name"] = "" + fullName
                hashMap["shopName"] = "" + shopName
                hashMap["phone"] = "" + phoneNumber
                hashMap["deliveryFee"] = "" + deliveryFees
                hashMap["country"] = "" + country
                hashMap["state"] = "" + state
                hashMap["city"] = "" + city
                hashMap["city"] = "" + city
                hashMap["address"] = "" + address
                hashMap["longitude"] = "" + longitude
                hashMap["latitude"] = "" + latitude
                hashMap["timestamp"] = "" + timeStamp
                hashMap["accountType"] = "seller"
                hashMap["online"] = "true"
                hashMap["shopOpen"] = "true"
                hashMap["profileImage"] = "" + downloadUrl

                //save to db
                val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(firebaseAuth.uid!!).setValue(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        startActivity(Intent(this, MainSellerActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        startActivity(Intent(this, MainSellerActivity::class.java))
                        finish()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        }

    }

    private fun detectLocation() {
        Toast.makeText(this, "Please wait..", Toast.LENGTH_LONG).show()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, p0, p1, this)

    }

    private fun findAddress() {
        val addresses: List<Address>
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
            val address: String = addresses[0].getAddressLine(0)
            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val country: String = addresses[0].countryName

            countryEt.setText(country)
            cityEt.setText(city)
            addressEt.setText(address)
            stateEt.setText(state)

        } catch (e: Exception) {
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == (PackageManager
            .PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    //permission granted
                    val locationAccepted: Boolean =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (locationAccepted) {
                        detectLocation()
                    } else {
                        //permission denied
                        Toast.makeText(
                            this,
                            "Location permission is necessary..",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_LONG).show()
    }

    override fun onLocationChanged(loaction: Location) {
        latitude = loaction.latitude
        longitude = loaction.longitude
        findAddress()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//        super.onStatusChanged(provider, status, extras)
    }

    private fun openGallery() {
        val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImage, REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ && resultCode == RESULT_OK && data != null && data.data !== null) {
            imageUri = data.data
            profileImg.setImageURI(imageUri)
        }
    }

    private fun init() {
        backBtn = findViewById(R.id.backBtn)
        gpsBtn = findViewById(R.id.gpsBtn)
        profileImg = findViewById(R.id.profileImg)
        fullNameEt = findViewById(R.id.fullName)
        shopNameEt = findViewById(R.id.shopName)
        phoneNumberEt = findViewById(R.id.phoneNumber)
        deliveryFeesEt = findViewById(R.id.deliveryFee)
        countryEt = findViewById(R.id.countryEt)
        stateEt = findViewById(R.id.stateEt)
        cityEt = findViewById(R.id.cityEt)
        addressEt = findViewById(R.id.completeAddressEt)
        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        cPassword = findViewById(R.id.confPasswordEt)
        registerBtn = findViewById(R.id.registerBtn)
    }


}