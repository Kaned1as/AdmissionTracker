package com.adonai.admissiontracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;

/**
 * Created by adonai on 05.07.14.
 */
public abstract class AbstractShowDataFragment extends BaseFragment {

    protected class NamesAdapter extends WithZeroAdapter<Element> {

        public NamesAdapter(Context context, Elements objects) {
            super(context, objects);
        }

        @Override
        public View newView(int position, View convertView, ViewGroup parent) {
            final View view;
            final TextView text;
            final Element row = getItem(position);


            if (convertView == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.tall_list_item, parent, false);
            else
                view = convertView;

            text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(row == null ? getContext().getString(R.string.select_from_list) : extractNameForStudent(row));

            return view;
        }
    }

    protected Element findRowWithName(Elements table, String name) throws ParseException {
        for(Element row : table)
            if(extractNameForStudent(row).equals(name))
                return row;

        throw new ParseException(getString(R.string.name_not_found), table.size());
    }

    protected abstract String extractNameForStudent(Element row);
}
