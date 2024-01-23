import korlibs.event.*
import korlibs.image.bitmap.*
import korlibs.time.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.paint.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.image.vector.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.korge.render.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*
import korlibs.math.geom.vector.*
import korlibs.math.interpolation.*

const val gridSize = 10
val game = DragonRadar(10, difficulty = Difficulty())

suspend fun main() = Korge(windowSize = Size(700, 600), backgroundColor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val cellSize = (views.virtualWidth / gridSize.toFloat()) / 1.5
        val fieldSize = 30 + gridSize * cellSize
        val leftIndent = (views.virtualWidth - fieldSize) / 2
        val topIndent = (views.virtualHeight - fieldSize) / 2

        val bgField = roundRect(Size(fieldSize, fieldSize), RectCorners(5.0), fill = Colors["#52b788"]) {
            position(leftIndent, topIndent)
        }

        addChild(bgField)

        // Draw radar-style grid lines
        renderBoard(game, cellSize, leftIndent, topIndent, bgField)

        sceneContainer.keys {
            down {
                if (!game.isGameOver()) {
                    when (it.key) {
                        Key.LEFT -> game.movePlayer(Direction.UP)
                        Key.RIGHT -> game.movePlayer(Direction.DOWN)
                        Key.UP -> game.movePlayer(Direction.LEFT)
                        Key.DOWN -> game.movePlayer(Direction.RIGHT)
                        else -> Unit
                    }
                    renderBoard(game, cellSize, leftIndent, topIndent, bgField)
                }
            }
        }
    }

    private suspend fun renderBoard(
        game: DragonRadar,
        cellSize: Double,
        leftIndent: Double,
        topIndent: Double,
        bgField: RoundRect
    ) {
        val bgEnergy =
            sceneContainer.roundRect(Size(cellSize * 5, cellSize), RectCorners(5.0), fill = Colors["#bbae9e"]) {
                alignRightToRightOf(bgField)
                alignTopToTopOf(bgField, -50)
            }

        val bgScore =
            sceneContainer.roundRect(Size(cellSize * 5, cellSize), RectCorners(5.0), fill = Colors["#56ae9e"]) {
                alignLeftToLeftOf(bgField)
                alignTopToTopOf(bgField, -50)
            }
        val font = resourcesVfs["ClearSans.ttf"].readFont()

        sceneContainer.addChild(bgScore)

        game.grid.forEachIndexed { x, row ->
            row.forEachIndexed { y, cell ->
                val size = Size(cellSize, cellSize)
                val background = sceneContainer.roundRect(size, RectCorners(5.0), fill = Colors["04e762"]) {
                    position(leftIndent + 10.0 + (1 + cellSize) * x, topIndent + 10.0 + (1 + cellSize) * y)
                }

                var image: Bitmap? = null
                if (game.shouldRenderPlayer(x, y)) {
                    image = resourcesVfs["player.png"].readBitmap()
                } else if (cell.content == CellStatus.EMPTY || cell.content == CellStatus.COLLECTED) sceneContainer.addChild(
                    background
                )
                else {
                    image = resourcesVfs["${cell.content}.png"].readBitmap()
                }

                if (image != null) {
                    val cellBlock = sceneContainer.container {
                        image(image) {
                            size(35, 35)
                            centerOn(background)
                        }
                    }
                    sceneContainer.addChild(cellBlock)
                }
            }
        }

        if (game.isGameOver()) {
            val bgWin =
                sceneContainer.roundRect(Size(cellSize * 10, cellSize * 4), RectCorners(5.0), fill = Colors["#ff6d00"]) {
                    centerOn(bgField)
                }
            sceneContainer.text("You win!", cellSize * 2, RGBA(100, 120, 210), font) {
                centerXOn(bgWin)
                alignTopToTopOf(bgWin, 5.0)
            }
        }

        sceneContainer.text("Energy: ${game.playerEnergy}", cellSize * 0.5, RGBA(239, 226, 210), font) {
            centerXOn(bgEnergy)
            alignTopToTopOf(bgEnergy, 5.0)
        }
        sceneContainer.text("Collected: ${game.dragonBallsCollected}", cellSize * 0.5, RGBA(239, 226, 210), font) {
            centerXOn(bgScore)
            alignTopToTopOf(bgScore, 5.0)
        }
    }
}
