package espl.apps.padosmart.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.StorageReference
import espl.apps.padosmart.R
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.viewmodels.AppViewModel

class ShopIdentificationInfo : Fragment(), View.OnClickListener {

    val TAG = "SignupShopIDInfo"

    lateinit var localView: View

    lateinit var appViewModel: AppViewModel

    var shopImage: Boolean = false

    var allDetailsValid = true

    lateinit var profileDisplaySelector: ImageView
    lateinit var proofImageSelector: ImageView

    lateinit var continueButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        localView =
            inflater.inflate(
                R.layout.fragment_signup_shop_identification_info,
                container,
                false
            ) as View


        profileDisplaySelector = localView.findViewById(R.id.imageDisplaySelector)
        proofImageSelector = localView.findViewById(R.id.documentDisplaySelector)
        continueButton = localView.findViewById(R.id.continueButton)

        proofImageSelector.setOnClickListener(this)
        profileDisplaySelector.setOnClickListener(this)
        continueButton.setOnClickListener(this)

        appViewModel = activity?.let { ViewModelProvider(it).get(AppViewModel::class.java) }!!

        return localView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.documentDisplaySelector -> {
                shopImage = false
                ImagePicker.create(this) // Activity or Fragment
                    .start()
            }
            R.id.imageDisplaySelector -> {
                shopImage = true
                ImagePicker.create(this) // Activity or Fragment
                    .start()
            }
            R.id.continueButton -> {

                if (allDetailsValid) {
                    localView.findNavController()
                        .navigate(R.id.action_shopIDInfo_to_shopDeliveryInfo)
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(
                            android.R.id.content
                        ),
                        "Unable to proceed, please try again later",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images

            val images: List<Image> = ImagePicker.getImages(data)

            if (shopImage) {
                appViewModel.authRepository.uploadShopImages(
                    images,
                    object : AuthRepository.ShopImgURIInterface {
                        override fun onUploadCallback(
                            reference: StorageReference?,
                            success: Boolean
                        ) {
                            if (success) {
                                reference!!.downloadUrl.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        allDetailsValid = true
                                        val downloadUri = it.result
                                        appViewModel.shopData.shopImageURL =
                                            downloadUri.toString()
                                        Glide.with(requireContext()).load(images[0].uri)
                                            .into(profileDisplaySelector)
                                    } else {
                                        allDetailsValid = false
                                        Snackbar.make(
                                            requireActivity().findViewById(
                                                android.R.id.content
                                            ),
                                            "Unable to upload image",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        // Handle failures
                                        // ...
                                    }
                                }
                            } else {
                                allDetailsValid = false
                                Snackbar.make(
                                    requireActivity().findViewById(
                                        android.R.id.content
                                    ),
                                    "Unable to connect to servers...",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    })

            } else {
                appViewModel.authRepository.uploadAuthImages(
                    images,
                    object : AuthRepository.ShopAuthURIInterface {
                        override fun onUploadCallback(
                            reference: StorageReference?,
                            success: Boolean
                        ) {
                            if (success) {
                                reference!!.downloadUrl.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        allDetailsValid = true
                                        val downloadUri = it.result
                                        appViewModel.shopData.shopVerificationImageURL =
                                            downloadUri.toString()
                                        Glide.with(requireContext()).load(images[0].uri)
                                            .into(proofImageSelector)
                                    } else {
                                        allDetailsValid = false
                                        Snackbar.make(
                                            requireActivity().findViewById(
                                                android.R.id.content
                                            ),
                                            "Unable to upload image",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        // Handle failures
                                        // ...
                                    }
                                }
                            } else {
                                allDetailsValid = false
                                Snackbar.make(
                                    requireActivity().findViewById(
                                        android.R.id.content
                                    ),
                                    "Unable to connect to servers...",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    })
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}