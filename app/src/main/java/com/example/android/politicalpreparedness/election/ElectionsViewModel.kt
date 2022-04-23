package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.example.android.politicalpreparedness.election.repository.ElectionRepository
import com.example.android.politicalpreparedness.network.Result
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch


class ElectionsViewModel(private val repository: ElectionRepository): ViewModel() {



    private var _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections


    private var _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    private val _navigateTo = MutableLiveData<NavDirections?>()
    val navigateTo: LiveData<NavDirections?>
        get() = _navigateTo

    private val _navigateToVoterInfo = MutableLiveData<Election>()
    val navigateToVoterInfo: LiveData<Election>
        get() = _navigateToVoterInfo


    private var _loadingUpcoming = MutableLiveData<Boolean>(false)
    val loadingUpcoming : LiveData<Boolean>
        get() = _loadingUpcoming

    var _loadingSaved = MutableLiveData<Boolean>(false)
    val loadingSaved : LiveData<Boolean>
        get() = _loadingSaved

    var errorMessage = MutableLiveData<String>()


    init {
        getUpcomingElections()
        getSavedElections()
    }



    private fun getUpcomingElections(){
        viewModelScope.launch {
            _loadingUpcoming.value = true
            val response = repository.getElections()
            when(response){
               is Result.Success -> {
                   _loadingUpcoming.value = false
                   response.data?.let { data ->
                       _upcomingElections.value = data.elections
                   }
               }
                is Result.Error -> {
                    _loadingUpcoming.value = false
                    errorMessage.value = response.message
                    Log.i("ELECTIONVM", "${response.message}")
                }
            }
        }
    }

    private fun getSavedElections(){
        viewModelScope.launch {
            _loadingSaved.value = true
            val response = repository.getSavedElections()
            when(response){
                is Result.Success -> {
                    _loadingSaved.value = false
                    response.data?.let { data ->
                        _savedElections.value = data
                    }
                }
                is Result.Error -> {
                    _loadingSaved.value = false
                    errorMessage.value = response.message
                    Log.i("ELECTIONVM", "${response.message}")
                }
            }
        }
    }


    fun navigateTo(election: Election) {
            _navigateTo.value = ElectionsFragmentDirections
                .actionElectionsFragmentToVoterInfoFragment(election.id, election.division)
   }

    fun navigateToVoterInfoCompleted() {
        _navigateToVoterInfo.value = null
    }
}