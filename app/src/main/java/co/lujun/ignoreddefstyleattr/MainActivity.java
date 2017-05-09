package co.lujun.ignoreddefstyleattr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        Button button1 = new Button(this, null  );
        Button button2 = new Button(this, null, 0);
        button1.setText("button1");
        button2.setText("button2");

        linearLayout.addView(button1);
        linearLayout.addView(button2);
    }
}
