package espl.apps.padosmart.models

import com.google.firebase.Timestamp

data class OrderDataModel(
    var orderID: String? = null,
    var customerID: String? = null,
    var customerName: String? = null,
    var shopID: String? = null,
    var shopName: String? = null,
    var orderStatus: Int? = null,
    var orderDeliveryLocation: String? = null,
    var orderPlacedTimeInMillis: Timestamp? = null,
    var orderDeliveredTimeInMillis: Timestamp? = null,
    var paymentType: Int? = null
)