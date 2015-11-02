package cardit.palomares.javier.com.mycardit;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;

import cardit.palomares.javier.com.mycardit.cardit.palaomares.javier.com.mycardit.card.Card;

public class MainActivity extends Activity {


    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private Card myCard;
    private Card[] cards;
    private EditText name;
    private ImageView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myCard = new Card("Javier", "Palomares", BitmapFactory.decodeResource(getResources(),R.drawable.android));

        name = (EditText) findViewById(R.id.name);
        name.setText(myCard.getFirstName() + " " + myCard.getLastName(), TextView.BufferType.EDITABLE);

        cardView = (ImageView) findViewById(R.id.imageView);
        cardView.setImageBitmap(myCard.getImg());
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.planets);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        cards = new Card[3];
        cards[0] = new Card("John","Doe",  Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
        cards[1] = new Card("Jane", "Doe", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
        cards[2] = new Card("James", "John", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));

        CardViewAdapter adapter = new CardViewAdapter(this, R.layout.contacts_listview_row,cards);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new CardClickListener());
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
