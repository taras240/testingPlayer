package com.example.practice

import android.content.Context
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import org.jsoup.Jsoup
import java.net.URL


val mediaPlayer = MediaPlayer()

class MainActivity : AppCompatActivity() {
    var request = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Log.d("my-tag", "html")

        val requestTextView = findViewById<TextInputEditText>(R.id.inputEditTextView)
        requestTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                request = requestTextView.text.toString()
                val searchMusicTask = MyAsyncTask()
                searchMusicTask.execute()

            }
            false
        }

        val url = "https://spaces.im/musicat/"

    }

    var ht = ""
    private var list = mutableListOf<String>()
    private val searchPrefix = "https://spaces.im/musicat/search/index/?Link_id=168667&T=28&sq="

    private inner class MyAsyncTask : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val html =
                getHtml("${searchPrefix}$request")
            ht = html
            return html
        }

        override fun onPostExecute(result: String?) {

            val listView = findViewById<ListView>(R.id.listView)
            val links = parseHtml(result!!)
            val adapter = MyAdapter(this@MainActivity, links)
            listView.adapter = adapter
        }
    }


    fun getHtml(url: String): String {

        val connection = URL(url).openConnection()

        connection.setRequestProperty("Content-Type", "text/html")
        connection.connect()
        val inputStream = connection.getInputStream()
        val html = inputStream.bufferedReader().readText()
        inputStream.close()

        return html
    }

    fun parseHtml(html: String): List<Song> {
        val doc = Jsoup.parse(html)
        val links = doc.select(".small_player1")
        val list = mutableListOf<Song>()
        for (i in 0 until links.size) {
//            val url = links.select(".")
            val songName = links[i].select("[itemprop=description]").attr("content")
            val songUrl = links[i].select("div div").attr("data-src") //data-duration="3:02"
            val songDuration = links[i].select("div div").attr("data-duration")
            val coverUrl = links[i].select("div div").attr("data-cover")

            val song = Song(songName, songDuration, songUrl, coverUrl)

            list.add(song)
        }
        return list
    }

}

class MyAdapter(private val context: Context, private val data: List<Song>) :
    BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.list_item,
            parent,
            false
        )

        val songNameTextView = view.findViewById<TextView>(R.id.songNameTextView)
        val urlTextView = view.findViewById<TextView>(R.id.urlTextView)
        val durationTextView = view.findViewById<TextView>(R.id.durationTextView)
        val coverImageView = view.findViewById<ImageView>(R.id.coverImageView)

        songNameTextView.text = data[position].songName
//        urlTextView.text = data[position].songUrl
        durationTextView.text = data[position].songDuration
        var coverUrl = data[position].songCoverUrl

        if (coverUrl == "") coverImageView.setImageResource(R.drawable.album)
        else Glide.with(context).load(coverUrl).into(coverImageView)

        view.setOnClickListener() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
            }

            mediaPlayer.setDataSource(data[position].songUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
            }

        }
        return view
    }

}

data class Song(
    val songName: String,
    val songDuration: String,
    val songUrl: String,
    val songCoverUrl: String
) {
}