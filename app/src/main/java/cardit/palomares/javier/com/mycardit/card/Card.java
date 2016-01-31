package cardit.palomares.javier.com.mycardit.card;

import android.graphics.Bitmap;

/**
 * Created by javierpalomares on 11/1/15.
 */
public class Card extends Object {
    private String firstName;
    private String lastName;
    private Bitmap img;
    private String imgFileName;
    private int id;

    public Card(String firstName,String lastName,Bitmap img,String imgFileName){
        this.firstName = firstName;
        this.lastName = lastName;
        this.img = img;
        this.imgFileName = imgFileName;
        this.id = 0; /**TODO: Need to find a way to set the id **/
    }
    public int getId()
    {
        return id;
    }
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

    public void setImgFileName(String str){
        imgFileName = str;
    }

    public String getImgFileName(){
        return imgFileName;
    }

}
