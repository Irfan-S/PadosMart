package espl.apps.padosmart.viewmodels

import android.app.Application
import android.location.Address
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AppRepository
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.ChatRepository
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.utils.END_USER
import espl.apps.padosmart.utils.SHOP_USER


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

    val orderRequested: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val isAddressFetchInProgress: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun loadUserData() {
        authRepository.getFirebaseUserType(callback = object : AuthRepository.AuthDataInterface {
            override fun onAuthCallback(response: Long) {
                if (response == END_USER.toLong()) {
                    authRepository.getEndUserDataObject(callback = object :
                        AuthRepository.UserDataInterface {
                        override fun onUploadCallback(success: Boolean) {
                            //Nothing
                        }

                        override fun onDataFetch(dataModel: UserDataModel) {
                            userData = dataModel
                        }

                    })
                }
            }

        })
    }

    fun getDate(): Timestamp {
        return Timestamp.now()
    }

    fun loadShopData() {
        authRepository.getFirebaseUserType(callback = object : AuthRepository.AuthDataInterface {
            override fun onAuthCallback(response: Long) {
                if (response == SHOP_USER.toLong()) {
                    authRepository.fetchShopDataObject(object :
                        AuthRepository.ShopDataFetch {
                        override fun onFetchComplete(shopDataModel: ShopDataModel?) {
                            shopData = shopDataModel!!
                            Log.d(TAG, "Shop data: $shopData")
                        }
                    })
                }
            }

        })

    }

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


    fun fetchNewShops() {

    }


}