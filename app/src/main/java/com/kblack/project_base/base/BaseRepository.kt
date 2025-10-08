package com.kblack.project_base.base

import android.content.Context
import com.kblack.project_base.base.network.NetworkMonitor
import com.kblack.project_base.base.network.NetworkMonitorImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.sql.SQLException

/**
 * BaseRepository provides a standardized way to handle data operations.
 * @param ioDispatcher CoroutineDispatcher for I/O operations (default: Dispatchers.IO).
 */
abstract class BaseRepository(
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val cache: MutableMap<String, Any?> = ConcurrentHashMap(),
) {

    /**
     * Executes a network call with caching and custom error handling.
     * @param call Suspend function representing the network call.
     * @param cacheKey Optional key to cache the result.
     * @param onError Optional callback to handle errors.
     * @return Flow emitting Result with Loading, Success, or Error states.
     */
    protected fun <T> executeNetworkCall(
        call: suspend () -> T,
        cacheKey: String? = null,
        onError: (Throwable) -> Unit = {},
        context: Context
    ): Flow<DataResult<T>> = flow {
        emit(DataResult.loading())

        if (!isNetworkAvailable(context)) {
            val error = IOException("No network connection")
            onError(error)
            emit(DataResult.error("No network connection"))
            return@flow
        }

        try {
            val result = cacheKey?.let { key ->
                @Suppress("UNCHECKED_CAST")
                cache[key] as? T
            } ?: run {
                val data: T = call()
                cacheKey?.let { key -> cache[key] = data }
                data
            }
            emit(DataResult.success(result))
        } catch (e: HttpException) {
            onError(e)
            emit(DataResult.error("No network connection"))
        }
    }.flowOn(ioDispatcher)

    /**
     * Executes a local database operation with detailed error handling.
     * @param call Suspend function representing the database operation.
     * @return Result with Success or Error state.
     */
    protected suspend fun <T> executeLocalCall(call: suspend () -> T): DataResult<T> {
        return try {
            withContext(ioDispatcher) {
                DataResult.success(call())
            }
        } catch (e: SQLException) {
            DataResult.error("Database error")
        } catch (e: Exception) {
            DataResult.error("Unknown error")
        }
    }

    /**
     * Wraps a Flow from a local data source with error handling.
     * @param flowCall Flow representing the local data source.
     * @return Flow emitting Result with Success or Error states.
     */
    protected fun <T> wrapLocalFlow(flowCall: Flow<T>): Flow<DataResult<T>> {
        return flowCall
            .map { DataResult.success(it) }
            .catch {}
            .flowOn(ioDispatcher)
    }

    private suspend fun isNetworkAvailable(context: Context): Boolean {
        val networkMonitor: NetworkMonitor = NetworkMonitorImpl(context)

        return networkMonitor.isNetworkConnected()
    }
}
