package com.github.shadowsocks

import android.os.{Bundle, PersistableBundle}
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.{DefaultItemAnimator, LinearLayoutManager, RecyclerView}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{CheckedTextView, ImageView, TextView}
import com.github.shadowsocks.ShadowsocksApplication.app
import com.github.shadowsocks.database.Profile

import scala.collection.mutable.ArrayBuffer

class ServerNodesActivity extends AppCompatActivity {
  private val serverNodesAdapter = new ServerNodesAdapter
  private var is_sort = true;

  private class ServerNodeViewHolder(val view: View) extends RecyclerView.ViewHolder(view) with View.OnClickListener {
    private var item: Profile = _
    itemView.setOnClickListener(this)

//    val iv_radio = itemView.findViewById(R.id.node_select_radio)
//    val tv_country = itemView.findViewById(R.id.tv_node_coutry).asInstanceOf[TextView]
    val iv_flag = itemView.findViewById(R.id.iv_country_flag).asInstanceOf[ImageView]
    val ctv_location = itemView.findViewById(R.id.ctv_location).asInstanceOf[CheckedTextView]

    def onClick(v: View) {
      app.switchProfile(item.id)
      finish
//      ctv_location.toggle();
    }

    def bind(item: Profile) {
      this.item = item
      if (item.country.equalsIgnoreCase("US")) {
        iv_flag.setImageResource(R.drawable.ic_flag_usa)
      }
      ctv_location.setText(item.location)
//      updateText()
      if (item.id == app.profileId) {
        ctv_location.setChecked(true)
//        selectedItem = this
      } else {
        ctv_location.setChecked(false)
//        if (selectedItem eq this) selectedItem = null
      }
    }
  }

  private class ServerNodesAdapter extends RecyclerView.Adapter[ServerNodeViewHolder] {
    var profiles = new ArrayBuffer[Profile]
    is_sort = true
    if (is_sort) {
      profiles ++= app.profileManager.getAllProfilesByLocation.getOrElse(List.empty[Profile])
    } else {
      profiles ++= app.profileManager.getAllProfiles.getOrElse(List.empty[Profile])
    }

    def getItemCount = profiles.length

    def onBindViewHolder(vh: ServerNodeViewHolder, i: Int) = vh.bind(profiles(i))

    def onCreateViewHolder(vg: ViewGroup, i: Int): ServerNodeViewHolder = {
      new ServerNodeViewHolder(LayoutInflater.from(vg.getContext).inflate(R.layout.layout_vpn_node_item, vg, false))
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_vpn_nodes)

    val serverNodesList = findViewById(R.id.server_nodes_list).asInstanceOf[RecyclerView]
    val lm = new LinearLayoutManager(this)
    serverNodesList.setLayoutManager(lm)
    serverNodesList.setItemAnimator(new DefaultItemAnimator)
    serverNodesList.setAdapter(serverNodesAdapter)
  }


//  def test(context: Context): Unit = {
//    context.startActivity(new Intent(context, classOf[ProfileManagerActivity]))
//  }
}
