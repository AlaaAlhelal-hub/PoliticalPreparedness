package com.example.android.politicalpreparedness.network.models

data class LocalJurisdiction(
    val electionAdministrationBody: AdministrationBody,
    val name: String,
    val sources: List<Source>
)
