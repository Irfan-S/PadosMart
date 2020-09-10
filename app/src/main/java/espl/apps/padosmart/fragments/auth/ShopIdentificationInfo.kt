package espl.apps.padosmart.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import espl.apps.padosmart.R
import espl.apps.padosmart.viewmodels.AuthViewModel

class ShopIdentificationInfo : Fragment(), View.OnClickListener {

    val TAG = "SignupShopIDInfo"

    lateinit var localView: View

    lateinit var authViewModel: AuthViewModel

    lateinit var profileDisplaySelector: ImageView
    lateinit var proofImageSelector: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        localView =
            inflater.inflate(
                R.layout.fragment_signup_shop_identification_info,
                container,
                false
            ) as View

        //TODO complete implementation

        profileDisplaySelector = localView.findViewById(R.id.imageDisplaySelector)
        proofImageSelector = localView.findViewById(R.id.documentDisplaySelector)

        proofImageSelector.setOnClickListener(this)
        profileDisplaySelector.setOnClickListener(this)


        authViewModel = activity?.let { ViewModelProvider(it).get(AuthViewModel::class.java) }!!

        return localView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.documentDisplaySelector -> {

            }
            R.id.imageDisplaySelector -> {

            }
        }
    }

}