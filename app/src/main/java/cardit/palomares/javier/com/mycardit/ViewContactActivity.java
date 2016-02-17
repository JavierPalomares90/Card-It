package cardit.palomares.javier.com.mycardit;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;



import cardit.palomares.javier.com.mycardit.card.Card;

public class ViewContactActivity extends Activity {
    private Card currCard;
    private static String FIRST_NAME = "firstName";
    private static String LAST_NAME = "lastName";
    private static String IMG_FILE_NAME = "imgFileName";
    private EditText name;
    private ImageView cardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String firstName = extras.getString(FIRST_NAME);
            String lastName = extras.getString(LAST_NAME);
            String imgFileName = extras.getString(IMG_FILE_NAME);
            Bitmap img = loadImg(imgFileName);
            currCard = new Card(firstName,lastName,img,imgFileName);
        }
        // name is null
        name = (EditText) findViewById(R.id.view_name);
        cardView = (ImageView) findViewById(R.id.view_card_image);
        name.setText(currCard.getFirstName() + " " + currCard.getLastName());
        //cardView.setImageBitmap(currCard.getImg());
        setContentView(R.layout.activity_view_contact);
    }

    private Bitmap loadImg(String filePath)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        return  bitmap;
    }

}
