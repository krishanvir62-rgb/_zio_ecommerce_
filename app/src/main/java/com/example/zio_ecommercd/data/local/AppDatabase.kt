package com.example.zio_ecommercd.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavoriteEntity::class, CartEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS cart_items (
                        productId TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        imageUrl TEXT NOT NULL,
                        category TEXT NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1
                    )""".trimIndent()
                )
            }
        }
    }
}
