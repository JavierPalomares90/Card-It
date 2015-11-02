package cardit.palomares.javier.com.mycardit.cardit.palaomares.javier.com.mycardit.card;

/**
 * Created by javierpalomares on 11/1/15.
 */
public class CardManager {

    private static CardManager instance = null;

    public static CardManager getInstance(){
        if(instance == null){
            instance = new CardManager();
        }
        return instance;
    }

    public boolean addCard(Card toAdd){
        return false;
    }

    public boolean deleteCard(Card toDelete){
        return false;
    }

    public Card getMyCard(){
        return null;
    }

    public boolean setMyCard(){
        return false;
    }

    public Card[] getAllCards(){
        return null;
    }
}
