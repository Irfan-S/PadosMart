package espl.apps.padosmart.models

data class UserDataModel(
    var name: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var address: String? = null,
    var pinCode: String? = null,
    var city: String? = null,
    var state: String? = null,
    var country: String? = null,
    var orderHistory: ArrayList<OrderDataModel> = ArrayList()
)