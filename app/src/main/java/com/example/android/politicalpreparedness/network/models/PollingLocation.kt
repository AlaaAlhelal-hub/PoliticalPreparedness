package com.example.android.politicalpreparedness.network.models

data class PollingLocation(
    val address: Address,
    val notes: String,
    val pollingHours: String,
    val sources: List<Source>
)