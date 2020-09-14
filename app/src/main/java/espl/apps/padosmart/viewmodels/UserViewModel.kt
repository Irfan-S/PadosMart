package espl.apps.padosmart.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.FirestoreRepository

class UserViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "UserViewModel"

    val authRepository = AuthRepository(app)
    val fireStoreRepository = FirestoreRepository(app)

    var selectedShop: ShopDataModel? = null


}