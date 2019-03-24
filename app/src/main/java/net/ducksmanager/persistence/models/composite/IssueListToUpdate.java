package net.ducksmanager.persistence.models.composite;

import java.util.List;

public class IssueListToUpdate {
    private String publicationCode;

    private List<String> issueNumbers;

    private String condition;

    private Integer purchaseId;

    public IssueListToUpdate(String publicationCode, List<String> issueNumbers, String condition, Integer purchaseId) {
        this.publicationCode = publicationCode;
        this.issueNumbers = issueNumbers;
        this.condition = condition;
        this.purchaseId = purchaseId;
    }

    public String getPublicationCode() {
        return publicationCode;
    }

    public void setPublicationCode(String publicationCode) {
        this.publicationCode = publicationCode;
    }

    public List<String> getIssueNumbers() {
        return issueNumbers;
    }

    public void setIssueNumbers(List<String> issueNumbers) {
        this.issueNumbers = issueNumbers;
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
