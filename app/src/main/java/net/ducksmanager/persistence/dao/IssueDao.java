package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.dm.IssueSimple;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<IssueSimple> issueList);
}
