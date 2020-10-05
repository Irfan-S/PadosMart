package espl.apps.padosmart.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.utils.*

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

    /**
     * Fetches all shops in the firestore database
     */
    fun fetchShops(onShopsFetched: OnShopsFetched) {
        fireStoreDB.collection(context.getString(R.string.firestore_shops))
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

    fun runTransaction(
        collection: String,
        shopPublicID: String,
        editNode: String,
        onFirestoreCallback: OnFirestoreCallback
    ) {
        val doc = fireStoreDB.collection(collection).document(shopPublicID)
        fireStoreDB.runTransaction { transaction ->
            val snapshot = transaction.get(doc)

            // Note: this could be done without a transaction
            //       by updating the population using FieldValue.increment()
            val newValue = snapshot.getDouble(editNode)!! + 1
            transaction.update(doc, editNode, newValue)
            null
        }.addOnCompleteListener {
            onFirestoreCallback.onUploadSuccessful(it.isSuccessful)
        }
    }


    fun attachNewChatShopListener(
        shopID: String,
        listener: EventListener<QuerySnapshot>
    ): ListenerRegistration {
        return fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .whereEqualTo(QUERY_ARG_SHOP_ID, shopID)
            .whereEqualTo(QUERY_ARG_CUSTOMER_ONLINE, true)
            .whereEqualTo(QUERY_ARG_ORDER_STATUS, ORDER_STATUS_NOT_PLACED)
            .addSnapshotListener(listener)
    }


    fun fetchRecentShops(userID: String, numOfShops: Long, onShopsFetched: OnShopsFetched) {
        var respArray = ArrayList<ShopDataModel>()
        Log.d(TAG, "Querying recent shops")
        fireStoreDB.collection(NODE_ORDERS)
            .whereEqualTo(QUERY_ARG_CUSTOMER_ID, userID)
            .limit(numOfShops)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(
                    TAG,
                    "Fetching recent shops from repo successful w ${documents.toObjects<OrderDataModel>()}"
                )
                for (document in documents) {
                    var resp = document.toObject<OrderDataModel>()
                    getShopDetails(resp.shopID!!,
                        object : OnShopFetched {
                            override fun onSuccess(shopData: ShopDataModel) {
                                if (!respArray.contains(shopData)) {
                                    respArray.add(shopData)
                                }
                                Log.d(TAG, "Recent shops are $respArray")
                                onShopsFetched.onSuccess(respArray)
                            }

                        })
                }
            }.addOnFailureListener {
                Log.d(TAG, "Failed recent fetching w $it")
                onShopsFetched.onSuccess(respArray)
            }
    }


    fun fetchPopularShops(userCity: String, onShopsFetched: OnShopsFetched, numOfShops: Long) {
        var resp = ArrayList<ShopDataModel>()
        Log.d(TAG, "Querying popular shops")
        fireStoreDB.collection(NODE_SHOPS)
            .whereEqualTo(QUERY_ARG_SHOP_STATUS, SHOP_USER)
            .whereEqualTo(QUERY_ARG_CITY, userCity)
            .orderBy(QUERY_ARG_SHOP_COUNTER)
            .limit(numOfShops)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    //Removing duplicates
                    if (!resp.contains(document.toObject())) {
                        resp.add(document.toObject())
                    }

                }
                Log.d(
                    TAG,
                    "Fetching popular shops from repo successful w ${documents.toObjects<ShopDataModel>()}"
                )
                //resp = documents.toObjects<ShopDataModel>() as ArrayList<ShopDataModel>
                onShopsFetched.onSuccess(resp)
            }.addOnFailureListener {
                Log.d(TAG, "Failed popular fetching w $it")
                onShopsFetched.onSuccess(resp)
            }
    }

    fun fetchNewShops(userCity: String, onShopsFetched: OnShopsFetched, numOfShops: Long) {
        var resp = ArrayList<ShopDataModel>()
        Log.d(TAG, "Querying new shops")
        fireStoreDB.collection(NODE_SHOPS)
            .whereEqualTo(QUERY_ARG_SHOP_STATUS, SHOP_USER)
            .whereEqualTo(QUERY_ARG_CITY, userCity)
            .orderBy(QUERY_ARG_SHOP_CREATION_DATE, Query.Direction.DESCENDING)
            .limit(numOfShops)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    //Removing duplicates
                    if (!resp.contains(document.toObject())) {
                        resp.add(document.toObject())
                    }

                }
                Log.d(
                    TAG,
                    "Fetching new shops from repo successful w ${documents.toObjects<ShopDataModel>()}"
                )
                //resp = documents.toObjects<ShopDataModel>() as ArrayList<ShopDataModel>
                onShopsFetched.onSuccess(resp)
            }.addOnFailureListener {
                Log.d(TAG, "Failed popular fetching w $it")
                onShopsFetched.onSuccess(resp)
            }
    }

    fun getShopDetails(
        shopID: String,
        callback: OnShopFetched
    ) {
        fireStoreDB.collection(NODE_SHOPS).document(shopID).get().addOnSuccessListener {
            if (it != null) {
                callback.onSuccess(it.toObject<ShopDataModel>()!!)
            }
        }
    }

    fun updateShopDetails(
        shopPublicID: String,
        editNode: String,
        value: Any,
        onFirestoreCallback: OnFirestoreCallback
    ) {
        fireStoreDB.collection(context.getString(R.string.firestore_shops)).document(shopPublicID)
            .update(
                editNode, value
            ).addOnCompleteListener {
                onFirestoreCallback.onUploadSuccessful(it.isSuccessful)
            }
    }

    fun signOut() {
        mAuth.signOut()
    }


    fun fetchQueryOrdersFromFirestore(
        node: String,
        queryArg: String,
        onOrdersFetched: OnOrdersFetched,
        limit: Long
    ) {
        val resp = ArrayList<OrderDataModel>()
        fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .whereEqualTo(node, queryArg)
            .limit(limit)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Fetching orders ${documents.toObjects<OrderDataModel>()}")
                for (document in documents) {
                    resp.add(document.toObject() as OrderDataModel)
                }
                onOrdersFetched.onSuccess(resp)
            }.addOnFailureListener {
                onOrdersFetched.onSuccess(resp)
            }
    }


    fun fetchCompoundQueryOrdersFromFirestore(
        queryID1: String,
        queryArg1: String,
        queryID2: String,
        queryArg2: List<Any>,
        onOrdersFetched: OnOrdersFetched,
    ) {
        val resp = ArrayList<OrderDataModel>()
        fireStoreDB.collection(context.getString(R.string.firestore_orders))
            .whereEqualTo(queryID1, queryArg1).whereIn(queryID2, queryArg2)
            .get()
            .addOnSuccessListener { documents ->
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
            .whereEqualTo("customerOnline", true)
            .get()
            .addOnSuccessListener { documents ->
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

    fun uploadShopDetails(shopDataModel: ShopDataModel, callback: OnFirestoreCallback) {
        fireStoreDB.collection(context.getString(R.string.firestore_shops))
            .document(shopDataModel.shopID!!).set(shopDataModel)
            .addOnCompleteListener {
                callback.onUploadSuccessful(true)
            }.addOnFailureListener {
                callback.onUploadSuccessful(false)
            }
    }

    //TODO implement order features


    interface OnFirestoreCallback {
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

    interface OnShopFetched {
        fun onSuccess(shopData: ShopDataModel)
    }

}