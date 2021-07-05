package adsingleton;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public final class onBackPressedInterstitial {

    public boolean showBackAd = false;

    private static volatile onBackPressedInterstitial backInterstitialInstance = null;
    private InterstitialAd backInterstitialAd = null;

    private onBackPressedInterstitial() {
    }

    public static onBackPressedInterstitial getInstance() {
        if (backInterstitialInstance == null) {
            synchronized (onBackPressedInterstitial.class) {
                if (backInterstitialInstance == null) {
                    backInterstitialInstance = new onBackPressedInterstitial();
                }
            }
        }
        return backInterstitialInstance;
    }

    public void makeShowBackAdTrue(){
        showBackAd = true;
    }

    public void makeShowBackAdFalse(){
        showBackAd = false;
    }

    public boolean isShowBackAd(){
        return showBackAd;
    }

    public void createAd(Context context) {

        Context backInterstitialContext = context.getApplicationContext();

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(backInterstitialContext, "ad-id", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                backInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                backInterstitialAd = null;
            }
        });

    }

    public void makeAdNull() {
        backInterstitialAd = null;
    }

    public InterstitialAd getInterstitialAd() {
        return backInterstitialAd;
    }

}
