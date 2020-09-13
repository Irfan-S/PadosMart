package espl.apps.padosmart.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import espl.apps.padosmart.models.OrderDataModel
import java.lang.ref.WeakReference

class OrderHistoryAdapter(
    private val orderList: ArrayList<OrderDataModel>,
    private val buttonListener: ButtonListener
) :
    RecyclerView.Adapter<ExerciseRecyclerAdapter.GIFHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GIFHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_exercise_display, parent, false)
        return GIFHolder(v, buttonListener)
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    override fun onBindViewHolder(holder: GIFHolder, position: Int) {
        holder.bindItems(exerciseList[position])
    }

    /**
     * Uses interface class to abstract out click listener to classes that can handle it. i.ExercisesFragment
     */

    inner class GIFHolder(itemView: View, private val listener: ButtonListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var listenerRef: WeakReference<ButtonListener>? = null
        fun bindItems(exercise: Exercise) {
            val gifView = itemView.findViewById(R.id.exercise_gif_360_display) as GifImageView
            val textViewNumber = itemView.findViewById(R.id.exercise_number_txtview) as TextView
            val textViewShortDesc =
                itemView.findViewById(R.id.exercise_description_txtview) as TextView
            listenerRef = WeakReference(listener)

            val startExerciseButton = itemView.findViewById<Button>(R.id.start_exercise_button)
            startExerciseButton.setOnClickListener(this)

            gifView.setImageResource(exercise.ID360)
            textViewNumber.text = exercise.name
            textViewShortDesc.text = exercise.shortDescription


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