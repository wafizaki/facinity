package com.example.takephoto;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class PhotoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "photos.db";
    private static final int DB_VERSION = 1;

    public PhotoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos (id INTEGER PRIMARY KEY AUTOINCREMENT, image BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS photos");
        onCreate(db);
    }

    public void insertPhoto(Bitmap bitmap) {
        SQLiteDatabase db = getWritableDatabase();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        ContentValues values = new ContentValues();
        values.put("image", byteArray);
        db.insert("photos", null, values);
    }

    public ArrayList<Bitmap> getAllPhotos() {
        ArrayList<Bitmap> photos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT image FROM photos", null);
        if (cursor.moveToFirst()) {
            do {
                byte[] imageBytes = cursor.getBlob(0);
                photos.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return photos;
    }
}
