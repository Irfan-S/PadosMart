package espl.apps.padosmart.fragments.enduser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.LoginBase
import espl.apps.padosmart.utils.AUTH_ACCESS_FAILED
import espl.apps.padosmart.viewmodels.AppViewModel

class UserProfile : Fragment() {
    val TAG = "UserProfile"

    lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_profile_user, container, false) as View

        appViewModel = activity?.let { ViewModelProvider(it).get(AppViewModel::class.java) }!!

        val button: Button = view.findViewById(R.id.button)

        val intent = Intent(context, LoginBase::class.java)
        intent.putExtra(getString(R.string.intent_userType), AUTH_ACCESS_FAILED)

        button.setOnClickListener {
            appViewModel.signOut()
            startActivity(
                intent
            )
            requireActivity().finish()
        }

        return view
    }
}