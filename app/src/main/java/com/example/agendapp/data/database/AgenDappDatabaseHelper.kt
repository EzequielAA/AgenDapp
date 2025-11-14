package com.example.agendapp.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.agendapp.data.model.Event
import com.example.agendapp.data.model.User
import java.io.File

class AgenDappDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "agendapp.db"
        private const val DATABASE_VERSION = 3

        // Tabla usuarios
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD = "password"
        private const val COL_GENDER = "gender"

        // Tabla eventos
        private const val TABLE_EVENTS = "events"
        private const val COL_EVENT_ID = "id"
        private const val COL_EVENT_USER_ID = "user_id"
        private const val COL_EVENT_TITLE = "title"
        private const val COL_EVENT_DATE = "date"
        private const val COL_EVENT_TIME = "time"
        private const val COL_EVENT_IMAGE = "image_path"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_GENDER TEXT NOT NULL
            )
        """.trimIndent()

        val createEventsTable = """
            CREATE TABLE $TABLE_EVENTS (
                $COL_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EVENT_USER_ID INTEGER NOT NULL,
                $COL_EVENT_TITLE TEXT NOT NULL,
                $COL_EVENT_DATE TEXT NOT NULL,
                $COL_EVENT_TIME TEXT,
                $COL_EVENT_IMAGE TEXT,
                FOREIGN KEY($COL_EVENT_USER_ID) REFERENCES $TABLE_USERS($COL_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createEventsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ========== USUARIOS ==========

    fun registerUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, user.name)
            put(COL_EMAIL, user.email)
            put(COL_PASSWORD, user.password)
            put(COL_GENDER, user.gender)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun loginUser(email: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COL_EMAIL=? AND $COL_PASSWORD=?",
            arrayOf(email, password),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
                gender = cursor.getString(cursor.getColumnIndexOrThrow(COL_GENDER))
            )
        }

        cursor.close()
        db.close()
        return user
    }

    // ========== EVENTOS ==========

    fun insertEvent(event: Event): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EVENT_USER_ID, event.userId)
            put(COL_EVENT_TITLE, event.title)
            put(COL_EVENT_DATE, event.date)
            put(COL_EVENT_TIME, event.time)
            put(COL_EVENT_IMAGE, event.imagePath ?: "")
        }
        val result = db.insert(TABLE_EVENTS, null, values)
        db.close()
        return result != -1L
    }

    fun getEventsByUser(userId: Int): List<Event> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EVENTS,
            null,
            "$COL_EVENT_USER_ID=?",
            arrayOf(userId.toString()),
            null, null,
            "$COL_EVENT_DATE ASC"
        )

        val events = mutableListOf<Event>()
        if (cursor.moveToFirst()) {
            do {
                var path = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_IMAGE))
                if (path.isNullOrBlank() || !File(path).exists()) {
                    path = null
                }

                events.add(
                    Event(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_USER_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TITLE)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE)),
                        time = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TIME)),
                        imagePath = path
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return events
    }

    fun getEventById(eventId: Int): Event? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EVENTS,
            null,
            "$COL_EVENT_ID=?",
            arrayOf(eventId.toString()),
            null, null, null
        )

        var event: Event? = null
        if (cursor.moveToFirst()) {
            var path = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_IMAGE))
            if (path.isNullOrBlank() || !File(path).exists()) {
                path = null
            }

            event = Event(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_USER_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TITLE)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE)),
                time = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TIME)),
                imagePath = path
            )
        }

        cursor.close()
        db.close()
        return event
    }

    fun updateEvent(event: Event): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EVENT_TITLE, event.title)
            put(COL_EVENT_DATE, event.date)
            put(COL_EVENT_TIME, event.time)
            put(COL_EVENT_IMAGE, event.imagePath ?: "")
        }

        val result = db.update(
            TABLE_EVENTS,
            values,
            "$COL_EVENT_ID=?",
            arrayOf(event.id.toString())
        )

        db.close()
        return result > 0
    }

    fun deleteEvent(eventId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_EVENTS, "$COL_EVENT_ID=?", arrayOf(eventId.toString()))
        db.close()
        return result > 0
    }
}
