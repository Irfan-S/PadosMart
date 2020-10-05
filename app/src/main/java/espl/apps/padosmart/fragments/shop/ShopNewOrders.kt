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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
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
    lateinit var chatListAdapter: ChatListDisplayAdapter

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
            inflater.inflate(R.layout.fragment_new_orders_shop, container, false) as View

        linearLayoutManager = LinearLayoutManager(requireContext())

        val emptyTextView = view.findViewById<TextView>(R.id.emptyListTextView)

        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        appViewModel.getOnlineCustomerList(
            appViewModel.shopData.shopID!!
        )
        val toolbar = view.findViewById<MaterialToolbar>(R.id.shopHomeAppBar)
        toolbar.title = appViewModel.shopData.shopName
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d(TAG, "Navigation on click clicked")
                view!!.findNavController().navigate(
                    R.id.action_shopNewOrders_to_shopProfile,
                    null
                )
            }
        })

        chatListAdapter = ChatListDisplayAdapter(
            orderList = appViewModel.ordersList.value!!,
            object : ChatListDisplayAdapter.ButtonListener {
                override fun onButtonClick(position: Int) {
                    Log.d(
                        TAG,
                        "Option selected is ${chatListAdapter.getItem(position).orderID}"
                    )
                    appViewModel.selectedOrder =
                        chatListAdapter.getItem(position)

                    appViewModel.orderID =
                        chatListAdapter.getItem(position).orderID
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
                chatListAdapter.filter!!.filter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, menuInflater)
    }
}