package com.example.amplifyfiretv.data

import android.content.Context
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
    private var cards: List<CardData> = emptyList()

    fun initialize(context: Context) {
        try {
            val jsonString = context.assets.open("cards.json").bufferedReader().use { it.readText() }
            val response = Gson().fromJson(jsonString, CardDataResponse::class.java)
            cards = response.cards
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to empty list if there's an error
            cards = emptyList()
        }
    }

    fun getCards(): List<CardData> = cards
} 
