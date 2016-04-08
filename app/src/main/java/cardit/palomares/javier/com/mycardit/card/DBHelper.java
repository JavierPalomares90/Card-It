package cardit.palomares.javier.com.mycardit.card;

import java.util.ArrayList;
import java.util.HashMap;

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
    public static final String CARDS_COLUMN_FIRST_NAME = "firstName";
    public static final String CARDS_COLUMN_LAST_NAME = "lastName";
    public static final String CARDS_COLUMN_IMG_FILE_NAME = "imgFileName";
    public static final String CARDS_COLUMN_BACK_IMG_FILE_NAME = "backImgFileName";
    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table cards " +
                        "(id integer primary key, firstName text,lastName text,imgFileName text, backImgFileName text)"
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
        contentValues.put(CARDS_COLUMN_IMG_FILE_NAME, card.getFrontCardImgFileName());
        contentValues.put(CARDS_COLUMN_BACK_IMG_FILE_NAME,card.getBackCardImgFileName());
        db.insert(CARDS_TABLE_NAME, null, contentValues);
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
        db.update(CARDS_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }
    /**TODO: Need to think about best way to implement this **/
    public Integer deleteCard(Card card)
    {
        String imgFileName = card.getFrontCardImgFileName();
        String firstName = card.getFirstName();
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(CARDS_TABLE_NAME,
                "imgFileName = ? ",
                new String[] { imgFileName });
        return result;
    }


    public ArrayList<Card> getAllCards()
    {
        ArrayList<Card> cards = new ArrayList<Card>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from cards", null );
        res.moveToFirst();

        while (!res.isAfterLast())
        {
            String firstName = res.getString(res.getColumnIndex(CARDS_COLUMN_FIRST_NAME));
            String lastName = res.getString(res.getColumnIndex(CARDS_COLUMN_LAST_NAME));
            String filePath = res.getString(res.getColumnIndex(CARDS_COLUMN_IMG_FILE_NAME));
            String backPath = res.getString(res.getColumnIndex(CARDS_COLUMN_BACK_IMG_FILE_NAME));
            Card card = new Card(firstName,lastName,null,filePath,null,backPath);
            cards.add(card);
            res.moveToNext();
        }
        return cards;

    }
}