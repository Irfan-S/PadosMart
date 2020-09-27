package espl.apps.padosmart.models


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderDataModel(
    var orderID: String? = null,
    var customerID: String? = null,
    var customerName: String? = null,
    var shopName: String? = null,
    var orderStatus: Int? = null,
    var customerOnline: Boolean? = true,
    var orderDeliveryLocation: String? = null,
    var orderPlacedTimeInMillis: Long? = null,
    var orderDeliveredTimeInMillis: Long? = null,
    var paymentType: Int? = null,
    var deliveryAddress: String? = null,
    var shopPublicID: String? = null,
    var OTP: Int? = null,
    var chats: ArrayList<ChatDataModel>? = ArrayList()
) : Parcelable