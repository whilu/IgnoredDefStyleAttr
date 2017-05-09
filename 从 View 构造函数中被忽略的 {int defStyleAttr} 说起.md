## 前言

一个好的 APP 总是不断演进，版本迭代的同时跟随着产品形态的变化，自定义 View 算是 Android 开发中常用满足产品需求的技巧之一。

- 定义 ```ddeclare-styleable``` 中的自定义属性；
- 构造函数，初始化自定义属性；
- 实现 ```onMeasure```、```onLayout``` 和 ```onDraw``` 等方法。

使用上面这几个步骤，根据自己的具体逻辑，一个自定义 View 就可以简单使用了。现在要关注的是一个不起眼的家伙，构造函数中的 ```defStyleAttr``` 参数。

## 实例

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

大意是：当前主题中一个包含 style 资源引用(Style 中有该 View 默认属性值集合)的值。如果是 0 则不会寻找默认属性值。

对上面的解释进行验证，分别使用第二个和第三个构造方法：

```java
Button button1 = new Button(this, null);
Button button2 = new Button(this, null, 0);
button1.setText("button1");
button2.setText("button2");
```

运行可以发现 button1 有 Button 预置的属性，而 button2 没有。