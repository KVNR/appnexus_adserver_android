package com.example.kgast.appnexustestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Button;
import android.os.Handler;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;


// BEGIN : Banners/Interstitials imports
import com.appnexus.opensdk.*;
// END : Banners/Interstitials imports

// BEGIN : Native imports
import android.widget.ImageView;
import android.widget.TextView;
// END : Native imports

// BEGIN : Video imports
import android.widget.VideoView;
import com.appnexus.opensdk.ResultCode;
import com.appnexus.opensdk.instreamvideo.Quartile;
import com.appnexus.opensdk.instreamvideo.VideoAd;
import com.appnexus.opensdk.instreamvideo.VideoAdLoadListener;
import com.appnexus.opensdk.instreamvideo.VideoAdPlaybackListener;
// END : Video imports


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdListener
{
    private EditText et_placementId = null;
    private EditText et_placementSize = null;
    private Button b_loadAd = null;
    private Spinner s_adType = null;
    private BannerAdView bav = null;
    private InterstitialAdView iav = null;

    public static final String TAG = MainActivity.class.getName();

    // BEGIN : Video placement
    // The Ad Video instance.
    // Its important to create this as a Instance variable to make sure its not removed Garbage Collected.
    private VideoAd videoAd = null;
    // Content video player.
    private static VideoView videoPlayer;
    // Ad Container.
    private RelativeLayout videoContainer;
    // END : Video placement

    // BEGIN : Native placement
    private LinearLayout nativeContainer;
    private NativeAdResponse nr = null;
    // END : Native placement

    protected MainActivity getActivity()
    {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_loadAd = (Button) findViewById(R.id.buttonLoad);
        b_loadAd.setOnClickListener(this);

        s_adType = (Spinner) findViewById(R.id.spinnerAdType);
        s_adType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                String adType = s_adType.getSelectedItem().toString();

                et_placementSize = (EditText) findViewById(R.id.editTextPlacementSize);
                if (adType.equals("Interstitial") || adType.equals("Video") || adType.equals("Native"))
                {
                    et_placementSize.setVisibility(View.GONE);
                }
                else
                {
                    et_placementSize.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub

            }
        });
    }

    private void loadBanner()
    {
        et_placementId = (EditText) findViewById(R.id.editTextPlacementId);
        et_placementSize = (EditText) findViewById(R.id.editTextPlacementSize);


        // Create the AdView and set its placement ID.  Tweak a few other settings.
        bav = new BannerAdView(this);
        bav.setPlacementID(et_placementId.getText().toString());

        String size = et_placementSize.getText().toString();
        int width = 0;
        int height = 0;

        if (size.contains("x")) {
            width = Integer.parseInt(size.split("x")[0]);
            height = Integer.parseInt(size.split("x")[1]);

        }

        bav.setAdSize(width, height);
        bav.setAutoRefreshInterval(100000); // Set to 0 to disable auto-refresh
        bav.setShouldServePSAs(false);
        bav.setOpensNativeBrowser(true);

        LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        lp.setMargins(0,10,0,0);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        bav.setLayoutParams(lp);

        FrameLayout adContainer = (FrameLayout)findViewById(R.id.adContainer);
        adContainer.addView(bav);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bav.loadAd();
            }
        }, 0);
    }

    private void loadInterstitial()
    {
        et_placementId = (EditText) findViewById(R.id.editTextPlacementId);

        // Set up an ad view with our placement ID.
        iav = new InterstitialAdView(this);
        iav.setPlacementID(et_placementId.getText().toString());
        iav.setAdListener(this);

        // Fetch an ad from the server.  If this works, `onAdLoaded' will
        // be called, and we can show the ad.
        iav.loadAd();
    }

    private void loadNative()
    {
        et_placementId = (EditText) findViewById(R.id.editTextPlacementId);

        // Create a NativeAdRequest object
        NativeAdRequest adRequest = new NativeAdRequest(this, et_placementId.getText().toString()); // Placement ID

        // Create a listener for ad events
        NativeAdEventListener adEventListener = new
                NativeAdEventListener()
                {
                    @Override
                    public void onAdWasClicked()
                    {
                        // Do something when the view is clicked
                    }

                    @Override
                    public void onAdWillLeaveApplication()
                    {
                        // Do something when the ad is taking user away from current app
                    }
                };

        // Whether to pre-load the native ad's icon and main image
        adRequest.shouldLoadIcon(true);
        adRequest.shouldLoadImage(true);

        adRequest.setListener(new NativeAdRequestListener()
        {

            @Override
            public void onAdLoaded(NativeAdResponse response)
            {
                // Cover image
                ImageView nativeImage = new ImageView(getActivity());
                nativeImage.setImageBitmap(response.getImage());

                // Icon image
                ImageView nativeIcon = new ImageView(getActivity());
                nativeIcon.setImageBitmap(response.getIcon());

                // Title
                TextView nativeTitle = new TextView(getActivity());
                nativeTitle.setText(response.getTitle());

                // Main text
                TextView nativeDescription = new TextView(getActivity());
                nativeDescription.setText(response.getDescription());

                // Text that indicates a call to action -- for example, to install an app
                TextView nativeCallToAction = new TextView(getActivity());
                nativeCallToAction.setText(response.getCallToAction());

                nativeContainer = new LinearLayout(getActivity());
                nativeContainer.setOrientation(LinearLayout.VERTICAL);
                nativeContainer.addView(nativeTitle);
                nativeContainer.addView(nativeImage);
                nativeContainer.addView(nativeDescription);
                nativeContainer.addView(nativeCallToAction);

                // Create a container (a parent view that holds all the
                // views for native ads)
                FrameLayout adContainer = (FrameLayout) findViewById(R.id.adContainer);
                adContainer.addView(nativeContainer);

                nr = response;
            }

            @Override
            public void onAdFailed(ResultCode errorcode)
            {

            }
        });
        // Call `loadAd()` to request a response once
        adRequest.loadAd();

        FrameLayout adContainer = (FrameLayout) findViewById(R.id.adContainer);
        // Register native views for click and impression tracking.  The
        // `adEventListener` is the listener created above; it can be `null` if
        // you don't want to receive notifications about click events.
        // Impressions and clicks won't be counted if the view is not registered.
        NativeAdSDK.registerTracking(nr, adContainer, adEventListener);
    }

    private void loadVideo()
    {
        videoPlayer = new VideoView(this);
        videoContainer = new RelativeLayout(this);

        LayoutParams lp = new RelativeLayout.LayoutParams(1000,1000);

        videoContainer.setLayoutParams(lp);

        FrameLayout adContainer = (FrameLayout)findViewById(R.id.adContainer);
        adContainer.addView(videoContainer);

        et_placementId = (EditText) findViewById(R.id.editTextPlacementId);

        // Initialize VideoAd
        videoAd = new VideoAd(this, et_placementId.getText().toString());

        // Set the Ad-Load Listener
        videoAd.setAdLoadListener(new VideoAdLoadListener()
        {
            @Override
            public void onAdLoaded(VideoAd videoAd)
            {
                // Pause App's Content Video Player
                videoPlayer.pause();
                // Play the VideoAd by passing the container.
                videoAd.playAd(videoContainer);
            }

            @Override
            public void onAdRequestFailed(VideoAd videoAd, ResultCode errorCode)
            {
                Log.d(TAG, "onAdRequestFailed::"+errorCode);
                videoPlayer.start();
            }
        });

        //videoAd.setAge("25");
        // videoAd.setGender(AdView.GENDER.FEMALE);
        // videoAd.setOpensNativeBrowser(true);
        // videoAd.setLoadsInBackground(true);
        // videoAd.addCustomKeywords("KEY_1","VALUE_1");
        // videoAd.addCustomKeywords("KEY_2","VALUE_2");


        //Load the Ad.
        videoAd.loadAd();


        // Set PlayBack Listener.
        videoAd.setVideoPlaybackListener(new VideoAdPlaybackListener()
        {

            @Override
            public void onQuartile(VideoAd view, Quartile quartile)
            {
                Log.d(TAG, "onQuartile::" + quartile);
            }

            @Override
            public void onAdCompleted(VideoAd view, PlaybackCompletionState playbackState)
            {
                Log.d(TAG, "onAdCompleted::playbackState" + playbackState);
                videoPlayer.start();
            }

            @Override
            public void onAdMuted(VideoAd view, boolean isMute)
            {
                Log.d(TAG, "isAudioMute::"+isMute);
            }

            @Override
            public void onAdClicked(VideoAd adView)
            {
                Log.d(TAG, "onAdClicked");
            }
        });
    }

    private void cleanAds()
    {
        FrameLayout adContainer = (FrameLayout)findViewById(R.id.adContainer);

        adContainer.removeView(bav);
        if (bav != null) {bav.destroy();}

        adContainer.removeView(iav);
        if (iav != null) {iav.destroy();}

        adContainer.removeView(videoContainer);
        if (videoAd != null) {videoAd.activityOnDestroy();}

        adContainer.removeView(nativeContainer);
        if (nativeContainer != null) {NativeAdSDK.unRegisterTracking(nativeContainer);}
    }

    @Override
    public void onClick(View v)
    {
        cleanAds();

        Spinner spinner = (Spinner)findViewById(R.id.spinnerAdType);
        String adType = spinner.getSelectedItem().toString();

        switch(v.getId())
        {
            case R.id.buttonLoad:
                switch(adType)
                {
                    case "Banner":
                        loadBanner();
                        break;
                    case "Interstitial":
                        loadInterstitial();
                        break;
                    case "Native":
                        loadNative();
                        break;
                    case "Video":
                        loadVideo();
                        break;
                }
                break;
        }
    }

    // BEGIN : Interstitial lifeCycle management
    @Override
    public void onAdLoaded(AdView av)
    {
        Log.d("onAdLoaded", "The ad has loaded, now we can show it...");

        // Now that the ad has loaded, we can show it to the user.
        InterstitialAdView iav = (InterstitialAdView) av;
        iav.show();
    }

    @Override
    public void onAdRequestFailed(AdView av, ResultCode rc)
    {
        Log.d("onAdRequestFailed", "Not sure why the ad request failed; try again? Return code ==> " + rc);
    }

    @Override
    public void onAdClicked(AdView av)
    {
        Log.d("onAdClicked", "The user clicked your ad.  Congrats!");
    }

    @Override
    public void onAdCollapsed(AdView av)
    {
        // Do something here.
    }

    @Override
    public void onAdExpanded(AdView av)
    {
        // Do something here as well.
    }
    // END : Interstitial lifeCycle management
    // BEGIN : Video lifeCycle management
    // Pass the Activity LifeCycle Callback's to VideoAd. This is very important for autoresuming the Video Ad after interruption.
    @Override
    public void onResume()
    {
        super.onResume();
        if (videoAd != null) {videoAd.activityOnResume();}
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (videoAd != null) {videoAd.activityOnPause();}
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (videoAd != null) {videoAd.activityOnDestroy();}
    }
    // END : Video lifeCycle management
}
