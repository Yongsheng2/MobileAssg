package com.tarc.edu.etrack.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CarViewModel : ViewModel() {
    val selectedCarsLiveData: MutableLiveData<List<String>> = MutableLiveData()
}