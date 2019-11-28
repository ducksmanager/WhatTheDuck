package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.dm.Purchase

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchases.date DESC")
    fun findAll(): LiveData<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(purchaseList: List<Purchase>)

    @Query("DELETE FROM purchases")
    fun deleteAll()
}