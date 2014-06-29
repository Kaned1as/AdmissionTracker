package com.adonai.admissiontracker.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;

/**
 * Created by adonai on 27.06.14.
 */
@DatabaseTable(tableName = "favorites")
public class Favorite {

    @DatabaseField(id = true)
    private String title;

    @DatabaseField(canBeNull = false)
    private String url;

    @DatabaseField
    private String name;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date lastUpdated;

    @ForeignCollectionField
    private Collection<Statistics> statList;

    public Favorite() {

    }

    public Favorite(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @Override
    public String toString() {
        return title;
    }
}
