package espl.apps.padosmart.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import espl.apps.padosmart.R

class ShopDetails: Fragment() {

    val TAG = "SignupShopDetails"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_signup_shop_details, container, false) as View

        return view
    }

}