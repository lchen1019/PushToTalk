package clqwq.press.push_to_talk;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import clqwq.press.push_to_talk.utils.Address;

public class SettingActivity extends AppCompatActivity {

    private ImageView back;
    private EditText ip;
    private EditText port;
    private EditText name;
    private Button check;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        back = findViewById(R.id.back);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        name = findViewById(R.id.name);
        check = findViewById(R.id.check);
        ip.setText(Address.ip);
        port.setText(Address.port + "");
        name.setText(Address.name);
        addListener();
    }

    public void addListener() {
        // 监听返回
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // 确认按钮
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip_ = String.valueOf(ip.getText());
                int port_ = Integer.parseInt(String.valueOf(port.getText()));
                String name_ = String.valueOf(name.getText());
                Address.ip = ip_;
                Address.port = port_;
                Address.name = name_;
                // 弹出对话框，提示
                AlertDialog alertDialog = new AlertDialog.Builder(SettingActivity.this).create();
                alertDialog.setTitle("提示");
                alertDialog.setMessage("修改成功");
                alertDialog.show();
            }
        });
    }

}