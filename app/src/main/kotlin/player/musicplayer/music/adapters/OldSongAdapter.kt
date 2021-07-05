package player.musicplayer.music.adapters

import android.content.Intent
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.musicplayer.commons.adapters.MyRecyclerViewAdapter
import com.musicplayer.commons.dialogs.PropertiesDialog
import com.musicplayer.commons.extensions.sharePathsIntent
import com.musicplayer.commons.views.FastScroller
import com.musicplayer.commons.views.MyRecyclerView
import player.musicplayer.music.BuildConfig
import player.musicplayer.music.R
import player.musicplayer.music.activities.SimpleActivity
import player.musicplayer.music.dialogs.EditDialog
import player.musicplayer.music.extensions.sendIntent
import player.musicplayer.music.helpers.EDIT
import player.musicplayer.music.helpers.EDITED_TRACK
import player.musicplayer.music.helpers.REFRESH_LIST
import player.musicplayer.music.models.Track
import player.musicplayer.music.services.MusicService

class OldSongAdapter(activity: SimpleActivity, var songs: ArrayList<Track>, val transparentView: View,
                     recyclerView: MyRecyclerView, fastScroller: FastScroller, itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick) {

    private val VIEW_TYPE_TRANSPARENT = 0
    private val VIEW_TYPE_NAVIGATION = 1
    private val VIEW_TYPE_ITEM = 2

    private var transparentViewHolder: TransparentViewHolder? = null

    private var navigationView: ViewGroup? = null
    private var navigationViewHolder: NavigationViewHolder? = null

    override fun getActionMenuId() = R.menu.cab

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRANSPARENT -> getTransparentViewHolder()
            VIEW_TYPE_NAVIGATION -> getNavigationViewHolder()
            else -> createViewHolder(R.layout.item_old_song, parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_TRANSPARENT
            1 -> VIEW_TYPE_NAVIGATION
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}

    override fun getItemCount() = songs.size

    private fun getItemWithKey(key: Int): Track? = songs.firstOrNull { it.path.hashCode() == key }

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_rename).isVisible = isOneItemSelected() && getSelectedSongs().firstOrNull()?.path?.startsWith("content://") != true
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_properties -> showProperties()
            R.id.cab_rename -> displayEditDialog()
            R.id.cab_share -> shareItems()
        }
    }

    override fun getSelectableItemCount() = songs.size

    override fun getIsItemSelectable(position: Int) = position >= 0

    override fun getItemSelectionKey(position: Int) = songs.getOrNull(position)?.path?.hashCode()

    override fun getItemKeyPosition(key: Int) = songs.indexOfFirst { it.path.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    private fun getTransparentViewHolder(): TransparentViewHolder {
        if (transparentViewHolder == null) {
            transparentViewHolder = TransparentViewHolder(transparentView)
        }

        return transparentViewHolder!!
    }

    private fun getNavigationViewHolder(): NavigationViewHolder {
        if (navigationView == null) {
            navigationView = activity.layoutInflater.inflate(R.layout.item_navigation, null) as ViewGroup
        }

        if (navigationViewHolder == null) {
            navigationViewHolder = NavigationViewHolder(navigationView!!)
        }

        return navigationViewHolder!!
    }

    private fun showProperties() {
        if (selectedKeys.size <= 1) {
            PropertiesDialog(activity, getFirstSelectedItemPath())
        } else {
            val paths = getSelectedSongs().map { it.path }
            PropertiesDialog(activity, paths)
        }
    }

    private fun displayEditDialog() {
        EditDialog(activity, getSelectedSongs().first()) {
            if (it == MusicService.mCurrTrack) {
                Intent(activity, MusicService::class.java).apply {
                    putExtra(EDITED_TRACK, it)
                    action = EDIT
                    activity.startService(this)
                }
            }

            activity.sendIntent(REFRESH_LIST)
            activity.runOnUiThread {
                finishActMode()
            }
        }
    }

    private fun shareItems() {
        val paths = getSelectedSongs().map { it.path } as ArrayList<String>
        activity.sharePathsIntent(paths, BuildConfig.APPLICATION_ID)
    }

    private fun getFirstSelectedItemPath() = getSelectedSongs().firstOrNull()?.path ?: ""

    private fun getSelectedSongs(): ArrayList<Track> {
        val selectedSongs = ArrayList<Track>(selectedKeys.size)
        selectedKeys.forEach {
            getItemWithKey(it)?.apply {
                selectedSongs.add(this)
            }
        }
        return selectedSongs
    }

    inner class TransparentViewHolder(view: View) : ViewHolder(view)

    inner class NavigationViewHolder(view: View) : ViewHolder(view)
}
