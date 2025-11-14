package com.example.agendapp.ui.main.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.agendapp.R
import com.example.agendapp.data.model.Event
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private val context: Context,
    private var events: List<Event>,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit,
    private val onRemindClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvEventDate)
        val ivImage: ImageView = itemView.findViewById(R.id.ivEventImage)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnRemind: Button = itemView.findViewById(R.id.btnRemind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.tvTitle.text = event.title
        holder.tvDate.text = "📅 ${event.date} ${event.time ?: ""}"

        // -------------------------------
        // Carga segura de imagen
        // -------------------------------
        if (!event.imagePath.isNullOrEmpty()) {
            val file = File(event.imagePath)
            if (file.exists()) {
                holder.ivImage.visibility = View.VISIBLE
                Glide.with(context)
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.no_image)
                    .into(holder.ivImage)
            } else {
                holder.ivImage.visibility = View.GONE
            }
        } else {
            holder.ivImage.visibility = View.GONE
        }

        // -------------------------------
        // Oscurecer item si evento pasado
        // -------------------------------
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val eventDateTime = try {
            sdf.parse("${event.date} ${event.time ?: "00:00"}")
        } catch (e: Exception) { null }

        if (eventDateTime != null && eventDateTime.time < System.currentTimeMillis()) {
            holder.itemView.alpha = 0.5f
            holder.tvTitle.text = "${event.title} (Evento Terminado)"
            holder.btnRemind.isEnabled = false
        } else {
            holder.itemView.alpha = 1f
            holder.btnRemind.isEnabled = true
        }

        // -------------------------------
        // Botones
        // -------------------------------
        holder.btnEdit.setOnClickListener { onEditClick(event) }
        holder.btnDelete.setOnClickListener { onDeleteClick(event) }
        holder.btnRemind.setOnClickListener {
            Toast.makeText(context, "Notificación activada para '${event.title}'", Toast.LENGTH_SHORT).show()
            onRemindClick(event)
        }
    }

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
