package com.donkingliang.labelsviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.donkingliang.labels.LabelsView;

import java.util.ArrayList;
import java.util.List;

import static com.donkingliang.labelsviewdemo.R.id.labels;

public class MainActivity extends AppCompatActivity implements LabelsView.OnLabelClickListener {

    private LabelsView btnLabels;
    private LabelsView labelsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLabels = findViewById(R.id.btnLabels);
        labelsView = findViewById(labels);

        // 按钮组
        ArrayList<String> btns = new ArrayList<>();
        btns.add("不可选中");
        btns.add("单选(可反选)");
        btns.add("单选(不可反选)");
        btns.add("多选");
        btns.add("多选(最多5个)");
        btns.add("多选(最少1个)");
        btns.add("多选(1,2必选)");
        btns.add("指示器模式");
        btns.add("取消选中");
        btns.add("点击");
        btnLabels.setLabels(btns);
        btnLabels.setOnLabelClickListener(this);

        //测试的数据
//        ArrayList<String> label = new ArrayList<>();
//        label.add("Android");
//        label.add("IOS");
//        label.add("前端");
//        label.add("后台");
//        label.add("微信开发");
//        label.add("游戏开发");
//        label.add("Java");
//        label.add("JavaScript");
//        label.add("C++");
//        label.add("PHP");
//        label.add("Python");
//        label.add("Swift");
//        labelsView.setLabels(label);

        ArrayList<TestBean> testList = new ArrayList<>();
        testList.add(new TestBean("Android", 1));
        testList.add(new TestBean("IOS", 2));
        testList.add(new TestBean("前端", 3));
        testList.add(new TestBean("后台", 4));
        testList.add(new TestBean("微信开发", 5));
        testList.add(new TestBean("游戏开发", 6));
        testList.add(new TestBean("Java", 7));
        testList.add(new TestBean("JavaScript", 8));
        testList.add(new TestBean("C++", 9));
        testList.add(new TestBean("PHP", 10));
        testList.add(new TestBean("Python", 11));
        testList.add(new TestBean("Swift", 12));

        labelsView.setLabels(testList, new LabelsView.LabelTextProvider<TestBean>() {
            @Override
            public CharSequence getLabelText(TextView label, int position, TestBean data) {
                return data.getName();
            }
        });

        // 设置最大显示行数，小于等于0则不限行数。
//        labelsView.setMaxLines(1);

        labelsView.clearAllSelect();
    }

    @Override
    public void onLabelClick(TextView label, Object data, int position) {
        labelsView.setOnLabelClickListener(null);
        labelsView.clearCompulsorys();
        switch (position) {
            case 0:
                labelsView.setSelectType(LabelsView.SelectType.NONE);
                break;

            case 1:
                labelsView.setSelectType(LabelsView.SelectType.SINGLE);
                break;

            case 2:
                labelsView.setSelectType(LabelsView.SelectType.SINGLE_IRREVOCABLY);
                break;

            case 3:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(0);
                labelsView.setMinSelect(0);
                break;

            case 4:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(5);
                labelsView.setMinSelect(0);
                break;

            case 5:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(0);
                labelsView.setMinSelect(1);
                break;

            case 6:
                labelsView.setSelectType(LabelsView.SelectType.MULTI);
                labelsView.setMaxSelect(0);
                labelsView.setMinSelect(0);
                labelsView.setCompulsorys(0, 1);
                break;

            case 7:
                labelsView.setIndicator(!labelsView.isIndicator());
                if (labelsView.isIndicator()) {
                    btnLabels.getLabels().set(position, "取消指示器模式");
                    label.setText("取消指示器模式");
                } else {
                    btnLabels.getLabels().set(position, "指示器模式");
                    label.setText("指示器模式");
                }
                break;

            case 8:
                labelsView.clearAllSelect();
                break;

            case 9:
                labelsView.setSelectType(LabelsView.SelectType.NONE);
                labelsView.setOnLabelClickListener(new LabelsView.OnLabelClickListener() {
                    @Override
                    public void onLabelClick(TextView label, Object data, int position) {
                        Toast.makeText(MainActivity.this, position + " : " + data,
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }
}
