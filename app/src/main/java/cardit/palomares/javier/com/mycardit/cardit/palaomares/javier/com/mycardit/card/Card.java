package cardit.palomares.javier.com.mycardit.cardit.palaomares.javier.com.mycardit.card;

import android.graphics.Bitmap;

/**
 * Created by javierpalomares on 11/1/15.
 */
public class Card extends Object {
    private String firstName;
    private String lastName;
    private Bitmap img;

    public String getFirstName(){
        return firstName;
    }

    public void setFirstName(String str){
        firstName = str;
    }

    public String getLastName(){
        return lastName;
    }

    public void setLastName(String str){
        lastName = str;
    }


    public Bitmap getImg(){
        return img;
    }

    public void setImg(Bitmap bitmap){
        img = bitmap;
    }


}
