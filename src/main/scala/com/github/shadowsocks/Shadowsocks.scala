/*
 * Shadowsocks - A shadowsocks client for Android
 * Copyright (C) 2014 <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *                            ___====-_  _-====___
 *                      _--^^^#####//      \\#####^^^--_
 *                   _-^##########// (    ) \\##########^-_
 *                  -############//  |\^^/|  \\############-
 *                _/############//   (@::@)   \\############\_
 *               /#############((     \\//     ))#############\
 *              -###############\\    (oo)    //###############-
 *             -#################\\  / VV \  //#################-
 *            -###################\\/      \//###################-
 *           _#/|##########/\######(   /\   )######/\##########|\#_
 *           |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 *           `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 *              `   `  `      `   / | |  | | \   '      '  '   '
 *                               (  | |  | |  )
 *                              __\ | |  | | /__
 *                             (vvv(VVV)(VVV)vvv)
 *
 *                              HERE BE DRAGONS
 *
 */
package com.github.shadowsocks

import java.lang.System.currentTimeMillis
import java.net.{HttpURLConnection, URL}
import java.util
import java.util.{GregorianCalendar, Locale}

import android.animation.ObjectAnimator
import android.app.backup.BackupManager
import android.app.{Activity, ProgressDialog}
import android.content._
import android.graphics.Typeface
import android.net.VpnService
import android.os._
import android.support.design.widget.{FloatingActionButton, Snackbar}
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View.OnClickListener
import android.view.{View, ViewGroup}
import android.widget._
import com.github.jorgecastilloprz.FABProgressCircle
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback
import com.github.shadowsocks.database._
import com.github.shadowsocks.utils.CloseUtils._
import com.github.shadowsocks.utils._
import com.github.shadowsocks.job.SSRSubUpdateJob
import com.github.shadowsocks.ShadowsocksApplication.app
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.sugarsvalley.svpn.ui.{MainActivity, PurchaseActivity}
import com.sugarsvalley.svpn.utils.PixelUtil
import com.sugarsvalley.svpn.widget.DoughnutProgress

import scala.util.Random

object Typefaces {
  def get(c: Context, assetPath: String): Typeface = {
    cache synchronized {
      if (!cache.containsKey(assetPath)) {
        try {
          cache.put(assetPath, Typeface.createFromAsset(c.getAssets, assetPath))
        } catch {
          case e: Exception =>
            Log.e(TAG, "Could not get typeface '" + assetPath + "' because " + e.getMessage)
            app.track(e)
            return null
        }
      }
      return cache.get(assetPath)
    }
  }

  private final val TAG = "Typefaces"
  private final val cache = new util.Hashtable[String, Typeface]
}

object Shadowsocks {
  // Constants
  private final val TAG = "Shadowsocks"
  private final val REQUEST_CONNECT = 1
  private val EXECUTABLES = Array(Executable.PDNSD, Executable.REDSOCKS, Executable.SS_TUNNEL, Executable.SS_LOCAL,
    Executable.TUN2SOCKS)
}

class Shadowsocks extends AppCompatActivity with ServiceBoundContext {
  import Shadowsocks._

  // Variables
  var serviceStarted: Boolean = _
  var fab: FloatingActionButton = _
  var fabProgressCircle: FABProgressCircle = _
  var progressDialog: ProgressDialog = _
  var circle_progress: DoughnutProgress = _
  var state = State.STOPPED
  var currentProfile = new Profile
  var newMainHandler: Handler = null
  var iv_cart: ImageView = null

  // Services
  private val callback = new IShadowsocksServiceCallback.Stub {
    def stateChanged(s: Int, profileName: String, m: String) {
      handler.post(() => {
        s match {
          case State.CONNECTING =>
            tv_connect.setText(R.string.vpn_connecting)
            tv_connect.setClickable(false)
            showCircleProgress()
//            fab.setBackgroundTintList(greyTint)
//            fab.setImageResource(R.drawable.ic_start_busy)
//            fab.setEnabled(false)
//            fabProgressCircle.show()
//            preferences.setEnabled(false)
//            stat.setVisibility(View.GONE)
          case State.CONNECTED =>
            tv_connect.setText(R.string.vpn_connected)
            tv_connect.setClickable(true)
            handler.postDelayed(() => hideCircleProgress(), 1000)
//            fab.setBackgroundTintList(greenTint)
//            if (state == State.CONNECTING) {
//              fabProgressCircle.beginFinalAnimation()
//            } else {
//              fabProgressCircle.postDelayed(hideCircle, 1000)
//            }
//            fab.setEnabled(true)
            changeSwitch(checked = true)
//            preferences.setEnabled(false)
//            stat.setVisibility(View.VISIBLE)
            if (app.isNatEnabled) connectionTestText.setVisibility(View.GONE) else {
              connectionTestText.setVisibility(View.VISIBLE)
              connectionTestText.setText(getString(R.string.connection_test_pending))
            }
          case State.STOPPED =>
            tv_connect.setText(R.string.vpn_connect)
            tv_connect.setClickable(true)
            handler.postDelayed(() => hideCircleProgress(), 1000)
//            fab.setBackgroundTintList(greyTint)
//            fabProgressCircle.postDelayed(hideCircle, 1000)
//            fab.setEnabled(true)
            changeSwitch(checked = false)
            if (m != null) {
              val snackbar = Snackbar.make(findViewById(android.R.id.content),
                getString(R.string.vpn_error).formatLocal(Locale.ENGLISH, m), Snackbar.LENGTH_LONG)
              if (m == getString(R.string.nat_no_root)) snackbar.setAction(R.string.switch_to_vpn,
                (_ => preferences.natSwitch.setChecked(false)): View.OnClickListener)
              snackbar.show
              Log.e(TAG, "Error to start VPN service: " + m)
            }
//            preferences.setEnabled(true)
//            stat.setVisibility(View.GONE)
          case State.STOPPING =>
            tv_connect.setClickable(false)
            if (state == State.CONNECTED)
              showCircleProgress()

//            fab.setBackgroundTintList(greyTint)
//            fab.setImageResource(R.drawable.ic_start_busy)
//            fab.setEnabled(false)
//            if (state == State.CONNECTED) fabProgressCircle.show()  // ignore for stopped
//            preferences.setEnabled(false)
//            stat.setVisibility(View.GONE)
        }
        state = s
      })
    }
    def trafficUpdated(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) {
      handler.post(() => updateTraffic(txRate, rxRate, txTotal, rxTotal))
    }
  }

  def updateTraffic(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) {
    txText.setText(TrafficMonitor.formatTraffic(txTotal))
    rxText.setText(TrafficMonitor.formatTraffic(rxTotal))
    txRateText.setText(TrafficMonitor.formatTraffic(txRate) + "/s")
    rxRateText.setText(TrafficMonitor.formatTraffic(rxRate) + "/s")
  }

  def attachService: Unit = attachService(callback)

  override def onServiceConnected() {
    // Update the UI
    if (tv_connect != null) tv_connect.setClickable(true)
//    if (fab != null) fab.setEnabled(true)

    updateState()
    if (Build.VERSION.SDK_INT >= 21 && app.isNatEnabled) {
      val snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.nat_deprecated, Snackbar.LENGTH_LONG)
      snackbar.setAction(R.string.switch_to_vpn, (_ => preferences.natSwitch.setChecked(false)): View.OnClickListener)
      snackbar.show
    }
  }

  override def onServiceDisconnected() {
    if (tv_connect != null) tv_connect.setClickable(false)
//    if (fab != null)fab.setEnabled(false)
  }


  override def binderDied {
    detachService()
    app.crashRecovery()
    attachService
  }

  private var testCount: Int = _
  private var stat: View = _
  private var connectionTestText: TextView = _
  private var txText: TextView = _
  private var rxText: TextView = _
  private var txRateText: TextView = _
  private var rxRateText: TextView = _
  private var mInterstitialAd: InterstitialAd = _

//  private var ll_conntecBtn: LinearLayout = _
  private var tv_connect: TextView = _
  private var tv_server_location: TextView = _

  private lazy val greyTint = ContextCompat.getColorStateList(this, R.color.material_blue_grey_700)
  private lazy val greenTint = ContextCompat.getColorStateList(this, R.color.material_green_700)
  //private var adView: AdView = _
  private lazy val preferences =
    getFragmentManager.findFragmentById(android.R.id.content).asInstanceOf[ShadowsocksSettings]

  var handler: Handler = new Handler() {
    override def handleMessage(msg: Message): Unit = {
      msg.what match {
        case Scala2JavaBridge.MESSAGE_CONNECT_BUTTON_CLICK =>
          clickConncetBtn()
        case Scala2JavaBridge.MESSAGE_START_PROFILE =>
          startActivity(new Intent(Shadowsocks.this, classOf[ProfileManagerActivity]))
        case Scala2JavaBridge.MESSAGE_NEW_MAIN_HANDLER_READY =>
          newMainHandler = msg.obj.asInstanceOf[Handler]
          Log.e(TAG, "receive newMainHandler")
        case _ =>
      }
    }
  }

  private def changeSwitch(checked: Boolean) {
    serviceStarted = checked
    if (tv_connect.isClickable) {
      tv_connect.setClickable(false)
      handler.postDelayed(() => tv_connect.setClickable(true), 1000)
    }
//    fab.setImageResource(if (checked) R.drawable.ic_start_connected else R.drawable.ic_start_idle)
//    if (fab.isEnabled) {
//      fab.setEnabled(false)
//      handler.postDelayed(() => fab.setEnabled(true), 1000)
//    }

//    if (newMainHandler != null) {
//      Log.e("handleMessage", "newMainHandler send msg")
//      //            super.handleMessage(msg);
//      if (checked) newMainHandler.sendEmptyMessage(Scala2JavaBridge.CONNECT_STATUS_CONNECTED)
//      else newMainHandler.sendEmptyMessage(Scala2JavaBridge.CONNECT_STATUS_DISCONNECTED)
//    }
  }

  private def showProgress(msg: Int): Handler = {
    clearDialog()
    progressDialog = ProgressDialog.show(this, "", getString(msg), true, false)
    new Handler {
      override def handleMessage(msg: Message) {
        clearDialog()
      }
    }
  }

  def cancelStart() {
    clearDialog()
    changeSwitch(checked = false)
  }

  def prepareStartService() {
    Utils.ThrowableFuture {
      if (app.isNatEnabled) serviceLoad() else {
        val intent = VpnService.prepare(this)
        if (intent != null) {
          startActivityForResult(intent, REQUEST_CONNECT)
        } else {
          handler.post(() => onActivityResult(REQUEST_CONNECT, Activity.RESULT_OK, null))
        }
      }
    }
  }

  override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_main)
/*
    MobileAds.initialize(this, "ca-app-pub-9293841874269644~3114136828")
    mInterstitialAd = new InterstitialAd(this)
    mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712")
    mInterstitialAd.loadAd(new AdRequest.Builder().build)

    mInterstitialAd.setAdListener(new AdListener() {
      override def onAdLoaded(): Unit = {
        // Code to be executed when an ad finishes loading.
      }

      override def onAdFailedToLoad(errorCode: Int): Unit = {
        // Code to be executed when an ad request fails.
      }
      override def onAdOpened(): Unit = {
        // Code to be executed when the ad is displayed.
      }
      override def onAdLeftApplication(): Unit = {
        // Code to be executed when the user has left the app.
      }
      override def onAdClosed(): Unit = {
        // Code to be executed when when the interstitial ad is closed.
        mInterstitialAd.loadAd(new AdRequest.Builder().build)
      }
    })
*/

    TestActivity.setMainHandler(handler)
//    MainActivity.setMainHandler(handler)
//    startActivity(new Intent(this, classOf[MainActivity]))

    // Initialize Toolbar
    val toolbar = findViewById(R.id.toolbar).asInstanceOf[Toolbar]
    toolbar.setTitle("shadowsocks R") // non-translatable logo
    toolbar.setTitleTextAppearance(toolbar.getContext, R.style.Toolbar_Logo)
    val field = classOf[Toolbar].getDeclaredField("mTitleTextView")
    field.setAccessible(true)
    val title = field.get(toolbar).asInstanceOf[TextView]
    title.setFocusable(true)
    title.setGravity(0x10)
    title.getLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    title.setOnClickListener(_ => startActivity(new Intent(this, classOf[TestActivity])))
    val typedArray = obtainStyledAttributes(Array(R.attr.selectableItemBackgroundBorderless))
    title.setBackgroundResource(typedArray.getResourceId(0, 0))
    typedArray.recycle
    val tf = Typefaces.get(this, "fonts/Iceland.ttf")
    if (tf != null) title.setTypeface(tf)
    title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0)

    stat = findViewById(R.id.stat)
    connectionTestText = findViewById(R.id.connection_test).asInstanceOf[TextView]
    txText = findViewById(R.id.tx).asInstanceOf[TextView]
    txRateText = findViewById(R.id.txRate).asInstanceOf[TextView]
    rxText = findViewById(R.id.rx).asInstanceOf[TextView]
    rxRateText = findViewById(R.id.rxRate).asInstanceOf[TextView]
//    ll_conntecBtn = findViewById(R.id.ll_connect).asInstanceOf[LinearLayout]
    tv_server_location = findViewById(R.id.tv_server_location).asInstanceOf[TextView]
    val ll_nodesList = findViewById(R.id.ll_region).asInstanceOf[LinearLayout]
    val view: View = findViewById(R.id.view_main_surface)
    val iv_purchase = findViewById(R.id.iv_perchase).asInstanceOf[ImageView]
    iv_cart = findViewById(R.id.iv_cart).asInstanceOf[ImageView]
    initView()

    view.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
      }
    })
//    ll_conntecBtn.setOnClickListener(new View.OnClickListener() {
//      override def onClick(view: View): Unit = {
//        clickConncetBtn()
//      }
//    })
    iv_cart.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        startActivity(new Intent(Shadowsocks.this, classOf[PurchaseActivity]))
      }
    })
    ll_nodesList.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        startActivity(new Intent(Shadowsocks.this, classOf[ServerNodesActivity]))
      }
    })
    iv_purchase.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        startActivity(new Intent(Shadowsocks.this, classOf[TestActivity]))
      }
    })

    stat.setOnClickListener(_ => {
      val id = synchronized {
        testCount += 1
        handler.post(() => connectionTestText.setText(R.string.connection_test_testing))
        testCount
      }
      Utils.ThrowableFuture {
        // Based on: https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/connectivity/NetworkMonitor.java#640
        autoDisconnect(new URL("https", "www.google.com", "/generate_204").openConnection()
          .asInstanceOf[HttpURLConnection]) { conn =>
          conn.setConnectTimeout(5 * 1000)
          conn.setReadTimeout(5 * 1000)
          conn.setInstanceFollowRedirects(false)
          conn.setUseCaches(false)
          if (testCount == id) {
            var result: String = null
            var success = true
            try {
              val start = currentTimeMillis
              conn.getInputStream
              val elapsed = currentTimeMillis - start
              val code = conn.getResponseCode
              if (code == 204 || code == 200 && conn.getContentLength == 0)
                result = getString(R.string.connection_test_available, elapsed: java.lang.Long)
              else throw new Exception(getString(R.string.connection_test_error_status_code, code: Integer))
            } catch {
              case e: Exception =>
                success = false
                result = getString(R.string.connection_test_error, e.getMessage)
            }
            synchronized(if (testCount == id && app.isVpnEnabled) handler.post(() =>
              if (success) connectionTestText.setText(result) else {
                connectionTestText.setText(R.string.connection_test_fail)
                Snackbar.make(findViewById(android.R.id.content), result, Snackbar.LENGTH_LONG).show
              }))
          }
        }
      }
    })

    tv_connect = findViewById(R.id.tv_ice_connect).asInstanceOf[TextView]
    tv_connect.setOnClickListener((v: View) => {
      clickConncetBtn()
    })
    circle_progress = findViewById(R.id.circle_progress).asInstanceOf[DoughnutProgress]

//    fab = findViewById(R.id.fab).asInstanceOf[FloatingActionButton]
//    fabProgressCircle = findViewById(R.id.fabProgressCircle).asInstanceOf[FABProgressCircle]
//    fab.setOnClickListener((v: View) => {
//      clickConncetBtn()})
//    fab.setOnLongClickListener((v: View) => {
//      Utils.positionToast(Toast.makeText(this, if (serviceStarted) R.string.stop else R.string.connect,
//        Toast.LENGTH_SHORT), fab, getWindow, 0, Utils.dpToPx(this, 8)).show
//      true
//    })
    updateTraffic(0, 0, 0, 0)

    app.ssrsubManager.getFirstSSRSub match {
      case Some(first) => {

      }
      case None => app.ssrsubManager.createDefault()
    }

    SSRSubUpdateJob.schedule()

    handler.post(() => attachService)

    hideCircleProgress()
  }

  private def initView(): Unit = {
    var distance = PixelUtil.getWindowWidth(this) - PixelUtil.dp2px(90, this)
    distance -= 60
    distance /= 2
    val xtransAnimator = ObjectAnimator.ofFloat(iv_cart, "translationX", 0f, 0f - distance, 0f, distance, 0f)
    xtransAnimator.setDuration(8000)
    xtransAnimator.setRepeatCount(-1)
    xtransAnimator.start()
  }

  private def clickConncetBtn(): Unit = {
    if (serviceStarted) serviceStop()
    else if (bgService != null) prepareStartService()
    else changeSwitch(checked = false)
  }

  private def showAd(): Unit = {
    if (mInterstitialAd.isLoaded) mInterstitialAd.show
  }

  private def showCircleProgress() {

//    circle_progress.start()
//    circle_progress.setVisibility(View.VISIBLE)
  }

  private def hideCircleProgress() {
//    circle_progress.stop()
//    circle_progress.setVisibility(View.GONE)
  }

  private def hideCircle() {
    try {
      fabProgressCircle.hide()
    } catch {
      case _: java.lang.NullPointerException => // Ignore
    }
  }

  private def updateState(resetConnectionTest: Boolean = true) {
    if (bgService != null) {
      bgService.getState match {
        case State.CONNECTING =>
          tv_connect.setText(R.string.vpn_connecting)
          showCircleProgress()
//          fab.setBackgroundTintList(greyTint)
//          fab.setImageResource(R.drawable.ic_start_busy)
          serviceStarted = false
//          preferences.setEnabled(false)
//          fabProgressCircle.show()
//          stat.setVisibility(View.GONE)
        case State.CONNECTED =>
          tv_connect.setText(R.string.vpn_connected)
          handler.postDelayed(() => hideCircleProgress(), 1000)
//          fab.setBackgroundTintList(greenTint)
//          fab.setImageResource(R.drawable.ic_start_connected)
          serviceStarted = true
//          preferences.setEnabled(false)
//          fabProgressCircle.postDelayed(hideCircle, 100)
//          stat.setVisibility(View.VISIBLE)
          if (resetConnectionTest || state != State.CONNECTED)
            if (app.isNatEnabled) connectionTestText.setVisibility(View.GONE) else {
              connectionTestText.setVisibility(View.VISIBLE)
              connectionTestText.setText(getString(R.string.connection_test_pending))
            }
        case State.STOPPING =>
          showCircleProgress()
//          fab.setBackgroundTintList(greyTint)
//          fab.setImageResource(R.drawable.ic_start_busy)
          serviceStarted = false
//          preferences.setEnabled(false)
//          fabProgressCircle.show()
//          stat.setVisibility(View.GONE)
        case _ =>
          tv_connect.setText(R.string.vpn_connect)
          handler.postDelayed(() => hideCircleProgress(), 1000)
//          fab.setBackgroundTintList(greyTint)
//          fab.setImageResource(R.drawable.ic_start_idle)
          serviceStarted = false
//          preferences.setEnabled(true)
//          fabProgressCircle.postDelayed(hideCircle, 100)
//          stat.setVisibility(View.GONE)
      }
      state = bgService.getState
    }
  }

  private def updateCurrentProfile() = {
    // Check if current profile changed
    if (preferences.profile == null || app.profileId != preferences.profile.id) {
      updatePreferenceScreen(app.currentProfile match {
        case Some(profile) => profile // updated
        case None =>                  // removed
          app.switchProfile((app.profileManager.getFirstProfile match {
            case Some(first) => first
            case None => app.profileManager.createDefault()
          }).id)
      })

      if (serviceStarted) serviceLoad()

      true
    } else {
      preferences.refreshProfile()
      false
    }
  }

  protected override def onResume() {
    super.onResume()

    app.refreshContainerHolder

    updateState(updateCurrentProfile())
  }

  private def updatePreferenceScreen(profile: Profile) {
    preferences.setProfile(profile)
    tv_server_location.setText(profile.name)
  }

  override def onStart() {
    super.onStart()
    registerCallback
  }
  override def onStop() {
    super.onStop()
    unregisterCallback
    clearDialog()
  }

  private var _isDestroyed: Boolean = _
  override def isDestroyed = if (Build.VERSION.SDK_INT >= 17) super.isDestroyed else _isDestroyed
  override def onDestroy() {
    super.onDestroy()
    _isDestroyed = true
    detachService()
    new BackupManager(this).dataChanged()
    handler.removeCallbacksAndMessages(null)
  }

  def recovery() {
    if (serviceStarted) serviceStop()
    val h = showProgress(R.string.recovering)
    Utils.ThrowableFuture {
      app.copyAssets()
      h.sendEmptyMessage(0)
    }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = resultCode match {
    case Activity.RESULT_OK =>
      serviceLoad()
    case _ =>
      cancelStart()
      Log.e(TAG, "Failed to start VpnService")
  }

  def serviceStop() {
    if (bgService != null) bgService.use(-1)
  }

  /** Called when connect button is clicked. */
  def serviceLoad() {
    bgService.use(app.profileId)

    if (app.isVpnEnabled) {
      changeSwitch(checked = false)
    }
  }

  def clearDialog() {
    if (progressDialog != null && progressDialog.isShowing) {
      if (!isDestroyed) progressDialog.dismiss()
      progressDialog = null
    }
  }
}
