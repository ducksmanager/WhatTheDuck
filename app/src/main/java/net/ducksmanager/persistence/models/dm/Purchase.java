package net.ducksmanager.persistence.models.dm;

import com.google.gson.annotations.Expose;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchases")
public class Purchase {
    @Expose
    @PrimaryKey
    private Integer id;

    @Expose
    @ColumnInfo
    private String date;

    @Expose
    @ColumnInfo
    private String description;

    public Purchase() {}

    public Purchase(String date, String description) {
        this.date = date;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
