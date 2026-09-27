package com.geoalarm.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Tiny factory so plain-constructor ViewModels can take our [com.geoalarm.app.di.ServiceLocator]
 *  without reflection-based DI. */
class GenericViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
