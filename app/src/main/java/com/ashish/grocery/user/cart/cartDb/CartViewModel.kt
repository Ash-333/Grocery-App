package com.ashish.grocery.user.cart.cartDb

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartViewModel(application: Application):AndroidViewModel(application) {
    private val repository:CartRepository
    val list: LiveData<List<Cart>>
    init {
        val dao=CartDatabase.getDatabase(application).cartDao()
        repository=CartRepository(dao)
        list=repository.list
    }
    fun delete(list: Cart)=viewModelScope.launch(Dispatchers.IO) {
        repository.delete(list)
    }
    fun insert(list: Cart)=viewModelScope.launch ( Dispatchers.IO ){
        repository.insert(list)
    }
}