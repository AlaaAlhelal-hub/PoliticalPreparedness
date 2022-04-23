package com.example.android.politicalpreparedness.network.models

import com.squareup.moshi.Json

data class State (
    val name: String,
    val electionAdministrationBody: AdministrationBody,
    @Json(name="local_jurisdiction") val localJurisdiction: LocalJurisdiction?,
    val sources: List<Source>
)