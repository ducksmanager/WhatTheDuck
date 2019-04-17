package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.List;

public class CoverSearchResults {

    @Expose
    private HashMap<String, CoverSearchIssue> issues;

    @Expose
    private List<Integer> imageIds;

    @Expose
    private String type;

    public HashMap<String, CoverSearchIssue> getIssues() {
        return issues;
    }

    public String getType() {
        return type;
    }

}
