package espl.apps.padosmart.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatRepository {
    private val TAG = "ChatRepository"
    private var firebaseDatabaseReference: DatabaseReference
    private var user: FirebaseUser?


    init {
        firebaseDatabaseReference = Firebase.database.getReference("chats")
        user = FirebaseAuth.getInstance().currentUser
    }

//    fun initiateChat(orderDataModel: OrderDataModel,onChatInterface: ChatInterface){
//        val chatID = firebaseDatabaseReference.push().key
//        if(chatID!=null){
//            firebaseDatabaseReference.push().setValue(orderDataModel).addOnSuccessListener {
//                onChatInterface.onChatInit(chatID)
//            }.addOnFailureListener {
//                onChatInterface.onChatInit(null)
//            }
//        }else{
//                onChatInterface.onChatInit(null)
//        }
//    }
//
//    fun updateChatDetails(chatID: String,chatObject:String,details:Any,onChatUpdated:ChatInterface){
//        firebaseDatabaseReference.child(chatID).child(chatObject).setValue(details).addOnCompleteListener {
//            onChatUpdated.onChatUpdated(it.isSuccessful)
//        }
//    }
//
//    fun attachChatListener(chatID:String,chatObject:String,listener:ValueEventListener){
//        firebaseDatabaseReference.child(chatID).child(chatObject).addValueEventListener(listener)
//    }
//
//
//    fun removeListeners(chatID: String,chatObject:String,listener: ValueEventListener){
//        firebaseDatabaseReference.child(chatID).child(chatObject).removeEventListener(listener)
//    }

    interface ChatInterface {
        fun onChatInit(chatID: String?)
        fun onChatUpdated(success: Boolean)
    }


}