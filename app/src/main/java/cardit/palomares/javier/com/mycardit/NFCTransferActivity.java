package cardit.palomares.javier.com.mycardit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import cardit.palomares.javier.com.mycardit.card.Card;


public class NFCTransferActivity extends Activity implements CreateNdefMessageCallback {

    private static String FIRST_NAME = "firstName";
    private static String LAST_NAME = "lastName";
    private static String IMG_FILE_NAME = "imgFileName";
    private static String BACK_IMG_FILE_NAME = "backImgFileName";

    private Card currCard;
    private String firstName;
    private String lastName;
    private String imgFileName;
    private String backImgFileName;
    private Bitmap frontPhoto;
    private Bitmap backPhoto;
    NfcAdapter mNfcAdapter;
    // List of URIs to provide to Android Beam
    private Uri[] mFileUris = new Uri[2];
    private  FileUriCallback mFileUriCallback;
    private AlertDialog mAlertDialog;

    byte[] frontByteArray;
    byte[] backByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfctransfer);

        PackageManager pm = getPackageManager();

        // NFC isn't available on the device
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            /*
             * Disable NFC features here.
             * For example, disable menu items or buttons that activate
             * NFC-related features
             */
            Toast.makeText(this,
                    "Your phone does not have NFC. Cancelling...",
                    Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            firstName = extras.getString(FIRST_NAME);
            lastName = extras.getString(LAST_NAME);
            imgFileName = extras.getString(IMG_FILE_NAME);
            backImgFileName = extras.getString(BACK_IMG_FILE_NAME);
        }

        mNfcAdapter.setNdefPushMessageCallback(this,this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NFCTransferActivity.this);
        alertDialogBuilder.setTitle("Transferring card");
        // set dialog message
        alertDialogBuilder.setMessage("AirCard will now transfer the business card using NFC. Tap your phones' back to transfer.").setCancelable(false);
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                frontPhoto = BitmapFactory.decodeFile(imgFileName);
                backPhoto = BitmapFactory.decodeFile(backImgFileName);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                frontPhoto.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                frontByteArray = stream.toByteArray();
                stream.reset();
                backPhoto.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                backByteArray = stream.toByteArray();
            }
        };
        r.run();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event)
    {
        return createCardNDef(frontPhoto,backPhoto, firstName, lastName);
    }

    private NdefMessage createCardNDef(Bitmap front, Bitmap back, String firstName, String lastName)
    {
        NdefRecord frontRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,  "image/jpeg/cardit.palomares.javier.com".getBytes(), null, frontByteArray);;
        NdefRecord backRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "image/jpeg/cardit.palomares.javier.com".getBytes(), null, backByteArray);
        NdefRecord firstNameRecord = NdefRecord.createTextRecord("UTF-8",firstName);
        NdefRecord lastNameRecord = NdefRecord.createTextRecord("UTF-8",lastName);
        NdefMessage message = new NdefMessage(new NdefRecord[]{frontRecord, backRecord, firstNameRecord, lastNameRecord});
        return message;
    }

    private class FileUriCallback implements
            NfcAdapter.CreateBeamUrisCallback {
        public FileUriCallback() {
        }
        /**
         * Create content URIs as needed to share with another device
         */
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            return mFileUris;
        }
    }

}
