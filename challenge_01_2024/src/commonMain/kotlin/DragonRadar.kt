import kotlin.math.abs
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

class DragonRadar(private val gridSize: Int, private val difficulty: Difficulty) {
    var playerEnergy = 100
    var dragonBallsCollected = 0
    private var dragonBallCount = 7

    private val dragonBalls = mutableMapOf<Int, Pair<Int, Int>>()
    private val enemies = mutableMapOf<Int, Pair<Int, Int>>()
    private val obstacles = mutableMapOf<Int, Pair<Int, Int>>()
    private val playerPos = PlayerPosition(1, 1)
    val grid: Array<Array<Cell>> = Array(gridSize) { Array(gridSize) { Cell(CellStatus.EMPTY) } }

    init {
        if (gridSize < 10) throw IllegalArgumentException("Grid size must be at least 10x10.")
        placePlayer(gridSize / 2, gridSize / 2)
        placeDragonBalls()
        placeEnemies()
        placeObstacles()
    }

    fun shouldRenderPlayer(x: Int, y: Int): Boolean {
        return x == playerPos.x && y == playerPos.y
    }

    fun movePlayer(direction: Direction) {
        val (x, y) = playerPos
        when (direction) {
            Direction.UP -> if (x > 0 && !isObstacle(x - 1, y)) playerPos.x = x - 1
            Direction.DOWN -> if (x < gridSize - 1 && !isObstacle(x + 1, y)) playerPos.x = x + 1
            Direction.LEFT -> if (y > 0 && !isObstacle(x, y - 1)) playerPos.y = y - 1
            Direction.RIGHT -> if (y < gridSize - 1 && !isObstacle(x, y + 1)) playerPos.y = y + 1
        }

        val content = grid[playerPos.x][playerPos.y].content
        when (content) {
            CellStatus.BALL -> collectBall()
            CellStatus.ENEMY -> playerEnergy -= difficulty.enemyPower
            else -> {}
        }
        updateRadar()
    }

    fun isGameOver(): Boolean {
        return dragonBallsCollected == dragonBallCount
    }

    private fun isObstacle(x: Int, y: Int): Boolean {
        return obstacles.any { it.value.first == x && it.value.second == y }
    }

    private fun collectBall() {
        dragonBallsCollected++
        grid[playerPos.x][playerPos.y] = Cell(CellStatus.COLLECTED)
    }

    private fun placePlayer(x: Int, y: Int) {
        playerPos.x = x
        playerPos.y = y
        updateRadar()
    }

    private fun placeDragonBalls() {
        for (i in 1..dragonBallCount) {
            var x: Int
            var y: Int
            do {
                x = Random.nextInt(gridSize)
                y = Random.nextInt(gridSize)
            } while (grid[x][y].content != CellStatus.EMPTY)

            grid[x][y] = Cell(CellStatus.BALL)
            dragonBalls[i] = Pair(x, y)
        }
        updateRadar()
    }

    private fun isAdjacentToDragonBall(x: Int, y: Int): Boolean {
        // Check if the given position is adjacent to any Dragon Ball
        return dragonBalls.any { (_, coords) ->
            val (dx, dy) = coords
            abs(x - dx) <= 1 && abs(y - dy) <= 1
        } && dragonBalls.none { (_, coords) ->
            val (dx, dy) = coords
            dx != x && dy != y
        }
    }

    private fun isAdjacentToEnemy(x: Int, y: Int): Boolean {
        // Check if the given position is adjacent to any Enemy
        return enemies.any { (_, coords) ->
            val (dx, dy) = coords
            abs(x - dx) <= 1 && abs(y - dy) <= 1
        }
    }

    private fun placeEnemies() {
        val maxEnemies = gridSize * gridSize / difficulty.enemyDensity // Adjust the divisor for desired enemy density
        val numberOfEnemies = Random.nextInt(1, maxEnemies + 1)

        repeat(numberOfEnemies) {
            var x: Int
            var y: Int
            do {
                x = Random.nextInt(gridSize)
                y = Random.nextInt(gridSize)
            } while (grid[x][y].content != CellStatus.EMPTY && !isAdjacentToDragonBall(x, y))
            grid[x][y] = Cell(CellStatus.ENEMY)
            enemies[it] = Pair(x, y)
        }
    }

    private fun placeObstacles() {
        val maxObstacles = gridSize * gridSize / difficulty.obstaclesDensity // Adjust the divisor for desired enemy density
        val numberOfObstacles = Random.nextInt(1, maxObstacles + 1)

        repeat(numberOfObstacles) {
            var x: Int
            var y: Int
            do {
                x = Random.nextInt(gridSize)
                y = Random.nextInt(gridSize)
            } while (grid[x][y].content != CellStatus.EMPTY && !isAdjacentToDragonBall(x, y) && !isAdjacentToEnemy(x, y))
            grid[x][y] = Cell(CellStatus.OBSTACLE)
            obstacles[it] = Pair(x, y)
        }
    }

    // updateRadar is responsible for displaying information about
    // the location of Dragon Balls relative to the player's current position.
    // It calculates the distance and direction of each Dragon Ball from the player
    private fun updateRadar() {
        val (px, py) = playerPos
        println("Player Energy: $playerEnergy")
        dragonBalls.forEach { (id, coords) ->
            val (dx, dy) = coords

            // Calculate Manhattan distance (sum of horizontal and vertical distances)
            val distanceY = abs(px - dx)
            val distanceX = abs(py - dy)

            // Determine the direction of the Dragon Ball relative to the player
            val directionX = if (dx > px) Direction.DOWN else if (dx < px) Direction.UP else ""
            val directionY = if (dy > py) Direction.RIGHT else if (dy < py) Direction.LEFT else ""

            println("Dragon Ball $id - Distance: $distanceX (X), $distanceY (Y), Direction: $directionY $directionX")
        }
        println()
    }
}
