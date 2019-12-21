package net.ducksmanager.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import net.ducksmanager.persistence.dao.*
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksIssue
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.CoverSearchIssue
import net.ducksmanager.persistence.models.composite.SuggestedIssueSimple
import net.ducksmanager.persistence.models.composite.UserMessage
import net.ducksmanager.persistence.models.composite.UserSetting
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User

@Database(entities = [
    Issue::class,
    Purchase::class,
    InducksCountryName::class,
    InducksPublication::class,
    InducksIssue::class,
    CoverSearchIssue::class,
    User::class,
    UserSetting::class,
    UserMessage::class,
    SuggestedIssueSimple::class
], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun issueDao(): IssueDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun inducksCountryDao(): InducksCountryDao
    abstract fun inducksPublicationDao(): InducksPublicationDao
    abstract fun inducksIssueDao(): InducksIssueDao
    abstract fun coverSearchIssueDao(): CoverSearchIssueDao
    abstract fun userDao(): UserDao
    abstract fun userSettingDao(): UserSettingDao
    abstract fun userMessageDao(): UserMessageDao
    abstract fun suggestedIssueDao(): SuggestedIssueDao
}