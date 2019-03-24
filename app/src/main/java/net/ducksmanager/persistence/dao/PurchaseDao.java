package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.dm.Purchase;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PurchaseDao {
    @Query("SELECT * FROM purchases")
    LiveData<List<Purchase>> findAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<Purchase> purchaseList);
}
