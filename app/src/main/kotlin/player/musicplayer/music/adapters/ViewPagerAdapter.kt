package player.musicplayer.music.adapters

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.musicplayer.commons.extensions.getAdjustedPrimaryColor
import player.musicplayer.music.R
import player.musicplayer.music.activities.SimpleActivity
import player.musicplayer.music.extensions.config
import player.musicplayer.music.fragments.MyViewPagerFragment

class ViewPagerAdapter(val activity: SimpleActivity) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = getFragment(position)
        val view = activity.layoutInflater.inflate(layout, container, false)
        container.addView(view)

        (view as MyViewPagerFragment).apply {
            setupFragment(activity)
            setupColors(activity.config.textColor, activity.getAdjustedPrimaryColor())
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    override fun getCount() = 4

    override fun isViewFromObject(view: View, item: Any) = view == item

    private fun getFragment(position: Int) = when (position) {
        0 -> R.layout.fragment_playlists
        1 -> R.layout.fragment_artists
        2 -> R.layout.fragment_albums
        else -> R.layout.fragment_tracks
    }
}
