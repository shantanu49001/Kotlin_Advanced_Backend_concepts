package com.lukaslechner.coroutineusecasesonandroid.usecases.flow.usecase3

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class FlowUseCase3ViewModel(
    stockPriceDataSource: StockPriceDataSource
) : BaseViewModel<UiState>() {


    val currentStockPriceAsLiveData: LiveData<UiState> = stockPriceDataSource.latestStockList
        .map { stockList ->
            UiState.Success(stockList) as UiState
        }
        .onStart {
            emit(UiState.Loading)
        }
        .onCompletion {
            Timber.tag("Flow").d("Flow has completed.")
        }
        .catch { throwable ->
            Timber.tag("Flow").d("Enter catch operator with $throwable")
            emit(UiState.Error("something went wrong"))
        }
        .catch {
            Timber.tag("Flow").d("Enter catch oprator with $it")
        }
        .asLiveData()
}