package com.ashish.grocery.user.cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish.grocery.R;
import com.ashish.grocery.databinding.ActivityCartBinding;
import com.ashish.grocery.user.adapters.CartItemAdapter;
import com.ashish.grocery.user.models.CartItemModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;



public class Carts extends AppCompatActivity {

     TextView allTotalPriceTv,deliveryFeeAmt,sTotalAmt;
    private RecyclerView cartRv;
    private ImageView backBtn;
    private Button confirmBtn;

    private  ProgressDialog progressDialog;
    private   FirebaseAuth firebaseAuth;
    private  ArrayList<CartItemModel> cartItem;
//    private  CartAdapter cartAdapter;
    Double allTotalPrice = 0.0;
    private   String name,myPhone,myLat,deliveryFee,shopUid,myLong;
    private  ArrayList<CartItemModel>  cart = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carts);
        init();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
        firebaseAuth = FirebaseAuth.getInstance();
        shopUid = "mRlqasSfeIRBmCCeXYumuGYC9vE2";
        deliveryFee = "100";
        loadMyInfo();
        //loadCart();
    }

//    private void loadCart() {
//        Paper.init(this);
//        cart = Paper.book().read("cart", new ArrayList());
//        for (int i=0;i<cart.size();i++){
//            String dFee = cart.get(i).getCost().replace("Rs", "");
//            allTotalPrice += Double.parseDouble(dFee);
//            Log.d("AllTotal", dFee);
//        }
//        cartAdapter=new CartAdapter(this,cart);
//        cartRv.setAdapter(cartAdapter);
//        deliveryFeeAmt.setText(deliveryFee);
//        sTotalAmt.setText("Rs "+allTotalPrice);
//        String a=String.valueOf(allTotalPrice+Double.parseDouble(deliveryFee));
//        allTotalPriceTv.setText(a);
//
//        confirmBtn.setOnClickListener(view -> {
//            if (myLat == "" || myLat.equals("null") || myLong == "" || myLong == "null") {
//                Toast.makeText(
//                        this,
//                        "Please enter address in your profile before placing order",
//                        Toast.LENGTH_SHORT
//                ).show();
//                return;
//            }
//            if (myPhone == "" || myPhone == "null") {
//                Toast.makeText(
//                        this,
//                        "Please enter phone in your profile before placing order",
//                        Toast.LENGTH_SHORT
//                ).show();
//                return;
//            }
//            if (cartItem.size() == 0) {
//                Toast.makeText(this, "No item in cart", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (allTotalPrice < 100) {
//                Toast.makeText(this, "Minimum order should be Rs 100", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            submitOrder();
//        });
//    }

    private void submitOrder() {
        progressDialog.setMessage("Placing order...");
        progressDialog.show();
        String timeStamp=""+System.currentTimeMillis();
        String costs = allTotalPriceTv.getText().toString().trim().replace("Rs", "");

        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("orderId",timeStamp);
        hashMap.put("orderTime",timeStamp);
        hashMap.put("orderStatus","In progress");
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("userName",""+name);
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("orderCost",""+costs);
        hashMap.put("latitude",""+myLat);
        hashMap.put("longitude",""+myLong);

        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(v -> {
            for (int i=0;i<cartItem.size();i++){
                String pId=cartItem.get(i).getPId();
                String cost=cartItem.get(i).getCost();
                String name=cartItem.get(i).getName();
                String price=cartItem.get(i).getPrice();
                String quantity=cartItem.get(i).getQuantity();

                HashMap<String,String> hashMap1=new HashMap<>();
                hashMap1.put("pId",pId);
                hashMap1.put("cost",cost);
                hashMap1.put("name",name);
                hashMap1.put("price",price);
                hashMap1.put("quantity",quantity);
                ref.child(timeStamp).child("Items").child(pId).setValue(hashMap1);
                progressDialog.dismiss();
                Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Carts.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Users");
        db.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    name = "" + ds.child("name").getValue();
                    myPhone = "" + ds.child("phone").getValue();
                    myLat = "" + ds.child("latitude").getValue();
                    myLong = "" + ds.child("longitude").getValue();
                }
            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });
    }

    private void init(){
        allTotalPriceTv=findViewById(R.id.allTotalPriceTv);
        deliveryFeeAmt=findViewById(R.id.deliveryFeeAmt);
        sTotalAmt=findViewById(R.id.sTotalAmt);
        cartRv=findViewById(R.id.cartRv);
        backBtn=findViewById(R.id.backBtn);
        confirmBtn=findViewById(R.id.confirmBtn);
    }
}