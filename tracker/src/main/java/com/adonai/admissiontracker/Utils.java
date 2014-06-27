package com.adonai.admissiontracker;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by adonai on 27.06.14.
 */
public class Utils {
    static final String DELIMITER = "--|--";
    static final String FAVORITES_PREF = "favorites";

    static String join(Collection<String> s, String delimiter)
    {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext())
        {
            String current = iter.next();
            if(current.equals("")) // skip empty parts
                continue;

            builder.append(current);

            if (!iter.hasNext())
                break;
            builder.append(delimiter);
        }
        return builder.toString();
    }

}
