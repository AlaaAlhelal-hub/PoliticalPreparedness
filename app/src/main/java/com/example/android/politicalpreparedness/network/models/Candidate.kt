package com.example.android.politicalpreparedness.network.models

data class Candidate(
    val candidateUrl: String,
    val channels: List<Channel>,
    val email: String,
    val name: String,
    val party: String,
    val phone: String
)
