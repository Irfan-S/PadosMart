package espl.apps.padosmart.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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

    private val fireStoreDB: FirebaseFirestore

    private val mAuth: FirebaseAuth

    private var user: FirebaseUser?

    private val firebaseStorage: FirebaseStorage


    init {
        fireStoreDB = Firebase.firestore
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        firebaseStorage = Firebase.storage
    }

    fun uploadShopDetails(shopDataModel: ShopDataModel, callback: OnAuthFirestoreCallback) {
        fireStoreDB.collection(context.getString(R.string.firestore_shops)).add(shopDataModel)
            .addOnCompleteListener {
                callback.onUploadSuccessful(it.result?.id)
            }.addOnFailureListener {
            callback.onUploadSuccessful(null)
        }
    }


    //TODO allow orders to be fed into the parent calls as well?

    fun addOrder(order: OrderDataModel, onOrderAdded: OnOrderAdded) {
        val id = fireStoreDB.collection(context.getString(R.string.firestore_orders)).document().id
        order.orderID = id
        fireStoreDB.collection(context.getString(R.string.firestore_orders)).add(order)
            .addOnSuccessListener {
                onOrderAdded.onSuccess(true)
            }.addOnFailureListener {
            onOrderAdded.onSuccess(false)
        }
    }

    fun fetchAllOrders(queryID: String, queryArg: String, onOrdersFetched: OnOrdersFetched) {
        val resp = ArrayList<OrderDataModel>()
        fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .whereEqualTo(queryArg, queryID).get().addOnSuccessListener { documents ->
            for (document in documents) {
                resp.add(document.toObject() as OrderDataModel)
            }
            onOrdersFetched.onSuccess(resp)
        }.addOnFailureListener {
            onOrdersFetched.onSuccess(resp)
        }
    }

    interface OnAuthFirestoreCallback {
        fun onUploadSuccessful(id: String?)
    }

    interface OnOrderAdded {
        fun onSuccess(boolean: Boolean)
    }

    interface OnOrdersFetched {
        fun onSuccess(orderList: ArrayList<OrderDataModel>)
    }

}