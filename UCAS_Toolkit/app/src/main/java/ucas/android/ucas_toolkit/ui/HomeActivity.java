package ucas.android.ucas_toolkit.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import ucas.android.ucas_toolkit.R;


/**
 * Created by kalen on 2017/11/28.
 */

public class HomeActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){}

        findViewById(R.id.home_item_bus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, BusOrderActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            backPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 提示是否退出
     */
    void backPressed() {
        final AlertDialog.Builder alert  = new AlertDialog.Builder(this);
        alert.setTitle("提示");
        alert.setMessage("您确定要退出吗");
        alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
