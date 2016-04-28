package cardit.palomares.javier.com.mycardit;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
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
        /*
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Uri firstNameUri = Uri.parse(firstName);
        Uri lastNameUri = Uri.parse(lastName);
        File frontCardFile = new File(imgFileName);
        File backCardFile = new File(backImgFileName);
        Uri frontCard = Uri.fromFile(frontCardFile);
        Uri backCard = Uri.fromFile(backCardFile);

        mFileUriCallback = new FileUriCallback();
        // Set the dynamic callback for URI requests.
        mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback,this);

        mFileUris[0] = frontCard;
        mFileUris[1] = backCard;
        */

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event)
    {
        return createCardNDef(frontPhoto,backPhoto, firstName, lastName);
    }

    private NdefMessage createCardNDef(Bitmap front, Bitmap back, String firstName, String lastName)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        front.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] frontByteArray = stream.toByteArray();
        NdefRecord frontRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,  "image/jpeg".getBytes(), null, frontByteArray);
        stream.reset();
        back.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] backByteArray = stream.toByteArray();
        NdefRecord backRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "image/jpeg".getBytes(), null, backByteArray);
        NdefRecord firstNameRecord = NdefRecord.createTextRecord(null,firstName);
        NdefRecord lastNameRecord = NdefRecord.createTextRecord(null,lastName);
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
