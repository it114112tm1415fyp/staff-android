package it114112fyp.staff_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it114112fyp.util.StaffData;

public class ReceiveOrder_Activity extends Activity {

    public static JSONObject orderInTaskObject;
    public static JSONArray goodsInTask;
    public static JSONObject orderObject;

    private SimpleAdapter goodInfoAdapter;

    private ProgressDialog progressDialog;

    private ListView goodsListView;
    private TextView txtViewOrderId, txtViewSender, txtViewReceiver, txtViewDeparture, txtViewDestination, txtViewOrderTime, txtViewUpdateTime, txtViewGoodsNumber,
            txtViewState;
    private Button btnAddGoods, btnReceiveButton, btnEditOrder;

    private List<HashMap<String, Object>> goodInfo = new ArrayList<>();
    private HashMap<String, Bitmap> image = new HashMap<>();

    private String activity, state;
    private int orderId, taskId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_order);
        Intent self = getIntent();
        orderId = self.getIntExtra("orderId", orderId);
        activity = self.getStringExtra("activity");
        orderInTaskObject = TaskDetails_Activity.orderInTaskObject;
        try {
            goodsInTask = orderInTaskObject.getJSONArray("goods_in_task");
        } catch (Exception e) {
            e.printStackTrace();
        }
        taskId = activity.equals("Task") ? self.getIntExtra("taskId", 0) : StaffData.SERVE_TASK_RECEIVE;
        findAllView();
        setAllView();
        progressDialog = ProgressDialog.show(ReceiveOrder_Activity.this, "Loading...", "Getting Order Details...", true);
        new Thread(SendGetOrderDetailsRequest).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            switch (requestCode) {
                case 1:
                    progressDialog = ProgressDialog.show(ReceiveOrder_Activity.this, "Loading...", "Getting Order Details...", true);
                    new Thread(SendGetOrderDetailsRequest).start();
                    break;
                case 2:
                    finish();
                    break;
            }
        }
    }

    private void findAllView() {
        goodsListView = (ListView) findViewById(R.id.goodslistView);
        txtViewOrderId = (TextView) findViewById(R.id.txtViewOrderId);
        txtViewSender = (TextView) findViewById(R.id.senderNameTextView);
        txtViewDeparture = (TextView) findViewById(R.id.orderDepartureTextView);
        txtViewReceiver = (TextView) findViewById(R.id.receiverNameTextView);
        txtViewDestination = (TextView) findViewById(R.id.orderDestinationTextView);
        txtViewOrderTime = (TextView) findViewById(R.id.orderTimeTextView);
        txtViewUpdateTime = (TextView) findViewById(R.id.orderUpdatetimeTextView);
        txtViewState = (TextView) findViewById(R.id.orderStateTextView);
        txtViewGoodsNumber = (TextView) findViewById(R.id.orderGoodsNumberTextView);
        btnAddGoods = (Button) findViewById(R.id.addGoodsButton);
        btnReceiveButton = (Button) findViewById(R.id.orderConfirmButton);
        btnEditOrder = (Button) findViewById(R.id.orderEditButton);
    }

    private void setAllView() {
        btnEditOrder.setVisibility(View.INVISIBLE);
        btnReceiveButton.setVisibility(View.GONE);
        btnAddGoods.setVisibility(View.GONE);
        btnAddGoods.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AddNewGoods_Activity.class);
                i.putExtra("activity", activity);
                i.putExtra("orderId", orderId);
                i.putExtra("action", "add good");
                i.putExtra("taskId", taskId);
                startActivityForResult(i, 1);
            }
        });
        btnReceiveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (goodInfo.size() <= 0) {
                    Toast.makeText(getApplicationContext(), "Can Not Receive Order If No Goods", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(getApplicationContext(), ConfirmOrder_Activity.class);
                i.putExtra("action", "Receive");
                i.putExtra("orderId", orderId);
                i.putExtra("taskId", taskId);
                startActivityForResult(i, 2);
            }
        });
        btnEditOrder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EditOrder_Activity.class);
                i.putExtra("activity", "ReceiveOrder");
                startActivityForResult(i, 1);
            }
        });
        goodInfo.clear();
        goodInfoAdapter = new SimpleAdapter(
                getApplicationContext(), goodInfo, R.layout.order_goods_list_item,
                new String[]{"picture", "good_id"},
                new int[]{R.id.goodImgV, R.id.goodIdInLv});
        goodInfoAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if ((view instanceof ImageView) && (data instanceof Bitmap)) {
                    ImageView imageView = (ImageView) view;
                    Bitmap bmp = (Bitmap) data;
                    imageView.setImageBitmap(bmp);
                    return true;
                }
                return false;
            }
        });
        goodsListView.setAdapter(goodInfoAdapter);
        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (state.equals("submitted") || state.equals("confirmed")) {
                    Intent i = new Intent(getApplicationContext(), AddNewGoods_Activity.class);
                    i.putExtra("goodsId", goodInfo.get(position).get("good_id").toString());
                    i.putExtra("orderId", orderId);
                    i.putExtra("action", "edit good");
                    startActivityForResult(i, 1);
                } else {
                    Intent i = new Intent(getApplicationContext(), GoodsDetails_Activity.class);
                    i.putExtra("goodsId", goodInfo.get(position).get("good_id").toString());
                    startActivity(i);
                }
            }
        });
    }

    private void setTextView() {
        try {
            txtViewOrderId.setText(orderId + "");
            txtViewSender.setText(orderObject.getJSONObject("sender").getString("name"));
            txtViewReceiver.setText(orderObject.getJSONObject("receiver").getString("name"));
            JSONObject destination = orderObject.getJSONObject("destination");
            JSONObject departure = orderObject.getJSONObject("departure");
            txtViewDeparture.setText(departure.getString("short_name"));
            txtViewDestination.setText(destination.getString("short_name"));
            txtViewGoodsNumber.setText("" + orderObject.getInt("goods_number"));
            txtViewOrderTime.setText(DateFormator.getDateTime(orderObject.getString("created_at")));
            txtViewUpdateTime.setText(DateFormator.getDateTime(orderObject.getString("updated_at")));
            state = orderObject.getString("order_state");
            txtViewState.setText(state);
            btnReceiveButton.setVisibility(state.equals("confirmed") || state.equals("submitted") ? View.VISIBLE : View.GONE);
            btnAddGoods.setVisibility(state.equals("confirmed") || state.equals("submitted") ? View.VISIBLE : View.GONE);
            btnEditOrder.setVisibility(state.equals("confirmed") || state.equals("submitted") ? View.VISIBLE : View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Thread SendGetOrderDetailsRequest = new Thread() {
        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.order_get_details(orderId);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        final JSONObject orderObject = result.getJSONObject("content");
                        ReceiveOrder_Activity.orderObject = orderObject;
                        final JSONArray goodsId = orderObject.getJSONArray("goods_ids");
                        goodInfo.clear();
                        if (goodsId.length() != 0) {
                            for (int i = 0; i < goodsId.length(); i++) {
                                Bitmap bitmap;
                                if (image.containsKey(goodsId.getString(i))) {
                                    bitmap = image.get(goodsId.getString(i));
                                } else {
                                    // Get Goods Image
                                    final JSONObject picture = HTTPRequest.picture_good(goodsId.getString(i));
                                    byte bytes[] = Base64.decode(picture.getJSONObject("content").getString("goods_photo"), Base64.DEFAULT);
                                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    image.put(goodsId.getString(i), bitmap);
                                }
                                HashMap<String, Object> hm = new HashMap<>();
                                hm.put("picture", bitmap);
                                hm.put("good_id", goodsId.getString(i));
                                goodInfo.add(hm);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "No Goods", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTextView();
                                goodInfoAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Fail to get information", Toast.LENGTH_SHORT).show();
                    }
                });
            } finally {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goodInfoAdapter.notifyDataSetChanged();
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };
}