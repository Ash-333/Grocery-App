package com.ashish.grocery.user.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.grocery.R;
import com.ashish.grocery.user.models.CartItemModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartHolder> {
    private Context context;
    private ArrayList<CartItemModel> list;

    public CartAdapter(Context context, ArrayList<CartItemModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public CartHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_cart_item, parent, false);
        return new CartHolder(view);
    }

    @Override
    public void onBindViewHolder( CartHolder holder, int position) {
        CartItemModel item=list.get(position);
        holder.titleTv.setText(item.getName());
        holder.quantityTv.setText(item.getQuantity());
        holder.eachPriceTv.setText(item.getPrice());
        holder.priceTv.setText(item.getCost());
        Glide.with(context).load(item.getImage()).into(holder.productImg);
        holder.removeTv.setOnClickListener(view -> {

            Toast.makeText(context, "Product removed from cart..", Toast.LENGTH_SHORT).show();

            double cost=Double.parseDouble(item.getCost().replace("Rs",""));
            double tx =Double.parseDouble (((Carts)context).allTotalPriceTv.getText().toString().replace("Rs", ""));
            double totalPrice=tx-cost;
            double sTotal=totalPrice-100.0;
            ((Carts)context).allTotalPrice=0.0;
            ((Carts)context).sTotalAmt.setText((int) sTotal);
            ((Carts)context).allTotalPriceTv.setText((int) totalPrice);

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class CartHolder extends RecyclerView.ViewHolder{
        TextView titleTv,priceTv,eachPriceTv,quantityTv,removeTv;
        ImageView productImg;
        public CartHolder( View view) {
            super(view);
            titleTv=view.findViewById(R.id.titleTv);
            priceTv=view.findViewById(R.id.itemPrice);
            eachPriceTv=view.findViewById(R.id.eachItemPrice);
            quantityTv=view.findViewById(R.id.itemQuantity);
            removeTv=view.findViewById(R.id.removeItem);
            productImg=view.findViewById(R.id.productImg);
        }
    }
}
