package com.lab1.notesapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity(private val navController: NavHostController) {
    private val api = APISender()
    private val notes = mutableStateOf(emptyList<NoteViewModel>())
    private val isLoading = mutableStateOf(true)
    private val errorMessage = mutableStateOf<String?>(null)

    init {
        loadNotes()
    }

    fun loadNotes() {
        isLoading.value = true
        errorMessage.value = null

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.get("/GetAllNotes").get()
                if (response.isSuccessful) {
                    val notesList = APISender.mapper.readValue<List<NoteViewModel>>(
                        response.body?.string().orEmpty()
                    )
                    withContext(Dispatchers.Main) {
                        notes.value = notesList.sortedByDescending { it.updatedAt }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage.value = "Ошибка загрузки: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    @Composable
    fun HomeActivityScreen() {
        LaunchedEffect(Unit) {
            loadNotes()
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Destinations.NoteScreen.createRoute("-1")) }
                ) {
                    Text("+", fontSize = 25.sp)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Заметки", fontSize = 25.sp)
                Spacer(Modifier.height(20.dp))

                when {
                    isLoading.value -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage.value != null -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text(errorMessage.value!!)
                        }
                    }
                    notes.value.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text("Нет заметок")
                        }
                    }
                    else -> {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(notes.value) { note ->
                                NoteItem(note) {
                                    navController.navigate(Destinations.NoteScreen.createRoute(note.id))
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NoteItem(note: NoteViewModel, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F4F2))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = note.title.ifEmpty { "Заголовок отсутствует" },
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = note.text.ifEmpty { "Текст отсутствует" },
                    fontSize = 11.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}