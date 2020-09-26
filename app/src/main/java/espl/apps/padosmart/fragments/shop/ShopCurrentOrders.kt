package espl.apps.padosmart.fragments.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.OrderHistoryAdapter
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.utils.QUERY_ARG_SHOP_ID
import espl.apps.padosmart.viewmodels.AppViewModel

class ShopCurrentOrders : Fragment() {

    lateinit var linearLayoutManager: LinearLayoutManager
    private val TAG = "ShopCurrentOrders"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_current_orders_shop, container, false) as View

        linearLayoutManager = LinearLayoutManager(requireContext())

        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        appViewModel.getCurrentOrdersList(
            QUERY_ARG_SHOP_ID,
            appViewModel.shopData.shopPublicID!!,
        )

        val currentOrdersAdapter = OrderHistoryAdapter(
            orderType = QUERY_ARG_SHOP_ID,
            orderList = appViewModel.ordersList.value!!,
            object : OrderHistoryAdapter.ButtonListener {
                override fun onButtonClick(position: Int) {
                    Log.d(
                        TAG,
                        "Option selected is ${appViewModel.ordersList.value!![position].orderID}"
                    )
                    appViewModel.selectedOrder =
                        appViewModel.ordersList.value!![position]
                }

            }
        )


        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager


        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { it ->
                run {
                    Log.d(TAG, "Setting adapters")
                    currentOrdersAdapter.updateOrders(it)
                }
            }

        exerciseRecyclerView.adapter = currentOrdersAdapter

        appViewModel.ordersList.observe(viewLifecycleOwner, ordersObserver)

        return view
    }
}