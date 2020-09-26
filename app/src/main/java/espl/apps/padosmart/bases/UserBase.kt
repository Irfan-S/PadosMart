package espl.apps.padosmart.bases

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.BuildConfig
import espl.apps.padosmart.R
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.utils.*
import espl.apps.padosmart.viewmodels.AppViewModel

class UserBase : AppCompatActivity(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private val TAG = "UserBase"

    private var foregroundOnlyLocationServiceBound = false


    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    lateinit var appViewModel: AppViewModel

    lateinit var navController: NavController

    lateinit var toolbar: MaterialToolbar

    var profileNavDirections: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val respShop = intent.getParcelableExtra<ShopDataModel>("shopData")
        val respUser = intent.getParcelableExtra<UserDataModel>("userData")

        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)
        if (respUser != null) {
            appViewModel.userData = respUser
        } else if (respShop != null) {
            appViewModel.shopData = respShop
        }

        locationBroadcastReceiver = LocationBroadcastReceiver()

        when (intent.getIntExtra(getString(R.string.intent_userType), AUTH_ACCESS_FAILED)) {
            END_USER -> {
                setContentView(R.layout.base_user_activity)
                Log.d(TAG, "in end user base")
                profileNavDirections = R.id.profileFragmentUser

                toolbar = findViewById<MaterialToolbar>(R.id.userHomeAppBar)
                toolbar.setNavigationOnClickListener(this)
                toolbar.setOnMenuItemClickListener(this)

                val host: NavHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

                navController = host.navController
                navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
                    if (nd.id == R.id.profileFragmentUser || nd.id == R.id.userChat) {
                        toolbar.visibility = View.GONE
                    } else {
                        toolbar.visibility = View.VISIBLE
                    }
                }

                setupBottomNavMenu(navController)
            }
            SHOP_USER -> {
                setContentView(R.layout.base_shop_activity)
                Log.d(TAG, "in shop base")
                shopStatusSet(status = true)

                toolbar = findViewById<MaterialToolbar>(R.id.shopHomeAppBar)
                toolbar.setNavigationOnClickListener(this)
                toolbar.setOnMenuItemClickListener(this)

                profileNavDirections = R.id.shopProfile

                val host: NavHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

                navController = host.navController
                navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
                    if (nd.id == R.id.shopProfile || nd.id == R.id.shopChat) {
                        toolbar.visibility = View.GONE
                    } else {
                        toolbar.visibility = View.VISIBLE
                    }
                }

                setupBottomNavMenu(navController)
            }
            AUTH_ACCESS_FAILED -> {
                setContentView(R.layout.base_auth)
            }
        }

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
        when (v!!.id) {
            R.id.userHomeAppBar -> {
                Log.d(TAG, "Navigation on click clicked")
                navController.navigate(R.id.profileFragmentUser, null)
            }
            R.id.shopHomeAppBar -> {
                Log.d(TAG, "Navigation on click clicked")
                navController.navigate(R.id.shopProfile, null)
            }
            else -> {
                Log.d(TAG, "Navigation on click clicked")
                navController.navigate(profileNavDirections, null)
            }
        }


    }

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "Service attached")
            val binder = service as LocationService.LocalBinder
            appViewModel.locationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service detached")
            appViewModel.locationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }


    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, LocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(
                LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            locationBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
            shopStatusSet(status = false)
        }

        super.onStop()
    }


    fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                findViewById(android.R.id.content),
                //TODO edit text
                "We need permission pls give",
                Snackbar.LENGTH_LONG
            )
                .setAction("OK") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            appViewModel.isAddressFetchInProgress.value = true
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun shopStatusSet(status: Boolean) {
        appViewModel.fireStoreRepository.updateShopDetails(
            appViewModel.shopData.shopPublicID!!,
            "isOnline",
            status,
            object : FirestoreRepository.OnFirestoreCallback {
                override fun onUploadSuccessful(isSuccess: Boolean) {
                    Log.d(TAG, "Shop status updated")
                }

            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    appViewModel.locationService?.subscribeToLocationUpdates()

                else -> {

                    //TODO edit the text
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Denied",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Settings") {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }


    /**
     * Receiver for location broadcasts from [LocationService].
     */
    inner class LocationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                LocationService.EXTRA_LOCATION
            )

            if (location != null) {
                appViewModel.isAddressFetchInProgress.value = false
                appViewModel.address.value = location.getAddress(
                    applicationContext,
                    location.latitude,
                    location.longitude
                )
            }
        }
    }
}