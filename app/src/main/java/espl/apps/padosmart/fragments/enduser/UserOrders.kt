package espl.apps.padosmart.fragments.enduser

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
import espl.apps.padosmart.utils.QUERY_ARG_USER_ID
import espl.apps.padosmart.viewmodels.AppViewModel

class UserOrders : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private val TAG = "UserOrdersFragment"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_orders_user, container, false) as View

        linearLayoutManager = LinearLayoutManager(requireContext())

        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        appViewModel.getOrdersList(
            appViewModel.authRepository.getFirebaseUser()!!.uid,
            QUERY_ARG_USER_ID
        )
        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager
        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { _ ->
                run {
                    Log.d(TAG, "Setting adapters")
                    val adapter =
                        OrderHistoryAdapter(
                            QUERY_ARG_USER_ID,
                            orderList = appViewModel.ordersList.value!!,
                            object :
                                OrderHistoryAdapter.ButtonListener {
                                override fun onButtonClick(position: Int) {

                                    //TODO animate transition of exercise with sharedwindowtransition(?)
                                    Log.d(
                                        TAG,
                                        "Option selected is ${appViewModel.ordersList.value!![position].orderID}"
                                    )
                                    appViewModel.selectedOrder =
                                        appViewModel.ordersList.value!![position]

//                    findNavController().navigate(action)
                                }
                            })
                    exerciseRecyclerView.adapter = adapter
                }
            }

        appViewModel.ordersList.observe(viewLifecycleOwner, ordersObserver)

//        if (!orderViewModel.ordersList.value.isNullOrEmpty()) {
//            //show empty screen
//            Log.d(TAG,"List is empty")
//        } else {
//            Log.d(TAG,"Setting adapters")
//            val adapter =
//                OrderHistoryAdapter(QUERY_ARG_USER, orderList = orderViewModel.ordersList.value!!, object :
//                    OrderHistoryAdapter.ButtonListener {
//                    override fun onButtonClick(position: Int) {
//
//                        //TODO animate transition of exercise with sharedwindowtransition(?)
//                        Log.d(TAG, "Option selected is ${orderViewModel.ordersList.value!![position].orderID}")
//                        orderViewModel.selectedOrder = orderViewModel.ordersList.value!![position]
//
////                    findNavController().navigate(action)
//                    }
//                })
//            exerciseRecyclerView.adapter = adapter
//        }

        return view
    }


}