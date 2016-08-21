package cardit.palomares.javier.com.mycardit.card;

import android.graphics.Bitmap;


/**
 * Created by javierpalomares on 11/1/15.
 */
public class Card extends Object implements Comparable<Card>{
    private String firstName;
    private String lastName;
    private Bitmap frontCardImg;
    private String frontCardImgFileName;
    private Bitmap backCardImg;
    private String backCardImgFileName;

    public Card(String firstName,String lastName,Bitmap frontCardImg,String frontCardImgFileName, Bitmap backCardImg, String backCardImgFileName){
        this.firstName = firstName;
        this.lastName = lastName;
        this.frontCardImg = frontCardImg;
        this.frontCardImgFileName = frontCardImgFileName;
        this.backCardImg = backCardImg;
        this.backCardImgFileName = backCardImgFileName;
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

    public Bitmap getFrontCardImg(){
        return frontCardImg;
    }

    public void setFrontCardImg(Bitmap bitmap){
        frontCardImg = bitmap;
    }

    public void setFrontCardImgFileName(String str){
        frontCardImgFileName = str;
    }

    public String getFrontCardImgFileName(){
        return frontCardImgFileName;
    }

    public Bitmap getBackCardImg()
    {
        return backCardImg;
    }

    public void setBackCardImg(Bitmap bitmap)
    {
        backCardImg = bitmap;
    }

    public String getBackCardImgFileName()
    {
        return backCardImgFileName;
    }

    public void setBackCardImgFileName(String file)
    {
        backCardImgFileName = file;
    }

    public int compareTo(Card card)
    {
        return this.firstName.compareTo(card.getFirstName());
    }

}

