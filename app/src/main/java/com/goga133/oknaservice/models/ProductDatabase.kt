package com.goga133.oknaservice.models


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class], version = 1)

abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        fun getInstance(context: Context): ProductDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ProductDatabase::class.java, "database-product"
            ).allowMainThreadQueries().build()
            // TODO: КАКОЙ МЕЙН ПОТОК, УБИРАЙ, ДУРАК
        }
    }
}

