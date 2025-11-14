package com.example.agendapp.ui.main

import android.animation.AnimatorListenerAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.agendapp.R
import com.example.agendapp.data.database.AgenDappDatabaseHelper
import com.example.agendapp.data.model.Event
import com.example.agendapp.util.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var db: AgenDappDatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var etEventName: EditText
    private lateinit var btnSelectDate: Button
    private lateinit var btnSelectTime: Button
    private lateinit var btnSelectImage: Button
    private lateinit var btnCreateEvent: Button
    private lateinit var ivPreview: ImageView
    private lateinit var tvSuccess: TextView

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var savedImagePath: String? = null   // Ruta interna segura
    private var imageUri: Uri? = null            // Para vista previa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        db = AgenDappDatabaseHelper(this)
        session = SessionManager(this)

        etEventName = findViewById(R.id.etEventName)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSelectTime = findViewById(R.id.btnSelectTime)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnCreateEvent = findViewById(R.id.btnCreateEvent)
        ivPreview = findViewById(R.id.ivPreview)
        tvSuccess = findViewById(R.id.tvSuccessMessage)

        // Fecha
        btnSelectDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = "$d/${m + 1}/$y"
                btnSelectDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Hora
        btnSelectTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
                btnSelectTime.text = selectedTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        // Imagen
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        // Crear evento
        btnCreateEvent.setOnClickListener {

            val eventName = etEventName.text.toString().trim()

            if (eventName.isEmpty()) {
                Toast.makeText(this, "El nombre del evento es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedDate == null) {
                Toast.makeText(this, "Debes seleccionar una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = session.getUser()
            if (user == null) {
                Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newEvent = Event(
                id = 0,
                userId = user.id,
                title = eventName,
                date = selectedDate ?: "",
                time = selectedTime,
                imagePath = savedImagePath   // Ruta interna segura
            )

            db.insertEvent(newEvent)
            showSuccessAnimation()
        }
    }

    // Copiar la imagen a la memoria interna de la app
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileName = "img_${System.currentTimeMillis()}.jpg"

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

    // Selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {

            val uri = data?.data ?: return
            imageUri = uri

            // Carga segura con Glide (evita CRASH)
            Glide.with(this)
                .load(uri)
                .error(R.drawable.no_image) // placeholder si falla
                .into(ivPreview)

            // Copiar imagen a almacenamiento interno
            savedImagePath = saveImageToInternalStorage(uri)
        }
    }

    private fun showSuccessAnimation() {
        tvSuccess.alpha = 0f
        tvSuccess.scaleX = 0.8f
        tvSuccess.scaleY = 0.8f
        tvSuccess.text = "¡Evento creado con éxito!"

        tvSuccess.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    tvSuccess.postDelayed({ finish() }, 1200)
                }
            })
    }
}
