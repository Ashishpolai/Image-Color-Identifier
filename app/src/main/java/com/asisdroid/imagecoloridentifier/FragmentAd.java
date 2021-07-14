package com.asisdroid.imagecoloridentifier;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;

import androidx.fragment.app.Fragment;

public class FragmentAd extends Fragment {


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_ad,
		        container, false);
		IronSource.init(getActivity(), getActivity().getResources().getString(R.string.ironsource_app_key), IronSource.AD_UNIT.BANNER);

		final FrameLayout bannerContainer = view.findViewById(R.id.bannerContainer);
		IronSourceBannerLayout banner = IronSource.createBanner(getActivity(), ISBannerSize.BANNER);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		bannerContainer.addView(banner, 0, layoutParams);

		banner.setBannerListener(new BannerListener() {
			@Override
			public void onBannerAdLoaded() {
				// Called after a banner ad has been successfully loaded
			}

			@Override
			public void onBannerAdLoadFailed(IronSourceError error) {
				// Called after a banner has attempted to load an ad but failed.
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						bannerContainer.removeAllViews();
					}
				});
			}

			@Override
			public void onBannerAdClicked() {
				// Called after a banner has been clicked.
			}

			@Override
			public void onBannerAdScreenPresented() {
				// Called when a banner is about to present a full screen content.
			}

			@Override
			public void onBannerAdScreenDismissed() {
				// Called after a full screen content has been dismissed
			}

			@Override
			public void onBannerAdLeftApplication() {
				// Called when a user would be taken out of the application context.
			}
		});

		IronSource.loadBanner(banner, "Startup");
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}