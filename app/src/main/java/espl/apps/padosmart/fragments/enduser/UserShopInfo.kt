package espl.apps.padosmart.fragments.enduser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.utils.ORDER_STATUS_NOT_PLACED
import espl.apps.padosmart.utils.generateOTP
import espl.apps.padosmart.viewmodels.AppViewModel
import java.util.concurrent.TimeUnit

class UserShopInfo : Fragment() {
    val TAG = "UserShopInfo"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =
            inflater.inflate(R.layout.fragment_shopinfo_user, container, false) as View

        val appViewModel: AppViewModel =
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        val startChatButton = view.findViewById<Button>(R.id.startChatButton)
        val shopImageView = view.findViewById<ImageView>(R.id.shopProfileImageView)
        val shopRatingBar = view.findViewById<RatingBar>(R.id.shopRatingBarStatic)
        val timingsText = view.findViewById<TextView>(R.id.shopTimingsTextView)
        val addressText = view.findViewById<TextView>(R.id.addressTextView)

        Glide.with(requireContext()).load(appViewModel.selectedShop!!.shopImageURL)
            .into(shopImageView)

        shopRatingBar.rating =
            ((appViewModel.selectedShop!!.shopTotalRating!! / appViewModel.selectedShop!!.shopTotalRatingCount!!).toFloat())
        timingsText.text =
            " Timings: ${TimeUnit.MILLISECONDS.toHours(appViewModel.selectedShop!!.shopDeliveryStart!!)} to ${
                TimeUnit.MILLISECONDS.toHours(appViewModel.selectedShop!!.shopDeliveryEnd!!)
            }"
        addressText.text = "Address: ${appViewModel.selectedShop!!.address!!}"


        startChatButton.setOnClickListener {
            val orderModel = OrderDataModel(
                customerName = appViewModel.userData.name!!,
                shopName = appViewModel.selectedShop!!.shopName!!,
                deliveryAddress = appViewModel.userData.address,
                customerID = appViewModel.firebaseUser!!.uid,
                shopPublicID = appViewModel.selectedShop!!.shopPublicID!!,
                customerOnline = true,
                OTP = generateOTP(),
                orderStatus = ORDER_STATUS_NOT_PLACED
            )
            appViewModel.fireStoreRepository.addOrderToFirestore(orderModel,
                object : FirestoreRepository.OnOrderAdded {

                    override fun onSuccess(orderID: String, boolean: Boolean) {
                        if (boolean && orderID.isBlank()) {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Unable to connect to servers, please try again later",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            appViewModel.selectedOrder = orderModel
                            appViewModel.orderID = orderID
                            view.findNavController()
                                .navigate(R.id.action_userShopInfo_to_userChat)

                        }
                    }

                })
        }



        return view
    }
}