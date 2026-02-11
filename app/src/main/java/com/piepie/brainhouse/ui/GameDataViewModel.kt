package com.piepie.brainhouse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piepie.brainhouse.data.AppDatabase
import com.piepie.brainhouse.data.Honor
import com.piepie.brainhouse.data.LevelRecord
import com.piepie.brainhouse.R
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class GameDataViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val honorDao = db.honorDao()
    private val levelRecordDao = db.levelRecordDao()

    val allHonors: StateFlow<List<Honor>> = honorDao.getAllHonors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate honors if empty
        viewModelScope.launch {
            if (honorDao.getUnlockedHonors().first().isEmpty()) { // Check if empty first
                val initialHonors = listOf(
                    // Schulte Honors (Focus/Speed)
                    Honor(1, "新手的派派", "完成舒尔特 Level 1", R.drawable.sticker_rookie_mouse, unlockCondition = "Schulte Level 1"),
                    Honor(2, "稳重的派派", "完成舒尔特 Level 2", R.drawable.sticker_steady_turtle, unlockCondition = "Schulte Level 2"),
                    Honor(3, "好奇的派派", "完成舒尔特 Level 3", R.drawable.sticker_curious_cat, unlockCondition = "Schulte Level 3"),
                    Honor(4, "活泼的派派", "完成舒尔特 Level 4", R.drawable.sticker_jumping_frog, unlockCondition = "Schulte Level 4"),
                    Honor(5, "敏捷的派派", "完成舒尔特 Level 5", R.drawable.sticker_quick_rabbit, unlockCondition = "Schulte Level 5"),
                    Honor(6, "飞翔的派派", "完成舒尔特 Level 6", R.drawable.sticker_flying_falcon, unlockCondition = "Schulte Level 6"),
                    Honor(7, "锐利的派派", "完成舒尔特 Level 7", R.drawable.sticker_eagle_eye, unlockCondition = "Schulte Level 7"),
                    Honor(8, "神速的派派", "完成舒尔特 Level 8", R.drawable.sticker_fast_cheetah, unlockCondition = "Schulte Level 8"),
                    
                    // Blind Box Honors (Memory)
                    Honor(9, "贪睡的派派", "完成盲盒 Level 1", R.drawable.sticker_sleepy_koala, unlockCondition = "Blind Box Level 1"),
                    Honor(10, "快乐的派派", "完成盲盒 Level 2", R.drawable.sticker_happy_puppy, unlockCondition = "Blind Box Level 2"),
                    Honor(11, "聪明的派派", "完成盲盒 Level 3", R.drawable.sticker_clever_fox, unlockCondition = "Blind Box Level 3"),
                    Honor(12, "聪慧的派派", "完成盲盒 Level 4", R.drawable.sticker_bright_dolphin, unlockCondition = "Blind Box Level 4"),
                    Honor(13, "博学的派派", "完成盲盒 Level 5", R.drawable.sticker_wise_elephant, unlockCondition = "Blind Box Level 5"),
                    Honor(14, "深邃的派派", "完成盲盒 Level 6", R.drawable.sticker_memory_whale, unlockCondition = "Blind Box Level 6"),
                    Honor(15, "天才的派派", "完成盲盒 Level 7", R.drawable.sticker_genius_monkey, unlockCondition = "Blind Box Level 7"),
                    Honor(16, "无敌的派派", "完成盲盒 Level 8", R.drawable.sticker_master_dragon, unlockCondition = "Blind Box Level 8"),
                    
                    // Special Honors
                    Honor(17, "初试的派派", "体验自定义模式", R.drawable.sticker_baby_pike, unlockCondition = "Play Custom Mode"),
                    Honor(18, "坚强的派派", "失败3次继续挑战", R.drawable.sticker_strong_bear, unlockCondition = "Resilience"),
                    Honor(19, "光速的派派", "5秒内完成挑战", R.drawable.sticker_rocket_pike, unlockCondition = "Super Speed"),
                    Honor(20, "王者派派", "解锁所有其他荣誉", R.drawable.sticker_king_pike, unlockCondition = "Collection Master")
                )
                honorDao.insertHonors(initialHonors)
            }
        }
    }
    
    fun unlockHonor(id: Int) {
        viewModelScope.launch {
            val honorList = honorDao.getAllHonors().first()
            val honor = honorList.find { it.id == id }
            if (honor != null && !honor.isUnlocked) {
                honorDao.updateHonor(honor.copy(isUnlocked = true))
                // Could play a sound or show a toast here if Context was available or via State
            }
        }
    }
    
    fun saveRecord(record: LevelRecord) {
        viewModelScope.launch {
            levelRecordDao.insertRecord(record)
            checkUnlocks(record)
        }
    }
    
    // Check for unlocks based on the just-completed record
    private fun checkUnlocks(record: LevelRecord) {
        // Schulte Honors: ID 1-8 for Levels 1-8
        if (record.gameType == "SCHULTE") {
            if (record.levelId in 1..8) {
                unlockHonor(record.levelId) // Honor ID maps directly to Level ID for 1-8
            }
            
            // Special Honors
            if (record.bestTime < 5000) { // < 5 seconds
                unlockHonor(19) // Rocket Pike
            }
        }
        
        // Blind Box Honors: ID 9-16 for Levels 1-8
        if (record.gameType == "BLINDBOX") {
            if (record.levelId in 1..8) {
                unlockHonor(record.levelId + 8) // Level 1 -> ID 9, Level 8 -> ID 16
            }
        }
        
        // General checks
        viewModelScope.launch {
             val allHonors = honorDao.getAllHonors().first()
             val unlockedCount = allHonors.count { it.isUnlocked }
             if (unlockedCount >= 19) {
                 unlockHonor(20) // King Pike
             }
        }
    }
    
    fun unlockCustomModeHonor() {
        unlockHonor(17) // Baby Pike
    }
    
    fun unlockResilienceHonor() {
        unlockHonor(18) // Strong Bear
    }

    fun resetHonors() {
        viewModelScope.launch(Dispatchers.IO) {
            // Re-lock all honors
            val currentHonors = allHonors.value
            val relockedHonors = currentHonors.map { 
                it.copy(isUnlocked = false)
            }
            honorDao.insertHonors(relockedHonors)
            
            // Reset all level records to 0 stars
            val records = levelRecordDao.getAllRecords().first()
            val resetRecords = records.map { it.copy(stars = 0, bestTime = 0) }
            for (record in resetRecords) {
                levelRecordDao.insertRecord(record)
            }
        }
    }
}
