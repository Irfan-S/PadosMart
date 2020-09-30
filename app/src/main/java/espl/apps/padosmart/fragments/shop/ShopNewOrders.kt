package espl.apps.padosmart.fragments.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.ChatListDisplayAdapter
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.viewmodels.AppViewModel

class ShopNewOrders : Fragment() {

    val TAG = "ShopNewOrders"

    lateinit var listenerRegistration: ListenerRegistration
    lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_new_orders_shop, container, false) as View

        linearLayoutManager = LinearLayoutManager(requireContext())

        val emptyTextView = view.findViewById<TextView>(R.id.emptyListTextView)

        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        appViewModel.getOnlineCustomerList(
            appViewModel.shopData.shopID!!
        )

        val chatListAdapter = ChatListDisplayAdapter(
            orderList = appViewModel.ordersList.value!!,
            object : ChatListDisplayAdapter.ButtonListener {
                override fun onButtonClick(position: Int) {
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
                }

            }
        )


        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.newOrdersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager
        exerciseRecyclerView.adapter = chatListAdapter

        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { it ->
                run {
                    Log.d(TAG, "orders updated with $it")
                    chatListAdapter.updateChatList(it)
                }
            }
        appViewModel.ordersList.observe(viewLifecycleOwner, ordersObserver)

        val orderListener =
            EventListener<QuerySnapshot> { value, error ->
                run {
                    Log.d(TAG, "Snapshot received is :$value")
                    val activeOrdersList = ArrayList<OrderDataModel>()
                    for (shop in value!!) {

                        activeOrdersList.add(shop.toObject<OrderDataModel>())
                    }

                    if (activeOrdersList.isEmpty()) {
                        emptyTextView.visibility = View.VISIBLE
                    } else {
                        emptyTextView.visibility = View.GONE
                    }

                    appViewModel.ordersList.value = activeOrdersList
                }
            }


        listenerRegistration = appViewModel.fireStoreRepository.attachNewChatShopListener(
            appViewModel.shopData.shopID!!,
            orderListener
        )

        return view
    }
}