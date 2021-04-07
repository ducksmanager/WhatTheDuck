package net.ducksmanager.persistence.models.dm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import org.jetbrains.annotations.NotNull

@Entity(tableName = "contribution_total_points")
class ContributionTotalPoints(
    @Expose
    @PrimaryKey
    @NotNull
    var contribution: String,

    @Expose
    @ColumnInfo
    @NotNull
    var totalPoints: Int)