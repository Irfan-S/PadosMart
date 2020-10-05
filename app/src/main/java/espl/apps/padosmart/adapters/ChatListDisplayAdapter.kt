package espl.apps.padosmart.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import java.lang.ref.WeakReference

class ChatListDisplayAdapter(
    private var orderList: ArrayList<OrderDataModel>,
    private val buttonListener: ButtonListener
) :
    RecyclerView.Adapter<ChatListDisplayAdapter.ChatHolder>(), Filterable {

    private val TAG = "ChatListAdapter"

    var orderListFiltered = ArrayList<OrderDataModel>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_commons_chatlist_tile, parent, false)
        return ChatHolder(v, buttonListener)
    }

    override fun getItemCount(): Int {
        return orderListFiltered.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bindItems(orderListFiltered[position])
    }

    fun getItem(position: Int): OrderDataModel {
        return orderListFiltered[position]
    }

    fun updateChatList(orderList: ArrayList<OrderDataModel>) {
        this.orderList = orderList
        orderListFiltered = orderList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter? {
        Log.d(TAG, "Filtering text")
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    orderListFiltered = orderList
                } else {
                    val filteredList: ArrayList<OrderDataModel> = ArrayList()
                    for (order in orderList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or UID match
                        if (order.customerName?.toLowerCase()
                            !!.contains(charString.toLowerCase()) || order.chats!!.contains(
                                charString.toLowerCase()
                            )
                        ) {
                            filteredList.add(order)
                        }
                    }
                    orderListFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = orderListFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                orderListFiltered = filterResults.values as ArrayList<OrderDataModel>
                notifyDataSetChanged()
            }
        }
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
                chatPeekTextView.text =
                    order.chats?.size?.minus(1)?.let { order.chats?.get(it) }!!.message
                        ?: "No message from user"
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

    init {
        orderListFiltered = orderList
    }

}