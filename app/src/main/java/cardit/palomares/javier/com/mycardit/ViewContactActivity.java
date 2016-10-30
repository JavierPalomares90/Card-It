package cardit.palomares.javier.com.mycardit;

import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.MotionEvent;

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
    private GestureDetector gestureDetector;
    private boolean fitToScreen;
    private Button flipCardButton;


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
        gestureDetector = new GestureDetector(ViewContactActivity.this,new GestureListener());
        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        flipCardButton = (Button) findViewById(R.id.flip_card_button);
        flipCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap;
                if (isFront) {
                    bitmap = currCard.getBackCardImg();
                    flipCardButton.setBackgroundResource(R.drawable.ic_flip_to_front_black_48dp);
                } else {
                    bitmap = currCard.getFrontCardImg();
                    flipCardButton.setBackgroundResource(R.drawable.ic_flip_to_back_black_48dp);
                }
                isFront = !isFront;

                if (bitmap != null) {
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
            transferCard();
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

    private void transferCard()
    {
        Toast.makeText(this,
                "Transfering Card",
                Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this,NFCTransferActivity.class);
        i.putExtra(FIRST_NAME, currCard.getFirstName());
        i.putExtra(LAST_NAME, currCard.getLastName());
        i.putExtra(IMG_FILE_NAME, currCard.getFrontCardImgFileName());
        i.putExtra(BACK_IMG_FILE_NAME, currCard.getBackCardImgFileName());
        startActivity(i);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e){
            if(fitToScreen == true){
                // return to normal size

            }
            else{

            }
            fitToScreen = !fitToScreen;
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Bitmap bitmap;
            if (isFront) {
                bitmap = currCard.getBackCardImg();
            } else {
                bitmap = currCard.getFrontCardImg();
            }
            isFront = !isFront;

            if (bitmap != null) {
                cardView.setImageBitmap(bitmap);
            }
            return true;
        }
    }

}
