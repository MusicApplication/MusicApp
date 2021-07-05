package player.musicplayer.music

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import player.musicplayer.music.activities.SimpleActivity

class AboutAppActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        val textView = findViewById<TextView>(R.id.changelog_text_button)
        textView.setOnClickListener {
            val myIntent = Intent(this@AboutAppActivity, ChangelogAppActivity::class.java)
            this@AboutAppActivity.startActivity(myIntent)
        }

        val textView2 = findViewById<TextView>(R.id.licenses_text_button)
        textView2.setOnClickListener {
            val myIntent2 = Intent(this@AboutAppActivity, LicensesAppActivity::class.java)
            this@AboutAppActivity.startActivity(myIntent2)
        }

        val textView3 = findViewById<TextView>(R.id.sourcecode_text_button)
        textView3.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/musicapplication/musicapp"))
            startActivity(browserIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

}
