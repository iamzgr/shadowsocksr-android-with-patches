package com.sugarsvalley.svpn.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.shadowsocks.R;

public class PurchaseActivity extends AppCompatActivity {
    ImageView   iv_go_back;
    ImageView   iv_show_more;
    TextView    tv_plan_free;
    TextView    tv_plan_a;
    TextView    tv_plan_b;
    TextView    tv_plan_c;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_purchase);

        iv_go_back = (ImageView) findViewById(R.id.iv_go_back);
        iv_show_more = (ImageView) findViewById(R.id.iv_show_more);
        tv_plan_free = (TextView) findViewById(R.id.tv_plan_free);
        tv_plan_a = (TextView) findViewById(R.id.tv_plan_a);
        tv_plan_b = (TextView) findViewById(R.id.tv_plan_b);
        tv_plan_c = (TextView) findViewById(R.id.tv_plan_c);
    }
}