# [Ultraman Agility Game] Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**LANGUAGE RULE:** The plan is written in **CHINESE** as required.

**Goal:** 在 `PikeBrainHouse` 中集成一个全新的“奥特曼特训”子游戏模块，包含三个敏捷挑战关卡。

**Architecture:** 
-   **独立模块:** `game/ultraman` 包，包含独立的 `UltramanEngine`, `UltramanScreen`, `UltramanAssets`.
-   **状态管理:** 使用 `UltramanViewModel` 管理游戏状态（倒计时、分数、角色状态、关卡解锁）。
-   **导航:** 主界面“玩具箱”入口 -> `UltramanMenuScreen` (选关) -> `UltramanGameScreen`.
-   **数据持久化:** 使用 DataStore 存储 `UltramanLevelProgress` (解锁状态) 和 `HighScore`.

**Tech Stack:** Kotlin, Jetpack Compose, DataStore, Android TTS.

---

### Task 1: 资源生成 (Asset Generation)

**Files:**
- Create: `scripts/generate_ultraman_assets.md` (Prompt Log)
- Generate: `app/src/main/res/drawable/ultraman_bg_graveyard.png` (怪兽墓场背景，Q版风格)
- Generate: `app/src/main/res/drawable/char_sd_zero.png` (Q版赛罗，站立/防御/攻击姿态)
- Generate: `app/src/main/res/drawable/char_sd_belial.png` (Q版贝利亚，站立/攻击/防御姿态)
- Generate: `app/src/main/res/drawable/char_sd_ace.png` (Q版艾斯，站立/跳跃姿态)
- Generate: `app/src/main/res/drawable/effect_beam_zero.png` (赛罗光线)
- Generate: `app/src/main/res/drawable/effect_beam_belial.png` (贝利亚光线，红黑色)
- Generate: `app/src/main/res/drawable/icon_ultraman_toy.png` (入口图标，奥特曼面具/玩偶)

**Step 1: Create Prompts & Generate Background**
-   Background: "Cute cartoon style illustration of Monster Graveyard from Ultraman, space background with floating rocks and toy monster skeletons, dark purple and grey theme but kid friendly."

**Step 2: Generate Characters (SD Style)**
-   Zero: "Cute SD Chibi Ultraman Zero toy figure, ceramic texture, heroic pose."
-   Belial: "Cute SD Chibi Ultraman Belial toy figure, ceramic texture, holding Giga Battlenizer."
-   Ace: "Cute SD Chibi Ultraman Ace toy figure, ceramic texture."

**Step 3: Generate Effects & Icon**
-   Beams: "Cartoon energy beam effect, simple shape."

---

### Task 2: 核心逻辑与数据结构 (Core Logic)

**Files:**
- Create: `app/src/main/java/com/piepie/brainhouse/game/ultraman/UltramanGameEngine.kt`
- Create: `app/src/main/java/com/piepie/brainhouse/game/ultraman/UltramanViewModel.kt`
- Modify: `app/src/main/java/com/piepie/brainhouse/data/GameRepository.kt` (Add Ultraman progress)

**Step 1: Define Game States**
-   `GameState`: IDLE, PLAYING, PAUSED, GAME_OVER, VICTORY.
-   `PlayerAction`: IDLE, ACTION (Clip/Shield/Jump).
-   `EnemyAction`: IDLE, CHARGING, ATTACKING.

**Step 2: Implement Game Loop**
-   Logic: Enemy spawns attack at random/increasing intervals.
-   Timing: Check if `PlayerAction` matches `EnemyAction` within `HitWindow` (e.g. 500ms).
-   Result: Hit -> Fail -> TTS; Block -> Score -> Continue.

**Step 3: Implement Progression Logic**
-   Unlock Level 2 only if Level 1 passed (60s survival).
-   Save progress to DataStore.

---

### Task 3: 游戏界面实现 (UI Implementation)

**Files:**
- Create: `app/src/main/java/com/piepie/brainhouse/game/ultraman/UltramanScreen.kt`
- Create: `app/src/main/java/com/piepie/brainhouse/game/ultraman/UltramanMenuScreen.kt`
- Modify: `app/src/main/java/com/piepie/brainhouse/ui/MainMenuScreen.kt` (Add entry point)

**Step 1: Create Menu Screen**
-   Layout: 3 Cards (Zero, Belial, Ace).
-   Lock State: Show "Locked" (Greyed out) for L2/L3 if not cleared.
-   Mode Toggle: "60s Challenge" vs "Endless".

**Step 2: Create Game Screen**
-   Background: `ultraman_bg_graveyard`.
-   Characters: Player (Left/Bottom), Enemy (Right/Top).
-   HUD: Timer (60s countdown), Score (Endless), Health (Life?).
-   Controls: Full screen tap or specific button? (User allowed change, but tap anywhere is easiest for kids).

**Step 3: Implement Animations**
-   `animateFloatAsState` for Beam movement.
-   Simple offset/scale/alpha animations for Actions (Jump = Y offset, Shield = Alpha/Visible).

---

### Task 4: 音频与反馈系统 (Audio & Polish)

**Files:**
- Modify: `app/src/main/java/com/piepie/brainhouse/util/SoundManager.kt`
- Modify: `app/src/main/java/com/piepie/brainhouse/game/ultraman/UltramanEngine.kt`

**Step 1: Integrate TTS**
-   L1 Fail (Zero): "你还差两万年呢！"
-   L2 Fail (Belial): "本大爷是最强的！"
-   L3 Fail (Belial): "黑暗将吞噬一切！" (Generic)
-   Victory (Ace): "热忱之心不可磨灭！"

**Step 2: Victory Reward**
-   Show `user_child_photo.jpg` next to the Victorious Ultraman.
-   "Photo Op" UI overlay.

---

### Task 5: 集成与测试 (Integration)

**Files:**
- Modify: `app/src/main/java/com/piepie/brainhouse/MainActivity.kt` (NavHost)

**Step 1: Navigation**
-   Add `UltramanMenu` and `UltramanGame` to NavGraph.

**Step 2: Manual Test**
-   Verify Progression (L1->L2->L3).
-   Verify TTS timing.
-   Verify Win/Loss conditions.
