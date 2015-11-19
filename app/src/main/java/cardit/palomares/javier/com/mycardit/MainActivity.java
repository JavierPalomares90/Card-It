package cardit.palomares.javier.com.mycardit;

import android.app.Activity;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cardit.palomares.javier.com.mycardit.card.Card;
import cardit.palomares.javier.com.mycardit.card.CardManager;

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
    private static String cardsFilePath;

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

        name = (EditText) findViewById(R.id.name);
        name.setText(myCard.getFirstName() + " " + myCard.getLastName(), TextView.BufferType.EDITABLE);

        cardView = (ImageView) findViewById(R.id.imageView);
        cardView.setImageBitmap(myCard.getImg());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        cards = new Card[3];
        cards[0] = new Card("John","Doe",  Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),cardsFilePath);
        cards[1] = new Card("Jane", "Doe", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),cardsFilePath);
        cards[2] = new Card("James", "John", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),cardsFilePath);

        CardViewAdapter adapter = new CardViewAdapter(this, R.layout.contacts_listview_row,cards);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new CardClickListener());
    }
    private static int ADD_CONTACT_REQUEST = 2;

    private void addContact(){
        Intent i = new Intent(MainActivity.this,AddContactActivity.class);
        startActivityForResult(i,ADD_CONTACT_REQUEST);
    }

    private void snapCard(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  // prefix
            ".jpg",         // suffix
            storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(mImageBitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
                cardView.setImageBitmap(thumbnail);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(requestCode == ADD_CONTACT_REQUEST && resultCode == RESULT_OK){
            String firstName;
            String lastName;
            String cardImgPath;
            Bitmap thumbnail;
            Bundle extras = getIntent().getExtras();
            if (extras == null){
                firstName = null;
                lastName = null;
                cardImgPath = null;
                thumbnail = null;
            }
            firstName = extras.getString("firstName");
            lastName = extras.getString("lastName");
            cardImgPath = extras.getString("photoPath");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            thumbnail = BitmapFactory.decodeFile(cardImgPath, options);
            Card newCard = new Card(firstName,lastName,thumbnail,cardImgPath);
            CardManager.getInstance().addCard(newCard);
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
