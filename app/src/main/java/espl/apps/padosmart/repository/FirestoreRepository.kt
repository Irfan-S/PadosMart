package espl.apps.padosmart.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
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

    fun uploadShopDetails(shopDataModel: ShopDataModel, callback: onAuthFirestoreCallback) {
        fireStoreDB.collection("shops").add(shopDataModel).addOnCompleteListener {
            callback.onUploadSuccessful(it.result?.id)
        }.addOnFailureListener {
            callback.onUploadSuccessful(null)
        }
    }

    interface onAuthFirestoreCallback {
        fun onUploadSuccessful(id: String?)
    }

}