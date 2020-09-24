package espl.apps.padosmart.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatDataModel(
    val senderName: String? = null,
    val message: String? = null,
    val time: Long? = null,
    val attachmentURI: String? = null
) : Parcelable