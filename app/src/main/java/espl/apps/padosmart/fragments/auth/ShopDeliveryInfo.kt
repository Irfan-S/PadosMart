package espl.apps.padosmart.fragments.auth

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.R
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.viewmodels.AuthViewModel
import java.util.concurrent.TimeUnit

class ShopDeliveryInfo : Fragment(), View.OnClickListener {

    val TAG = "SignupShopDeliveryInfo"

    lateinit var authViewModel: AuthViewModel

    lateinit var localView: View
    lateinit var startTimePicker: TimePicker
    lateinit var endTimePicker: TimePicker
    lateinit var shopCostsEditText: EditText
    lateinit var eulaCheckBox: CheckBox
    lateinit var submitDetailsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        localView =
            inflater.inflate(R.layout.fragment_signup_shop_delivery_info, container, false) as View

        authViewModel = activity?.let { ViewModelProvider(it).get(AuthViewModel::class.java) }!!
        startTimePicker = localView.findViewById(R.id.deliveryStartTimePicker)
        endTimePicker = localView.findViewById(R.id.deliveryEndTimePicker)
        eulaCheckBox = localView.findViewById(R.id.EULAcheckBox)
        shopCostsEditText = localView.findViewById(R.id.deliveryChargesEditText)
        submitDetailsButton = localView.findViewById(R.id.submitDetailsButton)
        submitDetailsButton.setOnClickListener(this)


        return localView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.submitDetailsButton -> {
                if (eulaCheckBox.isChecked) {
                    var startTimeInMillis: Long = 0
                    var endTimeInMillis: Long = 0
                    if (Build.VERSION.SDK_INT >= 23) {
                        var hour = startTimePicker.hour
                        var minute = startTimePicker.minute
                        startTimeInMillis =
                            TimeUnit.HOURS.toMillis(hour.toLong()) + TimeUnit.MINUTES.toMillis(
                                minute.toLong()
                            )
                    } else {
                        var hour = startTimePicker.currentHour
                        var minute = startTimePicker.currentMinute
                        startTimeInMillis =
                            TimeUnit.HOURS.toMillis(hour.toLong()) + TimeUnit.MINUTES.toMillis(
                                minute.toLong()
                            )
                    }
                    if (Build.VERSION.SDK_INT >= 23) {
                        var hour = endTimePicker.hour
                        var minute = endTimePicker.minute
                        endTimeInMillis =
                            TimeUnit.HOURS.toMillis(hour.toLong()) + TimeUnit.MINUTES.toMillis(
                                minute.toLong()
                            )
                    } else {
                        var hour = endTimePicker.currentHour
                        var minute = endTimePicker.currentMinute
                        endTimeInMillis =
                            TimeUnit.HOURS.toMillis(hour.toLong()) + TimeUnit.MINUTES.toMillis(
                                minute.toLong()
                            )
                    }
                    authViewModel.shopDataModel.shopDeliveryStart = startTimeInMillis
                    authViewModel.shopDataModel.shopDeliveryEnd = endTimeInMillis
                    //TODO verfiy amount and time
                    authViewModel.shopDataModel.shopDeliveryCosts =
                        shopCostsEditText.text.toString().toDouble()
                    authViewModel.authRepository.createShopDataObject(
                        authViewModel.shopDataModel,
                        object : AuthRepository.UserDataInterface {
                            override fun onUploadCallback(success: Boolean) {
                                if (success) {
                                    authViewModel.authRepository.createShopUserAuthObject()
                                    Snackbar.make(
                                        requireActivity().findViewById(
                                            android.R.id.content
                                        ),
                                        "Please wait for admin verification",
                                        Snackbar.LENGTH_LONG
                                    ).show()
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

                }
            }
        }
    }

}