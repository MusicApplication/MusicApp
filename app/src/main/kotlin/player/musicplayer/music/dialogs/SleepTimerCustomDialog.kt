package player.musicplayer.music.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.musicplayer.commons.extensions.hideKeyboard
import com.musicplayer.commons.extensions.setupDialogStuff
import com.musicplayer.commons.extensions.showKeyboard
import com.musicplayer.commons.extensions.value
import player.musicplayer.music.R
import kotlinx.android.synthetic.main.dialog_custom_sleep_timer_picker.view.*

class SleepTimerCustomDialog(val activity: Activity, val callback: (seconds: Int) -> Unit) {
    private var dialog: AlertDialog
    private val view = activity.layoutInflater.inflate(R.layout.dialog_custom_sleep_timer_picker, null)

    init {
        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.sleep_timer) {
                        showKeyboard(view.dialog_custom_sleep_timer_value)
                    }
                }
    }

    private fun dialogConfirmed() {
        val value = view.dialog_custom_sleep_timer_value.value
        val minutes = Integer.valueOf(if (value.isEmpty()) "0" else value)
        callback(minutes * 60)
        activity.hideKeyboard()
        dialog.dismiss()
    }
}
