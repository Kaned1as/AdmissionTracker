package com.adonai.admissiontracker.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;

/**
 * Entity representing favorite holder
 *
 * @author Adonai
 */
@DatabaseTable(tableName = "favorites")
public class Favorite {

    @DatabaseField(id = true, useGetSet = true)
    private String title;

    @DatabaseField(canBeNull = false)
    private String url;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private Integer number;

    @DatabaseField(canBeNull = false)
    private Integer parentInstitution;

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

    public String getTitle() {
        return title + " - " + name + " - " + number;
    }

    public String getTitleRaw() {
        return title;
    }

    public void setTitleRaw(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title.split(" - ")[0];
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getParentInstitution() {
        return parentInstitution;
    }

    public void setParentInstitution(Integer parentInstitution) {
        this.parentInstitution = parentInstitution;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
