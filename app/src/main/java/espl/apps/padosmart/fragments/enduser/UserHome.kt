package espl.apps.padosmart.fragments.enduser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.ShopDisplayAdapter
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.viewmodels.UserViewModel


class UserHome : Fragment() {

    val TAG = "UserHome"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_home_user, container, false) as View
        val horizontalLayout = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        var shopsList: ArrayList<ShopDataModel>
        val userViewModel: UserViewModel =
            ViewModelProvider(this).get(UserViewModel::class.java)
        val recentStoresRecyclerView: RecyclerView =
            view.findViewById(R.id.recentStoresRecyclerView)
        recentStoresRecyclerView.layoutManager = horizontalLayout

        userViewModel.fireStoreRepository.fetchShops(object : FirestoreRepository.OnShopsFetched {
            override fun onSuccess(shopList: ArrayList<ShopDataModel>) {
                Log.d(TAG, "Length: ${shopList.size}")
                shopsList = shopList
                val adapter =
                    ShopDisplayAdapter(shopsList, object :
                        ShopDisplayAdapter.ButtonListener {
                        override fun onButtonClick(position: Int) {
                            Log.d(TAG, "Position is $position")
                            //TODO animate transition of exercise with sharedwindowtransition(?)
                            Log.d(TAG, "Option selected is ${shopsList[position].shopID}")
                            userViewModel.selectedShop = shopsList[position]

//                    findNavController().navigate(action)
                        }
                    })
                recentStoresRecyclerView.adapter = adapter
            }
        })




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