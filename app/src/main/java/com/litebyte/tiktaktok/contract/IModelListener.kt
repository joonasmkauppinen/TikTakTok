package com.litebyte.tiktaktok.contract

interface IModelListener {
  fun disableButtons()
  fun enableButtons(emptyCells: ArrayList<Int>)
  fun animateAiDrawable(index: Int)
  fun showResult(result: Int, combination: Int)
}