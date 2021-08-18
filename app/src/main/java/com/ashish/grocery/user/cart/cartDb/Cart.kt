package com.ashish.grocery.user.cart.cartDb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cart_table")
class Cart(
    var image: String,
    var name: String,
    var price: String,
    var priceEach: String,
    var quantity: String,
    var pId: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}