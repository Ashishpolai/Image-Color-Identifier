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
            final int rgb[] = envelope.getRgb();
            textView.setText("#" + envelope.getHtmlCode()+"\nRGB:["+ rgb[0] + ","+rgb[1]+","+rgb[2]+"]\n"+getColorName(envelope.getHtmlCode(), envelope.getRgb()));
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
            Log.d("asisi","finished");
        }
    }


    public void initColorNameHashMap(){
        //Log.d("asisi","insertstart");
        colorNameDB.insertNewColorData("4C4F56","Abbey");
        colorNameDB.insertNewColorData("1B1404","Acadia");
        colorNameDB.insertNewColorData("7CB0A1","Acapulco");
        colorNameDB.insertNewColorData("C9FFE5","Aero Blue");
        colorNameDB.insertNewColorData("714693","Affair");
        colorNameDB.insertNewColorData("D4C4A8","Akaroa");
        //Log.d("asisi","insertend="+colorNameDB.getColorName("4C4F56"));
        colorNameDB.insertNewColorData("FAFAFA","Alabaster");
        colorNameDB.insertNewColorData("F5E9D3","Albescent White");
        colorNameDB.insertNewColorData("93DFB8","Algae Green");
        colorNameDB.insertNewColorData("F0F8FF","Alice Blue");
        colorNameDB.insertNewColorData("E32636","Alizarin Crimson");
        colorNameDB.insertNewColorData("0076A3","Allports");
        colorNameDB.insertNewColorData("EED9C4","Almond");
        colorNameDB.insertNewColorData("907B71","Almond Frost");
        colorNameDB.insertNewColorData("AF8F2C","Alpine");
        colorNameDB.insertNewColorData("DBDBDB","Alto");
        colorNameDB.insertNewColorData("A9ACB6","Aluminium");
        colorNameDB.insertNewColorData("E52B50","Amaranth");
        colorNameDB.insertNewColorData("3B7A57","Amazon");
        colorNameDB.insertNewColorData("FFBF00","Amber");
        colorNameDB.insertNewColorData("87756E","Americano");
        colorNameDB.insertNewColorData("9966CC","Amethyst");
        colorNameDB.insertNewColorData("A397B4","Amethyst Smoke");
        colorNameDB.insertNewColorData("F9EAF3","Amour");
        colorNameDB.insertNewColorData("7B9F80","Amulet");
        colorNameDB.insertNewColorData("9DE5FF","Anakiwa");
        colorNameDB.insertNewColorData("C88A65","Antique Brass");
        colorNameDB.insertNewColorData("704A07","Antique Bronze");
        colorNameDB.insertNewColorData("E0B646","Anzac");
        colorNameDB.insertNewColorData("DFBE6F","Apache");
        colorNameDB.insertNewColorData("4FA83D","Apple");
        colorNameDB.insertNewColorData("AF4D43","Apple Blossom");
        colorNameDB.insertNewColorData("E2F3EC","Apple Green");
        colorNameDB.insertNewColorData("EB9373","Apricot");
        colorNameDB.insertNewColorData("FBCEB1","Apricot Peach");
        colorNameDB.insertNewColorData("FFFEEC","Apricot White");
        colorNameDB.insertNewColorData("014B43","Aqua Deep");
        colorNameDB.insertNewColorData("5FA777","Aqua Forest");
        colorNameDB.insertNewColorData("EDF5F5","Aqua Haze");
        colorNameDB.insertNewColorData("A1DAD7","Aqua Island");
        colorNameDB.insertNewColorData("EAF9F5","Aqua Spring");
        colorNameDB.insertNewColorData("E8F5F2","Aqua Squeeze");
        colorNameDB.insertNewColorData("7FFFD4","Aquamarine");
        colorNameDB.insertNewColorData("71D9E2","Aquamarine Blue");
        colorNameDB.insertNewColorData("110C6C","Arapawa");
        colorNameDB.insertNewColorData("433E37","Armadillo");
        colorNameDB.insertNewColorData("948771","Arrowtown");
        colorNameDB.insertNewColorData("C6C3B5","Ash");
        colorNameDB.insertNewColorData("7BA05B","Asparagus");
        colorNameDB.insertNewColorData("130A06","Asphalt");
        colorNameDB.insertNewColorData("FAEAB9","Astra");
        colorNameDB.insertNewColorData("327DA0","Astral");
        colorNameDB.insertNewColorData("283A77","Astronaut");
        colorNameDB.insertNewColorData("013E62","Astronaut Blue");
        colorNameDB.insertNewColorData("EEF0F3","Athens Gray");
        colorNameDB.insertNewColorData("ECEBCE","Aths Special");
        colorNameDB.insertNewColorData("97CD2D","Atlantis");
        colorNameDB.insertNewColorData("0A6F75","Atoll");
        colorNameDB.insertNewColorData("FF9966","Atomic Tangerine");
        colorNameDB.insertNewColorData("97605D","Au Chico");
        colorNameDB.insertNewColorData("3B0910","Aubergine");
        colorNameDB.insertNewColorData("F5FFBE","Australian Mint");
        colorNameDB.insertNewColorData("888D65","Avocado");
        colorNameDB.insertNewColorData("4E6649","Axolotl");
        colorNameDB.insertNewColorData("F7C8DA","Azalea");
        colorNameDB.insertNewColorData("0D1C19","Aztec");
        colorNameDB.insertNewColorData("315BA1","Azure");
        colorNameDB.insertNewColorData("007FFF","Azure Radiance");
        colorNameDB.insertNewColorData("E0FFFF","Baby Blue");
        colorNameDB.insertNewColorData("026395","Bahama Blue");
        colorNameDB.insertNewColorData("A5CB0C","Bahia");
        colorNameDB.insertNewColorData("FFF8D1","Baja White");
        colorNameDB.insertNewColorData("859FAF","Bali Hai");
        colorNameDB.insertNewColorData("2A2630","Baltic Sea");
        colorNameDB.insertNewColorData("DA6304","Bamboo");
        colorNameDB.insertNewColorData("FBE7B2","Banana Mania");
        colorNameDB.insertNewColorData("858470","Bandicoot");
        colorNameDB.insertNewColorData("DED717","Barberry");
        colorNameDB.insertNewColorData("A68B5B","Barley Corn");
        colorNameDB.insertNewColorData("FFF4CE","Barley White");
        colorNameDB.insertNewColorData("44012D","Barossa");
        colorNameDB.insertNewColorData("292130","Bastille");
        colorNameDB.insertNewColorData("828F72","Battleship Gray");
        colorNameDB.insertNewColorData("7DA98D","Bay Leaf");
        colorNameDB.insertNewColorData("273A81","Bay of Many");
        colorNameDB.insertNewColorData("98777B","Bazaar");
        colorNameDB.insertNewColorData("3D0C02","Bean");
        colorNameDB.insertNewColorData("EEC1BE","Beauty Bush");
        colorNameDB.insertNewColorData("926F5B","Beaver");
        colorNameDB.insertNewColorData("FEF2C7","Beeswax");
        colorNameDB.insertNewColorData("F5F5DC","Beige");
        colorNameDB.insertNewColorData("7DD8C6","Bermuda");
        colorNameDB.insertNewColorData("6B8BA2","Bermuda Gray");
        colorNameDB.insertNewColorData("DEE5C0","Beryl Green");
        colorNameDB.insertNewColorData("FCFBF3","Bianca");
        colorNameDB.insertNewColorData("162A40","Big Stone");
        colorNameDB.insertNewColorData("327C14","Bilbao");
        colorNameDB.insertNewColorData("B2A1EA","Biloba Flower");
        colorNameDB.insertNewColorData("373021","Birch");
        colorNameDB.insertNewColorData("D4CD16","Bird Flower");
        colorNameDB.insertNewColorData("1B3162","Biscay");
        colorNameDB.insertNewColorData("497183","Bismark");
        colorNameDB.insertNewColorData("C1B7A4","Bison Hide");
        colorNameDB.insertNewColorData("3D2B1F","Bistre");
        colorNameDB.insertNewColorData("868974","Bitter");
        colorNameDB.insertNewColorData("CAE00D","Bitter Lemon");
        colorNameDB.insertNewColorData("FE6F5E","Bittersweet");
        colorNameDB.insertNewColorData("EEDEDA","Bizarre");
        colorNameDB.insertNewColorData("000000","Black");
        colorNameDB.insertNewColorData("081910","Black Bean");
        colorNameDB.insertNewColorData("0B1304","Black Forest");
        colorNameDB.insertNewColorData("F6F7F7","Black Haze");
        colorNameDB.insertNewColorData("3E2C1C","Black Marlin");
        colorNameDB.insertNewColorData("242E16","Black Olive");
        colorNameDB.insertNewColorData("041322","Black Pearl");
        colorNameDB.insertNewColorData("0D0332","Black Rock");
        colorNameDB.insertNewColorData("67032D","Black Rose");
        colorNameDB.insertNewColorData("0A001C","Black Russian");
        colorNameDB.insertNewColorData("F2FAFA","Black Squeeze");
        colorNameDB.insertNewColorData("FFFEF6","Black White");
        colorNameDB.insertNewColorData("4D0135","Blackberry");
        colorNameDB.insertNewColorData("32293A","Blackcurrant");
        colorNameDB.insertNewColorData("FF6600","Blaze Orange");
        colorNameDB.insertNewColorData("FEF3D8","Bleach White");
        colorNameDB.insertNewColorData("2C2133","Bleached Cedar");
        colorNameDB.insertNewColorData("A3E3ED","Blizzard Blue");
        colorNameDB.insertNewColorData("DCB4BC","Blossom");
        colorNameDB.insertNewColorData("0000FF","Blue");
        colorNameDB.insertNewColorData("496679","Blue Bayoux");
        colorNameDB.insertNewColorData("9999CC","Blue Bell");
        colorNameDB.insertNewColorData("F1E9FF","Blue Chalk");
        colorNameDB.insertNewColorData("010D1A","Blue Charcoal");
        colorNameDB.insertNewColorData("0C8990","Blue Chill");
        colorNameDB.insertNewColorData("380474","Blue Diamond");
        colorNameDB.insertNewColorData("204852","Blue Dianne");
        colorNameDB.insertNewColorData("2C0E8C","Blue Gem");
        colorNameDB.insertNewColorData("BFBED8","Blue Haze");
        colorNameDB.insertNewColorData("017987","Blue Lagoon");
        colorNameDB.insertNewColorData("7666C6","Blue Marguerite");
        colorNameDB.insertNewColorData("0066FF","Blue Ribbon");
        colorNameDB.insertNewColorData("D2F6DE","Blue Romance");
        colorNameDB.insertNewColorData("748881","Blue Smoke");
        colorNameDB.insertNewColorData("016162","Blue Stone");
        colorNameDB.insertNewColorData("6456B7","Blue Violet");
        colorNameDB.insertNewColorData("042E4C","Blue Whale");
        colorNameDB.insertNewColorData("13264D","Blue Zodiac");
        colorNameDB.insertNewColorData("18587A","Blumine");
        colorNameDB.insertNewColorData("B44668","Blush");
        colorNameDB.insertNewColorData("FF6FFF","Blush Pink");
        colorNameDB.insertNewColorData("AFB1B8","Bombay");
        colorNameDB.insertNewColorData("E5E0E1","Bon Jour");
        colorNameDB.insertNewColorData("0095B6","Bondi Blue");
        colorNameDB.insertNewColorData("E4D1C0","Bone");
        colorNameDB.insertNewColorData("5C0120","Bordeaux");
        colorNameDB.insertNewColorData("4E2A5A","Bossanova");
        colorNameDB.insertNewColorData("3B91B4","Boston Blue");
        colorNameDB.insertNewColorData("C7DDE5","Botticelli");
        colorNameDB.insertNewColorData("093624","Bottle Green");
        colorNameDB.insertNewColorData("7A7A7A","Boulder");
        colorNameDB.insertNewColorData("AE809E","Bouquet");
        colorNameDB.insertNewColorData("BA6F1E","Bourbon");
        colorNameDB.insertNewColorData("4A2A04","Bracken");
        colorNameDB.insertNewColorData("DEC196","Brandy");
        colorNameDB.insertNewColorData("CD8429","Brandy Punch");
        colorNameDB.insertNewColorData("BB8983","Brandy Rose");
        colorNameDB.insertNewColorData("5DA19F","Breaker Bay");
        colorNameDB.insertNewColorData("C62D42","Brick Red");
        colorNameDB.insertNewColorData("FFFAF4","Bridal Heath");
        colorNameDB.insertNewColorData("FEF0EC","Bridesmaid");
        colorNameDB.insertNewColorData("3C4151","Bright Gray");
        colorNameDB.insertNewColorData("66FF00","Bright Green");
        colorNameDB.insertNewColorData("B10000","Bright Red");
        colorNameDB.insertNewColorData("FED33C","Bright Sun");
        colorNameDB.insertNewColorData("08E8DE","Bright Turquoise");
        colorNameDB.insertNewColorData("F653A6","Brilliant Rose");
        colorNameDB.insertNewColorData("FB607F","Brink Pink");
        colorNameDB.insertNewColorData("ABA196","Bronco");
        colorNameDB.insertNewColorData("3F2109","Bronze");
        colorNameDB.insertNewColorData("4E420C","Bronze Olive");
        colorNameDB.insertNewColorData("4D400F","Bronzetone");
        colorNameDB.insertNewColorData("FFEC13","Broom");
        colorNameDB.insertNewColorData("964B00","Brown");
        colorNameDB.insertNewColorData("592804","Brown Bramble");
        colorNameDB.insertNewColorData("492615","Brown Derby");
        colorNameDB.insertNewColorData("401801","Brown Pod");
        colorNameDB.insertNewColorData("AF593E","Brown Rust");
        colorNameDB.insertNewColorData("37290E","Brown Tumbleweed");
        colorNameDB.insertNewColorData("E7FEFF","Bubbles");
        colorNameDB.insertNewColorData("622F30","Buccaneer");
        colorNameDB.insertNewColorData("A8AE9C","Bud");
        colorNameDB.insertNewColorData("C1A004","Buddha Gold");
        colorNameDB.insertNewColorData("F0DC82","Buff");
        colorNameDB.insertNewColorData("480607","Bulgarian Rose");
        colorNameDB.insertNewColorData("864D1E","Bull Shot");
        colorNameDB.insertNewColorData("0D1117","Bunker");
        colorNameDB.insertNewColorData("151F4C","Bunting");
        colorNameDB.insertNewColorData("900020","Burgundy");
        colorNameDB.insertNewColorData("002E20","Burnham");
        colorNameDB.insertNewColorData("FF7034","Burning Orange");
        colorNameDB.insertNewColorData("D99376","Burning Sand");
        colorNameDB.insertNewColorData("420303","Burnt Maroon");
        colorNameDB.insertNewColorData("CC5500","Burnt Orange");
        colorNameDB.insertNewColorData("E97451","Burnt Sienna");
        colorNameDB.insertNewColorData("8A3324","Burnt Umber");
        colorNameDB.insertNewColorData("0D2E1C","Bush");
        colorNameDB.insertNewColorData("F3AD16","Buttercup");
        colorNameDB.insertNewColorData("A1750D","Buttered Rum");
        colorNameDB.insertNewColorData("624E9A","Butterfly Bush");
        colorNameDB.insertNewColorData("FFF1B5","Buttermilk");
        colorNameDB.insertNewColorData("FFFCEA","Buttery White");
        colorNameDB.insertNewColorData("4D0A18","Cab Sav");
        colorNameDB.insertNewColorData("D94972","Cabaret");
        colorNameDB.insertNewColorData("3F4C3A","Cabbage Pont");
        colorNameDB.insertNewColorData("587156","Cactus");
        colorNameDB.insertNewColorData("A9B2C3","Cadet Blue");
        colorNameDB.insertNewColorData("B04C6A","Cadillac");
        colorNameDB.insertNewColorData("6F440C","Cafe Royale");
        colorNameDB.insertNewColorData("E0C095","Calico");
        colorNameDB.insertNewColorData("FE9D04","California");
        colorNameDB.insertNewColorData("31728D","Calypso");
        colorNameDB.insertNewColorData("00581A","Camarone");
        colorNameDB.insertNewColorData("893456","Camelot");
        colorNameDB.insertNewColorData("D9B99B","Cameo");
        colorNameDB.insertNewColorData("3C3910","Camouflage");
        colorNameDB.insertNewColorData("78866B","Camouflage Green");
        colorNameDB.insertNewColorData("D591A4","Can Can");
        colorNameDB.insertNewColorData("F3FB62","Canary");
        colorNameDB.insertNewColorData("FCD917","Candlelight");
        colorNameDB.insertNewColorData("FBEC5D","Candy Corn");
        colorNameDB.insertNewColorData("251706","Cannon Black");
        colorNameDB.insertNewColorData("894367","Cannon Pink");
        colorNameDB.insertNewColorData("3C4443","Cape Cod");
        colorNameDB.insertNewColorData("FEE5AC","Cape Honey");
        colorNameDB.insertNewColorData("A26645","Cape Palliser");
        colorNameDB.insertNewColorData("DCEDB4","Caper");
        colorNameDB.insertNewColorData("FFDDAF","Caramel");
        colorNameDB.insertNewColorData("EEEEE8","Cararra");
        colorNameDB.insertNewColorData("01361C","Cardin Green");
        colorNameDB.insertNewColorData("C41E3A","Cardinal");
        colorNameDB.insertNewColorData("8C055E","Cardinal Pink");
        colorNameDB.insertNewColorData("D29EAA","Careys Pink");
        colorNameDB.insertNewColorData("00CC99","Caribbean Green");
        colorNameDB.insertNewColorData("EA88A8","Carissma");
        colorNameDB.insertNewColorData("F3FFD8","Carla");
        colorNameDB.insertNewColorData("960018","Carmine");
        colorNameDB.insertNewColorData("5C2E01","Carnaby Tan");
        colorNameDB.insertNewColorData("F95A61","Carnation");
        colorNameDB.insertNewColorData("F9E0ED","Carousel Pink");
        colorNameDB.insertNewColorData("FFA6C9","Carnation Pink");
        colorNameDB.insertNewColorData("ED9121","Carrot Orange");
        colorNameDB.insertNewColorData("F8B853","Casablanca");
        colorNameDB.insertNewColorData("2F6168","Casal");
        colorNameDB.insertNewColorData("8BA9A5","Cascade");
        colorNameDB.insertNewColorData("E6BEA5","Cashmere");
        colorNameDB.insertNewColorData("ADBED1","Casper");
        colorNameDB.insertNewColorData("52001F","Castro");
        colorNameDB.insertNewColorData("062A78","Catalina Blue");
        colorNameDB.insertNewColorData("EEF6F7","Catskill White");
        colorNameDB.insertNewColorData("E3BEBE","Cavern Pink");
        colorNameDB.insertNewColorData("3E1C14","Cedar");
        colorNameDB.insertNewColorData("711A00","Cedar Wood Finish");
        colorNameDB.insertNewColorData("ACE1AF","Celadon");
        colorNameDB.insertNewColorData("B8C25D","Celery");
        colorNameDB.insertNewColorData("D1D2CA","Celeste");
        colorNameDB.insertNewColorData("1E385B","Cello");
        colorNameDB.insertNewColorData("163222","Celtic");
        colorNameDB.insertNewColorData("8D7662","Cement");
        colorNameDB.insertNewColorData("FCFFF9","Ceramic");
        colorNameDB.insertNewColorData("DA3287","Cerise");
        colorNameDB.insertNewColorData("DE3163","Cerise Red");
        colorNameDB.insertNewColorData("02A4D3","Cerulean");
        colorNameDB.insertNewColorData("2A52BE","Cerulean Blue");
        colorNameDB.insertNewColorData("FFF4F3","Chablis");
        colorNameDB.insertNewColorData("516E3D","Chalet Green");
        colorNameDB.insertNewColorData("EED794","Chalky");
        colorNameDB.insertNewColorData("354E8C","Chambray");
        colorNameDB.insertNewColorData("EDDCB1","Chamois");
        colorNameDB.insertNewColorData("FAECCC","Champagne");
        colorNameDB.insertNewColorData("F8C3DF","Chantilly");
        colorNameDB.insertNewColorData("292937","Charade");
        colorNameDB.insertNewColorData("FFF3F1","Chardon");
        colorNameDB.insertNewColorData("FFCD8C","Chardonnay");
        colorNameDB.insertNewColorData("BAEEF9","Charlotte");
        colorNameDB.insertNewColorData("D47494","Charm");
        colorNameDB.insertNewColorData("7FFF00","Chartreuse");
        colorNameDB.insertNewColorData("DFFF00","Chartreuse Yellow");
        colorNameDB.insertNewColorData("40A860","Chateau Green");
        colorNameDB.insertNewColorData("BDB3C7","Chatelle");
        colorNameDB.insertNewColorData("175579","Chathams Blue");
        colorNameDB.insertNewColorData("83AA5D","Chelsea Cucumber");
        colorNameDB.insertNewColorData("9E5302","Chelsea Gem");
        colorNameDB.insertNewColorData("DFCD6F","Chenin");
        colorNameDB.insertNewColorData("FCDA98","Cherokee");
        colorNameDB.insertNewColorData("2A0359","Cherry Pie");
        colorNameDB.insertNewColorData("651A14","Cherrywood");
        colorNameDB.insertNewColorData("F8D9E9","Cherub");
        colorNameDB.insertNewColorData("B94E48","Chestnut");
        colorNameDB.insertNewColorData("CD5C5C","Chestnut Rose");
        colorNameDB.insertNewColorData("8581D9","Chetwode Blue");
        colorNameDB.insertNewColorData("5D5C58","Chicago");
        colorNameDB.insertNewColorData("F1FFC8","Chiffon");
        colorNameDB.insertNewColorData("F77703","Chilean Fire");
        colorNameDB.insertNewColorData("FFFDE6","Chilean Heath");
        colorNameDB.insertNewColorData("FCFFE7","China Ivory");
        colorNameDB.insertNewColorData("CEC7A7","Chino");
        colorNameDB.insertNewColorData("A8E3BD","Chinook");
        colorNameDB.insertNewColorData("370202","Chocolate");
        colorNameDB.insertNewColorData("33036B","Christalle");
        colorNameDB.insertNewColorData("67A712","Christi");
        colorNameDB.insertNewColorData("E7730A","Christine");
        colorNameDB.insertNewColorData("E8F1D4","Chrome White");
        colorNameDB.insertNewColorData("0E0E18","Cinder");
        colorNameDB.insertNewColorData("FDE1DC","Cinderella");
        colorNameDB.insertNewColorData("E34234","Cinnabar");
        colorNameDB.insertNewColorData("7B3F00","Cinnamon");
        colorNameDB.insertNewColorData("55280C","Cioccolato");
        colorNameDB.insertNewColorData("FAF7D6","Citrine White");
        colorNameDB.insertNewColorData("9EA91F","Citron");
        colorNameDB.insertNewColorData("A1C50A","Citrus");
        colorNameDB.insertNewColorData("480656","Clairvoyant");
        colorNameDB.insertNewColorData("D4B6AF","Clam Shell");
        colorNameDB.insertNewColorData("7F1734","Claret");
        colorNameDB.insertNewColorData("FBCCE7","Classic Rose");
        colorNameDB.insertNewColorData("BDC8B3","Clay Ash");
        colorNameDB.insertNewColorData("8A8360","Clay Creek");
        colorNameDB.insertNewColorData("E9FFFD","Clear Day");
        colorNameDB.insertNewColorData("E96E00","Clementine");
        colorNameDB.insertNewColorData("371D09","Clinker");
        colorNameDB.insertNewColorData("C7C4BF","Cloud");
        colorNameDB.insertNewColorData("202E54","Cloud Burst");
        colorNameDB.insertNewColorData("ACA59F","Cloudy");
        colorNameDB.insertNewColorData("384910","Clover");
        colorNameDB.insertNewColorData("0047AB","Cobalt");
        colorNameDB.insertNewColorData("481C1C","Cocoa Bean");
        colorNameDB.insertNewColorData("301F1E","Cocoa Brown");
        colorNameDB.insertNewColorData("F8F7DC","Coconut Cream");
        colorNameDB.insertNewColorData("0B0B0B","Cod Gray");
        colorNameDB.insertNewColorData("706555","Coffee");
        colorNameDB.insertNewColorData("2A140E","Coffee Bean");
        colorNameDB.insertNewColorData("9F381D","Cognac");
        colorNameDB.insertNewColorData("3F2500","Cola");
        colorNameDB.insertNewColorData("ABA0D9","Cold Purple");
        colorNameDB.insertNewColorData("CEBABA","Cold Turkey");
        colorNameDB.insertNewColorData("FFEDBC","Colonial White");
        colorNameDB.insertNewColorData("5C5D75","Comet");
        colorNameDB.insertNewColorData("517C66","Como");
        colorNameDB.insertNewColorData("C9D9D2","Conch");
        colorNameDB.insertNewColorData("7C7B7A","Concord");
        colorNameDB.insertNewColorData("F2F2F2","Concrete");
        colorNameDB.insertNewColorData("E9D75A","Confetti");
        colorNameDB.insertNewColorData("593737","Congo Brown");
        colorNameDB.insertNewColorData("02478E","Congress Blue");
        colorNameDB.insertNewColorData("ACDD4D","Conifer");
        colorNameDB.insertNewColorData("C6726B","Contessa");
        colorNameDB.insertNewColorData("B87333","Copper");
        colorNameDB.insertNewColorData("7E3A15","Copper Canyon");
        colorNameDB.insertNewColorData("996666","Copper Rose");
        colorNameDB.insertNewColorData("944747","Copper Rust");
        colorNameDB.insertNewColorData("DA8A67","Copperfield");
        colorNameDB.insertNewColorData("FF7F50","Coral");
        colorNameDB.insertNewColorData("FF4040","Coral Red");
        colorNameDB.insertNewColorData("C7BCA2","Coral Reef");
        colorNameDB.insertNewColorData("A86B6B","Coral Tree");
        colorNameDB.insertNewColorData("606E68","Corduroy");
        colorNameDB.insertNewColorData("C4D0B0","Coriander");
        colorNameDB.insertNewColorData("40291D","Cork");
        colorNameDB.insertNewColorData("E7BF05","Corn");
        colorNameDB.insertNewColorData("F8FACD","Corn Field");
        colorNameDB.insertNewColorData("8B6B0B","Corn Harvest");
        colorNameDB.insertNewColorData("93CCEA","Cornflower");
        colorNameDB.insertNewColorData("6495ED","Cornflower Blue");
        colorNameDB.insertNewColorData("FFB0AC","Cornflower Lilac");
        colorNameDB.insertNewColorData("FAD3A2","Corvette");
        colorNameDB.insertNewColorData("76395D","Cosmic");
        colorNameDB.insertNewColorData("FFD8D9","Cosmos");
        colorNameDB.insertNewColorData("615D30","Costa Del Sol");
        colorNameDB.insertNewColorData("FFB7D5","Cotton Candy");
        colorNameDB.insertNewColorData("C2BDB6","Cotton Seed");
        colorNameDB.insertNewColorData("01371A","County Green");
        colorNameDB.insertNewColorData("4D282D","Cowboy");
        colorNameDB.insertNewColorData("B95140","Crail");
        colorNameDB.insertNewColorData("DB5079","Cranberry");
        colorNameDB.insertNewColorData("462425","Crater Brown");
        colorNameDB.insertNewColorData("FFFDD0","Cream");
        colorNameDB.insertNewColorData("FFE5A0","Cream Brulee");
        colorNameDB.insertNewColorData("F5C85C","Cream Can");
        colorNameDB.insertNewColorData("1E0F04","Creole");
        colorNameDB.insertNewColorData("737829","Crete");
        colorNameDB.insertNewColorData("DC143C","Crimson");
        colorNameDB.insertNewColorData("736D58","Crocodile");
        colorNameDB.insertNewColorData("771F1F","Crown of Thorns");
        colorNameDB.insertNewColorData("1C1208","Crowshead");
        colorNameDB.insertNewColorData("B5ECDF","Cruise");
        colorNameDB.insertNewColorData("004816","Crusoe");
        colorNameDB.insertNewColorData("FD7B33","Crusta");
        colorNameDB.insertNewColorData("924321","Cumin");
        colorNameDB.insertNewColorData("FDFFD5","Cumulus");
        colorNameDB.insertNewColorData("FBBEDA","Cupid");
        colorNameDB.insertNewColorData("2596D1","Curious Blue");
        colorNameDB.insertNewColorData("507672","Cutty Sark");
        colorNameDB.insertNewColorData("00FFFF","Cyan / Aqua");
        colorNameDB.insertNewColorData("003E40","Cyprus");
        colorNameDB.insertNewColorData("012731","Daintree");
        colorNameDB.insertNewColorData("F9E4BC","Dairy Cream");
        colorNameDB.insertNewColorData("4F2398","Daisy Bush");
        colorNameDB.insertNewColorData("6E4B26","Dallas");
        colorNameDB.insertNewColorData("FED85D","Dandelion");
        colorNameDB.insertNewColorData("6093D1","Danube");
        colorNameDB.insertNewColorData("0000C8","Dark Blue");
        colorNameDB.insertNewColorData("770F05","Dark Burgundy");
        colorNameDB.insertNewColorData("3C2005","Dark Ebony");
        colorNameDB.insertNewColorData("0A480D","Dark Fern");
        colorNameDB.insertNewColorData("661010","Dark Tan");
        colorNameDB.insertNewColorData("A6A29A","Dawn");
        colorNameDB.insertNewColorData("F3E9E5","Dawn Pink");
        colorNameDB.insertNewColorData("7AC488","De York");
        colorNameDB.insertNewColorData("D2DA97","Deco");
        colorNameDB.insertNewColorData("220878","Deep Blue");
        colorNameDB.insertNewColorData("E47698","Deep Blush");
        colorNameDB.insertNewColorData("4A3004","Deep Bronze");
        colorNameDB.insertNewColorData("007BA7","Deep Cerulean");
        colorNameDB.insertNewColorData("051040","Deep Cove");
        colorNameDB.insertNewColorData("002900","Deep Fir");
        colorNameDB.insertNewColorData("182D09","Deep Forest Green");
        colorNameDB.insertNewColorData("1B127B","Deep Koamaru");
        colorNameDB.insertNewColorData("412010","Deep Oak");
        colorNameDB.insertNewColorData("082567","Deep Sapphire");
        colorNameDB.insertNewColorData("01826B","Deep Sea");
        colorNameDB.insertNewColorData("095859","Deep Sea Green");
        colorNameDB.insertNewColorData("003532","Deep Teal");
        colorNameDB.insertNewColorData("B09A95","Del Rio");
        colorNameDB.insertNewColorData("396413","Dell");
        colorNameDB.insertNewColorData("A4A49D","Delta");
        colorNameDB.insertNewColorData("7563A8","Deluge");
        colorNameDB.insertNewColorData("1560BD","Denim");
        colorNameDB.insertNewColorData("FFEED8","Derby");
        colorNameDB.insertNewColorData("AE6020","Desert");
        colorNameDB.insertNewColorData("EDC9AF","Desert Sand");
        colorNameDB.insertNewColorData("F8F8F7","Desert Storm");
        colorNameDB.insertNewColorData("EAFFFE","Dew");
        colorNameDB.insertNewColorData("DB995E","Di Serria");
        colorNameDB.insertNewColorData("130000","Diesel");
        colorNameDB.insertNewColorData("5D7747","Dingley");
        colorNameDB.insertNewColorData("871550","Disco");
        colorNameDB.insertNewColorData("E29418","Dixie");
        colorNameDB.insertNewColorData("1E90FF","Dodger Blue");
        colorNameDB.insertNewColorData("F9FF8B","Dolly");
        colorNameDB.insertNewColorData("646077","Dolphin");
        colorNameDB.insertNewColorData("8E775E","Domino");
        colorNameDB.insertNewColorData("5D4C51","Don Juan");
        colorNameDB.insertNewColorData("A69279","Donkey Brown");
        colorNameDB.insertNewColorData("6B5755","Dorado");
        colorNameDB.insertNewColorData("EEE3AD","Double Colonial White");
        colorNameDB.insertNewColorData("FCF4D0","Double Pearl Lusta");
        colorNameDB.insertNewColorData("E6D7B9","Double Spanish White");
        colorNameDB.insertNewColorData("6D6C6C","Dove Gray");
        colorNameDB.insertNewColorData("092256","Downriver");
        colorNameDB.insertNewColorData("6FD0C5","Downy");
        colorNameDB.insertNewColorData("AF8751","Driftwood");
        colorNameDB.insertNewColorData("FDF7AD","Drover");
        colorNameDB.insertNewColorData("A899E6","Dull Lavender");
        colorNameDB.insertNewColorData("383533","Dune");
        colorNameDB.insertNewColorData("E5CCC9","Dust Storm");
        colorNameDB.insertNewColorData("A8989B","Dusty Gray");
        colorNameDB.insertNewColorData("B6BAA4","Eagle");
        colorNameDB.insertNewColorData("C9B93B","Earls Green");
        colorNameDB.insertNewColorData("FFF9E6","Early Dawn");
        colorNameDB.insertNewColorData("414C7D","East Bay");
        colorNameDB.insertNewColorData("AC91CE","East Side");
        colorNameDB.insertNewColorData("1E9AB0","Eastern Blue");
        colorNameDB.insertNewColorData("E9E3E3","Ebb");
        colorNameDB.insertNewColorData("0C0B1D","Ebony");
        colorNameDB.insertNewColorData("26283B","Ebony Clay");
        colorNameDB.insertNewColorData("311C17","Eclipse");
        colorNameDB.insertNewColorData("F5F3E5","Ecru White");
        colorNameDB.insertNewColorData("FA7814","Ecstasy");
        colorNameDB.insertNewColorData("105852","Eden");
        colorNameDB.insertNewColorData("C8E3D7","Edgewater");
        colorNameDB.insertNewColorData("A2AEAB","Edward");
        colorNameDB.insertNewColorData("FFF4DD","Egg Sour");
        colorNameDB.insertNewColorData("FFEFC1","Egg White");
        colorNameDB.insertNewColorData("614051","Eggplant");
        colorNameDB.insertNewColorData("1E1708","El Paso");
        colorNameDB.insertNewColorData("8F3E33","El Salva");
        colorNameDB.insertNewColorData("CCFF00","Electric Lime");
        colorNameDB.insertNewColorData("8B00FF","Electric Violet");
        colorNameDB.insertNewColorData("123447","Elephant");
        colorNameDB.insertNewColorData("088370","Elf Green");
        colorNameDB.insertNewColorData("1C7C7D","Elm");
        colorNameDB.insertNewColorData("50C878","Emerald");
        colorNameDB.insertNewColorData("6C3082","Eminence");
        colorNameDB.insertNewColorData("514649","Emperor");
        colorNameDB.insertNewColorData("817377","Empress");
        colorNameDB.insertNewColorData("0056A7","Endeavour");
        colorNameDB.insertNewColorData("F8DD5C","Energy Yellow");
        colorNameDB.insertNewColorData("022D15","English Holly");
        colorNameDB.insertNewColorData("3E2B23","English Walnut");
        colorNameDB.insertNewColorData("8BA690","Envy");
        colorNameDB.insertNewColorData("E1BC64","Equator");
        colorNameDB.insertNewColorData("612718","Espresso");
        colorNameDB.insertNewColorData("211A0E","Eternity");
        colorNameDB.insertNewColorData("278A5B","Eucalyptus");
        colorNameDB.insertNewColorData("CFA39D","Eunry");
        colorNameDB.insertNewColorData("024E46","Evening Sea");
        colorNameDB.insertNewColorData("1C402E","Everglade");
        colorNameDB.insertNewColorData("427977","Faded Jade");
        colorNameDB.insertNewColorData("FFEFEC","Fair Pink");
        colorNameDB.insertNewColorData("7F626D","Falcon");
        colorNameDB.insertNewColorData("ECEBBD","Fall Green");
        colorNameDB.insertNewColorData("801818","Falu Red");
        colorNameDB.insertNewColorData("FAF3F0","Fantasy");
        colorNameDB.insertNewColorData("796A78","Fedora");
        colorNameDB.insertNewColorData("9FDD8C","Feijoa");
        colorNameDB.insertNewColorData("63B76C","Fern");
        colorNameDB.insertNewColorData("657220","Fern Frond");
        colorNameDB.insertNewColorData("4F7942","Fern Green");
        colorNameDB.insertNewColorData("704F50","Ferra");
        colorNameDB.insertNewColorData("FBE96C","Festival");
        colorNameDB.insertNewColorData("F0FCEA","Feta");
        colorNameDB.insertNewColorData("B35213","Fiery Orange");
        colorNameDB.insertNewColorData("626649","Finch");
        colorNameDB.insertNewColorData("556D56","Finlandia");
        colorNameDB.insertNewColorData("692D54","Finn");
        colorNameDB.insertNewColorData("405169","Fiord");
        colorNameDB.insertNewColorData("AA4203","Fire");
        colorNameDB.insertNewColorData("E89928","Fire Bush");
        colorNameDB.insertNewColorData("0E2A30","Firefly");
        colorNameDB.insertNewColorData("DA5B38","Flame Pea");
        colorNameDB.insertNewColorData("FF7D07","Flamenco");
        colorNameDB.insertNewColorData("F2552A","Flamingo");
        colorNameDB.insertNewColorData("EEDC82","Flax");
        colorNameDB.insertNewColorData("7B8265","Flax Smoke");
        colorNameDB.insertNewColorData("FFCBA4","Flesh");
        colorNameDB.insertNewColorData("6F6A61","Flint");
        colorNameDB.insertNewColorData("A2006D","Flirt");
        colorNameDB.insertNewColorData("CA3435","Flush Mahogany");
        colorNameDB.insertNewColorData("FF7F00","Flush Orange");
        colorNameDB.insertNewColorData("D8FCFA","Foam");
        colorNameDB.insertNewColorData("D7D0FF","Fog");
        colorNameDB.insertNewColorData("CBCAB6","Foggy Gray");
        colorNameDB.insertNewColorData("228B22","Forest Green");
        colorNameDB.insertNewColorData("FFF1EE","Forget Me Not");
        colorNameDB.insertNewColorData("56B4BE","Fountain Blue");
        colorNameDB.insertNewColorData("FFDEB3","Frangipani");
        colorNameDB.insertNewColorData("BDBDC6","French Gray");
        colorNameDB.insertNewColorData("ECC7EE","French Lilac");
        colorNameDB.insertNewColorData("BDEDFD","French Pass");
        colorNameDB.insertNewColorData("F64A8A","French Rose");
        colorNameDB.insertNewColorData("990066","Fresh Eggplant");
        colorNameDB.insertNewColorData("807E79","Friar Gray");
        colorNameDB.insertNewColorData("B1E2C1","Fringy Flower");
        colorNameDB.insertNewColorData("F57584","Froly");
        colorNameDB.insertNewColorData("EDF5DD","Frost");
        colorNameDB.insertNewColorData("DBFFF8","Frosted Mint");
        colorNameDB.insertNewColorData("E4F6E7","Frostee");
        colorNameDB.insertNewColorData("4F9D5D","Fruit Salad");
        colorNameDB.insertNewColorData("7A58C1","Fuchsia Blue");
        colorNameDB.insertNewColorData("C154C1","Fuchsia Pink");
        colorNameDB.insertNewColorData("BEDE0D","Fuego");
        colorNameDB.insertNewColorData("ECA927","Fuel Yellow");
        colorNameDB.insertNewColorData("1959A8","Fun Blue");
        colorNameDB.insertNewColorData("016D39","Fun Green");
        colorNameDB.insertNewColorData("54534D","Fuscous Gray");
        colorNameDB.insertNewColorData("C45655","Fuzzy Wuzzy Brown");
        colorNameDB.insertNewColorData("163531","Gable Green");
        colorNameDB.insertNewColorData("EFEFEF","Gallery");
        colorNameDB.insertNewColorData("DCB20C","Galliano");
        colorNameDB.insertNewColorData("E49B0F","Gamboge");
        colorNameDB.insertNewColorData("D18F1B","Geebung");
        colorNameDB.insertNewColorData("15736B","Genoa");
        colorNameDB.insertNewColorData("FB8989","Geraldine");
        colorNameDB.insertNewColorData("D4DFE2","Geyser");
        colorNameDB.insertNewColorData("C7C9D5","Ghost");
        colorNameDB.insertNewColorData("523C94","Gigas");
        colorNameDB.insertNewColorData("B8B56A","Gimblet");
        colorNameDB.insertNewColorData("E8F2EB","Gin");
        colorNameDB.insertNewColorData("FFF9E2","Gin Fizz");
        colorNameDB.insertNewColorData("F8E4BF","Givry");
        colorNameDB.insertNewColorData("80B3C4","Glacier");
        colorNameDB.insertNewColorData("61845F","Glade Green");
        colorNameDB.insertNewColorData("726D4E","Go Ben");
        colorNameDB.insertNewColorData("3D7D52","Goblin");
        colorNameDB.insertNewColorData("FFD700","Gold");
        colorNameDB.insertNewColorData("F18200","Gold Drop");
        colorNameDB.insertNewColorData("E6BE8A","Gold Sand");
        colorNameDB.insertNewColorData("DEBA13","Gold Tips");
        colorNameDB.insertNewColorData("E28913","Golden Bell");
        colorNameDB.insertNewColorData("F0D52D","Golden Dream");
        colorNameDB.insertNewColorData("F5FB3D","Golden Fizz");
        colorNameDB.insertNewColorData("FDE295","Golden Glow");
        colorNameDB.insertNewColorData("DAA520","Golden Grass");
        colorNameDB.insertNewColorData("F0DB7D","Golden Sand");
        colorNameDB.insertNewColorData("FFCC5C","Golden Tainoi");
        colorNameDB.insertNewColorData("FCD667","Goldenrod");
        colorNameDB.insertNewColorData("261414","Gondola");
        colorNameDB.insertNewColorData("0B1107","Gordons Green");
        colorNameDB.insertNewColorData("FFF14F","Gorse");
        colorNameDB.insertNewColorData("069B81","Gossamer");
        colorNameDB.insertNewColorData("D2F8B0","Gossip");
        colorNameDB.insertNewColorData("6D92A1","Gothic");
        colorNameDB.insertNewColorData("2F3CB3","Governor Bay");
        colorNameDB.insertNewColorData("E4D5B7","Grain Brown");
        colorNameDB.insertNewColorData("FFD38C","Grandis");
        colorNameDB.insertNewColorData("8D8974","Granite Green");
        colorNameDB.insertNewColorData("D5F6E3","Granny Apple");
        colorNameDB.insertNewColorData("84A0A0","Granny Smith");
        colorNameDB.insertNewColorData("9DE093","Granny Smith Apple");
        colorNameDB.insertNewColorData("381A51","Grape");
        colorNameDB.insertNewColorData("251607","Graphite");
        colorNameDB.insertNewColorData("4A444B","Gravel");
        colorNameDB.insertNewColorData("808080","Gray");
        colorNameDB.insertNewColorData("465945","Gray Asparagus");
        colorNameDB.insertNewColorData("A2AAB3","Gray Chateau");
        colorNameDB.insertNewColorData("C3C3BD","Gray Nickel");
        colorNameDB.insertNewColorData("E7ECE6","Gray Nurse");
        colorNameDB.insertNewColorData("A9A491","Gray Olive");
        colorNameDB.insertNewColorData("C1BECD","Gray Suit");
        colorNameDB.insertNewColorData("00FF00","Green");
        colorNameDB.insertNewColorData("01A368","Green Haze");
        colorNameDB.insertNewColorData("24500F","Green House");
        colorNameDB.insertNewColorData("25311C","Green Kelp");
        colorNameDB.insertNewColorData("436A0D","Green Leaf");
        colorNameDB.insertNewColorData("CBD3B0","Green Mist");
        colorNameDB.insertNewColorData("1D6142","Green Pea");
        colorNameDB.insertNewColorData("A4AF6E","Green Smoke");
        colorNameDB.insertNewColorData("B8C1B1","Green Spring");
        colorNameDB.insertNewColorData("032B52","Green Vogue");
        colorNameDB.insertNewColorData("101405","Green Waterloo");
        colorNameDB.insertNewColorData("E8EBE0","Green White");
        colorNameDB.insertNewColorData("ADFF2F","Green Yellow");
        colorNameDB.insertNewColorData("D54600","Grenadier");
        colorNameDB.insertNewColorData("BA0101","Guardsman Red");
        colorNameDB.insertNewColorData("051657","Gulf Blue");
        colorNameDB.insertNewColorData("80B3AE","Gulf Stream");
        colorNameDB.insertNewColorData("9DACB7","Gull Gray");
        colorNameDB.insertNewColorData("B6D3BF","Gum Leaf");
        colorNameDB.insertNewColorData("7CA1A6","Gumbo");
        colorNameDB.insertNewColorData("414257","Gun Powder");
        colorNameDB.insertNewColorData("828685","Gunsmoke");
        colorNameDB.insertNewColorData("9A9577","Gurkha");
        colorNameDB.insertNewColorData("98811B","Hacienda");
        colorNameDB.insertNewColorData("6B2A14","Hairy Heath");
        colorNameDB.insertNewColorData("1B1035","Haiti");
        colorNameDB.insertNewColorData("85C4CC","Half Baked");
        colorNameDB.insertNewColorData("FDF6D3","Half Colonial White");
        colorNameDB.insertNewColorData("FEF7DE","Half Dutch White");
        colorNameDB.insertNewColorData("FEF4DB","Half Spanish White");
        colorNameDB.insertNewColorData("FFFEE1","Half and Half");
        colorNameDB.insertNewColorData("E5D8AF","Hampton");
        colorNameDB.insertNewColorData("3FFF00","Harlequin");
        colorNameDB.insertNewColorData("E6F2EA","Harp");
        colorNameDB.insertNewColorData("E0B974","Harvest Gold");
        colorNameDB.insertNewColorData("5590D9","Havelock Blue");
        colorNameDB.insertNewColorData("9D5616","Hawaiian Tan");
        colorNameDB.insertNewColorData("D4E2FC","Hawkes Blue");
        colorNameDB.insertNewColorData("541012","Heath");
        colorNameDB.insertNewColorData("B7C3D0","Heather");
        colorNameDB.insertNewColorData("B6B095","Heathered Gray");
        colorNameDB.insertNewColorData("2B3228","Heavy Metal");
        colorNameDB.insertNewColorData("DF73FF","Heliotrope");
        colorNameDB.insertNewColorData("5E5D3B","Hemlock");
        colorNameDB.insertNewColorData("907874","Hemp");
        colorNameDB.insertNewColorData("B6316C","Hibiscus");
        colorNameDB.insertNewColorData("6F8E63","Highland");
        colorNameDB.insertNewColorData("ACA586","Hillary");
        colorNameDB.insertNewColorData("6A5D1B","Himalaya");
        colorNameDB.insertNewColorData("E6FFE9","Hint of Green");
        colorNameDB.insertNewColorData("FBF9F9","Hint of Red");
        colorNameDB.insertNewColorData("FAFDE4","Hint of Yellow");
        colorNameDB.insertNewColorData("589AAF","Hippie Blue");
        colorNameDB.insertNewColorData("53824B","Hippie Green");
        colorNameDB.insertNewColorData("AE4560","Hippie Pink");
        colorNameDB.insertNewColorData("A1ADB5","Hit Gray");
        colorNameDB.insertNewColorData("FFAB81","Hit Pink");
        colorNameDB.insertNewColorData("C8A528","Hokey Pokey");
        colorNameDB.insertNewColorData("65869F","Hoki");
        colorNameDB.insertNewColorData("011D13","Holly");
        colorNameDB.insertNewColorData("F400A1","Hollywood Cerise");
        colorNameDB.insertNewColorData("4F1C70","Honey Flower");
        colorNameDB.insertNewColorData("EDFC84","Honeysuckle");
        colorNameDB.insertNewColorData("D06DA1","Hopbush");
        colorNameDB.insertNewColorData("5A87A0","Horizon");
        colorNameDB.insertNewColorData("604913","Horses Neck");
        colorNameDB.insertNewColorData("D2691E","Hot Cinnamon");
        colorNameDB.insertNewColorData("FF69B4","Hot Pink");
        colorNameDB.insertNewColorData("B38007","Hot Toddy");
        colorNameDB.insertNewColorData("CFF9F3","Humming Bird");
        colorNameDB.insertNewColorData("161D10","Hunter Green");
        colorNameDB.insertNewColorData("877C7B","Hurricane");
        colorNameDB.insertNewColorData("B7A458","Husk");
        colorNameDB.insertNewColorData("B1F4E7","Ice Cold");
        colorNameDB.insertNewColorData("DAF4F0","Iceberg");
        colorNameDB.insertNewColorData("F6A4C9","Illusion");
        colorNameDB.insertNewColorData("B0E313","Inch Worm");
        colorNameDB.insertNewColorData("C3B091","Indian Khaki");
        colorNameDB.insertNewColorData("4D1E01","Indian Tan");
        colorNameDB.insertNewColorData("4F69C6","Indigo");
        colorNameDB.insertNewColorData("C26B03","Indochine");
        colorNameDB.insertNewColorData("002FA7","International Klein Blue");
        colorNameDB.insertNewColorData("FF4F00","International Orange");
        colorNameDB.insertNewColorData("5F3D26","Irish Coffee");
        colorNameDB.insertNewColorData("433120","Iroko");
        colorNameDB.insertNewColorData("D4D7D9","Iron");
        colorNameDB.insertNewColorData("676662","Ironside Gray");
        colorNameDB.insertNewColorData("86483C","Ironstone");
        colorNameDB.insertNewColorData("FFFCEE","Island Spice");
        colorNameDB.insertNewColorData("FFFFF0","Ivory");
        colorNameDB.insertNewColorData("2E0329","Jacaranda");
        colorNameDB.insertNewColorData("3A2A6A","Jacarta");
        colorNameDB.insertNewColorData("2E1905","Jacko Bean");
        colorNameDB.insertNewColorData("20208D","Jacksons Purple");
        colorNameDB.insertNewColorData("00A86B","Jade");
        colorNameDB.insertNewColorData("EF863F","Jaffa");
        colorNameDB.insertNewColorData("C2E8E5","Jagged Ice");
        colorNameDB.insertNewColorData("350E57","Jagger");
        colorNameDB.insertNewColorData("080110","Jaguar");
        colorNameDB.insertNewColorData("5B3013","Jambalaya");
        colorNameDB.insertNewColorData("F4EBD3","Janna");
        colorNameDB.insertNewColorData("0A6906","Japanese Laurel");
        colorNameDB.insertNewColorData("780109","Japanese Maple");
        colorNameDB.insertNewColorData("D87C63","Japonica");
        colorNameDB.insertNewColorData("1FC2C2","Java");
        colorNameDB.insertNewColorData("A50B5E","Jazzberry Jam");
        colorNameDB.insertNewColorData("297B9A","Jelly Bean");
        colorNameDB.insertNewColorData("B5D2CE","Jet Stream");
        colorNameDB.insertNewColorData("126B40","Jewel");
        colorNameDB.insertNewColorData("3B1F1F","Jon");
        colorNameDB.insertNewColorData("EEFF9A","Jonquil");
        colorNameDB.insertNewColorData("8AB9F1","Jordy Blue");
        colorNameDB.insertNewColorData("544333","Judge Gray");
        colorNameDB.insertNewColorData("7C7B82","Jumbo");
        colorNameDB.insertNewColorData("29AB87","Jungle Green");
        colorNameDB.insertNewColorData("B4CFD3","Jungle Mist");
        colorNameDB.insertNewColorData("6D9292","Juniper");
        colorNameDB.insertNewColorData("ECCDB9","Just Right");
        colorNameDB.insertNewColorData("5E483E","Kabul");
        colorNameDB.insertNewColorData("004620","Kaitoke Green");
        colorNameDB.insertNewColorData("C6C8BD","Kangaroo");
        colorNameDB.insertNewColorData("1E1609","Karaka");
        colorNameDB.insertNewColorData("FFEAD4","Karry");
        colorNameDB.insertNewColorData("507096","Kashmir Blue");
        colorNameDB.insertNewColorData("454936","Kelp");
        colorNameDB.insertNewColorData("7C1C05","Kenyan Copper");
        colorNameDB.insertNewColorData("3AB09E","Keppel");
        colorNameDB.insertNewColorData("BFC921","Key Lime Pie");
        colorNameDB.insertNewColorData("F0E68C","Khaki");
        colorNameDB.insertNewColorData("E1EAD4","Kidnapper");
        colorNameDB.insertNewColorData("240C02","Kilamanjaro");
        colorNameDB.insertNewColorData("3A6A47","Killarney");
        colorNameDB.insertNewColorData("736C9F","Kimberly");
        colorNameDB.insertNewColorData("3E0480","Kingfisher Daisy");
        colorNameDB.insertNewColorData("E79FC4","Kobi");
        colorNameDB.insertNewColorData("6E6D57","Kokoda");
        colorNameDB.insertNewColorData("8F4B0E","Korma");
        colorNameDB.insertNewColorData("FFBD5F","Koromiko");
        colorNameDB.insertNewColorData("FFE772","Kournikova");
        colorNameDB.insertNewColorData("886221","Kumera");
        colorNameDB.insertNewColorData("368716","La Palma");
        colorNameDB.insertNewColorData("B3C110","La Rioja");
        colorNameDB.insertNewColorData("C6E610","Las Palmas");
        colorNameDB.insertNewColorData("C8B568","Laser");
        colorNameDB.insertNewColorData("FFFF66","Laser Lemon");
        colorNameDB.insertNewColorData("749378","Laurel");
        colorNameDB.insertNewColorData("B57EDC","Lavender");
        colorNameDB.insertNewColorData("BDBBD7","Lavender Gray");
        colorNameDB.insertNewColorData("EE82EE","Lavender Magenta");
        colorNameDB.insertNewColorData("FBAED2","Lavender Pink");
        colorNameDB.insertNewColorData("967BB6","Lavender Purple");
        colorNameDB.insertNewColorData("FBA0E3","Lavender Rose");
        colorNameDB.insertNewColorData("FFF0F5","Lavender blush");
        colorNameDB.insertNewColorData("967059","Leather");
        colorNameDB.insertNewColorData("FDE910","Lemon");
        colorNameDB.insertNewColorData("FFFACD","Lemon Chiffon");
        colorNameDB.insertNewColorData("AC9E22","Lemon Ginger");
        colorNameDB.insertNewColorData("9B9E8F","Lemon Grass");
        colorNameDB.insertNewColorData("FDD5B1","Light Apricot");
        colorNameDB.insertNewColorData("E29CD2","Light Orchid");
        colorNameDB.insertNewColorData("C9A0DC","Light Wisteria");
        colorNameDB.insertNewColorData("FCC01E","Lightning Yellow");
        colorNameDB.insertNewColorData("C8A2C8","Lilac");
        colorNameDB.insertNewColorData("9874D3","Lilac Bush");
        colorNameDB.insertNewColorData("C8AABF","Lily");
        colorNameDB.insertNewColorData("E7F8FF","Lily White");
        colorNameDB.insertNewColorData("76BD17","Lima");
        colorNameDB.insertNewColorData("BFFF00","Lime");
        colorNameDB.insertNewColorData("6F9D02","Limeade");
        colorNameDB.insertNewColorData("747D63","Limed Ash");
        colorNameDB.insertNewColorData("AC8A56","Limed Oak");
        colorNameDB.insertNewColorData("394851","Limed Spruce");
        colorNameDB.insertNewColorData("FAF0E6","Linen");
        colorNameDB.insertNewColorData("D9E4F5","Link Water");
        colorNameDB.insertNewColorData("AB0563","Lipstick");
        colorNameDB.insertNewColorData("423921","Lisbon Brown");
        colorNameDB.insertNewColorData("4D282E","Livid Brown");
        colorNameDB.insertNewColorData("EEF4DE","Loafer ");
        colorNameDB.insertNewColorData("BDC9CE","Loblolly");
        colorNameDB.insertNewColorData("2C8C84","Lochinvar");
        colorNameDB.insertNewColorData("007EC7","Lochmara");
        colorNameDB.insertNewColorData("A8AF8E","Locust");
        colorNameDB.insertNewColorData("242A1D","Log Cabin");
        colorNameDB.insertNewColorData("AAA9CD","Logan");
        colorNameDB.insertNewColorData("DFCFDB","Lola");
        colorNameDB.insertNewColorData("BEA6C3","London Hue");
        colorNameDB.insertNewColorData("6D0101","Lonestar");
        colorNameDB.insertNewColorData("863C3C","Lotus");
        colorNameDB.insertNewColorData("460B41","Loulou");
        colorNameDB.insertNewColorData("AF9F1C","Lucky");
        colorNameDB.insertNewColorData("1A1A68","Lucky Point");
        colorNameDB.insertNewColorData("3C493A","Lunar Green");
        colorNameDB.insertNewColorData("A7882C","Luxor Gold");
        colorNameDB.insertNewColorData("697E9A","Lynch");
        colorNameDB.insertNewColorData("D9F7FF","Mabel");
        colorNameDB.insertNewColorData("FFB97B","Macaroni and Cheese");
        colorNameDB.insertNewColorData("B7F0BE","Madang");
        colorNameDB.insertNewColorData("09255D","Madison");
        colorNameDB.insertNewColorData("3F3002","Madras");
        colorNameDB.insertNewColorData("FF00FF","Magenta / Fuchsia");
        colorNameDB.insertNewColorData("AAF0D1","Magic Mint");
        colorNameDB.insertNewColorData("F8F4FF","Magnolia");
        colorNameDB.insertNewColorData("4E0606","Mahogany");
        colorNameDB.insertNewColorData("B06608","Mai Tai");
        colorNameDB.insertNewColorData("F5D5A0","Maize");
        colorNameDB.insertNewColorData("897D6D","Makara");
        colorNameDB.insertNewColorData("444954","Mako");
        colorNameDB.insertNewColorData("0BDA51","Malachite");
        colorNameDB.insertNewColorData("7DC8F7","Malibu");
        colorNameDB.insertNewColorData("233418","Mallard");
        colorNameDB.insertNewColorData("BDB2A1","Malta");
        colorNameDB.insertNewColorData("8E8190","Mamba");
        colorNameDB.insertNewColorData("8D90A1","Manatee");
        colorNameDB.insertNewColorData("AD781B","Mandalay");
        colorNameDB.insertNewColorData("E25465","Mandy");
        colorNameDB.insertNewColorData("F2C3B2","Mandys Pink");
        colorNameDB.insertNewColorData("E77200","Mango Tango");
        colorNameDB.insertNewColorData("F5C999","Manhattan");
        colorNameDB.insertNewColorData("74C365","Mantis");
        colorNameDB.insertNewColorData("8B9C90","Mantle");
        colorNameDB.insertNewColorData("EEEF78","Manz");
        colorNameDB.insertNewColorData("350036","Mardi Gras");
        colorNameDB.insertNewColorData("B98D28","Marigold");
        colorNameDB.insertNewColorData("FBE870","Marigold Yellow");
        colorNameDB.insertNewColorData("286ACD","Mariner");
        colorNameDB.insertNewColorData("800000","Maroon");
        colorNameDB.insertNewColorData("C32148","Maroon Flush");
        colorNameDB.insertNewColorData("520C17","Maroon Oak");
        colorNameDB.insertNewColorData("0B0F08","Marshland");
        colorNameDB.insertNewColorData("AFA09E","Martini");
        colorNameDB.insertNewColorData("363050","Martinique");
        colorNameDB.insertNewColorData("F8DB9D","Marzipan");
        colorNameDB.insertNewColorData("403B38","Masala");
        colorNameDB.insertNewColorData("1B659D","Matisse");
        colorNameDB.insertNewColorData("B05D54","Matrix");
        colorNameDB.insertNewColorData("4E3B41","Matterhorn");
        colorNameDB.insertNewColorData("E0B0FF","Mauve");
        colorNameDB.insertNewColorData("F091A9","Mauvelous");
        colorNameDB.insertNewColorData("D8C2D5","Maverick");
        colorNameDB.insertNewColorData("AF4035","Medium Carmine");
        colorNameDB.insertNewColorData("9370DB","Medium Purple");
        colorNameDB.insertNewColorData("BB3385","Medium Red Violet");
        colorNameDB.insertNewColorData("E4C2D5","Melanie");
        colorNameDB.insertNewColorData("300529","Melanzane");
        colorNameDB.insertNewColorData("FEBAAD","Melon");
        colorNameDB.insertNewColorData("C7C1FF","Melrose");
        colorNameDB.insertNewColorData("E5E5E5","Mercury");
        colorNameDB.insertNewColorData("F6F0E6","Merino");
        colorNameDB.insertNewColorData("413C37","Merlin");
        colorNameDB.insertNewColorData("831923","Merlot");
        colorNameDB.insertNewColorData("49371B","Metallic Bronze");
        colorNameDB.insertNewColorData("71291D","Metallic Copper");
        colorNameDB.insertNewColorData("D07D12","Meteor");
        colorNameDB.insertNewColorData("3C1F76","Meteorite");
        colorNameDB.insertNewColorData("A72525","Mexican Red");
        colorNameDB.insertNewColorData("5F5F6E","Mid Gray");
        colorNameDB.insertNewColorData("011635","Midnight");
        colorNameDB.insertNewColorData("003366","Midnight Blue");
        colorNameDB.insertNewColorData("041004","Midnight Moss");
        colorNameDB.insertNewColorData("2D2510","Mikado");
        colorNameDB.insertNewColorData("FAFFA4","Milan");
        colorNameDB.insertNewColorData("B81104","Milano Red");
        colorNameDB.insertNewColorData("FFF6D4","Milk Punch");
        colorNameDB.insertNewColorData("594433","Millbrook");
        colorNameDB.insertNewColorData("F8FDD3","Mimosa");
        colorNameDB.insertNewColorData("E3F988","Mindaro");
        colorNameDB.insertNewColorData("323232","Mine Shaft");
        colorNameDB.insertNewColorData("3F5D53","Mineral Green");
        colorNameDB.insertNewColorData("36747D","Ming");
        colorNameDB.insertNewColorData("3F307F","Minsk");
        colorNameDB.insertNewColorData("98FF98","Mint Green");
        colorNameDB.insertNewColorData("F1EEC1","Mint Julep");
        colorNameDB.insertNewColorData("C4F4EB","Mint Tulip");
        colorNameDB.insertNewColorData("161928","Mirage");
        colorNameDB.insertNewColorData("D1D2DD","Mischka");
        colorNameDB.insertNewColorData("C4C4BC","Mist Gray");
        colorNameDB.insertNewColorData("7F7589","Mobster");
        colorNameDB.insertNewColorData("6E1D14","Moccaccino");
        colorNameDB.insertNewColorData("782D19","Mocha");
        colorNameDB.insertNewColorData("C04737","Mojo");
        colorNameDB.insertNewColorData("FFA194","Mona Lisa");
        colorNameDB.insertNewColorData("8B0723","Monarch");
        colorNameDB.insertNewColorData("4A3C30","Mondo");
        colorNameDB.insertNewColorData("B5A27F","Mongoose");
        colorNameDB.insertNewColorData("8A8389","Monsoon");
        colorNameDB.insertNewColorData("83D0C6","Monte Carlo");
        colorNameDB.insertNewColorData("C7031E","Monza");
        colorNameDB.insertNewColorData("7F76D3","Moody Blue");
        colorNameDB.insertNewColorData("FCFEDA","Moon Glow");
        colorNameDB.insertNewColorData("DCDDCC","Moon Mist");
        colorNameDB.insertNewColorData("D6CEF6","Moon Raker");
        colorNameDB.insertNewColorData("9EDEE0","Morning Glory");
        colorNameDB.insertNewColorData("441D00","Morocco Brown");
        colorNameDB.insertNewColorData("504351","Mortar");
        colorNameDB.insertNewColorData("036A6E","Mosque");
        colorNameDB.insertNewColorData("ADDFAD","Moss Green");
        colorNameDB.insertNewColorData("1AB385","Mountain Meadow");
        colorNameDB.insertNewColorData("959396","Mountain Mist");
        colorNameDB.insertNewColorData("997A8D","Mountbatten Pink");
        colorNameDB.insertNewColorData("B78E5C","Muddy Waters");
        colorNameDB.insertNewColorData("AA8B5B","Muesli");
        colorNameDB.insertNewColorData("C54B8C","Mulberry");
        colorNameDB.insertNewColorData("5C0536","Mulberry Wood");
        colorNameDB.insertNewColorData("8C472F","Mule Fawn");
        colorNameDB.insertNewColorData("4E4562","Mulled Wine");
        colorNameDB.insertNewColorData("FFDB58","Mustard");
        colorNameDB.insertNewColorData("D69188","My Pink");
        colorNameDB.insertNewColorData("FFB31F","My Sin");
        colorNameDB.insertNewColorData("E2EBED","Mystic");
        colorNameDB.insertNewColorData("4B5D52","Nandor");
        colorNameDB.insertNewColorData("ACA494","Napa");
        colorNameDB.insertNewColorData("EDF9F1","Narvik");
        colorNameDB.insertNewColorData("8B8680","Natural Gray");
        colorNameDB.insertNewColorData("FFDEAD","Navajo White");
        colorNameDB.insertNewColorData("000080","Navy Blue");
        colorNameDB.insertNewColorData("CBDBD6","Nebula");
        colorNameDB.insertNewColorData("FFE2C5","Negroni");
        colorNameDB.insertNewColorData("FF9933","Neon Carrot");
        colorNameDB.insertNewColorData("8EABC1","Nepal");
        colorNameDB.insertNewColorData("7CB7BB","Neptune");
        colorNameDB.insertNewColorData("140600","Nero");
        colorNameDB.insertNewColorData("646E75","Nevada");
        colorNameDB.insertNewColorData("F3D69D","New Orleans");
        colorNameDB.insertNewColorData("D7837F","New York Pink");
        colorNameDB.insertNewColorData("06A189","Niagara");
        colorNameDB.insertNewColorData("1F120F","Night Rider");
        colorNameDB.insertNewColorData("AA375A","Night Shadz");
        colorNameDB.insertNewColorData("193751","Nile Blue");
        colorNameDB.insertNewColorData("B7B1B1","Nobel");
        colorNameDB.insertNewColorData("BAB1A2","Nomad");
        colorNameDB.insertNewColorData("A8BD9F","Norway");
        colorNameDB.insertNewColorData("C59922","Nugget");
        colorNameDB.insertNewColorData("81422C","Nutmeg");
        colorNameDB.insertNewColorData("683600","Nutmeg Wood Finish");
        colorNameDB.insertNewColorData("FEEFCE","Oasis");
        colorNameDB.insertNewColorData("02866F","Observatory");
        colorNameDB.insertNewColorData("41AA78","Ocean Green");
        colorNameDB.insertNewColorData("CC7722","Ochre");
        colorNameDB.insertNewColorData("E6F8F3","Off Green");
        colorNameDB.insertNewColorData("FEF9E3","Off Yellow");
        colorNameDB.insertNewColorData("281E15","Oil");
        colorNameDB.insertNewColorData("901E1E","Old Brick");
        colorNameDB.insertNewColorData("724A2F","Old Copper");
        colorNameDB.insertNewColorData("CFB53B","Old Gold");
        colorNameDB.insertNewColorData("FDF5E6","Old Lace");
        colorNameDB.insertNewColorData("796878","Old Lavender");
        colorNameDB.insertNewColorData("C08081","Old Rose");
        colorNameDB.insertNewColorData("808000","Olive");
        colorNameDB.insertNewColorData("6B8E23","Olive Drab");
        colorNameDB.insertNewColorData("B5B35C","Olive Green");
        colorNameDB.insertNewColorData("8B8470","Olive Haze");
        colorNameDB.insertNewColorData("716E10","Olivetone");
        colorNameDB.insertNewColorData("9AB973","Olivine");
        colorNameDB.insertNewColorData("CDF4FF","Onahau");
        colorNameDB.insertNewColorData("2F270E","Onion");
        colorNameDB.insertNewColorData("A9C6C2","Opal");
        colorNameDB.insertNewColorData("8E6F70","Opium");
        colorNameDB.insertNewColorData("377475","Oracle");
        colorNameDB.insertNewColorData("FF681F","Orange");
        colorNameDB.insertNewColorData("FFA000","Orange Peel");
        colorNameDB.insertNewColorData("C45719","Orange Roughy");
        colorNameDB.insertNewColorData("FEFCED","Orange White");
        colorNameDB.insertNewColorData("DA70D6","Orchid");
        colorNameDB.insertNewColorData("FFFDF3","Orchid White");
        colorNameDB.insertNewColorData("9B4703","Oregon");
        colorNameDB.insertNewColorData("015E85","Orient");
        colorNameDB.insertNewColorData("C69191","Oriental Pink");
        colorNameDB.insertNewColorData("F3FBD4","Orinoco");
        colorNameDB.insertNewColorData("878D91","Oslo Gray");
        colorNameDB.insertNewColorData("E9F8ED","Ottoman");
        colorNameDB.insertNewColorData("2D383A","Outer Space");
        colorNameDB.insertNewColorData("FF6037","Outrageous Orange");
        colorNameDB.insertNewColorData("384555","Oxford Blue");
        colorNameDB.insertNewColorData("779E86","Oxley");
        colorNameDB.insertNewColorData("DAFAFF","Oyster Bay");
        colorNameDB.insertNewColorData("E9CECD","Oyster Pink");
        colorNameDB.insertNewColorData("A65529","Paarl");
        colorNameDB.insertNewColorData("776F61","Pablo");
        colorNameDB.insertNewColorData("009DC4","Pacific Blue");
        colorNameDB.insertNewColorData("778120","Pacifika");
        colorNameDB.insertNewColorData("411F10","Paco");
        colorNameDB.insertNewColorData("ADE6C4","Padua");
        colorNameDB.insertNewColorData("FFFF99","Pale Canary");
        colorNameDB.insertNewColorData("C0D3B9","Pale Leaf");
        colorNameDB.insertNewColorData("988D77","Pale Oyster");
        colorNameDB.insertNewColorData("FDFEB8","Pale Prim");
        colorNameDB.insertNewColorData("FFE1F2","Pale Rose");
        colorNameDB.insertNewColorData("6E7783","Pale Sky");
        colorNameDB.insertNewColorData("C3BFC1","Pale Slate");
        colorNameDB.insertNewColorData("09230F","Palm Green");
        colorNameDB.insertNewColorData("19330E","Palm Leaf");
        colorNameDB.insertNewColorData("F4F2EE","Pampas");
        colorNameDB.insertNewColorData("EAF6EE","Panache");
        colorNameDB.insertNewColorData("EDCDAB","Pancho");
        colorNameDB.insertNewColorData("FFEFD5","Papaya Whip");
        colorNameDB.insertNewColorData("8D0226","Paprika");
        colorNameDB.insertNewColorData("317D82","Paradiso");
        colorNameDB.insertNewColorData("F1E9D2","Parchment");
        colorNameDB.insertNewColorData("FFF46E","Paris Daisy");
        colorNameDB.insertNewColorData("26056A","Paris M");
        colorNameDB.insertNewColorData("CADCD4","Paris White");
        colorNameDB.insertNewColorData("134F19","Parsley");
        colorNameDB.insertNewColorData("77DD77","Pastel Green");
        colorNameDB.insertNewColorData("FFD1DC","Pastel Pink");
        colorNameDB.insertNewColorData("639A8F","Patina");
        colorNameDB.insertNewColorData("DEF5FF","Pattens Blue");
        colorNameDB.insertNewColorData("260368","Paua");
        colorNameDB.insertNewColorData("D7C498","Pavlova");
        colorNameDB.insertNewColorData("FFE5B4","Peach");
        colorNameDB.insertNewColorData("FFF0DB","Peach Cream");
        colorNameDB.insertNewColorData("FFCC99","Peach Orange");
        colorNameDB.insertNewColorData("FFDCD6","Peach Schnapps");
        colorNameDB.insertNewColorData("FADFAD","Peach Yellow");
        colorNameDB.insertNewColorData("782F16","Peanut");
        colorNameDB.insertNewColorData("D1E231","Pear");
        colorNameDB.insertNewColorData("E8E0D5","Pearl Bush");
        colorNameDB.insertNewColorData("FCF4DC","Pearl Lusta");
        colorNameDB.insertNewColorData("716B56","Peat");
        colorNameDB.insertNewColorData("3EABBF","Pelorous");
        colorNameDB.insertNewColorData("E3F5E1","Peppermint");
        colorNameDB.insertNewColorData("A9BEF2","Perano");
        colorNameDB.insertNewColorData("D0BEF8","Perfume");
        colorNameDB.insertNewColorData("E1E6D6","Periglacial Blue");
        colorNameDB.insertNewColorData("CCCCFF","Periwinkle");
        colorNameDB.insertNewColorData("C3CDE6","Periwinkle Gray");
        colorNameDB.insertNewColorData("1C39BB","Persian Blue");
        colorNameDB.insertNewColorData("00A693","Persian Green");
        colorNameDB.insertNewColorData("32127A","Persian Indigo");
        colorNameDB.insertNewColorData("F77FBE","Persian Pink");
        colorNameDB.insertNewColorData("701C1C","Persian Plum");
        colorNameDB.insertNewColorData("CC3333","Persian Red");
        colorNameDB.insertNewColorData("FE28A2","Persian Rose");
        colorNameDB.insertNewColorData("FF6B53","Persimmon");
        colorNameDB.insertNewColorData("7F3A02","Peru Tan");
        colorNameDB.insertNewColorData("7C7631","Pesto");
        colorNameDB.insertNewColorData("DB9690","Petite Orchid");
        colorNameDB.insertNewColorData("96A8A1","Pewter");
        colorNameDB.insertNewColorData("A3807B","Pharlap");
        colorNameDB.insertNewColorData("FFF39D","Picasso");
        colorNameDB.insertNewColorData("6E4826","Pickled Bean");
        colorNameDB.insertNewColorData("314459","Pickled Bluewood");
        colorNameDB.insertNewColorData("45B1E8","Picton Blue");
        colorNameDB.insertNewColorData("FDD7E4","Pig Pink");
        colorNameDB.insertNewColorData("AFBDD9","Pigeon Post");
        colorNameDB.insertNewColorData("4B0082","Pigment Indigo");
        colorNameDB.insertNewColorData("6D5E54","Pine Cone");
        colorNameDB.insertNewColorData("C7CD90","Pine Glade");
        colorNameDB.insertNewColorData("01796F","Pine Green");
        colorNameDB.insertNewColorData("171F04","Pine Tree");
        colorNameDB.insertNewColorData("FFC0CB","Pink");
        colorNameDB.insertNewColorData("FF66FF","Pink Flamingo");
        colorNameDB.insertNewColorData("E1C0C8","Pink Flare");
        colorNameDB.insertNewColorData("FFDDF4","Pink Lace");
        colorNameDB.insertNewColorData("FFF1D8","Pink Lady");
        colorNameDB.insertNewColorData("FF91A4","Pink Salmon");
        colorNameDB.insertNewColorData("BEB5B7","Pink Swan");
        colorNameDB.insertNewColorData("C96323","Piper");
        colorNameDB.insertNewColorData("FEF4CC","Pipi");
        colorNameDB.insertNewColorData("FFE1DF","Pippin");
        colorNameDB.insertNewColorData("BA7F03","Pirate Gold");
        colorNameDB.insertNewColorData("9DC209","Pistachio");
        colorNameDB.insertNewColorData("C0D8B6","Pixie Green");
        colorNameDB.insertNewColorData("FF9000","Pizazz");
        colorNameDB.insertNewColorData("C99415","Pizza");
        colorNameDB.insertNewColorData("27504B","Plantation");
        colorNameDB.insertNewColorData("843179","Plum");
        colorNameDB.insertNewColorData("8F021C","Pohutukawa");
        colorNameDB.insertNewColorData("E5F9F6","Polar");
        colorNameDB.insertNewColorData("8DA8CC","Polo Blue");
        colorNameDB.insertNewColorData("F34723","Pomegranate");
        colorNameDB.insertNewColorData("660045","Pompadour");
        colorNameDB.insertNewColorData("EFF2F3","Porcelain");
        colorNameDB.insertNewColorData("EAAE69","Porsche");
        colorNameDB.insertNewColorData("251F4F","Port Gore");
        colorNameDB.insertNewColorData("FFFFB4","Portafino");
        colorNameDB.insertNewColorData("8B9FEE","Portage");
        colorNameDB.insertNewColorData("F9E663","Portica");
        colorNameDB.insertNewColorData("F5E7E2","Pot Pourri");
        colorNameDB.insertNewColorData("8C5738","Potters Clay");
        colorNameDB.insertNewColorData("BCC9C2","Powder Ash");
        colorNameDB.insertNewColorData("B0E0E6","Powder Blue");
        colorNameDB.insertNewColorData("9A3820","Prairie Sand");
        colorNameDB.insertNewColorData("D0C0E5","Prelude");
        colorNameDB.insertNewColorData("F0E2EC","Prim");
        colorNameDB.insertNewColorData("EDEA99","Primrose");
        colorNameDB.insertNewColorData("FEF5F1","Provincial Pink");
        colorNameDB.insertNewColorData("003153","Prussian Blue");
        colorNameDB.insertNewColorData("CC8899","Puce");
        colorNameDB.insertNewColorData("7D2C14","Pueblo");
        colorNameDB.insertNewColorData("3FC1AA","Puerto Rico");
        colorNameDB.insertNewColorData("C2CAC4","Pumice");
        colorNameDB.insertNewColorData("FF7518","Pumpkin");
        colorNameDB.insertNewColorData("B1610B","Pumpkin Skin");
        colorNameDB.insertNewColorData("DC4333","Punch");
        colorNameDB.insertNewColorData("4D3D14","Punga");
        colorNameDB.insertNewColorData("660099","Purple");
        colorNameDB.insertNewColorData("652DC1","Purple Heart");
        colorNameDB.insertNewColorData("9678B6","Purple Mountain's Majesty");
        colorNameDB.insertNewColorData("FF00CC","Purple Pizzazz");
        colorNameDB.insertNewColorData("E7CD8C","Putty");
        colorNameDB.insertNewColorData("FFFDF4","Quarter Pearl Lusta");
        colorNameDB.insertNewColorData("F7F2E1","Quarter Spanish White");
        colorNameDB.insertNewColorData("BD978E","Quicksand");
        colorNameDB.insertNewColorData("D6D6D1","Quill Gray");
        colorNameDB.insertNewColorData("623F2D","Quincy");
        colorNameDB.insertNewColorData("0C1911","Racing Green");
        colorNameDB.insertNewColorData("FF355E","Radical Red");
        colorNameDB.insertNewColorData("EADAB8","Raffia");
        colorNameDB.insertNewColorData("B9C8AC","Rainee");
        colorNameDB.insertNewColorData("F7B668","Rajah");
        colorNameDB.insertNewColorData("2E3222","Rangitoto");
        colorNameDB.insertNewColorData("1C1E13","Rangoon Green");
        colorNameDB.insertNewColorData("727B89","Raven");
        colorNameDB.insertNewColorData("D27D46","Raw Sienna");
        colorNameDB.insertNewColorData("734A12","Raw Umber");
        colorNameDB.insertNewColorData("FF33CC","Razzle Dazzle Rose");
        colorNameDB.insertNewColorData("E30B5C","Razzmatazz");
        colorNameDB.insertNewColorData("3C1206","Rebel");
        colorNameDB.insertNewColorData("FF0000","Red");
        colorNameDB.insertNewColorData("7B3801","Red Beech");
        colorNameDB.insertNewColorData("8E0000","Red Berry");
        colorNameDB.insertNewColorData("DA6A41","Red Damask");
        colorNameDB.insertNewColorData("860111","Red Devil");
        colorNameDB.insertNewColorData("FF3F34","Red Orange");
        colorNameDB.insertNewColorData("6E0902","Red Oxide");
        colorNameDB.insertNewColorData("ED0A3F","Red Ribbon");
        colorNameDB.insertNewColorData("80341F","Red Robin");
        colorNameDB.insertNewColorData("D05F04","Red Stage");
        colorNameDB.insertNewColorData("C71585","Red Violet");
        colorNameDB.insertNewColorData("5D1E0F","Redwood");
        colorNameDB.insertNewColorData("C9FFA2","Reef");
        colorNameDB.insertNewColorData("9F821C","Reef Gold");
        colorNameDB.insertNewColorData("013F6A","Regal Blue");
        colorNameDB.insertNewColorData("86949F","Regent Gray");
        colorNameDB.insertNewColorData("AAD6E6","Regent St Blue");
        colorNameDB.insertNewColorData("FEEBF3","Remy");
        colorNameDB.insertNewColorData("A86515","Reno Sand");
        colorNameDB.insertNewColorData("002387","Resolution Blue");
        colorNameDB.insertNewColorData("2C1632","Revolver");
        colorNameDB.insertNewColorData("2E3F62","Rhino");
        colorNameDB.insertNewColorData("FFFEF0","Rice Cake");
        colorNameDB.insertNewColorData("EEFFE2","Rice Flower");
        colorNameDB.insertNewColorData("A85307","Rich Gold");
        colorNameDB.insertNewColorData("BBD009","Rio Grande");
        colorNameDB.insertNewColorData("F4D81C","Ripe Lemon");
        colorNameDB.insertNewColorData("410056","Ripe Plum");
        colorNameDB.insertNewColorData("8BE6D8","Riptide");
        colorNameDB.insertNewColorData("434C59","River Bed");
        colorNameDB.insertNewColorData("EAC674","Rob Roy");
        colorNameDB.insertNewColorData("00CCCC","Robin's Egg Blue");
        colorNameDB.insertNewColorData("4D3833","Rock");
        colorNameDB.insertNewColorData("9EB1CD","Rock Blue");
        colorNameDB.insertNewColorData("BA450C","Rock Spray");
        colorNameDB.insertNewColorData("C9B29B","Rodeo Dust");
        colorNameDB.insertNewColorData("747D83","Rolling Stone");
        colorNameDB.insertNewColorData("DE6360","Roman");
        colorNameDB.insertNewColorData("795D4C","Roman Coffee");
        colorNameDB.insertNewColorData("FFFEFD","Romance");
        colorNameDB.insertNewColorData("FFD2B7","Romantic");
        colorNameDB.insertNewColorData("ECC54E","Ronchi");
        colorNameDB.insertNewColorData("A62F20","Roof Terracotta");
        colorNameDB.insertNewColorData("8E4D1E","Rope");
        colorNameDB.insertNewColorData("FF007F","Rose");
        colorNameDB.insertNewColorData("FBB2A3","Rose Bud");
        colorNameDB.insertNewColorData("800B47","Rose Bud Cherry");
        colorNameDB.insertNewColorData("E7BCB4","Rose Fog");
        colorNameDB.insertNewColorData("FFF6F5","Rose White");
        colorNameDB.insertNewColorData("BF5500","Rose of Sharon");
        colorNameDB.insertNewColorData("65000B","Rosewood");
        colorNameDB.insertNewColorData("C6A84B","Roti");
        colorNameDB.insertNewColorData("A23B6C","Rouge");
        colorNameDB.insertNewColorData("4169E1","Royal Blue");
        colorNameDB.insertNewColorData("AB3472","Royal Heath");
        colorNameDB.insertNewColorData("6B3FA0","Royal Purple");
        colorNameDB.insertNewColorData("796989","Rum");
        colorNameDB.insertNewColorData("F9F8E4","Rum Swizzle");
        colorNameDB.insertNewColorData("80461B","Russet");
        colorNameDB.insertNewColorData("755A57","Russett");
        colorNameDB.insertNewColorData("B7410E","Rust");
        colorNameDB.insertNewColorData("480404","Rustic Red");
        colorNameDB.insertNewColorData("86560A","Rusty Nail");
        colorNameDB.insertNewColorData("4C3024","Saddle");
        colorNameDB.insertNewColorData("583401","Saddle Brown");
        colorNameDB.insertNewColorData("F4C430","Saffron");
        colorNameDB.insertNewColorData("F9BF58","Saffron Mango");
        colorNameDB.insertNewColorData("9EA587","Sage");
        colorNameDB.insertNewColorData("B7A214","Sahara");
        colorNameDB.insertNewColorData("F1E788","Sahara Sand");
        colorNameDB.insertNewColorData("B8E0F9","Sail");
        colorNameDB.insertNewColorData("097F4B","Salem");
        colorNameDB.insertNewColorData("FF8C69","Salmon");
        colorNameDB.insertNewColorData("FEDB8D","Salomie");
        colorNameDB.insertNewColorData("685E6E","Salt Box");
        colorNameDB.insertNewColorData("F1F7F2","Saltpan");
        colorNameDB.insertNewColorData("3A2010","Sambuca");
        colorNameDB.insertNewColorData("0B6207","San Felix");
        colorNameDB.insertNewColorData("304B6A","San Juan");
        colorNameDB.insertNewColorData("456CAC","San Marino");
        colorNameDB.insertNewColorData("826F65","Sand Dune");
        colorNameDB.insertNewColorData("AA8D6F","Sandal");
        colorNameDB.insertNewColorData("AB917A","Sandrift");
        colorNameDB.insertNewColorData("796D62","Sandstone");
        colorNameDB.insertNewColorData("F5E7A2","Sandwisp");
        colorNameDB.insertNewColorData("FFEAC8","Sandy Beach");
        colorNameDB.insertNewColorData("F4A460","Sandy brown");
        colorNameDB.insertNewColorData("92000A","Sangria");
        colorNameDB.insertNewColorData("8D3D38","Sanguine Brown");
        colorNameDB.insertNewColorData("B16D52","Santa Fe");
        colorNameDB.insertNewColorData("9FA0B1","Santas Gray");
        colorNameDB.insertNewColorData("DED4A4","Sapling");
        colorNameDB.insertNewColorData("2F519E","Sapphire");
        colorNameDB.insertNewColorData("555B10","Saratoga");
        colorNameDB.insertNewColorData("E6E4D4","Satin Linen");
        colorNameDB.insertNewColorData("FFF5F3","Sauvignon");
        colorNameDB.insertNewColorData("FFF4E0","Sazerac");
        colorNameDB.insertNewColorData("675FA6","Scampi");
        colorNameDB.insertNewColorData("CFFAF4","Scandal");
        colorNameDB.insertNewColorData("FF2400","Scarlet");
        colorNameDB.insertNewColorData("431560","Scarlet Gum");
        colorNameDB.insertNewColorData("950015","Scarlett");
        colorNameDB.insertNewColorData("585562","Scarpa Flow");
        colorNameDB.insertNewColorData("A9B497","Schist");
        colorNameDB.insertNewColorData("FFD800","School bus Yellow");
        colorNameDB.insertNewColorData("8B847E","Schooner");
        colorNameDB.insertNewColorData("0066CC","Science Blue");
        colorNameDB.insertNewColorData("2EBFD4","Scooter");
        colorNameDB.insertNewColorData("695F62","Scorpion");
        colorNameDB.insertNewColorData("FFFBDC","Scotch Mist");
        colorNameDB.insertNewColorData("66FF66","Screamin' Green");
        colorNameDB.insertNewColorData("FBA129","Sea Buckthorn");
        colorNameDB.insertNewColorData("2E8B57","Sea Green");
        colorNameDB.insertNewColorData("C5DBCA","Sea Mist");
        colorNameDB.insertNewColorData("78A39C","Sea Nymph");
        colorNameDB.insertNewColorData("ED989E","Sea Pink");
        colorNameDB.insertNewColorData("80CCEA","Seagull");
        colorNameDB.insertNewColorData("731E8F","Seance");
        colorNameDB.insertNewColorData("F1F1F1","Seashell");
        colorNameDB.insertNewColorData("FFF5EE","Seashell Peach");
        colorNameDB.insertNewColorData("1B2F11","Seaweed");
        colorNameDB.insertNewColorData("F0EEFD","Selago");
        colorNameDB.insertNewColorData("FFBA00","Selective Yellow");
        colorNameDB.insertNewColorData("704214","Sepia");
        colorNameDB.insertNewColorData("2B0202","Sepia Black");
        colorNameDB.insertNewColorData("9E5B40","Sepia Skin");
        colorNameDB.insertNewColorData("FFF4E8","Serenade");
        colorNameDB.insertNewColorData("837050","Shadow");
        colorNameDB.insertNewColorData("9AC2B8","Shadow Green");
        colorNameDB.insertNewColorData("AAA5A9","Shady Lady");
        colorNameDB.insertNewColorData("4EABD1","Shakespeare");
        colorNameDB.insertNewColorData("FBFFBA","Shalimar");
        colorNameDB.insertNewColorData("33CC99","Shamrock");
        colorNameDB.insertNewColorData("25272C","Shark");
        colorNameDB.insertNewColorData("004950","Sherpa Blue");
        colorNameDB.insertNewColorData("02402C","Sherwood Green");
        colorNameDB.insertNewColorData("E8B9B3","Shilo");
        colorNameDB.insertNewColorData("6B4E31","Shingle Fawn");
        colorNameDB.insertNewColorData("788BBA","Ship Cove");
        colorNameDB.insertNewColorData("3E3A44","Ship Gray");
        colorNameDB.insertNewColorData("B20931","Shiraz");
        colorNameDB.insertNewColorData("E292C0","Shocking");
        colorNameDB.insertNewColorData("FC0FC0","Shocking Pink");
        colorNameDB.insertNewColorData("5F6672","Shuttle Gray");
        colorNameDB.insertNewColorData("646A54","Siam");
        colorNameDB.insertNewColorData("F3E7BB","Sidecar");
        colorNameDB.insertNewColorData("BDB1A8","Silk");
        colorNameDB.insertNewColorData("C0C0C0","Silver");
        colorNameDB.insertNewColorData("ACACAC","Silver Chalice");
        colorNameDB.insertNewColorData("C9C0BB","Silver Rust");
        colorNameDB.insertNewColorData("BFC1C2","Silver Sand");
        colorNameDB.insertNewColorData("66B58F","Silver Tree");
        colorNameDB.insertNewColorData("9FD7D3","Sinbad");
        colorNameDB.insertNewColorData("7A013A","Siren");
        colorNameDB.insertNewColorData("718080","Sirocco");
        colorNameDB.insertNewColorData("D3CBBA","Sisal");
        colorNameDB.insertNewColorData("CAE6DA","Skeptic");
        colorNameDB.insertNewColorData("76D7EA","Sky Blue");
        colorNameDB.insertNewColorData("708090","Slate Gray");
        colorNameDB.insertNewColorData("003399","Smalt");
        colorNameDB.insertNewColorData("51808F","Smalt Blue");
        colorNameDB.insertNewColorData("605B73","Smoky");
        colorNameDB.insertNewColorData("F7FAF7","Snow Drift");
        colorNameDB.insertNewColorData("E4FFD1","Snow Flurry");
        colorNameDB.insertNewColorData("D6FFDB","Snowy Mint");
        colorNameDB.insertNewColorData("E2D8ED","Snuff");
        colorNameDB.insertNewColorData("FFFBF9","Soapstone");
        colorNameDB.insertNewColorData("D1C6B4","Soft Amber");
        colorNameDB.insertNewColorData("F5EDEF","Soft Peach");
        colorNameDB.insertNewColorData("893843","Solid Pink");
        colorNameDB.insertNewColorData("FEF8E2","Solitaire");
        colorNameDB.insertNewColorData("EAF6FF","Solitude");
        colorNameDB.insertNewColorData("FD7C07","Sorbus");
        colorNameDB.insertNewColorData("CEB98F","Sorrell Brown");
        colorNameDB.insertNewColorData("6A6051","Soya Bean");
        colorNameDB.insertNewColorData("819885","Spanish Green");
        colorNameDB.insertNewColorData("2F5A57","Spectra");
        colorNameDB.insertNewColorData("6A442E","Spice");
        colorNameDB.insertNewColorData("885342","Spicy Mix");
        colorNameDB.insertNewColorData("74640D","Spicy Mustard");
        colorNameDB.insertNewColorData("816E71","Spicy Pink");
        colorNameDB.insertNewColorData("B6D1EA","Spindle");
        colorNameDB.insertNewColorData("79DEEC","Spray");
        colorNameDB.insertNewColorData("00FF7F","Spring Green");
        colorNameDB.insertNewColorData("578363","Spring Leaves");
        colorNameDB.insertNewColorData("ACCBB1","Spring Rain");
        colorNameDB.insertNewColorData("F6FFDC","Spring Sun");
        colorNameDB.insertNewColorData("F8F6F1","Spring Wood");
        colorNameDB.insertNewColorData("C1D7B0","Sprout");
        colorNameDB.insertNewColorData("AAABB7","Spun Pearl");
        colorNameDB.insertNewColorData("8F8176","Squirrel");
        colorNameDB.insertNewColorData("2D569B","St Tropaz");
        colorNameDB.insertNewColorData("8A8F8A","Stack");
        colorNameDB.insertNewColorData("9F9F9C","Star Dust");
        colorNameDB.insertNewColorData("E5D7BD","Stark White");
        colorNameDB.insertNewColorData("ECF245","Starship");
        colorNameDB.insertNewColorData("4682B4","Steel Blue");
        colorNameDB.insertNewColorData("262335","Steel Gray");
        colorNameDB.insertNewColorData("9C3336","Stiletto");
        colorNameDB.insertNewColorData("928573","Stonewall");
        colorNameDB.insertNewColorData("646463","Storm Dust");
        colorNameDB.insertNewColorData("717486","Storm Gray");
        colorNameDB.insertNewColorData("000741","Stratos");
        colorNameDB.insertNewColorData("D4BF8D","Straw");
        colorNameDB.insertNewColorData("956387","Strikemaster");
        colorNameDB.insertNewColorData("325D52","Stromboli");
        colorNameDB.insertNewColorData("714AB2","Studio");
        colorNameDB.insertNewColorData("BAC7C9","Submarine");
        colorNameDB.insertNewColorData("F9FFF6","Sugar Cane");
        colorNameDB.insertNewColorData("C1F07C","Sulu");
        colorNameDB.insertNewColorData("96BBAB","Summer Green");
        colorNameDB.insertNewColorData("FBAC13","Sun");
        colorNameDB.insertNewColorData("C9B35B","Sundance");
        colorNameDB.insertNewColorData("FFB1B3","Sundown");
        colorNameDB.insertNewColorData("E4D422","Sunflower");
        colorNameDB.insertNewColorData("E16865","Sunglo");
        colorNameDB.insertNewColorData("FFCC33","Sunglow");
        colorNameDB.insertNewColorData("FE4C40","Sunset Orange");
        colorNameDB.insertNewColorData("FF9E2C","Sunshade");
        colorNameDB.insertNewColorData("FFC901","Supernova");
        colorNameDB.insertNewColorData("BBD7C1","Surf");
        colorNameDB.insertNewColorData("CFE5D2","Surf Crest");
        colorNameDB.insertNewColorData("0C7A79","Surfie Green");
        colorNameDB.insertNewColorData("87AB39","Sushi");
        colorNameDB.insertNewColorData("888387","Suva Gray");
        colorNameDB.insertNewColorData("001B1C","Swamp");
        colorNameDB.insertNewColorData("ACB78E","Swamp Green");
        colorNameDB.insertNewColorData("DCF0EA","Swans Down");
        colorNameDB.insertNewColorData("FBEA8C","Sweet Corn");
        colorNameDB.insertNewColorData("FD9FA2","Sweet Pink");
        colorNameDB.insertNewColorData("D3CDC5","Swirl");
        colorNameDB.insertNewColorData("DDD6D5","Swiss Coffee");
        colorNameDB.insertNewColorData("908D39","Sycamore");
        colorNameDB.insertNewColorData("A02712","Tabasco");
        colorNameDB.insertNewColorData("EDB381","Tacao");
        colorNameDB.insertNewColorData("D6C562","Tacha");
        colorNameDB.insertNewColorData("E97C07","Tahiti Gold");
        colorNameDB.insertNewColorData("EEF0C8","Tahuna Sands");
        colorNameDB.insertNewColorData("B32D29","Tall Poppy");
        colorNameDB.insertNewColorData("A8A589","Tallow");
        colorNameDB.insertNewColorData("991613","Tamarillo");
        colorNameDB.insertNewColorData("341515","Tamarind");
        colorNameDB.insertNewColorData("D2B48C","Tan");
        colorNameDB.insertNewColorData("FA9D5A","Tan Hide");
        colorNameDB.insertNewColorData("D9DCC1","Tana");
        colorNameDB.insertNewColorData("03163C","Tangaroa");
        colorNameDB.insertNewColorData("F28500","Tangerine");
        colorNameDB.insertNewColorData("ED7A1C","Tango");
        colorNameDB.insertNewColorData("7B7874","Tapa");
        colorNameDB.insertNewColorData("B05E81","Tapestry");
        colorNameDB.insertNewColorData("E1F6E8","Tara");
        colorNameDB.insertNewColorData("073A50","Tarawera");
        colorNameDB.insertNewColorData("CFDCCF","Tasman");
        colorNameDB.insertNewColorData("483C32","Taupe");
        colorNameDB.insertNewColorData("B3AF95","Taupe Gray");
        colorNameDB.insertNewColorData("692545","Tawny Port");
        colorNameDB.insertNewColorData("1E433C","Te Papa Green");
        colorNameDB.insertNewColorData("C1BAB0","Tea");
        colorNameDB.insertNewColorData("D0F0C0","Tea Green");
        colorNameDB.insertNewColorData("B19461","Teak");
        colorNameDB.insertNewColorData("008080","Teal");
        colorNameDB.insertNewColorData("044259","Teal Blue");
        colorNameDB.insertNewColorData("3B000B","Temptress");
        colorNameDB.insertNewColorData("CD5700","Tenn");
        colorNameDB.insertNewColorData("FFE6C7","Tequila");
        colorNameDB.insertNewColorData("E2725B","Terracotta");
        colorNameDB.insertNewColorData("F8F99C","Texas");
        colorNameDB.insertNewColorData("FFB555","Texas Rose");
        colorNameDB.insertNewColorData("B69D98","Thatch");
        colorNameDB.insertNewColorData("403D19","Thatch Green");
        colorNameDB.insertNewColorData("D8BFD8","Thistle");
        colorNameDB.insertNewColorData("CCCAA8","Thistle Green");
        colorNameDB.insertNewColorData("33292F","Thunder");
        colorNameDB.insertNewColorData("C02B18","Thunderbird");
        colorNameDB.insertNewColorData("C1440E","Tia Maria");
        colorNameDB.insertNewColorData("C3D1D1","Tiara");
        colorNameDB.insertNewColorData("063537","Tiber");
        colorNameDB.insertNewColorData("FC80A5","Tickle Me Pink");
        colorNameDB.insertNewColorData("F1FFAD","Tidal");
        colorNameDB.insertNewColorData("BFB8B0","Tide");
        colorNameDB.insertNewColorData("16322C","Timber Green");
        colorNameDB.insertNewColorData("D9D6CF","Timberwolf");
        colorNameDB.insertNewColorData("F0EEFF","Titan White");
        colorNameDB.insertNewColorData("9A6E61","Toast");
        colorNameDB.insertNewColorData("715D47","Tobacco Brown");
        colorNameDB.insertNewColorData("3A0020","Toledo");
        colorNameDB.insertNewColorData("1B0245","Tolopea");
        colorNameDB.insertNewColorData("3F583B","Tom Thumb");
        colorNameDB.insertNewColorData("E79F8C","Tonys Pink");
        colorNameDB.insertNewColorData("7C778A","Topaz");
        colorNameDB.insertNewColorData("FD0E35","Torch Red");
        colorNameDB.insertNewColorData("0F2D9E","Torea Bay");
        colorNameDB.insertNewColorData("1450AA","Tory Blue");
        colorNameDB.insertNewColorData("8D3F3F","Tosca");
        colorNameDB.insertNewColorData("991B07","Totem Pole");
        colorNameDB.insertNewColorData("A9BDBF","Tower Gray");
        colorNameDB.insertNewColorData("5FB3AC","Tradewind");
        colorNameDB.insertNewColorData("E6FFFF","Tranquil");
        colorNameDB.insertNewColorData("FFFDE8","Travertine");
        colorNameDB.insertNewColorData("FC9C1D","Tree Poppy");
        colorNameDB.insertNewColorData("3B2820","Treehouse");
        colorNameDB.insertNewColorData("7C881A","Trendy Green");
        colorNameDB.insertNewColorData("8C6495","Trendy Pink");
        colorNameDB.insertNewColorData("E64E03","Trinidad");
        colorNameDB.insertNewColorData("C3DDF9","Tropical Blue");
        colorNameDB.insertNewColorData("00755E","Tropical Rain Forest");
        colorNameDB.insertNewColorData("4A4E5A","Trout");
        colorNameDB.insertNewColorData("8A73D6","True V");
        colorNameDB.insertNewColorData("363534","Tuatara");
        colorNameDB.insertNewColorData("FFDDCD","Tuft Bush");
        colorNameDB.insertNewColorData("EAB33B","Tulip Tree");
        colorNameDB.insertNewColorData("DEA681","Tumbleweed");
        colorNameDB.insertNewColorData("353542","Tuna");
        colorNameDB.insertNewColorData("4A4244","Tundora");
        colorNameDB.insertNewColorData("FAE600","Turbo");
        colorNameDB.insertNewColorData("B57281","Turkish Rose");
        colorNameDB.insertNewColorData("CABB48","Turmeric");
        colorNameDB.insertNewColorData("30D5C8","Turquoise");
        colorNameDB.insertNewColorData("6CDAE7","Turquoise Blue");
        colorNameDB.insertNewColorData("2A380B","Turtle Green");
        colorNameDB.insertNewColorData("BD5E2E","Tuscany");
        colorNameDB.insertNewColorData("EEF3C3","Tusk");
        colorNameDB.insertNewColorData("C5994B","Tussock");
        colorNameDB.insertNewColorData("FFF1F9","Tutu");
        colorNameDB.insertNewColorData("E4CFDE","Twilight");
        colorNameDB.insertNewColorData("EEFDFF","Twilight Blue");
        colorNameDB.insertNewColorData("C2955D","Twine");
        colorNameDB.insertNewColorData("66023C","Tyrian Purple");
        colorNameDB.insertNewColorData("120A8F","Ultramarine");
        colorNameDB.insertNewColorData("D84437","Valencia");
        colorNameDB.insertNewColorData("350E42","Valentino");
        colorNameDB.insertNewColorData("2B194F","Valhalla");
        colorNameDB.insertNewColorData("49170C","Van Cleef");
        colorNameDB.insertNewColorData("D1BEA8","Vanilla");
        colorNameDB.insertNewColorData("F3D9DF","Vanilla Ice");
        colorNameDB.insertNewColorData("FFF6DF","Varden");
        colorNameDB.insertNewColorData("72010F","Venetian Red");
        colorNameDB.insertNewColorData("055989","Venice Blue");
        colorNameDB.insertNewColorData("928590","Venus");
        colorNameDB.insertNewColorData("5D5E37","Verdigris");
        colorNameDB.insertNewColorData("495400","Verdun Green");
        colorNameDB.insertNewColorData("FF4D00","Vermilion");
        colorNameDB.insertNewColorData("B14A0B","Vesuvius");
        colorNameDB.insertNewColorData("534491","Victoria");
        colorNameDB.insertNewColorData("549019","Vida Loca");
        colorNameDB.insertNewColorData("64CCDB","Viking");
        colorNameDB.insertNewColorData("983D61","Vin Rouge");
        colorNameDB.insertNewColorData("CB8FA9","Viola");
        colorNameDB.insertNewColorData("290C5E","Violent Violet");
        colorNameDB.insertNewColorData("240A40","Violet");
        colorNameDB.insertNewColorData("991199","Violet Eggplant");
        colorNameDB.insertNewColorData("F7468A","Violet Red");
        colorNameDB.insertNewColorData("40826D","Viridian");
        colorNameDB.insertNewColorData("678975","Viridian Green");
        colorNameDB.insertNewColorData("FFEFA1","Vis Vis");
        colorNameDB.insertNewColorData("8FD6B4","Vista Blue");
        colorNameDB.insertNewColorData("FCF8F7","Vista White");
        colorNameDB.insertNewColorData("FF9980","Vivid Tangerine");
        colorNameDB.insertNewColorData("803790","Vivid Violet");
        colorNameDB.insertNewColorData("533455","Voodoo");
        colorNameDB.insertNewColorData("10121D","Vulcan");
        colorNameDB.insertNewColorData("DECBC6","Wafer");
        colorNameDB.insertNewColorData("5A6E9C","Waikawa Gray");
        colorNameDB.insertNewColorData("363C0D","Waiouru");
        colorNameDB.insertNewColorData("773F1A","Walnut");
        colorNameDB.insertNewColorData("788A25","Wasabi");
        colorNameDB.insertNewColorData("A1E9DE","Water Leaf");
        colorNameDB.insertNewColorData("056F57","Watercourse");
        colorNameDB.insertNewColorData("7B7C94","Waterloo ");
        colorNameDB.insertNewColorData("DCD747","Wattle");
        colorNameDB.insertNewColorData("FFDDCF","Watusi");
        colorNameDB.insertNewColorData("FFC0A8","Wax Flower");
        colorNameDB.insertNewColorData("F7DBE6","We Peep");
        colorNameDB.insertNewColorData("FFA500","Web Orange");
        colorNameDB.insertNewColorData("4E7F9E","Wedgewood");
        colorNameDB.insertNewColorData("B43332","Well Read");
        colorNameDB.insertNewColorData("625119","West Coast");
        colorNameDB.insertNewColorData("FF910F","West Side");
        colorNameDB.insertNewColorData("DCD9D2","Westar ");
        colorNameDB.insertNewColorData("F19BAB","Wewak");
        colorNameDB.insertNewColorData("F5DEB3","Wheat");
        colorNameDB.insertNewColorData("F3EDCF","Wheatfield");
        colorNameDB.insertNewColorData("D59A6F","Whiskey");
        colorNameDB.insertNewColorData("F7F5FA","Whisper");
        colorNameDB.insertNewColorData("FFFFFF","White");
        colorNameDB.insertNewColorData("DDF9F1","White Ice");
        colorNameDB.insertNewColorData("F8F7FC","White Lilac");
        colorNameDB.insertNewColorData("F8F0E8","White Linen");
        colorNameDB.insertNewColorData("FEF8FF","White Pointer");
        colorNameDB.insertNewColorData("EAE8D4","White Rock");
        colorNameDB.insertNewColorData("7A89B8","Wild Blue Yonder");
        colorNameDB.insertNewColorData("ECE090","Wild Rice");
        colorNameDB.insertNewColorData("F4F4F4","Wild Sand");
        colorNameDB.insertNewColorData("FF3399","Wild Strawberry");
        colorNameDB.insertNewColorData("FD5B78","Wild Watermelon");
        colorNameDB.insertNewColorData("B9C46A","Wild Willow");
        colorNameDB.insertNewColorData("3A686C","William");
        colorNameDB.insertNewColorData("DFECDA","Willow Brook");
        colorNameDB.insertNewColorData("65745D","Willow Grove");
        colorNameDB.insertNewColorData("3C0878","Windsor");
        colorNameDB.insertNewColorData("591D35","Wine Berry");
        colorNameDB.insertNewColorData("D5D195","Winter Hazel");
        colorNameDB.insertNewColorData("FEF4F8","Wisp Pink");
        colorNameDB.insertNewColorData("9771B5","Wisteria");
        colorNameDB.insertNewColorData("A4A6D3","Wistful");
        colorNameDB.insertNewColorData("FFFC99","Witch Haze");
        colorNameDB.insertNewColorData("261105","Wood Bark");
        colorNameDB.insertNewColorData("4D5328","Woodland");
        colorNameDB.insertNewColorData("302A0F","Woodrush");
        colorNameDB.insertNewColorData("0C0D0F","Woodsmoke");
        colorNameDB.insertNewColorData("483131","Woody Brown");
        colorNameDB.insertNewColorData("738678","Xanadu");
        colorNameDB.insertNewColorData("FFFF00","Yellow");
        colorNameDB.insertNewColorData("C5E17A","Yellow Green");
        colorNameDB.insertNewColorData("716338","Yellow Metal");
        colorNameDB.insertNewColorData("FFAE42","Yellow Orange");
        colorNameDB.insertNewColorData("FEA904","Yellow Sea");
        colorNameDB.insertNewColorData("FFC3C0","Your Pink");
        colorNameDB.insertNewColorData("7B6608","Yukon Gold");
        colorNameDB.insertNewColorData("CEC291","Yuma");
        colorNameDB.insertNewColorData("685558","Zambezi");
        colorNameDB.insertNewColorData("DAECD6","Zanah");
        colorNameDB.insertNewColorData("E5841B","Zest");
        colorNameDB.insertNewColorData("292319","Zeus");
        colorNameDB.insertNewColorData("BFDBE2","Ziggurat");
        colorNameDB.insertNewColorData("EBC2AF","Zinnwaldite");
        colorNameDB.insertNewColorData("F4F8FF","Zircon");
        colorNameDB.insertNewColorData("E4D69B","Zombie");
        colorNameDB.insertNewColorData("A59B91","Zorba");
        colorNameDB.insertNewColorData("044022","Zuccini");
        colorNameDB.insertNewColorData("EDF6FF","Zumthor");

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

        for(int i = 0; i < allColorNameList.size(); i++)
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
        }


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
