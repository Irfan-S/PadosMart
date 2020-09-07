package espl.apps.padosmart.viewmodels

import android.app.Application
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import espl.apps.padosmart.repository.AuthRepository

class SignupViewModel (app: Application) : AndroidViewModel(app) {


    val authRepository = AuthRepository(app)

    val address: MutableLiveData<Address> by lazy{
        MutableLiveData<Address>(null)
    }

    val isAddressFetchInProgress:MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>(false)
    }



}