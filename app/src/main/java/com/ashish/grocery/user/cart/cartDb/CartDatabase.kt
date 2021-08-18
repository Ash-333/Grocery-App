package com.ashish.grocery.user.cart.cartDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Cart::class),version = 1,exportSchema = false)
abstract class CartDatabase :RoomDatabase(){

    abstract fun cartDao():CartDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: CartDatabase? = null

        fun getDatabase(context: Context): CartDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CartDatabase::class.java,
                    "cart_table"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}