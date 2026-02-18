package com.piepie.brainhouse.game.ultraman

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piepie.brainhouse.data.AppDatabase
import com.piepie.brainhouse.data.LevelRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UltramanProgress(
    val level1Unlocked: Boolean = true,
    val level2Unlocked: Boolean = false,
    val level3Unlocked: Boolean = false,
    val photoUnlocked: Boolean = false
)

class UltramanViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val levelRecordDao = db.levelRecordDao()
    private val honorDao = db.honorDao()

    private val _progress = MutableStateFlow(UltramanProgress())
    val progress: StateFlow<UltramanProgress> = _progress.asStateFlow()

    init {
        refreshProgress()
    }

    private fun refreshProgress() {
        viewModelScope.launch {
            val records = levelRecordDao.getAllRecords().first()
            val l1Passed = records.any { it.gameType == "ULTRAMAN" && it.levelId == 1 && it.stars > 0 }
            val l2Passed = records.any { it.gameType == "ULTRAMAN" && it.levelId == 2 && it.stars > 0 }
            val l3Passed = records.any { it.gameType == "ULTRAMAN" && it.levelId == 3 && it.stars > 0 }
            
            // Check Honor 21
            val honor21Unlocked = honorDao.getUnlockedHonors().first().any { it.id == 21 }

            _progress.value = UltramanProgress(
                level1Unlocked = true, // Always unlocked
                level2Unlocked = l1Passed,
                level3Unlocked = l2Passed,
                photoUnlocked = honor21Unlocked
            )
        }
    }

    fun completeLevel(levelId: Int, score: Int) {
        viewModelScope.launch {
            // Save Record (Stars = 3 for pass, 0 for fail)
            // Only save if improved?
            // Just save explicit "Pass" record
            val currentBest = levelRecordDao.getAllRecords().first()
                .find { it.gameType == "ULTRAMAN" && it.levelId == levelId }?.bestTime?.toLong() ?: 0L

            // For Ultraman, bestTime stores Score? (Time survived or Endless score)
            // Let's store Score in bestTime field (repurposed)
            val newScore = score.toLong()
            
            if (newScore > currentBest) {
                 levelRecordDao.insertRecord(
                    LevelRecord(
                        gameType = "ULTRAMAN",
                        levelId = levelId,
                        stars = 3, // Passed
                        bestTime = newScore // Store score
                    )
                )
            } else if (currentBest == 0L) {
                 // First clear
                 levelRecordDao.insertRecord(
                    LevelRecord(
                        gameType = "ULTRAMAN",
                        levelId = levelId,
                        stars = 3,
                        bestTime = newScore
                    )
                )
            }

            refreshProgress()

            // Check final unlock
            if (levelId == 3) {
                unlockPhotoHonor()
            }
        }
    }
    
    // Call this if failed but want to record attempt? No, only passes matter for unlock
    
    private fun unlockPhotoHonor() {
        viewModelScope.launch {
            val honorList = honorDao.getAllHonors().first()
            val honor = honorList.find { it.id == 21 }
            if (honor != null && !honor.isUnlocked) {
                honorDao.updateHonor(honor.copy(isUnlocked = true))
            }
            refreshProgress()
        }
    }
}
