package com.adonai.admissiontracker.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by adonai on 29.06.14.
 */
@DatabaseTable(tableName = "statistics")
public class Statistics {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Favorite parent;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date timestamp;

    @DatabaseField
    private Integer originalsAbove;

    @DatabaseField
    private Integer reclaimedAbove;

    @DatabaseField
    private Integer totalSubmitted;

    @DatabaseField
    private Integer reclaimedToday;

    @DatabaseField
    private Integer neededPoints;
}
