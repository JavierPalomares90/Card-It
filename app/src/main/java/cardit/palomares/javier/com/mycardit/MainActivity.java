package cardit.palomares.javier.com.mycardit;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

import cardit.palomares.javier.com.mycardit.card.Card;
import cardit.palomares.javier.com.mycardit.card.CardDatabase;
import cardit.palomares.javier.com.mycardit.card.CardManager;


//TODO: mImageBitmap reset on device rotation
public class MainActivity extends Activity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Card myCard;
    private Card[] cards;
    private EditText name;
    private ImageView cardView;
    private Button snapCardButton;
    private Button addContactButton;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private static String TAG = "MyCardIt";
    private static int THUMBNAIL_WIDTH = 750;
    private static int THUMBNAIL_HEIGHT = 500;
    private static String cardsFilePath = "myCardImg.png";
    private static int ADD_CONTACT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snapCardButton = (Button) findViewById(R.id.snap_card_button);
        snapCardButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 snapCard();
             }
         });

        addContactButton = (Button) findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    addContact();
                                                }
                                            });

        myCard = new Card("Javier", "Palomares", BitmapFactory.decodeResource(getResources(),R.drawable.android),cardsFilePath);
        setMyCard(myCard);
        CardManager.getInstance(this).addCard(myCard);
        name = (EditText) findViewById(R.id.name);
        name.setText(myCard.getFirstName() + " " + myCard.getLastName(), TextView.BufferType.EDITABLE);

        cardView = (ImageView) findViewById(R.id.imageView);
        cardView.setImageBitmap(myCard.getImg());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        updateDrawer();
        mDrawerList.setOnItemClickListener(new CardClickListener());

    }

    private void setMyCard(Card card){
        CardManager.getInstance(this).setMyCard(card);
    }

    private void addContact(){
        Log.d(TAG,"In add Contact");
        Intent i = new Intent(MainActivity.this,AddContactActivity.class);
        startActivityForResult(i, ADD_CONTACT_REQUEST);
    }

    private void snapCard(){
        Log.d(TAG,"In Snap Card");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File storageDir = cw.getDir("cardImageDirs", Context.MODE_PRIVATE);
        File image = File.createTempFile(
            imageFileName,  // prefix
            ".jpg",         // suffix
            storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d(TAG,"photo Path:" + mCurrentPhotoPath);
        return image;
    }

    private String savePhoto(Bitmap bitmapImage){
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.i(TAG, ex.getMessage());
        }
        // Continue only if the File was successfully created
        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(photoFile);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photoFile.getAbsolutePath();
    }

    private void updateDrawer()
    {
        ArrayList<Card> allCards = CardManager.getInstance(this).getAllCards();
        cards = allCards.toArray(new Card[allCards.size()]);
        CardViewAdapter adapter = new CardViewAdapter(this, R.layout.contacts_listview_row,cards);
        mDrawerList.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED){
            Log.d(TAG,"result canceled");
            return;
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG,"Got a requestCode REQUEST_IMAGE_CAPTURE");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            cardView.setImageBitmap(imageBitmap);
            String savePath = savePhoto(imageBitmap);
            Log.d(TAG,"photo saved to: " + savePath);
        }else if(requestCode == ADD_CONTACT_REQUEST && resultCode == RESULT_OK){
            Log.d(TAG,"Got a requestCode ADD_CONTACT_REQUEST");
            String firstName;
            String lastName;
            String cardImgPath;
            Bitmap thumbnail;
            Bundle extras = data.getExtras();
            if (extras == null){
                Log.d(TAG,"Extras are null. Exiting");
                return;
            }
            firstName = extras.getString("firstName");

            lastName = extras.getString("lastName");
            cardImgPath = extras.getString("photoPath");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            thumbnail = BitmapFactory.decodeFile(cardImgPath, options);
            Card newCard = new Card(firstName,lastName,thumbnail,cardImgPath);
            CardManager.getInstance(this).addCard(newCard);
            updateDrawer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CardClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        private void selectItem(int position) {

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            Card currCard = cards[position];
            getActionBar().setTitle(currCard.getFirstName() + " " + currCard.getLastName());
            mDrawerLayout.closeDrawer(mDrawerList);
            name.setText(currCard.getFirstName() + " " + currCard.getLastName(), TextView.BufferType.EDITABLE);
            cardView.setImageBitmap(currCard.getImg());
        }
    }

}
