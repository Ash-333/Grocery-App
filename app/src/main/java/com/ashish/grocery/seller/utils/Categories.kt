package com.ashish.grocery.seller.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.ashish.grocery.R

class Categories {
    val categories=arrayOf("Beverages"
        ,"Cookies, Snacks & Candy"
        , "Vegetables"
        ,"Sugar"
        ,"Shampoo"
        ,"Toothbrush/Dental Accessories"
        ,"Deodorant"
        ,"Bread & Bakery"
        ,"Miscellaneous ")

     fun showErrorToast(message: String?,context: Context) {
        val toast = Toast(context)
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.error_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }

     fun showSuccessToast(message: String?,context: Context) {
        val toast = Toast(context)
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.success_toast, null)
        val tvMessage: TextView = view.findViewById(R.id.toastText)
        tvMessage.text = message
        toast.view = view
        toast.show()
    }
}