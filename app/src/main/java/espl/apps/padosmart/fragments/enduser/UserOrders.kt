package espl.apps.padosmart.fragments.enduser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.OrderHistoryAdapter
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


        val orderViewModel: OrdersViewModel =
            ViewModelProvider(this).get(OrdersViewModel::class.java)
        val ordersList = orderViewModel.getOrderList()

        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager


        val adapter =
            OrderHistoryAdapter(orderList = ordersList, object :
                OrderHistoryAdapter.ButtonListener {
                override fun onButtonClick(position: Int) {

                    //TODO animate transition of exercise with sharedwindowtransition(?)
                    Log.d(TAG, "Option selected is ${ordersList[position].orderID}")
                    val exerciseSelected = ordersList[position]
                    val action = ExercisesFragmentDirections.viewExerciseAction(exerciseSelected)
                    findNavController().navigate(action)
                }
            })
        exerciseRecyclerView.adapter = adapter

        return view
    }


}