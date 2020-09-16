package espl.apps.padosmart.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.repository.AppRepository
import espl.apps.padosmart.repository.AuthRepository

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "UserViewModel"

    val authRepository = AuthRepository(app)

    val appRepository = AppRepository(app)


    var selectedShop: ShopDataModel? = null
    var selectedOrder: OrderDataModel? = null


}