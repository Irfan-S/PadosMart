package espl.apps.padosmart.viewmodels

import android.app.Application
import android.location.Address
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.ChatDataModel
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AppRepository
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.ChatRepository
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.utils.*


class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "AppViewModel"

    val authRepository = AuthRepository(app)
    val appRepository = AppRepository(app)
    val fireStoreRepository = FirestoreRepository(app)
    val chatRepository = ChatRepository()

    var orderID: String? = null

    var firebaseUser = authRepository.getFirebaseUser()

    var userData: UserDataModel = UserDataModel()
    var shopData = ShopDataModel()

    var selectedShop: ShopDataModel? = null

    var locationService: LocationService? = null

    val address: MutableLiveData<Address> by lazy {
        MutableLiveData<Address>(null)
    }

    val orderStatus: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(ORDER_STATUS_NOT_PLACED)
    }

    val chats: MutableLiveData<ArrayList<ChatDataModel>> by lazy {
        MutableLiveData<ArrayList<ChatDataModel>>(ArrayList())
    }

    val isAddressFetchInProgress: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun loadUserData(callback: AuthRepository.AuthDataInterface) {
        authRepository.getFirebaseUserType(callback)
    }

    fun getDate(): Long {
        return System.currentTimeMillis()
    }

    fun loadShopData(callback: AuthRepository.AuthDataInterface) {
        authRepository.getFirebaseUserType(callback)
    }

    var selectedOrder: OrderDataModel? = null


    val ordersList: MutableLiveData<ArrayList<OrderDataModel>> by lazy {
        MutableLiveData<ArrayList<OrderDataModel>>(ArrayList())
    }

    val recentShopsList: MutableLiveData<ArrayList<ShopDataModel>> by lazy {
        MutableLiveData<ArrayList<ShopDataModel>>(ArrayList())
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


    fun getCurrentOrdersList(queryID: String, queryArg: String) {
        Log.d(TAG, "fetching orders")
        fireStoreRepository.fetchCompoundQueryOrdersFromFirestore(
            queryID,
            queryArg,
            "orderStatus",
            listOf(ORDER_STATUS_CONFIRMED, ORDER_STATUS_IN_PROGRESS),
            object :
                FirestoreRepository.OnOrdersFetched {
                override fun onSuccess(orderList: ArrayList<OrderDataModel>) {
                    Log.d(TAG, "Order list: $orderList")
                    ordersList.value = orderList
                }
            })
    }

    fun updateShopCount() {
        fireStoreRepository.runTransaction(
            NODE_SHOPS,
            shopPublicID = selectedShop!!.shopPublicID!!,
            editNode = QUERY_ARG_SHOP_COUNTER,
            object : FirestoreRepository.OnFirestoreCallback {
                override fun onUploadSuccessful(isSuccess: Boolean) {
                    Log.d(TAG, "Successfully updated counter for shop visits")
                }

            }
        )
    }

    fun getOnlineCustomerList(shopID: String) {
        Log.d(TAG, "Fetching live customers")
        fireStoreRepository.fetchOnlineCustomersFromFirestore(shopID, object :
            FirestoreRepository.OnOrdersFetched {
            override fun onSuccess(orderList: ArrayList<OrderDataModel>) {
                Log.d(TAG, "Live customer list: $orderList")
                ordersList.value = orderList
            }
        })
    }


    fun fetchRecentShops() {
        fireStoreRepository.fetchRecentShops(

        )
    }

    fun fetchNewShops() {

    }


}