package com.example.android.politicalpreparedness.database

import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Insert
    fun insert(election: Election)

    @Insert(onConflict= OnConflictStrategy.REPLACE)
    fun insertAll(vararg election: Election)

    @Query("SELECT * FROM election_table")
    fun getAllElections(): List<Election>

    @Query("SELECT * FROM election_table WHERE id == :id")
    fun getElectionById(id: Int): Election

    @Delete
    fun delete(election: Election)

    @Query("DELETE FROM election_table")
    fun clear(): Int

}

