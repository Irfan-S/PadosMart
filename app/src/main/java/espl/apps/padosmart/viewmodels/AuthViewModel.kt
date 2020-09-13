package espl.apps.padosmart.viewmodels

import android.app.Application
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.services.LocationService

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "AuthViewModel"


    val authRepository = AuthRepository(app)
    val fireStoreRepository = FirestoreRepository(app)
    var userData: UserDataModel = UserDataModel()
    var shopDataModel = ShopDataModel()

    var locationService: LocationService? = null

    val address: MutableLiveData<Address> by lazy {
        MutableLiveData<Address>(null)
    }

    val isAddressFetchInProgress: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }


}