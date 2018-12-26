package com.github.shadowsocks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import android.widget.Toast;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.sugarsvalley.svpn.ui.MainSlideActivity;


public class TestActivity extends AppCompatActivity {
    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;

    public static Handler handler = null;
    public static void setMainHandler(Handler h) {
        handler = h;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);

        TextView tvRVad = (TextView) findViewById(R.id.textView);
        TextView tvIad = (TextView) findViewById(R.id.textViewI);
        TextView tvIp = (TextView) findViewById(R.id.textViewProfile);

        MobileAds.initialize(this, "ca-app-pub-9293841874269644~3114136828");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(interstitialAdListener);

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(videoAdListener);
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());

        tvRVad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(TestActivity.this, ProfileManagerActivity.class));
//                handler.sendEmptyMessage(1);
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    Log.d("TAG", "The reward video wasn't loaded yet.");
                }

                startActivity(new Intent(TestActivity.this, MainSlideActivity.class));

            }
        });

        tvIad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }
        });

        tvIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.sendEmptyMessage(Scala2JavaBridge.MESSAGE_START_PROFILE);
//                new HomeActivity().test(TestActivity.this);
            }
        });
    }

    private AdListener interstitialAdListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            // Code to be executed when an ad finishes loading.
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Code to be executed when an ad request fails.
        }

        @Override
        public void onAdOpened() {
            // Code to be executed when the ad is displayed.
        }

        @Override
        public void onAdLeftApplication() {
            // Code to be executed when the user has left the app.
        }

        @Override
        public void onAdClosed() {
            // Code to be executed when when the interstitial ad is closed.
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    };

    private RewardedVideoAdListener videoAdListener = new RewardedVideoAdListener() {
        @Override
        public void onRewarded(RewardItem reward) {
            Toast.makeText(TestActivity.this, "onRewarded! currency: " + reward.getType() + "  amount: " +
                    reward.getAmount(), Toast.LENGTH_SHORT).show();
            // Reward the user.
        }

        @Override
        public void onRewardedVideoAdLeftApplication() {
            Toast.makeText(TestActivity.this, "onRewardedVideoAdLeftApplication",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoAdClosed() {
            Toast.makeText(TestActivity.this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoAdFailedToLoad(int errorCode) {
            Toast.makeText(TestActivity.this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoAdLoaded() {
            Toast.makeText(TestActivity.this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoAdOpened() {
            Toast.makeText(TestActivity.this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoStarted() {
            Toast.makeText(TestActivity.this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
        }
    };


}
