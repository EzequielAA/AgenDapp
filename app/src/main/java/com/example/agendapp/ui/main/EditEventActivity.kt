package com.example.agendapp.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agendapp.R
import com.example.agendapp.data.database.AgenDappDatabaseHelper
import com.example.agendapp.data.model.Event
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class EditEventActivity : AppCompatActivity() {

    private lateinit var db: AgenDappDatabaseHelper

    private var eventId: Int = 0
    private var currentEvent: Event? = null

    private var newImageUri: Uri? = null            // URI temporal seleccionada
    private var finalImagePath: String? = null      // Ruta interna persistente

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

        // ----------------------------------------------------------------------
        // 🔥 CARGAR EL EVENTO REAL SEGÚN EL ID
        // ----------------------------------------------------------------------
        currentEvent = db.getEventById(eventId)

        if (currentEvent == null) {
            Toast.makeText(this, "Error: Evento no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ----------------------------------------------------------------------
        // Cargar datos en la vista
        // ----------------------------------------------------------------------
        val event = currentEvent!!

        etTitle.setText(event.title)
        tvDate.text = event.date
        tvTime.text = event.time ?: ""

        finalImagePath = event.imagePath

        // Cargar imagen interna
        if (!finalImagePath.isNullOrEmpty()) {
            val file = File(finalImagePath!!)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                ivPreview.setImageBitmap(bitmap)
            }
        }

        // ----------------------------------------------------------------------
        // Seleccionar fecha
        // ----------------------------------------------------------------------
        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                tvDate.text = "$d/${m + 1}/$y"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // ----------------------------------------------------------------------
        // Seleccionar hora
        // ----------------------------------------------------------------------
        btnPickTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                tvTime.text = String.format("%02d:%02d", h, min)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        // ----------------------------------------------------------------------
        // Seleccionar imagen
        // ----------------------------------------------------------------------
        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, 200)
        }

        // ----------------------------------------------------------------------
        // Guardar cambios
        // ----------------------------------------------------------------------
        btnSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val date = tvDate.text.toString().trim()
            val time = tvTime.text.toString().trim()

            if (title.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si seleccionó nueva imagen → guardarla internamente
            if (newImageUri != null) {

                // borrar imagen antigua si existía
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
                Toast.makeText(this, "Evento actualizado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar el evento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================================================
    // 📌 Guardar imagen dentro de la app (igual a CreateEventActivity)
    // ==========================================================================
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

    // ==========================================================================
    // Resultado de seleccionar imagen
    // ==========================================================================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            newImageUri = data.data
            findViewById<ImageView>(R.id.ivEditPreview).setImageURI(newImageUri)
        }
    }
}
