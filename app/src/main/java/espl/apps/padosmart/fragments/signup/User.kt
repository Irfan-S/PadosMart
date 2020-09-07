package espl.apps.padosmart.fragments.signup

import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.Login
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.SignupBase
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.services.LocationService
import espl.apps.padosmart.viewmodels.SignupViewModel

class User: Fragment(), View.OnClickListener {

    val TAG = "SignupUser"

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var locationService: LocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var locationBroadcastReceiver: SignupBase.LocationBroadcastReceiver


    lateinit var authRepository: AuthRepository

    //Declare editFields
    lateinit var userNameField: EditText
    lateinit var userPhoneTextView: TextView
    lateinit var userEmailField: EditText
    lateinit var userAddressField: EditText
    lateinit var userCityField: EditText
    lateinit var userStateField: EditText
    lateinit var userCountryField: EditText
    lateinit var userPinCodeField: EditText

    lateinit var userData: UserDataModel


    //Declare buttons
    lateinit var submitDetailsButton: Button
    private lateinit var locationButton: Button
    lateinit var signupViewModel: SignupViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_signup_user, container, false) as View
        signupViewModel =
            activity?.let { ViewModelProvider(it).get(SignupViewModel::class.java) }!!
        locationButton = view.findViewById(R.id.locationButton)

        userCityField = view.findViewById(R.id.editTextCity)
        userStateField = view.findViewById(R.id.editTextState)
        userCountryField = view.findViewById(R.id.editTextCountry)
        userPinCodeField = view.findViewById(R.id.editTextPin)


        locationButton.setOnClickListener(this)


        userNameField = view.findViewById(R.id.userNameField)
        userPhoneTextView = view.findViewById(R.id.userPhoneTextView)
        userEmailField = view.findViewById(R.id.userEmailField)
        userAddressField = view.findViewById(R.id.userAddressField)

        userPhoneTextView.text = userData.phone

        submitDetailsButton = view.findViewById(R.id.submitDetailsButton)
        submitDetailsButton.setOnClickListener(this)
        return view
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

    private fun fillDetails(address: Address) {
        userAddressField.setText(address.getAddressLine(0))
        userPinCodeField.setText(address.postalCode)
        userCityField.setText(address.locality)
        userStateField.setText(address.adminArea)
        userCountryField.setText(address.countryName)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.locationButton -> {
                locationService!!.checkGpsStatus()
                if ((activity as SignupBase).foregroundPermissionApproved()) {
                    locationButton.text = "Fetching..."
                    locationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    (activity as SignupBase).requestForegroundPermissions()
                }

            }
            R.id.submitDetailsButton -> {
                if (areUserDetailsValid()) {

                    //TODO verifying user data
                    userData.name = userNameField.text.toString()
                    userData.address = userAddressField.text.toString()
                    userData.email = userEmailField.text.toString()
                    userData.city = userCityField.text.toString()
                    userData.state = userStateField.text.toString()
                    userData.country = userCountryField.text.toString()
                    userData.pinCode = userPinCodeField.text.toString()

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

}