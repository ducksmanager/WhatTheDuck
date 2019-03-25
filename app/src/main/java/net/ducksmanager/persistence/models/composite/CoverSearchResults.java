package net.ducksmanager.persistence.models.composite;

import java.util.List;

public class CoverSearchResults {

    private List<CoverSearchIssue> issues;
    private List<Integer> imageIds;
    private String type;

    public CoverSearchResults(List<CoverSearchIssue> issues, List<Integer> imageIds, String type) {
        this.issues = issues;
        this.imageIds = imageIds;
        this.type = type;
    }

    public List<CoverSearchIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<CoverSearchIssue> issues) {
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
