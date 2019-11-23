package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.HashSet;

public class SuggestionList {
    @Expose
    private final Integer minScore;

    @Expose
    private final Integer maxScore;

    @Expose
    private final HashMap<String, SuggestedIssue> issues;

    @Expose
    private final HashMap<String, String> authors;

    @Expose
    private final HashMap<String, String> publicationTitles;

    @Expose
    private final HashMap<String, SuggestedStory> storyDetails;

    public SuggestionList(Integer minScore, Integer maxScore, HashMap<String, SuggestedIssue> issues, HashMap<String, String> authors, HashMap<String, String> publicationTitles, HashMap<String, SuggestedStory> storyDetails) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.issues = issues;
        this.authors = authors;
        this.publicationTitles = publicationTitles;
        this.storyDetails = storyDetails;
    }

    public HashMap<String, SuggestedIssue> getIssues() {
        return issues;
    }

    public HashMap<String, String> getPublicationTitles() {
        return publicationTitles;
    }

    public HashMap<String, String> getAuthors() {
        return authors;
    }

    public HashMap<String, SuggestedStory> getStoryDetails() {
        return storyDetails;
    }

    public class SuggestedStory {
        @Expose
        private final String title;

        @Expose
        private final String personcode;

        @Expose
        private final String storycomment;

        private SuggestedStory(String title, String personcode, String storycomment) {
            this.title = title;
            this.personcode = personcode;
            this.storycomment = storycomment;
        }

        public String getTitle() {
            return title;
        }
    }

    public class SuggestedIssue {
        @Expose
        private final HashMap<String, HashSet<String>> stories;

        @Expose
        private final Integer score;

        @Expose
        private final String publicationcode;

        @Expose
        private final String oldestdate;

        @Expose
        private final String issuenumber;

        private SuggestedIssue(HashMap<String, HashSet<String>> stories, Integer score, String publicationcode, String oldestdate, String issuenumber) {
            this.stories = stories;
            this.score = score;
            this.publicationcode = publicationcode;
            this.oldestdate = oldestdate;
            this.issuenumber = issuenumber;
        }

        public HashMap<String, HashSet<String>> getStories() {
            return stories;
        }

        public Integer getScore() {
            return score;
        }

        public String getPublicationcode() {
            return publicationcode;
        }

        public String getIssuenumber() {
            return issuenumber;
        }

        public String getOldestdate() {
            return oldestdate;
        }
    }
}
