package espl.apps.padosmart.commons
//
//import android.Manifest
//import android.content.*
//import android.content.pm.PackageManager
//import android.location.Location
//import android.net.Uri
//import android.os.Bundle
//import android.os.IBinder
//import android.provider.Settings
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.LinearLayout
//import android.widget.RadioGroup
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.lifecycle.ViewModelProvider
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import com.google.android.material.appbar.MaterialToolbar
//import com.google.android.material.snackbar.Snackbar
//import espl.apps.padosmart.BuildConfig
//import espl.apps.padosmart.R
//import espl.apps.padosmart.services.LocationService
//import espl.apps.padosmart.utils.*
//import espl.apps.padosmart.viewmodels.AppViewModel
//
//class ChatActivity : AppCompatActivity() {
//
//    val TAG = "ChatActivity"
//
//    private var foregroundOnlyLocationServiceBound = false
//
//
//    // Listens for location broadcasts from ForegroundOnlyLocationService.
//    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver
//
//    var confirmOrderButton: Button? = null
//    var requestOrderButton: Button? = null
//    var fetchLocationButton: Button? = null
//    var paymentTypeRadioGroup: RadioGroup? = null
//
//
//    private lateinit var locationService: LocationService
//
//    lateinit var address: String
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.fragment_commons_chatscreen)
//        locationBroadcastReceiver = LocationBroadcastReceiver()
//
//        //TODO create intent that fetches data of the caller, whether user or shop
//        val isCustomer = intent.getBooleanExtra("isCustomer", true)
//        val toolBar = findViewById<MaterialToolbar>(R.id.chatAppBar)
//
//        val appViewModel: AppViewModel =
//            ViewModelProvider(this).get(AppViewModel::class.java)
//
//
//        if (isCustomer) {
//            val userLayout = findViewById<LinearLayout>(R.id.userChatFields)
//            userLayout.visibility = View.VISIBLE
//            confirmOrderButton = findViewById(R.id.confirmOrderButton)
//            fetchLocationButton = findViewById(R.id.currentLocationButton)
//            paymentTypeRadioGroup = findViewById(R.id.paymentTypeRadioGroup)
//            toolBar.title =
//        } else {
//            val shopLayout = findViewById<LinearLayout>(R.id.shopChatFields)
//            shopLayout.visibility = View.VISIBLE
//
//        }
//
//
//    }
//
//
//    // Monitors connection to the while-in-use service.
//    private val foregroundOnlyServiceConnection = object : ServiceConnection {
//
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            Log.d(TAG, "Service attached")
//            val binder = service as LocationService.LocalBinder
//            locationService = binder.service
//            foregroundOnlyLocationServiceBound = true
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//            Log.d(TAG, "Service detached")
//            locationService = null
//            foregroundOnlyLocationServiceBound = false
//        }
//    }
//
//
//    override fun onStart() {
//        super.onStart()
//        val serviceIntent = Intent(this, LocationService::class.java)
//        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//            locationBroadcastReceiver,
//            IntentFilter(
//                LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
//            )
//        )
//    }
//
//    override fun onPause() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(
//            locationBroadcastReceiver
//        )
//        super.onPause()
//    }
//
//    override fun onStop() {
//        if (foregroundOnlyLocationServiceBound) {
//            unbindService(foregroundOnlyServiceConnection)
//            foregroundOnlyLocationServiceBound = false
//        }
//
//        super.onStop()
//    }
//
//
//    fun foregroundPermissionApproved(): Boolean {
//        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//    }
//
//    fun requestForegroundPermissions() {
//        val provideRationale = foregroundPermissionApproved()
//
//        // If the user denied a previous request, but didn't check "Don't ask again", provide
//        // additional rationale.
//        if (provideRationale) {
//            Snackbar.make(
//                findViewById(android.R.id.content),
//                //TODO edit text
//                "We need permission pls give",
//                Snackbar.LENGTH_LONG
//            )
//                .setAction("OK") {
//                    // Request permission
//                    ActivityCompat.requestPermissions(
//                        this,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//                    )
//                }
//                .show()
//        } else {
//            Log.d(TAG, "Request foreground only permission")
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        Log.d(TAG, "onRequestPermissionResult")
//
//        when (requestCode) {
//            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
//                grantResults.isEmpty() ->
//                    // If user interaction was interrupted, the permission request
//                    // is cancelled and you receive empty arrays.
//                    Log.d(TAG, "User interaction was cancelled.")
//
//                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
//                    // Permission was granted.
//                    locationService.subscribeToLocationUpdates()
//
//                else -> {
//
//                    //TODO edit the text
//                    Snackbar.make(
//                        findViewById(android.R.id.content),
//                        "Denied",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .setAction("Settings") {
//                            // Build intent that displays the App settings screen.
//                            val intent = Intent()
//                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            val uri = Uri.fromParts(
//                                "package",
//                                BuildConfig.APPLICATION_ID,
//                                null
//                            )
//                            intent.data = uri
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            startActivity(intent)
//                        }
//                        .show()
//                }
//            }
//        }
//    }
//
//    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
//        when (checkedId) {
//            R.id.maleRadioButton -> {
//                authViewModel.shopDataModel.gender = GENDER_MALE
//            }
//            R.id.femaleRadioButton -> {
//                authViewModel.shopDataModel.gender = GENDER_FEMALE
//            }
//            R.id.othersRadioButton -> {
//                authViewModel.shopDataModel.gender = GENDER_OTHERS
//            }
//        }
//    }
//
//
//    /**
//     * Receiver for location broadcasts from [LocationService].
//     */
//    inner class LocationBroadcastReceiver : BroadcastReceiver() {
//
//        override fun onReceive(context: Context, intent: Intent) {
//            val location = intent.getParcelableExtra<Location>(
//                LocationService.EXTRA_LOCATION
//            )
//
//            if (location != null) {
//                val addressObj = location.getAddress(
//                    applicationContext,
//                    location.latitude,
//                    location.longitude
//                )
//                address = addressObj.getAddressLine(0)
//            }
//        }
//    }
//
//}