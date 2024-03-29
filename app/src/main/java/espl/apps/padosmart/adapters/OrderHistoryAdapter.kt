package espl.apps.padosmart.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.R
import espl.apps.padosmart.models.OrderDataModel
import espl.apps.padosmart.utils.*
import java.lang.ref.WeakReference

class OrderHistoryAdapter(
    private val orderType: String,
    private var orderList: ArrayList<OrderDataModel>,
    private val buttonListener: ButtonListener
) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_order_list_tile, parent, false)
        return OrderHolder(v, buttonListener)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: OrderHolder, position: Int) {
        holder.bindItems(orderList[position])
    }


    fun updateOrders(orderList: ArrayList<OrderDataModel>) {
        this.orderList = orderList
        notifyDataSetChanged()
    }

    /**
     * Uses interface class to abstract out click listener to classes that can handle it. i.ExercisesFragment
     */

    inner class OrderHolder(itemView: View, private val listener: ButtonListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var listenerRef: WeakReference<ButtonListener>? = null
        fun bindItems(order: OrderDataModel) {
            val name = if (orderType == QUERY_ARG_USER_ID) {
                order.shopName
            } else {
                order.customerName
            }

            val nameTextView = itemView.findViewById(R.id.nameTextView) as TextView
            nameTextView.text = name
            val shopStatusTextView = itemView.findViewById(R.id.orderStatusText) as TextView

            val viewDetailsButton = itemView.findViewById<Button>(R.id.viewDetailsButton)
            viewDetailsButton.setOnClickListener(this)

            shopStatusTextView.text = when (order.orderStatus) {
                ORDER_STATUS_COMPLETED -> "delivered"
                ORDER_STATUS_CANCELLED -> "cancelled"
                ORDER_STATUS_IN_PROGRESS -> "In progress"
                ORDER_STATUS_CONFIRMED -> "Confirmed"
                else -> "N/A"
            }

            listenerRef = WeakReference(listener)

            val messageListTile = itemView.findViewById<LinearLayout>(R.id.messageListTile)
            messageListTile?.setOnClickListener(this)


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