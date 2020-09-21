package espl.apps.padosmart.viewmodels

import android.app.Application
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AppRepository
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.utils.END_USER

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "UserViewModel"

    val authRepository = AuthRepository(app)

    val appRepository = AppRepository(app)

    val fireStoreRepository = FirestoreRepository(app)

    var userData: UserDataModel = UserDataModel()
    var shopData = ShopDataModel()


    var newOrder = OrderDataModel()

    var selectedShop: ShopDataModel? = null
    var selectedOrder: OrderDataModel? = null

    var locationService: LocationService? = null

    val address: MutableLiveData<Address> by lazy {
        MutableLiveData<Address>(null)
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

    fun loadShopData() {
        authRepository.fetchShopDataObject(object :
            AuthRepository.ShopDataFetch {
            override fun onFetchComplete(shopDataModel: ShopDataModel?) {
                shopData = shopDataModel!!
            }
        })
    }


}