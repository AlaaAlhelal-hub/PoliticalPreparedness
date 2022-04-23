package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.election.repository.ElectionRepository
import com.example.android.politicalpreparedness.network.Result
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val repository: ElectionRepository) : ViewModel() {


    private var _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _openLinkAction = MutableLiveData<String>()
    val openLinkAction: LiveData<String>
        get() = _openLinkAction

    private var _loading = MutableLiveData<Boolean>(false)
    val loading : LiveData<Boolean>
        get() = _loading

    private var _followSuccessfully = MutableLiveData<Boolean>(false)
    val followSuccessfully : LiveData<Boolean>
        get() = _followSuccessfully

    private var _unfollowSuccessfully = MutableLiveData<Boolean>(false)
    val unfollowSuccessfully : LiveData<Boolean>
        get() = _unfollowSuccessfully



    val voterDate = Transformations.map(voterInfo) {
        it.election.electionDay
    }

    val correspondence = Transformations.map(voterInfo) {
        it.state?.firstOrNull()?.let { state ->
            state.electionAdministrationBody.correspondenceAddress?.line1
        }
    }

    val stateAddress = Transformations.map(voterInfo) {
        it.state?.firstOrNull()?.electionAdministrationBody?.correspondenceAddress?.let { address ->
            "${address.city}, ${address.state} ${address.zip}}"
        }
    }

    val stateHeader = Transformations.map(voterInfo) {
        it.state?.firstOrNull()?.name
    }

    val locationLink = Transformations.map(voterInfo) {
        it?.state?.firstOrNull()?.electionAdministrationBody?.votingLocationFinderUrl
    }

    val ballotInfoLink = Transformations.map(voterInfo) {
        it?.state?.firstOrNull()?.electionAdministrationBody?.ballotInfoUrl
    }

    /**
     * true: followed
     * false: unfollow
    **/
    private var _savedButtonState = MutableLiveData<Boolean>(false)
    val savedButtonState: LiveData<Boolean>
        get() = _savedButtonState

    var errorMessage = MutableLiveData<String>()

    fun openLink(url: String?){
        url?.let {
            _openLinkAction.value = url
        }
    }
    fun getVoterInfo(id: Int, division: Division) {
        viewModelScope.launch {
            _loading.value = true
            val request = mapOf( "electionId" to id, "address" to "${division.country} ${division.state}")
            val response = repository.getVoterInfo(request)
            when(response){
                is Result.Success -> {
                    _loading.value = false
                    response.data?.let { data ->
                        _voterInfo.value = data
                    }
                }
                is Result.Error -> {
                    _loading.value = false
                    errorMessage.value = response.message
                    Log.i("voterINfo", "${response.message}")
                }
            }
        }
    }


    private fun followVoterInfo(election: Election) {
        viewModelScope.launch {
            _followSuccessfully.value = false
            try {
                 repository.saveElection(election)
                _followSuccessfully.value = true
            }
            catch (e: Exception) {
                _followSuccessfully.value = false
                Log.i("VoterInfo", "${e.message}")
            }
        }
    }

    private fun unfollowVoterInfo(election: Election) {
        viewModelScope.launch {
            _unfollowSuccessfully.value = false
            try {
                repository.deleteElection(election)
                _unfollowSuccessfully.value = true
            }
            catch (e: Exception) {
                _unfollowSuccessfully.value = false
                Log.i("VoterInfo", "${e.message}")
            }
        }
    }



    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    fun onSaveElection() {
        viewModelScope.launch {
            voterInfo.value?.election?.let {
                if (_savedButtonState.value == true) {
                    unfollowVoterInfo(it)
                    _savedButtonState.value = false
                } else {
                    followVoterInfo(it)
                    _savedButtonState.value = true
                }
            }
        }
    }


}