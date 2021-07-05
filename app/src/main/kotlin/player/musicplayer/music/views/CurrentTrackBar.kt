package player.musicplayer.music.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.musicplayer.commons.extensions.fadeIn
import com.musicplayer.commons.extensions.fadeOut
import com.musicplayer.commons.extensions.getColoredDrawableWithColor
import player.musicplayer.music.R
import player.musicplayer.music.extensions.config
import player.musicplayer.music.models.Track
import kotlinx.android.synthetic.main.view_current_track_bar.view.*

class CurrentTrackBar(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {
    fun updateColors() {
        background = ColorDrawable(context.config.backgroundColor)
        current_track_label.setTextColor(context.config.textColor)
        current_track_play_pause.visibility = View.INVISIBLE
    }

    fun updateCurrentTrack(track: Track?) {
        if (track == null) {
            fadeOut()
            return
        } else {
            fadeIn()
        }

        val artist = if (track.artist.trim().isNotEmpty() && track.artist != MediaStore.UNKNOWN_STRING) {
            " • ${track.artist}"
        } else {
            ""
        }

        current_track_label.text = "${track.title}$artist"
        val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_small).toInt()
        val currentTrackPlaceholder = resources.getColoredDrawableWithColor(R.drawable.ic_headset, context.config.textColor)
        val options = RequestOptions()
            .error(currentTrackPlaceholder)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))

        Glide.with(this)
            .load(track.coverArt)
            .apply(options)
            .into(findViewById(R.id.current_track_image))
    }

    fun updateTrackState() {
        current_track_play_pause.visibility = View.INVISIBLE
    }
}
