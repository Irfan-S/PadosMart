package espl.apps.padosmart.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

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
    var shopImageURL: String? = null,
    var shopVerificationImageURL: String? = null,
    var shopDeliveryRadius: Int? = null,
    var shopDeliveryStart: Long? = null,
    var shopDeliveryEnd: Long? = null,
    var shopDeliveryCosts: Double? = null,
) : Parcelable