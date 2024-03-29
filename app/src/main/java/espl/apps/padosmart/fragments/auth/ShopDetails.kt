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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import espl.apps.padosmart.R
import espl.apps.padosmart.bases.UserBase
import espl.apps.padosmart.utils.GENDER_FEMALE
import espl.apps.padosmart.utils.GENDER_MALE
import espl.apps.padosmart.utils.GENDER_OTHERS
import espl.apps.padosmart.viewmodels.AppViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class ShopDetails : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    val TAG = "SignupShopDetails"

    lateinit var localView: View

    lateinit var appViewModel: AppViewModel

    lateinit var locationButton: Button

    lateinit var continueButton: Button

    //Initialize views
    lateinit var shopNameEditText: EditText
    lateinit var ownerNameEditText: EditText
    lateinit var shopNumberTextView: TextView
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
        appViewModel = activity?.let { ViewModelProvider(it).get(AppViewModel::class.java) }!!

        shopNameEditText = localView.findViewById(R.id.shopNameField)
        ownerNameEditText = localView.findViewById(R.id.shopOwnerField)
        shopNumberTextView = localView.findViewById(R.id.shopNumberTextView)
        shopNumberTextView.text = appViewModel.shopData.phone
        shopEmailEditText = localView.findViewById(R.id.shopEmailField)
        shopAddressEditText = localView.findViewById(R.id.shopAddressField)

        pinCodeEditText = localView.findViewById(R.id.editTextPin)
        cityEditText = localView.findViewById(R.id.editTextCity)
        stateEditText = localView.findViewById(R.id.editTextState)
        countryEditText = localView.findViewById(R.id.editTextCountry)
        dobEditText = localView.findViewById(R.id.editTextDOB)
        genderRadioGroup = localView.findViewById(R.id.genderRadioGroup)
        genderRadioGroup.setOnCheckedChangeListener(this)

        locationButton = localView.findViewById(R.id.locationButton)
        locationButton.setOnClickListener(this)

        continueButton = localView.findViewById(R.id.continueButton)
        continueButton.setOnClickListener(this)

        val serviceObserver =
            Observer<Address> { newStatus ->
                run {
                    if (newStatus != null) {
                        fillDetails(newStatus)
                    }
                }
            }

        appViewModel.address.observe(viewLifecycleOwner, serviceObserver)

        return localView
    }

    private fun areShopDetailsValid(): Boolean {
        val regex = "^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$"
        val pattern: Pattern = Pattern.compile(regex)

        var flag = true
        if (TextUtils.isEmpty(dobEditText.text)) {
            flag = false
            dobEditText.error = "DOB cannot be empty"
        } else {
            val matcher: Matcher = pattern.matcher(dobEditText.text.toString())
            if (!matcher.matches()) {
                flag = false
                dobEditText.error = "Invalid DOB"
            }
        }
        if (TextUtils.isEmpty(shopNameEditText.text)) {
            flag = false
            shopNameEditText.error = "Invalid name"
        }
        if (TextUtils.isEmpty(shopEmailEditText.text)) {
            flag = false
            shopEmailEditText.error = "Email cannot be empty"
        }
        if (TextUtils.isEmpty(shopAddressEditText.text)) {
            flag = false
            shopAddressEditText.error = "Invalid address"
        }
        if (TextUtils.isEmpty(ownerNameEditText.text)) {
            flag = false
            ownerNameEditText.error = "Invalid name"
        }
        return flag
    }


    private fun fillDetails(address: Address) {
        shopAddressEditText.setText(address.getAddressLine(0))
        pinCodeEditText.setText(address.postalCode)
        cityEditText.setText(address.locality)
        stateEditText.setText(address.adminArea)
        countryEditText.setText(address.countryName)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.locationButton -> {
                appViewModel.locationService!!.checkGpsStatus()
                if ((activity as UserBase).foregroundPermissionApproved()) {
                    appViewModel.locationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    (activity as UserBase).requestForegroundPermissions()
                }

            }
            R.id.continueButton -> {
                if (areShopDetailsValid()) {

                    //TODO verifying user data
                    appViewModel.shopData.DOB = dobEditText.text.toString()
                    appViewModel.shopData.shopName = shopNameEditText.text.toString()
                    appViewModel.shopData.ownerName = ownerNameEditText.text.toString()
                    appViewModel.shopData.address = shopAddressEditText.text.toString()
                    appViewModel.shopData.email = shopEmailEditText.text.toString()
                    appViewModel.shopData.city = cityEditText.text.toString()
                    appViewModel.shopData.state = stateEditText.text.toString()
                    appViewModel.shopData.country = countryEditText.text.toString()
                    appViewModel.shopData.pinCode = pinCodeEditText.text.toString()

                    saveGenderData(genderRadioGroup.checkedRadioButtonId)

                    appViewModel.authRepository.getFirebaseUser()!!
                        .updateEmail(appViewModel.shopData.email.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User email address updated.")
                                appViewModel.authRepository.getFirebaseUser()!!
                                    .sendEmailVerification()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(TAG, "Email sent.")
                                            Snackbar.make(
                                                requireActivity().findViewById(android.R.id.content),
                                                "Verification email sent, please check after finishing your signup",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                            localView.findNavController()
                                                .navigate(R.id.action_shopDetails_to_shopIDInfo)
                                        } else {
                                            Log.d(TAG, "Failure : ${task.result.toString()}")
                                            Snackbar.make(
                                                requireActivity().findViewById(android.R.id.content),
                                                "Email address does not exist",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                            shopEmailEditText.error = "Invalid email"
                                        }
                                    }
                            } else {
                                try {
                                    Log.d(TAG, "Error raised: ${task.exception}")
                                    throw task.exception!!
                                } catch (e: FirebaseAuthException) {
                                    if (e is FirebaseAuthUserCollisionException) {
                                        Snackbar.make(
                                            requireActivity().findViewById(android.R.id.content),
                                            "Email address already in use",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        shopEmailEditText.error = "Email exists"
                                    } else {
                                        Snackbar.make(
                                            requireActivity().findViewById(android.R.id.content),
                                            "Email address does not exist",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        shopEmailEditText.error = "Invalid email"
                                    }
                                }

                            }
                        }

                }
            }
        }
    }

    fun saveGenderData(checkedId: Int) {
        when (checkedId) {
            R.id.maleRadioButton -> {
                appViewModel.shopData.gender = GENDER_MALE
            }
            R.id.femaleRadioButton -> {
                appViewModel.shopData.gender = GENDER_FEMALE
            }
            R.id.othersRadioButton -> {
                appViewModel.shopData.gender = GENDER_OTHERS
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        saveGenderData(checkedId)
    }

}