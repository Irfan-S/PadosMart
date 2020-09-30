package espl.apps.padosmart.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//TODO Web admin needs to add in date to object before initialization.
@Parcelize
data class ShopDataModel(
    var shopName: String? = null,
    var ownerName: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var address: String? = null,
    var pinCode: String? = null,
    var city: String? = null,
    var state: String? = null,
    var country: String? = null,
    var DOB: String? = null,
    var gender: Int? = null,

    //TODO All shops initialized to 5 stars?
    var shopTotalRating: Double? = 5.0,
    var shopTotalRatingCount: Int? = 1,
    var isOnline: Boolean = false,
    var shopCreationDate: Long? = null,
    var shopVisitCount: Long? = 0,
    var shopPrivateID: String? = null,
    var shopPublicID: String? = null,
    var shopImageURL: String? = null,
    var shopVerificationImageURL: String? = null,
    var shopDeliveryStart: Long? = null,
    var doesShopDeliver: Boolean? = false,
    var shopDeliveryEnd: Long? = null,
    var orderHistory: ArrayList<OrderDataModel> = ArrayList()

) : Parcelable