package espl.apps.padosmart.models


import com.google.firebase.Timestamp


data class OrderDataModel(
    var orderID: String? = null,
    var customerID: String? = null,
    var customerName: String? = null,
    var shopName: String? = null,
    var orderStatus: Int? = null,
    var customerOnline: Boolean? = true,
    var orderDeliveryLocation: String? = null,
    var orderPlacedTimeInMillis: Timestamp? = null,
    var orderDeliveredTimeInMillis: Timestamp? = null,
    var paymentType: Int? = null,
    var deliveryAddress: String? = null,
    var shopPublicID: String? = null,
    var orderRequested: Boolean = false,
    var orderConfirmed: Boolean = false,
    var chats: ArrayList<String>? = ArrayList()
)