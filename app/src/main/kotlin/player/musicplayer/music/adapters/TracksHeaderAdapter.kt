package player.musicplayer.music.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.musicplayer.commons.adapters.MyRecyclerViewAdapter
import com.musicplayer.commons.dialogs.ConfirmationDialog
import com.musicplayer.commons.extensions.beGone
import com.musicplayer.commons.extensions.beVisible
import com.musicplayer.commons.extensions.getColoredDrawableWithColor
import com.musicplayer.commons.extensions.getFormattedDuration
import com.musicplayer.commons.helpers.ensureBackgroundThread
import com.musicplayer.commons.views.FastScroller
import com.musicplayer.commons.views.MyRecyclerView
import player.musicplayer.music.R
import player.musicplayer.music.activities.SimpleActivity
import player.musicplayer.music.extensions.addTracksToPlaylist
import player.musicplayer.music.extensions.addTracksToQueue
import player.musicplayer.music.extensions.deleteTracks
import player.musicplayer.music.models.AlbumHeader
import player.musicplayer.music.models.ListItem
import player.musicplayer.music.models.Track
import kotlinx.android.synthetic.main.item_album_header.view.*
import kotlinx.android.synthetic.main.item_track.view.*
import java.util.*

class TracksHeaderAdapter(activity: SimpleActivity, val items: ArrayList<ListItem>, recyclerView: MyRecyclerView, fastScroller: FastScroller,
                          itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick) {

    private val ITEM_HEADER = 0
    private val ITEM_TRACK = 1

    private val placeholder = resources.getColoredDrawableWithColor(R.drawable.ic_headset, textColor)
    private val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_big).toInt()

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_tracks_header

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            ITEM_HEADER -> R.layout.item_album_header
            else -> R.layout.item_track
        }

        return createViewHolder(layout, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        val allowClicks = item !is AlbumHeader
        holder.bindView(item, allowClicks, allowClicks) { itemView, layoutPosition ->
            when (item) {
                is AlbumHeader -> setupHeader(itemView, item)
                else -> setupTrack(itemView, item as Track)
            }
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AlbumHeader -> ITEM_HEADER
            else -> ITEM_TRACK
        }
    }

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_add_to_playlist -> addToPlaylist()
            R.id.cab_add_to_queue -> addToQueue()
            R.id.cab_delete -> askConfirmDelete()
            R.id.cab_select_all -> selectAll()
        }
    }

    override fun getSelectableItemCount() = items.size - 1

    override fun getIsItemSelectable(position: Int) = position != 0

    override fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.hashCode()

    override fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    private fun addToPlaylist() {
        activity.addTracksToPlaylist(getSelectedTracks()) {
            finishActMode()
            notifyDataSetChanged()
        }
    }

    private fun addToQueue() {
        activity.addTracksToQueue(getSelectedTracks()) {
            finishActMode()
        }
    }

    private fun askConfirmDelete() {
        ConfirmationDialog(activity) {
            ensureBackgroundThread {
                val positions = ArrayList<Int>()
                val selectedTracks = getSelectedTracks()
                selectedTracks.forEach { track ->
                    val position = items.indexOfFirst { it is Track && it.mediaStoreId == track.mediaStoreId }
                    if (position != -1) {
                        positions.add(position)
                    }
                }

                activity.deleteTracks(selectedTracks) {
                    activity.runOnUiThread {
                        positions.sortDescending()
                        removeSelectedItems(positions)
                        positions.forEach {
                            items.removeAt(it)
                        }
                    }
                }
            }
        }
    }

    private fun getSelectedTracks(): List<Track> = items.filter { it is Track && selectedKeys.contains(it.hashCode()) }.toList() as List<Track>

    private fun setupTrack(view: View, track: Track) {
        view.apply {
            track_frame?.isSelected = selectedKeys.contains(track.hashCode())
            track_title.text = track.title

            arrayOf(track_id, track_title, track_duration).forEach {
                it.setTextColor(textColor)
            }

            track_duration.text = track.duration.getFormattedDuration()
            track_id.text = track.trackId.toString()
            track_image.beGone()
            track_id.beVisible()
        }
    }

    private fun setupHeader(view: View, header: AlbumHeader) {
        view.apply {
            album_title.text = header.title
            album_artist.text = header.artist

            val tracks = resources.getQuantityString(R.plurals.tracks_plural, header.trackCnt, header.trackCnt)
            var year = ""
            if (header.year != 0) {
                year = "${header.year} • "
            }

            album_meta.text = "$year$tracks • ${header.duration.getFormattedDuration(true)}"

            arrayOf(album_title, album_artist, album_meta).forEach {
                it.setTextColor(textColor)
            }

            val options = RequestOptions()
                .error(placeholder)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))

            Glide.with(activity)
                .load(header.coverArt)
                .apply(options)
                .into(findViewById(R.id.album_image))
        }
    }
}
