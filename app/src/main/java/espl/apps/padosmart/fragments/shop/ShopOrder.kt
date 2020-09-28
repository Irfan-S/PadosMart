package espl.apps.padosmart.fragments.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.ChatAdapter
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.utils.ORDER_STATUS_CANCELLED
import espl.apps.padosmart.utils.ORDER_STATUS_COMPLETED
import espl.apps.padosmart.utils.QUERY_ARG_ORDER_STATUS
import espl.apps.padosmart.viewmodels.AppViewModel


class ShopOrder : Fragment(), View.OnClickListener {

    val TAG = "OrderFragment"


    private lateinit var localView: View

    lateinit var toolBar: Toolbar
    lateinit var appViewModel: AppViewModel

    lateinit var chatAdapter: ChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        localView =
            inflater.inflate(R.layout.fragment_order_shop, container, false) as View

        //TODO create intent that fetches data of the caller, whether user or shop

        toolBar = localView.findViewById<MaterialToolbar>(R.id.orderShopAppBar)


        val orderCompleteButton = localView.findViewById<Button>(R.id.completeOrderButton)

        val orderCancelledButton = localView.findViewById<Button>(R.id.orderCancelledButton)

        orderCompleteButton.setOnClickListener(this)
        orderCancelledButton.setOnClickListener(this)



        linearLayoutManager = LinearLayoutManager(requireContext())
        val chatRecyclerView: RecyclerView =
            localView.findViewById(R.id.recyclerview_message_list_order)
        chatRecyclerView.layoutManager = linearLayoutManager


        appViewModel = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)



        Log.d(TAG, "Order data: ${appViewModel.selectedOrder}")

        chatAdapter = ChatAdapter(
            appViewModel.selectedOrder!!.shopName!!,
            appViewModel.selectedOrder!!.chats!!
        )

        toolBar.title = appViewModel.selectedOrder!!.customerName


        chatRecyclerView.adapter = chatAdapter


//        val chatsObserver = Observer<ArrayList<ChatDataModel>> { orderStatus ->
//            run {
//                if(orderStatus.isNotEmpty()) {
//                    Log.d(TAG, "Chats added :$orderStatus")
//                    chatAdapter.updateChats(orderStatus)
//                }
//            }
//        }
//        appViewModel.chats.observe(viewLifecycleOwner, chatsObserver)


        return localView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.completeOrderButton -> {
                showOTPAlertDialog()
            }
            R.id.orderCancelledButton -> {
                showCancelAlertDialog()
            }
        }
    }

    private fun showOTPAlertDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Complete Order")
        // set the custom layout
        val customLayout: View = layoutInflater
            .inflate(
                R.layout.element_confirm_order_dialog,
                null
            )

        val otpEntry = customLayout.findViewById<EditText>(R.id.otpShopEditText)



        alertDialog.setView(customLayout)
        alertDialog.setMessage("Enter the OTP to complete the order")
        alertDialog.setPositiveButton(
            "Submit"
        ) { _, _ ->
            var OTP = otpEntry.text.toString().toInt()
            if (appViewModel.selectedOrder!!.OTP == OTP) {
                appViewModel.fireStoreRepository.updateOrderDetails(
                    orderID = appViewModel.selectedOrder!!.orderID!!,
                    orderObject = QUERY_ARG_ORDER_STATUS,
                    details = ORDER_STATUS_COMPLETED,
                    object : FirestoreRepository.OnOrderUpdated {
                        override fun onSuccess(success: Boolean) {
                            if (success) {
                                Toast.makeText(
                                    requireContext(),
                                    "Order successfully completed.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Unable to complete order, try again later",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    }
                )
            } else {
                Toast.makeText(requireContext(), "Incorrect OTP", Toast.LENGTH_LONG).show()
            }

        }
        alertDialog.setNegativeButton(
            "Cancel"
        ) { _, _ -> }

        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun showCancelAlertDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Cancel Order")
        // set the custom layout


        alertDialog.setMessage("Are you sure want to cancel this order? Note that cancellation can negatively affect your reputation if your customer disapproves ")
        alertDialog.setPositiveButton(
            "Yes"
        ) { _, _ ->
            appViewModel.fireStoreRepository.updateOrderDetails(
                orderID = appViewModel.selectedOrder!!.orderID!!,
                orderObject = QUERY_ARG_ORDER_STATUS,
                details = ORDER_STATUS_CANCELLED,
                object : FirestoreRepository.OnOrderUpdated {
                    override fun onSuccess(success: Boolean) {
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                "Order cancelled successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Unable to cancel order, try again later",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
            )
        }
        alertDialog.setNegativeButton(
            "No"
        ) { _, _ -> }

        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }


}
