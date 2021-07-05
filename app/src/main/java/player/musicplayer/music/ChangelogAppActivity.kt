package player.musicplayer.music

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import player.musicplayer.music.activities.SimpleActivity

class ChangelogAppActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changelog_app)
        val textviewChangeLog = findViewById<TextView>(R.id.changelog_text)
        textviewChangeLog.text = "Music App 15.0.0 - A lot of changes are made like removing some settings, color changes, recreating about activity, Admob SDK integration, Onesignal SDK integration. All other miner changes are available in git repository."
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

}
