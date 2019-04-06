package net.ducksmanager.persistence.models.composite;

import java.util.HashMap;
import java.util.List;

public class CoverSearchResults {

    private HashMap<String, CoverSearchIssue> issues;
    private List<Integer> imageIds;
    private String type;

    public CoverSearchResults(HashMap<String, CoverSearchIssue> issues, List<Integer> imageIds) {
        this.issues = issues;
        this.imageIds = imageIds;
    }

    public HashMap<String, CoverSearchIssue> getIssues() {
        return issues;
    }

    public void setIssues(HashMap<String, CoverSearchIssue> issues) {
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
