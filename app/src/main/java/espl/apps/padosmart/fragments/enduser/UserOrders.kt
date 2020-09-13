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
import espl.apps.padosmart.utils.QUERY_ARG_USER
import espl.apps.padosmart.viewmodels.OrdersViewModel

class UserOrders : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private val TAG = "ExercisesFragment"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_orders_user, container, false) as View

        linearLayoutManager = LinearLayoutManager(requireContext())

        var ordersList: ArrayList<OrderDataModel>? = ArrayList()
        val orderViewModel: OrdersViewModel =
            ViewModelProvider(this).get(OrdersViewModel::class.java)
        orderViewModel.getOrdersList(
            orderViewModel.authRepository.getFirebaseUser()!!.uid,
            QUERY_ARG_USER
        )
        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager
        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { _ ->
                run {
                    ordersList = orderViewModel.ordersList.value
                    exerciseRecyclerView.adapter?.notifyDataSetChanged()
                }
            }

        if (!ordersList.isNullOrEmpty()) {
            //show empty screen
        } else {
            val adapter =
                OrderHistoryAdapter(QUERY_ARG_USER, orderList = ordersList!!, object :
                    OrderHistoryAdapter.ButtonListener {
                    override fun onButtonClick(position: Int) {

                        //TODO animate transition of exercise with sharedwindowtransition(?)
                        Log.d(TAG, "Option selected is ${ordersList!![position].orderID}")
                        orderViewModel.selectedOrder = ordersList!![position]

//                    findNavController().navigate(action)
                    }
                })
            exerciseRecyclerView.adapter = adapter
        }

        return view
    }


}