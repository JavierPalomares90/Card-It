package cardit.palomares.javier.com.mycardit;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cardit.palomares.javier.com.mycardit.card.Card;

/**
 * Created by javierpalomares on 11/1/15.
 */
public class CardViewAdapter extends ArrayAdapter<Card> {

    Context mContext;
    int layoutResourceId;
    Card data[] = null;

    public CardViewAdapter(Context mContext, int layoutResourceId, Card data[]) {

        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
        TextView textViewName = (TextView) listItem.findViewById(R.id.textViewName);

        Card folder = data[position];


        imageViewIcon.setImageBitmap(folder.getImg());
        textViewName.setText(folder.getFirstName());

        return listItem;
    }
}
