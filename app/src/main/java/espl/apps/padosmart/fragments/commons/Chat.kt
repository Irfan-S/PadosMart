package espl.apps.padosmart.fragments.commons

import android.content.Intent
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.StorageReference
import espl.apps.padosmart.R
import espl.apps.padosmart.adapters.ChatAdapter
import espl.apps.padosmart.bases.UserBase
import espl.apps.padosmart.models.ChatDataModel
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.repository.AuthRepository
import espl.apps.padosmart.repository.FirestoreRepository
import espl.apps.padosmart.utils.*
import espl.apps.padosmart.viewmodels.AppViewModel


class Chat : Fragment(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    val TAG = "ChatFragment"


    private lateinit var localView: View

    lateinit var toolBar: Toolbar
    lateinit var appViewModel: AppViewModel
    lateinit var deliveryAddress: String
    lateinit var sendMessageButton: ImageButton
    lateinit var uploadImageButton: ImageButton
    lateinit var imagePreview: ImageView
    lateinit var messageEditText: EditText

    var imgURI: Uri? = null

    var listenerRegistration: ListenerRegistration? = null

    lateinit var chatAdapter: ChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var address: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        localView =
            inflater.inflate(R.layout.fragment_commons_chatscreen, container, false) as View

        //TODO create intent that fetches data of the caller, whether user or shop

        toolBar = localView.findViewById<MaterialToolbar>(R.id.chatAppBar)


        messageEditText = localView.findViewById(R.id.edittext_chatbox)
        sendMessageButton = localView.findViewById(R.id.button_chatbox_send)
        uploadImageButton = localView.findViewById(R.id.button_choose_image)
        imagePreview = localView.findViewById(R.id.selectedImagePreview)

        sendMessageButton.setOnClickListener(this)
        uploadImageButton.setOnClickListener(this)



        linearLayoutManager = LinearLayoutManager(requireContext())
        val chatRecyclerView: RecyclerView = localView.findViewById(R.id.reyclerview_message_list)
        chatRecyclerView.layoutManager = linearLayoutManager


        appViewModel = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)



        Log.d(TAG, "Order data: ${appViewModel.selectedOrder}")



        if (appViewModel.appRepository.userType == END_USER) {
            //Shop passed in when tile selected, so cannot be null
            chatAdapter =
                ChatAdapter(appViewModel.selectedOrder!!.customerName!!, appViewModel.chats.value!!)

            toolBar.title = appViewModel.selectedShop!!.shopName
            val orderObserver = Observer<Int> { orderStatus ->
                run {
                    showMenuOptions(orderStatus)
                }
            }

            appViewModel.orderStatus.observe(viewLifecycleOwner, orderObserver)

            deliveryAddress = appViewModel.userData.address!!




            val addressObserver = Observer<Address> { newStatus ->
                run {
                    if (newStatus != null) {
                        Log.d(TAG, "New address updated: $newStatus")
                        deliveryAddress = newStatus.getAddressLine(0)
                        appViewModel.fireStoreRepository.updateOrderDetails(
                            appViewModel.orderID!!,
                            "deliveryAddress",
                            deliveryAddress,
                            object : FirestoreRepository.OnOrderUpdated {
                                override fun onSuccess(success: Boolean) {
                                    if (success) {
                                        Snackbar.make(
                                            requireActivity().findViewById(android.R.id.content),
                                            "Location updated",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Snackbar.make(
                                            requireActivity().findViewById(android.R.id.content),
                                            "Unable to update location",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }

                            })


                    }
                }
            }

            appViewModel.address.observe(viewLifecycleOwner, addressObserver)

            //TODO Images not sent properly

        } else {
            chatAdapter = ChatAdapter(
                appViewModel.selectedOrder!!.shopName!!,
                appViewModel.chats.value!!
            )

            val orderObserver = Observer<Int> { orderStatus ->
                run {
                    showMenuOptions(orderStatus)
                }
            }

            appViewModel.orderStatus.observe(viewLifecycleOwner, orderObserver)
            toolBar.title = appViewModel.selectedOrder!!.customerName
        }

        toolBar.setOnMenuItemClickListener(this)
        val orderListener =
            EventListener<DocumentSnapshot> { value, error ->
                val localOrder = value?.toObject<OrderDataModel>()
                Log.d(TAG, "Event detected and data is $localOrder")
                if (localOrder != null) {
                    appViewModel.orderStatus.value = localOrder.orderStatus
                    if (localOrder.orderStatus == ORDER_STATUS_CONFIRMED) {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Order confirmed",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else if (localOrder.orderStatus == ORDER_STATUS_CANCELLED) {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Order cancelled",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    appViewModel.chats.value = localOrder.chats

                }
            }

        listenerRegistration = appViewModel.fireStoreRepository.attachOrderListener(
            appViewModel.orderID!!,
            orderListener
        )

        chatRecyclerView.adapter = chatAdapter

        val chatsObserver = Observer<ArrayList<ChatDataModel>> { orderStatus ->
            run {
                Log.d(TAG, "Chats added :$orderStatus")
                chatAdapter.updateChats(orderStatus)
            }
        }
        appViewModel.chats.observe(viewLifecycleOwner, chatsObserver)

        //TODO complete chat UI and its implementation.

        return localView

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.commons_chat_menu, menu)
    }

    override fun onPause() {
        listenerRegistration?.remove()
        if (appViewModel.appRepository.userType == END_USER) {
            appViewModel.fireStoreRepository.updateOrderDetails(
                appViewModel.orderID!!,
                "customerOnline",
                false,
                object : FirestoreRepository.OnOrderUpdated {
                    override fun onSuccess(success: Boolean) {
                        if (success) {
                            parentFragment?.findNavController()
                                ?.popBackStack()
                        } else {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Unable to connect to servers",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                })
        }

        super.onPause()
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        Log.d(TAG, "Menu item clicked: ${item!!.itemId}")
        return when (item.itemId) {
            R.id.updateLocation -> {
                appViewModel.locationService!!.checkGpsStatus()
                if ((activity as UserBase).foregroundPermissionApproved()) {
                    appViewModel.locationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    (activity as UserBase).requestForegroundPermissions()
                }
                true
            }
            R.id.placeOrder -> {
                appViewModel.fireStoreRepository.updateOrderDetails(
                    appViewModel.orderID!!,
                    "orderStatus",
                    ORDER_STATUS_REQUESTED,
                    object : FirestoreRepository.OnOrderUpdated {

                        override fun onSuccess(success: Boolean) {
                            toolBar.menu.findItem(R.id.placeOrder).isVisible = !success
                        }

                    })
                true
            }
            R.id.confirmOrder -> {
                appViewModel.fireStoreRepository.updateOrderDetails(
                    appViewModel.orderID!!,
                    "orderStatus",
                    ORDER_STATUS_CONFIRMED,
                    object : FirestoreRepository.OnOrderUpdated {

                        override fun onSuccess(success: Boolean) {
                            if (success) {
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    "Order successfully placed",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                toolBar.menu.findItem(R.id.confirmOrder).isVisible = !success
                                toolBar.menu.findItem(R.id.cancelOrder).isVisible = success
                            }
                        }

                    })
                true
            }
            R.id.cancelOrder -> {
                appViewModel.fireStoreRepository.updateOrderDetails(
                    appViewModel.orderID!!,
                    "orderStatus",
                    ORDER_STATUS_CANCELLED,
                    object : FirestoreRepository.OnOrderUpdated {
                        override fun onSuccess(success: Boolean) {
                            toolBar.menu.findItem(R.id.cancelOrder).isVisible = !success
                        }

                    })
                true
            }
            else -> false
        }
    }

    fun showMenuOptions(orderStatus: Int) {
        when (appViewModel.appRepository.userType) {
            END_USER -> {
                when (orderStatus) {
                    ORDER_STATUS_NOT_PLACED -> {
                        toolBar.menu.findItem(R.id.confirmOrder).isVisible = false
                        toolBar.menu.findItem(R.id.updateLocation).isVisible = true
                    }
                    ORDER_STATUS_REQUESTED -> {
                        toolBar.menu.findItem(R.id.confirmOrder).isVisible = true
                    }
                    ORDER_STATUS_CONFIRMED -> {
                        toolBar.menu.findItem(R.id.confirmOrder).isVisible = false
                        toolBar.menu.findItem(R.id.cancelOrder).isVisible = true
                    }
                }
            }
            SHOP_USER -> {
                when (orderStatus) {
                    ORDER_STATUS_NOT_PLACED -> {
                        toolBar.menu.findItem(R.id.placeOrder).isVisible = true
                    }
                    else -> {
                        toolBar.menu.findItem(R.id.placeOrder).isVisible = false
                    }
                }
            }

        }
    }

    fun sendMessage(attachmentURI: Uri?) {
        if (!TextUtils.isEmpty(messageEditText.text) || attachmentURI != null) {

            val newMessage = ChatDataModel(
                senderName = if (appViewModel.appRepository.userType == END_USER) appViewModel.selectedOrder!!.customerName else appViewModel.selectedOrder!!.shopName,
                message = if (messageEditText.text != null) messageEditText.text.toString() else "Image attached",
                time = System.currentTimeMillis(),
                attachmentURI = attachmentURI.toString(),
            )

            appViewModel.chats.value!!.add(newMessage)
            appViewModel.fireStoreRepository.updateOrderDetails(
                orderID = appViewModel.orderID!!,
                orderObject = "chats",
                appViewModel.chats.value!!,
                object : FirestoreRepository.OnOrderUpdated {
                    override fun onSuccess(success: Boolean) {
                        Log.d(TAG, "Message pushed successfully")
                        messageEditText.setText("")
                    }

                }
            )
        }
        //myDatabase.child(username).setValue(textbox.getText().toString());
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.button_choose_image -> {
                ImagePicker.create(this) // Activity or Fragment
                    .start()
            }
            R.id.button_chatbox_send -> {
                sendMessage(imgURI)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images

            val images: List<Image> = ImagePicker.getImages(data)
            appViewModel.authRepository.uploadChatImages(
                images,
                object : AuthRepository.ImgURIInterface {
                    override fun onUploadCallback(
                        reference: StorageReference?,
                        success: Boolean
                    ) {
                        if (success) {
                            reference!!.downloadUrl.addOnCompleteListener {
                                if (it.isSuccessful) {
                                    imgURI = it.result

                                    Glide.with(requireContext()).load(images[0].uri)
                                        .into(imagePreview)
                                } else {
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
        super.onActivityResult(requestCode, resultCode, data)

    }

}
