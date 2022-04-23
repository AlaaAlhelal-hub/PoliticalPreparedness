package com.example.android.politicalpreparedness.election.repository


import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.Result
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionRepository(private val electionDao: ElectionDao,
                         private val retrofitService: CivicsApiService)  {


    // Get elections from Api
    suspend fun getElections(): Result<ElectionResponse>  {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                retrofitService.getElections()?.let {
                    return@let Result.Success(it)
                } ?: kotlin.run {
                    return@run Result.Error("no data")
                }
            } catch (e: Exception) {
                Result.Error("${e.message}\n${e.cause?.message.orEmpty()}")
            }
        }
    }


    // Get saved elections from database
    suspend fun getSavedElections(): Result<List<Election>> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                Result.Success(electionDao.getAllElections())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Something went wrong")
            }
        }
    }


    suspend fun getVoterInfo(map: Map<String, Any>): Result<VoterInfoResponse> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                Result.Success(retrofitService.getVoterInfo(map))
            } catch (e: Exception) {
                Result.Error("${e.message}\n${e.cause?.message.orEmpty()}")
            }
        }
    }

    suspend fun saveElection(election: Election) {
        withContext(Dispatchers.IO) {
            electionDao.insert(election)
        }
    }

    suspend fun deleteElection(election: Election) {
        withContext(Dispatchers.IO) {
            electionDao.delete(election)
        }
    }

}