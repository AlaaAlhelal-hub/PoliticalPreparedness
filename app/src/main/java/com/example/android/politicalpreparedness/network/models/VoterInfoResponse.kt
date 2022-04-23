package com.example.android.politicalpreparedness.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VoterInfoResponse (
    val election: Election,
    //val pollingLocations: String? = null,
    val pollingLocations: List<PollingLocation>?,
    //val contests: String? = null,
    val contests: List<Contest>?,
    val state: List<State>? = null,
    val normalizedInput: NormalizedInput?,
    val electionElectionOfficials: List<ElectionOfficial>? = null,
    val kind: String?
)