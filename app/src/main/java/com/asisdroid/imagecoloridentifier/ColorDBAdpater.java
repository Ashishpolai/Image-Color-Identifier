package com.asisdroid.imagecoloridentifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

public class ColorDBAdpater extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "imagecoloridentifierasisdroi.db";
    static final int DATABASE_VERSION = 2;
    public static final int NAME_COLUMN = 1;
    // TODO: Create public field for each column in your table.
    // SQL Statement to create a new database.
    static final String DATABASE_CREATE = "create table " + "COLOR_NAME_WITH_CODE" +
            "( CODE text, NAME text, RGB text)";
    // Variable to hold the database instance
    public SQLiteDatabase db;
    // Context of the application using the database.
    // Database open/upgrade helper

    public ColorDBAdpater(Context _context) {
        super(_context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE COLOR_NAME_WITH_CODE ADD COLUMN RGB text DEFAULT null");
        }
        /*db.execSQL("DROP TABLE IF EXISTS COLOR_NAME_WITH_CODE");
        onCreate(db);*/
    }

    public ColorDBAdpater open() throws SQLException {
        db = this.getWritableDatabase();
        return this;
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDatabaseInstance() {
        return db;
    }

    public boolean insertNewColorData(String code, String name) {

            ContentValues newValues = new ContentValues();
            // Assign values for each row.
            newValues.put("CODE", code);
            newValues.put("NAME", name);
            // Insert the row into your table
            db.insertWithOnConflict("COLOR_NAME_WITH_CODE", null, newValues, SQLiteDatabase.CONFLICT_REPLACE);
            return true;


        ///Toast.makeText(context, "Reminder Is Successfully Saved", Toast.LENGTH_LONG).show();

    }

    public boolean insertNewColorData(String code, String name, String rgb) {

        ContentValues newValues = new ContentValues();
        // Assign values for each row.
        newValues.put("CODE", code);
        newValues.put("NAME", name);
        newValues.put("RGB", rgb);
        // Insert the row into your table
        db.insertWithOnConflict("COLOR_NAME_WITH_CODE", null, newValues, SQLiteDatabase.CONFLICT_REPLACE);
        return true;


        ///Toast.makeText(context, "Reminder Is Successfully Saved", Toast.LENGTH_LONG).show();

    }

    public int deleteAllColorData() {
        int numberOFEntriesDeleted = db.delete("COLOR_NAME_WITH_CODE", null, null);
        // Toast.makeText(context, "Number fo Entry Deleted Successfully : "+numberOFEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberOFEntriesDeleted;
    }

    public String getColorName(String colorcode) {
        String colorname;
        Cursor res = null;
      try {
          res = db.query("COLOR_NAME_WITH_CODE", new String[]{"NAME"}, "CODE = ?", new String[]{colorcode}, null, null, null);
          if (res != null && res.moveToFirst()) {
              colorname = res.getString(res.getColumnIndex("NAME"));
          } else {
              return "";
          }
      }
      finally {
          res.close();
      }
        return colorname;
    }


    public ArrayList getAllColorNames() {
        ArrayList<String> colornameList = new ArrayList<>();
        Cursor res = null;

        try {
            res = db.query("COLOR_NAME_WITH_CODE", new String[]{"NAME"}, null, null, null, null, null);
            if (res != null && res.moveToFirst()) {
                while (res.moveToNext()) {
                    colornameList.add(res.getString(res.getColumnIndex("NAME")));
                }
            }
        }
        finally{
            res.close();
        }
        return colornameList;
    }

    public ArrayList getColorNamesAlike(String colors) {
        ArrayList<String> colornameList = new ArrayList<>();
        Cursor res = null;

        try {
            res = db.query("COLOR_NAME_WITH_CODE", new String[]{"NAME"}, "NAME like ?", new String[] { "%"+colors+"%" }, null, null, null);
            if (res != null && res.moveToFirst()) {
                while (res.moveToNext()) {
                    colornameList.add(res.getString(res.getColumnIndex("NAME")));
                }
            }
        }
        finally{
            res.close();
        }
        return colornameList;
    }

    public ArrayList getAllColorCodes() {
        ArrayList<String> colorcodeList = new ArrayList<>();
        Cursor res = null;
        try {
            res = db.query("COLOR_NAME_WITH_CODE", new String[]{"CODE"}, null, null, null, null, null);
            if (res != null && res.moveToFirst()) {
                while (res.moveToNext()) {
                    colorcodeList.add(res.getString(res.getColumnIndex("CODE")));
                }
            }
        }
        finally {
            res.close();
        }
        return colorcodeList;
    }

    public ArrayList getColorCodesAlike(String colors) {
        ArrayList<String> colornameList = new ArrayList<>();
        Cursor res = null;

        try {
            res = db.query("COLOR_NAME_WITH_CODE", new String[]{"CODE"}, "NAME like ?", new String[] { "%"+colors+"%" }, null, null, null);
            if (res != null && res.moveToFirst()) {
                while (res.moveToNext()) {
                    colornameList.add(res.getString(res.getColumnIndex("CODE")));
                }
            }
        }
        finally{
            res.close();
        }
        return colornameList;
    }

    public ArrayList getAllColorRGB() {
        ArrayList<String> colorcodeList = new ArrayList<>();
        Cursor res = null;
        try {
            res = db.query("COLOR_NAME_WITH_CODE", new String[]{"RGB"}, null, null, null, null, null);
            if (res != null && res.moveToFirst()) {
                while (res.moveToNext()) {
                    colorcodeList.add(res.getString(res.getColumnIndex("RGB")));
                }
            }
        }
        finally {
            res.close();
        }
        return colorcodeList;
    }
}

