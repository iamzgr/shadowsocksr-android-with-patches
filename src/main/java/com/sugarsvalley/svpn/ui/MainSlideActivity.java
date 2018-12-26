package com.sugarsvalley.svpn.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import com.github.shadowsocks.R;
import com.sugarsvalley.svpn.widget.SlidingMenu;

public class MainSlideActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_svpn_main);
        setContentView(R.layout.layout_main_icecream);

        final SlidingMenu menu = (SlidingMenu) findViewById(R.id.slideMenu);
        ImageView ivOpenMenu = (ImageView) findViewById(R.id.iv_menu_open);
        ImageView ivCloseMenu = (ImageView) findViewById(R.id.iv_menu_close);

        ivOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.openMenu();
            }
        });

        ivCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.closeMenu();
            }
        });
    }
}
