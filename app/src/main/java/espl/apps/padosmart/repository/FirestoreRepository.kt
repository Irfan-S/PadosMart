package espl.apps.padosmart.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel

class FirestoreRepository(private var context: Context) {


    private val TAG = "FirestoreRepository"

    val fireStoreDB: FirebaseFirestore

    private val mAuth: FirebaseAuth


    private val firebaseStorage: FirebaseStorage


    init {
        fireStoreDB = Firebase.firestore
        mAuth = FirebaseAuth.getInstance()
        firebaseStorage = Firebase.storage
    }

    //TODO allow orders to be fed into the parent calls as well?
    fun addOrderToFirestore(order: OrderDataModel, onOrderAdded: OnOrderAdded) {
        val id = fireStoreDB.collection(context.getString(R.string.firestore_orders)).document().id
        order.orderID = id
        fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .document(order.orderID!!).set(order)
            .addOnSuccessListener {
                onOrderAdded.onSuccess(id, true)
            }.addOnFailureListener {
                onOrderAdded.onSuccess(id, false)
            }
    }

    fun updateOrderDetails(
        orderID: String,
        orderObject: String,
        details: Any,
        onOrderUpdated: OnOrderUpdated
    ) {
        fireStoreDB.collection(context.getString(R.string.firestore_orders)).document(orderID)
            .update(orderObject, details).addOnCompleteListener {
                Log.d(TAG, "Task : ${it.result}")
                onOrderUpdated.onSuccess(it.isSuccessful)
            }
    }

    fun attachOrderListener(
        orderID: String,
        listener: EventListener<DocumentSnapshot>
    ): ListenerRegistration {
        return fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .document(orderID).addSnapshotListener(listener)
    }


    fun fetchRecentShops(numOfShops: Long, onShopsFetched: OnShopsFetched) {
        val resp = ArrayList<ShopDataModel>()
        fireStoreDB.collection(context.getString(R.string.firestore_shops))
            .orderBy("shopCreationDate", Query.Direction.DESCENDING).limit(numOfShops).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    resp.add(document.toObject() as ShopDataModel)
                }
                onShopsFetched.onSuccess(resp)
            }.addOnFailureListener {
                onShopsFetched.onSuccess(resp)
            }
    }


    fun fetchQueryOrdersFromFirestore(
        queryID: String,
        queryArg: String,
        onOrdersFetched: OnOrdersFetched,
        limit: Long
    ) {
        val resp = ArrayList<OrderDataModel>()
        fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .whereEqualTo(queryArg, queryID).limit(limit).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    resp.add(document.toObject() as OrderDataModel)
                }
                onOrdersFetched.onSuccess(resp)
            }.addOnFailureListener {
                onOrdersFetched.onSuccess(resp)
            }
    }

    fun fetchOnlineCustomersFromFirestore(shopID: String, onOrdersFetched: OnOrdersFetched) {
        val resp = ArrayList<OrderDataModel>()
        fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .whereEqualTo("shopPublicID", shopID)
            .whereEqualTo("customerOnline", true).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    resp.add(document.toObject() as OrderDataModel)
                }
                onOrdersFetched.onSuccess(resp)
            }.addOnFailureListener {
                onOrdersFetched.onSuccess(resp)
            }
    }

    //TODO fetch shop details and add to orders list as it is incomplete
    fun fetchShopDetails(shopPublicID: String) {

    }

    fun uploadShopDetails(shopDataModel: ShopDataModel, callback: OnAuthFirestoreCallback) {
        fireStoreDB.collection(context.getString(R.string.firestore_shops))
            .document(shopDataModel.shopPublicID!!).set(shopDataModel)
            .addOnCompleteListener {
                callback.onUploadSuccessful(true)
            }.addOnFailureListener {
                callback.onUploadSuccessful(false)
            }
    }

    //TODO implement order features


    interface OnAuthFirestoreCallback {
        fun onUploadSuccessful(isSuccess: Boolean)
    }

    interface OnOrderAdded {
        fun onSuccess(orderID: String, boolean: Boolean)
    }

    interface OnOrderUpdated {
        fun onSuccess(success: Boolean)
    }

    interface OnOrdersFetched {
        fun onSuccess(orderList: ArrayList<OrderDataModel>)
    }

    interface OnShopsFetched {
        fun onSuccess(shopList: ArrayList<ShopDataModel>)
    }

}