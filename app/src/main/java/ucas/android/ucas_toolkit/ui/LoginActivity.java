package ucas.android.ucas_toolkit.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rey.material.widget.CheckBox;

import java.io.IOException;

import ucas.android.ucas_toolkit.R;
import ucas.android.ucas_toolkit.crawler.WebMethod;
import ucas.android.ucas_toolkit.model.ConstVal;

public class LoginActivity extends AppCompatActivity {

    EditText unameEdit;
    EditText passEdit;
    CheckBox savePassCkb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

                    //start asynctask
                    LoginAsyncTask task = new LoginAsyncTask(username, password);
                    task.execute();
                }
            }
        });
    }


    class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private String username;
        private String password;

        private ProgressDialog progressDialog;

        public LoginAsyncTask(String username, String password){
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "请等待...", "正在登陆中...", true, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            System.out.println(username + "--" + password);

            WebMethod.init();
            boolean ticketStatus = false;
            try {
                WebMethod.login(username, password);
                ticketStatus = ConstVal.buyTicket.ticketLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return WebMethod.getAuthState() && ticketStatus;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            progressDialog.dismiss();
            if(success) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "登陆失败，请确定密码是否正确", Toast.LENGTH_LONG).show();
            }

        }
    }
}
