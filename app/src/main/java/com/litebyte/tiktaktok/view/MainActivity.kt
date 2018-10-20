package com.litebyte.tiktaktok.view

import android.content.SharedPreferences
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.litebyte.tiktaktok.R
import com.litebyte.tiktaktok.contract.IFragmentToActivity
import com.litebyte.tiktaktok.contract.IGameBoard
import com.litebyte.tiktaktok.contract.IModelListener
import com.litebyte.tiktaktok.model.Board
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_EASY
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_HARD
import com.litebyte.tiktaktok.utils.DifficultyUtil.DIFFICULTY_MEDIUM
import com.litebyte.tiktaktok.utils.GameResultUtil.RESULT_DEFEAT
import com.litebyte.tiktaktok.utils.GameResultUtil.RESULT_DRAW
import com.litebyte.tiktaktok.utils.GameResultUtil.RESULT_VICTORY
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.ROW_1
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.ROW_2
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.ROW_3
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.COLUMN_1
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.COLUMN_2
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.COLUMN_3
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.DIAGONAL_1
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.DIAGONAL_2
import com.litebyte.tiktaktok.utils.WinningCombinationsUtil.NO_VALID_COMBINATION
import kotlin.collections.ArrayList

private const val THEME_LIGHT = 0
private const val THEME_DARK  = 1

class MainActivity : AppCompatActivity(), IModelListener, IGameBoard {

  private lateinit var mAdView: AdView

  private lateinit var model: Board
  private lateinit var preferences: SharedPreferences

  private var difficulty = 0
  private var currentTheme = 0
  private var startUp = true
  private var boardButtons: ArrayList<ImageButton> = ArrayList()
  private val paletteCoordinates = IntArray(2)
  private var statsCoordinates = IntArray(2)
  private var playing = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    MobileAds.initialize(this, "ca-app-pub-5172345594852087~9251039109")

    mAdView = findViewById(R.id.adView)
    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)
    val anim = AnimationUtils.loadAnimation(this, R.anim.ad_view_reveal)
    mAdView.adListener = object : AdListener() {
      override fun onAdLoaded() {
        super.onAdLoaded()
        mAdView.startAnimation(anim)
      }
    }

    preferences = PreferenceManager.getDefaultSharedPreferences(this)

    difficulty = preferences.getInt(getString(R.string.pref_difficulty), 0)
    currentTheme = preferences.getInt(getString(R.string.pref_theme), 0)

    model = Board(this)
    model.setDifficulty(difficulty)

    initFragment(currentTheme)
  }

  override fun onResume() {
    super.onResume()
    val showTutorial = preferences.getBoolean( getString(R.string.pref_show_tutorial), true )
    if (showTutorial) showTutorialDialog()
    startUp = false
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) hideSystemUI()
  }
  private fun hideSystemUI() {
    window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
  }

  override fun isStartUp(): Boolean {
    return startUp
  }
  override fun getPaletteCoordinates(): IntArray {
    return paletteCoordinates
  }
  override fun getStatsCoordiantes(): IntArray {
    return statsCoordinates
  }
  override fun getCurrentDifficulty(): Int {
    return difficulty
  }

  override fun showResult(result: Int, combination: Int) {
    updateStats(result)
    checkCombination(combination)
    when (result) {
      RESULT_DRAW    -> resultDraw()
      RESULT_DEFEAT  -> resultDefeat()
      RESULT_VICTORY -> resultVictory()
    }
    Handler().postDelayed({
      currentFragment().playAgainButton().visibility = View.VISIBLE
      val anim = AnimationUtils.loadAnimation(this, R.anim.play_again_reveal_anim)
      currentFragment().playAgainButton().startAnimation(anim)
      revealActionButtons()
    }, 1500)

  }
  override fun disableButtons() {
    for (button in boardButtons) {
      button.isEnabled = false
    }
  }
  override fun enableButtons(emptyCells: ArrayList<Int>) {
    for (cell in emptyCells) {
      boardButtons[cell].isEnabled = true
    }
  }
  override fun setBoardButtons(buttonArray: ArrayList<ImageButton>) {
    boardButtons = buttonArray
  }

  override fun animateAiDrawable(index: Int) {
    val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.circle)
    val button = boardButtons[index]
    button.visibility = View.VISIBLE
    button.setImageDrawable(avd)
    button.isEnabled = false
    avd?.start()
  }
  fun animatePlayerDrawable(v: View) {
    if (!playing) hideActionButtons()
    if (v is ImageButton) {
      val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.cross)
      v.visibility = View.VISIBLE
      v.setImageDrawable(avd)
      avd?.start()
      v.isEnabled = false

      val buttonTag: Int = v.tag.toString().toInt()
      model.markPlayerMove(buttonTag)
    }
  }

  fun onChangeTheme(v: View) {
    if (v is ImageButton) {

      model.resetModel()
      v.getLocationInWindow(paletteCoordinates)

      when (currentTheme) {
        THEME_LIGHT -> {
          currentTheme = THEME_DARK
          preferences.edit()
                  .putInt(getString(R.string.pref_theme), currentTheme)
                  .apply()
          initFragment(currentTheme)
        }
        THEME_DARK -> {
          currentTheme = THEME_LIGHT
          preferences.edit()
                  .putInt(getString(R.string.pref_theme), currentTheme)
                  .apply()
          initFragment(currentTheme)
        }
      }
    }
  }
  fun onChangeDifficulty(v: View) {
    if (v is ImageButton) {
      val anim = AnimationUtils.loadAnimation(this, R.anim.rotate_difficulty_button)
      v.startAnimation(anim)
      setDifficultyAvd(difficulty)
      setDifficultyText(difficulty)
      model.setDifficulty(difficulty)
      preferences.edit()
              .putInt(getString(R.string.pref_difficulty), difficulty)
              .apply()

    }
  }
  fun onViewStats(v: View) {
    if (v is ImageButton) {
      v.getLocationOnScreen(statsCoordinates)
      supportFragmentManager.beginTransaction()
              .addToBackStack(null)
              .add(R.id.container, StatsFragment.newInstance(), "STATS_FRAGMENT")
              .commit()
    }
  }
  fun onPlayAgain(v: View) {
    if (v is Button) {
      val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim)
      currentFragment().fadeView().visibility = View.VISIBLE
      currentFragment().fadeView().startAnimation(anim)
      Handler().postDelayed({
        resetGame()
      }, 300)
    }
  }

  private fun resetGame() {
    model.resetModel()
    currentFragment().resultText().visibility = View.INVISIBLE
    currentFragment().playAgainButton().visibility = View.INVISIBLE
    currentFragment().resultLineDiagonal().setImageDrawable(null)
    currentFragment().resultLineHorizontal().setImageDrawable(null)
    currentFragment().resultLineVertical().setImageDrawable(null)

    for (button in boardButtons) {
      button.setImageDrawable(null)
      button.isEnabled = true
    }

    val anim = AnimationUtils.loadAnimation(this, R.anim.fade_out_anim)
    currentFragment().fadeView().startAnimation(anim)
    currentFragment().fadeView().visibility = View.INVISIBLE
  }

  private fun hideActionButtons() {
    playing = true
    val animFast = AnimationUtils.loadAnimation(this, R.anim.transition_up_fast)
    val anim = AnimationUtils.loadAnimation(this, R.anim.transition_up)

    currentFragment().difficultyButton().visibility = View.INVISIBLE
    currentFragment().paletteButton().visibility = View.INVISIBLE
    currentFragment().statsButton().visibility = View.INVISIBLE

    currentFragment().difficultyButton().startAnimation(animFast)
    currentFragment().paletteButton().startAnimation(anim)
    currentFragment().statsButton().startAnimation(anim)
  }
  private fun revealActionButtons() {
    playing = false
    val animFast = AnimationUtils.loadAnimation(this, R.anim.transition_down_fast)
    val anim = AnimationUtils.loadAnimation(this, R.anim.transition_down)

    currentFragment().difficultyButton().visibility = View.VISIBLE
    currentFragment().paletteButton().visibility = View.VISIBLE
    currentFragment().statsButton().visibility = View.VISIBLE

    currentFragment().difficultyButton().startAnimation(animFast)
    currentFragment().paletteButton().startAnimation(anim)
    currentFragment().statsButton().startAnimation(anim)
  }

  private fun setDifficultyAvd(difficulty: Int) {
    val button = currentFragment().difficultyButton()
    when (difficulty) {
      DIFFICULTY_EASY   -> {
        this.difficulty = DIFFICULTY_MEDIUM
        val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.transition_easy_to_medium)
        button.setImageDrawable(avd)
        avd?.start()
      }
      DIFFICULTY_MEDIUM -> {
        this.difficulty = DIFFICULTY_HARD
        val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.transition_medium_to_hard)
        button.setImageDrawable(avd)
        avd?.start()
      }
      DIFFICULTY_HARD   -> {
        this.difficulty = DIFFICULTY_EASY
        val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.transition_hard_to_easy)
        button.setImageDrawable(avd)
        avd?.start()
      }
    }
  }
  private fun setDifficultyText(difficulty: Int) {
    val anim = AnimationUtils.loadAnimation(this, R.anim.difficulty_text_reveal)
    val textView = currentFragment().difficultyToast()
    textView.visibility = View.VISIBLE
    when (difficulty) {
      DIFFICULTY_EASY -> {
        textView.text = getString(R.string.text_difficulty_easy)
        textView.startAnimation(anim)
      }
      DIFFICULTY_MEDIUM -> {
        textView.text = getString(R.string.text_difficulty_medium)
        textView.startAnimation(anim)
      }
      DIFFICULTY_HARD -> {
        textView.text = getString(R.string.text_difficulty_hard)
        textView.startAnimation(anim)
      }
    }
    Handler().postDelayed({
      val fadeOutAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
      textView.visibility = View.INVISIBLE
      textView.startAnimation(fadeOutAnim)
    }, 500)
  }

  private fun checkCombination(combination: Int) {
    when (combination) {
      ROW_1 -> showRow1(currentFragment())
      ROW_2 -> showRow2(currentFragment())
      ROW_3 -> showRow3(currentFragment())
      COLUMN_1 -> showColumn1(currentFragment())
      COLUMN_2 -> showColumn2(currentFragment())
      COLUMN_3 -> showColumn3(currentFragment())
      DIAGONAL_1 -> showDiagonal1()
      DIAGONAL_2 -> showDiagonal2()
      NO_VALID_COMBINATION -> { /* Do nothing */ }
    }
  }

  private fun showRow1(board: IFragmentToActivity?) {
    board!!.setHorizontalResultLinePosition(0f)
    initHorizontalResultLine()
  }
  private fun showRow2(board: IFragmentToActivity?) {
    board!!.setHorizontalResultLinePosition(0.5f)
    initHorizontalResultLine()
  }
  private fun showRow3(board: IFragmentToActivity?) {
    board!!.setHorizontalResultLinePosition(1f)
    initHorizontalResultLine()
  }

  private fun showColumn1(board: IFragmentToActivity?) {
    board!!.setVerticalResultLinePosition(0f)
    initVerticalResultLine()
  }
  private fun showColumn2(board: IFragmentToActivity?) {
    board!!.setVerticalResultLinePosition(0.5f)
    initVerticalResultLine()
  }
  private fun showColumn3(board: IFragmentToActivity?) {
    board!!.setVerticalResultLinePosition(1f)
    initVerticalResultLine()
  }

  private fun showDiagonal1() {
    currentFragment().resultLineDiagonal().rotation = 90f
    initDiagonalResultLine()
  }
  private fun showDiagonal2() {
    currentFragment().resultLineDiagonal().rotation = 0f
    initDiagonalResultLine()
  }

  private fun initVerticalResultLine() {
    val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.result_line_vertical)
    currentFragment().resultLineVertical().setImageDrawable(avd)
    avd?.start()
  }
  private fun initHorizontalResultLine() {
    val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.result_line_horizontal)
    currentFragment().resultLineHorizontal().setImageDrawable(avd)
    avd?.start()
  }
  private fun initDiagonalResultLine() {
    val avd = AnimatedVectorDrawableCompat.create(this, R.drawable.result_line_diagonal)
    currentFragment().resultLineDiagonal().setImageDrawable(avd)
    avd?.start()
  }

  private fun resultDraw() {
    setupResultView(
            stringId = R.string.result_draw,
            drawableId = R.drawable.result_draw_background
    )
    Handler().postDelayed({
      val anim = AnimationUtils.loadAnimation(this, R.anim.result_draw)
      currentFragment().resultText().visibility = View.VISIBLE
      currentFragment().resultText().startAnimation(anim)
    }, 1000)
  }
  private fun resultDefeat() {
    setupResultView(
            stringId = R.string.result_defeat,
            drawableId = R.drawable.result_defeat_background
    )
    Handler().postDelayed({
      val anim = AnimationUtils.loadAnimation(this, R.anim.result_defeat)
      currentFragment().resultText().visibility = View.VISIBLE
      currentFragment().resultText().startAnimation(anim)
    }, 1000)
  }
  private fun resultVictory() {
    setupResultView(
            stringId = R.string.result_victory,
            drawableId = R.drawable.result_victory_background
    )
    Handler().postDelayed({
      val anim = AnimationUtils.loadAnimation(this, R.anim.result_victory)
      currentFragment().resultText().visibility = View.VISIBLE
      currentFragment().resultText().startAnimation(anim)
    }, 1000)
  }

  private fun setupResultView(stringId: Int, drawableId: Int) {
    val textView = currentFragment().resultText()
    textView.text = getString(stringId)
    textView.background = getDrawable(drawableId)

  }

  private fun updateStats(result: Int) {
    when (difficulty) {
      DIFFICULTY_EASY   -> updateEasyTable(result)
      DIFFICULTY_MEDIUM -> updateMediumTable(result)
      DIFFICULTY_HARD   -> updateHardTable(result)
    }
  }
  private fun updateEasyTable(result: Int) {
    when (result) {
      RESULT_VICTORY -> {
        var ev = preferences.getInt(getString(R.string.pref_easy_victory), 0); ev++
        preferences.edit().putInt(getString(R.string.pref_easy_victory), ev).apply()
      }
      RESULT_DRAW -> {
        var ed = preferences.getInt(getString(R.string.pref_easy_draw), 0); ed++
        preferences.edit().putInt(getString(R.string.pref_easy_draw), ed).apply()
      }
      RESULT_DEFEAT -> {
        var eDef = preferences.getInt(getString(R.string.pref_easy_defeat), 0); eDef++
        preferences.edit().putInt(getString(R.string.pref_easy_defeat), eDef).apply()
      }
    }
  }
  private fun updateMediumTable(result: Int) {
    when (result) {
      RESULT_VICTORY -> {
        var mv = preferences.getInt(getString(R.string.pref_medium_victory), 0); mv++
        preferences.edit().putInt(getString(R.string.pref_medium_victory), mv).apply()
      }
      RESULT_DRAW -> {
        var md = preferences.getInt(getString(R.string.pref_medium_draw), 0); md++
        preferences.edit().putInt(getString(R.string.pref_medium_draw), md).apply()
      }
      RESULT_DEFEAT -> {
        var mDef = preferences.getInt(getString(R.string.pref_medium_defeat), 0); mDef++
        preferences.edit().putInt(getString(R.string.pref_medium_defeat), mDef).apply()
      }
    }
  }
  private fun updateHardTable(result: Int) {
    when (result) {
      RESULT_VICTORY -> {
        var hv = preferences.getInt(getString(R.string.pref_hard_victory), 0); hv++
        preferences.edit().putInt(getString(R.string.pref_hard_victory), hv).apply()
      }
      RESULT_DRAW -> {
        var hd = preferences.getInt(getString(R.string.pref_hard_draw), 0); hd++
        preferences.edit().putInt(getString(R.string.pref_hard_draw), hd).apply()
      }
      RESULT_DEFEAT -> {
        var hDef = preferences.getInt(getString(R.string.pref_hard_defeat), 0); hDef++
        preferences.edit().putInt(getString(R.string.pref_hard_defeat), hDef).apply()
      }
    }
  }

  private fun showTutorialDialog() {
    val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
    } else {
      AlertDialog.Builder(this)
    }

    builder.setTitle(getString(R.string.dialog_title))
            .setView(R.layout.dialog_tutorial)
            .setPositiveButton(getString(R.string.dialog_negative_button)) { _, _ ->
              preferences.edit()
                      .putBoolean( getString(R.string.pref_show_tutorial), false )
                      .apply()
            }
            .show()
  }

  private fun initFragment(theme: Int) {
    when (theme) {
      THEME_LIGHT -> {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, MainLightFragment.newInstance(), "MAIN_LIGHT_FRAGMENT")
                .commit()
      }
      THEME_DARK -> {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, MainDarkFragment.newInstance(), "MAIN_DARK_FRAGMENT")
                .commit()
      }
    }
  }
  private fun currentFragment(): IFragmentToActivity {
    return when (currentTheme) {
      THEME_LIGHT -> supportFragmentManager.findFragmentByTag("MAIN_LIGHT_FRAGMENT") as IFragmentToActivity
      THEME_DARK  -> supportFragmentManager.findFragmentByTag("MAIN_DARK_FRAGMENT") as IFragmentToActivity
      else -> throw Exception("No current fragment found.")
    }
  }

}
