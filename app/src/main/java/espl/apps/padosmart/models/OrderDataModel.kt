package espl.apps.padosmart.models

import com.google.firebase.Timestamp

data class OrderDataModel(
    var orderID: String,
    var customerID: String,
    var customerName: String,
    var shopID: String,
    var shopName: String,
    var amount: Int,
    var orderStatus: Int,
    var orderDeliveryLocation: String,
    var orderPlacedTimeInMillis: Timestamp,
    var orderDeliveredTimeInMillis: Timestamp,
    var paymentType: Int
)