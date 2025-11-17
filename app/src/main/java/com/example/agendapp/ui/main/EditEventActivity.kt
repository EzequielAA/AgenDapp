package com.example.agendapp.ui.main

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agendapp.R
import com.example.agendapp.data.database.AgenDappDatabaseHelper
import com.example.agendapp.data.model.Event
import com.example.agendapp.util.ReminderReceiver
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class EditEventActivity : AppCompatActivity() {

    private lateinit var db: AgenDappDatabaseHelper

    private var eventId: Int = 0
    private var currentEvent: Event? = null

    private var newImageUri: Uri? = null
    private var finalImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        db = AgenDappDatabaseHelper(this)
        eventId = intent.getIntExtra("eventId", 0)

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val tvDate = findViewById<TextView>(R.id.tvEditDate)
        val tvTime = findViewById<TextView>(R.id.tvEditTime)
        val ivPreview = findViewById<ImageView>(R.id.ivEditPreview)
        val btnPickDate = findViewById<Button>(R.id.btnEditPickDate)
        val btnPickTime = findViewById<Button>(R.id.btnEditPickTime)
        val btnPickImage = findViewById<Button>(R.id.btnEditPickImage)
        val btnSave = findViewById<Button>(R.id.btnSaveChanges)

        currentEvent = db.getEventById(eventId)

        if (currentEvent == null) {
            Toast.makeText(this, "Error: Evento no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val event = currentEvent!!

        etTitle.setText(event.title)
        tvDate.text = event.date
        tvTime.text = event.time ?: ""

        finalImagePath = event.imagePath

        if (!finalImagePath.isNullOrEmpty()) {
            val file = File(finalImagePath!!)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                ivPreview.setImageBitmap(bitmap)
            }
        }

        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                tvDate.text = "$d/${m + 1}/$y"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPickTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                tvTime.text = String.format("%02d:%02d", h, min)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, 200)
        }

        btnSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val date = tvDate.text.toString().trim()
            val time = tvTime.text.toString().trim()

            if (title.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newImageUri != null) {
                finalImagePath?.let { oldPath ->
                    val oldFile = File(oldPath)
                    if (oldFile.exists()) oldFile.delete()
                }
                finalImagePath = saveImageToInternal(newImageUri!!)
            }

            val updatedEvent = event.copy(
                title = title,
                date = date,
                time = time,
                imagePath = finalImagePath
            )

            if (db.updateEvent(updatedEvent)) {

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, ReminderReceiver::class.java)

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    updatedEvent.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)

                Toast.makeText(this, "Evento actualizado. Vuelve a activar el recordatorio si es necesario.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar el evento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternal(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileName = "edit_img_${System.currentTimeMillis()}.jpg"

            val dir = File(filesDir, "event_images")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            newImageUri = data.data
            findViewById<ImageView>(R.id.ivEditPreview).setImageURI(newImageUri)
        }

    }
}