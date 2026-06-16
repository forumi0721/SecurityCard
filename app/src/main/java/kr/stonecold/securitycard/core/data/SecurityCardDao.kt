package kr.stonecold.securitycard.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityCardDao {
    @Query("SELECT * FROM security_cards")
    fun getAll(): Flow<List<SecurityCardEntity>>

    @Query("SELECT * FROM security_cards WHERE id = :id")
    suspend fun getById(id: Long): SecurityCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: SecurityCardEntity): Long

    @Update
    suspend fun update(card: SecurityCardEntity)

    @Delete
    suspend fun delete(card: SecurityCardEntity)

    @Query("UPDATE security_cards SET readCount = readCount + 1, lastReadAt = :timestamp WHERE id = :id")
    suspend fun incrementReadCount(id: Long, timestamp: Long = System.currentTimeMillis())
}
