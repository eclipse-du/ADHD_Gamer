package com.piepie.brainhouse.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "honors")
data class Honor(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val iconResId: Int, // Resource ID for the sticker
    val isUnlocked: Boolean = false,
    val unlockCondition: String // Description of how to unlock, e.g. "Schulte Level 5"
)

@Entity(tableName = "level_records", primaryKeys = ["gameType", "levelId"])
data class LevelRecord(
    val gameType: String, // "SCHULTE" or "BLINDBOX"
    val levelId: Int,
    val stars: Int, // 0 to 3
    val bestTime: Long // Milliseconds
)

@Dao
interface HonorDao {
    @Query("SELECT * FROM honors ORDER BY id ASC")
    fun getAllHonors(): Flow<List<Honor>>

    @Query("SELECT * FROM honors WHERE isUnlocked = 1")
    fun getUnlockedHonors(): Flow<List<Honor>>

    @Update
    suspend fun updateHonor(honor: Honor)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHonors(honors: List<Honor>)
}

@Dao
interface LevelRecordDao {
    @Query("SELECT * FROM level_records")
    fun getAllRecords(): Flow<List<LevelRecord>>

    @Query("SELECT * FROM level_records WHERE gameType = :gameType")
    fun getRecordsForGame(gameType: String): Flow<List<LevelRecord>>

    @Query("SELECT * FROM level_records WHERE gameType = :gameType AND levelId = :levelId")
    suspend fun getRecord(gameType: String, levelId: Int): LevelRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LevelRecord)
}

@Database(entities = [Honor::class, LevelRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun honorDao(): HonorDao
    abstract fun levelRecordDao(): LevelRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pike_brain_house_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
