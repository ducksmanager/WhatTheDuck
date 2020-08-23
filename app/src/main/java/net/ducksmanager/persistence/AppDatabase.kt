package net.ducksmanager.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.ducksmanager.persistence.dao.*
import net.ducksmanager.persistence.models.coa.*
import net.ducksmanager.persistence.models.composite.CoverSearchIssue
import net.ducksmanager.persistence.models.composite.SuggestedIssueSimple
import net.ducksmanager.persistence.models.composite.UserMessage
import net.ducksmanager.persistence.models.composite.UserSetting
import net.ducksmanager.persistence.models.converter.StringListConverter
import net.ducksmanager.persistence.models.converter.StringMutableSetConverter
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.NotificationCountry
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User

@Database(entities = [
    CoverSearchIssue::class,
    InducksCountryName::class,
    InducksIssue::class,
    InducksPerson::class,
    InducksPublication::class,
    InducksStory::class,
    Issue::class,
    NotificationCountry::class,
    Purchase::class,
    SuggestedIssueSimple::class,
    User::class,
    UserMessage::class,
    UserSetting::class
], version = 6, exportSchema = true)
@TypeConverters(StringMutableSetConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coverSearchIssueDao(): CoverSearchIssueDao
    abstract fun inducksCountryDao(): InducksCountryDao
    abstract fun inducksIssueDao(): InducksIssueDao
    abstract fun inducksPersonDao(): InducksPersonDao
    abstract fun inducksPublicationDao(): InducksPublicationDao
    abstract fun inducksStoryDao(): InducksStoryDao
    abstract fun issueDao(): IssueDao
    abstract fun notificationCountryDao(): NotificationCountryDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun suggestedIssueDao(): SuggestedIssueDao
    abstract fun userDao(): UserDao
    abstract fun userMessageDao(): UserMessageDao
    abstract fun userSettingDao(): UserSettingDao
}