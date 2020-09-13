package espl.apps.padosmart.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.FirestoreRepository

class OrdersViewModel(app: Application) : AndroidViewModel(app) {

    val authRepository = AuthRepository(app)
    val fireStoreRepository = FirestoreRepository(app)

    var selectedOrder: OrderDataModel? = null


    val ordersList: MutableLiveData<ArrayList<OrderDataModel>> by lazy {
        MutableLiveData<ArrayList<OrderDataModel>>(ArrayList())
    }

    fun getOrdersList(queryID: String, queryArg: String) {
        fireStoreRepository.fetchAllOrders(queryID, queryArg, object :
            FirestoreRepository.OnOrdersFetched {
            override fun onSuccess(orderList: ArrayList<OrderDataModel>) {
                ordersList.value = orderList
            }
        })
    }


}