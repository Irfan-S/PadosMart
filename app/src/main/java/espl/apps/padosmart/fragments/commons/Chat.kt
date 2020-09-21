package espl.apps.padosmart.fragments.commons

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.UserBase
import espl.apps.padosmart.utils.END_USER
import espl.apps.padosmart.viewmodels.AppViewModel

class Chat : Fragment(), Toolbar.OnMenuItemClickListener {

    val TAG = "ChatActivity"


    private lateinit var localView: View


    lateinit var appViewModel: AppViewModel


    lateinit var address: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        localView =
            inflater.inflate(R.layout.fragment_commons_chatscreen, container, false) as View

        //TODO create intent that fetches data of the caller, whether user or shop

        val toolBar = localView.findViewById<MaterialToolbar>(R.id.chatAppBar)

        appViewModel = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)


        if (appViewModel.appRepository.userType == END_USER) {

            toolBar.menu.findItem(R.id.confirmOrder).isVisible = true
            toolBar.menu.findItem(R.id.updateLocation).isVisible = true
            //Shop passed in when tile selected, so cannot be null
            toolBar.title = appViewModel.selectedShop!!.shopName
        } else {
            toolBar.menu.findItem(R.id.placeOrder).isVisible = true
        }

        //TODO complete chat UI and its implementation.

        return localView

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.updateLocation -> {
                appViewModel.locationService!!.checkGpsStatus()
                if ((activity as UserBase).foregroundPermissionApproved()) {
                    appViewModel.locationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    (activity as UserBase).requestForegroundPermissions()
                }
                true
            }
            R.id.placeOrder -> {

                true
            }
            R.id.confirmOrder -> {

                true
            }
            else -> false
        }
    }



}