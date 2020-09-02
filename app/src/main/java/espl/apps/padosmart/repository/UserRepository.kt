package espl.apps.padosmart.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import espl.apps.padosmart.R

class UserRepository(context: Context) {

    private var firebaseUserDatabaseReference: DatabaseReference
    private var user: FirebaseUser?

    init {
        firebaseUserDatabaseReference =
            Firebase.database.getReference(context.getString(R.string.firebase_user_data_node))
        user = FirebaseAuth.getInstance().currentUser
    }


}