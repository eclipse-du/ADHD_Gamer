package com.piepie.brainhouse.game.blindbox

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BlindBoxState {
    PLAYING,        // User is opening/closing boxes
    QUESTION_TIME,  // User must answer a specific question
    LEVEL_COMPLETE  // Level finished
}

data class BoxItem(
    val id: Int,
    val content: BoxContent,
    val isOpen: Boolean = false,
    val isLocked: Boolean = true
)

sealed class BoxContent {
    data class ColorContent(val color: Color, val name: String) : BoxContent()
    // data class ImageContent(val resId: Int) : BoxContent() 
}

data class Question(
    val targetIndex: Int,
    val options: List<BoxContent>,
    val correctAnswer: BoxContent
)

data class AnswerRecord(
    val question: Question,
    val userAnswer: BoxContent,
    val isCorrect: Boolean
)

data class BlindBoxLevelResult(
    val stars: Int,
    val timeMs: Long,
    val history: List<AnswerRecord>
)

class BlindBoxEngine(private val scope: CoroutineScope) {
    private val _gameState = MutableStateFlow(BlindBoxState.PLAYING)
    val gameState = _gameState.asStateFlow()

    private val _boxes = MutableStateFlow<List<BoxItem>>(emptyList())
    val boxes = _boxes.asStateFlow()

    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion = _currentQuestion.asStateFlow()
    
    // Tracking
    private val _answerHistory = MutableStateFlow<List<AnswerRecord>>(emptyList())
    
    // Logic internal state
    private var currentStepIndex = 0
    private var triggerIndices: Set<Int> = emptySet()
    private var startTime = 0L
    private var currentAutoCloseJob: Job? = null
    
    // Palette for generation (Macaron/Distinct)
    private val colors = listOf(
        Color(0xFFFF5252) to "红色", // Red A200
        Color(0xFF448AFF) to "蓝色", // Blue A200
        Color(0xFF69F0AE) to "绿色", // Green A200
        Color(0xFFFFD740) to "黄色", // Amber A200
        Color(0xFFE040FB) to "紫色", // Purple A200
        Color(0xFFFF6E40) to "橙色", // Deep Orange A200
        Color(0xFF18FFFF) to "青色", // Cyan A200
        Color(0xFFFF4081) to "粉色", // Pink A200
        Color(0xFF607D8B) to "灰蓝", // Blue Gray
        Color(0xFF795548) to "棕色"  // Brown
    )

    fun startLevel(config: BlindBoxLevelConfig) {
        // 1. Generate Content
        val count = config.boxCount
        val shuffled = colors.shuffled().take(count)
        val newBoxes = shuffled.mapIndexed { index, (color, name) ->
            BoxItem(
                id = index, 
                content = BoxContent.ColorContent(color, name), 
                isOpen = false,
                isLocked = true
            )
        }
        _boxes.value = newBoxes
        
        // 2. Setup Logic
        currentStepIndex = 0
        unlockBox(0)
        startTime = System.currentTimeMillis()
        _answerHistory.value = emptyList()
        _currentQuestion.value = null
        
        // 3. Determine Trigger Points (Pop Quiz)
        // Rule: Can only quiz about PREVIOUS boxes. So cannot trigger on index 0.
        // We pick 3 random points from 1..lastIndex
        // If count is small (e.g. 3), we might only have index 1 and 2 available.
        // We need ensuring we trigger enough questions for 3 stars if possible, OR adjust scoring.
        
        val candidates = (1 until count).toList()
        
        // For small levels (count <= 3), we might not have 3 candidates.
        // If count=3 (indices 0,1,2), candidates=[1,2]. We can trigger on 1 and 2. Max 2 questions.
        // If count=4 (indices 0,1,2,3), candidates=[1,2,3]. Max 3 questions.
        
        val questionsToAsk = minOf(3, candidates.size)
        triggerIndices = candidates.shuffled().take(questionsToAsk).toSet() 
        
        // 4. Start
        _gameState.value = BlindBoxState.PLAYING
    }
    
    // ... (rest of methods unchanged until calculateResult) ...
    
    private fun unlockBox(index: Int) {
        if (index >= _boxes.value.size) return
        _boxes.update { list ->
            list.map { if (it.id == index) it.copy(isLocked = false) else it }
        }
    }

    fun onBoxClick(index: Int) {
        if (_gameState.value != BlindBoxState.PLAYING) return
        
        val box = _boxes.value.find { it.id == index } ?: return
        if (box.isLocked) return
        
        if (index != currentStepIndex) {
             if (box.isOpen) {
                 manualClose(index)
             }
             return
        }

        if (box.isOpen) {
            manualClose(index)
        } else {
            openBox(index)
        }
    }
    
    private fun openBox(index: Int) {
        _boxes.update { list ->
            list.map { if (it.id == index) it.copy(isOpen = true) else it }
        }
        
        currentAutoCloseJob?.cancel()
        currentAutoCloseJob = scope.launch {
            delay(2000) 
            if (isActive) {
                closeSequence(index)
            }
        }
    }
    
    private fun manualClose(index: Int) {
        currentAutoCloseJob?.cancel()
        currentAutoCloseJob = null
        scope.launch {
             closeSequence(index)
        }
    }
    
    private suspend fun closeSequence(index: Int) {
        _boxes.update { list ->
            list.map { if (it.id == index) it.copy(isOpen = false) else it }
        }
        
        delay(300)
        
        if (triggerIndices.contains(index)) {
            triggerQuestion(index)
        } else {
            advanceStep()
        }
    }
    
    private fun triggerQuestion(currentIndex: Int) {
        _gameState.value = BlindBoxState.QUESTION_TIME
        
        val candidateIndices = (0 until currentIndex).toList()
        if (candidateIndices.isEmpty()) {
            advanceStep() 
            return
        }
        
        val targetIndex = candidateIndices.random() // Pick a previous box to ask about
        // Potential improvement: Pick a box we haven't asked about yet?
        // But simple random is okay for now.
        
        val targetBox = _boxes.value[targetIndex]
        
        val allContent = _boxes.value.map { it.content }.distinct()
        val correct = targetBox.content
        val wrong = (allContent - correct).shuffled().take(2)
        val options = (wrong + correct).shuffled()
        
        _currentQuestion.value = Question(
            targetIndex = targetIndex,
            options = options,
            correctAnswer = correct
        )
    }
    
    fun submitAnswer(answer: BoxContent) {
        val question = _currentQuestion.value ?: return
        val isCorrect = answer == question.correctAnswer
        
        _answerHistory.update { 
            it + AnswerRecord(question, answer, isCorrect)
        }
        
        _currentQuestion.value = null
        _gameState.value = BlindBoxState.PLAYING
        
        advanceStep()
    }
    
    private fun advanceStep() {
        if (currentStepIndex >= _boxes.value.size - 1) {
            finishLevel()
        } else {
            currentStepIndex++
            unlockBox(currentStepIndex)
        }
    }
    
    private fun finishLevel() {
        _gameState.value = BlindBoxState.LEVEL_COMPLETE
        _boxes.update { list -> list.map { it.copy(isOpen = true) } }
    }
    
    fun calculateResult(): BlindBoxLevelResult {
        // Scoring Logic Update for Low Levels
        // If total questions < 3, we should scale the score?
        // Current logic: stars = correct count.
        // If max questions = 2 (Level 1 with 3 boxes), max stars = 2. User cannot get 3 stars.
        // Fix: Calculate percentage or explicit mapping.
        
        val correctCount = _answerHistory.value.count { it.isCorrect }
        val totalQuestions = _answerHistory.value.size
        
        val stars = if (totalQuestions == 0) {
            3 // No questions asked? (Should not happen unless configured) -> Free pass
        } else if (totalQuestions < 3) {
            // If we asked fewer than 3 questions (e.g. Level 1), 
            // All correct = 3 stars.
            // One mistake = 1 star?
            if (correctCount == totalQuestions) 3 
            else if (correctCount > 0) 2 
            else 0
        } else {
            // Standard logic for 3+ questions
            // 3 items: 3 stars. 2 items: 2 stars.
            correctCount
        }
        
        val timeMs = System.currentTimeMillis() - startTime
        return BlindBoxLevelResult(stars, timeMs, _answerHistory.value)
    }
}
