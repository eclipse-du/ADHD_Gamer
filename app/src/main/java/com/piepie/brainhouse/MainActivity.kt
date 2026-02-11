package com.piepie.brainhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.piepie.brainhouse.data.LevelRecord
import com.piepie.brainhouse.game.blindbox.BlindBoxGameScreen
import com.piepie.brainhouse.game.blindbox.BlindBoxLevelConfig
import com.piepie.brainhouse.game.schulte.SchulteGameScreen
import com.piepie.brainhouse.game.schulte.SchulteLevelConfig
import com.piepie.brainhouse.ui.CustomModeScreen
import com.piepie.brainhouse.ui.GameDataViewModel
import com.piepie.brainhouse.ui.HallOfFameScreen
import com.piepie.brainhouse.ui.LevelSelectSyncScreen
import com.piepie.brainhouse.ui.MainMenuScreen
import com.piepie.brainhouse.ui.theme.PikeBrainHouseTheme
import com.piepie.brainhouse.util.SoundManager

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager
    private val gameViewModel: GameDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        soundManager = SoundManager(this)
        
        setContent {
            PikeBrainHouseTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main_menu"
                    ) {
                        composable("main_menu") {
                            MainMenuScreen(
                                onNavigateToGameSelect = { navController.navigate("game_select") },
                                onNavigateToCustom = { navController.navigate("custom_mode") },
                                onNavigateToHonors = { navController.navigate("hall_of_fame") },
                                soundManager = soundManager
                            )
                        }
                        
                        composable("game_select") {
                            LevelSelectSyncScreen(
                                onLevelSelected = { gameType, level ->
                                    navController.navigate("game/${gameType}/${level}")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("custom_mode") {
                            CustomModeScreen(
                                onExit = { navController.popBackStack() },
                                onStartGame = { type, p1, _ ->
                                    navController.navigate("game_custom/$type/$p1") 
                                },
                                soundManager = soundManager
                            )
                        }
                        
                        composable("hall_of_fame") {
                            HallOfFameScreen(
                                onExit = { navController.popBackStack() },
                                soundManager = soundManager
                            )
                        }
                        
                        // Game Routes "game/SCHULTE/1"
                        composable(
                            "game/{type}/{level}",
                            arguments = listOf(
                                navArgument("type") { type = NavType.StringType },
                                navArgument("level") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val type = backStackEntry.arguments?.getString("type") ?: "SCHULTE"
                            val level = backStackEntry.arguments?.getInt("level") ?: 1
                            
                            fun onNextLevel() {
                                if (level < 8) {
                                    navController.navigate("game/$type/${level + 1}") {
                                        popUpTo("game/$type/$level") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("hall_of_fame") {
                                        popUpTo("game_select") { inclusive = false }
                                    }
                                }
                            }

                            if (type == "SCHULTE") {
                                SchulteGameScreen(
                                    levelId = level,
                                    onExit = { navController.popBackStack() },
                                    onLevelComplete = { stars, time -> 
                                        val record = LevelRecord("SCHULTE", level, stars, time)
                                        gameViewModel.saveRecord(record)
                                        // Do NOT pop back stack here, wait for user action
                                    },
                                    onNextLevel = ::onNextLevel,
                                    soundManager = soundManager
                                )
                            } else {
                                BlindBoxGameScreen(
                                    levelId = level,
                                    onExit = { navController.popBackStack() },
                                    onLevelComplete = { stars, time -> 
                                        val record = LevelRecord("BLINDBOX", level, stars, time)
                                        gameViewModel.saveRecord(record)
                                    },
                                    onNextLevel = ::onNextLevel,
                                    soundManager = soundManager
                                )
                            }
                        }
                        
                        // Custom Game Route
                        composable(
                            "game_custom/{type}/{param}",
                             arguments = listOf(
                                navArgument("type") { type = NavType.StringType },
                                navArgument("param") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val type = backStackEntry.arguments?.getString("type")
                            val param = backStackEntry.arguments?.getInt("param") ?: 3
                            
                            LaunchedEffect(Unit) {
                                gameViewModel.unlockCustomModeHonor()
                            }
                            
                            fun onNextLevelCustom() {
                                navController.popBackStack()
                            }
                            
                            if (type == "SCHULTE") {
                                val config = SchulteLevelConfig(
                                    levelId = -1,
                                    gridSize = param,
                                    timeLimitSec = 999,
                                    star3TimeSec = 10, 
                                    star2TimeSec = 20
                                )
                                SchulteGameScreen(
                                    levelId = -1,
                                    customConfig = config,
                                    onExit = { navController.popBackStack() },
                                    onLevelComplete = { _, _ -> /* No record for custom */ },
                                    onNextLevel = ::onNextLevelCustom,
                                    soundManager = soundManager
                                )
                            } else {
                                val config = BlindBoxLevelConfig(
                                    levelId = -1,
                                    boxCount = param,
                                    memorizeTimeSec = 10 
                                )
                                BlindBoxGameScreen(
                                    levelId = -1,
                                    customConfig = config,
                                    onExit = { navController.popBackStack() },
                                    onLevelComplete = { _, _ -> },
                                    onNextLevel = ::onNextLevelCustom,
                                    soundManager = soundManager
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
