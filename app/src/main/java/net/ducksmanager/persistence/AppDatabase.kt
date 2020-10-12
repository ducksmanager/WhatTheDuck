package net.ducksmanager.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.ducksmanager.persistence.dao.*
import net.ducksmanager.persistence.models.coa.*
import net.ducksmanager.persistence.models.composite.*
import net.ducksmanager.persistence.models.converter.InstantConverter
import net.ducksmanager.persistence.models.converter.StringMutableSetConverter
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.NotificationCountry
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User
import net.ducksmanager.persistence.models.internal.Sync

@Database(entities = [
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
    Sync::class,
    User::class,
    UserMessage::class,
    UserSetting::class
], version = 7, exportSchema = true)
@TypeConverters(StringMutableSetConverter::class, InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
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
    abstract fun syncDao(): SyncDao
    abstract fun userDao(): UserDao
    abstract fun userMessageDao(): UserMessageDao
    abstract fun userSettingDao(): UserSettingDao
}