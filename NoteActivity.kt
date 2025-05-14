package com.lab1.notesapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteActivity(
    private val navController: NavHostController,
    private val noteId: String,
    private val onNoteSaved: () -> Unit
) {
    private val api = APISender()
    private val title = mutableStateOf("")
    private val text = mutableStateOf("")
    private val isLoading = mutableStateOf(true)
    private val isSaving = mutableStateOf(false)
    private val errorMessage = mutableStateOf<String?>(null)

    init {
        if (noteId != "-1") loadNote() else isLoading.value = false
    }

    private fun loadNote() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.get("/GetNoteById?id=$noteId").get()
                if (response.isSuccessful) {
                    val note = APISender.mapper.readValue<NoteViewModel>(
                        response.body?.string().orEmpty()
                    )
                    withContext(Dispatchers.Main) {
                        title.value = note.title
                        text.value = note.text
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

    private fun saveNote() {
        if (title.value.isBlank()) {
            errorMessage.value = "Заголовок не может быть пустым"
            return
        }

        isSaving.value = true
        errorMessage.value = null

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val endpoint = if (noteId == "-1") "/CreateNote" else "/EditNote"
                val params = mutableMapOf(
                    "title" to title.value.trim(),
                    "text" to text.value.trim()
                ).apply {
                    if (noteId != "-1") put("id", noteId)
                }

                val response = api.post(endpoint, params).get()
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        onNoteSaved()
                        navController.navigateUp()
                    } else {
                        errorMessage.value = "Не удалось сохранить заметку"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage.value = "Ошибка: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSaving.value = false
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NoteActivityScreen() {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(errorMessage.value) {
            errorMessage.value?.let { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                    errorMessage.value = null
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (noteId == "-1") "Новая заметка" else "Редактирование") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Назад")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { saveNote() },
                    modifier = Modifier.size(56.dp)
                ) {
                    if (isSaving.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("✓", fontSize = 24.sp)
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (isLoading.value) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        label = { Text("Заголовок") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 20.sp)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = text.value,
                        onValueChange = { text.value = it },
                        label = { Text("Текст заметки") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                }
            }
        }
    }
}