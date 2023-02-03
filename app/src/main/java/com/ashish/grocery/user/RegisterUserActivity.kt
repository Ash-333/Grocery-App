package com.ashish.grocery.user

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
import com.ashish.grocery.MainShopActivity
import com.ashish.grocery.R
import com.ashish.grocery.seller.MainSellerActivity
import com.ashish.grocery.seller.RegisterSellerActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

class RegisterUserActivity : AppCompatActivity(), LocationListener {
    private lateinit var profileImg: ImageView
    private lateinit var fullNameEt: EditText
    private lateinit var phoneEt: EditText
    private lateinit var countryEt: EditText
    private lateinit var stateEt: EditText
    private lateinit var cityEt: EditText
    private lateinit var addressEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var confirmPasswordEt: EditText
    private lateinit var registerBtn: Button
    private lateinit var backBtn: ImageButton
    private lateinit var gpsBtn: ImageButton
    private lateinit var registerAsSeller: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var token:String
    private val storage by lazy{
        FirebaseStorage.getInstance()
    }
    private val auth by lazy{
        FirebaseAuth.getInstance()
    }
    private lateinit var downloadUrl:String

    private var imageUri: Uri? = null

    private val REQ = 1
    private var p0: Long = 0
    private var p1: Float = 0f

    //Longitude and latitude
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    //permission const
    private var LOCATION_REQUEST_CODE = 100

    //permission arrays
    private lateinit var locationPermission: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        init()
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        locationPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        token= task.result!!
                    }
                }
            }

        backBtn.setOnClickListener {
            onBackPressed()
        }

        profileImg.setOnClickListener {
            //pick image
            openGallery()
        }

        gpsBtn.setOnClickListener {
            //detect user Location
            if (checkLocationPermission()) {
                //already allowed
                detectLocation()
            } else {
                //not allowed, request
                requestLocationPermission()
            }

        }

        registerBtn.setOnClickListener {
            inputData()
        }

        registerAsSeller.setOnClickListener {
            startActivity(Intent(this@RegisterUserActivity, RegisterSellerActivity::class.java))
        }
    }

    private lateinit var fullName: String
    private lateinit var phoneNumber: String
    private lateinit var country: String
    private lateinit var state: String
    private lateinit var city: String
    private lateinit var address: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confirmPassword: String

    private fun inputData() {
        fullName = fullNameEt.text.toString().trim()
        phoneNumber = phoneEt.text.toString().trim()
        country = countryEt.text.toString().trim()
        state = stateEt.text.toString().trim()
        city = cityEt.text.toString().trim()
        address = addressEt.text.toString().trim()
        email = emailEt.text.toString()
        password = passwordEt.text.toString()
        confirmPassword = confirmPasswordEt.toString()

        //Validate input data
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Full Name..", Toast.LENGTH_SHORT).show()
            return
        }


        if (TextUtils.isEmpty(phoneNumber) && phoneNumber.length<10) {
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
//        if (password != confirmPassword) {
//            Toast.makeText(this, "Password didn't matched", Toast.LENGTH_SHORT).show()
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
        }
    }

    private fun saveFirebaseData() {
        progressDialog.setMessage("Saving Account info")
        val timeStamp: String = "" + System.currentTimeMillis()

        if (imageUri == null) {
            //saving data without img
            val hashMap = HashMap<String, String>()
            hashMap["messageToken"]=""+token
            hashMap["uid"] = "" + firebaseAuth.uid
            hashMap["email"] = "" + email
            hashMap["password"] = ""+password
            hashMap["name"] = "" + fullName
            hashMap["phone"] = "" + phoneNumber
            hashMap["country"] = "" + country
            hashMap["state"] = "" + state
            hashMap["city"] = "" + city
            hashMap["city"] = "" + city
            hashMap["address"] = "" + address
            hashMap["longitude"] = "" + longitude
            hashMap["latitude"] = "" + latitude
            hashMap["timestamp"] = "" + timeStamp
            hashMap["accountType"] = "user"
            hashMap["online"] = "true"
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
        } else {
            //save info with image
            //name and path of image
            uploadImage(imageUri!!)
        }
    }

    private fun uploadImage(it: Uri) {
        val timeStamp:String=""+System.currentTimeMillis()
        val ref=storage.reference.child("profile_img/"+auth.uid.toString())
        val uploadTask=ref.putFile(it)
        uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{
                task ->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener{ task->
            if (task.isSuccessful){
                progressDialog.dismiss()
                downloadUrl=task.result.toString()
                Log.i("URL","DownloadUrl $downloadUrl")

                val hashMap = HashMap<String, String>()
                hashMap["messageToken"]=""+token
                hashMap["uid"] = ""+firebaseAuth.uid
                hashMap["email"] = ""+email
                hashMap["password"] = ""+password
                hashMap["name"] = ""+fullName
                hashMap["phone"] = ""+phoneNumber
                hashMap["country"] = ""+country
                hashMap["state"] = ""+state
                hashMap["city"] = ""+city
                hashMap["city"] = ""+city
                hashMap["address"] = ""+address
                hashMap["longitude"] = ""+longitude
                hashMap["latitude"] = ""+latitude
                hashMap["timestamp"] = ""+timeStamp
                hashMap["accountType"] = "user"
                hashMap["online"] = "true"
                hashMap["profileImage"] = ""+downloadUrl

                //save to db
                val dbRef:DatabaseReference=FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(firebaseAuth.uid!!).setValue(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        startActivity(Intent(this, MainShopActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        startActivity(Intent(this, MainShopActivity::class.java))
                        finish()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this,it.message.toString(),Toast.LENGTH_SHORT).show()
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
        val addresses: List<Address?>
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1) as List<Address?>
            val address: String = addresses[0]!!.getAddressLine(0)
            val city: String? = addresses[0]?.locality
            val state: String? = addresses[0]?.adminArea
            val country: String? = addresses[0]?.countryName

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

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

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
        profileImg = findViewById(R.id.profileImg)
        fullNameEt = findViewById(R.id.fullName)
        phoneEt = findViewById(R.id.phoneNumber)
        countryEt = findViewById(R.id.countryEt)
        stateEt = findViewById(R.id.stateEt)
        cityEt = findViewById(R.id.cityEt)
        addressEt = findViewById(R.id.completeAddressEt)
        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        confirmPasswordEt = findViewById(R.id.confPasswordEt)
        registerBtn = findViewById(R.id.registerBtn)
        backBtn = findViewById(R.id.backBtn)
        registerAsSeller = findViewById(R.id.registerSellerTv)
        gpsBtn = findViewById(R.id.gpsBtn)
    }
}