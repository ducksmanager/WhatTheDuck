package net.ducksmanager.persistence.models.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "sync")
class Sync(
    @PrimaryKey
    val timestamp: Instant
)