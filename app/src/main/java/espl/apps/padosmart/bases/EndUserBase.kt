package espl.apps.padosmart.bases

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import espl.apps.padosmart.R

class EndUserBase : AppCompatActivity(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private val TAG = "EndUserBase"

    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_user_activity)
        Log.d("hi", "in end user base")

        val toolbar = findViewById<MaterialToolbar>(R.id.userHomeAppBar)
        toolbar.setNavigationOnClickListener(this)
        toolbar.setOnMenuItemClickListener(this)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        navController = host.navController
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.profileFragmentUser) {
                toolbar.visibility = View.GONE
            } else {
                toolbar.visibility = View.VISIBLE
            }
        }

        setupBottomNavMenu(navController)

    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setupWithNavController(navController)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            R.id.search -> {
                val searchView: SearchView = item as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        searchView.clearFocus()
                        /*   if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                }*/     return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        Log.d(TAG, "Entering text in searchbar")
                        //adapter.getFilter().filter(newText)
                        return false
                    }
                })
                return true
            }
            else -> return false
        }
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "Navigation on click clicked")
        navController.navigate(R.id.profileFragmentUser, null)
    }
}