package com.lab1.notesapp

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class NoteViewModel(
    @JsonProperty("id") val id: String,
    @JsonProperty("text") val text: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("createdAt") val createdAt: String,
    @JsonProperty("updatedAt") val updatedAt: String
) : Serializable