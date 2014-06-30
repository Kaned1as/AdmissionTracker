package com.adonai.admissiontracker;

import java.text.SimpleDateFormat;

/**
 * Created by adonai on 29.06.14.
 */
public class Constants {

    // Opcodes
    public final static int GET_URL = 0;
    public final static int RELOAD_PAGE = 1;
    public final static int NETWORK_ERROR = -1;

    // URLs
    public enum Universities {
        NONE,
        SPBU("https://cabinet.spbu.ru/Lists/1k_EntryLists/", new SimpleDateFormat("dd.MM.YYYY"));

        private String url;
        private SimpleDateFormat timeFormat;

        Universities(String url, SimpleDateFormat timeFormat) {
            this.url = url;
            this.timeFormat = timeFormat;
        }

        Universities() {
        }

        public String getUrl() {
            return url;
        }

        public SimpleDateFormat getTimeFormat() {
            return timeFormat;
        }
    }
}
