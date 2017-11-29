package ucas.android.ucas_toolkit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rey.material.widget.CheckBox;

import ucas.android.ucas_toolkit.R;
import ucas.android.ucas_toolkit.model.ConstVal;

public class LoginActivity extends AppCompatActivity {

    EditText unameEdit;
    EditText passEdit;
    CheckBox savePassCkb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        Button btn = findViewById(R.id.click_btn);
//
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(LoginActivity.this, "点击了button", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                intent.putExtra("key", "value");
//                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//            }
//        });

        unameEdit = findViewById(R.id.login_username_edit);
        passEdit = findViewById(R.id.login_pass_edit);
        savePassCkb = findViewById(R.id.login_is_save_pass);

        SharedPreferences preferences = getSharedPreferences(ConstVal.USER_SHARE_PREFERENCE, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        boolean isChecked = preferences.getBoolean("isChecked", true);
        unameEdit.setText(username);
        passEdit.setText(password);
        savePassCkb.setChecked(isChecked);

        findViewById(R.id.login_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = unameEdit.getText().toString();
                final String password = passEdit.getText().toString();
                if (username.equals("")){
                    Toast.makeText(LoginActivity.this,
                            "请输入用户名", Toast.LENGTH_LONG).show();
                }else if (password.equals("")) {
                    Toast.makeText(LoginActivity.this,
                            "请输入密码", Toast.LENGTH_LONG).show();
                }else{
                    // 保存用户信息
                    SharedPreferences.Editor editor = getSharedPreferences(
                            ConstVal.USER_SHARE_PREFERENCE, MODE_PRIVATE).edit();
                    if(savePassCkb.isChecked()){
                        editor.putString("username", username);
                        editor.putString("password", password);
                    }else{
                        editor.putString("username", "");
                        editor.putString("password", "");
                    }
                    editor.putBoolean("isChecked",savePassCkb.isChecked());
                    editor.apply();

                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    //start asynctask
//                    LoginAsyncTask task = new LoginAsyncTask(username, password);
//                    task.execute();

                }

            }
        });

    }
}
