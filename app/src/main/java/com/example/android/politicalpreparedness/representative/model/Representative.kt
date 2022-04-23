package com.example.android.politicalpreparedness.representative.model

import com.example.android.politicalpreparedness.network.models.Office
import com.example.android.politicalpreparedness.network.models.Official

data class Representative (
        val id: String?,//add here id
        val official: Official,
        val office: Office
)