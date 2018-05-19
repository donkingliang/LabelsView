LabelsView
======
标签列表控件的使用介绍。

**1、引入依赖**
在Project的build.gradle在添加以下代码
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
在Module的build.gradle在添加以下代码
```
dependencies {
    compile 'com.github.donkingliang:LabelsView:1.4.2'
}
```

**2、编写布局：**

```xml
   <com.donkingliang.labels.LabelsView 
       xmlns:app="http://schemas.android.com/apk/res-auto"
       android:id="@+id/labels"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       app:labelBackground="@drawable/label_bg"     //标签的背景
       app:labelTextColor="@drawable/label_text_color" //标签的字体颜色 可以是一个颜色值
       app:labelTextSize="14sp"      //标签的字体大小
       app:labelTextPaddingBottom="5dp"   //标签的上下左右边距
       app:labelTextPaddingLeft="10dp"
       app:labelTextPaddingRight="10dp"
       app:labelTextPaddingTop="5dp"
       app:lineMargin="10dp"   //行与行的距离
       app:wordMargin="10dp"   //标签与标签的距离
       app:selectType="SINGLE"   //标签的选择类型 有单选(可反选)、单选(不可反选)、多选、不可选四种类型
       app:maxSelect="5" />  //标签的最大选择数量，只有多选的时候才有用，0为不限数量
```
这里有两个地方需要说明一下：

1）标签的正常样式和选中样式是通过drawable来实现的。比如下面两个drawable。
```xml
<!-- 标签的背景 label_bg -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 标签选中时的背景 -->
    <item android:state_selected="true">
        <shape>
            <stroke android:width="2dp" android:color="#fb435b" />
            <corners android:radius="8dp" />
            <solid android:color="@android:color/white" />
        </shape>
    </item>
    <!-- 标签的正常背景 -->
    <item>
        <shape>
            <stroke android:width="2dp" android:color="#656565" />
            <corners android:radius="8dp" />
            <solid android:color="@android:color/white" />
        </shape>
    </item>
</selector>
```
```xml
<!-- 标签的文字颜色 label_text_color -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 标签选中时的文字颜色 -->
    <item android:color="#fb435b" android:state_selected="true" />
    <!-- 标签的正常文字颜色 -->
    <item android:color="#2d2b2b" />
</selector>
```
TextView的textColor属性除了可以设置一个颜色值以外，也可以通过资源来设置的，这一点很多同学都不知道。

2）标签的选择类型有四种：

**NONE** ：标签不可选中，也不响应选中事件监听，这是默认值。

**SINGLE**：单选(可反选)。这种模式下，可以一个也不选。

**SINGLE_IRREVOCABLY**：单选(不可反选)。这种模式下，有且只有一个是选中的。默认是第一个。

**MULTI**：多选，可以通过设置maxSelect限定选择的最大数量，0为不限数量。maxSelect只有在多选的时候才有效。多选模式下可以设置一些标签为必选项。必选项的标签默认选中，且不能取消。

**3、设置标签：**

```java
labelsView = (LabelsView) findViewById(labels);
ArrayList<String> label = new ArrayList<>();
label.add("Android");
label.add("IOS");
label.add("前端");
label.add("后台");
label.add("微信开发");
label.add("游戏开发");
labelsView.setLabels(label); //直接设置一个字符串数组就可以了。

//LabelsView可以设置任何类型的数据，而不仅仅是String。
ArrayList<TestBean> testList = new ArrayList<>();
testList.add(new TestBean("Android",1));
testList.add(new TestBean("IOS",2));
testList.add(new TestBean("前端",3));
testList.add(new TestBean("后台",4));
testList.add(new TestBean("微信开发",5));
testList.add(new TestBean("游戏开发",6));
labelsView.setLabels(testList, new LabelsView.LabelTextProvider<TestBean>() {
    @Override
    public CharSequence getLabelText(TextView label, int position, TestBean data) {
    	//根据data和position返回label需要显示的数据。
        return data.getName();
    }
});
```
**4、设置事件监听：**(如果需要的话)

```java
//标签的点击监听
labelsView.setOnLabelClickListener(new LabelsView.OnLabelClickListener() {
    @Override
    public void onLabelClick(TextView label, Object data, int position) {
         //label是被点击的标签，data是标签所对应的数据，position是标签的位置。
    }
});
//标签的选中监听
labelsView.setOnLabelSelectChangeListener(new LabelsView.OnLabelSelectChangeListener() {
    @Override
    public void onLabelSelectChange(TextView label, Object data, boolean isSelect, int position) {
        //label是被选中的标签，data是标签所对应的数据，isSelect是是否选中，position是标签的位置。
    }
});
```
**5、常用方法**

```java
//设置选中标签。
//positions是个可变类型，表示被选中的标签的位置。
//比喻labelsView.setSelects(1,2,5);选中第1,3,5个标签。如果是单选的话，只有第一个参数有效。
public void setSelects(int... positions);
public void setSelects(List<Integer> positions)；

//获取选中的标签(返回的是所有选中的标签的位置)。返回的是一个Integer的数组，表示被选中的标签的下标。如果没有选中，数组的size等于0。
public ArrayList<Integer> getSelectLabels();
//获取选中的label(返回的是所有选中的标签的数据)。如果没有选中，数组的size等于0。T表示标签的数据类型。
public <T> List<T> getSelectLabelDatas();

//取消所有选中的标签。
public void clearAllSelect();

//设置标签的选择类型，有NONE、SINGLE、SINGLE_IRREVOCABLY和MULTI四种类型。
public void setSelectType(SelectType selectType);

//设置最大的选择数量，只有selectType等于MULTI是有效。
public void setMaxSelect(int maxSelect);

//设置必选项，只有在多项模式下，这个方法才有效
public void setCompulsorys(int... positions)
public void setCompulsorys(List<Integer> positions)

//清空必选项，只有在多项模式下，这个方法才有效
public void clearCompulsorys()

//设置标签背景
public void setLabelBackgroundResource(int resId);

//设置标签的文字颜色
public void setLabelTextColor(int color);
public void setLabelTextColor(ColorStateList color);

//设置标签的文字大小（单位是px）
public void setLabelTextSize(float size);

//设置标签内边距
public void setLabelTextPadding(int left, int top, int right, int bottom);

//设置行间隔
public void setLineMargin(int margin);

//设置标签的间隔
public void setWordMargin(int margin);
```
所有的set方法都有对应的get方法，这里就不说了。

### 效果图：
![效果图](https://github.com/donkingliang/LabelsView/blob/master/%E6%95%88%E6%9E%9C%E5%9B%BE.gif)  
想要了解该控件的具体实现的同学，欢迎访问[我的博客](http://blog.csdn.net/u010177022)  
[Android自定义标签列表控件LabelsView解析](http://blog.csdn.net/u010177022/article/details/60324117)
