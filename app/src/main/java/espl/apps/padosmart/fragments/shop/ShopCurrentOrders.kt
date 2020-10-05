package espl.apps.padosmart.fragments.shop

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.OrderHistoryAdapter
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.utils.QUERY_ARG_SHOP_ID
import espl.apps.padosmart.viewmodels.AppViewModel

class ShopCurrentOrders : Fragment() {

    lateinit var linearLayoutManager: LinearLayoutManager
    private val TAG = "ShopCurrentOrders"
    lateinit var currentOrdersAdapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        val emptyTextView = view.findViewById<TextView>(R.id.emptyListTextView)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.shopHomeAppBar)
        toolbar.title = appViewModel.shopData.shopName
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d(TAG, "Navigation on click clicked")
                view!!.findNavController().navigate(
                    R.id.action_shopCurrentOrders_to_shopProfile,
                    null
                )
            }
        })

        appViewModel.getCurrentOrdersList(
            QUERY_ARG_SHOP_ID,
            appViewModel.shopData.shopID!!,
        )

        currentOrdersAdapter = OrderHistoryAdapter(
            orderType = QUERY_ARG_SHOP_ID,
            orderList = appViewModel.ordersList.value!!,
            object : OrderHistoryAdapter.ButtonListener {
                override fun onButtonClick(position: Int) {
                    Log.d(
                        TAG,
                        "Option selected is ${currentOrdersAdapter.getItem(position).orderID}"
                    )
                    appViewModel.selectedOrder =
                        currentOrdersAdapter.getItem(position)
                    findNavController().navigate(R.id.action_shopCurrentOrders_to_shopOrder)
                }

            }
        )


        val exerciseRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        exerciseRecyclerView.layoutManager = linearLayoutManager


        val ordersObserver =
            Observer<ArrayList<OrderDataModel>> { it ->
                run {
                    Log.d(TAG, "Setting adapters")
                    if (it.isEmpty()) {
                        emptyTextView.visibility = View.VISIBLE
                    } else {
                        emptyTextView.visibility = View.GONE
                    }
                    currentOrdersAdapter.updateOrders(it)
                }
            }

        exerciseRecyclerView.adapter = currentOrdersAdapter

        appViewModel.ordersList.observe(viewLifecycleOwner, ordersObserver)

        return view
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
                currentOrdersAdapter.filter!!.filter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, menuInflater)
    }

}