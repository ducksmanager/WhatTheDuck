package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import java.util.List;

public class IssueListToUpdate {
    @Expose
    private final String publicationCode;

    @Expose
    private final List<String> issueNumbers;

    @Expose
    private final String condition;

    @Expose
    private final Integer purchaseId;

    public IssueListToUpdate(String publicationCode, List<String> issueNumbers, String condition, Integer purchaseId) {
        this.publicationCode = publicationCode;
        this.issueNumbers = issueNumbers;
        this.condition = condition;
        this.purchaseId = purchaseId;
    }
}
