package com.litebyte.tiktaktok.utils

object WinningCombinationsUtil {

  const val ROW_1 = 0
  const val ROW_2 = 1
  const val ROW_3 = 2
  const val COLUMN_1 = 3
  const val COLUMN_2 = 4
  const val COLUMN_3 = 5
  const val DIAGONAL_1 = 6
  const val DIAGONAL_2 = 7
  const val NO_VALID_COMBINATION = 8

  val winningCombinations = setOf(
          // rows
          intArrayOf(0,1,2), // 0
          intArrayOf(3,4,5), // 1
          intArrayOf(6,7,8), // 2

          // columns
          intArrayOf(0,3,6), // 3
          intArrayOf(1,4,7), // 4
          intArrayOf(2,5,8), // 5

          //diagonal
          intArrayOf(0,4,8), // 6
          intArrayOf(2,4,6)  // 7
  )

}