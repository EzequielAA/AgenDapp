package com.example.agendapp.ui.main

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agendapp.R
import com.example.agendapp.data.database.AgenDappDatabaseHelper
import com.example.agendapp.data.model.Event
import com.example.agendapp.ui.auth.LoginActivity
import com.example.agendapp.ui.clima.ClimaActivity // 🔥 IMPORTANTE: Importar la nueva Activity
import com.example.agendapp.ui.main.adapter.EventAdapter
import com.example.agendapp.util.ReminderReceiver
import com.example.agendapp.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: AgenDappDatabaseHelper
    private lateinit var adapter: EventAdapter
    private lateinit var rvEvents: RecyclerView
    private lateinit var session: SessionManager

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
        private const val EXACT_ALARM_PERMISSION_REQUEST = 1002
        private var pendingEventForPermission: Event? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AgenDappDatabaseHelper(this)
        session = SessionManager(this)

        val user = session.getUser()
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        rvEvents = findViewById(R.id.rvEvents)
        val btnAdd = findViewById<ImageButton>(R.id.btnAddEvent)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 🔥 NUEVO: Referencia al botón del clima
        val btnClima = findViewById<Button>(R.id.btnClima)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        val genderWord = if (user.gender.lowercase() == "femenino") "¡Bienvenida" else "¡Bienvenido"
        tvWelcome.text = "$genderWord, ${user.name}!"

        rvEvents.layoutManager = LinearLayoutManager(this)

        adapter = EventAdapter(
            context = this,
            events = db.getEventsByUser(user.id), // ID como INT (SQLite)
            onEditClick = { editEvent(it) },
            onDeleteClick = { deleteEvent(it) },
            onRemindClick = { remindEvent(it) }
        )

        rvEvents.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }

        // 🔥 NUEVO: Lógica para abrir la pantalla del Clima
        btnClima.setOnClickListener {
            val intent = Intent(this, ClimaActivity::class.java)
            // Pasamos el ID como INT directo para que ClimaActivity lo reciba igual
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ... El resto del código se mantiene igual ...

    override fun onResume() {
        super.onResume()
        val user = session.getUser()
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        adapter.updateData(db.getEventsByUser(user.id))
    }

    private fun editEvent(event: Event) {
        val intent = Intent(this, EditEventActivity::class.java)
        intent.putExtra("eventId", event.id)
        startActivity(intent)
    }

    private fun deleteEvent(event: Event) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPasswordConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { alertDialog.dismiss() }

        btnConfirm.setOnClickListener {
            val user = session.getUser()
            val input = etPassword.text.toString().trim()

            if (user == null) {
                Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                return@setOnClickListener
            }

            if (user.password.isBlank()) {
                Toast.makeText(this, "Debes volver a iniciar sesión", Toast.LENGTH_SHORT).show()
                session.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (input == user.password) {
                db.deleteEvent(event.id)
                adapter.updateData(db.getEventsByUser(user.id))

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, ReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    event.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)

                alertDialog.dismiss()
                Toast.makeText(this, "Evento eliminado y recordatorio cancelado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }

    private fun remindEvent(event: Event) {
        pendingEventForPermission = event
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                    startActivityForResult(it, EXACT_ALARM_PERMISSION_REQUEST)
                }
                Toast.makeText(this, "Necesitamos permiso para alarmas exactas", Toast.LENGTH_LONG).show()
                return
            }
        }
        scheduleReminder(event)
    }

    private fun scheduleReminder(event: Event) {
        try {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("eventName", event.title)
                putExtra("eventDate", event.date)
                putExtra("eventTime", event.time ?: "00:00")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                event.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val eventDateTime = try {
                sdf.parse("${event.date} ${event.time ?: "00:00"}")
            } catch (e: Exception) { null }

            if (eventDateTime == null) {
                Toast.makeText(this, "Formato de fecha/hora inválido", Toast.LENGTH_SHORT).show()
                return
            }

            val now = Calendar.getInstance().time
            if (eventDateTime.before(now)) {
                Toast.makeText(this, "El evento ya pasó, no se puede poner recordatorio", Toast.LENGTH_SHORT).show()
                return
            }

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                eventDateTime.time,
                pendingIntent
            )

            Toast.makeText(this, "Recordatorio activado para '${event.title}'", Toast.LENGTH_SHORT).show()

        } catch (se: SecurityException) {
            se.printStackTrace()
            Toast.makeText(this, "Error de seguridad. ¿Permiso denegado?", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al activar recordatorio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pendingEventForPermission?.let { remindEvent(it) }
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                pendingEventForPermission = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EXACT_ALARM_PERMISSION_REQUEST) {
            pendingEventForPermission?.let { remindEvent(it) }
        }
    }
}