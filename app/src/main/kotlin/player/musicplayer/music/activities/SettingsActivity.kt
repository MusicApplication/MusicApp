package player.musicplayer.music.activities

import android.os.Bundle
import android.view.Menu
import com.musicplayer.commons.dialogs.RadioGroupDialog
import com.musicplayer.commons.extensions.*
import com.musicplayer.commons.models.RadioItem
import kotlinx.android.synthetic.main.activity_settings.*
import player.musicplayer.music.R
import player.musicplayer.music.extensions.config
import player.musicplayer.music.extensions.sendIntent
import player.musicplayer.music.helpers.REFRESH_LIST
import player.musicplayer.music.helpers.SHOW_FILENAME_ALWAYS
import player.musicplayer.music.helpers.SHOW_FILENAME_IF_UNAVAILABLE
import player.musicplayer.music.helpers.SHOW_FILENAME_NEVER

class SettingsActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupSwapPrevNext()
        setupReplaceTitle()
        updateTextColors(settings_scrollview)
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupSwapPrevNext() {
        settings_swap_prev_next.isChecked = config.swapPrevNext
        settings_swap_prev_next_holder.setOnClickListener {
            settings_swap_prev_next.toggle()
            config.swapPrevNext = settings_swap_prev_next.isChecked
        }
    }

    private fun setupReplaceTitle() {
        settings_show_filename.text = getShowFilenameText()
        settings_show_filename_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(SHOW_FILENAME_NEVER, getString(R.string.never)),
                    RadioItem(SHOW_FILENAME_IF_UNAVAILABLE, getString(R.string.title_is_not_available)),
                    RadioItem(SHOW_FILENAME_ALWAYS, getString(R.string.always)))

            RadioGroupDialog(this@SettingsActivity, items, config.showFilename) {
                config.showFilename = it as Int
                settings_show_filename.text = getShowFilenameText()
                sendIntent(REFRESH_LIST)
            }
        }
    }

    private fun getShowFilenameText() = getString(when (config.showFilename) {
        SHOW_FILENAME_NEVER -> R.string.never
        SHOW_FILENAME_IF_UNAVAILABLE -> R.string.title_is_not_available
        else -> R.string.always
    })

}
