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
    private val cache: MutableMap<String, Any> = mutableMapOf()
) {

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        sealed class Error(val exception: Throwable) : Result<Nothing>() {
            class NetworkError(exception: Throwable) : Error(exception)
            class DatabaseError(exception: Throwable) : Error(exception)
            class UnknownError(exception: Throwable) : Error(exception)
        }
        object Loading : Result<Nothing>()
    }

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
    ): Flow<Result<T>> = flow {
        emit(Result.Loading)

        // Giả định có hàm kiểm tra kết nối mạng (cần triển khai riêng)
        if (!isNetworkAvailable(context)) {
            val error = IOException("No network connection")
            onError(error)
            emit(Result.Error.NetworkError(error))
            return@flow
        }

        try {
            val result = withContext(ioDispatcher) {
                // Kiểm tra cache trước khi gọi mạng
                cacheKey?.let { key ->
                    cache[key]?.let { cachedData ->
                        return@withContext cachedData as T
                    }
                }
                // Thực hiện gọi mạng
                val data = call()
                // Lưu vào cache nếu có cacheKey
                cacheKey?.let { key -> cache[key] = data as Any }
                data
            }
            emit(Result.Success(result))
        } catch (e: HttpException) {
            onError(e)
            emit(Result.Error.NetworkError(e))
        } catch (e: IOException) {
            onError(e)
            emit(Result.Error.NetworkError(e))
        } catch (e: Exception) {
            onError(e)
            emit(Result.Error.UnknownError(e))
        }
    }.flowOn(ioDispatcher)

    /**
     * Executes a local database operation with detailed error handling.
     * @param call Suspend function representing the database operation.
     * @return Result with Success or Error state.
     */
    protected suspend fun <T> executeLocalCall(call: suspend () -> T): Result<T> {
        return try {
            withContext(ioDispatcher) {
                Result.Success(call())
            }
        } catch (e: SQLException) {
            Result.Error.DatabaseError(e)
        } catch (e: Exception) {
            Result.Error.UnknownError(e)
        }
    }

    /**
     * Wraps a Flow from a local data source with error handling.
     * @param flowCall Flow representing the local data source.
     * @return Flow emitting Result with Success or Error states.
     */
    protected fun <T> wrapLocalFlow(flowCall: Flow<T>): Flow<Result<T>> {
        return flowCall
            .map { Result.Success(it) }
            .catch {}
            .flowOn(ioDispatcher)
    }

    private suspend fun isNetworkAvailable(context: Context): Boolean {
        val networkMonitor: NetworkMonitor = NetworkMonitorImpl(context)

        return networkMonitor.isNetworkConnected()
    }
}