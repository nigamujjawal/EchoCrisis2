package com.uj.echocrisismain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SchemesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schemes.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SCHEMES = "schemes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ELIGIBILITY = "eligibility";
    public static final String COLUMN_LINK = "link";

    public SchemesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCHEMES_TABLE = "CREATE TABLE " + TABLE_SCHEMES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_ELIGIBILITY + " TEXT, "
                + COLUMN_LINK + " TEXT"
                + ")";
        db.execSQL(CREATE_SCHEMES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEMES);
        onCreate(db);
    }

    // Insert a new scheme
    public void insertScheme(String title, String description, String eligibility, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_ELIGIBILITY, eligibility);
        values.put(COLUMN_LINK, link);
        db.insert(TABLE_SCHEMES, null, values);
        db.close();
    }

    // Clear all schemes
    public void clearAllSchemes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEMES, null, null);
        db.close();
    }

    // Get all schemes as a List<SchemeModel>
    public List<SchemeModel> getAllSchemes() {
        List<SchemeModel> schemesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SCHEMES, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String eligibility = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ELIGIBILITY));
                String link = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LINK));

                schemesList.add(new SchemeModel(title, description, eligibility, link));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return schemesList;
    }
}
