package com.kblack.project_base.base

import androidx.lifecycle.ViewModel
import com.kblack.project_base.base.ibase.IBaseViewModel

abstract class BaseViewModel : ViewModel(), IBaseViewModel {

    override fun onCleared() {
        super.onCleared()
        TODO("Not yet implemented")
    }
}