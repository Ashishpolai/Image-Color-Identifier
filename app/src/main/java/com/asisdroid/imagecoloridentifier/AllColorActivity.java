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
import androidx.cardview.widget.CardView;
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

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllColorActivity extends Activity {

    private static final String TAG = "AllCOlorActivity";
    GridView colorGridView;
    private AllColorViewAdapter allColorViewAdapter;

    public ColorDBAdpater colorNameDB;

    private ArrayList<String> colorNameList, colorCodeList, colorRgbList;

    private ClipboardManager clipboard;


    private ImageView btnBack;

    private EditText edtSearch;

    private TextView txtError;
    ProgressDialog dialog;
    public ProgressDialog progressForAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_all_color);

        progressForAds = new ProgressDialog(this);
        progressForAds.setMessage("Loading Ad..");
        progressForAds.setCanceledOnTouchOutside(false);
        progressForAds.setCancelable(false);

        /**
         *Ad Units should be in the type of IronSource.Ad_Unit.AdUnitName, example
         */
        IronSource.init(this, getResources().getString(R.string.ironsource_app_key),  IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.BANNER);

        IronSource.setInterstitialListener(new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                hideProgressbar();
                IronSource.showInterstitial("Startup");
            }

            @Override
            public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                hideProgressbar();
            }

            @Override
            public void onInterstitialAdOpened() {
            }

            @Override
            public void onInterstitialAdClosed() {
                finish();
            }

            @Override
            public void onInterstitialAdShowSucceeded() {

            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {

            }

            @Override
            public void onInterstitialAdClicked() {

            }
        });

        IntegrationHelper.validateIntegration(this);

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
                holder.txtColorRgb = (TextView) convertView.findViewById(R.id.txt_colorrgb);
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

            holder.txtColorCode.setText("#"+colorCodeList.get(position));
            holder.txtColorName.setText(colroname);
            holder.txtColorRgb.setText("RGB:"+rgbValue);
            holder.colorDemo.setBackgroundColor(Color.parseColor("#"+colorCodeList.get(position)));
            final String copyRgbVal = rgbValue;
            holder.colorFullLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipData clip = ClipData.newPlainText("Image color identifier", colorNameList.get(position)+"( #"+colorCodeList.get(position)+" )\nRGB:"+
                            copyRgbVal);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(AllColorActivity.this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }

    class ViewHolder {
        TextView txtColorName, txtColorCode, txtColorRgb;
        LinearLayout colorDemo;
        CardView colorFullLayout;
    }

    @Override
    public void onBackPressed() {
        if(checkInternetConenction()){
            progressForAds.show();
            IronSource.loadInterstitial();
        }
        else {
            super.onBackPressed();
        }

    }

    private void hideProgressbar(){
        if(progressForAds.isShowing())
            progressForAds.hide();
    }

    public void onPressingBack(){

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
