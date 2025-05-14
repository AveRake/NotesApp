package com.lab1.notesapp

sealed class Destinations(
    val route: String,
    val title: String? = null
) {
    object HomeScreen : Destinations(
        route = "home_screen",
        title = "Заметки"
    )

    object NoteScreen : Destinations(
        route = "note_screen/{note_id}",
        title = "Заметка"
    ) {
        fun createRoute(noteId: String) = "note_screen/$noteId"
    }
}