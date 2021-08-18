package com.ashish.grocery.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashish.grocery.R
import com.ashish.grocery.user.CategoryActivity
import com.ashish.grocery.user.models.CategoryModel

class CategoryAdapter(val context: Context, var list:ArrayList<CategoryModel>):RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val inflater= LayoutInflater.from(context)
        val view=inflater.inflate(R.layout.category_row,parent,false)
        return CategoryHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val pos=list[position]

        holder.img.setImageResource(pos.categoryImg)
        holder.rel.setBackgroundColor(pos.color)
        holder.txt.text=pos.category
        holder.itemView.setOnClickListener {
            val intent= Intent(context,CategoryActivity::class.java)
            intent.putExtra("category",pos.category)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int= list.size

    class CategoryHolder(view: View):RecyclerView.ViewHolder(view){
        var img:ImageView=view.findViewById(R.id.productImg)
        var txt:TextView=view.findViewById(R.id.categoryTv)
        val rel:RelativeLayout=view.findViewById(R.id.pathRelative)
    }
}