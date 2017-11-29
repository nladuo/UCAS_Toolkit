package ucas.android.ucas_toolkit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

//        setContentView();
//        Toast.makeText(HomeActivity.this, "拿到了:" + getIntent().getStringExtra("key"), Toast.LENGTH_LONG).show();

    }
}
