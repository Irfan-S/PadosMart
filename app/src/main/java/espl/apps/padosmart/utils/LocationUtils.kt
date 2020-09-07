package espl.apps.padosmart.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.util.*

fun Location?.toText():String {
        return if (this != null) {
            "($latitude, $longitude)"
        } else {
            "Unknown location"
        }
    }

fun Location?.getAddress(context:Context,latitude:Double,longitude:Double): Address {
    val addresses: List<Address>
    val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    addresses = geocoder.getFromLocation(
        latitude,
        longitude,
        1
    ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

//
//        val address: String =
//            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//        val city: String = addresses[0].locality
//        val state: String = addresses[0].adminArea
//        val country: String = addresses[0].countryName
//        val postalCode: String = addresses[0].postalCode
//        val knownName: String = addresses[0].featureName // Only if available else return NULL

    return addresses[0]
}
