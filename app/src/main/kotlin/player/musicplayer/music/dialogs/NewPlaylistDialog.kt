package player.musicplayer.music.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.musicplayer.commons.extensions.setupDialogStuff
import com.musicplayer.commons.extensions.showKeyboard
import com.musicplayer.commons.extensions.toast
import com.musicplayer.commons.extensions.value
import com.musicplayer.commons.helpers.ensureBackgroundThread
import player.musicplayer.music.R
import player.musicplayer.music.extensions.getPlaylistIdWithTitle
import player.musicplayer.music.extensions.playlistDAO
import player.musicplayer.music.models.Playlist
import kotlinx.android.synthetic.main.dialog_new_playlist.view.*

class NewPlaylistDialog(val activity: Activity, var playlist: Playlist? = null, val callback: (playlistId: Int) -> Unit) {
    var isNewPlaylist = playlist == null

    init {
        if (playlist == null) {
            playlist = Playlist(0, "")
        }

        val view = activity.layoutInflater.inflate(R.layout.dialog_new_playlist, null).apply {
            new_playlist_title.setText(playlist!!.title)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    val dialogTitle = if (isNewPlaylist) R.string.create_new_playlist else R.string.rename_playlist
                    activity.setupDialogStuff(view, this, dialogTitle) {
                        showKeyboard(view.new_playlist_title)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val title = view.new_playlist_title.value
                            ensureBackgroundThread {
                                val playlistIdWithTitle = activity.getPlaylistIdWithTitle(title)
                                var isPlaylistTitleTaken = isNewPlaylist && playlistIdWithTitle != -1
                                if (!isPlaylistTitleTaken) {
                                    isPlaylistTitleTaken = !isNewPlaylist && playlist!!.id != playlistIdWithTitle && playlistIdWithTitle != -1
                                }

                                if (title.isEmpty()) {
                                    activity.toast(R.string.empty_name)
                                    return@ensureBackgroundThread
                                } else if (isPlaylistTitleTaken) {
                                    activity.toast(R.string.playlist_name_exists)
                                    return@ensureBackgroundThread
                                }

                                playlist!!.title = title

                                val eventTypeId = if (isNewPlaylist) {
                                    activity.playlistDAO.insert(playlist!!).toInt()
                                } else {
                                    activity.playlistDAO.update(playlist!!)
                                    playlist!!.id
                                }

                                if (eventTypeId != -1) {
                                    dismiss()
                                    callback(eventTypeId)
                                } else {
                                    activity.toast(R.string.unknown_error_occurred)
                                }
                            }
                        }
                    }
                }
    }
}
