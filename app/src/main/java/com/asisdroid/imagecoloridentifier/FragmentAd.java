package com.asisdroid.imagecoloridentifier;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import androidx.fragment.app.Fragment;

public class FragmentAd extends Fragment {

	//private AdView mAdView;
	private AdView adView;;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//		    View view = inflater.inflate(R.layout.fragment_ad,
//		        container, false);
//			MobileAds.initialize(getActivity(), getResources().getString(R.string.admobappID));
//			mAdView = (AdView) view.findViewById(R.id.adView);
//			AdRequest adRequest = new AdRequest.Builder().build();
//			mAdView.loadAd(adRequest);
//		    return view;

		View view = inflater.inflate(R.layout.fragment_ad,
		        container, false);
		adView = new AdView(getContext(), getResources().getString(R.string.fb_banner_placementid), AdSize.BANNER_HEIGHT_50);

		// Find the Ad Container
		LinearLayout adContainer = (LinearLayout) view.findViewById(R.id.adView);

		// Add the ad view to your activity layout
		adContainer.addView(adView);

		// Request an ad
		adView.loadAd();
		return view;
	}

	@Override
	public void onDestroyView() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroyView();
	}
}