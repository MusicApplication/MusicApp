package adsingleton;

public final class firstCreateAd {

    public boolean createBackInterstitial = false;
    public boolean createPauseInterstitial = false;

    private static volatile firstCreateAd firstCreateAdInstance = null;

    private firstCreateAd() {}

    public static firstCreateAd getInstance() {
        if (firstCreateAdInstance == null) {
            synchronized (firstCreateAd.class) {
                if (firstCreateAdInstance == null) {
                    firstCreateAdInstance = new firstCreateAd();
                }
            }
        }
        return firstCreateAdInstance;
    }

    public void makeCreateBackInterstitialTrue(){
        createBackInterstitial = true;
    }
    public boolean isCreateBackInterstitial(){
        return createBackInterstitial;
    }

    public void makeCreatePauseInterstitialTrue(){
        createPauseInterstitial = true;
    }
    public boolean isCreatePauseInterstitial(){
        return createPauseInterstitial;
    }

}
