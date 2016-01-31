package cardit.palomares.javier.com.mycardit.card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Javier on 30-Jan-16.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyCardsDB.db";
    public static final String CARDS_TABLE_NAME = "cards";
    public static final String CARDS_COLUMN_ID = "id";
    public static final String CARDS_COLUMN_FIRST_NAME = "firstName";
    public static final String CARDS_COLUMN_LAST_NAME = "lastName";
    public static final String CARDS_COLUMN_IMG_FILE_NAME = "imgFileName";
    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table contacts " +
                        "(id integer primary key, firstName text,lastNmae text,imgFileName text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertCard(Card card)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CARDS_COLUMN_FIRST_NAME,card.getFirstName());
        contentValues.put(CARDS_COLUMN_LAST_NAME,card.getLastName());
        contentValues.put(CARDS_COLUMN_IMG_FILE_NAME, card.getImgFileName());
        db.insert(DATABASE_NAME, null, contentValues);
        return true;
    }

    public boolean insertContact (String name, String phone, String email, String street,String place)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.insert("contacts", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CARDS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String phone, String email, String street,String place)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteCard(Card card)
    {
        int id = card.getId();
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DATABASE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Integer deleteContact (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<Card> getAllCards()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from cards", null );
        res.moveToFirst();

        while (!res.isAfterLast())
        {
            array_list.add(res.getString(res.getColumnIndex(CARDS_COLUMN_FIRST_NAME)));
            res.moveToNext();
        }
        return array_list;

    }

    public ArrayList<String> getAllCotacts()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(CARDS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }
}