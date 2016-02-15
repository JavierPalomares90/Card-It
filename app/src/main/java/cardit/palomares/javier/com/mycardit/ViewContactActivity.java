package cardit.palomares.javier.com.mycardit;

import android.os.Bundle;
import android.app.Activity;
import cardit.palomares.javier.com.mycardit.card.Card;

public class ViewContactActivity extends Activity {
    private Card currCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
    }

}
