package player.musicplayer.music.activities

import adsingleton.firstCreateAd
import adsingleton.inAppMusicPauseInterstitial
import adsingleton.onBackPressedInterstitial
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.viewpager.widget.ViewPager
import com.musicplayer.commons.dialogs.FilePickerDialog
import com.musicplayer.commons.dialogs.RadioGroupDialog
import com.musicplayer.commons.extensions.*
import com.musicplayer.commons.helpers.PERMISSION_WRITE_STORAGE
import com.musicplayer.commons.helpers.ensureBackgroundThread
import com.musicplayer.commons.models.RadioItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_albums.*
import kotlinx.android.synthetic.main.fragment_artists.*
import kotlinx.android.synthetic.main.fragment_playlists.*
import kotlinx.android.synthetic.main.fragment_tracks.*
import kotlinx.android.synthetic.main.view_current_track_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import player.musicplayer.music.AboutAppActivity
import player.musicplayer.music.BuildConfig
import player.musicplayer.music.R
import player.musicplayer.music.adapters.ViewPagerAdapter
import player.musicplayer.music.dialogs.NewPlaylistDialog
import player.musicplayer.music.dialogs.SleepTimerCustomDialog
import player.musicplayer.music.extensions.*
import player.musicplayer.music.fragments.MyViewPagerFragment
import player.musicplayer.music.helpers.INIT_QUEUE
import player.musicplayer.music.helpers.START_SLEEP_TIMER
import player.musicplayer.music.helpers.STOP_SLEEP_TIMER
import player.musicplayer.music.models.Events
import player.musicplayer.music.services.MusicService

class MainActivity : SimpleActivity() {
    private var isSearchOpen = false
    private var searchMenuItem: MenuItem? = null
    private var bus: EventBus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                initActivity()
            } else {
                toast(R.string.no_storage_permissions)
                finish()
            }
        }

        volumeControlStream = AudioManager.STREAM_MUSIC
        checkAppOnSDCard()

        createInterstitials()
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(main_holder)
        sleep_timer_holder.background = ColorDrawable(config.backgroundColor)
        sleep_timer_stop.applyColorFilter(config.textColor)
        updateCurrentTrackBar()

        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        main_tabs_holder.apply {
            setTabTextColors(config.textColor, adjustedPrimaryColor)
            setSelectedTabIndicatorColor(adjustedPrimaryColor)
        }

        getAllFragments().forEach {
            it?.setupColors(config.textColor, adjustedPrimaryColor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.unregister(this)
        config.lastUsedViewPagerPage = viewpager.currentItem
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        setupSearch(menu)
        updateMenuItemColors(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.apply {
            findItem(R.id.create_new_playlist).isVisible = getCurrentFragment() == playlists_fragment_holder
            findItem(R.id.create_playlist_from_folder).isVisible = getCurrentFragment() == playlists_fragment_holder
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> showSortingDialog()
            R.id.sleep_timer -> showSleepTimer()
            R.id.create_new_playlist -> createNewPlaylist()
            R.id.create_playlist_from_folder -> createPlaylistFromFolder()
            R.id.equalizer -> launchEqualizer()
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun createInterstitials(){

        if (!firstCreateAd.getInstance().isCreateBackInterstitial) {
            if (onBackPressedInterstitial.getInstance().interstitialAd == null) {
                onBackPressedInterstitial.getInstance().createAd(this)
            }
            firstCreateAd.getInstance().makeCreateBackInterstitialTrue()
        }

        if (!firstCreateAd.getInstance().isCreatePauseInterstitial) {
            if (inAppMusicPauseInterstitial.getInstance().interstitialAd == null) {
                inAppMusicPauseInterstitial.getInstance().createAd(this)
            }
            firstCreateAd.getInstance().makeCreatePauseInterstitialTrue()
        }

    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        getCurrentFragment()?.onSearchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                getCurrentFragment()?.onSearchOpened()
                isSearchOpen = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                getCurrentFragment()?.onSearchClosed()
                isSearchOpen = false
                return true
            }
        })
    }

    private fun initActivity() {
        bus = EventBus.getDefault()
        bus!!.register(this)
        initFragments()
        sleep_timer_stop.setOnClickListener { stopSleepTimer() }

        current_track_bar.setOnClickListener {
            Intent(this, TrackActivity::class.java).apply {
                startActivity(this)
            }
        }

        if (MusicService.mCurrTrack == null) {
            ensureBackgroundThread {
                if (queueDAO.getAll().isNotEmpty()) {
                    sendIntent(INIT_QUEUE)
                }
            }
        }
    }

    private fun initFragments() {
        viewpager.adapter = ViewPagerAdapter(this)
        viewpager.offscreenPageLimit = 3
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                if (isSearchOpen) {
                    searchMenuItem?.collapseActionView()
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                main_tabs_holder.getTabAt(position)?.select()
                getAllFragments().forEach {
                    it?.finishActMode()
                }
            }
        })

        main_tabs_holder.onTabSelectionChanged(
            tabSelectedAction = {
                viewpager.currentItem = it.position
            }
        )

        val tabLabels = arrayOf(getString(R.string.playlists), getString(R.string.artists), getString(R.string.albums), getString(R.string.tracks))
        main_tabs_holder.apply {
            removeAllTabs()

            for (i in tabLabels.indices) {
                val tab = newTab().setText(tabLabels[i])
                addTab(tab, i, i == 0)
            }
        }

        viewpager.currentItem = config.lastUsedViewPagerPage
    }

    private fun getCurrentFragment(): MyViewPagerFragment? {
        return when (viewpager.currentItem) {
            0 -> playlists_fragment_holder
            1 -> artists_fragment_holder
            2 -> albums_fragment_holder
            else -> tracks_fragment_holder
        }
    }

    private fun showSortingDialog() {
        getCurrentFragment()?.onSortOpen(this)
    }

    private fun updateCurrentTrackBar() {
        current_track_bar.updateColors()
        current_track_bar.updateCurrentTrack(MusicService.mCurrTrack)
        current_track_bar.updateTrackState()
        checkSleepTimerPosition()
    }

    private fun createNewPlaylist() {
        NewPlaylistDialog(this) {
            EventBus.getDefault().post(Events.PlaylistsUpdated())
        }
    }

    private fun createPlaylistFromFolder() {
        FilePickerDialog(this, pickFile = false) {
            createPlaylistFrom(it)
        }
    }

    private fun createPlaylistFrom(path: String) {
        ensureBackgroundThread {
            getFolderTracks(path, true) { tracks ->
                runOnUiThread {
                    NewPlaylistDialog(this) { playlistId ->
                        tracks.forEach {
                            it.playListId = playlistId
                        }

                        ensureBackgroundThread {
                            tracksDAO.insertAll(tracks)
                            EventBus.getDefault().post(Events.PlaylistsUpdated())
                        }
                    }
                }
            }
        }
    }

    private fun showSleepTimer() {
        val minutes = getString(R.string.minutes_raw)
        val hour = resources.getQuantityString(R.plurals.hours, 1, 1)

        val items = arrayListOf(
            RadioItem(5 * 60, "5 $minutes"),
            RadioItem(10 * 60, "10 $minutes"),
            RadioItem(20 * 60, "20 $minutes"),
            RadioItem(30 * 60, "30 $minutes"),
            RadioItem(60 * 60, hour))

        if (items.none { it.id == config.lastSleepTimerSeconds }) {
            val lastSleepTimerMinutes = config.lastSleepTimerSeconds / 60
            val text = resources.getQuantityString(R.plurals.minutes, lastSleepTimerMinutes, lastSleepTimerMinutes)
            items.add(RadioItem(config.lastSleepTimerSeconds, text))
        }

        items.sortBy { it.id }
        items.add(RadioItem(-1, getString(R.string.custom)))

        RadioGroupDialog(this, items, config.lastSleepTimerSeconds) {
            if (it as Int == -1) {
                SleepTimerCustomDialog(this) {
                    if (it > 0) {
                        pickedSleepTimer(it)
                    }
                }
            } else if (it > 0) {
                pickedSleepTimer(it)
            }
        }
    }

    private fun pickedSleepTimer(seconds: Int) {
        config.lastSleepTimerSeconds = seconds
        config.sleepInTS = System.currentTimeMillis() + seconds * 1000
        startSleepTimer()
    }

    private fun startSleepTimer() {
        checkSleepTimerPosition()
        sleep_timer_holder.fadeIn()
        sendIntent(START_SLEEP_TIMER)
    }

    private fun stopSleepTimer() {
        sendIntent(STOP_SLEEP_TIMER)
        sleep_timer_holder.fadeOut()
    }

    private fun checkSleepTimerPosition() {
        if (current_track_bar.isVisible()) {
            (sleep_timer_holder.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        } else {
            (sleep_timer_holder.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        }
    }

    private fun getAllFragments() = arrayListOf(playlists_fragment_holder, artists_fragment_holder, albums_fragment_holder, tracks_fragment_holder)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun trackChangedEvent(event: Events.TrackChanged) {
        current_track_bar.updateCurrentTrack(event.track)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun trackStateChanged(event: Events.TrackStateChanged) {
        current_track_bar.updateTrackState()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun noStoragePermission(event: Events.NoStoragePermission) {
        toast(R.string.no_storage_permissions)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun sleepTimerChanged(event: Events.SleepTimerChanged) {
        sleep_timer_value.text = event.seconds.getFormattedDuration()
        sleep_timer_holder.beVisible()

        if (event.seconds == 0) {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun playlistsUpdated(event: Events.PlaylistsUpdated) {
        playlists_fragment_holder?.setupFragment(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun trackDeleted(event: Events.TrackDeleted) {
        getAllFragments().forEach {
            it.setupFragment(this)
        }
    }

    private fun launchEqualizer() {
        startActivity(Intent(applicationContext, EqualizerActivity::class.java))
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        startActivity(Intent(applicationContext, AboutAppActivity::class.java))
    }

}
