package ucas.android.ucas_toolkit.ui;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucas.android.ucas_toolkit.R;
import ucas.android.ucas_toolkit.model.ConstVal;
import ucas.android.ucas_toolkit.view.ListViewDialog;

/**
 * Created by kalen on 2017/11/29.
 */

public class BusOrderActivity extends AppCompatActivity {

    ListView listView;
    SimpleAdapter adapter;


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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BusRouteAsyncTask task = new BusRouteAsyncTask(position);
                task.execute();
            }
        });

    }

    private void initAdapter() {

        List<Map<String, Object>> listems = new ArrayList<>();
        for (int i = 0; i < ConstVal.buyTicket.ticketDate().size(); i++) {
            Map<String, Object> listem = new HashMap<>();
            listem.put("time", ConstVal.buyTicket.ticketDate().get(i));
            listems.add(listem);
        }

        adapter = new SimpleAdapter(this, listems,
                R.layout.item_bus_time, new String[] { "time" },
                new int[] {R.id.item_bus_time});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    class BusRouteAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private int index;

        private ProgressDialog progressDialog;

        public BusRouteAsyncTask(int index){
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(BusOrderActivity.this,
                    "请等待...", "加载中...", true, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return ConstVal.buyTicket.fetchBusRouteData(index);

        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            progressDialog.dismiss();

            if (success) {

                final ListViewDialog dialog = new ListViewDialog(BusOrderActivity.this,
                        ConstVal.buyTicket.getRouteList(), new AdapterView.OnItemClickListener() {
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
                        if (last_item != -1) {
                            PayAsyncTask task = new PayAsyncTask(last_item);
                            task.execute();
                            last_item = -1; // 重新置为-1
                        } else {
                            Toast.makeText(BusOrderActivity.this, "您还没选择班车", Toast.LENGTH_LONG).show();
                        }

                    }
                });
                dialog.setTitle("班车选择");
                dialog.show();

            } else {
                Toast.makeText(BusOrderActivity.this, "获取数据失败", Toast.LENGTH_LONG).show();
            }
        }
    }



    class PayAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private int index;

        private ProgressDialog progressDialog;

        public PayAsyncTask(int index){
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(BusOrderActivity.this,
                    "请等待...", "加载中...", true, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = ConstVal.buyTicket.CheckRemainSeat(index);
            if (!status) {
                return false;
            }
            try {
                ConstVal.buyTicket.ConfigPayment();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            progressDialog.dismiss();

            if (success) {

                try {
                    String payUrl = URLDecoder.decode(ConstVal.buyTicket.getPaymentUrl(), "GBK");
                    System.out.println("PaymentUrl-->" + payUrl);
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);  // 复制到剪切板
                    cm.setText(payUrl);
                    Toast.makeText(BusOrderActivity.this, "支付链接已复制到剪切板", Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(BusOrderActivity.this, "发生异常", Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(BusOrderActivity.this, "获取支付信息失败", Toast.LENGTH_LONG).show();
            }
        }
    }


}
