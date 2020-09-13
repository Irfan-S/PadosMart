package espl.apps.padosmart.models

data class OrderDataModel(
    val orderID: Long,
    val customerName: String,
    val shopID: String,
    val amount: Int,
    var orderPlacedTimeInMillis: Long,
    var orderDeliveredTimeInMillis: Long? = null,
    val paymentType: Int
)