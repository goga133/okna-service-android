package com.goga133.oknaservice.ui.lead

import android.app.Application
import androidx.lifecycle.*
import com.goga133.oknaservice.models.product.Product
import com.goga133.oknaservice.models.product.ProductDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeadViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ProductDatabase.getInstance(application).productDao()

    fun getProducts() = dao.getAll()

    fun deleteAllProduct() = viewModelScope.launch(Dispatchers.IO){
        dao.deleteAll()
    }

    fun deleteProduct(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        dao.delete(product)
    }
}