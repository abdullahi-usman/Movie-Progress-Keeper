package com.dahham.movieprogresskeeper

import android.content.Context
import androidx.room.*

@Database(entities = [Movie::class], version = 2)
abstract class MovieDatabase : RoomDatabase(){
    abstract fun movieDao(): MovieDao

    companion object{

        private var database: MovieDao? = null
        fun database(context: Context): MovieDao{

            if (database == null){
                database = Room.databaseBuilder(context.applicationContext, MovieDatabase::class.java, "movies_database").allowMainThreadQueries().build().movieDao()
            }

            return database!!
        }
    }
}
