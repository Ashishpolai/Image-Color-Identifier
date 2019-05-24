package com.asisdroid.imagecoloridentifier;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.skydoves.multicolorpicker.ColorEnvelope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AllColorActivity extends Activity {

    GridView colorGridView;
    private AllColorViewAdapter allColorViewAdapter;

    public ColorDBAdpater colorNameDB;

    private ArrayList<String> colorNameList, colorCodeList, colorRgbList;

    private ClipboardManager clipboard;

    private InterstitialAd mInterstitialAd;

    private ImageView btnBack;

    private EditText edtSearch;

    private TextView txtError;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_color);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.admobInterstetialunitID));

        if(checkInternetConenction()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading colors...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        colorNameDB = new ColorDBAdpater(this);
        colorNameDB = colorNameDB.open();

        colorGridView = (GridView) findViewById(R.id.allcolors_view);

        new GetDBDatas().execute();

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        txtError = (TextView) findViewById(R.id.txt_err);
        btnBack = (ImageView) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        edtSearch = (EditText) findViewById(R.id.edt_search);
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //Toast.makeText(AllColorActivity.this, "enter", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length()>1){
                    colorNameList = colorNameDB.getColorNamesAlike(String.valueOf(charSequence));
                    colorCodeList = colorNameDB.getColorCodesAlike(String.valueOf(charSequence));
                }
                else{
                    colorCodeList=  colorNameDB.getAllColorCodes();
                    colorNameList = colorNameDB.getAllColorNames();
                }
                allColorViewAdapter.notifyDataSetChanged();
                colorGridView.setAdapter(allColorViewAdapter);

                if(colorNameList.size()==0){
                    txtError.setVisibility(View.VISIBLE);
                    colorGridView.setVisibility(View.GONE);
                }
                else{
                    txtError.setVisibility(View.GONE);
                    colorGridView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
       // Toast.makeText(this, ""+colorNameList.size(), Toast.LENGTH_SHORT).show();
    }

    public class AllColorViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public AllColorViewAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            if(colorNameList!=null) {
                return colorNameList.size();
            }
            else{
                return 0;
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.colors_layout, null);
                holder.txtColorCode = (TextView) convertView.findViewById(R.id.txt_colorcode);
                holder.txtColorName = (TextView) convertView.findViewById(R.id.txt_colroname);
                holder.colorDemo = (LinearLayout) convertView.findViewById(R.id.color_demo);
                holder.colorFullLayout = (CardView) convertView.findViewById(R.id.color_layout);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txtColorCode.setTag(R.id.txt_colorcode,position);
            holder.txtColorName.setTag(R.id.txt_colroname,position);
            holder.colorDemo.setTag(R.id.color_demo,position);
            holder.colorFullLayout.setTag(R.id.color_layout,position);

            String colroname = colorNameList.get(position);
            if(colroname.length()>9){
                colroname = colroname.substring(0,7)+"..";
            }

            String rgbValue = "";
            try {
                final JSONObject obj = new JSONObject(colorRgbList.get(position));
                final JSONArray rgbArray = obj.getJSONArray("rgb");
                rgbValue = "("+rgbArray.getInt(0)+","+rgbArray.getInt(1)+","+rgbArray.getInt(2)+")";

            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.txtColorCode.setText("#"+colorCodeList.get(position)+"\nRGB:\n"+rgbValue);
            holder.txtColorName.setText(colroname);
            holder.colorDemo.setBackgroundColor(Color.parseColor("#"+colorCodeList.get(position)));

            holder.colorFullLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipData clip = ClipData.newPlainText("Image color identifier", colorNameList.get(position)+"( #"+colorCodeList.get(position)+" )");
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(AllColorActivity.this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }

    class ViewHolder {
        TextView txtColorName, txtColorCode;
        LinearLayout colorDemo;
        CardView colorFullLayout;
    }

    @Override
    public void onBackPressed() {
        if(checkInternetConenction()){
            if(mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
                mInterstitialAd.setAdListener(new AdListener() {
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
                        finish();
                    }
                });
            }
            else{
                super.onBackPressed();
            }
        }
        else {
            super.onBackPressed();
        }

    }

    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        NetworkInfo info = connec.getActiveNetworkInfo();

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {

            return false;
        }
        return false;
    }

    public class GetDBDatas extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //Log.d("asisi","started");
            colorNameList = colorNameDB.getAllColorNames();
            colorCodeList = colorNameDB.getAllColorCodes();
            colorRgbList = colorNameDB.getAllColorRGB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            allColorViewAdapter = new AllColorViewAdapter();
            colorGridView.setAdapter(allColorViewAdapter);
            dialog.dismiss();
           // Log.d("asisi","finished");
        }
    }

}
