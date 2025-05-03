package com.example.amplifyfiretv.data

import android.content.Context
import android.util.Log
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.amplifyframework.storage.StoragePath
import com.example.amplifyfiretv.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CardData(
    val videoId: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val videoUrl: String
)

data class CardDataResponse(
    val cards: List<CardData>
)

object CardDataProvider {
    private const val TAG = "CardDataProvider"
    private const val FILE_NAME = "cards.json"
    private const val DATA_SUBFOLDER = "data"
    private var cards: List<CardData> = emptyList()
    private var onCardsLoaded: (() -> Unit)? = null

    fun setOnCardsLoadedListener(listener: () -> Unit) {
        onCardsLoaded = listener
        // If cards are already loaded, notify immediately
        if (cards.isNotEmpty()) {
            listener()
        }
    }

    suspend fun initialize(context: Context) {
        Log.d(TAG, "=== Starting card data initialization ===")
        try {
            withContext(Dispatchers.IO) {
                // Create cache directory if it doesn't exist
                val cacheDir = context.cacheDir
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                
                // Set up local file for downloading
                val localFile = File(cacheDir, FILE_NAME)
                Log.d(TAG, "Local file path: ${localFile.absolutePath}")
                
                // Get bucket name from AmplifyOutputs
                val outputs = AmplifyOutputs.fromResource(R.raw.amplify_outputs)
                val jsonString = context.resources.openRawResource(R.raw.amplify_outputs).bufferedReader().use { it.readText() }
                val jsonObject = Gson().fromJson(jsonString, JsonObject::class.java)
                val storageObject = jsonObject.getAsJsonObject("storage")
                val bucketName = storageObject.get("bucket_name").asString
                
                // Set up S3 path - using data/ prefix to match permissions
                val s3Path = StoragePath.fromString("$DATA_SUBFOLDER/$FILE_NAME")
                Log.d(TAG, "S3 path: $s3Path")
                
                val options = StorageDownloadFileOptions.builder().build()
                Log.d(TAG, "Initiating S3 download...")
                Amplify.Storage.downloadFile(
                    s3Path,
                    localFile,
                    options,
                    { result ->
                        Log.d(TAG, "=== Download completed successfully ===")
                        Log.d(TAG, "Downloaded file path: ${result.file.absolutePath}")
                        Log.d(TAG, "Downloaded file exists: ${result.file.exists()}")
                        Log.d(TAG, "Downloaded file size: ${result.file.length()} bytes")
                        
                        try {
                            val jsonString = localFile.readText()
                            val response = Gson().fromJson(jsonString, CardDataResponse::class.java)
                            cards = response.cards
                            Log.d(TAG, "Successfully parsed ${cards.size} cards")
                            
                            // Notify listeners that cards are loaded
                            onCardsLoaded?.invoke()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing downloaded file", e)
                            e.printStackTrace()
                            throw e
                        }
                    },
                    { error ->
                        Log.e(TAG, "=== Download failed ===")
                        Log.e(TAG, "Error message: ${error.message}")
                        Log.e(TAG, "Error cause: ${error.cause?.message}")
                        error.printStackTrace()
                        throw error
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "=== Card data initialization failed ===")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Error cause: ${e.cause?.message}")
            e.printStackTrace()
            throw e
        }
        Log.d(TAG, "=== Completed card data initialization ===")
    }

    fun getCards(): List<CardData> {
        Log.d(TAG, "getCards called, returning ${cards.size} cards")
        return cards
    }
} 
