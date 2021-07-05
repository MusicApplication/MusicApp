package player.musicplayer.music.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.KeyEvent
import player.musicplayer.music.extensions.config
import player.musicplayer.music.extensions.sendIntent
import player.musicplayer.music.helpers.NEXT
import player.musicplayer.music.helpers.PLAYPAUSE
import player.musicplayer.music.helpers.PREVIOUS

class RemoteControlReceiver : BroadcastReceiver() {

    companion object {
        private const val MAX_CLICK_DURATION = 700

        private var mContext: Context? = null
        private val mHandler = Handler()

        private var mClicksCnt = 0

        private val runnable = Runnable {
            if (mClicksCnt == 0)
                return@Runnable

            mContext!!.sendIntent(
                    when (mClicksCnt) {
                        1 -> PLAYPAUSE
                        2 -> NEXT
                        else -> PREVIOUS
                    }
            )
            mClicksCnt = 0
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        if (intent.action == Intent.ACTION_MEDIA_BUTTON) {
            val swapPrevNext = context.config.swapPrevNext
            val intentNext = if (swapPrevNext) PREVIOUS else NEXT
            val intentPrevious = if (swapPrevNext) NEXT else PREVIOUS
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return
            if (event.action == KeyEvent.ACTION_UP) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> context.sendIntent(PLAYPAUSE)
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> context.sendIntent(intentPrevious)
                    KeyEvent.KEYCODE_MEDIA_NEXT -> context.sendIntent(intentNext)
                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        mClicksCnt++

                        mHandler.removeCallbacks(runnable)
                        if (mClicksCnt >= 3) {
                            mHandler.post(runnable)
                        } else {
                            mHandler.postDelayed(runnable, MAX_CLICK_DURATION.toLong())
                        }
                    }
                }
            }
        }
    }
}
