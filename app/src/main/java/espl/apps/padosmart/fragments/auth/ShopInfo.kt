package espl.apps.padosmart.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import espl.apps.padosmart.R

class ShopInfo: Fragment() {

    val TAG = "SignupShopInfo"

    lateinit var localView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        localView =
            inflater.inflate(R.layout.fragment_signup_shop, container, false) as View



        return localView
    }

}