package ucas.android.ucas_toolkit.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucas.android.ucas_toolkit.R;

/**
 * Created by kalen on 2017/11/29.
 */

public class ListViewDialog extends Dialog {

    private final Context mContext;
    private ListView mListView;
    private List<String> mDatas;
    private AdapterView.OnItemClickListener listViewListener;
    private View.OnClickListener confirmBtnLister;


    public ListViewDialog(Context context, List<String> datas, AdapterView.OnItemClickListener listViewListener,
                          View.OnClickListener confirmBtnLister) {
        super(context, android.R.style.Theme_Holo_Light_Dialog);

        mContext = context;
        this.mDatas = datas;
        this.listViewListener = listViewListener;
        this.confirmBtnLister = confirmBtnLister;
        initView();
        initListView();
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.dialog_listview, null);
        mListView = contentView.findViewById(R.id.dialog_lv);
        contentView.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListViewDialog.this.dismiss();
            }
        });
        contentView.findViewById(R.id.dialog_confirm_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListViewDialog.this.dismiss();
                confirmBtnLister.onClick(v);
            }
        });
        setContentView(contentView);
    }

    private void initListView() {

        List<Map<String, Object>> listems = new ArrayList<>();
        for (int i = 0; i < mDatas.size(); i++) {
            Map<String, Object> listem = new HashMap<>();
            listem.put("order", mDatas.get(i));
            listems.add(listem);
        }

        SimpleAdapter adapter = new SimpleAdapter(mContext, listems,
                R.layout.item_bus_order, new String[] { "order" },
                new int[] {R.id.item_bus_order});

        mListView.setAdapter(adapter);


        mListView.setOnItemClickListener(listViewListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
//        setHeight();
    }

    private void setHeight() {
        Window window = getWindow();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (window.getDecorView().getHeight() >= (int) (displayMetrics.heightPixels * 0.6)) {
            attributes.height = (int) (displayMetrics.heightPixels * 0.6);
        }
        window.setAttributes(attributes);
    }
}