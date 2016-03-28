package cardit.palomares.javier.com.mycardit;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
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
import java.io.FileNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import cardit.palomares.javier.com.mycardit.card.Card;

public class AddContactActivity extends Activity {

    private Card myCard;
    private EditText firstName;
    private EditText lastName;
    private ImageView cardView;
    private Button snapCardButton;
    private String firstNameString;
    private String lastNameString;
    private Button addNewContactButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GET_FROM_GALLERY = 2;
    private static final int PIC_CROP = 3;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private static String TAG = "MyCardIt";
    private static int THUMBNAIL_WIDTH = 750;
    private static int THUMBNAIL_HEIGHT = 500;
    private static int SET_MY_CARD_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCard = null;
        firstNameString = null;
        lastNameString = null;
        mImageBitmap = null;
        mCurrentPhotoPath = null;
        setContentView(R.layout.activity_add_contact);
        addNewContactButton = (Button) findViewById(R.id.add_new_contact_button);
        snapCardButton = (Button) findViewById(R.id.snap_new_card_button);
        cardView = (ImageView) findViewById(R.id.new_card_view);
        firstName = (EditText) findViewById(R.id.new_card_first_name_text);
        lastName = (EditText) findViewById(R.id.new_card_last_name_text);
        snapCardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                snapCard();
            }
        });

        addNewContactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewContact();
            }
        });

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

    private void snapCard(){
        Log.d(TAG,"In Snap Card");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        String[] items = {"Capture card","Attach card"};
        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"selected" + which);
                // Use the camera to select the card
                if (which == 0)
                {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {

                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                // Upload from files
                else if (which == 1)
                {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                }
            }
        }).create().show();


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
        Log.d(TAG, "photo Path:" + mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == GET_FROM_GALLERY && resultCode == RESULT_OK) {
            Log.d(TAG, "Got a requestCode REQUEST_IMAGE_CAPTURE");
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");

        }
        else if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK)
        {
            Uri selectedImage = data.getData();

            try{
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.e(TAG,e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG,e.getMessage());
            }
        }
        if (bitmap != null)
        {
            bitmap = cropAndRotateImage(bitmap);
            cardView.setImageBitmap(bitmap);
            String savePath = savePhoto(bitmap);
            mImageBitmap = bitmap;
            Log.d(TAG,"photo saved to: " + savePath);
        }
    }

    private Bitmap cropAndRotateImage(Bitmap bitmap)
    {
        Uri picUri = null;
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 4);
            cropIntent.putExtra("aspectY", 3);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 200);
            cropIntent.putExtra("outputY", 150);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
        return bitmap;
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
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile.getAbsolutePath();
    }

    private void addNewContact(){
        Log.d(TAG,"In add New Contact");
        if (mImageBitmap != null && firstNameString != null && lastNameString != null && mCurrentPhotoPath!= null)
        {
            myCard = new Card(firstNameString,lastNameString,mImageBitmap,mCurrentPhotoPath);
            Log.d(TAG, "Made new card");
            Intent returnIntent = new Intent();
            returnIntent.putExtra("firstName",firstNameString);
            returnIntent.putExtra("lastName",lastNameString);
            returnIntent.putExtra("photoPath", mCurrentPhotoPath);
            setResult(Activity.RESULT_OK, returnIntent);
            Log.d(TAG, "Exiting add new Contact activity");
            finish();

        }else{
            String text = "Fields are missing";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
