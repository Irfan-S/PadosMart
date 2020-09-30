package espl.apps.padosmart.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import espl.apps.padosmart.R
import espl.apps.padosmart.models.ShopDataModel
import java.lang.ref.WeakReference

class ShopDisplayAdapter(
    private var shopList: ArrayList<ShopDataModel>,
    private val buttonListener: ButtonListener
) :
    RecyclerView.Adapter<ShopDisplayAdapter.ShopHolder>() {

    private val TAG = "ShopAdapter"

    lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShopHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_home_shop_tile, parent, false)
        context = parent.context
        return ShopHolder(v, buttonListener)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "Fetch size : ${shopList.size}")
        return shopList.size
    }

    override fun onBindViewHolder(holder: ShopHolder, position: Int) {
        Log.d(TAG, "Binding")
        holder.bindItems(shopList[position])
    }

    fun updateChats(shopList: java.util.ArrayList<ShopDataModel>) {
        this.shopList = shopList
        notifyDataSetChanged()
    }

    /**
     * Uses interface class to abstract out click listener to classes that can handle it. i.ExercisesFragment
     */

    inner class ShopHolder(itemView: View, private val listener: ButtonListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var listenerRef: WeakReference<ButtonListener>? = null
        fun bindItems(shop: ShopDataModel) {


            val nameTextView = itemView.findViewById(R.id.shopNameElementTextView) as TextView
            nameTextView.text = shop.shopName

            val shopDisplayImageView = itemView.findViewById(R.id.shopDisplayImg) as ImageView

            Glide.with(context).load(shop.shopImageURL)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(shopDisplayImageView)
            val shopTile = itemView.findViewById(R.id.elementShopTile) as LinearLayout

            listenerRef = WeakReference(listener)

            shopTile.setOnClickListener(this)


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