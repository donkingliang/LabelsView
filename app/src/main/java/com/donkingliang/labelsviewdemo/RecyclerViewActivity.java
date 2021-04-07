package com.donkingliang.labelsviewdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author teach liang
 * @Description
 * @Date 2021/4/7
 */
public class RecyclerViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        List<ListTestBean> data = new ArrayList<>();
        for (int i = 1;i <= 15; i++){
            ListTestBean bean = new ListTestBean();
            List<String> labels = new ArrayList<>();
            for (int y = 1;y <= 5; y++){
                labels.add("第" + i + "组第" + y + "个");
            }
            bean.setLabels(labels);
            data.add(bean);
        }

        recyclerView.setAdapter(new LabelAdapter(this,data));

    }
}
