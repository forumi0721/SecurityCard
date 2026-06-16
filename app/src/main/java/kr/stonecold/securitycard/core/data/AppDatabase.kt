package kr.stonecold.securitycard.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SecurityCardEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun securityCardDao(): SecurityCardDao
}
