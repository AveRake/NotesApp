package com.lab1.notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lab1.notesapp.ui.theme.NotesAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavigationGraph(navController)
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    val home = remember { HomeActivity(navController) }

    NavHost(
        navController = navController,
        startDestination = Destinations.HomeScreen.route
    ) {
        composable(Destinations.HomeScreen.route) {
            home.HomeActivityScreen()
        }
        composable(Destinations.NoteScreen.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("note_id") ?: "-1"
            NoteActivity(
                navController = navController,
                noteId = noteId,
                onNoteSaved = { home.loadNotes() }
            ).NoteActivityScreen()
        }
    }
}