package cardit.palomares.javier.com.mycardit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.app.Activity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.text.TextUtils;
import android.provider.MediaStore;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import cardit.palomares.javier.com.mycardit.card.Card;
import cardit.palomares.javier.com.mycardit.card.CardManager;

public class NFCTransferListenerActivity extends Activity {
    private static final String TAG = "NFCTransfer";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfctransfer_listener);

        Intent intent = getIntent();
        if(intent.getType() != null && intent.getType().equals("image/jpeg/cardit.palomares.javier.com")) {
            // Read the first record which contains the NFC data
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefRecord frontCardRecord = ((NdefMessage)rawMsgs[0]).getRecords()[0];
            NdefRecord backCardRecord = ((NdefMessage)rawMsgs[0]).getRecords()[1];
            NdefRecord firstNameRecord = ((NdefMessage)rawMsgs[0]).getRecords()[2];
            NdefRecord lastNameRecord = ((NdefMessage)rawMsgs[0]).getRecords()[3];

            byte[] firstNameBytes = firstNameRecord.getPayload();
            byte[] lastNameBytes = lastNameRecord.getPayload();

            // Get the Text Encoding
            String textEncoding = ((firstNameBytes[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = firstNameBytes[0] & 0063;
            // bug in encoding of first name
            String firstName = null;
            String lastName = null;
            try {
                firstName = new String(firstNameBytes, languageCodeLength + textEncoding.length(), firstNameBytes.length - languageCodeLength - textEncoding.length(), textEncoding);
                lastName = new String(lastNameBytes, languageCodeLength + textEncoding.length(), lastNameBytes.length - languageCodeLength - textEncoding.length(), textEncoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // NPEing here
            Bitmap frontCard = BitmapFactory.decodeByteArray(frontCardRecord.getPayload(),0,frontCardRecord.getPayload().length);
            Bitmap backCard = BitmapFactory.decodeByteArray(backCardRecord.getPayload(),0,backCardRecord.getPayload().length);

            String cardImgPath = savePhoto(frontCard, firstName, lastName, true);
            String backCardImgPath = savePhoto(backCard,firstName, lastName, false);
            Toast.makeText(this,"Adding the card for " + firstName + lastName, Toast.LENGTH_LONG).show();

            Card newCard = new Card(firstName,lastName,frontCard,cardImgPath,backCard,backCardImgPath);
            CardManager.getInstance(this).addCard(newCard);

            // Just finish the activity
            finish();
        }
    }

    private String savePhoto(Bitmap bitmapImage, String firstName, String lastName, boolean isFront){
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile(firstName, lastName,isFront);
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
        return photoFile.getAbsolutePath();
    }

    private File createImageFile(String firstNameString, String lastNameString, boolean isFront) throws IOException {
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
        return image;
    }

}
