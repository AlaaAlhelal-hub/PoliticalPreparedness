package com.example.android.politicalpreparedness.election

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.election.repository.ElectionRepository


class VoterInfoViewModelFactory(private val repository: ElectionRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)) {
            return VoterInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}