package espl.apps.padosmart.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import espl.apps.padosmart.R
import espl.apps.padosmart.models.UserDataModel


class AuthRepository(context: Context) {


    val END_USER = 1
    val SHOP_USER = 2
    val NEW_USER = 3
    val AUTH_ACCESS_FAILED = 0

    private val TAG = "CoreRepository"

    private val firebaseAuthDatabaseReference: DatabaseReference

    private val mAuth: FirebaseAuth

    private var user: FirebaseUser?

    private var context: Context


    init {
        this.context = context
        firebaseAuthDatabaseReference =
            Firebase.database.getReference(context.getString(R.string.firebase_user_access_node))
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
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
    fun getFirebaseReference(): DatabaseReference {
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


    fun getFirebaseUserType(callback: AuthDataInterface) {
        if (user != null) {
            val userTypeListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    callback.onAuthCallback(AUTH_ACCESS_FAILED.toLong())
                }

                override fun onDataChange(p0: DataSnapshot) {
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
    }

}