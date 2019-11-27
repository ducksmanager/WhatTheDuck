package net.ducksmanager.persistence;

import net.ducksmanager.persistence.dao.CoverSearchIssueDao;
import net.ducksmanager.persistence.dao.InducksCountryDao;
import net.ducksmanager.persistence.dao.InducksIssueDao;
import net.ducksmanager.persistence.dao.InducksPublicationDao;
import net.ducksmanager.persistence.dao.IssueDao;
import net.ducksmanager.persistence.dao.PurchaseDao;
import net.ducksmanager.persistence.dao.UserDao;
import net.ducksmanager.persistence.dao.UserMessageDao;
import net.ducksmanager.persistence.dao.UserSettingDao;
import net.ducksmanager.persistence.models.coa.InducksCountryName;
import net.ducksmanager.persistence.models.coa.InducksIssue;
import net.ducksmanager.persistence.models.coa.InducksPublication;
import net.ducksmanager.persistence.models.composite.CoverSearchIssue;
import net.ducksmanager.persistence.models.composite.UserMessage;
import net.ducksmanager.persistence.models.composite.UserSetting;
import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.persistence.models.dm.Purchase;
import net.ducksmanager.persistence.models.dm.User;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {
    Issue.class, Purchase.class,
    InducksCountryName.class, InducksPublication.class, InducksIssue.class,
    CoverSearchIssue.class,
    User.class, UserSetting.class, UserMessage.class
}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    public abstract IssueDao issueDao();
    public abstract PurchaseDao purchaseDao();

    public abstract InducksCountryDao inducksCountryDao();
    public abstract InducksPublicationDao inducksPublicationDao();
    public abstract InducksIssueDao inducksIssueDao();

    public abstract CoverSearchIssueDao coverSearchIssueDao();

    public abstract UserDao userDao();
    public abstract UserSettingDao userSettingDao();
    public abstract UserMessageDao userMessageDao();

}
