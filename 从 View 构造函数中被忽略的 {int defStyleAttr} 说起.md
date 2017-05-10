## 前言

一个好的 APP 总是不断演进，版本迭代的同时跟随着产品形态的变化，自定义 View 算是 Android 开发中常用满足产品需求的技巧之一。

- 定义 ```ddeclare-styleable``` 中的自定义属性；
- 构造函数，初始化自定义属性；
- 实现 ```onMeasure```、```onLayout``` 和 ```onDraw``` 等方法。

使用上面这几个步骤，根据自己的具体逻辑，一个自定义 View 就可以简单使用了。现在要关注的是一个不起眼的家伙，构造函数中的 ```defStyleAttr``` 参数。

## 探寻

首先看看 Button 源码中的几个构造方法：

```java
public Button(Context context) {
    this(context, null);
}

public Button(Context context, AttributeSet attrs) {
    this(context, attrs, com.android.internal.R.attr.buttonStyle);
}

public Button(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
}

public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
}
```

可以观察到第一个构造方法调用的是第二个构造方法，第二个调用的是第三个构造方法，最终前三个构造方法调用都是第四个构造方法。这里有几种情况：

- 使用 ```new Button(context)``` 直接实例化一个 Button 会调用第一个方法；
- 在 .xml 文件中使用 Button 调用第二个构造方法。

这里重点关注第二个构造方法。方法的第一参数是 Context，第二个参数是我们自定义属性的集合，那么第三个参数是什么？进入源码继续看，最后来到 View 的代码，看到对于该参数的解释：

```
An attribute in the current theme that contains a reference to a style resource that supplies default values for the view. Can be 0 to not look for defaults.
```

大意是：当前主题中一个包含 style 资源引用(Style 中有该 View 默认属性值集合)的值，这个引用对应的资源属性/值会填充 attrs 中没有声明的属性。如果是 0 则不会寻找默认属性值填充。

对上面的解释进行验证，分别使用第二个和第三个构造方法：

```java
Button button1 = new Button(this, null);
Button button2 = new Button(this, null, 0);
button1.setText("button1");
button2.setText("button2");
```

效果：

<img src="https://raw.githubusercontent.com/whilu/lujun.co-storge/master/image/781494423801_.pic.jpg" width="311" height="57" />

运行可以发现 button1 有 Button 预置的一些基础属性(如背景、点击效果等)，而 button2 没有。其中 button1 的预置属性从 ```com.android.internal.R.attr.buttonStyle``` 中获得。

更近一步，我们知道了这个参数是为一个 View 提供基础的属性，下面尝试实现这样的功能：

- 定义一个 attribute

```xml
<resources>
    <declare-styleable name="AppTheme">
        <attr name="myButtonStyle" format="reference" />
    </declare-styleable>
</resources>
```

- 在我们当前的 Theme 中，为上面定义的 attribute 添加一个 style

```xml
<resources>
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <item name="myButtonStyle">@style/MyButtonStyle</item>
    </style>
    <style name="MyButtonStyle" parent="@style/Widget.AppCompat.Button">
        <item name="android:textColor">@android:color/holo_red_dark</item>
    </style>
</resources>
```

其中 style 继承自 Button style，但修改了 Button 文字颜色为红色。

- 在自定义 View 中使用自定义 attribute

```java
public class MyButton extends Button {

    public MyButton(Context context) {
        this(context, null);
    }

    public MyButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.myButtonStyle);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
```

最后在 .xml 中使用 MyButton，由于默认填充使用了 Button style(文字颜色被修改为红色)，所以样式如下：

<img src="https://raw.githubusercontent.com/whilu/lujun.co-storge/master/image/811494434509_.pic.jpg" width="196" height="82" />

看完 ```defStyleAttr```，第四个构造方法中的 ```defStyleRes``` 参数又引起了我们的注意，进入 View 的源码，同样可以看到对于该参数的解释：

```
A resource identifier of a style resource that supplies default values for the view, used only if defStyleAttr is 0 or can not be found in the theme. Can be 0 to not look for defaults.
```

大意是：为 View 提供默认值的一个样式资源标识符(不局限于当前 Theme 中)，仅在 ```defStyleAttr``` 为 0 或提供的样式资源无法找到的时候使用。如果设置为 0 无效。

继续看：

```java
Button button3 = new Button(this, null, 0, 0);
Button button4 = new Button(this, null, 0, android.R.style.Widget_Button_Small);
button3.setText("button3");
button4.setText("button4");
```

效果(连同第一、二种情况对比)：

<img src="https://raw.githubusercontent.com/whilu/lujun.co-storge/master/image/791494427270_.pic.jpg" width="312" height="109" />

这里设置 defStyleRes 为  ```android.R.style.Widget_Button_Small``` style，相比默认的 Button style 有区别。

## 分析

## 参考

- [http://blog.danlew.net/2016/07/19/a-deep-dive-into-android-view-constructors/](http://blog.danlew.net/2016/07/19/a-deep-dive-into-android-view-constructors/)




