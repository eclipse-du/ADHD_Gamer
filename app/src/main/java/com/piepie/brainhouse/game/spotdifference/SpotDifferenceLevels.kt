package com.piepie.brainhouse.game.spotdifference

import com.piepie.brainhouse.R

data class DifferenceSpot(
    val x: Float,
    val y: Float,
    val radius: Float
)

data class SpotDifferenceLevel(
    val id: Int,
    val title: String,
    val leftImageRes: Int,
    val rightImageRes: Int,
    val requiredFinds: Int = 5,
    val spots: List<DifferenceSpot>
)

object SpotDifferenceLevels {
    private val rawLevels = listOf(
        SpotDifferenceLevel(
            id = 1,
            title = "公园小侦探",
            leftImageRes = R.drawable.spotdiff_park_a,
            rightImageRes = R.drawable.spotdiff_park_b,
            spots = listOf(
                DifferenceSpot(0.158f, 0.538f, 0.095f),
                DifferenceSpot(0.532f, 0.734f, 0.095f),
                DifferenceSpot(0.919f, 0.871f, 0.095f),
                DifferenceSpot(0.204f, 0.084f, 0.071f),
                DifferenceSpot(0.737f, 0.407f, 0.063f)
            )
        ),
        SpotDifferenceLevel(
            id = 2,
            title = "海滩小侦探 1",
            leftImageRes = R.drawable.spotdiff_beach1_a,
            rightImageRes = R.drawable.spotdiff_beach1_b,
            spots = listOf(
                DifferenceSpot(0.436f, 0.461f, 0.095f),
                DifferenceSpot(0.776f, 0.371f, 0.095f),
                DifferenceSpot(0.549f, 0.799f, 0.095f),
                DifferenceSpot(0.102f, 0.868f, 0.095f),
                DifferenceSpot(0.300f, 0.625f, 0.095f),
                DifferenceSpot(0.561f, 0.602f, 0.085f)
            )
        ),
        SpotDifferenceLevel(
            id = 3,
            title = "海滩小侦探 2",
            leftImageRes = R.drawable.spotdiff_beach2_a,
            rightImageRes = R.drawable.spotdiff_beach2_b,
            spots = listOf(
                DifferenceSpot(0.403f, 0.639f, 0.095f),
                DifferenceSpot(0.919f, 0.237f, 0.095f),
                DifferenceSpot(0.175f, 0.350f, 0.095f),
                DifferenceSpot(0.914f, 0.883f, 0.095f),
                DifferenceSpot(0.055f, 0.827f, 0.095f),
                DifferenceSpot(0.134f, 0.187f, 0.063f),
                DifferenceSpot(0.345f, 0.744f, 0.095f)
            )
        ),
        SpotDifferenceLevel(
            id = 4,
            title = "海滩小侦探 3",
            leftImageRes = R.drawable.spotdiff_beach3_a,
            rightImageRes = R.drawable.spotdiff_beach3_b,
            spots = listOf(
                DifferenceSpot(0.821f, 0.398f, 0.095f),
                DifferenceSpot(0.384f, 0.533f, 0.095f),
                DifferenceSpot(0.677f, 0.571f, 0.095f),
                DifferenceSpot(0.150f, 0.247f, 0.095f),
                DifferenceSpot(0.153f, 0.639f, 0.077f)
            )
        ),
        SpotDifferenceLevel(
            id = 5,
            title = "海滩小侦探 4",
            leftImageRes = R.drawable.spotdiff_beach4_a,
            rightImageRes = R.drawable.spotdiff_beach4_b,
            spots = listOf(
                DifferenceSpot(0.290f, 0.738f, 0.095f),
                DifferenceSpot(0.391f, 0.510f, 0.095f),
                DifferenceSpot(0.835f, 0.228f, 0.095f),
                DifferenceSpot(0.871f, 0.648f, 0.083f),
                DifferenceSpot(0.333f, 0.288f, 0.095f),
                DifferenceSpot(0.806f, 0.801f, 0.075f)
            )
        ),
        SpotDifferenceLevel(
            id = 6,
            title = "农场小侦探 1",
            leftImageRes = R.drawable.spotdiff_farm1_a,
            rightImageRes = R.drawable.spotdiff_farm1_b,
            spots = listOf(
                DifferenceSpot(0.265f, 0.516f, 0.095f),
                DifferenceSpot(0.722f, 0.866f, 0.083f),
                DifferenceSpot(0.255f, 0.919f, 0.095f),
                DifferenceSpot(0.058f, 0.274f, 0.095f),
                DifferenceSpot(0.151f, 0.366f, 0.057f),
                DifferenceSpot(0.940f, 0.716f, 0.059f),
                DifferenceSpot(0.196f, 0.242f, 0.055f)
            )
        ),
        SpotDifferenceLevel(
            id = 7,
            title = "农场小侦探 2",
            leftImageRes = R.drawable.spotdiff_farm2_a,
            rightImageRes = R.drawable.spotdiff_farm2_b,
            spots = listOf(
                DifferenceSpot(0.796f, 0.489f, 0.095f),
                DifferenceSpot(0.890f, 0.362f, 0.095f),
                DifferenceSpot(0.898f, 0.901f, 0.095f),
                DifferenceSpot(0.751f, 0.911f, 0.095f),
                DifferenceSpot(0.139f, 0.139f, 0.095f)
            )
        ),
        SpotDifferenceLevel(
            id = 8,
            title = "农场小侦探 3",
            leftImageRes = R.drawable.spotdiff_farm3_a,
            rightImageRes = R.drawable.spotdiff_farm3_b,
            spots = listOf(
                DifferenceSpot(0.381f, 0.573f, 0.095f),
                DifferenceSpot(0.153f, 0.554f, 0.095f),
                DifferenceSpot(0.929f, 0.274f, 0.095f),
                DifferenceSpot(0.795f, 0.574f, 0.091f),
                DifferenceSpot(0.751f, 0.424f, 0.095f),
                DifferenceSpot(0.230f, 0.144f, 0.095f)
            )
        ),
        SpotDifferenceLevel(
            id = 9,
            title = "农场小侦探 4",
            leftImageRes = R.drawable.spotdiff_farm4_a,
            rightImageRes = R.drawable.spotdiff_farm4_b,
            spots = listOf(
                DifferenceSpot(0.671f, 0.595f, 0.095f),
                DifferenceSpot(0.925f, 0.649f, 0.095f),
                DifferenceSpot(0.843f, 0.301f, 0.095f),
                DifferenceSpot(0.950f, 0.891f, 0.095f),
                DifferenceSpot(0.706f, 0.920f, 0.095f),
                DifferenceSpot(0.337f, 0.415f, 0.075f),
                DifferenceSpot(0.953f, 0.148f, 0.062f)
            )
        ),
        SpotDifferenceLevel(
            id = 10,
            title = "街道小侦探 1",
            leftImageRes = R.drawable.spotdiff_street1_a,
            rightImageRes = R.drawable.spotdiff_street1_b,
            spots = listOf(
                DifferenceSpot(0.785f, 0.076f, 0.095f),
                DifferenceSpot(0.891f, 0.538f, 0.095f),
                DifferenceSpot(0.736f, 0.799f, 0.077f),
                DifferenceSpot(0.076f, 0.417f, 0.095f),
                DifferenceSpot(0.805f, 0.254f, 0.083f)
            )
        ),
        SpotDifferenceLevel(
            id = 11,
            title = "街道小侦探 2",
            leftImageRes = R.drawable.spotdiff_street2_a,
            rightImageRes = R.drawable.spotdiff_street2_b,
            spots = listOf(
                DifferenceSpot(0.507f, 0.845f, 0.095f),
                DifferenceSpot(0.314f, 0.704f, 0.095f),
                DifferenceSpot(0.940f, 0.439f, 0.095f),
                DifferenceSpot(0.676f, 0.073f, 0.095f),
                DifferenceSpot(0.247f, 0.120f, 0.095f),
                DifferenceSpot(0.470f, 0.143f, 0.084f)
            )
        ),
        SpotDifferenceLevel(
            id = 12,
            title = "街道小侦探 3",
            leftImageRes = R.drawable.spotdiff_street3_a,
            rightImageRes = R.drawable.spotdiff_street3_b,
            spots = listOf(
                DifferenceSpot(0.603f, 0.832f, 0.095f),
                DifferenceSpot(0.365f, 0.690f, 0.095f),
                DifferenceSpot(0.697f, 0.405f, 0.091f),
                DifferenceSpot(0.859f, 0.430f, 0.079f),
                DifferenceSpot(0.663f, 0.209f, 0.095f),
                DifferenceSpot(0.959f, 0.337f, 0.067f),
                DifferenceSpot(0.227f, 0.333f, 0.062f)
            )
        ),
        SpotDifferenceLevel(
            id = 13,
            title = "街道小侦探 4",
            leftImageRes = R.drawable.spotdiff_street4_a,
            rightImageRes = R.drawable.spotdiff_street4_b,
            spots = listOf(
                DifferenceSpot(0.062f, 0.712f, 0.095f),
                DifferenceSpot(0.685f, 0.876f, 0.095f),
                DifferenceSpot(0.204f, 0.331f, 0.089f),
                DifferenceSpot(0.818f, 0.917f, 0.095f),
                DifferenceSpot(0.406f, 0.469f, 0.089f)
            )
        )
    )

    private val levels = rawLevels.map { level ->
        level.copy(
            requiredFinds = requiredFindsFor(level.id),
            spots = mergeFriendlySpots(level.spots, extraSpotsFor(level.id))
        )
    }

    fun getLevel(id: Int): SpotDifferenceLevel {
        return levels.find { it.id == id } ?: levels.first()
    }

    fun maxLevel(): Int = levels.size

    fun allLevels(): List<SpotDifferenceLevel> = levels

    private fun requiredFindsFor(id: Int): Int {
        return when ((id - 1) % 3) {
            0 -> 5
            1 -> 6
            else -> 7
        }
    }

    private fun mergeFriendlySpots(
        base: List<DifferenceSpot>,
        extras: List<DifferenceSpot>
    ): List<DifferenceSpot> {
        val merged = base.toMutableList()
        extras.forEach { candidate ->
            val overlaps = merged.any { existing ->
                kotlin.math.abs(existing.x - candidate.x) < 0.055f &&
                    kotlin.math.abs(existing.y - candidate.y) < 0.055f
            }
            if (!overlaps) merged += candidate
        }
        return merged
    }

    private fun extraSpotsFor(id: Int): List<DifferenceSpot> {
        return when (id) {
            1 -> listOf(
                DifferenceSpot(0.323f, 0.878f, 0.095f),
                DifferenceSpot(0.618f, 0.391f, 0.095f),
                DifferenceSpot(0.455f, 0.253f, 0.095f),
                DifferenceSpot(0.597f, 0.103f, 0.083f),
                DifferenceSpot(0.507f, 0.882f, 0.078f),
                DifferenceSpot(0.066f, 0.257f, 0.069f),
                DifferenceSpot(0.839f, 0.596f, 0.060f)
            )
            2 -> listOf(
                DifferenceSpot(0.915f, 0.675f, 0.095f),
                DifferenceSpot(0.802f, 0.853f, 0.095f),
                DifferenceSpot(0.318f, 0.051f, 0.095f),
                DifferenceSpot(0.426f, 0.213f, 0.095f),
                DifferenceSpot(0.772f, 0.690f, 0.079f),
                DifferenceSpot(0.754f, 0.138f, 0.069f)
            )
            3 -> listOf(
                DifferenceSpot(0.820f, 0.608f, 0.095f),
                DifferenceSpot(0.784f, 0.188f, 0.095f),
                DifferenceSpot(0.386f, 0.084f, 0.086f),
                DifferenceSpot(0.045f, 0.514f, 0.095f),
                DifferenceSpot(0.251f, 0.543f, 0.095f)
            )
            4 -> listOf(
                DifferenceSpot(0.236f, 0.248f, 0.095f),
                DifferenceSpot(0.714f, 0.783f, 0.095f),
                DifferenceSpot(0.849f, 0.114f, 0.078f),
                DifferenceSpot(0.181f, 0.916f, 0.084f),
                DifferenceSpot(0.929f, 0.330f, 0.083f),
                DifferenceSpot(0.625f, 0.267f, 0.060f)
            )
            5 -> listOf(
                DifferenceSpot(0.130f, 0.248f, 0.078f),
                DifferenceSpot(0.711f, 0.326f, 0.085f),
                DifferenceSpot(0.654f, 0.642f, 0.081f),
                DifferenceSpot(0.064f, 0.264f, 0.074f),
                DifferenceSpot(0.192f, 0.248f, 0.073f),
                DifferenceSpot(0.578f, 0.115f, 0.075f)
            )
            6 -> listOf(
                DifferenceSpot(0.647f, 0.316f, 0.095f),
                DifferenceSpot(0.722f, 0.586f, 0.080f),
                DifferenceSpot(0.642f, 0.742f, 0.078f),
                DifferenceSpot(0.645f, 0.642f, 0.074f),
                DifferenceSpot(0.968f, 0.514f, 0.058f)
            )
            7 -> listOf(
                DifferenceSpot(0.454f, 0.377f, 0.095f),
                DifferenceSpot(0.458f, 0.217f, 0.095f),
                DifferenceSpot(0.040f, 0.594f, 0.095f),
                DifferenceSpot(0.144f, 0.605f, 0.083f),
                DifferenceSpot(0.621f, 0.282f, 0.084f),
                DifferenceSpot(0.088f, 0.751f, 0.066f)
            )
            8 -> listOf(
                DifferenceSpot(0.903f, 0.728f, 0.091f),
                DifferenceSpot(0.379f, 0.758f, 0.076f),
                DifferenceSpot(0.612f, 0.767f, 0.074f),
                DifferenceSpot(0.855f, 0.160f, 0.074f),
                DifferenceSpot(0.233f, 0.378f, 0.070f)
            )
            9 -> listOf(
                DifferenceSpot(0.358f, 0.180f, 0.095f),
                DifferenceSpot(0.045f, 0.836f, 0.091f),
                DifferenceSpot(0.436f, 0.857f, 0.088f),
                DifferenceSpot(0.573f, 0.388f, 0.065f),
                DifferenceSpot(0.114f, 0.373f, 0.055f)
            )
            10 -> listOf(
                DifferenceSpot(0.579f, 0.761f, 0.095f),
                DifferenceSpot(0.072f, 0.778f, 0.095f),
                DifferenceSpot(0.719f, 0.291f, 0.095f),
                DifferenceSpot(0.193f, 0.036f, 0.095f),
                DifferenceSpot(0.441f, 0.219f, 0.086f),
                DifferenceSpot(0.202f, 0.485f, 0.086f)
            )
            11 -> listOf(
                DifferenceSpot(0.129f, 0.945f, 0.095f),
                DifferenceSpot(0.906f, 0.578f, 0.095f),
                DifferenceSpot(0.108f, 0.249f, 0.076f),
                DifferenceSpot(0.858f, 0.342f, 0.076f),
                DifferenceSpot(0.588f, 0.239f, 0.066f)
            )
            12 -> listOf(
                DifferenceSpot(0.138f, 0.777f, 0.095f),
                DifferenceSpot(0.402f, 0.398f, 0.083f),
                DifferenceSpot(0.925f, 0.630f, 0.094f),
                DifferenceSpot(0.204f, 0.199f, 0.063f),
                DifferenceSpot(0.817f, 0.689f, 0.055f)
            )
            13 -> listOf(
                DifferenceSpot(0.530f, 0.912f, 0.095f),
                DifferenceSpot(0.640f, 0.353f, 0.095f),
                DifferenceSpot(0.935f, 0.323f, 0.093f),
                DifferenceSpot(0.091f, 0.940f, 0.090f),
                DifferenceSpot(0.441f, 0.332f, 0.084f),
                DifferenceSpot(0.760f, 0.145f, 0.075f)
            )
            else -> emptyList()
        }
    }
}
