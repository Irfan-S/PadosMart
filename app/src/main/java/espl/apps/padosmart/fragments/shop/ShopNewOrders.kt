package espl.apps.padosmart.fragments.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.ChatListDisplayAdapter
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.viewmodels.AppViewModel

class ShopNewOrders : Fragment() {

    val TAG = "ShopNewOrders"
    lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_new_orders_shop, container, false) as View

        linearLayoutManager = LinearLayoutManager(requireContext())


        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
        if (appViewModel.shopData.shopPublicID != null) {
            appViewModel.getOnlineCustomerList(
                appViewModel.shopData.shopPublicID!!
            )
        }

        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.newOrdersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager
        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { _ ->
                run {
                    Log.d(TAG, "Setting adapters")
                    val adapter =
                        ChatListDisplayAdapter(
                            orderList = appViewModel.ordersList.value!!,
                            object :
                                ChatListDisplayAdapter.ButtonListener {
                                override fun onButtonClick(position: Int) {

                                    //TODO animate transition of exercise with sharedwindowtransition(?)
                                    Log.d(
                                        TAG,
                                        "Option selected is ${appViewModel.ordersList.value!![position].orderID}"
                                    )
                                    appViewModel.selectedOrder =
                                        appViewModel.ordersList.value!![position]

                                    appViewModel.orderID =
                                        appViewModel.ordersList.value!![position].orderID
                                    view.findNavController()
                                        .navigate(R.id.action_shopNewOrders_to_shopChat)
//                    findNavController().navigate(action)
                                }
                            })
                    exerciseRecyclerView.adapter = adapter
                }
            }
        appViewModel.ordersList.observe(viewLifecycleOwner, ordersObserver)
        return view
    }
}