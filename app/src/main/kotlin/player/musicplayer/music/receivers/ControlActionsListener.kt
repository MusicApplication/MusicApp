package player.musicplayer.music.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import player.musicplayer.music.extensions.sendIntent
import player.musicplayer.music.helpers.FINISH
import player.musicplayer.music.helpers.NEXT
import player.musicplayer.music.helpers.PLAYPAUSE
import player.musicplayer.music.helpers.PREVIOUS

class ControlActionsListener : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            PREVIOUS, PLAYPAUSE, NEXT, FINISH -> context.sendIntent(action)
        }
    }
}
