package cardit.palomares.javier.com.mycardit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

import cardit.palomares.javier.com.mycardit.card.Card;
import cardit.palomares.javier.com.mycardit.card.CardManager;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

public class MainActivity extends Activity {

    private static boolean isFront;
    private boolean fitToScreen;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Card myCard;
    private Card[] cards;
    private ImageView cardView;
    private FloatingActionButton mFloatingActionButton;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private static String TAG = "MyCardIt";
    private static int ADD_CONTACT_REQUEST = 2;
    private static int SET_MY_CARD_REQUEST = 3;
    private static String IS_MY_CARD_SET = "isMyCardSet";
    private static String FIRST_NAME = "";
    private static String LAST_NAME = "";
    private static String IMG_FILE_PATH = "imgFilePath";
    private static String BACK_IMG_FILE_PATH = "backImgFilePath";
    private static String MY_CARD_PREFERENCES = "MyCardPreferences";
    private static String IMG_FILE_NAME = "imgFileName";
    private static String BACK_IMG_FILE_NAME = "backImgFileName";
    private Button flipCardButton;
    private GestureDetector gestureDetector;
    private View.OnClickListener mFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           addContact();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFront = true;
        setContentView(R.layout.activity_main);

        final ImageView plusIcon = new ImageView(this);
        flipCardButton = (Button) findViewById(R.id.flip_card_button);
        flipCardButton.setVisibility(View.INVISIBLE);
        flipCardButton.setClickable(false);
        plusIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
        mFloatingActionButton = new FloatingActionButton.Builder(this).setContentView(plusIcon)
                .build();

        mFloatingActionButton.setOnClickListener(mFabClickListener);
        mFloatingActionButton.setBackground(getResources().getDrawable(R.drawable.button_action_red));

        gestureDetector = new GestureDetector(MainActivity.this,new GestureListener());
        cardView = (ImageView) findViewById(R.id.imageView);
        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        updateDrawer();
        mDrawerList.setOnItemClickListener(new CardClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    private Card getMyCard(){
        Card result = null;
        SharedPreferences settings = this.getSharedPreferences(MY_CARD_PREFERENCES,0);
        String firstName = settings.getString(FIRST_NAME, FIRST_NAME);
        String lastName = settings.getString(LAST_NAME,LAST_NAME);
        String imgPath = settings.getString(IMG_FILE_PATH,getApplicationInfo().dataDir);
        String backImgPath = settings.getString(BACK_IMG_FILE_PATH,getApplicationInfo().dataDir);
        flipCardButton.setClickable(true);
        flipCardButton.setVisibility(View.VISIBLE);
        flipCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap;
                if (isFront) {
                    bitmap = myCard.getBackCardImg();
                    flipCardButton.setBackgroundResource(R.drawable.ic_flip_to_front_black_48dp);
                } else {
                    bitmap = myCard.getFrontCardImg();
                    flipCardButton.setBackgroundResource(R.drawable.ic_flip_to_back_black_48dp);
                }
                isFront = !isFront;

                if (bitmap != null) {
                    cardView.setImageBitmap(bitmap);
                }
            }
        });

        result = new Card(firstName,lastName,BitmapFactory.decodeFile(imgPath),imgPath,BitmapFactory.decodeFile(backImgPath),backImgPath);
        return result;
    }

    private void setMyCard(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        // set dialog message
        alertDialogBuilder
                .setMessage("Welcome to AirCard, a minimalist app designed to manage your business cards of people you interact with, without cluttering your contact list or your wallet.\n We'll begin by creating your card.")
                .setCancelable(false)
                .setPositiveButton("Enter",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        formMyCard();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void formMyCard()
    {
        Log.d(TAG, "forming my Contact card");
        Intent i = new Intent(MainActivity.this,AddContactActivity.class);
        startActivityForResult(i, SET_MY_CARD_REQUEST);

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
        Arrays.sort(cards);
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
            String backCardImgPath;
            Bitmap thumbnail;
            Bitmap backThumbnail;
            Bundle extras = data.getExtras();
            if (extras == null){
                Log.d(TAG,"Extras are null. Exiting");
                return;
            }
            firstName = extras.getString("firstName");

            lastName = extras.getString("lastName");
            cardImgPath = extras.getString("photoPath");
            backCardImgPath = extras.getString("backPhotoPath");


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            thumbnail = BitmapFactory.decodeFile(cardImgPath, options);
            backThumbnail = BitmapFactory.decodeFile(backCardImgPath,options);
            Card newCard = new Card(firstName,lastName,thumbnail,cardImgPath,backThumbnail,backCardImgPath);
            CardManager.getInstance(this).addCard(newCard);
        }
        else if (requestCode == SET_MY_CARD_REQUEST && resultCode == RESULT_OK){
            Log.d(TAG,"Got a requestCode SET_MY_CARD_REQUEST");
            String firstName;
            String lastName;
            String cardImgPath;
            Bitmap thumbnail;
            String backCardImgPath;
            Bundle extras = data.getExtras();
            if (extras == null){
                Log.d(TAG,"Extras are null. Exiting");
                return;
            }
            firstName = extras.getString("firstName");

            lastName = extras.getString("lastName");
            cardImgPath = extras.getString("photoPath");
            backCardImgPath = extras.getString("backPhotoPath");

            SharedPreferences settings = this.getSharedPreferences(MY_CARD_PREFERENCES,0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(FIRST_NAME,firstName);
            editor.putString(LAST_NAME,lastName);
            editor.putString(IMG_FILE_PATH, cardImgPath);
            editor.putString(BACK_IMG_FILE_PATH,backCardImgPath);
            editor.putBoolean(IS_MY_CARD_SET, true);
            editor.commit();
            Log.d(TAG,"Commiting my card");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        SharedPreferences settings = this.getSharedPreferences(MY_CARD_PREFERENCES,0);
        if(!settings.contains(IS_MY_CARD_SET))
        {
            Log.d(TAG,"Creating card");
            setMyCard();
        }

        myCard = getMyCard();
        if (myCard != null)
        {
            String title = "My Card: " + myCard.getFirstName() + " " + myCard.getLastName();
            getActionBar().setTitle(title);
            cardView.setImageBitmap(myCard.getFrontCardImg());
        }

        updateDrawer();
        mDrawerToggle.syncState();
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
        else if (id ==R.id.transfer_my_contact_option)
        {
            transferCard();
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void transferCard()
    {
        if (myCard != null) {
            Toast.makeText(this,
                    "Transfering Card",
                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, NFCTransferActivity.class);
            i.putExtra(FIRST_NAME, myCard.getFirstName());
            i.putExtra(LAST_NAME, myCard.getLastName());
            i.putExtra(IMG_FILE_NAME, myCard.getFrontCardImgFileName());
            i.putExtra(BACK_IMG_FILE_NAME, myCard.getBackCardImgFileName());
            startActivity(i);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class CardClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //selectItem(position);
            Card currCard = cards[position];
            int idToSearch = position + 1;
            viewContactCard(currCard);
        }

        private void viewContactCard(Card card)
        {
            Intent i = new Intent(MainActivity.this,ViewContactActivity.class);
            String firstName = card.getFirstName();
            String lastName = card.getLastName();
            String imgFileName = card.getFrontCardImgFileName();
            String backImgFileName = card.getBackCardImgFileName();
            i.putExtra("firstName",firstName);
            i.putExtra("lastName",lastName);
            i.putExtra("imgFileName",imgFileName);
            i.putExtra("backImgFileName",backImgFileName);
            startActivity(i);
        }

        private void selectItem(int position) {

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            Card currCard = cards[position];
            mDrawerLayout.closeDrawer(mDrawerList);
            getActionBar().setTitle(currCard.getFirstName() + " " + currCard.getLastName());
            cardView.setImageBitmap(currCard.getFrontCardImg());
        }
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
                bitmap = myCard.getBackCardImg();
            } else {
                bitmap = myCard.getFrontCardImg();
            }
            isFront = !isFront;

            if (bitmap != null) {
                cardView.setImageBitmap(bitmap);
            }
            return true;
        }
    }

}
