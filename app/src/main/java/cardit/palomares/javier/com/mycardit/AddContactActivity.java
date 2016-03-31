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
import java.util.ArrayList;
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
                    File file = null;
                    try {
                        file = createImageFile();
                    }catch (Exception e)
                    {
                        Log.e(TAG,e.getMessage());
                    }
                    // Save the full size photo
                    if (file != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    }
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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        Log.d(TAG, "photo Path:" + mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri imgUri = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == GET_FROM_GALLERY && resultCode == RESULT_OK) {
            Log.d(TAG, "Got a requestCode REQUEST_IMAGE_CAPTURE");
            imgUri= data.getData();
            if (imgUri != null)
            {
                Toast.makeText(getApplicationContext(),
                        "Cropping and rotating your image", Toast.LENGTH_LONG).show();
                cropAndRotateImage(imgUri);
            }
        }else if (requestCode == PIC_CROP && resultCode == Activity.RESULT_OK)
        {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");

                cardView.setImageBitmap(selectedBitmap);
                String savePath = savePhoto(selectedBitmap);
                mImageBitmap = selectedBitmap;
                Log.d(TAG, "photo saved to: " + savePath);
            }
        }
    }

    private boolean hasImageCaptureBug() {

        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE);

    }

    private void cropAndRotateImage(Uri imgUri)
    {
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(imgUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            // Business cards have 7:4 ratio
            cropIntent.putExtra("aspectX", 7);
            cropIntent.putExtra("aspectY", 4);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 210);
            cropIntent.putExtra("outputY", 120);
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
