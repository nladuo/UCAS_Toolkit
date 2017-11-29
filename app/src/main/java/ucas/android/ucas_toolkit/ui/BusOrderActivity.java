package ucas.android.ucas_toolkit.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucas.android.ucas_toolkit.R;
import ucas.android.ucas_toolkit.view.ListViewDialog;

/**
 * Created by kalen on 2017/11/29.
 */

public class BusOrderActivity extends AppCompatActivity {

    ListView listView;
    SimpleAdapter adapter;

    private String[] times = {
            "[1]:2017-11-29 Wednesday",
            "[2]:2017-11-30 Thursday",
            "[3]:2017-12-01 Friday",
            "[4]:2017-12-02 Saturday"
    };

    int last_item = -1;
    View oldView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_order);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){}

        listView = findViewById(R.id.bus_listview);
        initAdapter();
        listView.setAdapter(adapter);



        final List<String> avaliable_orders = new ArrayList<>();
        avaliable_orders.add("[1]:雁栖湖—玉泉路7:00");
        avaliable_orders.add("[2]:雁栖湖—奥运村7:00");
        avaliable_orders.add("[3]:雁栖湖—玉泉路13:00");
        avaliable_orders.add("[4]:雁栖湖—玉泉路15:40");
        avaliable_orders.add("[5]:玉泉路—雁栖湖6:30");
        avaliable_orders.add("[6]:玉泉路—雁栖湖10:00");
        avaliable_orders.add("[7]:玉泉路—雁栖湖15:00");
        avaliable_orders.add("[8]:奥运村—雁栖湖15:50");
        avaliable_orders.add("[9]:助教班车");


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final ListViewDialog dialog = new ListViewDialog(BusOrderActivity.this,
                        avaliable_orders, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        view.setBackgroundColor(getResources().getColor(R.color.list_background));//把当前选中的条目加上选中效果
                        if (last_item != -1 && last_item != position) {//如果已经单击过条目并且上次保存的item位置和当前位置不同
                            oldView.setBackgroundColor(getResources().getColor(R.color.list_background_white));//把上次选中的样式去掉
                        }
                        last_item = position;
                        oldView = view;
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(BusOrderActivity.this, "您选择了：" + avaliable_orders.
                                get(last_item), Toast.LENGTH_LONG).show();
                    }
                });
                dialog.setTitle("班车选择");
                dialog.show();
            }
        });

    }

    private void initAdapter() {

        List<Map<String, Object>> listems = new ArrayList<>();
        for (int i = 0; i < times.length; i++) {
            Map<String, Object> listem = new HashMap<>();
            listem.put("time", times[i]);
            listems.add(listem);
        }

        adapter = new SimpleAdapter(this, listems,
                R.layout.item_bus_time, new String[] { "time" },
                new int[] {R.id.item_bus_time});
    }
}
