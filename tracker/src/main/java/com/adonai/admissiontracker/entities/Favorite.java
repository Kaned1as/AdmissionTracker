package com.adonai.admissiontracker.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

/**
 * Entity representing favorite holder
 * These entries are ones that do not change over time
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
    private Integer parentInstitution;

    @DatabaseField
    private Integer maxBudgetCount;

    @DatabaseField
    private Integer priority;

    @DatabaseField
    private Integer points;

    @ForeignCollectionField
    private Collection<Statistics> statList;

    public Favorite() {

    }

    public Favorite(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title + " - " + name;
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

    public Integer getParentInstitution() {
        return parentInstitution;
    }

    public void setParentInstitution(Integer parentInstitution) {
        this.parentInstitution = parentInstitution;
    }

    public Integer getMaxBudgetCount() {
        return maxBudgetCount;
    }

    public void setMaxBudgetCount(Integer maxBudgetCount) {
        this.maxBudgetCount = maxBudgetCount;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
