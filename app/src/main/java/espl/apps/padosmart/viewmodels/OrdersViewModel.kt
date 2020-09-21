package espl.apps.padosmart.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.FirestoreRepository

class OrdersViewModel(app: Application) : AndroidViewModel(app) {

    val authRepository = AuthRepository(app)
    val TAG = "OrdersViewModel"
    val fireStoreRepository = FirestoreRepository(app)

    var selectedOrder: OrderDataModel? = null


    val ordersList: MutableLiveData<ArrayList<OrderDataModel>> by lazy {
        MutableLiveData<ArrayList<OrderDataModel>>(ArrayList())
    }

    //TODO add input limiting
    fun getOrdersList(queryID: String, queryArg: String) {

        Log.d(TAG, "fetching orders")
        fireStoreRepository.fetchQueryOrdersFromFirestore(queryID, queryArg, object :
            FirestoreRepository.OnOrdersFetched {
            override fun onSuccess(orderList: ArrayList<OrderDataModel>) {
                Log.d(TAG, "Order list: $orderList")
                ordersList.value = orderList
            }
        }, limit = 10)
    }


}