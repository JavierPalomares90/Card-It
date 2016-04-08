package cardit.palomares.javier.com.mycardit;

import android.graphics.Bitmap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.view.MenuItem;
import android.widget.Toast;

import cardit.palomares.javier.com.mycardit.card.Card;
import cardit.palomares.javier.com.mycardit.card.CardManager;

public class ViewContactActivity extends Activity {
    private Card currCard;
    private static String FIRST_NAME = "firstName";
    private static String LAST_NAME = "lastName";
    private static String IMG_FILE_NAME = "imgFileName";
    private static String BACK_IMG_FILE_NAME = "backImgFileName";
    private ImageView cardView;
    private boolean isFront;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFront = true;
        setContentView(R.layout.activity_view_contact);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String firstName = extras.getString(FIRST_NAME);
            String lastName = extras.getString(LAST_NAME);
            String imgFileName = extras.getString(IMG_FILE_NAME);
            String backImgFileName = extras.getString(BACK_IMG_FILE_NAME);
            getActionBar().setTitle(firstName + " " + lastName);
            Bitmap img = loadImg(imgFileName);
            Bitmap backImg = loadImg(backImgFileName);
            currCard = new Card(firstName,lastName,img,imgFileName,backImg,backImgFileName);
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        cardView = (ImageView) findViewById(R.id.view_card_view_image);
        cardView.setImageBitmap(currCard.getFrontCardImg());
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text;
                Bitmap bitmap;
                if (isFront) {
                    text = "Showing back of card";
                    bitmap = currCard.getBackCardImg();
                } else {
                    text = "Showing front of card";
                    bitmap = currCard.getFrontCardImg();
                }
                isFront = !isFront;

                if (bitmap != null) {
                    Toast.makeText(v.getContext(),
                            text,
                            Toast.LENGTH_LONG).show();
                    cardView.setImageBitmap(bitmap);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.view_contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        // Handle item selection
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.delete_contact_option)
        {
            deleteContact();
            return true;
        }
        if( id == R.id.transfer_contact_option)
        {
            Toast.makeText(this,
                    "Transfering",
                    Toast.LENGTH_LONG).show();
        }
        return true;
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
