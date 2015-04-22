package it114112fyp.staff_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class ShopFunction_Activity extends Activity {

    public static JSONArray scannedGoods;

    private ProgressDialog progressDialog;

    private Button btnFindOrder, btnFindGoods;

    private String taskType, taskAction;
    private int taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_function);
        Intent self = getIntent();
        taskId = self.getIntExtra("taskId", 0);
        progressDialog = ProgressDialog.show(ShopFunction_Activity.this, "Loading...", "Connecting...", true);
        new SendGetTaskDetailsRequest().start();
        findAllView();
        setAllView();
    }

    private void findAllView() {
        btnFindGoods = (Button) findViewById(R.id.btnFindGoods);
        btnFindOrder = (Button) findViewById(R.id.btnFindOrder);

    }

    private void setAllView() {
        btnFindOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FindOrderDetails_Activity.class);
                i.putExtra("activity", "Shop");
                i.putExtra("taskId", taskId);
                i.putExtra("action", taskAction);
                startActivity(i);
            }
        });
        btnFindGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FindGoodsDetails_Activity.class));
            }
        });

    }

    class SendGetTaskDetailsRequest extends Thread {
        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.task_get_details(taskId);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        final JSONObject taskObject = result.getJSONObject("content");
                        taskType = taskObject.getString("type");
                        taskAction = taskObject.getString("check_action");
                        scannedGoods = taskObject.getJSONArray("scanned_goods");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(),
                                            result.getString("error"), Toast.LENGTH_SHORT).show();
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
                            try {
                                Toast.makeText(getApplicationContext(), "Fail connect server.\nPlease check your network status", Toast.LENGTH_SHORT)
                                        .show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    }
}
