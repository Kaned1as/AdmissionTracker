package com.adonai.admissiontracker.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Entity representing statistics
 * These entries change over time
 *
 * Created by adonai on 29.06.14.
 */
@DatabaseTable(tableName = "statistics")
public class Statistics {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private Favorite parent;

    @DatabaseField(dataType = DataType.DATE_LONG, uniqueCombo = true)
    private Date timestamp;

    @DatabaseField
    private Integer originalsAbove;

    @DatabaseField
    private Integer copiesAbove;

    @DatabaseField
    private Integer reclaimedAbove;

    @DatabaseField
    private Integer totalSubmitted;

    @DatabaseField
    private Integer reclaimedToday;

    @DatabaseField
    private Integer neededPoints;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Favorite getParent() {
        return parent;
    }

    public void setParent(Favorite parent) {
        this.parent = parent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getOriginalsAbove() {
        return originalsAbove;
    }

    public void setOriginalsAbove(Integer originalsAbove) {
        this.originalsAbove = originalsAbove;
    }

    public Integer getReclaimedAbove() {
        return reclaimedAbove;
    }

    public void setReclaimedAbove(Integer reclaimedAbove) {
        this.reclaimedAbove = reclaimedAbove;
    }

    public Integer getTotalSubmitted() {
        return totalSubmitted;
    }

    public void setTotalSubmitted(Integer totalSubmitted) {
        this.totalSubmitted = totalSubmitted;
    }

    public Integer getReclaimedToday() {
        return reclaimedToday;
    }

    public void setReclaimedToday(Integer reclaimedToday) {
        this.reclaimedToday = reclaimedToday;
    }

    public Integer getNeededPoints() {
        return neededPoints;
    }

    public void setNeededPoints(Integer neededPoints) {
        this.neededPoints = neededPoints;
    }

    public Integer getCopiesAbove() {
        return copiesAbove;
    }

    public void setCopiesAbove(Integer copiesAbove) {
        this.copiesAbove = copiesAbove;
    }
}
