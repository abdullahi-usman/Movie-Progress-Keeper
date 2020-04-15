package com.dahham.movieprogresskeeper

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface MovieDao {
    @Query("SELECT * FROM movie")
    fun getAll(): LiveData<List<Movie>>

    @Query("SELECT * FROM movie")
    fun getAllMovies(): List<Movie>

    @Insert
    fun put(vararg movie: Movie)

    @Delete
    fun delete(vararg movie: Movie)

    @Update
    fun update(vararg movie: Movie)
}
