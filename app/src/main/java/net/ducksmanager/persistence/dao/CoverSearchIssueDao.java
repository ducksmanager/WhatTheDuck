package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.composite.CoverSearchIssue;
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueDetails;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CoverSearchIssueDao {
    @Query("SELECT coversearch_issue.*, issues.* FROM coversearch_issue LEFT JOIN issues ON coversearch_issue.publicationCode = issues.country || '/' || issues.magazine AND coversearch_issue.issueNumber = issues.number")
    LiveData<List<CoverSearchIssueWithUserIssueDetails>> findAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<CoverSearchIssue> coverSearchIssues);

    @Query("DELETE FROM coversearch_issue")
    void deleteAll();
}
