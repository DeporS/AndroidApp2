package edu.put.inf151778

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text
import android.app.AlertDialog
import android.content.DialogInterface



class MainActivity : AppCompatActivity() {

    val dbHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var gamesCountText = findViewById<TextView>(R.id.gamesCount)
        var nameText = findViewById<TextView>(R.id.nameText)
        var lastModifiedText = findViewById<TextView>(R.id.synchroDate)
        gamesCountText.text = ""
        nameText.text = ""
        lastModifiedText.text = ""

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val editor = sharedPreferences.edit()

        // Tworzenie AlertDialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Gotowe")
        alertDialogBuilder.setMessage("Dane zostały poprawnie pobrane!")
        alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            // Wywołanie funkcji po kliknięciu OK
            GetUser()
        }

        // Wyświetlanie AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        if (isFirstRun) {
            // Uruchom ekran konfiguracyjny
            val intent = Intent(this, SetupScreen::class.java)
            startActivity(intent)
        }

        var dropBut = findViewById<Button>(R.id.dropBut)
        var gameBut = findViewById<Button>(R.id.gamesBut)


        dropBut.setOnClickListener{
            // Tworzenie AlertDialog
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Potwierdzenie")
            alertDialogBuilder.setMessage("Czy na pewno chcesz usunąć aktualnego użytkownika z bazy danych?")
            alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                // Wywołanie funkcji po kliknięciu OK
                editor.putBoolean("isFirstRun", true)
                editor.apply()
                dbHelper.deleteAllGames()

                val intent = Intent(this, SetupScreen::class.java)
                startActivity(intent)
            }
            alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            // Wyświetlanie AlertDialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }


        gameBut.setOnClickListener{
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }


    }

    fun GetGames(){
        // Pobieranie gier z bazy danych
        val gamesList = dbHelper.getGames()

        // Wyświetlanie danych z bazy danych

        for (game in gamesList) {
            println(game.title)
            println(game.image)
            println(game.date)
        }
    }

    fun GetUser(){
        var gamesCountText = findViewById<TextView>(R.id.gamesCount)
        var nameText = findViewById<TextView>(R.id.nameText)
        var lastModifiedText = findViewById<TextView>(R.id.synchroDate)

        val user = dbHelper.getUser(1)

        if (user != null) {
            println(user.username)
            println(user.gameCount)
            println(user.lastModified)

            nameText.text = user.username
            gamesCountText.text = "Posiadanych gier i dodatków: " + user.gameCount
            lastModifiedText.text = "Ostatnia synchronizacja " + user.lastModified
        } else {
            println("Użytkownik o podanym identyfikatorze nie istnieje.")
        }
    }
}