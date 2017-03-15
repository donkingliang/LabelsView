LabelsView
======
标签列表控件的使用介绍。
### 引入
```Groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
dependencies {
	    compile 'com.github.donkingliang:LabelsView:1.0.0'
	}
```
## 使用
### 编写布局：
```xml
<com.donkingliang.labels.LabelsView
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/labels"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:labelBackground="@drawable/pink_frame_bg"  //标签的背景
        app:labelTextColor="#fb435b"  //标签的字体颜色
        app:labelTextSize="14sp"  //标签的字体大小
        app:labelTextPaddingBottom="5dp"  //标签的上下左右边距
        app:labelTextPaddingLeft="10dp"
        app:labelTextPaddingRight="10dp"
        app:labelTextPaddingTop="5dp"
        app:lineMargin="10dp"  //行与行的距离
        app:wordMargin="10dp" />  //标签与标签的距离
```

### 设置标签：
```java
    ArrayList<String> list = new ArrayList<>();
        list.add("Android");
        list.add("IOS");
        list.add("前端");
        list.add("后台");
        list.add("微信开发");
        list.add("Java");
        list.add("JavaScript");
        list.add("C++");
        list.add("PHP");
        list.add("Python");
    labelsView.setLabels(list);
```

### 设置点击监听：(如果需要的话)
```java
    labels.setOnLabelClickListener(new LabelsView.OnLabelClickListener() {
            @Override
            public void onLabelClick(TextView label, int position) {
                //label就是被点击的标签，position就是标签的位置。
            }
        });
```

### 效果图：
![](https://github.com/donkingliang/LabelsView/blob/master/%E6%95%88%E6%9E%9C%E5%9B%BE.png)  
想要了解该控件的具体实现的同学，欢迎访问[我的博客](http://blog.csdn.net/u010177022/article/details/60324117)  
