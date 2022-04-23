package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.getRepresentativeDeferred
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel: ViewModel() {


    private var _representatives = MutableLiveData<List<Representative>>()
    val representatives : LiveData<List<Representative>>
        get() = _representatives

    val addressInputMutableLiveData = MutableLiveData<Address>().apply {
        value = Address()
    }
    private val addressInput: LiveData<Address>
        get() = addressInputMutableLiveData


    private var _loadingData = MutableLiveData<Boolean>(false)
    val loadingData : LiveData<Boolean>
        get() = _loadingData

    var responseError = MutableLiveData<String>()


    private fun getRepresentatives(){
        viewModelScope.launch {
            _loadingData.value = true
                try {
                    Log.i("addressInput: " ,addressInput.value!!.toFormattedString())
                    val (offices, officials) = getRepresentativeDeferred(mapOf("address" to addressInput.value?.toFormattedString())).await()
                    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
                    _loadingData.value = false
                } catch (e: Exception) {
                    _loadingData.value = false
                    responseError.value = "Error while fetching data"
                    Log.e("Error fetch Data", "Error: ${e}")
                }
        }
    }

    fun getGeoLocation(address: Address) {
        addressInputMutableLiveData.value = address
        getRepresentatives()
    }

    fun getAddressLocationFromFields() {
        getRepresentatives()
    }


}
