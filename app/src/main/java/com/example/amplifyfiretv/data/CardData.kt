package com.example.amplifyfiretv.data

import com.example.amplifyfiretv.R

data class CardData(
    val title: String,
    val subtitle: String,
    val imageUrl: String
)

object CardDataProvider {
    val cards = listOf(
        CardData(
            title = "Getting Started with Fire TV",
            subtitle = "Development Basics",
            imageUrl = "https://fastly.picsum.photos/id/767/300/200.jpg?hmac=TTc0t0lEJWrTHSWhos7VTReXgTKIk-OUc3fQA1w91sI"
        ),
        CardData(
            title = "UI Components",
            subtitle = "Building TV Interfaces",
            imageUrl = "https://fastly.picsum.photos/id/448/300/200.jpg?hmac=WHgZcNfmMcA8Sl33YH3lirNV6pSOFPOrxigNhp-lNzc"
        ),
        CardData(
            title = "Content Integration",
            subtitle = "Media Playback",
            imageUrl = "https://fastly.picsum.photos/id/890/300/200.jpg?hmac=0LKMOsjIqNRCSE4Iqm1C8jryvFHQKLxO2PLNzrhDCKw"
        )
    )
} 