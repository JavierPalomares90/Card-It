package cardit.palomares.javier.com.mycardit.card;

import android.content.Context;

/**
 * Created by javierpalomares on 11/1/15.
 */
public class CardManager {
    private static Context context;

    private static CardManager instance = null;

    public static CardManager getInstance(Context c){
        if(instance == null){
            context = c;
            instance = new CardManager();
        }
        return instance;
    }

    public boolean addCard(Card toAdd){
        CardDatabase.getInstance(context).addCard(toAdd);
        return true;
    }

    public boolean deleteCard(Card toDelete){
        CardDatabase.getInstance(context).removeCard(toDelete);
        return true;
    }

    public Card getMyCard(){
        return null;
    }

    public boolean setMyCard(Card card){
        CardDatabase.getInstance(context).setMyCard(card);
        return false;
    }

    public Card[] getAllCards(){
        return null;
    }
}
