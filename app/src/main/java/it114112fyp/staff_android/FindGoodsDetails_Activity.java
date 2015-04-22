package it114112fyp.staff_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindGoodsDetails_Activity extends Activity {

    private SimpleAdapter lstGoodsLogAd;

    private ProgressDialog progressDialog;

    private LinearLayout layLinearLogs;
    private ListView lstGoodsLog;
    private TextView txtViewId, txtViewWeight, txtViewFragile, txtViewFlammable, txtViewLocation, txtViewOrderTime, txtViewLastUpdateTime;
    private EditText edTxtGoodsId;
    private Button btnFindGoods;

    private List<HashMap<String, Object>> logList = new ArrayList<>();

    private Boolean fragile, flammable = false;
    private String goodId, location, orderTime, lastUpdateTime = "";
    private double weight = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_goods_details);
        findAllView();
        setAllView();
    }

    public void findAllView() {
        layLinearLogs = (LinearLayout) findViewById(R.id.layLinearLogs);
        lstGoodsLog = (ListView) findViewById(R.id.lstGoodsLog);
        txtViewId = (TextView) findViewById(R.id.txtViewGoodsId);
        txtViewWeight = (TextView) findViewById(R.id.txtViewWeight);
        txtViewFragile = (TextView) findViewById(R.id.txtViewFragile);
        txtViewFlammable = (TextView) findViewById(R.id.txtViewFlammable);
        txtViewLocation = (TextView) findViewById(R.id.txtViewLocation);
        txtViewOrderTime = (TextView) findViewById(R.id.txtViewOrderTime);
        txtViewLastUpdateTime = (TextView) findViewById(R.id.txtViewLastUpdateTime);
        edTxtGoodsId = (EditText) findViewById(R.id.edTxtGoodsId);
        btnFindGoods = (Button) findViewById(R.id.btnFindGoods);
    }

    public void setAllView() {
        layLinearLogs.setVisibility(View.INVISIBLE);
        btnFindGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goodId = edTxtGoodsId.getText().toString();
                progressDialog = ProgressDialog.show(FindGoodsDetails_Activity.this, "Loading...", "Getting Goods Details...", true);
                new Thread(sendGetGoodDetailRequest).start();
            }
        });
        lstGoodsLogAd = new SimpleAdapter(
                getApplicationContext(), logList, R.layout.goods_log_item,
                new String[]{"time", "action", "staff"},
                new int[]{R.id.txtViewTime, R.id.txtViewAction, R.id.txtViewStaff});
        lstGoodsLog.setAdapter(lstGoodsLogAd);
        setTextView();
    }

    private void setTextView() {
        txtViewId.setText(goodId);
        txtViewWeight.setText(weight + "");
        txtViewFragile.setText(fragile + "");
        txtViewFlammable.setText(flammable + "");
        txtViewLocation.setText(location);
        txtViewOrderTime.setText(orderTime);
        txtViewLastUpdateTime.setText(lastUpdateTime);
    }

    Thread sendGetGoodDetailRequest = new Thread() {
        public void run() {
            final JSONObject result = HTTPRequest.goods_get_details(goodId);
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        JSONObject goodsObject = result.getJSONObject("content");
                        fragile = goodsObject.getBoolean("fragile");
                        flammable = goodsObject.getBoolean("flammable");
                        weight = goodsObject.getDouble("weight");
                        JSONObject locationObject = goodsObject.getJSONObject("location");
                        location = locationObject.getString("type").equals("Shop") ? locationObject.getString("short_name") : locationObject.getString("long_name");
                        orderTime = DateFormator.getDateTime(goodsObject.getString("created_at"));
                        lastUpdateTime = DateFormator.getDateTime(goodsObject.getString("updated_at"));
                        JSONArray locationArray = goodsObject.getJSONArray("check_logs");
                        logList.clear();
                        for (int i = 0; i < locationArray.length(); i++) {
                            JSONObject logsObject = locationArray.getJSONObject(i);
                            HashMap logMap = new HashMap();
                            logMap.put("time", DateFormator.getDateTime(logsObject.getString("time")));
                            logMap.put("action", logsObject.getString("check_action"));
                            JSONObject staffObject = logsObject.getJSONObject("staff");
                            logMap.put("staff", staffObject.getString("name"));
                            logList.add(logMap);
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                setTextView();
                                lstGoodsLogAd.notifyDataSetChanged();
                                layLinearLogs.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Fail to server. \nPlease try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(new Runnable() {
                    public void run() {
                        setTextView();
                        lstGoodsLogAd.notifyDataSetChanged();
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };
}
