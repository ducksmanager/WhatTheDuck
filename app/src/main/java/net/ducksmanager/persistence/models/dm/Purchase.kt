package net.ducksmanager.persistence.models.dm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "purchases")
open class Purchase {
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "purchaseId")
    var id: Int? = null

    @Expose
    @ColumnInfo
    var date: String? = null
        private set

    @Expose
    @ColumnInfo
    var description: String? = null
        private set

    @Ignore
    constructor()

    constructor(date: String?, description: String?) {
        this.date = date
        this.description = description
    }
}