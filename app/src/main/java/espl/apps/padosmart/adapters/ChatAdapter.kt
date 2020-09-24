package espl.apps.padosmart.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import espl.apps.padosmart.R
import espl.apps.padosmart.models.ChatDataModel
import java.text.SimpleDateFormat
import java.util.*


class ChatAdapter(
    private val userName: String,
    private var chatList: ArrayList<ChatDataModel>,
) :
    RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    private val TAG = "ChatAdapter"
    val SENT = 0
    val RECEIVED = 1
    lateinit var context: Context


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatHolder {
        context = parent.context
        Log.d(TAG, "viewtype is :$viewType")
        val viewHolder = when (viewType) {
            SENT -> ChatHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.element_commons_chat_tile_sender,
                    parent,
                    false
                )
            )
            // other view holders...
            else -> ChatHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.element_commons_chat_tile_received,
                    parent,
                    false
                )
            )
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        Log.d(TAG, "viewtype is :$position")
        val type = when (chatList[position].senderName) {
            (userName) -> SENT
            else -> RECEIVED
        }
        return type
    }


    override fun getItemCount(): Int {
        Log.d(TAG, "In item count w size: ${chatList.size}")
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bindItems(chatList[position])
    }

    fun updateChats(chatList: ArrayList<ChatDataModel>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    //TODO system works in a way, ned to add in proper ways to laod user data and display it. Along with caching

    inner class ChatHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var nameTextView: TextView
        var messageText: TextView
        var imageDisplay: ImageView
        var timeText: TextView
        fun bindItems(item: ChatDataModel) {
            if (item.attachmentURI != null) {
                Glide.with(context).load(item.attachmentURI).into(imageDisplay)
            }
            nameTextView.text = item.senderName
            messageText.text = item.message

            val simpleDateFormat = SimpleDateFormat("hh:mm:ss")
            val date = Date(item.time!!)
            val time = simpleDateFormat.format(date)
            // Format the stored timestamp into a readable String using method.

            timeText.text = time
        }

        init {
            nameTextView = itemView.findViewById(R.id.text_message_name)
            imageDisplay = itemView.findViewById(R.id.chatImageView)
            messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
            timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
        }

    }


}
/**v
private val context: Context,
private val orderList: ArrayList<OrderDataModel>,
) :
RecyclerView.Adapter<ChatAdapter.BaseViewHolder<*>>() {


private val VIEW_TYPE_MESSAGE_SENT = 1
private val VIEW_TYPE_MESSAGE_RECEIVED = 2

override fun onCreateViewHolder(
parent: ViewGroup,
viewType: Int
): ChatHolder {
var view:View?

if (viewType == VIEW_TYPE_MESSAGE_SENT) {
view = LayoutInflater.from(parent.context)
.inflate(R.layout.element_commons_chat_tile_sender, parent, false);
return SentMessageHolder(view);
} else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
view = LayoutInflater.from(parent.context)
.inflate(R.layout.element_commons_chat_tile_received, parent, false);
return ReceivedMessageHolder(view);
}

return null;
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

val nameTextView = itemView.findViewById(espl.apps.padosmart.R.id.userNameTextView) as TextView
nameTextView.text = order.customerName

val chatPeekTextView = itemView.findViewById<TextView>(espl.apps.padosmart.R.id.messagePeekTextView)
if (order.chats!!.isEmpty()) {
chatPeekTextView.text = "No message from user"
} else {
chatPeekTextView.text = order.chats?.get(0) ?: "No message from user"
}

listenerRef = WeakReference(listener)

val shopChatTile = itemView.findViewById<LinearLayout>(espl.apps.padosmart.R.id.messageListTile)

shopChatTile.setOnClickListener(this)


}

override fun onClick(v: View?) {
// Extracting adapter position to send to calling class.
listenerRef?.get()?.onButtonClick(adapterPosition)

}
}

inner class SentMessageHolder(itemView: View) :
BaseViewHolder<ChatDataModel>(itemView) {
var messageText: TextView
var imageDisplay:ImageView
var timeText: TextView
override fun bind(item: ChatDataModel) {
if(item.attachmentURI!=null){
Glide.with(context).load(item.attachmentURI).into(imageDisplay)
}
messageText.text= item.message

val hm = String.format(
"%02d:%02d", TimeUnit.MILLISECONDS.toHours(item.time!!),
TimeUnit.MILLISECONDS.toMinutes(item.time!!) % TimeUnit.HOURS.toMinutes(1)
)

// Format the stored timestamp into a readable String using method.

timeText.text = hm
}

init {
imageDisplay = itemView.findViewById(R.id.chatImageView)
messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
}
}

inner class ReceivedMessageHolder (itemView: View) :
BaseViewHolder<ChatDataModel>(itemView) {
var messageText: TextView
var imageDisplay:ImageView
var timeText: TextView
override fun bind(item: ChatDataModel) {
if(item.attachmentURI!=null){
Glide.with(context).load(item.attachmentURI).into(imageDisplay)
}
messageText.text= item.message

val hm = String.format(
"%02d:%02d", TimeUnit.MILLISECONDS.toHours(item.time!!),
TimeUnit.MILLISECONDS.toMinutes(item.time!!) % TimeUnit.HOURS.toMinutes(1)
)

// Format the stored timestamp into a readable String using method.

timeText.text = hm
}

init {
imageDisplay = itemView.findViewById(R.id.chatImageView)
messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
}
}

interface ButtonListener {
fun onButtonClick(position: Int)
}

abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
abstract fun bind(item: T)
}


}


private var mContext: Context? = null
lateinit var mMessageList: ArrayList<ChatDataModel>

fun ChatAdapter(context: Context, messageList: ArrayList<ChatDataModel>) {
mContext = context
mMessageList = messageList
}

override fun getItemCount(): Int {
return mMessageList.size
}

// Determines the appropriate ViewType according to the sender of the message.
override fun getItemViewType(position: Int): Int {
val message: ChatDataModel = mMessageList[position] as ChatDataModel
return if (message.senderID.equals(SendBird.getCurrentUser().getUserId())) {
// If the current user is the sender of the message
VIEW_TYPE_MESSAGE_SENT
} else {
// If some other user sent the message
VIEW_TYPE_MESSAGE_RECEIVED
}
}

// Inflates the appropriate layout according to the ViewType.
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
val view: View
if (viewType == VIEW_TYPE_MESSAGE_SENT) {
view = LayoutInflater.from(parent.context)
.inflate(R.layout.item_message_sent, parent, false)
return SentMessageHolder(view)
} else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
view = LayoutInflater.from(parent.context)
.inflate(R.layout.item_message_received, parent, false)
return ReceivedMessageHolder(view)
}
return null
}

// Passes the message object to a ViewHolder so that the contents can be bound to UI.
fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
val message: UserMessage = mMessageList.get(position) as UserMessage
when (holder.itemViewType) {
VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
}
}



 **/