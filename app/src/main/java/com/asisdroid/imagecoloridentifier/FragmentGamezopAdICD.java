package com.asisdroid.imagecoloridentifier;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static java.security.AccessController.getContext;


public class FragmentGamezopAdICD extends Fragment {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		    View view = inflater.inflate(R.layout.fragment_gamezop_ad,
		        container, false);

		    final ImageView gamezopAd = (ImageView) view.findViewById(R.id.gamezop_ad);
		    gamezopAd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openPortal();
				}
			});

		    return view;
		  
	}

	private void openPortal() {
		String url = "https://www.gamezop.com/?id=9ntlx2Ol5";
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
		CustomTabsIntent customTabsIntent = builder.build();
		PackageManager packageManager = getActivity().getPackageManager();
		if(isPackageInstalled("com.android.chrome", packageManager)) {
			customTabsIntent.intent.setPackage("com.android.chrome");
			customTabsIntent.launchUrl(getContext(), Uri.parse(url));
		}
		else{
			Toast.makeText(getContext(), "Please install a Google Chrome browser to play interesting games!", Toast.LENGTH_SHORT).show();
			final String appPackageName = "com.android.chrome"; // chrome install
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}
		}
	}

	public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
		try {
			return packageManager.getApplicationInfo(packageName, 0).enabled;
		}
		catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
}