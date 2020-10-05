package espl.apps.padosmart.fragments.enduser

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.OrderHistoryAdapter
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.utils.QUERY_ARG_CUSTOMER_ID
import espl.apps.padosmart.viewmodels.AppViewModel

class UserOrders : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private val TAG = "UserOrdersFragment"

    lateinit var ordersAdapter: OrderHistoryAdapter

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
            QUERY_ARG_CUSTOMER_ID,
            appViewModel.firebaseUser!!.uid
        )
        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager


        val toolbar = view.findViewById<MaterialToolbar>(R.id.userAppBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d(TAG, "Navigation on click clicked")
                view!!.findNavController().navigate(
                    R.id.action_homeFragmentUser_to_profileFragmentUser,
                    null
                )
            }
        })

        ordersAdapter =
            OrderHistoryAdapter(
                QUERY_ARG_CUSTOMER_ID,
                orderList = appViewModel.ordersList.value!!,
                object :
                    OrderHistoryAdapter.ButtonListener {
                    override fun onButtonClick(position: Int) {

                        //TODO animate transition of exercise with sharedwindowtransition(?)
                        Log.d(
                            TAG,
                            "Option selected is ${ordersAdapter.getItem(position).orderID}"
                        )
                        appViewModel.selectedOrder =
                            ordersAdapter.getItem(position)

//                    findNavController().navigate(action)
                    }
                })
        exerciseRecyclerView.adapter = ordersAdapter

        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { it ->
                run {
                    Log.d(TAG, "Updating adapters")
                    ordersAdapter.updateOrders(it)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.user_home_appbar, menu)


        // Associate searchable configuration with the SearchView
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search)
            .actionView as SearchView
        searchView.setSearchableInfo(
            searchManager
                .getSearchableInfo(requireActivity().componentName)
        )

        Log.d(TAG, "Menu created")
        searchView.maxWidth = Int.MAX_VALUE

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "Entering text in searchbar")
                ordersAdapter.filter!!.filter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, menuInflater)
    }


}