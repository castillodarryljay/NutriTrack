package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.NutriDatabase
import com.example.data.repository.NutriRepository

class NutriTrackApp : Application() {

    lateinit var database: NutriDatabase
        private set

    lateinit var repository: NutriRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            NutriDatabase::class.java,
            "nutritrack_db"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = NutriRepository(database)
    }
}
