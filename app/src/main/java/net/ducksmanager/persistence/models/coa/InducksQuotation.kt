package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "inducks_quotation")
open class InducksQuotation(
    @Expose
    @PrimaryKey
    val issuecode: String,

    @Expose
    @ColumnInfo
    val estimationmin: Float,

    @Expose
    @ColumnInfo
    val estimationmax: Float?
)