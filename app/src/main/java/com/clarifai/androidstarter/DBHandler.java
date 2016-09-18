package com.clarifai.androidstarter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "imagesInfo";

    // Contacts table name
    private static final String TABLE_IMAGES = "images";

    // images Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SH_ADDR = "image_address";
    private static final String KEY_TAG = "tags";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_IMAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_SH_ADDR + " TEXT," + KEY_TAG + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        // Creating tables again
        onCreate(db);
    }

    // Adding new shop
    public void addShop(Shop img) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, img.getName()); // Image Name
        values.put(KEY_SH_ADDR, img.getAddress()); // Image Phone Number
        values.put(KEY_TAG, img.getTag()); // Image Phone Number

        // Inserting Row
        db.insert(TABLE_IMAGES, null, values);
        db.close(); // Closing database connection
    }

    // Getting one shop
    public Shop getShop(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_IMAGES, new String[]{KEY_ID,
                        KEY_NAME, KEY_SH_ADDR, KEY_TAG}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Shop contact = new Shop(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3));
        // return shop
        return contact;
    }

    // Getting All images
    public List<Shop> getAllImages() {
        List<Shop> shopList = new ArrayList<Shop>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_IMAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Shop shop = new Shop();
                shop.setId(Integer.parseInt(cursor.getString(0)));
                shop.setName(cursor.getString(1));
                shop.setAddress(cursor.getString(2));
                shop.setTag(cursor.getString(3));
                // Adding contact to list
                shopList.add(shop);
            } while (cursor.moveToNext());
        }

        // return contact list
        return shopList;
    }

    // Getting images Count
    public int getImagesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_IMAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // Updating a shop
    public int updateShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, shop.getName());
        values.put(KEY_SH_ADDR, shop.getAddress());
        values.put(KEY_TAG, shop.getTag());

        // updating row
        return db.update(TABLE_IMAGES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(shop.getId())});
    }

    // Deleting a shop
    public void deleteShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_IMAGES, KEY_ID + " = ?",
                new String[] { String.valueOf(shop.getId()) });
        db.close();
    }
}
