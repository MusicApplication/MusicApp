package adsingleton;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public final class inAppMusicPauseInterstitial {

    public boolean showPauseAd = false;

    private static volatile inAppMusicPauseInterstitial pauseInterstitialInstance = null;
    private InterstitialAd pauseInterstitialAd = null;

    private inAppMusicPauseInterstitial() {
    }

    public static inAppMusicPauseInterstitial getInstance() {
        if (pauseInterstitialInstance == null) {
            synchronized (inAppMusicPauseInterstitial.class) {
                if (pauseInterstitialInstance == null) {
                    pauseInterstitialInstance = new inAppMusicPauseInterstitial();
                }
            }
        }
        return pauseInterstitialInstance;
    }

    public void makeShowPauseAdTrue(){
        showPauseAd = true;
    }

    public void makeShowPauseAdFalse(){
        showPauseAd = false;
    }

    public boolean isShowPauseAd(){
        return showPauseAd;
    }

    public void createAd(Context context) {

        Context pauseInterstitialContext = context.getApplicationContext();

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(pauseInterstitialContext, "ad-id", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                pauseInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                pauseInterstitialAd = null;
            }
        });

    }

    public void makeAdNull() {
        pauseInterstitialAd = null;
    }

    public InterstitialAd getInterstitialAd() {
        return pauseInterstitialAd;
    }

}
