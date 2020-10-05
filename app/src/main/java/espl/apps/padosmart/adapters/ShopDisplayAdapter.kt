package espl.apps.padosmart.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    RecyclerView.Adapter<ShopDisplayAdapter.ShopHolder>(), Filterable {

    private val TAG = "ShopAdapter"

    var shopFilterList = ArrayList<ShopDataModel>()

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
        Log.d(TAG, "Fetch size : ${shopFilterList.size}")
        return shopFilterList.size
    }

    override fun onBindViewHolder(holder: ShopHolder, position: Int) {
        Log.d(TAG, "Binding")
        holder.bindItems(shopFilterList[position])
    }

    fun getItem(position: Int): ShopDataModel {
        return shopFilterList[position]
    }

    fun updateChats(shopList: java.util.ArrayList<ShopDataModel>) {
        this.shopList = shopList
        shopFilterList = shopList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter? {
        Log.d(TAG, "Filtering text")
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    shopFilterList = shopList
                } else {
                    val filteredList: ArrayList<ShopDataModel> = ArrayList()
                    for (shop in shopList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or UID match
                        if (shop.shopName?.toLowerCase()
                            !!.contains(charString.toLowerCase()) || shop.address?.toLowerCase()
                            !!.contains(charString.toLowerCase())
                        ) {
                            filteredList.add(shop)
                        }
                    }
                    shopFilterList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = shopFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                shopFilterList = filterResults.values as ArrayList<ShopDataModel>
                notifyDataSetChanged()
            }
        }
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

    init {
        shopFilterList = shopList
    }


}