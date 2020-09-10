package espl.apps.padosmart.fragments.auth

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.AuthBase
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.viewmodels.AuthViewModel

class User: Fragment(), View.OnClickListener {

    val TAG = "UserAuthFragment"



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

    lateinit var localView: View

    //Declare buttons
    lateinit var submitDetailsButton: Button
    private lateinit var locationButton: Button
    lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        localView =
            inflater.inflate(R.layout.fragment_signup_user, container, false) as View
        authViewModel =
            activity?.let { ViewModelProvider(it).get(AuthViewModel::class.java) }!!

        userData = authViewModel.userData

        locationButton = localView.findViewById(R.id.locationButton)

        userCityField = localView.findViewById(R.id.editTextCity)
        userStateField = localView.findViewById(R.id.editTextState)
        userCountryField = localView.findViewById(R.id.editTextCountry)
        userPinCodeField = localView.findViewById(R.id.editTextPin)


        locationButton.setOnClickListener(this)

        val serviceObserver =
            Observer<Address> { newStatus ->
                run {
                    if (newStatus != null) {
                        fillDetails(newStatus)
                    }
                }
            }

        val buttonObserver =
            Observer<Boolean> { newStatus ->
                run {
                    if (newStatus) {
                        locationButton.text = "Fetching..."
                    } else {
                        locationButton.text = "Find me"
                    }
                }
            }

        authViewModel.address.observe(viewLifecycleOwner, serviceObserver)
        authViewModel.isAddressFetchInProgress.observe(viewLifecycleOwner, buttonObserver)

        userNameField = localView.findViewById(R.id.shopNameField)
        userPhoneTextView = localView.findViewById(R.id.userPhoneTextView)
        userEmailField = localView.findViewById(R.id.shopEmailField)
        userAddressField = localView.findViewById(R.id.shopAddressField)

        userPhoneTextView.text = userData.phone

        submitDetailsButton = localView.findViewById(R.id.submitDetailsButton)
        submitDetailsButton.setOnClickListener(this)
        return localView
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
                authViewModel.locationService!!.checkGpsStatus()
                if ((activity as AuthBase).foregroundPermissionApproved()) {
                    authViewModel.locationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    (activity as AuthBase).requestForegroundPermissions()
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

                    authViewModel.authRepository.getFirebaseUser()!!
                        .updateEmail(userData.email.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User email address updated.")
                                authViewModel.authRepository.getFirebaseUser()!!
                                    .sendEmailVerification()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(TAG, "Email sent.")
                                            Snackbar.make(
                                                requireActivity().findViewById(android.R.id.content),
                                                "Please verify your email account to proceed",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                            authViewModel.authRepository.createEndUserDataObject(
                                                userData,
                                                object : AuthRepository.UserDataInterface {
                                                    override fun onUploadCallback(success: Boolean) {
                                                        if (success) {
                                                            authViewModel.authRepository.createEndUserAuthObject()
                                                            localView.findNavController()
                                                                .navigate(R.id.login)
                                                        } else {
                                                            Snackbar.make(
                                                                requireActivity().findViewById(
                                                                    android.R.id.content
                                                                ),
                                                                "Unable to sign you up at this time",
                                                                Snackbar.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                                })
                                        } else {
                                            Log.d(TAG, "Failure : ${task.result.toString()}")
                                            Snackbar.make(
                                                requireActivity().findViewById(android.R.id.content),
                                                "Email address does not exist",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                            userEmailField.error = "Invalid email"
                                        }
                                    }
                            } else {
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
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