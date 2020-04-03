package com.dahham.movieprogresskeeper

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface MovieDao{
    @Query("SELECT * FROM movie")
    fun getAll(): LiveData<List<Movie>>

    @Insert
    fun put(movie: Movie)

    @Delete
    fun delete(movie: Movie)

    @Update
    fun update(movie: Movie)
}
