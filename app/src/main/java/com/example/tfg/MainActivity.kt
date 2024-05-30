package com.example.tfg

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.data.GameDatabase
import com.example.tfg.state.CustomMainViewModelFactory
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.mainactivity.MainScreen
import com.example.tfg.ui.theme.TFGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val dao = GameDatabase.getDatabase(this).gameDao()
        val sharedPref = getSharedPreferences("Configuration", Context.MODE_PRIVATE)

        val viewModel: MainViewModel by viewModels{ CustomMainViewModelFactory(dao, sharedPref) }

        super.onCreate(savedInstanceState)
        setContent {
            TFGTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TFGTheme {
                        MainScreen(
                            viewModel = viewModel,
                            modifier = Modifier
                                .background(colorResource(id = R.color.primary_background))
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val database = GameDatabase.getInMemoryDatabase(LocalContext.current)
    val sharedPreferences = LocalContext.current.getSharedPreferences("Configuration", Context.MODE_PRIVATE)
    val viewModel: MainViewModel = viewModel(factory = CustomMainViewModelFactory(database.gameDao(), sharedPreferences, true))

    TFGTheme {
        MainScreen(
            viewModel = viewModel,
            modifier = Modifier
                .background(colorResource(id = R.color.primary_background))
                .fillMaxWidth()
        )
    }
}



