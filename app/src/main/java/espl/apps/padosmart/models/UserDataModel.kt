package espl.apps.padosmart.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDataModel(
    var name: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var address: String? = null
) : Parcelable