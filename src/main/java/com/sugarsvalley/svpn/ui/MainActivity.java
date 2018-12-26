package com.sugarsvalley.svpn.ui;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.shadowsocks.R;
import com.github.shadowsocks.Scala2JavaBridge;
import com.sugarsvalley.svpn.utils.PixelUtil;

public class MainActivity extends AppCompatActivity {
    private LinearLayout ll_conntecBtn;
    private TextView tv_connect;
    private ImageView iv_cart;

    public static Handler mainhandler = null;
    public static void setMainHandler(Handler h) {
        mainhandler = h;
    }

    public Handler newMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("handleMessage", "newMainHandler receive msg");
            super.handleMessage(msg);
            switch (msg.what) {
                case Scala2JavaBridge.CONNECT_STATUS_CONNECTING:
                    tv_connect.setText(R.string.vpn_connecting);
                    setConnBtnClickable(0);
                    break;
                case Scala2JavaBridge.CONNECT_STATUS_CONNECTED:
                    tv_connect.setText(R.string.vpn_connected);
                    setConnBtnClickable(2);
                    break;
                case Scala2JavaBridge.CONNECT_STATUS_DISCONNECTING:
                    tv_connect.setText(R.string.vpn_disconnecting);
                    setConnBtnClickable(0);
                    break;
                case Scala2JavaBridge.CONNECT_STATUS_DISCONNECTED:
                    tv_connect.setText(R.string.vpn_connect);
                    setConnBtnClickable(2);
                    break;
                case Scala2JavaBridge.MESSAGE_SERVICE_CONNECTED:
                    setConnBtnClickable(1);
                    break;
                case Scala2JavaBridge.MESSAGE_SERVICE_DISCONNECTED:
                    setConnBtnClickable(0);
                    break;
            }
        }
    };

    private void setNewMainHandler() {
        Message msg = new Message();
        msg.what = Scala2JavaBridge.MESSAGE_NEW_MAIN_HANDLER_READY;
        msg.obj = (Object) newMainHandler;
        mainhandler.sendMessage(msg);
    }

    private void setConnBtnClickable(int type) {
        if (type == 0) {
            ll_conntecBtn.setEnabled(false);
        } else if (type == 1) {
            ll_conntecBtn.setEnabled(true);
        } else if (type == 2) {
            ll_conntecBtn.setEnabled(false);
            newMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ll_conntecBtn.setEnabled(true);
                }
            }, 1500);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_svpn_main);
        setContentView(R.layout.layout_main_icecream);

        iv_cart = (ImageView) findViewById(R.id.iv_cart);
        tv_connect = (TextView) findViewById(R.id.tv_connect);
        tv_connect.setText(R.string.vpn_connected);
        ll_conntecBtn = (LinearLayout) findViewById(R.id.ll_connect);
        ll_conntecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainhandler.sendEmptyMessage(Scala2JavaBridge.MESSAGE_CONNECT_BUTTON_CLICK);
            }
        });

        setNewMainHandler();
        initView();
    }

    private void initView() {
        float distance = PixelUtil.getWindowWidth(this) - PixelUtil.dp2px(iv_cart.getWidth(), this);
        distance -= 60;
        distance /= 2;
        ObjectAnimator xtransAnimator = ObjectAnimator.ofFloat(iv_cart, "translationX", 0f, 0f - distance, 0f, distance, 0f);
        xtransAnimator.setDuration(8000);
        xtransAnimator.setRepeatCount(-1);
        xtransAnimator.start();
    }
}
