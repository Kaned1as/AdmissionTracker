package com.adonai.admissiontracker.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;

/**
 * Created by adonai on 27.06.14.
 */
@DatabaseTable(tableName = "favorites")
public class Favorite {


    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String url;

    @DatabaseField
    private String name;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date lastUpdated;

    @ForeignCollectionField
    private List<Statistics> statList;

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
