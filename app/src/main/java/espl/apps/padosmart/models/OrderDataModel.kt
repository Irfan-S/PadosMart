package espl.apps.padosmart.models

data class OrderDataModel(
    var orderID: String,
    var customerID: String,
    var customerName: String,
    var shopID: String,
    var shopName: String,
    var amount: Int,
    var orderStatus: Int,
    var orderPlacedTimeInMillis: Long,
    var orderDeliveredTimeInMillis: Long? = null,
    var paymentType: Int
)