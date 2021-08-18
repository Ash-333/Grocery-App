package com.ashish.grocery.user.cart.cartDb

import androidx.lifecycle.LiveData

class CartRepository(private var cartDao: CartDao) {

    val list:LiveData<List<Cart>> = cartDao.getAllItem()

    suspend fun insert(cartItem:Cart){
        cartDao.insert(cartItem)
    }
    suspend fun delete(cartItem:Cart){
        cartDao.delete(cartItem)
    }
}