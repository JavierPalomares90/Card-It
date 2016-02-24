package cardit.palomares.javier.com.mycardit;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.view.View;
import android.widget.LinearLayout;

import cardit.palomares.javier.com.mycardit.card.Card;
import cardit.palomares.javier.com.mycardit.card.CardManager;

public class ViewContactActivity extends Activity {
    private Card currCard;
    private static String FIRST_NAME = "firstName";
    private static String LAST_NAME = "lastName";
    private static String IMG_FILE_NAME = "imgFileName";
    private ImageView cardView;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String firstName = extras.getString(FIRST_NAME);
            String lastName = extras.getString(LAST_NAME);
            String imgFileName = extras.getString(IMG_FILE_NAME);
            getActionBar().setTitle(firstName + " " + lastName);
            Bitmap img = loadImg(imgFileName);
            currCard = new Card(firstName,lastName,img,imgFileName);
        }
        cardView = (ImageView) findViewById(R.id.view_card_view_image);
        cardView.setImageBitmap(currCard.getImg());
        button = (Button) findViewById(R.id.delete_contact_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                deleteContact();
            }
        });
    }


    private void deleteContact()
    {
        CardManager.getInstance(this).deleteCard(currCard);
        finish();
    }

    private Bitmap loadImg(String filePath)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        return  bitmap;
    }

}
