package net.ducksmanager.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.ducksmanager.persistence.dao.*
import net.ducksmanager.persistence.models.appfollow.AppVersion
import net.ducksmanager.persistence.models.coa.*
import net.ducksmanager.persistence.models.composite.*
import net.ducksmanager.persistence.models.converter.InstantConverter
import net.ducksmanager.persistence.models.converter.StringIntMapConverter
import net.ducksmanager.persistence.models.converter.StringMutableSetConverter
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.NotificationCountry
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User
import net.ducksmanager.persistence.models.internal.Sync


@Database(entities = [
    AppVersion::class,
    CoverSearchIssue::class,
    InducksCountryName::class,
    InducksIssue::class,
    InducksIssueCount::class,
    InducksPerson::class,
    InducksPublication::class,
    InducksStory::class,
    Issue::class,
    NotificationCountry::class,
    Purchase::class,
    SuggestedIssueSimple::class,
    SuggestedIssueByReleaseDateSimple::class,
    Sync::class,
    User::class,
    UserMessage::class,
    UserSetting::class
 ], version = 11, exportSchema = true)
@TypeConverters(StringMutableSetConverter::class, StringIntMapConverter::class, InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("create table app_version(version text not null constraint app_version_pk primary key)")
            }
        }
        val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS inducks_issue_count")
                database.execSQL("CREATE TABLE inducks_issue_count(`code` TEXT NOT NULL, `count` INTEGER NOT NULL, PRIMARY KEY(`code`))")
            }
        }
        val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE suggested_issues_by_release_date(`publicationCode` TEXT NOT NULL, `issueNumber` TEXT NOT NULL, `suggestionScore` INTEGER NOT NULL, `oldestdate` TEXT, `stories` TEXT NOT NULL, PRIMARY KEY(`publicationCode`, `issueNumber`))")
            }
        }
        val MIGRATION_10_11: Migration = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE coversearch_issue ADD `quotation` TEXT DEFAULT null")
            }
        }
    }

    abstract fun appVersionDao(): AppVersionDao
    abstract fun coverSearchIssueDao(): CoverSearchIssueDao
    abstract fun inducksCountryDao(): InducksCountryDao
    abstract fun inducksIssueDao(): InducksIssueDao
    abstract fun inducksIssueCountDao(): InducksIssueCountDao
    abstract fun inducksPersonDao(): InducksPersonDao
    abstract fun inducksPublicationDao(): InducksPublicationDao
    abstract fun inducksStoryDao(): InducksStoryDao
    abstract fun issueDao(): IssueDao
    abstract fun notificationCountryDao(): NotificationCountryDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun suggestedIssueDao(): SuggestedIssueDao
    abstract fun suggestedIssueByReleaseDateDao(): SuggestedIssueByReleaseDateDao
    abstract fun syncDao(): SyncDao
    abstract fun userDao(): UserDao
    abstract fun userMessageDao(): UserMessageDao
    abstract fun userSettingDao(): UserSettingDao
}