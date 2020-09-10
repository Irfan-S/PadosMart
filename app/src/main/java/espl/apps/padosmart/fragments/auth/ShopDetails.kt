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
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.AuthBase
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.viewmodels.AuthViewModel

class ShopDetails : Fragment(), View.OnClickListener {

    val TAG = "SignupShopDetails"

    lateinit var localView: View

    lateinit var authViewModel: AuthViewModel

    lateinit var locationButton: Button
    lateinit var continueButton: Button

    //Initialize views
    lateinit var shopNameEditText: EditText
    lateinit var ownerNameEditText: EditText
    lateinit var shopNumberEditText: EditText
    lateinit var shopEmailEditText: EditText
    lateinit var shopAddressEditText: EditText

    lateinit var pinCodeEditText: EditText
    lateinit var cityEditText: EditText
    lateinit var stateEditText: EditText
    lateinit var countryEditText: EditText

    lateinit var genderRadioGroup: RadioGroup
    lateinit var dobEditText: EditText


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        localView =
            inflater.inflate(R.layout.fragment_signup_shop_details, container, false) as View
        authViewModel = activity?.let { ViewModelProvider(it).get(AuthViewModel::class.java) }!!

        shopNameEditText = localView.findViewById(R.id.shopNameField)
        ownerNameEditText = localView.findViewById(R.id.shopOwnerField)
        shopNumberEditText = localView.findViewById(R.id.shopNumberField)
        shopEmailEditText = localView.findViewById(R.id.shopEmailField)
        shopAddressEditText = localView.findViewById(R.id.shopAddressField)

        pinCodeEditText = localView.findViewById(R.id.editTextPin)
        cityEditText = localView.findViewById()


        locationButton = localView.findViewById(R.id.locationButton)
        locationButton.setOnClickListener(this)

        continueButton = localView.findViewById(R.id.continueButton)
        continueButton.setOnClickListener(this)

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
            R.id.continueButton -> {

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