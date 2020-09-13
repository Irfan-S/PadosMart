package espl.apps.padosmart.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.OrderDataModel

class OrdersViewModel(app: Application) : AndroidViewModel(app) {

    val ordersList: MutableLiveData<ArrayList<OrderDataModel>> by lazy {
        MutableLiveData<ArrayList<OrderDataModel>>(ArrayList())
    }

    fun getOrdersList() {

    }


}