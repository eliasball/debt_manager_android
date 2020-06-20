package com.eliasball.debtmanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eliasball.debtmanager.data.db.entities.Debt

@Database(
    entities = [Debt::class],
    version = 1
)
abstract class DebtDatabase : RoomDatabase() {
    abstract fun debtDao(): DebtDao

    companion object {
        @Volatile
        private var instance: DebtDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                DebtDatabase::class.java, "debt.db"
            )
                .build()

    }
}