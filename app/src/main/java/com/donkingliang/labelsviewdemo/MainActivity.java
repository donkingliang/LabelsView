package com.donkingliang.labelsviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.donkingliang.labels.LabelsView;

import java.util.ArrayList;

import static com.donkingliang.labelsviewdemo.R.id.labels;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LabelsView labelsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelsView = (LabelsView) findViewById(labels);

        //测试的数据
        ArrayList<String> label = new ArrayList<>();
        label.add("Android");
        label.add("IOS");
        label.add("前端");
        label.add("后台");
        label.add("微信开发");
        label.add("游戏开发");
        label.add("Java");
        label.add("JavaScript");
        label.add("C++");
        label.add("PHP");
        label.add("Python");
        label.add("Swift");
        labelsView.setLabels(label);

        findViewById(R.id.btn_none).setOnClickListener(this);
        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_multi).setOnClickListener(this);
        findViewById(R.id.btn_multi_5).setOnClickListener(this);
        findViewById(R.id.btn_un_select).setOnClickListener(this);
        findViewById(R.id.btn_click).setOnClickListener(this);
        findViewById(R.id.btn_single_irrevocably).setOnClickListener(this);
        findViewById(R.id.btn_multi_compulsory).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        labelsView.setOnLabelClickListener(null);
        switch (v.getId()) {
            case R.id.btn_none:
                labelsView.setSelectType(LabelsView.SelectType.NONE);
                break;

            case R.id.btn_single:
                labelsView.setSelectType(LabelsView.SelectType.SINGLE);
                break;

            case R.id.btn_single_irrevocably:
                labelsView.setSelectType(LabelsView.SelectType.SINGLE_IRREVOCABLY);
                break;

            case R.id.btn_multi:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(0);
                break;

            case R.id.btn_multi_5:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(5);
                break;

            case R.id.btn_multi_compulsory:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(0);
                labelsView.setCompulsorys(0,1);
                break;

            case R.id.btn_un_select:
                labelsView.clearAllSelect();
                break;

            case R.id.btn_click:
                labelsView.setSelectType(LabelsView.SelectType.NONE);
                labelsView.setOnLabelClickListener(new LabelsView.OnLabelClickListener() {
                    @Override
                    public void onLabelClick(View label, String labelText, int position) {
                        Toast.makeText(MainActivity.this, position + " : " + labelText,
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }
}
