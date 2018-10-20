package com.litebyte.tiktaktok.model

import android.os.Handler
import com.litebyte.tiktaktok.contract.IModelListener
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_EASY
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_HARD
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_MEDIUM
import com.litebyte.tiktaktok.utils.GameResultUtil.RESULT_DEFEAT
import com.litebyte.tiktaktok.utils.GameResultUtil.RESULT_DRAW
import com.litebyte.tiktaktok.utils.GameResultUtil.RESULT_VICTORY
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.winningCombinations
import kotlin.collections.ArrayList

class Board(private val activity: IModelListener) {

  private var turnNumber = 0
  private var currentPlayer = Player.X
  private var winningCombination = 0
  private val cornerCells = intArrayOf(0,2,6,8)
  private val edgeCells = intArrayOf(1,3,5,7)

  private var previousEdgeCell = 1
  private var previousCornerCell = 0
  private var previousTurn4Cell = 0

  private var cells = ArrayList<String>()
  private var currentDifficulty = 0

  init {
    for (i in 0..8) cells.add(i, "")
  }

  fun resetModel() {
    turnNumber = 0
    currentPlayer = Player.X
    for (i in 0..8) cells[i] = ""
  }

  fun setDifficulty(difficulty: Int){
    currentDifficulty = difficulty
  }

  fun markPlayerMove(index: Int) {
    currentPlayer = Player.X
    turnNumber++
    cells[index] = currentPlayer.toString()
    activity.disableButtons()

    if ( gameHasResult() ) {
      activity.disableButtons()
      activity.showResult( RESULT_VICTORY, winningCombination )
    } else if ( !gameHasResult() && turnNumber < 9 ) {
      Handler().postDelayed({
        markAiTurn()
      }, 600)
    } else if (turnNumber == 9) {
      activity.showResult( RESULT_DRAW, winningCombination )
    }
  }

  private fun markAiTurn() {
    currentPlayer = Player.O
    turnNumber++

    var cellIndex = 0
    when (currentDifficulty) {
      DIFFICULTY_EASY   -> cellIndex = difficultyEasy()
      DIFFICULTY_MEDIUM -> cellIndex = difficultyMedium()
      DIFFICULTY_HARD   -> cellIndex = difficultyHard()
    }

    cells[cellIndex] = currentPlayer.toString()

    activity.enableButtons(getEmptyCells())
    activity.animateAiDrawable(cellIndex)

    if (gameHasResult()) {
      activity.disableButtons()
      activity.showResult( RESULT_DEFEAT, winningCombination )
    }
  }

  private fun difficultyEasy(): Int {
    return prioritiseBlocking(false)
  }
  private fun difficultyMedium(): Int {
    return prioritiseWinning()
  }
  private fun difficultyHard(): Int {
    return prioritiseWinning()
  }

  private fun prioritiseBlocking(checkCenter: Boolean): Int {
    if (cells[4].isEmpty() && checkCenter) return 4
    var cellIndex = 0
    for (combination in winningCombinations) {
      var blockPlayer = 0
      var cellsInCombinationOccupied = 0
      for (index in combination) {

        if (cells[index].isNotEmpty()) {
          cellsInCombinationOccupied++
          if (cells[index] == "X") blockPlayer++
        } else {
          cellIndex = index
        }

      }
      if (blockPlayer == 2 && cellsInCombinationOccupied != 3) return cellIndex
    }

    if (turnNumber == 4) {
      var newTurn4Cell: Int
      do { newTurn4Cell = randomEmptyCell() } while (newTurn4Cell == previousTurn4Cell)
      previousTurn4Cell = newTurn4Cell
      return newTurn4Cell
    }

    return randomEmptyCell()
  }
  private fun prioritiseWinning(): Int {
    var cellIndex = 0

    for (combination in winningCombinations) {
      var winningMove = 0
      var xInCombination = 0
      for (index in combination) {
        when (cells[index]) {
          ""  -> cellIndex = index
          "O" -> winningMove++
          "X" -> xInCombination++
        }
      }
      if (winningMove == 2 && xInCombination == 0) return cellIndex
    }

    when (turnNumber) {
      2 -> {
        if (cells[4].isEmpty()) return 4
        return randomCornerCell()
      }
      4 -> {
        val diagonals = setOf( winningCombinations.elementAt(6), winningCombinations.elementAt(7) )
        for (combination in diagonals) {
          var diagonalVal = ""
          for (index in combination) {
            diagonalVal += cells[index]
          }

          if (diagonalVal == "XOX") {
            var edgeCell: Int
            do { edgeCell = randomEdgeCell() } while(edgeCell == previousEdgeCell)
            previousEdgeCell = edgeCell
            return edgeCell
          } else if (diagonalVal == "XXO" || diagonalVal == "OXX") {
            var cornerCell: Int
            do { cornerCell = randomCornerCell() } while(cornerCell == previousCornerCell)
            previousCornerCell = cornerCell
            return cornerCell
          }
        }
        if (currentDifficulty == DIFFICULTY_HARD) {
          when (true) {
            checkCellsForX(1,8) -> return randomEmptyCellExcluding(6,7)
            checkCellsForX(5,6) -> return randomEmptyCellExcluding(0,3)
            checkCellsForX(0,7) -> return randomEmptyCellExcluding(1,2)
            checkCellsForX(2,3) -> return randomEmptyCellExcluding(5,8)

            checkCellsForX(1,6) -> return randomEmptyCellExcluding(7,8)
            checkCellsForX(0,5) -> return randomEmptyCellExcluding(3,6)
            checkCellsForX(2,7) -> return randomEmptyCellExcluding(0,1)
            checkCellsForX(3,8) -> return randomEmptyCellExcluding(2,5)

            checkCellsForX(1,3) -> return 0
            checkCellsForX(1,5) -> return 2
            checkCellsForX(3,7) -> return 6
            checkCellsForX(5,7) -> return 8
          }
        }
      }
    }
    return prioritiseBlocking(true)
  }

  private fun randomEmptyCell(): Int {
    var index: Int
    do {
      index = (0..8).shuffled().last()
    } while (cells[index].isNotEmpty())
    return index
  }
  private fun randomCornerCell(): Int {
    var cellIndex: Int
    do {
      cellIndex = cornerCells[(0..3).shuffled().last()]
    } while(cells[cellIndex].isNotEmpty())
    return cellIndex
  }
  private fun randomEdgeCell(): Int {
    var cellIndex: Int
    do {
      cellIndex = edgeCells[(0..3).shuffled().last()]
    } while(cells[cellIndex].isNotEmpty())
    return cellIndex
  }
  private fun randomEmptyCellExcluding(i1: Int, i2: Int): Int {
    var index: Int
    do {
      index = randomEmptyCell()
    } while(index==i1 || index==i2)
    return index
  }
  private fun checkCellsForX(i1:Int, i2: Int): Boolean {
    return (cells[i1]=="X" && cells[i2]=="X")
  }

  private fun gameHasResult(): Boolean {
    var hasResult = false
    winningCombination = 0
    for (combination in winningCombinations) {
      var threeInRow = 0
      for (index in combination) {
        if ( cells[index] == currentPlayer.toString() ) threeInRow++
      }
      if (threeInRow == 3) {
        hasResult = true
        break
      }
      winningCombination++
    }
    return hasResult
  }

  private fun getEmptyCells(): ArrayList<Int> {
    val emptyCells: ArrayList<Int> = ArrayList()
    for (i in 0..8) {
      if (cells[i] == "")
        emptyCells.add(i)
    }
    return emptyCells
  }

}