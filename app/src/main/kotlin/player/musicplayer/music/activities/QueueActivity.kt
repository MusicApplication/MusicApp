package player.musicplayer.music.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.musicplayer.commons.helpers.ensureBackgroundThread
import player.musicplayer.music.R
import player.musicplayer.music.adapters.QueueAdapter
import player.musicplayer.music.dialogs.NewPlaylistDialog
import player.musicplayer.music.helpers.PLAY_TRACK
import player.musicplayer.music.helpers.RoomHelper
import player.musicplayer.music.helpers.TRACK_ID
import player.musicplayer.music.models.Events
import player.musicplayer.music.models.Track
import player.musicplayer.music.services.MusicService
import kotlinx.android.synthetic.main.activity_queue.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class QueueActivity : SimpleActivity() {
    private var bus: EventBus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)
        bus = EventBus.getDefault()
        bus!!.register(this)
        setupAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_queue, menu)
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_playlist_from_queue -> createPlaylistFromQueue()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setupAdapter() {
        val adapter = queue_list.adapter
        if (adapter == null) {
            val queueAdapter = QueueAdapter(this, MusicService.mTracks, queue_list, queue_fastscroller) {
                Intent(this, MusicService::class.java).apply {
                    action = PLAY_TRACK
                    putExtra(TRACK_ID, (it as Track).mediaStoreId)
                    startService(this)
                }
            }.apply {
                queue_list.adapter = this
            }

            queue_list.scheduleLayoutAnimation()
            queue_fastscroller.setViews(queue_list) {
                val track = queueAdapter.items.getOrNull(it)
                queue_fastscroller.updateBubbleText(track?.title ?: "")
            }
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    private fun createPlaylistFromQueue() {
        NewPlaylistDialog(this) { newPlaylistId ->
            val tracks = ArrayList<Track>()
            (queue_list.adapter as? QueueAdapter)?.items?.forEach {
                it.playListId = newPlaylistId
                tracks.add(it)
            }

            ensureBackgroundThread {
                RoomHelper(this).insertTracksWithPlaylist(tracks)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun trackChangedEvent(event: Events.TrackChanged) {
        setupAdapter()
    }
}
