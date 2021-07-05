package player.musicplayer.music.activities

import adsingleton.onBackPressedInterstitial
import android.os.Handler
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.musicplayer.commons.activities.BaseSimpleActivity
import player.musicplayer.music.R


open class SimpleActivity : BaseSimpleActivity() {

    override fun getAppIconIDs() = arrayListOf(
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher
    )

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && isTaskRoot) {
            finish()
            true
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                backInterstitial()
            }
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> backInterstitial()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun backInterstitial() {

        if (!onBackPressedInterstitial.getInstance().isShowBackAd){
            if (onBackPressedInterstitial.getInstance().getInterstitialAd() != null) {
                onBackPressedInterstitial.getInstance().getInterstitialAd()?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onBackPressedInterstitial.getInstance().makeAdNull()
                            Handler().postDelayed({
                                if (onBackPressedInterstitial.getInstance().getInterstitialAd() == null) {
                                    onBackPressedInterstitial.getInstance().createAd(this@SimpleActivity)
                                }
                            }, 540000)
                            Handler().postDelayed({
                                onBackPressedInterstitial.getInstance().makeShowBackAdFalse()
                            }, 600000)
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        onBackPressedInterstitial.getInstance().makeAdNull()
                    }
                    override fun onAdShowedFullScreenContent() {
                        onBackPressedInterstitial.getInstance().makeAdNull()
                    }
                }
                onBackPressedInterstitial.getInstance().getInterstitialAd()?.show(this)
            }
            onBackPressedInterstitial.getInstance().makeShowBackAdTrue()
        }

        finish()

    }

    override fun getAppLauncherName() = getString(R.string.app_launcher_name)

}
