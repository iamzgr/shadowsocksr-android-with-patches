package com.github.shadowsocks

import android.content.{Context, Intent}
import android.os.{Bundle, PersistableBundle}
import android.support.v7.app.AppCompatActivity

class HomeActivitye extends AppCompatActivity {
  override def onCreate(savedInstanceState: Bundle, persistentState: PersistableBundle) {
    super.onCreate(savedInstanceState, persistentState)
    startActivity(new Intent(this, classOf[Shadowsocks]))
  }

  def test(context: Context): Unit = {
    context.startActivity(new Intent(context, classOf[ProfileManagerActivity]))
  }
}
