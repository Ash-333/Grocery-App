package com.ashish.grocery.user

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import com.ashish.grocery.R
import com.ashish.grocery.ui.LoginActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

class EditProfileUserActivity : AppCompatActivity(), LocationListener{
    private lateinit var backBtn:ImageButton
    private lateinit var gpsBtn:ImageButton
    private lateinit var profileImg:ImageView
    private lateinit var fullNameEt:EditText
    private lateinit var phoneEt:EditText
    private lateinit var countryEt:EditText
    private lateinit var stateEt:EditText
    private lateinit var cityEt:EditText
    private lateinit var addressEt:EditText
    private lateinit var updateBtn:Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var locationManager: LocationManager
    private val storage by lazy{
        FirebaseStorage.getInstance()
    }
    private val auth by lazy{
        FirebaseAuth.getInstance()
    }
    private lateinit var downloadUrl:String
    private var imageUri: Uri? = null
    private var p0: Long = 0
    private var p1: Float = 0f
    //Longitude and latitude
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    //permission const
    private var LOCATION_REQUEST_CODE = 100
    private val REQ = 1
    //permission arrays
    private lateinit var locationPermission: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_user)
        firebaseAuth= FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCancelable(false)
        locationPermission=arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        init()
        checkUser()
        profileImg.setOnClickListener {
            //pick image
            openGallery()
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }
        gpsBtn.setOnClickListener {
            if (checkLocationPermission()) {
                //already allowed
                detectLocation()
            } else {
                //not allowed, request
                requestLocationPermission()
            }
        }
        updateBtn.setOnClickListener {
            inputData()
        }
    }

    private lateinit var fullName:String
    private lateinit var phoneNumber:String
    private lateinit var shopName:String
    private lateinit var country:String
    private lateinit var state:String
    private lateinit var city:String
    private lateinit var address:String
    private fun inputData() {
        fullName=fullNameEt.text.toString().trim()
        phoneNumber=phoneEt.text.toString().trim()
        country=countryEt.text.toString().trim()
        state=stateEt.text.toString().trim()
        city=cityEt.text.toString().trim()
        address=addressEt.text.toString().trim()
        updateProfile()
    }

    private fun updateProfile() {
        progressDialog.setMessage("Updating...")
        progressDialog.show()
        if(imageUri==null){
            //saving data without img
            val hashMap = java.util.HashMap<String, String>()
            hashMap["name"] = ""+fullName
            hashMap["phone"] = ""+phoneNumber
            hashMap["country"] = ""+country
            hashMap["state"] = ""+state
            hashMap["city"] = ""+city
            hashMap["city"] = ""+city
            hashMap["address"] = ""+address
            hashMap["longitude"] = ""+longitude
            hashMap["latitude"] = ""+latitude
            hashMap["online"] = "true"

            //save to db
            val dbRef:DatabaseReference=FirebaseDatabase.getInstance().getReference("Users")
            dbRef.child(firebaseAuth.uid!!).updateChildren(hashMap as Map<String, Any>)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this,it.message.toString(),Toast.LENGTH_SHORT).show()
                }

        }
        else{
            //save info with image

            uploadImage(imageUri!!)

        }
    }

    private fun uploadImage(it: Uri) {
        val ref=storage.reference.child("profile_img/"+auth.uid.toString())
        val uploadTask=ref.putFile(it)
        uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{
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
                hashMap["name"] = ""+fullName
                hashMap["phone"] = ""+phoneNumber
                hashMap["country"] = ""+country
                hashMap["state"] = ""+state
                hashMap["city"] = ""+city
                hashMap["city"] = ""+city
                hashMap["address"] = ""+address
                hashMap["longitude"] = ""+longitude
                hashMap["latitude"] = ""+latitude
                hashMap["profileImage"] = ""+downloadUrl

                //save to db
                val dbRef:DatabaseReference=FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(firebaseAuth.uid!!).updateChildren(hashMap as Map<String, Any>)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this,it.message.toString(),Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this,it.message.toString(),Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkUser() {
        val user:FirebaseUser?=firebaseAuth.currentUser
        if (user==null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
        else{
            loadMyInfo()
        }
    }

    private fun loadMyInfo() {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        db.orderByChild("uid").equalTo(firebaseAuth.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    val name=""+ds.child("name").value
                    val address=""+ds.child("address").value
                    val city=""+ds.child("city").value
                    val state=""+ds.child("state").value
                    val country=""+ds.child("country").value
                    val profileImage=""+ds.child("profileImage").value
                    val phone=""+ds.child("phone").value

                    fullNameEt.text=name.toEditable()
                    cityEt.text=city.toEditable()
                    countryEt.text=country.toEditable()
                    stateEt.text=state.toEditable()
                    phoneEt.text=phone.toEditable()
                    addressEt.text=address.toEditable()
                    profileImg.load(profileImage){
                        placeholder(R.drawable.ic_person_gray)
                    }

//                    try {
//                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_shop).into(profileImg)
//                    }catch (e:Exception){
//                        profileImg.setImageResource(R.drawable.ic_person_gray)
//                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

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
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
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
    private fun init (){
        backBtn=findViewById(R.id.backBtn)
        gpsBtn=findViewById(R.id.gpsBtn)
        profileImg=findViewById(R.id.profileImg)
        fullNameEt=findViewById(R.id.fullName)
        phoneEt=findViewById(R.id.phoneNumber)
        countryEt=findViewById(R.id.countryEt)
        cityEt=findViewById(R.id.cityEt)
        stateEt=findViewById(R.id.stateEt)
        cityEt=findViewById(R.id.cityEt)
        addressEt=findViewById(R.id.completeAddressEt)
        updateBtn=findViewById(R.id.updateBtn)

    }
}