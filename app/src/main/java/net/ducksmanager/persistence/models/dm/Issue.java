package net.ducksmanager.persistence.models.dm;

import com.google.gson.annotations.Expose;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class Issue {
    @Expose
    @PrimaryKey
    private Integer id;

    @Expose
    @ColumnInfo
    private String country;

    @Expose
    @ColumnInfo
    private String magazine;

    @Expose
    @ColumnInfo
    private String issueNumber;

    @Expose
    @ColumnInfo
    private String condition;

    @Expose
    @ColumnInfo(name="issuePurchaseId")
    private Integer purchaseId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMagazine() {
        return magazine;
    }

    public void setMagazine(String magazine) {
        this.magazine = magazine;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
    }
}
