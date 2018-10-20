package com.litebyte.tiktaktok.contract

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

interface IFragmentToActivity {
  fun setHorizontalResultLinePosition(bias: Float)
  fun setVerticalResultLinePosition(bias: Float)

  fun paletteButton(): ImageButton
  fun difficultyButton(): ImageButton
  fun statsButton(): ImageButton
  fun resultText(): TextView
  fun difficultyToast(): TextView
  fun playAgainButton(): Button
  fun fadeView(): View
  fun resultLineDiagonal(): ImageView
  fun resultLineHorizontal(): ImageView
  fun resultLineVertical(): ImageView
}