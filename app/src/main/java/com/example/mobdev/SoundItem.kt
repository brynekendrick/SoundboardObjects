package com.example.soundboard

import java.io.Serializable

data class SoundItem(
    val id: String,
    var title: String,
    var category: String,
    var duration: String,
    var resourceId: Int = -1,
    var isFavorite: Boolean = false,
    var filePath: String? = null
) : Serializable {

    constructor(id: String, title: String, category: String, duration: String, resourceId: Int) : this(
        id, title, category, duration, resourceId, isFavorite = false
    )

    constructor(id: String, title: String, category: String, duration: String, filePath: String) : this(
        id, title, category, duration, resourceId = -1, isFavorite = false, filePath = filePath
    )

    fun toggleFavorite() {
        isFavorite = !isFavorite
    }

    fun isDownloaded(): Boolean {
        return !filePath.isNullOrEmpty()
    }

    fun isResourceSound(): Boolean {
        return resourceId != -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as SoundItem
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
