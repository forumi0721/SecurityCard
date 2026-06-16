package kr.stonecold.securitycard.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SecurityCardEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun securityCardDao(): SecurityCardDao
}
