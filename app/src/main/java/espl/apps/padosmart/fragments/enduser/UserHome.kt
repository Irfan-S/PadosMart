package espl.apps.padosmart.fragments.enduser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import espl.apps.padosmart.R


class UserHome : Fragment() {

    val TAG = "UserHome"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_home_user, container, false) as View

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