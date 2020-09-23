package espl.apps.padosmart.fragments.commons

import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.UserBase
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.utils.END_USER
import espl.apps.padosmart.viewmodels.AppViewModel


class Chat : Fragment(), Toolbar.OnMenuItemClickListener {

    val TAG = "ChatFragment"


    private lateinit var localView: View

    lateinit var toolBar: Toolbar
    lateinit var appViewModel: AppViewModel
    lateinit var deliveryAddress: String

    var listenerRegistration: ListenerRegistration? = null

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

        toolBar = localView.findViewById<MaterialToolbar>(R.id.chatAppBar)


        appViewModel = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)



        if (appViewModel.appRepository.userType == END_USER) {


            toolBar.menu.findItem(R.id.confirmOrder).isVisible = false
            toolBar.menu.findItem(R.id.updateLocation).isVisible = true
            //Shop passed in when tile selected, so cannot be null
            toolBar.title = appViewModel.selectedShop!!.shopName

            val orderObserver = Observer<Boolean> { orderPlaced ->
                run {
                    toolBar.menu.findItem(R.id.confirmOrder).isVisible = orderPlaced
                }
            }

            appViewModel.orderRequested.observe(viewLifecycleOwner, orderObserver)

            deliveryAddress = appViewModel.userData.address!!

            val orderListener =
                EventListener<DocumentSnapshot> { value, error ->
                    val localOrder = value?.toObject<OrderDataModel>()
                    if (localOrder != null) {
                        appViewModel.orderRequested.value = localOrder.orderRequested

                        if (localOrder.orderConfirmed) {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Order confirmed",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else if (!localOrder.orderConfirmed) {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "No order placed",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }


            val addressObserver = Observer<Address> { newStatus ->
                run {
                    if (newStatus != null) {
                        Log.d(TAG, "New address updated: $newStatus")
                        deliveryAddress = newStatus.getAddressLine(0)
                        appViewModel.fireStoreRepository.updateOrderDetails(
                            appViewModel.orderID!!,
                            "deliveryAddress",
                            deliveryAddress,
                            object : FirestoreRepository.OnOrderUpdated {
                                override fun onSuccess(success: Boolean) {
                                    if (success) {
                                        Snackbar.make(
                                            requireActivity().findViewById(android.R.id.content),
                                            "Location updated",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Snackbar.make(
                                            requireActivity().findViewById(android.R.id.content),
                                            "Unable to update location",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }

                            })


                    }
                }
            }

            listenerRegistration = appViewModel.fireStoreRepository.attachOrderListener(
                appViewModel.orderID!!,
                orderListener
            )



            appViewModel.address.observe(viewLifecycleOwner, addressObserver)


        } else {
            toolBar.menu.findItem(R.id.cancelOrder).isVisible = false
            toolBar.menu.findItem(R.id.placeOrder).isVisible = true
        }
        toolBar.setOnMenuItemClickListener(this)

        //TODO complete chat UI and its implementation.

        return localView

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.commons_chat_menu, menu)
    }

    override fun onPause() {
        listenerRegistration?.remove()
        if (appViewModel.appRepository.userType == END_USER) {
            appViewModel.fireStoreRepository.updateOrderDetails(
                appViewModel.orderID!!,
                "isCustomerOnline",
                false,
                object : FirestoreRepository.OnOrderUpdated {
                    override fun onSuccess(success: Boolean) {
                        if (success) {
                            parentFragment?.findNavController()
                                ?.popBackStack()
                        } else {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Unable to connect to servers",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                })
        }

        super.onPause()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        Log.d(TAG, "Menu item clicked: ${item!!.itemId}")
        return when (item.itemId) {
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
                appViewModel.fireStoreRepository.updateOrderDetails(
                    appViewModel.orderID!!,
                    "orderRequested",
                    true,
                    object : FirestoreRepository.OnOrderUpdated {

                        override fun onSuccess(success: Boolean) {
                            toolBar.menu.findItem(R.id.placeOrder).isVisible = !success
                            toolBar.menu.findItem(R.id.cancelOrder).isVisible = success
                        }

                    })
                true
            }
            R.id.confirmOrder -> {
                appViewModel.fireStoreRepository.updateOrderDetails(
                    appViewModel.orderID!!,
                    "orderConfirmed",
                    true,
                    object : FirestoreRepository.OnOrderUpdated {

                        override fun onSuccess(success: Boolean) {
                            if (success) {
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    "Order successfully placed",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                toolBar.menu.findItem(R.id.confirmOrder).isVisible = false
                            }
                        }

                    })
                true
            }
            R.id.cancelOrder -> {
                appViewModel.fireStoreRepository.updateOrderDetails(
                    appViewModel.orderID!!,
                    "orderRequested",
                    true,
                    object : FirestoreRepository.OnOrderUpdated {

                        override fun onSuccess(success: Boolean) {
                            toolBar.menu.findItem(R.id.placeOrder).isVisible = success
                            toolBar.menu.findItem(R.id.cancelOrder).isVisible = !success
                        }

                    })
                true
            }
            else -> false
        }
    }


}