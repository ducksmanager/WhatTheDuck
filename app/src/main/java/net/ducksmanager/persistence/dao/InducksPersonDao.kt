package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksPerson

@Dao
interface InducksPersonDao {
    @Query("SELECT * FROM inducks_person")
    fun findAll(): List<InducksPerson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<InducksPerson>)
}