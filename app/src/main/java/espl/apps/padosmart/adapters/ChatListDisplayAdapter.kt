package espl.apps.padosmart.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import java.lang.ref.WeakReference

class ChatListDisplayAdapter(
    private val orderList: ArrayList<OrderDataModel>,
    private val buttonListener: ButtonListener
) :
    RecyclerView.Adapter<ChatListDisplayAdapter.ChatHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_commons_chat_tile, parent, false)
        return ChatHolder(v, buttonListener)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bindItems(orderList[position])
    }

    /**
     * Uses interface class to abstract out click listener to classes that can handle it. i.ExercisesFragment
     */

    //TODO system works in a way, ned to add in proper ways to laod user data and display it. Along with caching

    inner class ChatHolder(itemView: View, private val listener: ButtonListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var listenerRef: WeakReference<ButtonListener>? = null
        fun bindItems(order: OrderDataModel) {

            val nameTextView = itemView.findViewById(R.id.userNameTextView) as TextView
            nameTextView.text = order.customerName

            val chatPeekTextView = itemView.findViewById<TextView>(R.id.messagePeekTextView)
            if (order.chats!!.isEmpty()) {
                chatPeekTextView.text = "No message from user"
            } else {
                chatPeekTextView.text = order.chats?.get(0) ?: "No message from user"
            }

            listenerRef = WeakReference(listener)

            val shopChatTile = itemView.findViewById<LinearLayout>(R.id.messageListTile)

            shopChatTile.setOnClickListener(this)


        }

        override fun onClick(v: View?) {
            // Extracting adapter position to send to calling class.
            listenerRef?.get()?.onButtonClick(adapterPosition)

        }
    }

    interface ButtonListener {
        fun onButtonClick(position: Int)
    }


}