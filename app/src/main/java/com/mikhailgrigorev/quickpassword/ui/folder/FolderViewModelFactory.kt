package com.mikhailgrigorev.quickpassword.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FolderViewModelFactory :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderViewModel::class.java)) {
            return FolderViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}