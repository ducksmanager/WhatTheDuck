package net.ducksmanager.persistence.models.composite;

import java.util.List;

public class CoverSearchResults {

    private List<IssueWithFullUrl> issues;
    private List<Integer> imageIds;
    private String type;

    public CoverSearchResults(List<IssueWithFullUrl> issues, List<Integer> imageIds, String type) {
        this.issues = issues;
        this.imageIds = imageIds;
        this.type = type;
    }

    public List<IssueWithFullUrl> getIssues() {
        return issues;
    }

    public void setIssues(List<IssueWithFullUrl> issues) {
        this.issues = issues;
    }

    public List<Integer> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Integer> imageIds) {
        this.imageIds = imageIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
