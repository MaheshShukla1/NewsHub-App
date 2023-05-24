package com.example.newsapp.data.api.model

data class Source(
    val id: String? = "",
    val name: String? = ""
) {
    override fun hashCode(): Int {
        return id.hashCode() + name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Source) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }
}
