package com.ashish.grocery.user.cart.cartDb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CartDao {
    @Insert
    fun insert(cartItem:Cart)

    @Delete
    fun delete(cartItem: Cart)

    @Query("Select * from cart_table ")
    fun getAllItem():LiveData<List<Cart>>

    @Query("Delete  from cart_table")
    fun delAllItem()
}