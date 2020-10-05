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
import espl.apps.padosmart.adapters.ShopDisplayAdapter
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.viewmodels.AppViewModel


class UserHome : Fragment() {

    val TAG = "UserHome"
    lateinit var toolbar: MaterialToolbar
    lateinit var recentShopsAdapter: ShopDisplayAdapter
    lateinit var popularShopsAdapter: ShopDisplayAdapter
    lateinit var newShopsAdapter: ShopDisplayAdapter
    lateinit var appViewModel: AppViewModel

    lateinit var localView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        localView =
            inflater.inflate(R.layout.fragment_home_user, container, false) as View
        val recentShopsHorizontalLayout = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val popularShopsHorizontalLayout = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val newShopsHorizontalLayout = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        toolbar = localView.findViewById<MaterialToolbar>(R.id.userAppBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                Log.d(TAG, "Navigation on click clicked")
                localView.findNavController().navigate(
                    R.id.action_homeFragmentUser_to_profileFragmentUser,
                    null
                )
            }
        })

        val recentShopsRecyclerView: RecyclerView =
            localView.findViewById(R.id.recentsShopsRecyclerView)
        val popularShopsRecyclerView: RecyclerView =
            localView.findViewById(R.id.popularShopsRecyclerView)
        val newShopsRecyclerView: RecyclerView =
            localView.findViewById(R.id.newShopsRecyclerView)

        appViewModel.fetchRecentShops()
        appViewModel.fetchPopularShops()
        appViewModel.fetchNewShops()

        recentShopsRecyclerView.layoutManager = recentShopsHorizontalLayout
        popularShopsRecyclerView.layoutManager = popularShopsHorizontalLayout
        newShopsRecyclerView.layoutManager = newShopsHorizontalLayout

        recentShopsAdapter =
            ShopDisplayAdapter(
                appViewModel.recentShopsList.value!!,
                object :
                    ShopDisplayAdapter.ButtonListener {
                    override fun onButtonClick(position: Int) {
                        Log.d(TAG, "Position is $position")
                        Log.d(
                            TAG,
                            "Option selected is ${recentShopsAdapter.getItem(position).shopID}"
                        )
                        appViewModel.selectedShop =
                            recentShopsAdapter.getItem(position)

                        localView.findNavController()
                            .navigate(R.id.action_homeFragmentUser_to_userShopInfo)

                    }
                })

        popularShopsAdapter =
            ShopDisplayAdapter(
                appViewModel.popularShopsList.value!!,
                object :
                    ShopDisplayAdapter.ButtonListener {
                    override fun onButtonClick(position: Int) {
                        Log.d(TAG, "Position is $position")
                        Log.d(
                            TAG,
                            "Option selected is ${appViewModel.popularShopsList.value!![position].shopID}"
                        )
                        appViewModel.selectedShop =
                            appViewModel.popularShopsList.value!![position]

                        localView.findNavController()
                            .navigate(R.id.action_homeFragmentUser_to_userShopInfo)

                    }
                }
            )

        newShopsAdapter =
            ShopDisplayAdapter(
                appViewModel.newShopsList.value!!,
                object :
                    ShopDisplayAdapter.ButtonListener {
                    override fun onButtonClick(position: Int) {
                        Log.d(TAG, "Position is $position")
                        Log.d(
                            TAG,
                            "Option selected is ${appViewModel.newShopsList.value!![position].shopID}"
                        )
                        appViewModel.selectedShop =
                            appViewModel.newShopsList.value!![position]

                        localView.findNavController()
                            .navigate(R.id.action_homeFragmentUser_to_userShopInfo)

                    }
                }
            )

        val recentShopsObserver = Observer<ArrayList<ShopDataModel>> { shopList ->
            run {
                Log.d(TAG, "recent shops added :$shopList")
                recentShopsAdapter.updateChats(shopList)
//                if(shopList.isEmpty()){
//                    recentShopsLayout.visibility = View.GONE
//                }
//                else{
//                    recentShopsLayout.visibility = View.VISIBLE
//                }
            }
        }
        val popularShopsObserver = Observer<ArrayList<ShopDataModel>> { shopList ->
            run {
                Log.d(TAG, "popular shops added :$shopList")
                popularShopsAdapter.updateChats(shopList)
//                if(shopList.isEmpty()){
//                    popularShopsLayout.visibility = View.GONE
//                }else{
//                    popularShopsLayout.visibility = View.VISIBLE
//                }
            }
        }

        val newShopsObserver = Observer<ArrayList<ShopDataModel>> { shopList ->
            run {
                Log.d(TAG, "new shops added :$shopList")
                newShopsAdapter.updateChats(shopList)
//                if(shopList.isEmpty()){
//                    popularShopsLayout.visibility = View.GONE
//                }else{
//                    popularShopsLayout.visibility = View.VISIBLE
//                }
            }
        }

        appViewModel.recentShopsList.observe(viewLifecycleOwner, recentShopsObserver)
        appViewModel.popularShopsList.observe(viewLifecycleOwner, popularShopsObserver)
        appViewModel.newShopsList.observe(viewLifecycleOwner, newShopsObserver)


        recentShopsRecyclerView.adapter = recentShopsAdapter
        popularShopsRecyclerView.adapter = popularShopsAdapter
        newShopsRecyclerView.adapter = newShopsAdapter

        return localView
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
                recentShopsAdapter.filter!!.filter(newText)
                popularShopsAdapter.filter!!.filter(newText)
                newShopsAdapter.filter!!.filter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, menuInflater)
    }


}