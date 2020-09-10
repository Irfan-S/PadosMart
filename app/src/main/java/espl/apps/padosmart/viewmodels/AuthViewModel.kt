package espl.apps.padosmart.viewmodels

import android.app.Application
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.services.LocationService

class AuthViewModel(app: Application) : AndroidViewModel(app) {


    val authRepository = AuthRepository(app)
    var userData: UserDataModel = UserDataModel()

    var locationService: LocationService? = null

    val address: MutableLiveData<Address> by lazy {
        MutableLiveData<Address>(null)
    }

    val isAddressFetchInProgress: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

}