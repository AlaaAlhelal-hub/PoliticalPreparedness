package com.example.android.politicalpreparedness.network.models

data class Contest(
    val candidates: List<Candidate>,
    val district: District,
    val level: List<String>,
    val office: String,
    val referendumSubtitle: String,
    val referendumTitle: String,
    val referendumUrl: String,
    val roles: List<String>,
    val sources: List<Source>,
    val type: String
)