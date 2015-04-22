package it114112fyp.staff_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class FindOrderDetails_Activity extends Activity {

    public static JSONObject orderObject;
    public static JSONArray goodsId;
    public static JSONArray scannedGoods;

    private SimpleAdapter goodInfoAdapter;

    private ProgressDialog progressDialog;

    private LinearLayout layLinearGoods;
    private ListView lstGoods;
    private TextView txtViewOrderId, txtViewSender, txtViewReceiver, txtViewDeparture, txtViewDestination, txtViewOrderTime, txtViewUpdateTime, txtViewGoodsNumber,
            txtViewState;
    private Button btnEditOrder, btnFindOrder, btnReceiveOrder, btnIssueOrder;

    private List<HashMap<String, Object>> goodInfo = new ArrayList<>();
    private HashMap<String, Bitmap> image = new HashMap<>();

    private String activity, action;
    private int orderId, taskId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_order_details);
        Intent self = getIntent();
        activity = self.getStringExtra("activity");
        action = self.getStringExtra("action");
        if (activity.equals("Shop"))
            scannedGoods = ShopFunction_Activity.scannedGoods;
        taskId = self.getIntExtra("taskId", 0);
        findAllView();
        setAllView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            progressDialog = ProgressDialog.show(FindOrderDetails_Activity.this, "Loading...", "Getting Order Details ...", true);
            new Thread(SendGetOrderDetailsRequest).start();
        }
    }

    private void findAllView() {
        layLinearGoods = (LinearLayout) findViewById(R.id.layLinearGoods);
        lstGoods = (ListView) findViewById(R.id.goodslistView);
        txtViewOrderId = (TextView) findViewById(R.id.txtViewOrderId);
        txtViewSender = (TextView) findViewById(R.id.senderNameTextView);
        txtViewDeparture = (TextView) findViewById(R.id.orderDepartureTextView);
        txtViewReceiver = (TextView) findViewById(R.id.receiverNameTextView);
        txtViewDestination = (TextView) findViewById(R.id.orderDestinationTextView);
        txtViewOrderTime = (TextView) findViewById(R.id.orderTimeTextView);
        txtViewUpdateTime = (TextView) findViewById(R.id.orderUpdatetimeTextView);
        txtViewState = (TextView) findViewById(R.id.orderStateTextView);
        txtViewGoodsNumber = (TextView) findViewById(R.id.orderGoodsNumberTextView);
        btnEditOrder = (Button) findViewById(R.id.orderEditButton);
        btnFindOrder = (Button) findViewById(R.id.findOrderBtn);
        btnIssueOrder = (Button) findViewById(R.id.btnIssueOrder);
        btnReceiveOrder = (Button) findViewById(R.id.btnReceiveOrder);
    }

    private void setAllView() {
        layLinearGoods.setVisibility(View.INVISIBLE);
        btnIssueOrder.setVisibility(View.GONE);
        btnReceiveOrder.setVisibility(View.GONE);
        btnEditOrder.setVisibility(View.GONE);
        btnEditOrder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EditOrder_Activity.class);
                i.putExtra("activity", "FindOrder");
                startActivityForResult(i, 1);
            }
        });
        lstGoods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), GoodsDetails_Activity.class);
                i.putExtra("goodsId", goodInfo.get(position).get("good_id").toString());
                i.putExtra("orderId", orderId);
                i.putExtra("action", "edit good");
                startActivity(i);
            }
        });
        btnFindOrder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(FindOrderDetails_Activity.this);
                dialog.setTitle("Make your selection");
                final EditText editText = new EditText(getApplicationContext());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setBackgroundColor(Color.WHITE);
                editText.setTextColor(Color.BLACK );
                dialog.setView(editText);
                dialog.setPositiveButton("Find", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        orderId = Integer.parseInt(editText.getText().toString());
                        if (orderId > 0) {
                            progressDialog = ProgressDialog.show(FindOrderDetails_Activity.this, "Loading...", "Getting Order Details...", true);
                            new Thread(SendGetOrderDetailsRequest).start();
                        }
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        btnReceiveOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ReceiveOrder_Activity.class);
                i.putExtra("activity", "FindOrder");
                i.putExtra("taskId", taskId);
                i.putExtra("orderId", orderId);
                i.putExtra("action", action);
                startActivityForResult(i, 1);
            }
        });
        btnIssueOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), IssueOrder_Activity.class);
                i.putExtra("activity", "FindOrder");
                i.putExtra("taskId", taskId);
                i.putExtra("orderId", orderId);
                i.putExtra("action", action);
                startActivityForResult(i, 1);
            }
        });
        goodInfoAdapter = new SimpleAdapter(
                getApplicationContext(), goodInfo, R.layout.order_goods_list_item,
                new String[]{"picture", "good_id"},
                new int[]{R.id.goodImgV, R.id.goodIdInLv});
        goodInfoAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
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
        lstGoods.setAdapter(goodInfoAdapter);
    }

    private void setTextView() {
        try {
            txtViewOrderId.setText(orderObject.getInt("id") + "");
            txtViewSender.setText(orderObject.getJSONObject("sender").getString("name"));
            txtViewReceiver.setText(orderObject.getJSONObject("receiver").getString("name"));
            JSONObject destinationObject = orderObject.getJSONObject("destination");
            JSONObject departureObject = orderObject.getJSONObject("departure");
            txtViewDeparture.setText(departureObject.getString("type").equals("Shop")?departureObject.getString("short_name") : departureObject.getString("long_name"));
            txtViewDestination.setText(destinationObject.getString("type").equals("Shop")?destinationObject.getString("short_name") : destinationObject.getString("long_name"));
            txtViewGoodsNumber.setText("" + orderObject.getInt("goods_number"));
            txtViewOrderTime.setText(DateFormator.getDateTime(orderObject.getString("created_at")));
            txtViewUpdateTime.setText(DateFormator.getDateTime(orderObject.getString("updated_at")));
            String state = orderObject.getString("order_state");
            txtViewState.setText(state);
            if (state.equals("submitted")) {
                btnEditOrder.setVisibility(View.VISIBLE);
                layLinearGoods.setVisibility(View.INVISIBLE);
                btnReceiveOrder.setVisibility(View.GONE);
                btnIssueOrder.setVisibility(View.GONE);
            } else if (state.equals("confirmed")) {
                btnEditOrder.setVisibility(View.VISIBLE);
                layLinearGoods.setVisibility(View.VISIBLE);
                btnReceiveOrder.setVisibility(View.VISIBLE);
                btnIssueOrder.setVisibility(View.GONE);
            } else if (state.equals("sending")) {
                btnEditOrder.setVisibility(View.GONE);
                layLinearGoods.setVisibility(View.VISIBLE);
                btnReceiveOrder.setVisibility(View.GONE);
                btnIssueOrder.setVisibility(View.VISIBLE);
            } else {
                btnEditOrder.setVisibility(View.GONE);
                layLinearGoods.setVisibility(View.VISIBLE);
                btnReceiveOrder.setVisibility(View.GONE);
                btnIssueOrder.setVisibility(View.GONE);
            }
            if (activity.equals("more")) {
                btnIssueOrder.setVisibility(View.GONE);
                btnReceiveOrder.setVisibility(View.GONE);
            }
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
                        FindOrderDetails_Activity.orderObject = orderObject;
                        goodsId = orderObject.getJSONArray("goods_ids");
                        if (goodsId.length() != 0) {
                            goodInfo.clear();
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
                                    Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT)
                                            .show();
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
                        try {
                            goodInfoAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };
}