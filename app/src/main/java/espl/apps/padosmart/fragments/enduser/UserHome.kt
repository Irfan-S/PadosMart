package espl.apps.padosmart.fragments.enduser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.ShopDisplayAdapter
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.viewmodels.AppViewModel


class UserHome : Fragment() {

    val TAG = "UserHome"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)


        val view =
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


        val recentShopsLayout: LinearLayout = view.findViewById(R.id.recentShopsLayout)
        val popularShopsLayout: LinearLayout = view.findViewById(R.id.popularShopsLayout)


        val recentShopsRecyclerView: RecyclerView =
            view.findViewById(R.id.recentsShopsRecyclerView)
        val popularShopsRecyclerView: RecyclerView =
            view.findViewById(R.id.popularShopsRecyclerView)

        appViewModel.fetchRecentShops()
        appViewModel.fetchPopularShops()

        recentShopsRecyclerView.layoutManager = recentShopsHorizontalLayout
        popularShopsRecyclerView.layoutManager = popularShopsHorizontalLayout

        val recentShopsAdapter =
            ShopDisplayAdapter(
                appViewModel.recentShopsList.value!!,
                object :
                    ShopDisplayAdapter.ButtonListener {
                    override fun onButtonClick(position: Int) {
                        Log.d(TAG, "Position is $position")
                        Log.d(
                            TAG,
                            "Option selected is ${appViewModel.recentShopsList.value!![position].shopID}"
                        )
                        appViewModel.selectedShop =
                            appViewModel.recentShopsList.value!![position]

                        view.findNavController()
                            .navigate(R.id.action_homeFragmentUser_to_userShopInfo)

                    }
                })

        val popularShopsAdapter =
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

                        view.findNavController()
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

        appViewModel.recentShopsList.observe(viewLifecycleOwner, recentShopsObserver)
        appViewModel.popularShopsList.observe(viewLifecycleOwner, popularShopsObserver)



        recentShopsRecyclerView.adapter = recentShopsAdapter
        popularShopsRecyclerView.adapter = popularShopsAdapter

        return view
    }
//    override fun onMenuItemClick(item: MenuItem?): Boolean {
//        when(item!!.itemId){
//            R.id.search -> {
//                val searchView:SearchView = item as SearchView
//                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                    override fun onQueryTextSubmit(query: String?): Boolean {
//                        searchView.clearFocus()
//                        /*   if(list.contains(query)){
//                    adapter.getFilter().filter(query);
//                }else{
//                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
//                }*/     return false
//                    }
//
//                    override fun onQueryTextChange(newText: String?): Boolean {
//                        Log.d(TAG,"Entering text in searchbar")
//                        //adapter.getFilter().filter(newText)
//                        return false
//                    }
//                })
//                return true
//            }
//            else -> return false
//        }
//    }
}