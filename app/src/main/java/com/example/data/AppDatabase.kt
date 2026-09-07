package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.MessageDao
import com.example.data.dao.SessionDao
import com.example.data.dao.UserDao
import com.example.data.model.AnonymousMessageEntity
import com.example.data.model.SessionLogEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SessionLogEntity::class,
        AnonymousMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anonymous_messages_db"
                ).fallbackToDestructiveMigration(false)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
