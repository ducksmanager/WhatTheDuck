package net.ducksmanager.persistence.models.dm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import org.jetbrains.annotations.NotNull

@Entity(tableName = "issues")
class Issue {
    @Expose
    @PrimaryKey
    var id: Int? = null

    @Expose
    @ColumnInfo
    @NotNull
    lateinit var country: String

    @Expose
    @ColumnInfo
    @NotNull
    lateinit var magazine: String

    @Expose
    @ColumnInfo
    @NotNull
    lateinit var issueNumber: String

    @Expose
    @ColumnInfo
    @NotNull
    lateinit var condition: String

    @Expose
    @ColumnInfo
    var creationDate: String? = null

    @Expose
    @ColumnInfo(name = "issuePurchaseId")
    var purchaseId: Int? = null

}