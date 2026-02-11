# ADHD_Gamer (Pike Brain House / 派派脑力屋)

**[English]** | [中文](#中文)

## Introduction

**ADHD_Gamer** (internally developed as *Pike Brain House*) is a gamified cognitive training application designed for children (specifically targeting 6-year-olds). It combines fun gameplay with scientifically grounded training methods to help improve attention span and working memory. The app features a cute, kid-friendly "Macaron" aesthetic and a 3D "Pike" character companion.

### Key Features

*   **Schulte Grid Challenge (Attention Training):** A classic attention span training game. Players must find numbers in sequence (1-9, 1-16, etc.) on a grid. Includes visual feedback and 3-star scoring based on speed.
*   **Blind Box Memory (Working Memory):** A memory game where players must memorize the contents of open boxes before they close, then answer questions about what was inside. Features a sequential "Open -> Memorize -> Close -> Quiz" loop.
*   **Custom Mode:** Allows parents or children to set up their own game rules (grid size, box count) using a simple, chalkboard-style interface.
*   **Hall of Fame:** A digital sticker album where players collect 3D ceramic-style figurines ("Honors") by achieving high scores.
*   **Kid-Friendly UI:** Vertical layout, warm "Macaron" color palette, large text, and voice encouragement (TTS).

### Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Architecture:** MVVM (Model-View-ViewModel) + Single Activity
*   **Persistence:** Room Database (Game Records & Honors), DataStore (Settings)
*   **Audio:** Android TextToSpeech (TTS), MediaPlayer
*   **Build System:** Gradle (Kotlin DSL)

### Getting Started

1.  Clone the repository.
2.  Open in Android Studio (Ladybug or newer recommended).
3.  Sync Gradle project.
4.  Run on an Android device or emulator (min SDK 24).

---

<a name="中文"></a>
## 中文介绍

**ADHD_Gamer** (项目代号 *派派脑力屋*) 是一款专为儿童设计的游戏化认知训练应用。它将趣味游戏与科学训练方法相结合，旨在通过轻松愉快的方式帮助提升孩子的注意力和工作记忆。应用采用了温馨的“马卡龙”配色风格和可爱的3D“派派”角色 IP。

### 核心功能

*   **舒尔特方格 (注意力训练):** 经典的注意力训练游戏。玩家需要在网格中按顺序（1-9, 1-16等）快速找出数字。包含点击反馈动画和基于速度的三星评分系统。
*   **盲盒记忆 (工作记忆):** 一个考验短时记忆的游戏。玩家需要记住盲盒打开时的物品，待盒子关闭后回答关于物品的问题。采用“展示 -> 记忆 -> 关闭 -> 提问”的线性流程，并针对低龄段优化了难度。
*   **自定义模式:** 允许家长或孩子通过简单的黑板手绘风格界面，自定义游戏的难度（如网格大小、盲盒数量）。
*   **荣誉墙:** 一个数字贴纸收集册。玩家通过闯关获得高分，解锁精美的3D陶瓷风格“荣誉”手办。
*   **儿童友好交互:** 竖屏设计，大字体，温馨配色，以及全程语音鼓励（TTS）。

### 技术栈

*   **编程语言:** Kotlin
*   **UI 框架:** Jetpack Compose (Material3)
*   **架构模式:** MVVM (Model-View-ViewModel) + 单 Activity
*   **数据存储:** Room Database (游戏记录与荣誉), DataStore (配置)
*   **音频:** Android 原生 TTS, MediaPlayer
*   **构建系统:** Gradle (Kotlin DSL)

### 如何运行

1.  克隆本项目到本地。
2.  使用 Android Studio 打开项目文件夹。
3.  等待 Gradle Sync 完成。
4.  连接 Android 设备或启动模拟器 (最低支持 Android 7.0 / SDK 24) 并运行。

---

*Verified & Polished by Agent Antigravity*
