package com.litebyte.tiktaktok.view

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.litebyte.tiktaktok.R
import com.litebyte.tiktaktok.contract.IFragmentToActivity
import com.litebyte.tiktaktok.contract.IGameBoard
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_EASY
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_HARD
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_MEDIUM
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_dark.*
import java.lang.Exception

class MainDarkFragment : Fragment(), IFragmentToActivity {

  private lateinit var parentActivity: IGameBoard
  private var difficulty = -1

  companion object {
    fun newInstance(): MainDarkFragment {
      return MainDarkFragment()
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    parentActivity = context as IGameBoard
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_main_dark, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    parentActivity.setBoardButtons( initButtonArray() )

    // Animate only when changing theme
    if (!parentActivity.isStartUp()) revealAnim( parentActivity.getPaletteCoordinates() )
  }

  override fun onStart() {
    super.onStart()
    difficulty = parentActivity.getCurrentDifficulty()
    loadDifficultyAvd(difficulty)
  }

  override fun setHorizontalResultLinePosition(bias: Float) {
    val set = ConstraintSet()
    set.clone(board_dark)
    set.setVerticalBias(R.id.result_line_horizontal_dark, bias)
    set.applyTo(board_dark)
  }
  override fun setVerticalResultLinePosition(bias: Float) {
    val set = ConstraintSet()
    set.clone(board_dark)
    set.setHorizontalBias(R.id.result_line_vertical_dark, bias)
    set.applyTo(board_dark)
  }
  override fun paletteButton(): ImageButton {
    return imageButton_palette_dark
  }
  override fun difficultyButton(): ImageButton {
    return imageButton_difficulty_dark
  }
  override fun statsButton(): ImageButton {
    return imageButton_stats_dark
  }
  override fun resultText(): TextView {
    return textView_game_result_dark
  }
  override fun playAgainButton(): Button {
    return button_play_again_dark
  }
  override fun fadeView(): View {
    return fade_view_dark
  }
  override fun resultLineDiagonal(): ImageView {
    return result_line_diagonal_dark
  }
  override fun resultLineHorizontal(): ImageView {
    return result_line_horizontal_dark
  }
  override fun resultLineVertical(): ImageView {
    return result_line_vertical_dark
  }
  override fun difficultyToast(): TextView {
    return textView_difficulty_dark
  }

  private fun loadDifficultyAvd(difficulty: Int) {
      when (difficulty) {
        DIFFICULTY_EASY   -> imageButton_difficulty_dark.setImageResource(R.drawable.ic_difficulty_easy)
        DIFFICULTY_MEDIUM -> imageButton_difficulty_dark.setImageResource(R.drawable.ic_difficulty_medium)
        DIFFICULTY_HARD   -> imageButton_difficulty_dark.setImageResource(R.drawable.ic_difficulty_hard)
        else -> throw Exception("Couldn't load difficulty.")
      }

  }

  private fun revealAnim(coordinates: IntArray) {

    val cx = coordinates[0] + 36
    val cy = coordinates[1] + 88

    val pow = 2.0
    val screenWidth: Double = activity!!.container.width.toDouble()
    val screenHeight: Double = activity!!.container.height.toDouble()
    val endRadius: Float = Math.sqrt( Math.pow(screenWidth, pow) + Math.pow(screenHeight, pow) ).toFloat()

    val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, endRadius)
    anim.duration = 800
    anim.interpolator = FastOutSlowInInterpolator()
    anim.start()

  }

  private fun initButtonArray(): ArrayList<ImageButton> {
    val boardButtons: ArrayList<ImageButton> = ArrayList()
    boardButtons.add(button0_dark)
    boardButtons.add(button1_dark)
    boardButtons.add(button2_dark)
    boardButtons.add(button3_dark)
    boardButtons.add(button4_dark)
    boardButtons.add(button5_dark)
    boardButtons.add(button6_dark)
    boardButtons.add(button7_dark)
    boardButtons.add(button8_dark)
    return boardButtons
  }

}