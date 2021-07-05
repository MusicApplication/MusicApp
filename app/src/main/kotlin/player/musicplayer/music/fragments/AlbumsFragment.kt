package player.musicplayer.music.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.google.gson.Gson
import com.musicplayer.commons.adapters.MyRecyclerViewAdapter
import com.musicplayer.commons.extensions.beGoneIf
import com.musicplayer.commons.extensions.beVisibleIf
import com.musicplayer.commons.helpers.ensureBackgroundThread
import player.musicplayer.music.R
import player.musicplayer.music.activities.SimpleActivity
import player.musicplayer.music.activities.TracksActivity
import player.musicplayer.music.adapters.AlbumsAdapter
import player.musicplayer.music.dialogs.ChangeSortingDialog
import player.musicplayer.music.extensions.*
import player.musicplayer.music.helpers.ALBUM
import player.musicplayer.music.helpers.TAB_ALBUMS
import player.musicplayer.music.models.Album
import kotlinx.android.synthetic.main.fragment_albums.view.*

// Artists -> Albums -> Tracks
class AlbumsFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var albumsIgnoringSearch = ArrayList<Album>()

    override fun setupFragment(activity: SimpleActivity) {
        Album.sorting = context.config.albumSorting
        ensureBackgroundThread {
            val cachedAlbums = activity.albumsDAO.getAll() as ArrayList<Album>
            activity.runOnUiThread {
                gotAlbums(activity, cachedAlbums, true)

                ensureBackgroundThread {
                    val albums = ArrayList<Album>()

                    val artists = context.getArtistsSync()
                    artists.forEach { artist ->
                        albums.addAll(context.getAlbumsSync(artist))
                    }

                    gotAlbums(activity, albums, false)
                }
            }
        }
    }

    private fun gotAlbums(activity: SimpleActivity, albums: ArrayList<Album>, isFromCache: Boolean) {
        albums.sort()

        activity.runOnUiThread {
            albums_placeholder.text = context.getString(R.string.no_items_found)
            albums_placeholder.beVisibleIf(albums.isEmpty() && !isFromCache)

            val adapter = albums_list.adapter
            if (adapter == null) {
                AlbumsAdapter(activity, albums, albums_list, albums_fastscroller) {
                    Intent(activity, TracksActivity::class.java).apply {
                        putExtra(ALBUM, Gson().toJson(it))
                        activity.startActivity(this)
                    }
                }.apply {
                    albums_list.adapter = this
                }

                albums_list.scheduleLayoutAnimation()
                albums_fastscroller.setViews(albums_list) {
                    val album = (albums_list.adapter as AlbumsAdapter).albums.getOrNull(it)
                    albums_fastscroller.updateBubbleText(album?.getBubbleText() ?: "")
                }
            } else {
                val oldItems = (adapter as AlbumsAdapter).albums
                if (oldItems.sortedBy { it.id }.hashCode() != albums.sortedBy { it.id }.hashCode()) {
                    adapter.updateItems(albums)

                    ensureBackgroundThread {
                        albums.forEach {
                            context.albumsDAO.insert(it)
                        }

                        // remove deleted albums from cache
                        if (!isFromCache) {
                            val newIds = albums.map { it.id }
                            val idsToRemove = arrayListOf<Long>()
                            oldItems.forEach { album ->
                                if (!newIds.contains(album.id)) {
                                    idsToRemove.add(album.id)
                                }
                            }

                            idsToRemove.forEach {
                                activity.albumsDAO.deleteAlbum(it)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun finishActMode() {
        (albums_list.adapter as? MyRecyclerViewAdapter)?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = albumsIgnoringSearch.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Album>
        (albums_list.adapter as? AlbumsAdapter)?.updateItems(filtered, text)
        albums_placeholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchOpened() {
        albumsIgnoringSearch = (albums_list?.adapter as? AlbumsAdapter)?.albums ?: ArrayList()
    }

    override fun onSearchClosed() {
        (albums_list.adapter as? AlbumsAdapter)?.updateItems(albumsIgnoringSearch)
        albums_placeholder.beGoneIf(albumsIgnoringSearch.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_ALBUMS) {
            val adapter = albums_list.adapter as? AlbumsAdapter ?: return@ChangeSortingDialog
            val albums = adapter.albums
            Album.sorting = activity.config.albumSorting
            albums.sort()
            adapter.updateItems(albums, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        albums_fastscroller.updatePrimaryColor()
        albums_fastscroller.updateBubbleColors()
    }
}
