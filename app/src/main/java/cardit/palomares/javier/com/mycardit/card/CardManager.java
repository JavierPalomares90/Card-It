package cardit.palomares.javier.com.mycardit.card;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by javierpalomares on 11/1/15.
 */
public class CardManager {
    private static Context context;
    private static DBHelper mydb;

    private static CardManager instance = null;

    public static CardManager getInstance(Context c){
        if(instance == null){
            context = c;
            instance = new CardManager();
            mydb = new DBHelper(c);
        }
        return instance;
    }

    public boolean addCard(Card toAdd){
        mydb.insertCard(toAdd);
        return true;
    }

    public boolean deleteCard(Card toDelete){
        mydb.deleteCard(toDelete);
        return true;
    }

    public Card getMyCard(){
        return null;
    }

    public boolean setMyCard(Card card){
        CardDatabase.getInstance(context).setMyCard(card);
        return false;
    }

    public ArrayList<Card> getAllCards()
    {
        ArrayList<Card> allCards = mydb.getAllCards();
        return allCards;
    }
}
