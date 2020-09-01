package espl.apps.padosmart.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class AuthRepository {


    val END_USER = 1
    val SHOP_USER = 2
    val DELIVERY_USER = 3
    val NEW_USER = 4
    val AUTH_ACCESS_FAILED = -1
    val AUTH_ACCESS_SUCCESSFUL = 0

    private val TAG = "CoreRepository"

    private val firebaseAuthDatabaseReference: DatabaseReference

    private val mAuth: FirebaseAuth

    private var user: FirebaseUser?


    init {
        firebaseAuthDatabaseReference = Firebase.database.getReference("user_access_node")
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


    fun getFirebaseUserType(callback: AuthDataInterface): Int {
        if (user != null) {
            val userTypeListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    //TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.value != null) {
                        val response = p0.value as Int
                        callback.onAuthCallback(response)
                    } else {
                        callback.onAuthCallback(NEW_USER)
                    }

                }

            }
            firebaseAuthDatabaseReference.child(user!!.uid)
                .addListenerForSingleValueEvent(userTypeListener)
            return AUTH_ACCESS_SUCCESSFUL
        } else {
            return AUTH_ACCESS_FAILED
        }
    }


    interface AuthDataInterface {
        fun onAuthCallback(response: Int)
    }

}