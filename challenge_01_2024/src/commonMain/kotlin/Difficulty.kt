enum class DifficultyLevel {
    WARRIOR, SAIYAJIN, SUPER_SAIYAJIN
}

data class Difficulty(
    val level: DifficultyLevel = DifficultyLevel.WARRIOR,
    val enemyDensity: Int = 10,
    val enemyPower: Int = 2,
    val obstaclesDensity: Int = 10
)
