package net.ducksmanager.persistence;

import net.ducksmanager.persistence.dao.InducksCountryDao;
import net.ducksmanager.persistence.dao.InducksIssueDao;
import net.ducksmanager.persistence.dao.InducksPublicationDao;
import net.ducksmanager.persistence.dao.IssueDao;
import net.ducksmanager.persistence.models.coa.InducksCountryName;
import net.ducksmanager.persistence.models.coa.InducksIssue;
import net.ducksmanager.persistence.models.coa.InducksPublication;
import net.ducksmanager.persistence.models.dm.IssueSimple;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {IssueSimple.class, InducksCountryName.class, InducksPublication.class, InducksIssue.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract IssueDao issueDao();
    public abstract InducksCountryDao inducksCountryDao();
    public abstract InducksPublicationDao inducksPublicationDao();
    public abstract InducksIssueDao inducksIssueDao();

}
