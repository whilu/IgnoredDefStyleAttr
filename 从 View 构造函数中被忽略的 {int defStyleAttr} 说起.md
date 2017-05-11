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

> An attribute in the current theme that contains a reference to a style resource that supplies default values for the view. Can be 0 to not look for defaults.

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

> A resource identifier of a style resource that supplies default values for the view, used only if defStyleAttr is 0 or can not be found in the theme. Can be 0 to not look for defaults.

大意是：为 View 提供默认值的一个样式资源标识符(不局限于当前 Theme 中)，仅在 ```defStyleAttr``` 为 0 或 ```defStyleAttr``` 指定的 style 中无法找到默认值。如果设置为 0 无效。

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

利用 Context 的 ```obtainStyledAttributes``` 方法，可以将属性值取回到一个 TypedArray 中([为什么使用 TypedArray](http://blog.csdn.net/lmj623565791/article/details/45022631))。

一个 attribute 值的确定过程大致如下：

1. xml 中查找，若未找到进入第 2 步；
2. xml 中的 style 查找，若未找到进入第 3 步；
3. 若 defStyleAttr 不为 0，由 defStyleAttr 指定的 style 中寻找，若未找到进入第 4 步；
4. 若 defStyleAttr 为 0 或 defStyleAttr 指定的 style 中寻找失败，进入 defStyleRes 指定的 style 中寻找，若寻找失败，进入第 5 步查找；
5. 查找在当前 Theme 中指定的属性值。

进入 TextView 的源码，一路找寻 ```obtainStyledAttributes``` 的调用链，如下(tl;dr)：

TextView

```java
public TextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
	super(context, attrs, defStyleAttr, defStyleRes);
    // ...
}
```

View

```java
public View(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
	// ...
	final TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View, defStyleAttr, defStyleRes);
	// ...
}
```

Context

```java
public final TypedArray obtainStyledAttributes(AttributeSet set, @StyleableRes int[] attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
	return getTheme().obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
}
```

Resource.Theme

```java
public TypedArray obtainStyledAttributes(AttributeSet set, @StyleableRes int[] attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
	return mThemeImpl.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes);
}
```

来到 Resource 类，该类的作用就是帮助我们获取 Application 的资源，其中内部类 Theme 持有当前主题中所有定义的属性值(也就是上面说到的确定 attribute 值的第 5 步)。方法中调用了 ```mThemeImpl``` 的 ```obtainStyledAttributes``` 方法，ThemeImpl 类就是 Theme 类的"实现"，进入到 ThemeImpl 类 ```obtainStyledAttributes``` 方法：

```java
TypedArray obtainStyledAttributes(@NonNull Resources.Theme wrapper, AttributeSet set, @StyleableRes int[] attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    synchronized (mKey) {
        final int len = attrs.length;
        final TypedArray array = TypedArray.obtain(wrapper.getResources(), len);

        // XXX note that for now we only work with compiled XML files.
        // To support generic XML files we will need to manually parse
        // out the attributes from the XML file (applying type information
        // contained in the resources and such).
        final XmlBlock.Parser parser = (XmlBlock.Parser) set;
        AssetManager.applyStyle(mTheme, defStyleAttr, defStyleRes, parser != null ? parser.mParseState : 0, attrs, array.mData, array.mIndices);
        array.mTheme = wrapper;
        array.mXml = parser;

        return array;
    }
}
```

这里 obtain 了我们需要的 TypedArray，根据之前说过的规则通过调用 AssetManager 的 ```applyStyle``` 方法(本地方法)，确定了最后各个 attribute 的值。

下面看看 [android_util_AssetManager.cpp](https://android.googlesource.com/platform/frameworks/base.git/+/android-4.3_r3.1/core/jni/android_util_AssetManager.cpp) 中 ```android_content_AssetManager_applyStyle``` 函数的源码，里面有我们需要的 native ``` applyStyle``` 方法(代码很长，只保留了注释)：

```cpp
static jboolean android_content_AssetManager_applyStyle(JNIEnv* env, jobject clazz, jint themeToken, jint defStyleAttr, jint defStyleRes, jint xmlParserToken, jintArray attrs, jintArray outValues, jintArray outIndices)
{
    // ...
    // Retrieve the style class associated with the current XML tag.
    // 检索与当前 XML 标签关联的样式类
    // ...
    // Now lock down the resource object and start pulling stuff from it.
    // 锁定资源对象并开始从其中抽取所需要的内容
    // ...
    // Retrieve the default style bag, if requested.
    // 如有需要取出默认样式
    //...
    // Retrieve the XML attributes, if requested.
    // 如有需要检索 XML 属性
    // ...
    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    // 遍历客户端请求的所有属性，填充每个可以找到的数据
    // ...
    for (// ...) {
    	// ...
        // Try to find a value for this attribute...  we prioritize values
        // coming from, first XML attributes, then XML style, then default
        // style, and finally the theme.
        // 尝试找到这个属性的值... 优先级：
        // 首先是 XML 中定义的，其次是 XML 中的 style 定义的，然后是默认样式，最后是主题
        // ...
    }
    return JNI_TRUE;
}
```

到此，attribute 值的查找过程结束。attribute 值的确定是按照一系列规则来最终确定的。

接下来，我们看看 TypedArray 这个类。

## 示例源码

[https://github.com/whilu/IgnoredDefStyleAttr](https://github.com/whilu/IgnoredDefStyleAttr)

## 参考

- [http://blog.danlew.net/2016/07/19/a-deep-dive-into-android-view-constructors/](http://blog.danlew.net/2016/07/19/a-deep-dive-into-android-view-constructors/)




