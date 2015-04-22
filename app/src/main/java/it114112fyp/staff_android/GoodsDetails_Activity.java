package it114112fyp.staff_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoodsDetails_Activity extends Activity {

    private SimpleAdapter lstGoodsLogAd;

    private ProgressDialog progressDialog;

    private ListView lstGoodsLog;
    private TextView txtViewId, txtViewWeight, txtViewFragile, txtViewFlammable, txtViewLocation, txtViewOrderTime, txtViewLastUpdateTime;

    private List<HashMap<String, Object>> logList = new ArrayList<>();

    private Boolean fragile, flammable = false;
    private String goodId, location, orderTime, lastUpdateTime = "";
    private double weight = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goods_details);
        Intent i = getIntent();
        goodId = i.getStringExtra("goodsId");
        findAllView();
        setAllView();
        progressDialog = ProgressDialog.show(GoodsDetails_Activity.this, "Loading...", "Getting Today Task...", true);
        new Thread(sendGetGoodDetailRequest).start();
    }

    public void findAllView() {
        lstGoodsLog = (ListView) findViewById(R.id.lstGoodsLog);
        txtViewId = (TextView) findViewById(R.id.txtViewGoodsId);
        txtViewWeight = (TextView) findViewById(R.id.txtViewWeight);
        txtViewFragile = (TextView) findViewById(R.id.txtViewFragile);
        txtViewFlammable = (TextView) findViewById(R.id.txtViewFlammable);
        txtViewLocation = (TextView) findViewById(R.id.txtViewLocation);
        txtViewOrderTime = (TextView) findViewById(R.id.txtViewOrderTime);
        txtViewLastUpdateTime = (TextView) findViewById(R.id.txtViewLastUpdateTime);
    }

    public void setAllView() {
        lstGoodsLogAd = new SimpleAdapter(
                getApplicationContext(), logList, R.layout.goods_log_item,
                new String[]{"staff", "action", "time"},
                new int[]{R.id.txtViewStaff, R.id.txtViewAction, R.id.txtViewTime});
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
                        location = goodsObject.getJSONObject("location").getString("short_name");
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
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT).show();
                                    finish();
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
                            finish();
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
