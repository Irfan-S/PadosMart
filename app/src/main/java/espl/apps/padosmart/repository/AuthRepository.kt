package espl.apps.padosmart.repository

import android.content.Context
import android.util.Log
import com.esafirm.imagepicker.model.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import espl.apps.padosmart.R
import espl.apps.padosmart.models.AccessDataModel
import espl.apps.padosmart.models.AppVersionDataModel
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.utils.*
import java.util.*


class AuthRepository(private var context: Context) {


    private val TAG = "AuthRepository"

    val fireStoreDB: FirebaseFirestore

    private val mAuth: FirebaseAuth
    private var user: FirebaseUser?
    private val firebaseStorage: FirebaseStorage

    init {
        fireStoreDB = Firebase.firestore
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        firebaseStorage = Firebase.storage
    }


    /**
     * Returns current auth instance
     */
    fun getFirebaseAuthReference(): FirebaseAuth {
        Log.d(TAG, "Returning firebase auth object")
        return mAuth
    }

    fun getAppVersion(callback: AppVersionCallback) {
        fireStoreDB.collection(NODE_APP_VERSION).document(SUB_NODE_VERSION).get()
            .addOnSuccessListener {
                if (it != null) {
                    callback.onVersionGet(it.toObject<AppVersionDataModel>()!!)
                } else {
                    callback.onVersionGet(AppVersionDataModel())
                }
            }.addOnFailureListener {
            callback.onVersionGet(AppVersionDataModel())
        }
    }

    /**
     * End users sign up only via the app, hence a single creation object
     */
    fun createEndUserAuthObject() {
        if (user != null) {
            val authData = AccessDataModel(END_USER)
            fireStoreDB.collection(NODE_ACCESS_PERMS).document(user!!.uid).set(authData)
            Log.d(TAG, "New end user successfully added to database")
        } else {
            Log.d(TAG, "Unable to add new user to database")
        }
    }

    fun createEndUserDataObject(userData: UserDataModel, callback: UserDataInterface) {
        fireStoreDB.collection(NODE_USERS).document(user!!.uid).set(userData)
            .addOnCompleteListener {
                Log.d(TAG, "Successfully created a user data object")
                callback.onUploadCallback(true)
            }.addOnFailureListener {
            Log.d(TAG, "Failed to create a user data object")
            callback.onUploadCallback(false)
        }

    }

    fun getEndUserDataObject(callback: UserDataInterface) {
        fireStoreDB.collection(NODE_USERS).document(user!!.uid).get().addOnSuccessListener {
            if (it.data != null) {
                Log.d(TAG, "Resp from user : $it")
                callback.onDataFetch(it.toObject<UserDataModel>()!!)
            } else {
                callback.onDataFetch(UserDataModel())
            }
        }.addOnFailureListener {
            callback.onDataFetch(UserDataModel())
        }


    }


    fun createShopUserAuthObject(shopAuthType: Int) {
        if (user != null) {
            val authData = AccessDataModel(shopAuthType)
            fireStoreDB.collection(NODE_ACCESS_PERMS).document(user!!.uid).set(authData)
            Log.d(TAG, "New shop successfully added to database")
        } else {
            Log.d(TAG, "Unable to add new shop to database")
        }
    }

    fun createShopDataObject(shopData: ShopDataModel, callback: UserDataInterface) {
        fireStoreDB.collection(NODE_SHOPS).document(user!!.uid).set(shopData)
            .addOnCompleteListener {
                Log.d(TAG, "Successfully created a shop data object")
                callback.onUploadCallback(true)
            }.addOnFailureListener {
            Log.d(TAG, "Failed to create a shop data object")
            callback.onUploadCallback(false)
        }

    }


    //TODO add checks in place for sending verified data over once complete. Streamline data flow instead of multiple callback nested loops.

    fun fetchShopDataObject(callback: ShopDataFetch) {

        fireStoreDB.collection(NODE_SHOPS).document(user!!.uid).get().addOnSuccessListener {
            if (it != null) {
                callback.onFetchComplete(it.toObject<ShopDataModel>())
            }
        }
    }

    fun uploadAuthImages(images: List<Image>, callback: ShopAuthURIInterface) {
        val path = context.getString(R.string.storage_authdata) + user!!.phoneNumber
        for (image in images) {
            val imgPath = path + "/" + UUID.randomUUID()
            val ref = firebaseStorage.reference.child(imgPath)
            ref.putFile(image.uri).addOnCompleteListener {
                callback.onUploadCallback(ref, success = true)
            }.addOnFailureListener {
            }.addOnFailureListener {
                callback.onUploadCallback(null, false)
            }
        }
    }


    fun uploadShopImages(images: List<Image>, callback: ImgURIInterface) {
        val path = context.getString(R.string.storage_shopinfo) + user!!.phoneNumber
        for (image in images) {
            val imgPath = path + "/" + UUID.randomUUID()
            val ref = firebaseStorage.reference.child(imgPath)
            ref.putFile(image.uri).addOnCompleteListener {
                Log.d(TAG, "Upload completed for user image")
                callback.onUploadCallback(ref, success = true)

            }.addOnFailureListener {
                callback.onUploadCallback(null, false)
            }

        }
    }

    fun uploadChatImages(images: List<Image>, callback: ImgURIInterface) {
        val path = context.getString(R.string.storage_chat) + user!!.phoneNumber
        for (image in images) {
            val imgPath = path + "/" + UUID.randomUUID()
            val ref = firebaseStorage.reference.child(imgPath)
            ref.putFile(image.uri).addOnCompleteListener {
                Log.d(TAG, "Upload completed for user image")
                callback.onUploadCallback(ref, success = true)

            }.addOnFailureListener {
                callback.onUploadCallback(null, false)
            }

        }
    }

    /**
     * Returns current logged in firebase user
     */
    fun getFirebaseUser(): FirebaseUser? {
        Log.d(TAG, "Returning user object")
        return user
    }

    fun setFirebaseUser(user: FirebaseUser) {
        Log.d(TAG, "Setting user object")
        this.user = user
    }

    fun signOut() {
        mAuth.signOut()
    }


    fun getFirebaseUserType(callback: AuthDataInterface) {
        Log.d(TAG, "Searching database for user.")
        if (user != null) {
            fireStoreDB.collection(NODE_ACCESS_PERMS).document(user!!.uid).get()
                .addOnSuccessListener {
                    if (it.data == null) {
                        callback.onAuthCallback(AccessDataModel())
                    } else {
                        callback.onAuthCallback(it.toObject()!!)
                    }
                }.addOnFailureListener {
                callback.onAuthCallback(AccessDataModel(AUTH_ACCESS_FAILED))
            }
        }
    }


    interface AuthDataInterface {
        fun onAuthCallback(accessDataModel: AccessDataModel)
    }

    interface UserDataInterface {
        fun onUploadCallback(success: Boolean)
        fun onDataFetch(dataModel: UserDataModel)
    }

    interface ShopAuthURIInterface {
        fun onUploadCallback(reference: StorageReference?, success: Boolean)
    }

    interface ImgURIInterface {
        fun onUploadCallback(reference: StorageReference?, success: Boolean)
    }

    interface ShopDataFetch {
        fun onFetchComplete(shopDataModel: ShopDataModel?)
    }

    interface AppVersionCallback {
        fun onVersionGet(appVersionDataModel: AppVersionDataModel)
    }
}