package espl.apps.padosmart.fragments.shop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.AuthBase

class ShopProfile : Fragment() {
    val TAG = "ShopProfile"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_profile_shop, container, false) as View

        val button: Button = view.findViewById(R.id.button)
        button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(
                    context,
                    AuthBase::class.java
                )
            )
            requireActivity().finish()
        }

        return view
    }
}