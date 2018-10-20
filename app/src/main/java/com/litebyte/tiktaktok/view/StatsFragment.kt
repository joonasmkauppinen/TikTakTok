package com.litebyte.tiktaktok.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import com.litebyte.tiktaktok.R
import com.litebyte.tiktaktok.contract.IGameBoard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_stats.*
import kotlinx.android.synthetic.main.fragment_stats.view.*

class StatsFragment: Fragment(), View.OnClickListener {

  private lateinit var parentActivity: IGameBoard

  companion object {
    fun newInstance(): StatsFragment {
      return StatsFragment()
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    parentActivity = context as IGameBoard
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_stats, container, false)
    view.close_stats.setOnClickListener(this)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

    easy_victory.text = prefs.getInt( getString(R.string.pref_easy_victory), 0 ).toString()
    easy_draw.text    = prefs.getInt( getString(R.string.pref_easy_draw), 0 ).toString()
    easy_defeat.text  = prefs.getInt( getString(R.string.pref_easy_defeat), 0 ).toString()

    medium_victory.text = prefs.getInt( getString(R.string.pref_medium_victory), 0 ).toString()
    medium_draw.text    = prefs.getInt( getString(R.string.pref_medium_draw), 0 ).toString()
    medium_defeat.text  = prefs.getInt( getString(R.string.pref_medium_defeat), 0 ).toString()

    hard_victory.text = prefs.getInt( getString(R.string.pref_hard_victory), 0 ).toString()
    hard_draw.text    = prefs.getInt( getString(R.string.pref_hard_draw), 0 ).toString()
    hard_defeat.text  = prefs.getInt( getString(R.string.pref_hard_defeat), 0 ).toString()

    revealAnim(true, parentActivity.getStatsCoordiantes())

  }

  private fun revealAnim(opening: Boolean, coordinates: IntArray) {

    val cx = coordinates[0] + 72
    val cy = coordinates[1] + 72

    var startRadius = 0f
    val screenWidth: Double = activity!!.container.width.toDouble()
    val screenHeight: Double = activity!!.container.height.toDouble()
    var endRadius: Float = Math.sqrt( screenWidth * screenWidth + screenHeight * screenHeight).toFloat()

    if (!opening) {
      val temp = endRadius
      endRadius = startRadius
      startRadius = temp
    }

    val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, endRadius)
    anim.duration = 800
    anim.interpolator = FastOutSlowInInterpolator()
    anim.start()

  }

  override fun onClick(v: View?) {
    revealAnim(false, parentActivity.getStatsCoordiantes())
    Handler().postDelayed({
      activity!!.supportFragmentManager.popBackStack()
    }, 800)
  }
}