package com.example.amplifyfiretv.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson

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
    private var cards: List<CardData> = emptyList()

    fun initialize(context: Context) {
        try {
            Log.d(TAG, "Starting card data initialization")
            val jsonString = context.assets.open("cards.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "Successfully read JSON file, length: ${jsonString.length}")
            
            val response = Gson().fromJson(jsonString, CardDataResponse::class.java)
            cards = response.cards
            Log.d(TAG, "Successfully parsed ${cards.size} cards")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing card data", e)
            // Fallback to empty list if there's an error
            cards = emptyList()
        }
    }

    fun getCards(): List<CardData> {
        Log.d(TAG, "getCards called, returning ${cards.size} cards")
        return cards
    }
} 
