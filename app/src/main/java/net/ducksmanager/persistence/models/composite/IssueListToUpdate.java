package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import java.util.List;

public class IssueListToUpdate {
    @Expose
    private String publicationCode;

    @Expose
    private List<String> issueNumbers;

    @Expose
    private String condition;

    @Expose
    private Integer purchaseId;

    public IssueListToUpdate(String publicationCode, List<String> issueNumbers, String condition, Integer purchaseId) {
        this.publicationCode = publicationCode;
        this.issueNumbers = issueNumbers;
        this.condition = condition;
        this.purchaseId = purchaseId;
    }
}
