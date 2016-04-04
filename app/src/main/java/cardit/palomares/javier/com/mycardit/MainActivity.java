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
import android.widget.ListView;
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

public class MainActivity extends Activity {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Card myCard;
    private Card[] cards;
    private ImageView cardView;
    private Button addContactButton;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private static String TAG = "MyCardIt";
    private static int ADD_CONTACT_REQUEST = 2;
    private static int SET_MY_CARD_REQUEST = 3;
    private static String IS_MY_CARD_SET = "isMyCardSet";
    private static String FIRST_NAME = "firstName";
    private static String LAST_NAME = "lastName";
    private static String IMG_FILE_PATH = "imgFilePath";
    private static String BACK_IMG_FILE_PATH = "backImgFilePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addContactButton = (Button) findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    addContact();
                                                }
                                            });

        //SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        //while(!settings.contains(IS_MY_CARD_SET))
        //{
            setMyCard();
        //}

        cardView = (ImageView) findViewById(R.id.imageView);
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
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        String firstName = settings.getString(FIRST_NAME, FIRST_NAME);
        String lastName = settings.getString(LAST_NAME,LAST_NAME);
        String imgPath = settings.getString(IMG_FILE_PATH,getApplicationInfo().dataDir);
        String backImgPath = settings.getString(BACK_IMG_FILE_PATH,getApplicationInfo().dataDir);

        result = new Card(firstName,lastName,BitmapFactory.decodeFile(imgPath),imgPath,BitmapFactory.decodeFile(backImgPath),backImgPath);
        return result;
    }

    private void setMyCard(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("Set your card");
        // set dialog message
        alertDialogBuilder
                .setMessage("Hit enter to set your card")
                .setCancelable(false)
                .setPositiveButton("Enter",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        formMyCard();
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
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

            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(FIRST_NAME,firstName);
            editor.putString(LAST_NAME,lastName);
            editor.putString(IMG_FILE_PATH, cardImgPath);
            editor.putString(BACK_IMG_FILE_PATH,backCardImgPath);
            editor.putBoolean(IS_MY_CARD_SET, true);
            editor.commit();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

}
