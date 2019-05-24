package com.asisdroid.imagecoloridentifier;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/*
import com.asisdroid.colorpickerview.ColorListener;
import com.asisdroid.colorpickerview.MultiColorPickerView;*/

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.skydoves.multicolorpicker.ColorEnvelope;
import com.skydoves.multicolorpicker.MultiColorPickerView;
import com.skydoves.multicolorpicker.listeners.ColorListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    private MultiColorPickerView multiColorPickerView;
    public Button btnChngeImg;
    ImageView btnAllColors;

    Uri uriSavedImage;

    private int width, height;
    private static TextView textView;
    private static ClipboardManager clipboard;

    private SharedPreferences permissionStatus, firstTime;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 2200;
    private static final int REQUEST_PERMISSION_SETTING = 2031;
    private static final int CAMERA_REQUEST_CODE = 041;
    private static final int GALLERY_REQUEST_CODE = 032;
    private static final String SHOWCASE_ID = "first time opendfdasdsdfjsdkjfksdj";
    private InterstitialAd mInterstitialAd;

    public ColorDBAdpater colorNameDB;
    LinearLayout linearLayout;

    private String selectedColorCode = "";
    private int selected_rgb[] = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_color_picker_view_example);

        MobileAds.initialize(this, getResources().getString(R.string.admobappID));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.admobInterstetialunitID));

        if(checkInternetConenction()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        textView = findViewById(R.id.textView0);
        multiColorPickerView = findViewById(R.id.multiColorPickerView);
        multiColorPickerView.setPaletteDrawable(ContextCompat.getDrawable(this, R.drawable.bgimg));
        multiColorPickerView.addSelector(ContextCompat.getDrawable(this, R.drawable.wheel), colorListener0);

        width = ContextCompat.getDrawable(this, R.drawable.bgimg).getIntrinsicWidth();
        height = ContextCompat.getDrawable(this, R.drawable.bgimg).getIntrinsicHeight();
        Log.d("asisi",""+ContextCompat.getDrawable(this, R.drawable.bgimg).getIntrinsicWidth()+":"+ContextCompat.getDrawable(this, R.drawable.bgimg).getIntrinsicHeight());

        multiColorPickerView.setFlagView(new CustomFlag(MainActivity.this, R.layout.layout_flag));
        multiColorPickerView.setFlagFlipable(false);

        permissionStatus = getSharedPreferences("imagecoloridentifier",MODE_PRIVATE);
        firstTime = getSharedPreferences("imagecoloridentifierfirstftisme",MODE_PRIVATE);

        btnChngeImg = (Button) findViewById(R.id.btn_changeimg);
        btnChngeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                askPermissions();
                                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                            }
                        });
                    }
                    else{
                        askPermissions();
                    }
                }
                else {
                    askPermissions();
                }
            }
        });

        btnAllColors  = (ImageView) findViewById(R.id.btn_allcolors);
        btnAllColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent allColorIntent = new Intent(MainActivity.this, AllColorActivity.class);
                startActivity(allColorIntent);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    ClipData clip = ClipData.newPlainText("Image color identifier", textView.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
            }
        });

        //Call this only for the first time
        colorNameDB = new ColorDBAdpater(this);
        colorNameDB = colorNameDB.open();
        if(firstTime.getAll().size()==0) {
            new InsertDBData().execute();
            presentShowcaseView(); //Startup show tips for the first time
        }

        //initColorNameHashMap();
    }

    private ColorListener colorListener0  = new ColorListener () {
        @Override
        public void onColorSelected(ColorEnvelope envelope) {
            selected_rgb = envelope.getRgb();
            selectedColorCode = envelope.getHtmlCode();
            if(!firstTime.getBoolean("isShown", false)) {
                textView.setText("#" + selectedColorCode + "\nRGB:[" + selected_rgb[0] + "," + selected_rgb[1] + "," + selected_rgb[2] + "]");
            }
            else{
                textView.setText("#" + selectedColorCode + "\nRGB:[" + selected_rgb[0] + "," + selected_rgb[1] + "," + selected_rgb[2] + "]\n"+
                        getColorName(selectedColorCode, selected_rgb));
            }
            //textView.setTextColor(envelope.getColor());

            linearLayout = findViewById(R.id.linearLayout0);
            linearLayout.setBackgroundColor(envelope.getColor());
        }
    };

    public void askPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Image Color Identifier");
                    builder.setMessage("Need permissions to upload your custom images.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                    //Previously Permission Request was cancelled with 'Dont Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Image Color Identifier");
                    builder.setMessage("Need permissions to upload your custom images");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            Toast.makeText(MainActivity.this, "Go to Permissions to Grant Location", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    //just request the permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                }

                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
                editor.commit();


            } else {
                //You already have the permission, just go ahead.
                showChoice();
                //Intent mapIntent = new Intent(MainActivity.this,MapActivity.class);
                //startActivityForRe;sult(mapIntent,0);
            }
        }
        else{
            //Not needed for asking permissions below MarshMallow
            showChoice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0] == 0){
            showChoice();
        }
    }

    private void showChoice(){ //FOR SELECTING IMAGE
        final String x[] = {"Take photo", "Select from gallery"};
        AlertDialog.Builder setImage = new AlertDialog.Builder(this);
        // setImage.setTitle("Image for Transaction");
        setImage.setItems(x, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){ //camera
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    File imagesFolder = new File(Environment.getExternalStorageDirectory(), "ImageColorIdentifier");
                    imagesFolder.mkdirs(); // <----
                    File image = new File(imagesFolder, "coloridentifier_" + System.currentTimeMillis() + "_image_.jpg");
                    //uriSavedImage = Uri.fromFile(image);
                    //FOr above Android 24...below code
                    uriSavedImage = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".GenericFileProvider", image);;
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                    startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
                }
                else if(which==1){ //Gallery
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);//one can be replaced with any action code
                }
            }
        });
        setImage.create();
        setImage.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        switch(requestCode) {
            case CAMERA_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = uriSavedImage;
                    setImageFromPath(selectedImage);
                }

                break;
            case GALLERY_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    setImageFromPath(selectedImage);
                }
                break;
        }
    }

    private void setImageFromPath(Uri img) {
       // Glide.with(MainActivity.this).load(imagePath).transition(DrawableTransitionOptions.withCrossFade()).into();
       // multiColorPickerView.setPaletteDrawable(img);
        Drawable finalDrawable;
        try {
            InputStream inputStream = getContentResolver().openInputStream(img);
            finalDrawable = Drawable.createFromStream(inputStream, img.toString() );
        } catch (FileNotFoundException e) {
            finalDrawable = getResources().getDrawable(R.drawable.bgimg);
            Toast.makeText(this, "Problem in Loading. Try again!", Toast.LENGTH_SHORT).show();
        }
        Log.d("asisi",""+finalDrawable.getIntrinsicWidth()+":"+finalDrawable.getIntrinsicHeight());
        Bitmap bitmap = ((BitmapDrawable) finalDrawable).getBitmap();
// Scale it to 50 x 50
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, width, height, true));
// Set your new, scaled drawable "d"
        multiColorPickerView.setPaletteDrawable(d);
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

    public class InsertDBData extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("asisi","started");
            initColorNameHashMap();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SharedPreferences.Editor editor = firstTime.edit();
            editor.putBoolean("isShown", true);
            editor.commit();
            if(btnAllColors!=null)
                btnAllColors.setVisibility(View.VISIBLE);

            if(textView!=null){
                textView.setText(textView.getText()+"\n"+getColorName(selectedColorCode, selected_rgb));
            }
            Log.d("asisi","finished");
        }
    }


    public void initColorNameHashMap(){
        //Log.d("asisi","insertstart");
        colorNameDB.insertNewColorData("4C4F56","Abbey","{\"rgb\" : [76,79,86]}");
        colorNameDB.insertNewColorData("1B1404","Acadia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7CB0A1","Acapulco","{\"rgb\" : [124, 176, 161]}");
        colorNameDB.insertNewColorData("C9FFE5","Aero Blue","{\"rgb\" : [201, 255, 229]}");
        colorNameDB.insertNewColorData("714693","Affair","{\"rgb\" : [113, 70, 147]}");
        colorNameDB.insertNewColorData("D4C4A8","Akaroa","{\"rgb\" : [212, 196, 168]}");
        //Log.d("asisi","insertend="+colorNameDB.getColorName("4C4F56"),"{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FAFAFA","Alabaster","{\"rgb\" : [250, 250, 250]}");
        colorNameDB.insertNewColorData("F5E9D3","Albescent White","{\"rgb\" : [245, 233, 211]}");
        colorNameDB.insertNewColorData("93DFB8","Algae Green","{\"rgb\" : [147, 223, 184]}");
        colorNameDB.insertNewColorData("F0F8FF","Alice Blue","{\"rgb\" : [240, 248, 255]}");
        colorNameDB.insertNewColorData("E32636","Alizarin Crimson","{\"rgb\" : [227, 38, 54]}");
        colorNameDB.insertNewColorData("0076A3","Allports","{\"rgb\" : [0, 118, 163]}");
        colorNameDB.insertNewColorData("EED9C4","Almond","{\"rgb\" : [238, 217, 196]}");
        colorNameDB.insertNewColorData("907B71","Almond Frost","{\"rgb\" : [144, 123, 113]}");
        colorNameDB.insertNewColorData("AF8F2C","Alpine","{\"rgb\" : [175, 143, 44]}");
        colorNameDB.insertNewColorData("DBDBDB","Alto","{\"rgb\" : [219, 219, 219]}");
        colorNameDB.insertNewColorData("A9ACB6","Aluminium","{\"rgb\" : [169, 172, 182]}");
        colorNameDB.insertNewColorData("E52B50","Amaranth","{\"rgb\" : [229, 43, 80]}");
        colorNameDB.insertNewColorData("3B7A57","Amazon","{\"rgb\" : [59, 122, 87]}");
        colorNameDB.insertNewColorData("FFBF00","Amber","{\"rgb\" : [255, 191, 0]}");
        colorNameDB.insertNewColorData("87756E","Americano","{\"rgb\" : [135, 117, 110]}");
        colorNameDB.insertNewColorData("9966CC","Amethyst","{\"rgb\" : [153, 102, 204]}");
        colorNameDB.insertNewColorData("A397B4","Amethyst Smoke","{\"rgb\" : [163, 151, 180]}");
        colorNameDB.insertNewColorData("F9EAF3","Amour","{\"rgb\" : [249, 234, 243]}");
        colorNameDB.insertNewColorData("7B9F80","Amulet","{\"rgb\" : [123, 159, 128]}");
        colorNameDB.insertNewColorData("9DE5FF","Anakiwa","{\"rgb\" : [157, 229, 255]}");
        colorNameDB.insertNewColorData("C88A65","Antique Brass","{\"rgb\" : [200, 138, 101]}");
        colorNameDB.insertNewColorData("704A07","Antique Bronze","{\"rgb\" : [112, 74, 7]}");
        colorNameDB.insertNewColorData("E0B646","Anzac","{\"rgb\" : [224, 182, 70]}");
        colorNameDB.insertNewColorData("DFBE6F","Apache","{\"rgb\" : [223, 190, 111]}");
        colorNameDB.insertNewColorData("4FA83D","Apple","{\"rgb\" : [79, 168, 61]}");
        colorNameDB.insertNewColorData("AF4D43","Apple Blossom","{\"rgb\" : [175, 77, 67]}");
        colorNameDB.insertNewColorData("E2F3EC","Apple Green","{\"rgb\" : [226, 243, 236]}");
        colorNameDB.insertNewColorData("EB9373","Apricot","{\"rgb\" : [235, 147, 115]}");
        colorNameDB.insertNewColorData("FBCEB1","Apricot Peach","{\"rgb\" : [251, 206, 177]}");
        colorNameDB.insertNewColorData("FFFEEC","Apricot White","{\"rgb\" : [255, 254, 236]}");
        colorNameDB.insertNewColorData("014B43","Aqua Deep","{\"rgb\" : [1, 75, 67]}");
        colorNameDB.insertNewColorData("5FA777","Aqua Forest","{\"rgb\" : [95, 167, 119]}");
        colorNameDB.insertNewColorData("EDF5F5","Aqua Haze","{\"rgb\" : [237, 245, 245]}");
        colorNameDB.insertNewColorData("A1DAD7","Aqua Island","{\"rgb\" : [161, 218, 215]}");
        colorNameDB.insertNewColorData("EAF9F5","Aqua Spring","{\"rgb\" : [234, 249, 245]}");
        colorNameDB.insertNewColorData("E8F5F2","Aqua Squeeze","{\"rgb\" : [232, 245, 242]}");
        colorNameDB.insertNewColorData("7FFFD4","Aquamarine","{\"rgb\" : [127, 255, 212]}");
        colorNameDB.insertNewColorData("71D9E2","Aquamarine Blue","{\"rgb\" : [113, 217, 226]}");
        colorNameDB.insertNewColorData("110C6C","Arapawa","{\"rgb\" : [17, 12, 108]}");
        colorNameDB.insertNewColorData("433E37","Armadillo","{\"rgb\" : [67, 62, 55]}");
        colorNameDB.insertNewColorData("948771","Arrowtown","{\"rgb\" : [148, 135, 113]}");
        colorNameDB.insertNewColorData("C6C3B5","Ash","{\"rgb\" : [198, 195, 181]}");
        colorNameDB.insertNewColorData("7BA05B","Asparagus","{\"rgb\" : [123, 160, 91]}");
        colorNameDB.insertNewColorData("130A06","Asphalt","{\"rgb\" : [19, 10, 6]}");
        colorNameDB.insertNewColorData("FAEAB9","Astra","{\"rgb\" : [250, 234, 185]}");
        colorNameDB.insertNewColorData("327DA0","Astral","{\"rgb\" : [50, 125, 160]}");
        colorNameDB.insertNewColorData("283A77","Astronaut","{\"rgb\" : [40, 58, 119]}");
        colorNameDB.insertNewColorData("013E62","Astronaut Blue","{\"rgb\" : [1, 62, 98]}");
        colorNameDB.insertNewColorData("EEF0F3","Athens Gray","{\"rgb\" : [238, 240, 243]}");
        colorNameDB.insertNewColorData("ECEBCE","Aths Special","{\"rgb\" : [236, 235, 206]}");
        colorNameDB.insertNewColorData("97CD2D","Atlantis","{\"rgb\" : [151, 205, 45]}");
        colorNameDB.insertNewColorData("0A6F75","Atoll","{\"rgb\" : [10, 111, 117]}");
        colorNameDB.insertNewColorData("FF9966","Atomic Tangerine","{\"rgb\" : [255, 153, 102]}");
        colorNameDB.insertNewColorData("97605D","Au Chico","{\"rgb\" : [151, 96, 93]}");
        colorNameDB.insertNewColorData("3B0910","Aubergine","{\"rgb\" : [59, 9, 16]}");
        colorNameDB.insertNewColorData("F5FFBE","Australian Mint","{\"rgb\" : [245, 255, 190]}");
        colorNameDB.insertNewColorData("888D65","Avocado","{\"rgb\" : [136, 141, 101]}");
        colorNameDB.insertNewColorData("4E6649","Axolotl","{\"rgb\" : [78, 102, 73]}");
        colorNameDB.insertNewColorData("F7C8DA","Azalea","{\"rgb\" : [247, 200, 218]}");
        colorNameDB.insertNewColorData("0D1C19","Aztec","{\"rgb\" : [13, 28, 25]}");
        colorNameDB.insertNewColorData("315BA1","Azure","{\"rgb\" : [49, 91, 161]}");
        colorNameDB.insertNewColorData("007FFF","Azure Radiance","{\"rgb\" : [0, 127, 255]}");
        colorNameDB.insertNewColorData("E0FFFF","Baby Blue","{\"rgb\" : [224, 255, 255]}");
        colorNameDB.insertNewColorData("026395","Bahama Blue","{\"rgb\" : [2, 99, 149]}");
        colorNameDB.insertNewColorData("A5CB0C","Bahia","{\"rgb\" : [165, 203, 12)]}");
        colorNameDB.insertNewColorData("FFF8D1","Baja White","{\"rgb\" : [255, 248, 209]}");
        colorNameDB.insertNewColorData("859FAF","Bali Hai","{\"rgb\" : [133, 159, 175]}");
        colorNameDB.insertNewColorData("2A2630","Baltic Sea","{\"rgb\" : [42, 38, 48]}");
        colorNameDB.insertNewColorData("DA6304","Bamboo","{\"rgb\" : [218, 99, 4]}");
        colorNameDB.insertNewColorData("FBE7B2","Banana Mania","{\"rgb\" : [251, 231, 178]}");
        colorNameDB.insertNewColorData("858470","Bandicoot","{\"rgb\" : [133, 132, 112]}");
        colorNameDB.insertNewColorData("DED717","Barberry","{\"rgb\" : [222, 215, 23]}");
        colorNameDB.insertNewColorData("A68B5B","Barley Corn","{\"rgb\" : [166, 139, 91)]}");
        colorNameDB.insertNewColorData("FFF4CE","Barley White","{\"rgb\" : [255, 244, 206]}");
        colorNameDB.insertNewColorData("44012D","Barossa","{\"rgb\" : [68, 1, 45]}");
        colorNameDB.insertNewColorData("292130","Bastille","{\"rgb\" : [41, 33, 48]}");
        colorNameDB.insertNewColorData("828F72","Battleship Gray","{\"rgb\" : [130, 143, 114]}");
        colorNameDB.insertNewColorData("7DA98D","Bay Leaf","{\"rgb\" : [125, 169, 141]}");
        colorNameDB.insertNewColorData("273A81","Bay of Many","{\"rgb\" : [39, 58, 129]}");
        colorNameDB.insertNewColorData("98777B","Bazaar","{\"rgb\" : [152, 119, 123]}");
        colorNameDB.insertNewColorData("3D0C02","Bean","{\"rgb\" : [61, 12, 2]}");
        colorNameDB.insertNewColorData("EEC1BE","Beauty Bush","{\"rgb\" : [238, 193, 190]}");
        colorNameDB.insertNewColorData("926F5B","Beaver","{\"rgb\" : [146, 111, 91]}");
        colorNameDB.insertNewColorData("FEF2C7","Beeswax","{\"rgb\" : [254, 242, 199]}");
        colorNameDB.insertNewColorData("F5F5DC","Beige","{\"rgb\" : [245, 245, 220]}");
        colorNameDB.insertNewColorData("7DD8C6","Bermuda","{\"rgb\" : [125, 216, 198]}");
        colorNameDB.insertNewColorData("6B8BA2","Bermuda Gray","{\"rgb\" : [107, 139, 162]}");
        colorNameDB.insertNewColorData("DEE5C0","Beryl Green","{\"rgb\" : [222, 229, 192]}");
        colorNameDB.insertNewColorData("FCFBF3","Bianca","{\"rgb\" : [252, 251, 243]}");
        colorNameDB.insertNewColorData("162A40","Big Stone","{\"rgb\" : [22, 42, 64]}");
        colorNameDB.insertNewColorData("327C14","Bilbao","{\"rgb\" : [50, 124, 20]}");
        colorNameDB.insertNewColorData("B2A1EA","Biloba Flower","{\"rgb\" : [178, 161, 234]}");
        colorNameDB.insertNewColorData("373021","Birch","{\"rgb\" : [55, 48, 33]}");
        colorNameDB.insertNewColorData("D4CD16","Bird Flower","{\"rgb\" : [212, 205, 22]}");
        colorNameDB.insertNewColorData("1B3162","Biscay","{\"rgb\" : [27, 49, 98]}");
        colorNameDB.insertNewColorData("497183","Bismark","{\"rgb\" : [73, 113, 131]}");
        colorNameDB.insertNewColorData("C1B7A4","Bison Hide","{\"rgb\" : [193, 183, 164]}");
        colorNameDB.insertNewColorData("3D2B1F","Bistre","{\"rgb\" : [61, 43, 31]}");
        colorNameDB.insertNewColorData("868974","Bitter","{\"rgb\" : [134, 137, 116]}");
        colorNameDB.insertNewColorData("CAE00D","Bitter Lemon","{\"rgb\" : [202, 224, 13]}");
        colorNameDB.insertNewColorData("FE6F5E","Bittersweet","{\"rgb\" : [254, 111, 94]}");
        colorNameDB.insertNewColorData("EEDEDA","Bizarre","{\"rgb\" : [238, 222, 218]}");
        colorNameDB.insertNewColorData("000000","Black","{\"rgb\" : [0, 0, 0]}");
        colorNameDB.insertNewColorData("081910","Black Bean","{\"rgb\" : [8, 25, 16]}");
        colorNameDB.insertNewColorData("0B1304","Black Forest","{\"rgb\" : [11, 19, 4]}");
        colorNameDB.insertNewColorData("F6F7F7","Black Haze","{\"rgb\" : [246, 247, 247]}");
        colorNameDB.insertNewColorData("3E2C1C","Black Marlin","{\"rgb\" : [62, 44, 28]}");
        colorNameDB.insertNewColorData("242E16","Black Olive","{\"rgb\" : [36, 46, 22]}");
        colorNameDB.insertNewColorData("041322","Black Pearl","{\"rgb\" : [4, 19, 34]}");
        colorNameDB.insertNewColorData("0D0332","Black Rock","{\"rgb\" : [13, 3, 50]}");
        colorNameDB.insertNewColorData("67032D","Black Rose","{\"rgb\" : [103, 3, 45]}");
        colorNameDB.insertNewColorData("0A001C","Black Russian","{\"rgb\" : [10, 0, 28]}");
        colorNameDB.insertNewColorData("F2FAFA","Black Squeeze","{\"rgb\" : [242, 250, 250]}");
        colorNameDB.insertNewColorData("FFFEF6","Black White","{\"rgb\" : [255, 254, 246]}");
        colorNameDB.insertNewColorData("4D0135","Blackberry","{\"rgb\" : [77, 1, 53]}");
        colorNameDB.insertNewColorData("32293A","Blackcurrant","{\"rgb\" : [50, 41, 58]}");
        colorNameDB.insertNewColorData("FF6600","Blaze Orange","{\"rgb\" : [255, 102, 0]}");
        colorNameDB.insertNewColorData("FEF3D8","Bleach White","{\"rgb\" : [254, 243, 216]}");
        colorNameDB.insertNewColorData("2C2133","Bleached Cedar","{\"rgb\" : [44, 33, 51]}");
        colorNameDB.insertNewColorData("A3E3ED","Blizzard Blue","{\"rgb\" : [163, 227, 237]}");
        colorNameDB.insertNewColorData("DCB4BC","Blossom","{\"rgb\" : [220, 180, 188]}");
        colorNameDB.insertNewColorData("0000FF","Blue","{\"rgb\" : [0, 0, 255]}");
        colorNameDB.insertNewColorData("496679","Blue Bayoux","{\"rgb\" : [73, 102, 121]}");
        colorNameDB.insertNewColorData("9999CC","Blue Bell","{\"rgb\" : [153, 153, 204]}");
        colorNameDB.insertNewColorData("F1E9FF","Blue Chalk","{\"rgb\" : [241, 233, 255]}");
        colorNameDB.insertNewColorData("010D1A","Blue Charcoal","{\"rgb\" : [1, 13, 26]}");
        colorNameDB.insertNewColorData("0C8990","Blue Chill","{\"rgb\" : [12, 137, 144]}");
        colorNameDB.insertNewColorData("380474","Blue Diamond","{\"rgb\" : [56, 4, 116]}");
        colorNameDB.insertNewColorData("204852","Blue Dianne","{\"rgb\" : [32, 72, 82]}");
        colorNameDB.insertNewColorData("2C0E8C","Blue Gem","{\"rgb\" : [44, 14, 140]}");
        colorNameDB.insertNewColorData("BFBED8","Blue Haze","{\"rgb\" : [191, 190, 216]}");
        colorNameDB.insertNewColorData("017987","Blue Lagoon","{\"rgb\" : [1, 121, 135]}");
        colorNameDB.insertNewColorData("7666C6","Blue Marguerite","{\"rgb\" : [118, 102, 198]}");
        colorNameDB.insertNewColorData("0066FF","Blue Ribbon","{\"rgb\" : [0, 102, 255]}");
        colorNameDB.insertNewColorData("D2F6DE","Blue Romance","{\"rgb\" : [210, 246, 222]}");
        colorNameDB.insertNewColorData("748881","Blue Smoke","{\"rgb\" : [116, 136, 129]}");
        colorNameDB.insertNewColorData("016162","Blue Stone","{\"rgb\" : [1, 97, 98]}");
        colorNameDB.insertNewColorData("6456B7","Blue Violet","{\"rgb\" : [100, 86, 183]}");
        colorNameDB.insertNewColorData("042E4C","Blue Whale","{\"rgb\" : [4, 46, 76]}");
        colorNameDB.insertNewColorData("13264D","Blue Zodiac","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("18587A","Blumine","{\"rgb\" : [19, 38, 77]}");
        colorNameDB.insertNewColorData("B44668","Blush","{\"rgb\" : [180, 70, 104]}");
        colorNameDB.insertNewColorData("FF6FFF","Blush Pink","{\"rgb\" : [255, 111, 255]}");
        colorNameDB.insertNewColorData("AFB1B8","Bombay","{\"rgb\" : [175, 177, 184]}");
        colorNameDB.insertNewColorData("E5E0E1","Bon Jour","{\"rgb\" : [229, 224, 225]}");
        colorNameDB.insertNewColorData("0095B6","Bondi Blue","{\"rgb\" : [0, 149, 182]}");
        colorNameDB.insertNewColorData("E4D1C0","Bone","{\"rgb\" : [228, 209, 192]}");
        colorNameDB.insertNewColorData("5C0120","Bordeaux","{\"rgb\" : [92, 1, 32]}");
        colorNameDB.insertNewColorData("4E2A5A","Bossanova","{\"rgb\" : [78, 42, 90]}");
        colorNameDB.insertNewColorData("3B91B4","Boston Blue","{\"rgb\" : [(59, 145, 180]}");
        colorNameDB.insertNewColorData("C7DDE5","Botticelli","{\"rgb\" : [199, 221, 229]}");
        colorNameDB.insertNewColorData("093624","Bottle Green","{\"rgb\" : [9, 54, 36]}");
        colorNameDB.insertNewColorData("7A7A7A","Boulder","{\"rgb\" : [122, 122, 122]}");
        colorNameDB.insertNewColorData("AE809E","Bouquet","{\"rgb\" : [174, 128, 158]}");
        colorNameDB.insertNewColorData("BA6F1E","Bourbon","{\"rgb\" : [186, 111, 30]}");
        colorNameDB.insertNewColorData("4A2A04","Bracken","{\"rgb\" : [74, 42, 4]}");
        colorNameDB.insertNewColorData("DEC196","Brandy","{\"rgb\" : [222, 193, 150]}");
        colorNameDB.insertNewColorData("CD8429","Brandy Punch","{\"rgb\" : [205, 132, 41]}");
        colorNameDB.insertNewColorData("BB8983","Brandy Rose","{\"rgb\" : [187, 137, 131]}");
        colorNameDB.insertNewColorData("5DA19F","Breaker Bay","{\"rgb\" : [93, 161, 159]}");
        colorNameDB.insertNewColorData("C62D42","Brick Red","{\"rgb\" : [198, 45, 66]}");
        colorNameDB.insertNewColorData("FFFAF4","Bridal Heath","{\"rgb\" : [255, 250, 244]}");
        colorNameDB.insertNewColorData("FEF0EC","Bridesmaid","{\"rgb\" : [254, 240, 236]}");
        colorNameDB.insertNewColorData("3C4151","Bright Gray","{\"rgb\" : [60, 65, 81]}");
        colorNameDB.insertNewColorData("66FF00","Bright Green","{\"rgb\" : [102, 255, 0]}");
        colorNameDB.insertNewColorData("B10000","Bright Red","{\"rgb\" : [177, 0, 0]}");
        colorNameDB.insertNewColorData("FED33C","Bright Sun","{\"rgb\" : [254, 211, 60]}");
        colorNameDB.insertNewColorData("08E8DE","Bright Turquoise","{\"rgb\" : [8, 232, 222]}");
        colorNameDB.insertNewColorData("F653A6","Brilliant Rose","{\"rgb\" : [246, 83, 166]}");
        colorNameDB.insertNewColorData("FB607F","Brink Pink","{\"rgb\" : [251, 96, 127]}");
        colorNameDB.insertNewColorData("ABA196","Bronco","{\"rgb\" : [171, 161, 150]}");
        colorNameDB.insertNewColorData("3F2109","Bronze","{\"rgb\" : [63, 33, 9]}");
        colorNameDB.insertNewColorData("4E420C","Bronze Olive","{\"rgb\" : [78, 66, 12]}");
        colorNameDB.insertNewColorData("4D400F","Bronzetone","{\"rgb\" : [77, 64, 15]}");
        colorNameDB.insertNewColorData("FFEC13","Broom","{\"rgb\" : [255, 236, 19]}");
        colorNameDB.insertNewColorData("964B00","Brown","{\"rgb\" : [150, 75, 0]}");
        colorNameDB.insertNewColorData("592804","Brown Bramble","{\"rgb\" : [89, 40, 4]}");
        colorNameDB.insertNewColorData("492615","Brown Derby","{\"rgb\" : [73, 38, 21]}");
        colorNameDB.insertNewColorData("401801","Brown Pod","{\"rgb\" : [64, 24, 1]}");
        colorNameDB.insertNewColorData("AF593E","Brown Rust","{\"rgb\" : [175, 89, 62]}");
        colorNameDB.insertNewColorData("37290E","Brown Tumbleweed","{\"rgb\" : [55, 41, 14]}");
        colorNameDB.insertNewColorData("E7FEFF","Bubbles","{\"rgb\" : [231, 254, 255]}");
        colorNameDB.insertNewColorData("622F30","Buccaneer","{\"rgb\" : [98, 47, 48]}");
        colorNameDB.insertNewColorData("A8AE9C","Bud","{\"rgb\" : [168, 174, 156]}");
        colorNameDB.insertNewColorData("C1A004","Buddha Gold","{\"rgb\" : [193, 160, 4]}");
        colorNameDB.insertNewColorData("F0DC82","Buff","{\"rgb\" : [240, 220, 130]}");
        colorNameDB.insertNewColorData("480607","Bulgarian Rose","{\"rgb\" : [72, 6, 7]}");
        colorNameDB.insertNewColorData("864D1E","Bull Shot","{\"rgb\" : [134, 77, 30]}");
        colorNameDB.insertNewColorData("0D1117","Bunker","{\"rgb\" : [13, 17, 23]}");
        colorNameDB.insertNewColorData("151F4C","Bunting","{\"rgb\" : [21, 31, 76]}");
        colorNameDB.insertNewColorData("900020","Burgundy","{\"rgb\" : [144, 0, 32]}");
        colorNameDB.insertNewColorData("002E20","Burnham","{\"rgb\" : [0, 46, 32]}");
        colorNameDB.insertNewColorData("FF7034","Burning Orange","{\"rgb\" : [255, 112, 52]}");
        colorNameDB.insertNewColorData("D99376","Burning Sand","{\"rgb\" : [217, 147, 118]}");
        colorNameDB.insertNewColorData("420303","Burnt Maroon","{\"rgb\" : [66, 3, 3]}");
        colorNameDB.insertNewColorData("CC5500","Burnt Orange","{\"rgb\" : [204, 85, 0]}");
        colorNameDB.insertNewColorData("E97451","Burnt Sienna","{\"rgb\" : [233, 116, 81]}");
        colorNameDB.insertNewColorData("8A3324","Burnt Umber","{\"rgb\" : [138, 51, 36]}");
        colorNameDB.insertNewColorData("0D2E1C","Bush","{\"rgb\" : [13, 46, 28]}");
        colorNameDB.insertNewColorData("F3AD16","Buttercup","{\"rgb\" : [243, 173, 22]}");
        colorNameDB.insertNewColorData("A1750D","Buttered Rum","{\"rgb\" : [161, 117, 13]}");
        colorNameDB.insertNewColorData("624E9A","Butterfly Bush","{\"rgb\" : [98, 78, 154]}");
        colorNameDB.insertNewColorData("FFF1B5","Buttermilk","{\"rgb\" : [255, 241, 181]}");
        colorNameDB.insertNewColorData("FFFCEA","Buttery White","{\"rgb\" : [255, 252, 234]}");
        colorNameDB.insertNewColorData("4D0A18","Cab Sav","{\"rgb\" : [77, 10, 24]}");
        colorNameDB.insertNewColorData("D94972","Cabaret","{\"rgb\" : [217, 73, 114]}");
        colorNameDB.insertNewColorData("3F4C3A","Cabbage Pont","{\"rgb\" : [63, 76, 58]}");
        colorNameDB.insertNewColorData("587156","Cactus","{\"rgb\" : [88, 113, 86]}");
        colorNameDB.insertNewColorData("A9B2C3","Cadet Blue","{\"rgb\" : [169, 178, 195]}");
        colorNameDB.insertNewColorData("B04C6A","Cadillac","{\"rgb\" : [176, 76, 106]}");
        colorNameDB.insertNewColorData("6F440C","Cafe Royale","{\"rgb\" : [111, 68, 12]}");
        colorNameDB.insertNewColorData("E0C095","Calico","{\"rgb\" : [224, 192, 149]}");
        colorNameDB.insertNewColorData("FE9D04","California","{\"rgb\" : [254, 157, 4]}");
        colorNameDB.insertNewColorData("31728D","Calypso","{\"rgb\" : [49, 114, 141]}");
        colorNameDB.insertNewColorData("00581A","Camarone","{\"rgb\" : [0, 88, 26]}");
        colorNameDB.insertNewColorData("893456","Camelot","{\"rgb\" : [137, 52, 86]}");
        colorNameDB.insertNewColorData("D9B99B","Cameo","{\"rgb\" : [217, 185, 155]}");
        colorNameDB.insertNewColorData("3C3910","Camouflage","{\"rgb\" : [60, 57, 16]}");
        colorNameDB.insertNewColorData("78866B","Camouflage Green","{\"rgb\" : [120, 134, 107]}");
        colorNameDB.insertNewColorData("D591A4","Can Can","{\"rgb\" : [213, 145, 164]}");
        colorNameDB.insertNewColorData("F3FB62","Canary","{\"rgb\" : [243, 251, 98]}");
        colorNameDB.insertNewColorData("FCD917","Candlelight","{\"rgb\" : [252, 217, 23]}");
        colorNameDB.insertNewColorData("FBEC5D","Candy Corn","{\"rgb\" : [251, 236, 93]}");
        colorNameDB.insertNewColorData("251706","Cannon Black","{\"rgb\" : [37, 23, 6]}");
        colorNameDB.insertNewColorData("894367","Cannon Pink","{\"rgb\" : [137, 67, 103]}");
        colorNameDB.insertNewColorData("3C4443","Cape Cod","{\"rgb\" : [60, 68, 67]}");
        colorNameDB.insertNewColorData("FEE5AC","Cape Honey","{\"rgb\" : [254, 229, 172]}");
        colorNameDB.insertNewColorData("A26645","Cape Palliser","{\"rgb\" : [162, 102, 69]}");
        colorNameDB.insertNewColorData("DCEDB4","Caper","{\"rgb\" : [220, 237, 180]}");
        colorNameDB.insertNewColorData("FFDDAF","Caramel","{\"rgb\" : [255, 221, 175]}");
        colorNameDB.insertNewColorData("EEEEE8","Cararra","{\"rgb\" : [238, 238, 232]}");
        colorNameDB.insertNewColorData("01361C","Cardin Green","{\"rgb\" : [1, 54, 28]}");
        colorNameDB.insertNewColorData("C41E3A","Cardinal","{\"rgb\" : [196, 30, 58]}");
        colorNameDB.insertNewColorData("8C055E","Cardinal Pink","{\"rgb\" : [140, 5, 94]}");
        colorNameDB.insertNewColorData("D29EAA","Careys Pink","{\"rgb\" : [210, 158, 170]}");
        colorNameDB.insertNewColorData("00CC99","Caribbean Green","{\"rgb\" : [0, 204, 153]}");
        colorNameDB.insertNewColorData("EA88A8","Carissma","{\"rgb\" : [234, 136, 168]}");
        colorNameDB.insertNewColorData("F3FFD8","Carla","{\"rgb\" : [243, 255, 216]}");
        colorNameDB.insertNewColorData("960018","Carmine","{\"rgb\" : [150, 0, 24]}");
        colorNameDB.insertNewColorData("5C2E01","Carnaby Tan","{\"rgb\" : [92, 46, 1]}");
        colorNameDB.insertNewColorData("F95A61","Carnation","{\"rgb\" : [249, 90, 97]}");
        colorNameDB.insertNewColorData("F9E0ED","Carousel Pink","{\"rgb\" : [249, 224, 237]}");
        colorNameDB.insertNewColorData("FFA6C9","Carnation Pink","{\"rgb\" : [255, 166, 201]}");
        colorNameDB.insertNewColorData("ED9121","Carrot Orange","{\"rgb\" : [237, 145, 33]}");
        colorNameDB.insertNewColorData("F8B853","Casablanca","{\"rgb\" : [248, 184, 83]}");
        colorNameDB.insertNewColorData("2F6168","Casal","{\"rgb\" : [47, 97, 104]}");
        colorNameDB.insertNewColorData("8BA9A5","Cascade","{\"rgb\" : [139, 169, 165]}");
        colorNameDB.insertNewColorData("E6BEA5","Cashmere","{\"rgb\" : [230, 190, 165]}");
        colorNameDB.insertNewColorData("ADBED1","Casper","{\"rgb\" : [173, 190, 209]}");
        colorNameDB.insertNewColorData("52001F","Castro","{\"rgb\" : [82, 0, 31]}");
        colorNameDB.insertNewColorData("062A78","Catalina Blue","{\"rgb\" : [6, 42, 120]}");
        colorNameDB.insertNewColorData("EEF6F7","Catskill White","{\"rgb\" : [238, 246, 247]}");
        colorNameDB.insertNewColorData("E3BEBE","Cavern Pink","{\"rgb\" : [227, 190, 190]}");
        colorNameDB.insertNewColorData("3E1C14","Cedar","{\"rgb\" : [62, 28, 2]}");
        colorNameDB.insertNewColorData("711A00","Cedar Wood Finish","{\"rgb\" : [113, 26, 0]}");
        colorNameDB.insertNewColorData("ACE1AF","Celadon","{\"rgb\" : [172, 225, 175]}");
        colorNameDB.insertNewColorData("B8C25D","Celery","{\"rgb\" : [184, 194, 93]}");
        colorNameDB.insertNewColorData("D1D2CA","Celeste","{\"rgb\" : [209, 210, 202]}");
        colorNameDB.insertNewColorData("1E385B","Cello","{\"rgb\" : [30, 56, 91]}");
        colorNameDB.insertNewColorData("163222","Celtic","{\"rgb\" : [22, 50, 34]}");
        colorNameDB.insertNewColorData("8D7662","Cement","{\"rgb\" : [141, 118, 98]}");
        colorNameDB.insertNewColorData("FCFFF9","Ceramic","{\"rgb\" : [252, 255, 249]}");
        colorNameDB.insertNewColorData("DA3287","Cerise","{\"rgb\" : [218, 50, 135]}");
        colorNameDB.insertNewColorData("DE3163","Cerise Red","{\"rgb\" : [222, 49, 99]}");
        colorNameDB.insertNewColorData("02A4D3","Cerulean","{\"rgb\" : [2, 164, 211]}");
        colorNameDB.insertNewColorData("2A52BE","Cerulean Blue","{\"rgb\" : [42, 82, 190]}");
        colorNameDB.insertNewColorData("FFF4F3","Chablis","{\"rgb\" : [255, 244, 243]}");
        colorNameDB.insertNewColorData("516E3D","Chalet Green","{\"rgb\" : [81, 110, 61]}");
        colorNameDB.insertNewColorData("EED794","Chalky","{\"rgb\" : [238, 215, 148]}");
        colorNameDB.insertNewColorData("354E8C","Chambray","{\"rgb\" : [53, 78, 140]}");
        colorNameDB.insertNewColorData("EDDCB1","Chamois","{\"rgb\" : [237, 220, 177]}");
        colorNameDB.insertNewColorData("FAECCC","Champagne","{\"rgb\" : [250, 236, 204]}");
        colorNameDB.insertNewColorData("F8C3DF","Chantilly","{\"rgb\" : [248, 195, 223]}");
        colorNameDB.insertNewColorData("292937","Charade","{\"rgb\" : [41, 41, 55]}");
        colorNameDB.insertNewColorData("FFF3F1","Chardon","{\"rgb\" : [255, 243, 241]}");
        colorNameDB.insertNewColorData("FFCD8C","Chardonnay","{\"rgb\" : [255, 205, 140]}");
        colorNameDB.insertNewColorData("BAEEF9","Charlotte","{\"rgb\" : [186, 238, 249]}");
        colorNameDB.insertNewColorData("D47494","Charm","{\"rgb\" : [212, 116, 148]}");
        colorNameDB.insertNewColorData("7FFF00","Chartreuse","{\"rgb\" : [127, 255, 0]}");
        colorNameDB.insertNewColorData("DFFF00","Chartreuse Yellow","{\"rgb\" : [223, 255, 0]}");
        colorNameDB.insertNewColorData("40A860","Chateau Green","{\"rgb\" : [64, 168, 96]}");
        colorNameDB.insertNewColorData("BDB3C7","Chatelle","{\"rgb\" : [189, 179, 199]}");
        colorNameDB.insertNewColorData("175579","Chathams Blue","{\"rgb\" : [23, 85, 121]}");
        colorNameDB.insertNewColorData("83AA5D","Chelsea Cucumber","{\"rgb\" : [131, 170, 93]}");
        colorNameDB.insertNewColorData("9E5302","Chelsea Gem","{\"rgb\" : [158, 83, 2]}");
        colorNameDB.insertNewColorData("DFCD6F","Chenin","{\"rgb\" : [223, 205, 111]}");
        colorNameDB.insertNewColorData("FCDA98","Cherokee","{\"rgb\" : [252, 218, 152]}");
        colorNameDB.insertNewColorData("2A0359","Cherry Pie","{\"rgb\" : [42, 3, 89]}");
        colorNameDB.insertNewColorData("651A14","Cherrywood","{\"rgb\" : [101, 26, 20]}");
        colorNameDB.insertNewColorData("F8D9E9","Cherub","{\"rgb\" : [248, 217, 233]}");
        colorNameDB.insertNewColorData("B94E48","Chestnut","{\"rgb\" : [185, 78, 72]}");
        colorNameDB.insertNewColorData("CD5C5C","Chestnut Rose","{\"rgb\" : [205, 92, 92]}");
        colorNameDB.insertNewColorData("8581D9","Chetwode Blue","{\"rgb\" : [133, 129, 217]}");
        colorNameDB.insertNewColorData("5D5C58","Chicago","{\"rgb\" : [93, 92, 88]}");
        colorNameDB.insertNewColorData("F1FFC8","Chiffon","{\"rgb\" : [241, 255, 200]}");
        colorNameDB.insertNewColorData("F77703","Chilean Fire","{\"rgb\" : [247, 119, 3]}");
        colorNameDB.insertNewColorData("FFFDE6","Chilean Heath","{\"rgb\" : [255, 253, 230]}");
        colorNameDB.insertNewColorData("FCFFE7","China Ivory","{\"rgb\" : [252, 255, 231]}");
        colorNameDB.insertNewColorData("CEC7A7","Chino","{\"rgb\" : [206, 199, 167]}");
        colorNameDB.insertNewColorData("A8E3BD","Chinook","{\"rgb\" : [168, 227, 189]}");
        colorNameDB.insertNewColorData("370202","Chocolate","{\"rgb\" : [55, 2, 2]}");
        colorNameDB.insertNewColorData("33036B","Christalle","{\"rgb\" : [51, 3, 107]}");
        colorNameDB.insertNewColorData("67A712","Christi","{\"rgb\" : [103, 167, 18]}");
        colorNameDB.insertNewColorData("E7730A","Christine","{\"rgb\" : [231, 115, 10]}");
        colorNameDB.insertNewColorData("E8F1D4","Chrome White","{\"rgb\" : [232, 241, 212]}");
        colorNameDB.insertNewColorData("0E0E18","Cinder","{\"rgb\" : [14, 14, 24]}");
        colorNameDB.insertNewColorData("FDE1DC","Cinderella","{\"rgb\" : [253, 225, 220]}");
        colorNameDB.insertNewColorData("E34234","Cinnabar","{\"rgb\" : [227, 66, 52)]}");
        colorNameDB.insertNewColorData("7B3F00","Cinnamon","{\"rgb\" : [123, 63, 0]}");
        colorNameDB.insertNewColorData("55280C","Cioccolato","{\"rgb\" : [85, 40, 12]}");
        colorNameDB.insertNewColorData("FAF7D6","Citrine White","{\"rgb\" : [250, 247, 214]}");
        colorNameDB.insertNewColorData("9EA91F","Citron","{\"rgb\" : [158, 169, 31]}");
        colorNameDB.insertNewColorData("A1C50A","Citrus","{\"rgb\" : [161, 197, 10]}");
        colorNameDB.insertNewColorData("480656","Clairvoyant","{\"rgb\" : [72, 6, 86]}");
        colorNameDB.insertNewColorData("D4B6AF","Clam Shell","{\"rgb\" : [212, 182, 175]}");
        colorNameDB.insertNewColorData("7F1734","Claret","{\"rgb\" : [127, 23, 52]}");
        colorNameDB.insertNewColorData("FBCCE7","Classic Rose","{\"rgb\" : [251, 204, 231]}");
        colorNameDB.insertNewColorData("BDC8B3","Clay Ash","{\"rgb\" : [189, 200, 179]}");
        colorNameDB.insertNewColorData("8A8360","Clay Creek","{\"rgb\" : [138, 131, 96]}");
        colorNameDB.insertNewColorData("E9FFFD","Clear Day","{\"rgb\" : [233, 255, 253]}");
        colorNameDB.insertNewColorData("E96E00","Clementine","{\"rgb\" : [233, 110, 0]}");
        colorNameDB.insertNewColorData("371D09","Clinker","{\"rgb\" : [55, 29, 9]}");
        colorNameDB.insertNewColorData("C7C4BF","Cloud","{\"rgb\" : [199, 196, 191]}");
        colorNameDB.insertNewColorData("202E54","Cloud Burst","{\"rgb\" : [32, 46, 84]}");
        colorNameDB.insertNewColorData("ACA59F","Cloudy","{\"rgb\" : [172, 165, 159]}");
        colorNameDB.insertNewColorData("384910","Clover","{\"rgb\" : [56, 73, 16]}");
        colorNameDB.insertNewColorData("0047AB","Cobalt","{\"rgb\" : [0, 71, 171]}");
        colorNameDB.insertNewColorData("481C1C","Cocoa Bean","{\"rgb\" : [72, 28, 28]}");
        colorNameDB.insertNewColorData("301F1E","Cocoa Brown","{\"rgb\" : [48, 31, 30]}");
        colorNameDB.insertNewColorData("F8F7DC","Coconut Cream","{\"rgb\" : [248, 247, 220]}");
        colorNameDB.insertNewColorData("0B0B0B","Cod Gray","{\"rgb\" : [11, 11, 11]}");
        colorNameDB.insertNewColorData("706555","Coffee","{\"rgb\" : [112, 101, 85]}");
        colorNameDB.insertNewColorData("2A140E","Coffee Bean","{\"rgb\" : [42, 20, 14]}");
        colorNameDB.insertNewColorData("9F381D","Cognac","{\"rgb\" : [159, 56, 29]}");
        colorNameDB.insertNewColorData("3F2500","Cola","{\"rgb\" : [63, 37, 0]}");
        colorNameDB.insertNewColorData("ABA0D9","Cold Purple","{\"rgb\" : [171, 160, 217]}");
        colorNameDB.insertNewColorData("CEBABA","Cold Turkey","{\"rgb\" : [206, 186, 186]}");
        colorNameDB.insertNewColorData("FFEDBC","Colonial White","{\"rgb\" : [255, 237, 188]}");
        colorNameDB.insertNewColorData("5C5D75","Comet","{\"rgb\" : [92, 93, 117]}");
        colorNameDB.insertNewColorData("517C66","Como","{\"rgb\" : [81, 124, 102]}");
        colorNameDB.insertNewColorData("C9D9D2","Conch","{\"rgb\" : [201, 217, 210]}");
        colorNameDB.insertNewColorData("7C7B7A","Concord","{\"rgb\" : [124, 123, 122]}");
        colorNameDB.insertNewColorData("F2F2F2","Concrete","{\"rgb\" : [242, 242, 242]}");
        colorNameDB.insertNewColorData("E9D75A","Confetti","{\"rgb\" : [233, 215, 90]}");
        colorNameDB.insertNewColorData("593737","Congo Brown","{\"rgb\" : [89, 55, 55]}");
        colorNameDB.insertNewColorData("02478E","Congress Blue","{\"rgb\" : [2, 71, 142]}");
        colorNameDB.insertNewColorData("ACDD4D","Conifer","{\"rgb\" : [172, 221, 77]}");
        colorNameDB.insertNewColorData("C6726B","Contessa","{\"rgb\" : [198, 114, 107)]}");
        colorNameDB.insertNewColorData("B87333","Copper","{\"rgb\" : [184, 115, 51]}");
        colorNameDB.insertNewColorData("7E3A15","Copper Canyon","{\"rgb\" : [126, 58, 21]}");
        colorNameDB.insertNewColorData("996666","Copper Rose","{\"rgb\" : [153, 102, 102]}");
        colorNameDB.insertNewColorData("944747","Copper Rust","{\"rgb\" : [148, 71, 71]}");
        colorNameDB.insertNewColorData("DA8A67","Copperfield","{\"rgb\" : [218, 138, 103]}");
        colorNameDB.insertNewColorData("FF7F50","Coral","{\"rgb\" : [255, 127, 80]}");
        colorNameDB.insertNewColorData("FF4040","Coral Red","{\"rgb\" : [255, 64, 64]}");
        colorNameDB.insertNewColorData("C7BCA2","Coral Reef","{\"rgb\" : [199, 188, 162]}");
        colorNameDB.insertNewColorData("A86B6B","Coral Tree","{\"rgb\" : [168, 107, 107]}");
        colorNameDB.insertNewColorData("606E68","Corduroy","{\"rgb\" : [96, 110, 104]}");
        colorNameDB.insertNewColorData("C4D0B0","Coriander","{\"rgb\" : [196, 208, 176]}");
        colorNameDB.insertNewColorData("40291D","Cork","{\"rgb\" : [64, 41, 29]}");
        colorNameDB.insertNewColorData("E7BF05","Corn","{\"rgb\" : [231, 191, 5]}");
        colorNameDB.insertNewColorData("F8FACD","Corn Field","{\"rgb\" : [248, 250, 205]}");
        colorNameDB.insertNewColorData("8B6B0B","Corn Harvest","{\"rgb\" : [139, 107, 11]}");
        colorNameDB.insertNewColorData("93CCEA","Cornflower","{\"rgb\" : [147, 204, 234]}");
        colorNameDB.insertNewColorData("6495ED","Cornflower Blue","{\"rgb\" : [100, 149, 237]}");
        colorNameDB.insertNewColorData("FFB0AC","Cornflower Lilac","{\"rgb\" : [255, 176, 172]}");
        colorNameDB.insertNewColorData("FAD3A2","Corvette","{\"rgb\" : [250, 211, 162]}");
        colorNameDB.insertNewColorData("76395D","Cosmic","{\"rgb\" : [118, 57, 9]}");
        colorNameDB.insertNewColorData("FFD8D9","Cosmos","{\"rgb\" : [255, 216, 217]}");
        colorNameDB.insertNewColorData("615D30","Costa Del Sol","{\"rgb\" : [97, 93, 48]}");
        colorNameDB.insertNewColorData("FFB7D5","Cotton Candy","{\"rgb\" : [255, 183, 213]}");
        colorNameDB.insertNewColorData("C2BDB6","Cotton Seed","{\"rgb\" : [194, 189, 182]}");
        colorNameDB.insertNewColorData("01371A","County Green","{\"rgb\" : [1, 55, 26]}");
        colorNameDB.insertNewColorData("4D282D","Cowboy","{\"rgb\" : [77, 40, 45]}");
        colorNameDB.insertNewColorData("B95140","Crail","{\"rgb\" : [185, 81, 64]}");
        colorNameDB.insertNewColorData("DB5079","Cranberry","{\"rgb\" : [219, 80, 121]}");
        colorNameDB.insertNewColorData("462425","Crater Brown","{\"rgb\" : [70, 36, 37]}");
        colorNameDB.insertNewColorData("FFFDD0","Cream","{\"rgb\" : [255, 253, 208]}");
        colorNameDB.insertNewColorData("FFE5A0","Cream Brulee","{\"rgb\" : [255, 229, 160]}");
        colorNameDB.insertNewColorData("F5C85C","Cream Can","{\"rgb\" : [245, 200, 92]}");
        colorNameDB.insertNewColorData("1E0F04","Creole","{\"rgb\" : [30, 15, 4]}");
        colorNameDB.insertNewColorData("737829","Crete","{\"rgb\" : [115, 120, 41]}");
        colorNameDB.insertNewColorData("DC143C","Crimson","{\"rgb\" : [220, 20, 60]}");
        colorNameDB.insertNewColorData("736D58","Crocodile","{\"rgb\" : [115, 109, 88]}");
        colorNameDB.insertNewColorData("771F1F","Crown of Thorns","{\"rgb\" : [119, 31, 31]}");
        colorNameDB.insertNewColorData("1C1208","Crowshead","{\"rgb\" : [28, 18, 8]}");
        colorNameDB.insertNewColorData("B5ECDF","Cruise","{\"rgb\" : [181, 236, 223]}");
        colorNameDB.insertNewColorData("004816","Crusoe","{\"rgb\" : [0, 72, 22]}");
        colorNameDB.insertNewColorData("FD7B33","Crusta","{\"rgb\" : [253, 123, 51]}");
        colorNameDB.insertNewColorData("924321","Cumin","{\"rgb\" : [146, 67, 33]}");
        colorNameDB.insertNewColorData("FDFFD5","Cumulus","{\"rgb\" : [253, 255, 213]}");
        colorNameDB.insertNewColorData("FBBEDA","Cupid","{\"rgb\" : [251, 190, 218]}");
        colorNameDB.insertNewColorData("2596D1","Curious Blue","{\"rgb\" : [37, 150, 209]}");
        colorNameDB.insertNewColorData("507672","Cutty Sark","{\"rgb\" : [80, 118, 114]}");
        colorNameDB.insertNewColorData("00FFFF","Cyan / Aqua","{\"rgb\" : [0, 255, 255]}");
        colorNameDB.insertNewColorData("003E40","Cyprus","{\"rgb\" : [0, 62, 64]}");
        colorNameDB.insertNewColorData("012731","Daintree","{\"rgb\" : [1, 39, 49]}");
        colorNameDB.insertNewColorData("F9E4BC","Dairy Cream","{\"rgb\" : [249, 228, 188]}");
        colorNameDB.insertNewColorData("4F2398","Daisy Bush","{\"rgb\" : [79, 35, 152]}");
        colorNameDB.insertNewColorData("6E4B26","Dallas","{\"rgb\" : [110, 75, 38]}");
        colorNameDB.insertNewColorData("FED85D","Dandelion","{\"rgb\" : [254, 216, 93]}");
        colorNameDB.insertNewColorData("6093D1","Danube","{\"rgb\" : [96, 147, 209]}");
        colorNameDB.insertNewColorData("0000C8","Dark Blue","{\"rgb\" : [0, 0, 200]}");
        colorNameDB.insertNewColorData("770F05","Dark Burgundy","{\"rgb\" : [119, 15, 5]}");
        colorNameDB.insertNewColorData("3C2005","Dark Ebony","{\"rgb\" : [60, 32, 5]}");
        colorNameDB.insertNewColorData("0A480D","Dark Fern","{\"rgb\" : [10, 72, 13]}");
        colorNameDB.insertNewColorData("661010","Dark Tan","{\"rgb\" : [102, 16, 16]}");
        colorNameDB.insertNewColorData("A6A29A","Dawn","{\"rgb\" : [66, 162, 154]}");
        colorNameDB.insertNewColorData("F3E9E5","Dawn Pink","{\"rgb\" : [243, 233, 229]}");
        colorNameDB.insertNewColorData("7AC488","De York","{\"rgb\" : [122, 196, 136]}");
        colorNameDB.insertNewColorData("D2DA97","Deco","{\"rgb\" : [210, 218, 151]}");
        colorNameDB.insertNewColorData("220878","Deep Blue","{\"rgb\" : [34, 8, 120]}");
        colorNameDB.insertNewColorData("E47698","Deep Blush","{\"rgb\" : [228, 118, 152]}");
        colorNameDB.insertNewColorData("4A3004","Deep Bronze","{\"rgb\" : [74, 48, 4]}");
        colorNameDB.insertNewColorData("007BA7","Deep Cerulean","{\"rgb\" : [0, 123, 167]}");
        colorNameDB.insertNewColorData("051040","Deep Cove","{\"rgb\" : [5, 16, 64]}");
        colorNameDB.insertNewColorData("002900","Deep Fir","{\"rgb\" : [0, 41, 0)]}");
        colorNameDB.insertNewColorData("182D09","Deep Forest Green","{\"rgb\" : [24, 45, 9]}");
        colorNameDB.insertNewColorData("1B127B","Deep Koamaru","{\"rgb\" : [27, 18, 123]}");
        colorNameDB.insertNewColorData("412010","Deep Oak","{\"rgb\" : [65, 32, 16]}");
        colorNameDB.insertNewColorData("082567","Deep Sapphire","{\"rgb\" : [8, 37, 103]}");
        colorNameDB.insertNewColorData("01826B","Deep Sea","{\"rgb\" : [1, 130, 107]}");
        colorNameDB.insertNewColorData("095859","Deep Sea Green","{\"rgb\" : [9, 88, 89]}");
        colorNameDB.insertNewColorData("003532","Deep Teal","{\"rgb\" : [0, 53, 50]}");
        colorNameDB.insertNewColorData("B09A95","Del Rio","{\"rgb\" : [176, 154, 149]}");
        colorNameDB.insertNewColorData("396413","Dell","{\"rgb\" : [57, 100, 19]}");
        colorNameDB.insertNewColorData("A4A49D","Delta","{\"rgb\" : [164, 164, 157]}");
        colorNameDB.insertNewColorData("7563A8","Deluge","{\"rgb\" : [117, 99, 168]}");
        colorNameDB.insertNewColorData("1560BD","Denim","{\"rgb\" : [21, 96, 189]}");
        colorNameDB.insertNewColorData("FFEED8","Derby","{\"rgb\" : [255, 238, 216]}");
        colorNameDB.insertNewColorData("AE6020","Desert","{\"rgb\" : [174, 96, 32]}");
        colorNameDB.insertNewColorData("EDC9AF","Desert Sand","{\"rgb\" : [237, 201, 175]}");
        colorNameDB.insertNewColorData("F8F8F7","Desert Storm","{\"rgb\" : [248, 248, 247]}");
        colorNameDB.insertNewColorData("EAFFFE","Dew","{\"rgb\" : [234, 255, 254]}");
        colorNameDB.insertNewColorData("DB995E","Di Serria","{\"rgb\" : [219, 153, 94]}");
        colorNameDB.insertNewColorData("130000","Diesel","{\"rgb\" : [19, 0, 0]}");
        colorNameDB.insertNewColorData("5D7747","Dingley","{\"rgb\" : [93, 119, 71]}");
        colorNameDB.insertNewColorData("871550","Disco","{\"rgb\" : [135, 21, 80]}");
        colorNameDB.insertNewColorData("E29418","Dixie","{\"rgb\" : [226, 148, 24]}");
        colorNameDB.insertNewColorData("1E90FF","Dodger Blue","{\"rgb\" : [30, 144, 255]}");
        colorNameDB.insertNewColorData("F9FF8B","Dolly","{\"rgb\" : [249, 255, 139]}");
        colorNameDB.insertNewColorData("646077","Dolphin","{\"rgb\" : [100, 96, 119]}");
        colorNameDB.insertNewColorData("8E775E","Domino","{\"rgb\" : [142, 119, 94]}");
        colorNameDB.insertNewColorData("5D4C51","Don Juan","{\"rgb\" : [93, 76, 81]}");
        colorNameDB.insertNewColorData("A69279","Donkey Brown","{\"rgb\" : [166, 146, 121]}");
        colorNameDB.insertNewColorData("6B5755","Dorado","{\"rgb\" : [107, 87, 85]}");
        colorNameDB.insertNewColorData("EEE3AD","Double Colonial White","{\"rgb\" : [238, 227, 173]}");
        colorNameDB.insertNewColorData("FCF4D0","Double Pearl Lusta","{\"rgb\" : [252, 244, 208]}");
        colorNameDB.insertNewColorData("E6D7B9","Double Spanish White","{\"rgb\" : [230, 215, 185]}");
        colorNameDB.insertNewColorData("6D6C6C","Dove Gray","{\"rgb\" : [109, 108, 108]}");
        colorNameDB.insertNewColorData("092256","Downriver","{\"rgb\" : [9, 34, 86]}");
        colorNameDB.insertNewColorData("6FD0C5","Downy","{\"rgb\" : [111, 208, 197)]}");
        colorNameDB.insertNewColorData("AF8751","Driftwood","{\"rgb\" : [175, 135, 81]}");
        colorNameDB.insertNewColorData("FDF7AD","Drover","{\"rgb\" : [253, 247, 173]}");
        colorNameDB.insertNewColorData("A899E6","Dull Lavender","{\"rgb\" : [168, 153, 230]}");
        colorNameDB.insertNewColorData("383533","Dune","{\"rgb\" : [56, 53, 51]}");
        colorNameDB.insertNewColorData("E5CCC9","Dust Storm","{\"rgb\" : [229, 204, 201]}");
        colorNameDB.insertNewColorData("A8989B","Dusty Gray","{\"rgb\" : [168, 152, 155]}");
        colorNameDB.insertNewColorData("B6BAA4","Eagle","{\"rgb\" : [182, 186, 164]}");
        colorNameDB.insertNewColorData("C9B93B","Earls Green","{\"rgb\" : [201, 185, 59]}");
        colorNameDB.insertNewColorData("FFF9E6","Early Dawn","{\"rgb\" : [255, 249, 230]}");
        colorNameDB.insertNewColorData("414C7D","East Bay","{\"rgb\" : [65, 76, 125]}");
        colorNameDB.insertNewColorData("AC91CE","East Side","{\"rgb\" : [172, 145, 206]}");
        colorNameDB.insertNewColorData("1E9AB0","Eastern Blue","{\"rgb\" : [30, 154, 176]}");
        colorNameDB.insertNewColorData("E9E3E3","Ebb","{\"rgb\" : [233, 227, 227]}");
        colorNameDB.insertNewColorData("0C0B1D","Ebony","{\"rgb\" : [12, 11, 29]}");
        colorNameDB.insertNewColorData("26283B","Ebony Clay","{\"rgb\" : [38, 40, 59]}");
        colorNameDB.insertNewColorData("311C17","Eclipse","{\"rgb\" : [49, 28, 23]}");
        colorNameDB.insertNewColorData("F5F3E5","Ecru White","{\"rgb\" : [245, 243, 229]}");
        colorNameDB.insertNewColorData("FA7814","Ecstasy","{\"rgb\" : [250, 120, 20]}");
        colorNameDB.insertNewColorData("105852","Eden","{\"rgb\" : [16, 88, 82]}");
        colorNameDB.insertNewColorData("C8E3D7","Edgewater","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A2AEAB","Edward","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF4DD","Egg Sour","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFEFC1","Egg White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("614051","Eggplant","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1E1708","El Paso","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8F3E33","El Salva","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CCFF00","Electric Lime","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B00FF","Electric Violet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("123447","Elephant","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("088370","Elf Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1C7C7D","Elm","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("50C878","Emerald","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6C3082","Eminence","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("514649","Emperor","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("817377","Empress","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0056A7","Endeavour","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8DD5C","Energy Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("022D15","English Holly","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3E2B23","English Walnut","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8BA690","Envy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E1BC64","Equator","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("612718","Espresso","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("211A0E","Eternity","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("278A5B","Eucalyptus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CFA39D","Eunry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("024E46","Evening Sea","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1C402E","Everglade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("427977","Faded Jade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFEFEC","Fair Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7F626D","Falcon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECEBBD","Fall Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("801818","Falu Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FAF3F0","Fantasy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("796A78","Fedora","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9FDD8C","Feijoa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("63B76C","Fern","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("657220","Fern Frond","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4F7942","Fern Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("704F50","Ferra","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBE96C","Festival","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0FCEA","Feta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B35213","Fiery Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("626649","Finch","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("556D56","Finlandia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("692D54","Finn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("405169","Fiord","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AA4203","Fire","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E89928","Fire Bush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0E2A30","Firefly","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DA5B38","Flame Pea","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF7D07","Flamenco","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F2552A","Flamingo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEDC82","Flax","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7B8265","Flax Smoke","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFCBA4","Flesh","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6F6A61","Flint","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A2006D","Flirt","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CA3435","Flush Mahogany","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF7F00","Flush Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D8FCFA","Foam","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D7D0FF","Fog","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CBCAB6","Foggy Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("228B22","Forest Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF1EE","Forget Me Not","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("56B4BE","Fountain Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDEB3","Frangipani","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BDBDC6","French Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECC7EE","French Lilac","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BDEDFD","French Pass","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F64A8A","French Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("990066","Fresh Eggplant","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("807E79","Friar Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B1E2C1","Fringy Flower","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F57584","Froly","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDF5DD","Frost","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DBFFF8","Frosted Mint","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4F6E7","Frostee","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4F9D5D","Fruit Salad","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7A58C1","Fuchsia Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C154C1","Fuchsia Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BEDE0D","Fuego","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECA927","Fuel Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1959A8","Fun Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("016D39","Fun Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("54534D","Fuscous Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C45655","Fuzzy Wuzzy Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("163531","Gable Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EFEFEF","Gallery","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DCB20C","Galliano","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E49B0F","Gamboge","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D18F1B","Geebung","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("15736B","Genoa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FB8989","Geraldine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D4DFE2","Geyser","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C7C9D5","Ghost","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("523C94","Gigas","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B8B56A","Gimblet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E8F2EB","Gin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF9E2","Gin Fizz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8E4BF","Givry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("80B3C4","Glacier","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("61845F","Glade Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("726D4E","Go Ben","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3D7D52","Goblin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFD700","Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F18200","Gold Drop","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E6BE8A","Gold Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DEBA13","Gold Tips","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E28913","Golden Bell","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0D52D","Golden Dream","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5FB3D","Golden Fizz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDE295","Golden Glow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DAA520","Golden Grass","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0DB7D","Golden Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFCC5C","Golden Tainoi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FCD667","Goldenrod","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("261414","Gondola","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0B1107","Gordons Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF14F","Gorse","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("069B81","Gossamer","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D2F8B0","Gossip","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6D92A1","Gothic","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2F3CB3","Governor Bay","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4D5B7","Grain Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFD38C","Grandis","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8D8974","Granite Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D5F6E3","Granny Apple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("84A0A0","Granny Smith","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9DE093","Granny Smith Apple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("381A51","Grape","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("251607","Graphite","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4A444B","Gravel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("808080","Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("465945","Gray Asparagus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A2AAB3","Gray Chateau","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C3C3BD","Gray Nickel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E7ECE6","Gray Nurse","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A9A491","Gray Olive","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C1BECD","Gray Suit","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("00FF00","Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("01A368","Green Haze","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("24500F","Green House","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("25311C","Green Kelp","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("436A0D","Green Leaf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CBD3B0","Green Mist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1D6142","Green Pea","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A4AF6E","Green Smoke","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B8C1B1","Green Spring","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("032B52","Green Vogue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("101405","Green Waterloo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E8EBE0","Green White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ADFF2F","Green Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D54600","Grenadier","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BA0101","Guardsman Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("051657","Gulf Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("80B3AE","Gulf Stream","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9DACB7","Gull Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B6D3BF","Gum Leaf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7CA1A6","Gumbo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("414257","Gun Powder","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("828685","Gunsmoke","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9A9577","Gurkha","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("98811B","Hacienda","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6B2A14","Hairy Heath","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1B1035","Haiti","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("85C4CC","Half Baked","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDF6D3","Half Colonial White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF7DE","Half Dutch White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF4DB","Half Spanish White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFEE1","Half and Half","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E5D8AF","Hampton","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3FFF00","Harlequin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E6F2EA","Harp","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E0B974","Harvest Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5590D9","Havelock Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9D5616","Hawaiian Tan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D4E2FC","Hawkes Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("541012","Heath","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B7C3D0","Heather","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B6B095","Heathered Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2B3228","Heavy Metal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DF73FF","Heliotrope","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5E5D3B","Hemlock","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("907874","Hemp","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B6316C","Hibiscus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6F8E63","Highland","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ACA586","Hillary","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6A5D1B","Himalaya","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E6FFE9","Hint of Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBF9F9","Hint of Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FAFDE4","Hint of Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("589AAF","Hippie Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("53824B","Hippie Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AE4560","Hippie Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A1ADB5","Hit Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFAB81","Hit Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C8A528","Hokey Pokey","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("65869F","Hoki","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("011D13","Holly","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F400A1","Hollywood Cerise","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4F1C70","Honey Flower","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDFC84","Honeysuckle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D06DA1","Hopbush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5A87A0","Horizon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("604913","Horses Neck","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D2691E","Hot Cinnamon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF69B4","Hot Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B38007","Hot Toddy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CFF9F3","Humming Bird","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("161D10","Hunter Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("877C7B","Hurricane","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B7A458","Husk","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B1F4E7","Ice Cold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DAF4F0","Iceberg","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F6A4C9","Illusion","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B0E313","Inch Worm","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C3B091","Indian Khaki","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4D1E01","Indian Tan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4F69C6","Indigo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C26B03","Indochine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("002FA7","International Klein Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF4F00","International Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5F3D26","Irish Coffee","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("433120","Iroko","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D4D7D9","Iron","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("676662","Ironside Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("86483C","Ironstone","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFCEE","Island Spice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFFF0","Ivory","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2E0329","Jacaranda","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3A2A6A","Jacarta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2E1905","Jacko Bean","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("20208D","Jacksons Purple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("00A86B","Jade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EF863F","Jaffa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C2E8E5","Jagged Ice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("350E57","Jagger","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("080110","Jaguar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5B3013","Jambalaya","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4EBD3","Janna","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0A6906","Japanese Laurel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("780109","Japanese Maple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D87C63","Japonica","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1FC2C2","Java","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A50B5E","Jazzberry Jam","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("297B9A","Jelly Bean","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B5D2CE","Jet Stream","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("126B40","Jewel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3B1F1F","Jon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEFF9A","Jonquil","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8AB9F1","Jordy Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("544333","Judge Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7C7B82","Jumbo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("29AB87","Jungle Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B4CFD3","Jungle Mist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6D9292","Juniper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECCDB9","Just Right","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5E483E","Kabul","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("004620","Kaitoke Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C6C8BD","Kangaroo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1E1609","Karaka","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFEAD4","Karry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("507096","Kashmir Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("454936","Kelp","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7C1C05","Kenyan Copper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3AB09E","Keppel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BFC921","Key Lime Pie","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0E68C","Khaki","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E1EAD4","Kidnapper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("240C02","Kilamanjaro","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3A6A47","Killarney","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("736C9F","Kimberly","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3E0480","Kingfisher Daisy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E79FC4","Kobi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6E6D57","Kokoda","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8F4B0E","Korma","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFBD5F","Koromiko","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFE772","Kournikova","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("886221","Kumera","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("368716","La Palma","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B3C110","La Rioja","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C6E610","Las Palmas","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C8B568","Laser","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFF66","Laser Lemon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("749378","Laurel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B57EDC","Lavender","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BDBBD7","Lavender Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EE82EE","Lavender Magenta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBAED2","Lavender Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("967BB6","Lavender Purple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBA0E3","Lavender Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF0F5","Lavender blush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("967059","Leather","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDE910","Lemon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFACD","Lemon Chiffon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AC9E22","Lemon Ginger","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9B9E8F","Lemon Grass","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDD5B1","Light Apricot","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E29CD2","Light Orchid","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C9A0DC","Light Wisteria","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FCC01E","Lightning Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C8A2C8","Lilac","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9874D3","Lilac Bush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C8AABF","Lily","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E7F8FF","Lily White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("76BD17","Lima","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BFFF00","Lime","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6F9D02","Limeade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("747D63","Limed Ash","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AC8A56","Limed Oak","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("394851","Limed Spruce","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FAF0E6","Linen","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D9E4F5","Link Water","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AB0563","Lipstick","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("423921","Lisbon Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4D282E","Livid Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEF4DE","Loafer ","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BDC9CE","Loblolly","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2C8C84","Lochinvar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("007EC7","Lochmara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A8AF8E","Locust","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("242A1D","Log Cabin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AAA9CD","Logan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DFCFDB","Lola","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BEA6C3","London Hue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6D0101","Lonestar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("863C3C","Lotus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("460B41","Loulou","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AF9F1C","Lucky","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1A1A68","Lucky Point","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3C493A","Lunar Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A7882C","Luxor Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("697E9A","Lynch","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D9F7FF","Mabel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFB97B","Macaroni and Cheese","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B7F0BE","Madang","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("09255D","Madison","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3F3002","Madras","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF00FF","Magenta / Fuchsia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AAF0D1","Magic Mint","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8F4FF","Magnolia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4E0606","Mahogany","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B06608","Mai Tai","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5D5A0","Maize","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("897D6D","Makara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("444954","Mako","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0BDA51","Malachite","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7DC8F7","Malibu","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("233418","Mallard","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BDB2A1","Malta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8E8190","Mamba","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8D90A1","Manatee","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AD781B","Mandalay","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E25465","Mandy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F2C3B2","Mandys Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E77200","Mango Tango","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5C999","Manhattan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("74C365","Mantis","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B9C90","Mantle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEEF78","Manz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("350036","Mardi Gras","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B98D28","Marigold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBE870","Marigold Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("286ACD","Mariner","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("800000","Maroon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C32148","Maroon Flush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("520C17","Maroon Oak","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0B0F08","Marshland","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AFA09E","Martini","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("363050","Martinique","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8DB9D","Marzipan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("403B38","Masala","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1B659D","Matisse","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B05D54","Matrix","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4E3B41","Matterhorn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E0B0FF","Mauve","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F091A9","Mauvelous","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D8C2D5","Maverick","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AF4035","Medium Carmine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9370DB","Medium Purple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BB3385","Medium Red Violet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4C2D5","Melanie","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("300529","Melanzane","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEBAAD","Melon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C7C1FF","Melrose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E5E5E5","Mercury","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F6F0E6","Merino","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("413C37","Merlin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("831923","Merlot","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("49371B","Metallic Bronze","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("71291D","Metallic Copper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D07D12","Meteor","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3C1F76","Meteorite","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A72525","Mexican Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5F5F6E","Mid Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("011635","Midnight","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("003366","Midnight Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("041004","Midnight Moss","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2D2510","Mikado","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FAFFA4","Milan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B81104","Milano Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF6D4","Milk Punch","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("594433","Millbrook","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8FDD3","Mimosa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E3F988","Mindaro","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("323232","Mine Shaft","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3F5D53","Mineral Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("36747D","Ming","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3F307F","Minsk","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("98FF98","Mint Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F1EEC1","Mint Julep","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C4F4EB","Mint Tulip","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("161928","Mirage","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D1D2DD","Mischka","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C4C4BC","Mist Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7F7589","Mobster","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6E1D14","Moccaccino","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("782D19","Mocha","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C04737","Mojo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFA194","Mona Lisa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B0723","Monarch","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4A3C30","Mondo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B5A27F","Mongoose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8A8389","Monsoon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("83D0C6","Monte Carlo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C7031E","Monza","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7F76D3","Moody Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FCFEDA","Moon Glow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DCDDCC","Moon Mist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D6CEF6","Moon Raker","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9EDEE0","Morning Glory","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("441D00","Morocco Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("504351","Mortar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("036A6E","Mosque","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ADDFAD","Moss Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1AB385","Mountain Meadow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("959396","Mountain Mist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("997A8D","Mountbatten Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B78E5C","Muddy Waters","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AA8B5B","Muesli","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C54B8C","Mulberry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5C0536","Mulberry Wood","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8C472F","Mule Fawn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4E4562","Mulled Wine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDB58","Mustard","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D69188","My Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFB31F","My Sin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E2EBED","Mystic","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4B5D52","Nandor","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ACA494","Napa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDF9F1","Narvik","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B8680","Natural Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDEAD","Navajo White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("000080","Navy Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CBDBD6","Nebula","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFE2C5","Negroni","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF9933","Neon Carrot","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8EABC1","Nepal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7CB7BB","Neptune","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("140600","Nero","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("646E75","Nevada","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F3D69D","New Orleans","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D7837F","New York Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("06A189","Niagara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1F120F","Night Rider","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AA375A","Night Shadz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("193751","Nile Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B7B1B1","Nobel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BAB1A2","Nomad","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A8BD9F","Norway","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C59922","Nugget","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("81422C","Nutmeg","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("683600","Nutmeg Wood Finish","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEEFCE","Oasis","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("02866F","Observatory","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("41AA78","Ocean Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CC7722","Ochre","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E6F8F3","Off Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF9E3","Off Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("281E15","Oil","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("901E1E","Old Brick","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("724A2F","Old Copper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CFB53B","Old Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDF5E6","Old Lace","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("796878","Old Lavender","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C08081","Old Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("808000","Olive","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6B8E23","Olive Drab","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B5B35C","Olive Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B8470","Olive Haze","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("716E10","Olivetone","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9AB973","Olivine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CDF4FF","Onahau","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2F270E","Onion","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A9C6C2","Opal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8E6F70","Opium","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("377475","Oracle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF681F","Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFA000","Orange Peel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C45719","Orange Roughy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEFCED","Orange White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DA70D6","Orchid","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFDF3","Orchid White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9B4703","Oregon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("015E85","Orient","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C69191","Oriental Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F3FBD4","Orinoco","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("878D91","Oslo Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E9F8ED","Ottoman","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2D383A","Outer Space","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF6037","Outrageous Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("384555","Oxford Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("779E86","Oxley","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DAFAFF","Oyster Bay","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E9CECD","Oyster Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A65529","Paarl","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("776F61","Pablo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("009DC4","Pacific Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("778120","Pacifika","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("411F10","Paco","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ADE6C4","Padua","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFF99","Pale Canary","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C0D3B9","Pale Leaf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("988D77","Pale Oyster","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDFEB8","Pale Prim","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFE1F2","Pale Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6E7783","Pale Sky","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C3BFC1","Pale Slate","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("09230F","Palm Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("19330E","Palm Leaf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4F2EE","Pampas","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EAF6EE","Panache","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDCDAB","Pancho","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFEFD5","Papaya Whip","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8D0226","Paprika","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("317D82","Paradiso","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F1E9D2","Parchment","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF46E","Paris Daisy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("26056A","Paris M","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CADCD4","Paris White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("134F19","Parsley","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("77DD77","Pastel Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFD1DC","Pastel Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("639A8F","Patina","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DEF5FF","Pattens Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("260368","Paua","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D7C498","Pavlova","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFE5B4","Peach","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF0DB","Peach Cream","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFCC99","Peach Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDCD6","Peach Schnapps","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FADFAD","Peach Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("782F16","Peanut","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D1E231","Pear","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E8E0D5","Pearl Bush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FCF4DC","Pearl Lusta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("716B56","Peat","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3EABBF","Pelorous","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E3F5E1","Peppermint","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A9BEF2","Perano","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D0BEF8","Perfume","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E1E6D6","Periglacial Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CCCCFF","Periwinkle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C3CDE6","Periwinkle Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1C39BB","Persian Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("00A693","Persian Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("32127A","Persian Indigo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F77FBE","Persian Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("701C1C","Persian Plum","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CC3333","Persian Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FE28A2","Persian Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF6B53","Persimmon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7F3A02","Peru Tan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7C7631","Pesto","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DB9690","Petite Orchid","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("96A8A1","Pewter","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A3807B","Pharlap","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF39D","Picasso","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6E4826","Pickled Bean","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("314459","Pickled Bluewood","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("45B1E8","Picton Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FDD7E4","Pig Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AFBDD9","Pigeon Post","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4B0082","Pigment Indigo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6D5E54","Pine Cone","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C7CD90","Pine Glade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("01796F","Pine Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("171F04","Pine Tree","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFC0CB","Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF66FF","Pink Flamingo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E1C0C8","Pink Flare","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDDF4","Pink Lace","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF1D8","Pink Lady","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF91A4","Pink Salmon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BEB5B7","Pink Swan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C96323","Piper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF4CC","Pipi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFE1DF","Pippin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BA7F03","Pirate Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9DC209","Pistachio","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C0D8B6","Pixie Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF9000","Pizazz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C99415","Pizza","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("27504B","Plantation","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("843179","Plum","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8F021C","Pohutukawa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E5F9F6","Polar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8DA8CC","Polo Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F34723","Pomegranate","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("660045","Pompadour","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EFF2F3","Porcelain","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EAAE69","Porsche","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("251F4F","Port Gore","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFFB4","Portafino","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B9FEE","Portage","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F9E663","Portica","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5E7E2","Pot Pourri","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8C5738","Potters Clay","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BCC9C2","Powder Ash","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B0E0E6","Powder Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9A3820","Prairie Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D0C0E5","Prelude","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0E2EC","Prim","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDEA99","Primrose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF5F1","Provincial Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("003153","Prussian Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CC8899","Puce","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7D2C14","Pueblo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3FC1AA","Puerto Rico","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C2CAC4","Pumice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF7518","Pumpkin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B1610B","Pumpkin Skin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DC4333","Punch","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4D3D14","Punga","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("660099","Purple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("652DC1","Purple Heart","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9678B6","Purple Mountain's Majesty","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF00CC","Purple Pizzazz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E7CD8C","Putty","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFDF4","Quarter Pearl Lusta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F7F2E1","Quarter Spanish White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BD978E","Quicksand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D6D6D1","Quill Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("623F2D","Quincy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0C1911","Racing Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF355E","Radical Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EADAB8","Raffia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B9C8AC","Rainee","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F7B668","Rajah","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2E3222","Rangitoto","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1C1E13","Rangoon Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("727B89","Raven","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D27D46","Raw Sienna","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("734A12","Raw Umber","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF33CC","Razzle Dazzle Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E30B5C","Razzmatazz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3C1206","Rebel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF0000","Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7B3801","Red Beech","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8E0000","Red Berry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DA6A41","Red Damask","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("860111","Red Devil","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF3F34","Red Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6E0902","Red Oxide","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ED0A3F","Red Ribbon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("80341F","Red Robin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D05F04","Red Stage","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C71585","Red Violet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5D1E0F","Redwood","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C9FFA2","Reef","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9F821C","Reef Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("013F6A","Regal Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("86949F","Regent Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AAD6E6","Regent St Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEEBF3","Remy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A86515","Reno Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("002387","Resolution Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2C1632","Revolver","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2E3F62","Rhino","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFEF0","Rice Cake","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEFFE2","Rice Flower","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A85307","Rich Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BBD009","Rio Grande","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4D81C","Ripe Lemon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("410056","Ripe Plum","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8BE6D8","Riptide","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("434C59","River Bed","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EAC674","Rob Roy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("00CCCC","Robin's Egg Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4D3833","Rock","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9EB1CD","Rock Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BA450C","Rock Spray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C9B29B","Rodeo Dust","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("747D83","Rolling Stone","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DE6360","Roman","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("795D4C","Roman Coffee","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFEFD","Romance","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFD2B7","Romantic","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECC54E","Ronchi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A62F20","Roof Terracotta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8E4D1E","Rope","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF007F","Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBB2A3","Rose Bud","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("800B47","Rose Bud Cherry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E7BCB4","Rose Fog","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF6F5","Rose White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BF5500","Rose of Sharon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("65000B","Rosewood","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C6A84B","Roti","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A23B6C","Rouge","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4169E1","Royal Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AB3472","Royal Heath","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6B3FA0","Royal Purple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("796989","Rum","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F9F8E4","Rum Swizzle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("80461B","Russet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("755A57","Russett","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B7410E","Rust","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("480404","Rustic Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("86560A","Rusty Nail","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4C3024","Saddle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("583401","Saddle Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4C430","Saffron","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F9BF58","Saffron Mango","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9EA587","Sage","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B7A214","Sahara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F1E788","Sahara Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B8E0F9","Sail","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("097F4B","Salem","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF8C69","Salmon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEDB8D","Salomie","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("685E6E","Salt Box","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F1F7F2","Saltpan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3A2010","Sambuca","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0B6207","San Felix","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("304B6A","San Juan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("456CAC","San Marino","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("826F65","Sand Dune","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AA8D6F","Sandal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AB917A","Sandrift","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("796D62","Sandstone","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5E7A2","Sandwisp","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFEAC8","Sandy Beach","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4A460","Sandy brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("92000A","Sangria","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8D3D38","Sanguine Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B16D52","Santa Fe","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9FA0B1","Santas Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DED4A4","Sapling","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2F519E","Sapphire","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("555B10","Saratoga","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E6E4D4","Satin Linen","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF5F3","Sauvignon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF4E0","Sazerac","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("675FA6","Scampi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CFFAF4","Scandal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF2400","Scarlet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("431560","Scarlet Gum","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("950015","Scarlett","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("585562","Scarpa Flow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A9B497","Schist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFD800","School bus Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8B847E","Schooner","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0066CC","Science Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2EBFD4","Scooter","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("695F62","Scorpion","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFBDC","Scotch Mist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("66FF66","Screamin' Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBA129","Sea Buckthorn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2E8B57","Sea Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C5DBCA","Sea Mist","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("78A39C","Sea Nymph","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ED989E","Sea Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("80CCEA","Seagull","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("731E8F","Seance","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F1F1F1","Seashell","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF5EE","Seashell Peach","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1B2F11","Seaweed","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0EEFD","Selago","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFBA00","Selective Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("704214","Sepia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2B0202","Sepia Black","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9E5B40","Sepia Skin","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF4E8","Serenade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("837050","Shadow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9AC2B8","Shadow Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AAA5A9","Shady Lady","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4EABD1","Shakespeare","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBFFBA","Shalimar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("33CC99","Shamrock","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("25272C","Shark","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("004950","Sherpa Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("02402C","Sherwood Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E8B9B3","Shilo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6B4E31","Shingle Fawn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("788BBA","Ship Cove","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3E3A44","Ship Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B20931","Shiraz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E292C0","Shocking","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FC0FC0","Shocking Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5F6672","Shuttle Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("646A54","Siam","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F3E7BB","Sidecar","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BDB1A8","Silk","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C0C0C0","Silver","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ACACAC","Silver Chalice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C9C0BB","Silver Rust","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BFC1C2","Silver Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("66B58F","Silver Tree","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9FD7D3","Sinbad","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7A013A","Siren","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("718080","Sirocco","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D3CBBA","Sisal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CAE6DA","Skeptic","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("76D7EA","Sky Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("708090","Slate Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("003399","Smalt","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("51808F","Smalt Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("605B73","Smoky","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F7FAF7","Snow Drift","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4FFD1","Snow Flurry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D6FFDB","Snowy Mint","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E2D8ED","Snuff","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFBF9","Soapstone","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D1C6B4","Soft Amber","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5EDEF","Soft Peach","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("893843","Solid Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF8E2","Solitaire","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EAF6FF","Solitude","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FD7C07","Sorbus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CEB98F","Sorrell Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6A6051","Soya Bean","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("819885","Spanish Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2F5A57","Spectra","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6A442E","Spice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("885342","Spicy Mix","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("74640D","Spicy Mustard","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("816E71","Spicy Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B6D1EA","Spindle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("79DEEC","Spray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("00FF7F","Spring Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("578363","Spring Leaves","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ACCBB1","Spring Rain","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F6FFDC","Spring Sun","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8F6F1","Spring Wood","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C1D7B0","Sprout","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("AAABB7","Spun Pearl","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8F8176","Squirrel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2D569B","St Tropaz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8A8F8A","Stack","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9F9F9C","Star Dust","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E5D7BD","Stark White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECF245","Starship","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4682B4","Steel Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("262335","Steel Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9C3336","Stiletto","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("928573","Stonewall","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("646463","Storm Dust","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("717486","Storm Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("000741","Stratos","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D4BF8D","Straw","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("956387","Strikemaster","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("325D52","Stromboli","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("714AB2","Studio","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BAC7C9","Submarine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F9FFF6","Sugar Cane","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C1F07C","Sulu","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("96BBAB","Summer Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBAC13","Sun","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C9B35B","Sundance","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFB1B3","Sundown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4D422","Sunflower","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E16865","Sunglo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFCC33","Sunglow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FE4C40","Sunset Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF9E2C","Sunshade","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFC901","Supernova","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BBD7C1","Surf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CFE5D2","Surf Crest","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0C7A79","Surfie Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("87AB39","Sushi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("888387","Suva Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("001B1C","Swamp","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ACB78E","Swamp Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DCF0EA","Swans Down","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FBEA8C","Sweet Corn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FD9FA2","Sweet Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D3CDC5","Swirl","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DDD6D5","Swiss Coffee","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("908D39","Sycamore","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A02712","Tabasco","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDB381","Tacao","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D6C562","Tacha","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E97C07","Tahiti Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEF0C8","Tahuna Sands","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B32D29","Tall Poppy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A8A589","Tallow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("991613","Tamarillo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("341515","Tamarind","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D2B48C","Tan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FA9D5A","Tan Hide","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D9DCC1","Tana","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("03163C","Tangaroa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F28500","Tangerine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ED7A1C","Tango","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7B7874","Tapa","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B05E81","Tapestry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E1F6E8","Tara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("073A50","Tarawera","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CFDCCF","Tasman","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("483C32","Taupe","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B3AF95","Taupe Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("692545","Tawny Port","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1E433C","Te Papa Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C1BAB0","Tea","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D0F0C0","Tea Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B19461","Teak","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("008080","Teal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("044259","Teal Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3B000B","Temptress","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CD5700","Tenn","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFE6C7","Tequila","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E2725B","Terracotta","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8F99C","Texas","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFB555","Texas Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B69D98","Thatch","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("403D19","Thatch Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D8BFD8","Thistle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CCCAA8","Thistle Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("33292F","Thunder","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C02B18","Thunderbird","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C1440E","Tia Maria","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C3D1D1","Tiara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("063537","Tiber","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FC80A5","Tickle Me Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F1FFAD","Tidal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BFB8B0","Tide","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("16322C","Timber Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D9D6CF","Timberwolf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F0EEFF","Titan White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9A6E61","Toast","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("715D47","Tobacco Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3A0020","Toledo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1B0245","Tolopea","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3F583B","Tom Thumb","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E79F8C","Tonys Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7C778A","Topaz","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FD0E35","Torch Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0F2D9E","Torea Bay","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("1450AA","Tory Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8D3F3F","Tosca","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("991B07","Totem Pole","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A9BDBF","Tower Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5FB3AC","Tradewind","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E6FFFF","Tranquil","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFDE8","Travertine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FC9C1D","Tree Poppy","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3B2820","Treehouse","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7C881A","Trendy Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8C6495","Trendy Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E64E03","Trinidad","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C3DDF9","Tropical Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("00755E","Tropical Rain Forest","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4A4E5A","Trout","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8A73D6","True V","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("363534","Tuatara","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDDCD","Tuft Bush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EAB33B","Tulip Tree","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DEA681","Tumbleweed","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("353542","Tuna","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4A4244","Tundora","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FAE600","Turbo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B57281","Turkish Rose","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CABB48","Turmeric","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("30D5C8","Turquoise","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("6CDAE7","Turquoise Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2A380B","Turtle Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BD5E2E","Tuscany","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEF3C3","Tusk","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C5994B","Tussock","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF1F9","Tutu","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4CFDE","Twilight","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EEFDFF","Twilight Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C2955D","Twine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("66023C","Tyrian Purple","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("120A8F","Ultramarine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D84437","Valencia","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("350E42","Valentino","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("2B194F","Valhalla","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("49170C","Van Cleef","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D1BEA8","Vanilla","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F3D9DF","Vanilla Ice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFF6DF","Varden","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("72010F","Venetian Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("055989","Venice Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("928590","Venus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5D5E37","Verdigris","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("495400","Verdun Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF4D00","Vermilion","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B14A0B","Vesuvius","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("534491","Victoria","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("549019","Vida Loca","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("64CCDB","Viking","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("983D61","Vin Rouge","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CB8FA9","Viola","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("290C5E","Violent Violet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("240A40","Violet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("991199","Violet Eggplant","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F7468A","Violet Red","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("40826D","Viridian","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("678975","Viridian Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFEFA1","Vis Vis","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("8FD6B4","Vista Blue","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FCF8F7","Vista White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF9980","Vivid Tangerine","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("803790","Vivid Violet","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("533455","Voodoo","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("10121D","Vulcan","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DECBC6","Wafer","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("5A6E9C","Waikawa Gray","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("363C0D","Waiouru","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("773F1A","Walnut","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("788A25","Wasabi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A1E9DE","Water Leaf","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("056F57","Watercourse","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7B7C94","Waterloo ","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DCD747","Wattle","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFDDCF","Watusi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFC0A8","Wax Flower","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F7DBE6","We Peep","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFA500","Web Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4E7F9E","Wedgewood","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B43332","Well Read","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("625119","West Coast","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF910F","West Side","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DCD9D2","Westar ","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F19BAB","Wewak","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F5DEB3","Wheat","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F3EDCF","Wheatfield","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D59A6F","Whiskey","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F7F5FA","Whisper","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFFFF","White","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DDF9F1","White Ice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8F7FC","White Lilac","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F8F0E8","White Linen","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF8FF","White Pointer","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EAE8D4","White Rock","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7A89B8","Wild Blue Yonder","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("ECE090","Wild Rice","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4F4F4","Wild Sand","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FF3399","Wild Strawberry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FD5B78","Wild Watermelon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("B9C46A","Wild Willow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3A686C","William","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DFECDA","Willow Brook","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("65745D","Willow Grove","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("3C0878","Windsor","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("591D35","Wine Berry","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("D5D195","Winter Hazel","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEF4F8","Wisp Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("9771B5","Wisteria","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A4A6D3","Wistful","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFC99","Witch Haze","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("261105","Wood Bark","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("4D5328","Woodland","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("302A0F","Woodrush","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("0C0D0F","Woodsmoke","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("483131","Woody Brown","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("738678","Xanadu","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFFF00","Yellow","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("C5E17A","Yellow Green","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("716338","Yellow Metal","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFAE42","Yellow Orange","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FEA904","Yellow Sea","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("FFC3C0","Your Pink","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("7B6608","Yukon Gold","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("CEC291","Yuma","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("685558","Zambezi","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("DAECD6","Zanah","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E5841B","Zest","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("292319","Zeus","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("BFDBE2","Ziggurat","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EBC2AF","Zinnwaldite","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("F4F8FF","Zircon","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("E4D69B","Zombie","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("A59B91","Zorba","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("044022","Zuccini","{\"rgb\" : [27,20,4]}");
        colorNameDB.insertNewColorData("EDF6FF","Zumthor","{\"rgb\" : [27,20,4]}");

    }

    //FIRST TIME SHOWCASE VIEW TIPS
    private void presentShowcaseView() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(findViewById(R.id.btn_changeimg), "Tap anywhere on the image to identify the color instantly. Tap on change image to upload your own image.", "OK");

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.linearLayout0))
                        .setTitleText("COLOR SAMPLE")
                        .setContentText("See your sample color of the part of image you tapped on.")
                        .withCircleShape()
                        .setDismissOnTargetTouch(true)
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.textView0))
                        .setTitleText("COLOR CODE")

                        .setContentText("See the color code and click to copy to the clipboard.")
                        .withCircleShape()
                        .setDismissOnTargetTouch(true)
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.btn_allcolors))
                        .setTitleText("EXPLORE 1500 COLORS")
                        .setContentText("Click and explore among more than 1500 different colors with their names and color codes.")
                        .withCircleShape()
                        .setDismissOnTargetTouch(true)
                        .setDismissOnTouch(true)
                        .build()
        );

        sequence.start();
    }

    private String getColorName(String colorCode, int rgb[]){
        colorCode = colorCode.toUpperCase();

        if(colorCode.length()<3 && colorCode.length()>7){
            return "Invalid Colour";
        }
        else if(colorCode.length() % 3 == 0){
            colorCode = "#" +colorCode;
        }

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        int hsl[] = getHsl(rgb);

        int h = hsl[0];
        int s = hsl[1];
        int l = hsl[2];

        int ndf1 = 0, ndf2 = 0, ndf = 0;
        int cl = -1, df = -1;

        ArrayList<String> allColorCodeList = colorNameDB.getAllColorCodes();
        ArrayList<String> allColorNameList = colorNameDB.getAllColorNames();
        ArrayList<String> allColourRgbList = colorNameDB.getAllColorRGB();

        int shortestRgbDistance = 10000;
        String approxColourName = "Unnamed Colour";

        for(int i = 0; i < allColorNameList.size(); i++){
            //If color code exists in my colour list
            if(colorCode.equalsIgnoreCase("#" + allColorCodeList.get(i)))
                return allColorNameList.get(i);

            try {
                final JSONObject obj = new JSONObject(allColourRgbList.get(i));
                final JSONArray rgbArray = obj.getJSONArray("rgb");
                final int rgbDistance = Math.abs(rgb[0] - rgbArray.getInt(0)) +
                        Math.abs(rgb[1] - rgbArray.getInt(1)) +
                        Math.abs(rgb[2] - rgbArray.getInt(2));
                if(rgbDistance<shortestRgbDistance){
                    shortestRgbDistance = rgbDistance;
                    approxColourName = allColorNameList.get(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "Unnamed Colour";
            }
        }

        if(approxColourName.equalsIgnoreCase(""))
        {return "Unnamed Colour";}
        else{
            return approxColourName;
        }

         /*for(int i = 0; i < allColorNameList.size(); i++)
        {

            if(colorCode == "#" + allColorCodeList.get(i))
                return allColorNameList.get(i);

            char charAtTwo = allColorCodeList.get(i).charAt(0);
            char charAtThree = allColorCodeList.get(i).charAt(1);
            char charAtFour = allColorCodeList.get(i).charAt(2);
            char charAtFive = allColorCodeList.get(i).charAt(3);
            char charAtSix = allColorCodeList.get(i).charAt(4);
            char charAtSeven = allColorCodeList.get(i).charAt(5);

            Log.d("asisi", allColorCodeList.get(i)+"\n"+charAtTwo+"-"+charAtThree+"-"+charAtFour+"-"+charAtFive+"-"+charAtSix+"-"+charAtSeven);

            String charAtTwoS = "0", charAtThreeS = "0", charAtFourS = "0", charAtFiveS = "0", charAtSixS = "0", charAtSevenS = "0";

            if(Character.isDigit(charAtTwo))
                charAtTwoS = charAtTwo+"";

            if(Character.isDigit(charAtThree))
                charAtThreeS = charAtThree+"";

            if(Character.isDigit(charAtFour))
                charAtFourS = charAtFour+"";

            if(Character.isDigit(charAtFive))
                charAtFiveS = charAtFive+"";

            if(Character.isDigit(charAtSix))
                charAtSixS = charAtSix+"";

            if(Character.isDigit(charAtSeven))
                charAtSevenS = charAtSeven+"";

            ndf1 = (int) (Math.pow(r - parseInt(charAtTwoS), 2) + Math.pow(g - parseInt(charAtThreeS), 2) + Math.pow(b - parseInt(charAtFourS), 2));
            ndf2 = (int) (Math.pow(h - parseInt(charAtFiveS), 2) + Math.pow(s - parseInt(charAtSixS), 2) + Math.pow(l - parseInt(charAtSevenS), 2));
            ndf = ndf1 + ndf2 * 2;
            if(df < 0 || df > ndf)
            {
                df = ndf;
                cl = i;
            }
        }

        if(cl>0){
            return allColorNameList.get(cl);
        }
        else{
            return "Unnamed Colour";
        }*/
    }

    private int[] getHsl(int rgb[]){
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        int min, max, delta, h, s, l;

        min = Math.min(r, Math.min(g, b));
        max = Math.max(r, Math.max(g, b));
        delta = max - min;
        l = (min + max) / 2;

        s = 0;
        if(l > 0 && l < 1)
            s = delta / (l < 0.5 ? (2 * l) : (2 - 2 * l));

        h = 0;
        if(delta > 0)
        {
            if (max == r && max != g) h += (g - b) / delta;
            if (max == g && max != b) h += (2 + (b - r) / delta);
            if (max == b && max != r) h += (4 + (r - g) / delta);
            h /= 6;
        }

        int hsl[] = new int[3];
        hsl[0] = Integer.valueOf(h*225);
        hsl[1] = Integer.valueOf(s*225);
        hsl[2] = Integer.valueOf(l*225);
        return hsl;
    }
}
