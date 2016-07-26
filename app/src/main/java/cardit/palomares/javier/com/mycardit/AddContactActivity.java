package cardit.palomares.javier.com.mycardit;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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
import java.io.FileNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import cardit.palomares.javier.com.mycardit.card.Card;

import eu.janmuller.android.simplecropimage.CropImage;

public class AddContactActivity extends Activity {

    private Card myCard;
    private EditText firstName;
    private EditText lastName;
    private ImageView cardView;
    private Button snapCardButton;
    private String firstNameString;
    private String lastNameString;
    private Button addNewContactButton;
    private Uri selectedImageUri;
    private ProgressDialog mProgressDialog;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GET_FROM_GALLERY = 2;
    private static final int PIC_CROP = 3;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private Bitmap backCardBitmap;
    private String backCardPhotoPath;
    private File      mFileTemp;
    private boolean isFront;
    private static String TAG = "MyCardIt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCard = null;
        firstNameString = null;
        lastNameString = null;
        mImageBitmap = null;
        mCurrentPhotoPath = null;
        backCardBitmap = null;
        backCardPhotoPath = null;
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
        snapCardButton.setClickable(false);
        snapCardButton.setAlpha(.5f);

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Saving to private storage");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

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
                if (firstNameString != null && lastNameString != null)
                {
                    snapCardButton.setClickable(true);
                    snapCardButton.setAlpha(1f);
                }
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
                if (firstNameString != null && lastNameString != null)
                {
                    snapCardButton.setClickable(true);
                    snapCardButton.setAlpha(1f);
                }
            }
        });
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void snapCard(){
        Log.d(TAG,"In Snap Card");
        isFront = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We'll get the front and back of the card!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // add front image
                        addImage();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void addImage()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        String title = "";
        if (isFront){
            title = "Select the front of the card";
        }else
        {
            title = "Select the back of the card";
        }
        alertDialogBuilder.setTitle(title);
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
                cropAndRotateImage(imgUri);

            }

        }
        else if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK)
        {
            Log.d(TAG, " Got a requestCode GET_FROM_GALLERY");
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
                }else{
                    backCardBitmap = bitmap;
                }
                isFront = !isFront;
                // add back Image
                if (!isFront) {
                    addImage();
                }
                if (mImageBitmap!= null && backCardBitmap != null)
                {
                    addNewContactButton.setClickable(true);
                    addNewContactButton.setAlpha(1f);
                }
                Log.d(TAG, "photo saved to: " + mFileTemp);
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

        // create explicit intent
        Intent intent = new Intent(this, CropImage.class);

        // tell CropImage activity to look for image to crop
        String filePath = imgUri.getPath();
        intent.putExtra(CropImage.IMAGE_PATH, filePath);
        mFileTemp = new File(filePath);

        // allow CropImage activity to rescale image
        intent.putExtra(CropImage.SCALE, true);

        // if the aspect ratio is fixed to ratio 3/2
        intent.putExtra(CropImage.ASPECT_X, 7);
        intent.putExtra(CropImage.ASPECT_Y, 4);
        startActivityForResult(intent, PIC_CROP);
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
            cropAndRotateImage(result);
        }


    }

}
