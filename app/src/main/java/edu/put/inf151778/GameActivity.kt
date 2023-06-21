package edu.put.inf151778

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import edu.put.inf151778.R

class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val gameImage: ImageView = itemView.findViewById(R.id.gameImage)
    val gameName: TextView = itemView.findViewById(R.id.gameName)
    val gameDate: TextView = itemView.findViewById(R.id.date)
}

class GameAdapter(private val games: List<Game>) : RecyclerView.Adapter<GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_row_item, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.gameName.text = game.title
        holder.gameDate.text = game.date
        Picasso.get().load(game.image).into(holder.gameImage)
    }

    override fun getItemCount(): Int {
        return games.size
    }
}

class GameActivity : AppCompatActivity(){
    val dbHelper = DatabaseHelper(this)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.games)



        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val adapter = GameAdapter(getGames()) // gamesList to lista zawierająca dane gier
        recyclerView.adapter = adapter
    }

    fun getGames(): List<Game> {
        // Pobieranie gier z bazy danych
        val gamesList = dbHelper.getGames()

        // Wyświetlanie danych z bazy danych
        for (game in gamesList) {
            println(game.title)
            println(game.image)
            println(game.date)
        }

        return gamesList
    }

}