package com.litebyte.tiktaktok.contract

import android.widget.ImageButton

interface IGameBoard {
  fun setBoardButtons(buttonArray: ArrayList<ImageButton>)
  fun isStartUp(): Boolean
  fun getPaletteCoordinates(): IntArray
  fun getStatsCoordiantes(): IntArray
  fun getCurrentDifficulty(): Int
}