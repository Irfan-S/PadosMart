package espl.apps.padosmart

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.utils.getAddress
import espl.apps.padosmart.utils.toText

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class UserSignupActivity : AppCompatActivity(), View.OnClickListener {


    val TAG = "signupActivity"

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var locationService: LocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver





    lateinit var authRepository: AuthRepository

    //Declare editFields
    lateinit var userNameField: EditText
    lateinit var userPhoneTextView: TextView
    lateinit var userEmailField: EditText
    lateinit var userAddressField: EditText
    lateinit var userCityField:EditText
    lateinit var userStateField:EditText
    lateinit var userCountryField:EditText
    lateinit var userPinCodeField:EditText

    lateinit var userData: UserDataModel


    //Declare buttons
    lateinit var submitDetailsButton: Button
    private lateinit var locationButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        authRepository = AuthRepository(applicationContext)

        userData = intent.getParcelableExtra(getString(R.string.userDataParcel)) as UserDataModel

        locationBroadcastReceiver = LocationBroadcastReceiver()


        locationButton = findViewById(R.id.locationButton)

        userCityField = findViewById(R.id.editTextCity)
        userStateField = findViewById(R.id.editTextState)
        userCountryField = findViewById(R.id.editTextCountry)
        userPinCodeField = findViewById(R.id.editTextPin)


        locationButton.setOnClickListener(this)


        userNameField = findViewById(R.id.userNameField)
        userPhoneTextView = findViewById(R.id.userPhoneTextView)
        userEmailField = findViewById(R.id.userEmailField)
        userAddressField = findViewById(R.id.userAddressField)

        userPhoneTextView.text = userData.phone

        submitDetailsButton = findViewById(R.id.submitDetailsButton)
        submitDetailsButton.setOnClickListener(this)


    }

    private fun areUserDetailsValid(): Boolean {
        var flag = true
        if (TextUtils.isEmpty(userNameField.text)) {
            flag = false
            userNameField.error = "Invalid name"
        }
        if (TextUtils.isEmpty(userEmailField.text)) {
            flag = false
            userEmailField.error = "Email cannot be empty"
        }
        if (TextUtils.isEmpty(userAddressField.text)) {
            flag = false
            userAddressField.error = "Invalid address"
        }

        return flag
    }


    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.locationButton -> {
                locationService!!.checkGpsStatus()
                if (foregroundPermissionApproved()) {
                    locationButton.text = "Fetching..."
                    locationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }

            }
            R.id.submitDetailsButton ->{
                if (areUserDetailsValid()) {

                    //TODO verifying user data
                    userData.name = userNameField.text.toString()
                    userData.address = userAddressField.text.toString()
                    userData.email = userEmailField.text.toString()
                    userData.city = userCityField.text.toString()
                    userData.state = userStateField.text.toString()
                    userData.country = userCountryField.text.toString()
                    userData.pinCode= userPinCodeField.text.toString()

                    authRepository.getFirebaseUser()!!.updateEmail(userData.email.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User email address updated.")
                                authRepository.getFirebaseUser()!!.sendEmailVerification()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(TAG, "Email sent.")
                                            Snackbar.make(
                                                findViewById(android.R.id.content),
                                                "Please verify your email account to proceed",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                            authRepository.createEndUserDataObject(userData,
                                                object : AuthRepository.UserDataInterface {
                                                    override fun onUploadCallback(success: Boolean) {
                                                        if (success) {
                                                            authRepository.createEndUserAuthObject()
                                                            val intent = Intent(
                                                                applicationContext,
                                                                Login::class.java
                                                            )
                                                            startActivity(intent)
                                                            finish()
                                                        } else {
                                                            Snackbar.make(
                                                                findViewById(android.R.id.content),
                                                                "Unable to sign you up at this time",
                                                                Snackbar.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                                })
                                        } else {
                                            Log.d(TAG, "Failure : ${task.result.toString()}")
                                            Snackbar.make(
                                                findViewById(android.R.id.content),
                                                "Email address does not exist",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                            userEmailField.error = "Invalid email"
                                        }
                                    }
                            } else {
                                Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Email address does not exist",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                userEmailField.error = "Invalid email"
                            }
                        }

                }
            }
        }
    }




    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
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
                LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
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


    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
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
                    locationService?.subscribeToLocationUpdates()

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


    private fun fillDetails(address: Address) {
        userAddressField.setText(address.getAddressLine(0))
        userPinCodeField.setText(address.postalCode)
        userCityField.setText(address.locality)
        userStateField.setText(address.adminArea)
        userCountryField.setText(address.countryName)
    }

    /**
     * Receiver for location broadcasts from [LocationService].
     */
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                LocationService.EXTRA_LOCATION
            )

            if (location != null) {
                locationButton.text = "Fetch"
                fillDetails(location.getAddress(applicationContext,location.latitude,location.longitude))
            }
        }
    }



}