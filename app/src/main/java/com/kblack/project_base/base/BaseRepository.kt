package com.kblack.project_base.base

import com.kblack.project_base.base.network.NetworkMonitor
import com.kblack.project_base.utils.DataResult
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
    private val cache: MutableMap<String, Any?> = HashMap(), // Simple in-memory cache HashMap, LinkedHashMap for LRU (Least Recently Used) cache
    private val networkMonitor: NetworkMonitor,
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
    ): Flow<DataResult<T>> = flow {
        emit(DataResult.loading())

        if (!networkMonitor.isNetworkConnected()) {
            val error = IOException("[executeNetworkCall] No network connection")
            onError(error)
            emit(DataResult.error(error.message ?: "No network"))
            return@flow
        }

        try {
            val result = getOrFetchFromCache(cacheKey, call)
            emit(DataResult.success(result))
        } catch (e: HttpException) {
            onError(e)
            emit(DataResult.error(e.message ?: "HTTP Error"))
        } catch (e: Exception) {
            onError(e)
            emit(DataResult.error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    /**
     * Executes a local database operation with detailed error handling.
     * @param call Suspend function representing the database operation.
     * @return Result with Success or Error state.
     */
    protected suspend fun <T> executeLocalCall(call: suspend () -> T): DataResult<T> =
        withContext(ioDispatcher) {
            try {
                DataResult.success(call())
            } catch (e: SQLException) {
                DataResult.error("Database error [executeLocalCall]: ${e.message}")
            } catch (e: Exception) {
                DataResult.error("Unknown error [executeLocalCall]: ${e.message}")
            }
        }

    /**
     * Wraps a Flow from a local data source with error handling.
     * @param flowCall Flow representing the local data source.
     * @return Flow emitting Result with Success or Error states.
     */
    protected fun <T> wrapLocalFlow(flowCall: Flow<T>): Flow<DataResult<T>> =
        flowCall
            .map { DataResult.success(it) }
            .catch { emit(DataResult.error("[ERROR:wrapLocalFlow] ${it.message}" ?: "Unknown error [wrapLocalFlow]")) }
            .flowOn(ioDispatcher)


    // Check network availability
//    private suspend fun isNetworkAvailable(context: Context): Boolean
//        = NetworkMonitorImpl(context).isNetworkConnected()

    // Get from cache or fetch and cache the result
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> getOrFetchFromCache(
        cacheKey: String?,
        call: suspend () -> T,
    ): T = cacheKey?.let { key ->
        (cache[key] as? T) ?: fetchAndCache(key, call)
    } ?: call()

    private suspend fun <T> fetchAndCache(key: String, call: suspend () -> T): T =
        call().also { cache[key] = it }
}
