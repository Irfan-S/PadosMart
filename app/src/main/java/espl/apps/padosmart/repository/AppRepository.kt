package espl.apps.padosmart.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.utils.END_USER
import espl.apps.padosmart.utils.SHOP_USER

class AppRepository(val context: Context) {

    private val TAG = "AppRepository"
    private lateinit var firebaseDatabaseReference: DatabaseReference
    private var authRepository: AuthRepository = AuthRepository(context)
    private var user: FirebaseUser?
    var userType: Int? = null

    var fireStoreRepository: FirestoreRepository

    init {
        authRepository.getFirebaseUserType(object : AuthRepository.AuthDataInterface {
            override fun onAuthCallback(response: Long) {
                when (response) {
                    SHOP_USER.toLong() -> {
                        firebaseDatabaseReference =
                            Firebase.database.getReference(context.getString(R.string.firebase_shop_data_node))
                        userType = SHOP_USER
                    }
                    END_USER.toLong() -> {
                        firebaseDatabaseReference =
                            Firebase.database.getReference(context.getString(R.string.firebase_user_data_node))
                        userType = END_USER
                    }

                }
            }
        })
        fireStoreRepository = FirestoreRepository(context)
        user = FirebaseAuth.getInstance().currentUser
    }



    //TODO function that attaches to chat bucket, for live order editing by both parties before deleting.

    fun addOrderToShopNode(order: OrderDataModel, onOrderAdded: FirestoreRepository.OnOrderAdded) {
        //TODO assert all order field are not null
        fireStoreRepository.addOrderToFirestore(order, object : FirestoreRepository.OnOrderAdded {
            //After adding data into firestore, fetch its ID and assign into shop.
            override fun onSuccess(boolean: Boolean) {
                if (boolean) {
                    firebaseDatabaseReference.child(context.getString(R.string.firebase_shop_orderhistory))
                        .child(order.orderID!!).setValue(order)
                        .addOnSuccessListener {
                            onOrderAdded.onSuccess(true)
                        }.addOnFailureListener {
                            onOrderAdded.onSuccess(false)
                        }
                }
            }
        })
    }

    /**
     * Fetches all shops in the firestore database
     */
    fun fetchShops(onShopsFetched: FirestoreRepository.OnShopsFetched) {
        fireStoreRepository.fireStoreDB.collection(context.getString(R.string.firestore_shops))
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Fetching all shop data")
                val shopsList: ArrayList<ShopDataModel> = ArrayList()
                for (document in documents) {
                    shopsList.add(document.toObject<ShopDataModel>())
                    Log.d(TAG, shopsList.toString())
                }
                Log.d(TAG, "Length: ${shopsList.size}")
                onShopsFetched.onSuccess(shopsList)
            }.addOnFailureListener {
                onShopsFetched.onSuccess(ArrayList())
            }
    }

    //TODO implement order features

    fun addOrderToUserNode(order: OrderDataModel, onOrderAdded: FirestoreRepository.OnOrderAdded) {
        fireStoreRepository.addOrderToFirestore(order, object : FirestoreRepository.OnOrderAdded {
            //After adding data into firestore, fetch its ID and assign into shop.
            override fun onSuccess(boolean: Boolean) {
                if (boolean) {
                    firebaseDatabaseReference.child(context.getString(R.string.firebase_user_orderhistory))
                        .child(order.orderID!!).setValue(order)
                        .addOnSuccessListener {
                            onOrderAdded.onSuccess(true)
                        }.addOnFailureListener {
                            onOrderAdded.onSuccess(false)
                        }
                }
            }
        })
    }

}