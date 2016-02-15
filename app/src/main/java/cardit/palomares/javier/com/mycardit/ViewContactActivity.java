package cardit.palomares.javier.com.mycardit;

import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cardit.palomares.javier.com.mycardit.card.Card;

public class ViewContactActivity extends Activity {
    private Card currCard;
    private static String CARD = "card";
    private EditText name;
    private ImageView cardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currCard = (Card) extras.get(CARD);
        }
        name = (EditText) findViewById(R.id.view_name);
        cardView = (ImageView) findViewById(R.id.view_card_image);
        name.setText(currCard.getFirstName() + " " + currCard.getLastName());
        cardView.setImageBitmap(currCard.getImg());
        setContentView(R.layout.activity_view_contact);
    }

}
