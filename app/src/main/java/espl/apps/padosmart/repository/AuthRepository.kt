package espl.apps.padosmart.repository

import android.content.Context
import android.util.Log
import com.esafirm.imagepicker.model.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import espl.apps.padosmart.R
import espl.apps.padosmart.models.ShopDataModel
import espl.apps.padosmart.models.UserDataModel
import espl.apps.padosmart.utils.AUTH_ACCESS_FAILED
import espl.apps.padosmart.utils.END_USER
import espl.apps.padosmart.utils.NEW_USER
import java.util.*


class AuthRepository(private var context: Context) {


    private val TAG = "AuthRepository"

    private val firebaseAuthDatabaseReference: DatabaseReference

    private val mAuth: FirebaseAuth

    private var user: FirebaseUser?

    private val firebaseStorage: FirebaseStorage


    init {
        firebaseAuthDatabaseReference =
            Firebase.database.getReference(context.getString(R.string.firebase_access_permission_node))
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

    /**
     * Returns current database reference
     */
    fun getAuthFirebaseReference(): DatabaseReference {
        Log.d(TAG, "Returning database reference object")
        return firebaseAuthDatabaseReference
    }

    /**
     * End users sign up only via the app, hence a single creation object
     */
    fun createEndUserAuthObject() {
        if (user != null) {
            firebaseAuthDatabaseReference.child(user!!.uid).setValue(END_USER)
            Log.d(TAG, "New end user successfully added to database")
        } else {
            Log.d(TAG, "Unable to add new user to database")
        }
    }

    fun createEndUserDataObject(userData: UserDataModel, callback: UserDataInterface) {
        val dbr =
            Firebase.database.getReference(context.getString(R.string.firebase_user_data_node))
        dbr.child(user!!.uid).setValue(userData).addOnCompleteListener {
            Log.d(TAG, "Successfully created a user data object")
            callback.onUploadCallback(true)
        }.addOnFailureListener {
            Log.d(TAG, "Failed to create a user data object")
            callback.onUploadCallback(false)
        }

    }

    fun getEndUserDataObject(callback: UserDataInterface) {
        val dbr =
            Firebase.database.getReference(context.getString(R.string.firebase_user_data_node))
                .child(user!!.uid)

        val userDataObjectListener = object : ValueEventListener {
            var respModel: UserDataModel? = UserDataModel()
            override fun onCancelled(p0: DatabaseError) {
                respModel = UserDataModel()
                callback.onDataFetch(respModel!!)
            }


            override fun onDataChange(p0: DataSnapshot) {
                Log.d(TAG, "Shop found with response ${p0.value}")
                respModel = p0.getValue<UserDataModel>()
                if (respModel == null) {
                    callback.onDataFetch(UserDataModel())
                } else {
                    callback.onDataFetch(respModel!!)
                }

            }

        }

        dbr.addListenerForSingleValueEvent(userDataObjectListener)


    }


    fun createShopUserAuthObject(shopAuthType: Int) {
        if (user != null) {
            firebaseAuthDatabaseReference.child(user!!.uid).setValue(shopAuthType)
            Log.d(TAG, "New shop successfully added to database")
        } else {
            Log.d(TAG, "Unable to add new shop to database")
        }
    }

    fun createShopDataObject(shopData: ShopDataModel, callback: UserDataInterface) {
        val dbr =
            Firebase.database.getReference(context.getString(R.string.firebase_shop_data_node))
        dbr.child(user!!.uid).setValue(shopData).addOnCompleteListener {
            Log.d(TAG, "Successfully created a shop data object")
            callback.onUploadCallback(true)
        }.addOnFailureListener {
            Log.d(TAG, "Failed to create a shop data object")
            callback.onUploadCallback(false)
        }

    }

    //TODO add checks in place for sending verified data over once complete. Streamline data flow instead of multiple callback nested loops.

    fun fetchShopDataObject(callback: ShopDataFetch) {
        val dbr =
            Firebase.database.getReference(context.getString(R.string.firebase_shop_data_node))
                .child(user!!.uid)

        var respModel: ShopDataModel?

        val shopObjectListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                respModel = null
                callback.onFetchComplete(respModel)
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d(TAG, "Shop found with response ${p0.value}")
                respModel = p0.getValue<ShopDataModel>()
                callback.onFetchComplete(respModel)
            }

        }

        dbr.addListenerForSingleValueEvent(shopObjectListener)
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

    fun uploadShopImages(images: List<Image>, callback: ShopImgURIInterface) {
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
            val userTypeListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    callback.onAuthCallback(AUTH_ACCESS_FAILED.toLong())
                }

                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(TAG, "User found with response ${p0.value}")
                    if (p0.value != null) {
                        val response = p0.value as Long
                        callback.onAuthCallback(response)
                    } else {
                        callback.onAuthCallback(NEW_USER.toLong())
                    }

                }


            }
            firebaseAuthDatabaseReference.child(user!!.uid)
                .addListenerForSingleValueEvent(userTypeListener)
        }
    }


    interface AuthDataInterface {
        fun onAuthCallback(response: Long)
    }

    interface UserDataInterface {
        fun onUploadCallback(success: Boolean)
        fun onDataFetch(dataModel: UserDataModel)
    }

    interface ShopAuthURIInterface {
        fun onUploadCallback(reference: StorageReference?, success: Boolean)
    }

    interface ShopImgURIInterface {
        fun onUploadCallback(reference: StorageReference?, success: Boolean)
    }

    interface ShopDataFetch {
        fun onFetchComplete(shopDataModel: ShopDataModel?)
    }

}