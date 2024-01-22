package com.example.trelloclone.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.models.Task
import java.util.Collections

open class TaskListItemsAdapter(private val context: Context, private var taskList: ArrayList<Task>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        // Here the layout params are converted dynamically according to the screen size as width is 70% and height is wrap_content.
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Here the dynamic margins are applied to the view.
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = taskList[position]

        val tvAddTaskList: TextView? = holder.itemView.findViewById(R.id.tv_add_task_list)
        val llTaskItem: LinearLayout? = holder.itemView.findViewById(R.id.ll_task_item)

        if (tvAddTaskList != null && llTaskItem != null) {
            if (holder is MyViewHolder) {

                if (position == taskList.size - 1) {
                    tvAddTaskList.visibility = View.VISIBLE
                    llTaskItem.visibility = View.GONE
                } else {
                    tvAddTaskList.visibility = View.GONE
                    llTaskItem.visibility = View.VISIBLE
                }

                holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).let { textView -> textView.text = model.title }
                tvAddTaskList.setOnClickListener {
                    tvAddTaskList.visibility = View.GONE
                    holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).let { cardView -> cardView.visibility = View.VISIBLE }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        tvAddTaskList.visibility = View.VISIBLE
                        holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name)
                            .let { cardView -> cardView.visibility = View.GONE }
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        holder.itemView.findViewById<EditText>(R.id.et_task_list_name)
                            .let { editText ->
                                val listName = editText.text.toString()
                                if (listName.isNotEmpty()) {
                                    if (context is TaskListActivity) {
                                        context.createTaskList(listName)
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please enter list name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).setText(model.title)
                        holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
                        holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.VISIBLE
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).let { linearLayout -> linearLayout.visibility = View.VISIBLE }
                        holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).let { cardView -> cardView.visibility = View.GONE }
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).let { editText ->
                            val listName = editText.text.toString()
                            if (listName.isNotEmpty()) {
                                if (context is TaskListActivity) {
                                    context.updateTaskList(position, listName, model)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a list name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        alertDialogForDeleteList(position, model.title)
                    }
                }

                holder.itemView.findViewById<TextView>(R.id.tv_add_card).let { textView ->
                    textView.setOnClickListener {
                        holder.itemView.findViewById<TextView>(R.id.tv_add_card).let { it.visibility = View.GONE }
                        holder.itemView.findViewById<CardView>(R.id.cv_add_card).let { it.visibility = View.VISIBLE }
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        holder.itemView.findViewById<TextView>(R.id.tv_add_card).let { it.visibility = View.VISIBLE }
                        holder.itemView.findViewById<CardView>(R.id.cv_add_card).let { it.visibility = View.GONE }
                    }
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).let { imageBtn ->
                    imageBtn.setOnClickListener {
                        holder.itemView.findViewById<EditText>(R.id.et_card_name).let { editText ->
                            val cardName = editText.text.toString()
                            if (cardName.isNotEmpty()) {
                                if (context is TaskListActivity) {
                                    context.addCardToTaskList(position, cardName)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a card name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).let { recyclerView ->
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.setHasFixedSize(true)

                    val adapter = CardListItemsAdapter(context, model.cards)
                    recyclerView.adapter = adapter

                    adapter.setOnClickListener(object: CardListItemsAdapter.OnClickListener {
                        override fun onClick(cardPosition: Int) {
                            if (context is TaskListActivity) {
                                context.cardDetails(position, cardPosition)
                            }
                        }
                    })


                    val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    recyclerView.addItemDecoration(dividerItemDecoration)
                    val helper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                        override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                            val draggedPosition = dragged.adapterPosition
                            val targetPosition = target.adapterPosition

                            if (mPositionDraggedFrom == -1) {
                                mPositionDraggedFrom = draggedPosition
                            }
                            mPositionDraggedTo = targetPosition
                            Collections.swap(taskList[position].cards, draggedPosition, targetPosition)
                            adapter.notifyItemMoved(draggedPosition, targetPosition)
                            return false
                        }

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                            super.clearView(recyclerView, viewHolder)
                            if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {
                                (context as TaskListActivity).updateCardsInTaskList(position, taskList[position].cards)
                            }
                            mPositionDraggedFrom = -1
                            mPositionDraggedTo = -1
                        }
                    })
                    helper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))
                }
            }
        }
    }

    /**
     * Method is used to show the Alert Dialog for deleting the task list.
     */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return taskList.size
    }

    /**
     * A function to get density pixel from pixel
     */
    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    /**
     * A function to get pixel from density pixel
     */
    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}