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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.BuildConfig
import espl.apps.padosmart.R
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.utils.*
import espl.apps.padosmart.viewmodels.AppViewModel

class LoginBase : AppCompatActivity() {

    private val TAG = "LoginBase"


    lateinit var appViewModel: AppViewModel

    private var foregroundOnlyLocationServiceBound = false


    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val respShop = intent.getParcelableExtra<ShopDataModel>("shopData")
        val respUser = intent.getParcelableExtra<UserDataModel>("userData")

        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)

        locationBroadcastReceiver = LocationBroadcastReceiver()
        setContentView(R.layout.base_auth)

        when (intent.getIntExtra(getString(R.string.intent_userType), AUTH_ACCESS_FAILED)) {
            END_USER -> {
                val locIntent = Intent(this, UserBase::class.java)
                locIntent.putExtra("userData", respUser)
                startActivity(locIntent)
                finish()
            }
            SHOP_USER -> {
                val locIntent = Intent(this, UserBase::class.java)
                locIntent.putExtra("shopData", respShop)
                startActivity(locIntent)
                finish()
            }
            AUTH_ACCESS_FAILED -> {
                Log.d(TAG, "In nav base")
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

        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "User base being destroyed")
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