package edu.put.inf151778

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date
import java.text.SimpleDateFormat

// BAZA DANYCH
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "games.db"
        private const val DATABASE_VERSION = 8

        private const val TABLE_NAME = "games"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_IMAGE = "image"
        private const val COLUMN_DATE = "date"

        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_GAME_COUNT = "game_count"
        private const val COLUMN_LAST_MODIFIED = "last_modified"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_IMAGE TEXT, $COLUMN_DATE TEXT)"
        db.execSQL(createTableQuery)

        val createUsersTableQuery = "CREATE TABLE $TABLE_USERS ($COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_GAME_COUNT INTEGER, $COLUMN_LAST_MODIFIED TEXT)"
        db.execSQL(createUsersTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db.execSQL(dropTableQuery)

        val dropUsersTableQuery = "DROP TABLE IF EXISTS $TABLE_USERS"
        db.execSQL(dropUsersTableQuery)

        onCreate(db)
    }

    fun insertGame(game: Game): Long {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, game.title)
            put(COLUMN_IMAGE, game.image)
            put(COLUMN_DATE, game.date)
        }
        return writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun insertUser(user: User): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_GAME_COUNT, user.gameCount)
            put(COLUMN_LAST_MODIFIED, user.lastModified)
        }
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    @SuppressLint("Range")
    fun getGames(): List<Game> {
        val gamesList = mutableListOf<Game>()

        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = readableDatabase.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
            val image = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE))
            val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))

            val game = Game(title, image, date)
            gamesList.add(game)
        }

        cursor.close()

        return gamesList
    }

    @SuppressLint("Range")
    fun getUser(userId: Int): User? {
        val userList = mutableListOf<User>()

        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))

        while (cursor.moveToNext()) {
            val username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME))
            val gameCount = cursor.getInt(cursor.getColumnIndex(COLUMN_GAME_COUNT))
            val lastModified = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MODIFIED))

            val user = User(username, gameCount, lastModified)
            userList.add(user)
        }

        cursor.close()

        return userList.firstOrNull()
    }


    fun deleteAllGames() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
        println("Pomyslnie usunieto zawartosc bazy danych")
    }
}

// KLASA GRY
data class Game(val title: String, val image: String, val date: String)

// KLASA USERA
data class User(val username: String, val gameCount: Int, val lastModified: String)

val mainAct = MainActivity()



class SetupScreen : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_screen)

        databaseHelper = DatabaseHelper(this)

        val button = findViewById<Button>(R.id.downloadBut)
        val nickEditText = findViewById<EditText>(R.id.nickText)


        val objectsList = mutableListOf<Game>()



        button.setOnClickListener {
            button.isEnabled = false
            val nick = nickEditText.text.toString()
            try {
                Thread {
                    val url = "https://boardgamegeek.com/xmlapi2/collection?username=$nick"
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    val xpp = factory.newPullParser()

                    var count = 0

                    val inputStream = URL(url).openStream()
                    xpp.setInput(inputStream, null)



                    var eventType = xpp.eventType
                    var currentTag: String? = null
                    var gameName: String? = null
                    var gameYearPublished: String? = null
                    var gameThumbnail: String? = null
                    var isParsingNameTag = false


                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                //Log.d("Tag", xpp.name)
                                currentTag = xpp.name
                                if (currentTag != "item") {
                                    isParsingNameTag = true
                                }
                            }
                            XmlPullParser.TEXT -> {
                                if (isParsingNameTag) {
                                    when (currentTag) {
                                        "name" -> {
                                            gameName = xpp.text
                                            //println("Game Name: $gameName")
                                        }
                                        "yearpublished" -> {
                                            gameYearPublished = xpp.text
                                            //println("Year Published: $gameYearPublished")
                                        }
                                        "thumbnail" -> {
                                            gameThumbnail = xpp.text
                                            //println("Thumbnail: $gameThumbnail")
                                        }

                                    }
                                    if(gameName != null && gameThumbnail != null && gameYearPublished != null){
                                        val obj = Game(gameName.toString(), gameThumbnail.toString(), gameYearPublished.toString())
                                        objectsList.add(obj)
                                        gameName = null
                                        gameThumbnail = null
                                        gameYearPublished = null
                                        count += 1
                                    }


                                }

                            }
                            XmlPullParser.END_TAG -> {
                                if (currentTag != "item") {
                                    isParsingNameTag = false
                                } else if (xpp.name == "item") {

                                }
                            }
                        }
                        eventType = xpp.next()
                    }



                    for (i in objectsList) {
                        val insertedRowId = databaseHelper.insertGame(i)
                        Log.d("Database", "Inserted row id: $insertedRowId")
                    }

                    val user = User(nick, count, getCurrentDate().toString())
                    val userId = databaseHelper.insertUser(user)

                    inputStream.close()

                    // Wyświetlanie komunikatu o sukcesie w AlertDialog
                    runOnUiThread {



                        val alertDialog = AlertDialog.Builder(this)
                            .setTitle("Sukces")
                            .setMessage("Pobieranie danych o użytkowniku zakończone sukcesem.")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                // Dodatkowe działania po kliknięciu przycisku OK, jeśli potrzebne
                                downloadSuccess()
                            }
                            .create()

                        alertDialog.show()
                    }
                }.start()



            } catch (e: Exception) {
                e.printStackTrace()

                // Obsługa błędu - wyświetlanie komunikatu o błędzie w AlertDialog
                runOnUiThread {
                    val errorMessage = "Wystąpił błąd podczas parsowania XML: ${e.message}"
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Błąd")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            // Dodatkowe działania po kliknięciu przycisku OK, jeśli potrzebne
                        }
                        .create()

                    alertDialog.show()
                }
            }
        }



    }

    fun downloadSuccess(){
        // Zmiana wartosc isFirstRun
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstRun", false)
        editor.apply()

        //mainAct.GetGames()

        finish()
    }

}