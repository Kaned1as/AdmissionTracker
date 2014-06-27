package com.adonai.admissiontracker;

import java.net.URL;

/**
 * Created by adonai on 27.06.14.
 */
final class PageAddresses {
    public final static URL SPBU;

    static {
        try {
            SPBU = new URL("https://cabinet.spbu.ru/Lists/1k_EntryLists/");
        } catch (Exception e) {
            throw new RuntimeException(e); // should never happen
        }
    }
}
