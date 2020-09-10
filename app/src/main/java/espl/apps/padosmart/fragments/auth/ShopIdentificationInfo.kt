package espl.apps.padosmart.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import espl.apps.padosmart.R

class ShopIdentificationInfo : Fragment() {

    val TAG = "SignupShopIDInfo"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(
                R.layout.fragment_signup_shop_identification_info,
                container,
                false
            ) as View

        return view
    }

}