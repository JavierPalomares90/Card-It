package cardit.palomares.javier.com.mycardit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cardit.palomares.javier.com.mycardit.card.Card;

import eu.janmuller.android.simplecropimage.CropImage;

public class AddContactActivity extends Activity {

    //TODO: Bug when rotating phone halfway through snapping cards
    private Card myCard;
    private EditText firstName;
    private EditText lastName;
    private ImageView cardView;
    private FloatingActionButton mFloatingActionButton;
    private String firstNameString = null;
    private String lastNameString = null;
    private Button addNewContactButton;
    private Uri selectedImageUri;
    private ProgressDialog mProgressDialog;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GET_FROM_GALLERY = 2;
    private static final int PIC_CROP = 3;
    private static int CARD_VIEW_WIDTH = 1400;
    private static int CARD_VIEW_HEIGHT = 800;
    private static int ASPECT_X = 7;
    private static int ASPECT_Y = 4;
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String FRONT_PHOTO_PATH = "frontPhotoPath";
    private static final String BACK_PHOTO_PATH = "backPhotoPath";
    private static final String SELECT_FRONT = "We'll now upload the front of the card";
    private static final String SELECT_BACK = "We'll now upload the back of the card";
    private static final String ADD_BACK_CARD = "addBackCard";
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath = null;
    private Bitmap backCardBitmap;
    private String backCardPhotoPath = null;
    private File      mFileTemp;
    private boolean isFront;
    private boolean addBackCardOnResume = false;
    private boolean showDiag = false;
    private static String TAG = "MyCardIt";

    @Override
    protected void onSaveInstanceState(Bundle outstate)
    {
        super.onSaveInstanceState(outstate);
        if(firstNameString != null)
        {
            outstate.putString(FIRST_NAME,firstNameString);
        }
        if(lastNameString != null)
        {
            outstate.putString(LAST_NAME,lastNameString);
        }
        if(mCurrentPhotoPath != null)
        {
            outstate.putString(FRONT_PHOTO_PATH,mCurrentPhotoPath);
        }
        if(backCardPhotoPath != null)
        {
            outstate.putString(BACK_PHOTO_PATH,backCardPhotoPath);
        }
        outstate.putBoolean(ADD_BACK_CARD,addBackCardOnResume);
        outstate.putBoolean("showDiag",showDiag);
        //outstate.putBoolean(IS_FRONT,isFront);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
        {
            firstNameString = savedInstanceState.getString(FIRST_NAME);
            lastNameString = savedInstanceState.getString(LAST_NAME);
            mCurrentPhotoPath = savedInstanceState.getString(FRONT_PHOTO_PATH);
            backCardPhotoPath = savedInstanceState.getString(BACK_PHOTO_PATH);
            addBackCardOnResume = savedInstanceState.getBoolean(ADD_BACK_CARD);
            showDiag = savedInstanceState.getBoolean("showDiag");
        }

        myCard = null;
        mImageBitmap = null;
        backCardBitmap = null;
        setContentView(R.layout.activity_add_contact);
        addNewContactButton = (Button) findViewById(R.id.add_new_contact_button);


        cardView = (ImageView) findViewById(R.id.new_card_view);
        cardView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_travel_black_48dp));
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstNameString != null && lastNameString != null) {
                    snapCard();
                }
                else {
                    Toast.makeText(AddContactActivity.this,"Enter the first and last name",Toast.LENGTH_SHORT).show();
                }
            }
        });

        firstName = (EditText) findViewById(R.id.new_card_first_name_text);
        lastName = (EditText) findViewById(R.id.new_card_last_name_text);

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);


        addNewContactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewContact();
            }
        });
        addNewContactButton.setClickable(false);
        addNewContactButton.setAlpha(.5f);

        firstName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                firstNameString = s.toString();
            }
        });

        lastName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                lastNameString = s.toString();
            }
        });
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(firstNameString != null)
        {
            firstName.setText(firstNameString);
        }
        if(lastNameString != null)
        {
            lastName.setText(lastNameString);
        }
        boolean isFrontCardSet = mCurrentPhotoPath != null;
        boolean isBackCardSet = backCardPhotoPath != null;
        if(isFrontCardSet)
        {
            mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        }
        if (isBackCardSet)
        {
            backCardBitmap = BitmapFactory.decodeFile(backCardPhotoPath);
        }

        if (showDiag) {
            if (firstNameString != null && lastNameString != null) {
                if (!isFrontCardSet && !isBackCardSet) {
                    snapCard();
                } else if (!isFrontCardSet)
                {
                    isFront = true;
                    if(isBackCardSet)
                    {
                        cardView.setImageBitmap(backCardBitmap);
                    }
                    addImage(SELECT_FRONT);
                }
                else if (!isBackCardSet)
                {
                    isFront = false;
                    if(isFrontCardSet)
                    {
                        cardView.setImageBitmap(mImageBitmap);
                    }
                    addImage(SELECT_BACK);
                }
            }
        }
    }

    private void snapCard(){
        Log.d(TAG,"In Snap Card");
        isFront = true;
        showDiag = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We'll set your card now!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // add front image
                        addCardFrontOrBack();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void addCardFrontOrBack()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("We'll need the front and back of the card");
        String[] items = {"Upload the front of card","Upload the back of card"};
        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(which == 0)
                {
                    isFront = true;
                    addImage(SELECT_FRONT);
                }
                else if (which == 1)
                {
                    isFront = false;
                    addImage(SELECT_BACK);
                }
            }
        }).create().show();
    }

    private void addImage(String title)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                addBackCardOnResume = false;
            }
        });
        String[] items = {"Capture card","Attach card"};
        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"selected" + which);
                // Use the camera to select the card
                if (which == 0)
                {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = null;
                    try {
                        file = createImageFile();
                    }catch (Exception e)
                    {
                        Log.e(TAG,e.getMessage());
                    }
                    // Save the full size photo
                    if (file != null) {
                        selectedImageUri = Uri.fromFile(file);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
                    }
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {

                        showDiag = false;
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                // Upload from files
                else if (which == 1)
                {
                    showDiag = false;
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                }
            }
        }).create().show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String suffix = isFront ? "front": "back";
        String imageFileName = firstNameString + "_" + lastNameString + "_" + suffix;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        if( isFront) {
            mCurrentPhotoPath = image.getAbsolutePath();
        }else{
            backCardPhotoPath = image.getAbsolutePath();
        }
        Log.d(TAG, "photo Path:" + mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri imgUri = null;
        // TODO: If save image from gallery to storage prior to rotating and cropping
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "Got a requestCode REQUEST_IMAGE_CAPTURE");
            showDiag = false;
            if (data == null)
            {
                imgUri = selectedImageUri;
            }else
            {
                imgUri = data.getData();
            }
            if (imgUri == null)
            {
                imgUri = selectedImageUri;
            }
            if (imgUri != null)
            {
                Toast.makeText(getApplicationContext(),
                        "Cropping and rotating your image", Toast.LENGTH_LONG).show();
                new CropAndRotateTask().execute(imgUri);
            }

        }
        else if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK)
        {
            Log.d(TAG, " Got a requestCode GET_FROM_GALLERY");
            showDiag = false;
            Uri img = data.getData();
            if (img == null)
            {
                img = selectedImageUri;
            }
            if (img != null)
            {
                new SaveBitmapTask(this).execute(img);
            }
        }
        else if (requestCode == PIC_CROP && resultCode == Activity.RESULT_OK)
        {
            showDiag = true;
            if (data != null) {

                String path = data.getStringExtra(CropImage.IMAGE_PATH);

                // if nothing received
                if (path == null) {

                    return;
                }

                // cropped bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());

                cardView.setImageBitmap(bitmap);
                // TODO: Save photo
                //String savePath = savePhoto(selectedBitmap);
                if(isFront) {
                    mImageBitmap = bitmap;
                    mCurrentPhotoPath = mFileTemp.getPath();
                }else{
                    backCardBitmap = bitmap;
                    backCardPhotoPath = mFileTemp.getPath();
                }
                /*
                if (mImageBitmap == null)
                {
                    isFront = true;
                    // add the front card
                    addImage(SELECT_FRONT);
                }
                else if (backCardBitmap == null)
                {
                    isFront = false;
                    addImage(SELECT_BACK);
                }
                */
                if (mImageBitmap!= null && backCardBitmap != null)
                {
                    addNewContactButton.setClickable(true);
                    addNewContactButton.setAlpha(1f);
                }
                Log.d(TAG, "photo saved to: " + mFileTemp);
            }
        }
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Handle item selection
        if (id==android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    private Uri savePhoto(Bitmap bitmapImage){
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(TAG, ex.getMessage());
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
        if( isFront) {
            mCurrentPhotoPath = photoFile.getAbsolutePath();
        }else {
            backCardPhotoPath = photoFile.getAbsolutePath();
        }
        return Uri.fromFile(photoFile);
    }

    private void addNewContact() {
        Log.d(TAG,"In add New Contact");
        if (mImageBitmap != null && firstNameString != null && lastNameString != null && mCurrentPhotoPath!= null && backCardBitmap != null && backCardPhotoPath != null)
        {
            myCard = new Card(firstNameString,lastNameString,mImageBitmap,mCurrentPhotoPath,backCardBitmap,backCardPhotoPath);
            Log.d(TAG, "Made new card");
            Intent returnIntent = new Intent();
            returnIntent.putExtra("firstName",firstNameString);
            returnIntent.putExtra("lastName",lastNameString);
            returnIntent.putExtra("photoPath", mCurrentPhotoPath);
            returnIntent.putExtra("backPhotoPath",backCardPhotoPath);
            setResult(Activity.RESULT_OK, returnIntent);
            Log.d(TAG, "Exiting add new Contact activity");
            finish();

        }else{
            String text = "Fields are missing";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private class SaveBitmapTask extends AsyncTask<Uri,Void,Uri>
    {
        private Context context;

        public SaveBitmapTask(Context context)
        {
            this.context = context;
        }

        // Save the picture in the background
        @Override
        protected Uri doInBackground(Uri... uris)
        {
            Log.d(TAG,"Saving card in background thread");
            Uri uri = uris[0];
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            }catch (Exception e)
            {
                Log.e(TAG, "unable to get bitmap");
            }
            return savePhoto(bitmap);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Saving to private storage");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Uri result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Saved the card.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(context, "Unable to save card to private storage.", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(),
                    "Cropping and rotating your image", Toast.LENGTH_LONG).show();
            new CropAndRotateTask().execute(result);
        }

    }

    private class CropAndRotateTask extends AsyncTask<Uri,Void,Intent> {


        // Save the picture in the background
        @Override
        protected Intent doInBackground(Uri... uris) {
            Log.d(TAG, "In crop and rotate");
            // create explicit intent
            Uri imgUri = uris[0];
            Intent intent = new Intent(AddContactActivity.this, CropImage.class);

            // tell CropImage activity to look for image to crop
            String filePath = imgUri.getPath();
            intent.putExtra(CropImage.IMAGE_PATH, filePath);
            mFileTemp = new File(filePath);

            // allow CropImage activity to rescale image
            intent.putExtra(CropImage.SCALE, true);

            // if the aspect ratio is fixed to ratio 7/4
            intent.putExtra(CropImage.ASPECT_X, ASPECT_X);
            intent.putExtra(CropImage.ASPECT_Y, ASPECT_Y);
            intent.putExtra(CropImage.OUTPUT_X, CARD_VIEW_WIDTH);
            intent.putExtra(CropImage.OUTPUT_Y, CARD_VIEW_HEIGHT);
            return intent;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Getting ready to crop the image");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Intent result) {
            mProgressDialog.dismiss();
            if (result != null) {
                startActivityForResult(result, PIC_CROP);
            }
        }
    }
}
