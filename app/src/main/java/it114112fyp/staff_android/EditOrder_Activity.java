package it114112fyp.staff_android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import it114112fyp.util.StaffData;

public class EditOrder_Activity extends Activity {

    private ProgressDialog progressDialog;

    private LinearLayout layLinearShop;
    private RelativeLayout layLinearAddress;
    private EditText destinationEd, quantityOfGoodsEd;
    private TextView txtViewSender, receiverNameTv, receiverPhoneTv, receiverEmailTv, departureTv, destinationTv;
    private Button editBtn, selectDestinationBtn, selectDepartureBtn, switchAddressBtn;
    private Spinner spnnrRegion;

    private ArrayList<JSONObject> departureList = new ArrayList<>();
    private ArrayList<JSONObject> destinationList = new ArrayList<>();

    private JSONObject order;
    private String departure, departureType, destination, destinationType, receiverType;
    private int orderId, receiverId, departureId, destinationId, destinationRegionId, quantity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent self = getIntent();
        String activity = self.getStringExtra("activity");
        Log.e("activity", activity);
        if (activity.equals("FindOrder"))
            order = FindOrderDetails_Activity.orderObject;
        else if (activity.equals("ReceiveOrder"))
            order = ReceiveOrder_Activity.orderObject;
        setContentView(R.layout.edit_order);
        try {
            orderId = order.getInt("id");
            destination = order.getJSONObject("destination").getString("short_name");
            destinationType = order.getJSONObject("destination").getString("type");
            destinationId = order.getJSONObject("destination").getInt("id");
            destinationRegionId = order.getJSONObject("destination").getJSONObject("region").getInt("id");
            departure = order.getJSONObject("departure").getString("short_name");
            departureType = order.getJSONObject("departure").getString("type");
            departureId = order.getJSONObject("departure").getInt("id");
            receiverType = order.getJSONObject("receiver").getString("type");
        } catch (Exception e) {
            e.printStackTrace();
        }
        findAllView();
        progressDialog = ProgressDialog.show(EditOrder_Activity.this, "Loading...", "Getting Information...", true);
        new Thread(SendGetInformationRequest).start();
        try {
            departureList.clear();
//            destinationList.clear();
            JSONArray departureArray = order.getJSONObject("sender").getJSONArray("specify_addresses");
            for (int i = 0; i < departureArray.length(); i++) {
                departureList.add(departureArray.getJSONObject(i));
            }
//            JSONArray destinationArray = order.getJSONObject("receiver").getJSONArray("specify_addresses");
//            for (int i = 0; i < destinationArray.length(); i++) {
//                destinationList.add(destinationArray.getJSONObject(i));
//            }
            departureList.addAll(StaffData.shopList);
//            destinationList.addAll(StaffData.shopList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAllView() {
        layLinearAddress = (RelativeLayout) findViewById(R.id.layLinearAddress);
        layLinearShop = (LinearLayout) findViewById(R.id.layLinearShop);
        txtViewSender = (TextView) findViewById(R.id.txtViewSender);
        receiverNameTv = (TextView) findViewById(R.id.receiverNameTv);
        receiverPhoneTv = (TextView) findViewById(R.id.receiverPhoneTv);
        receiverEmailTv = (TextView) findViewById(R.id.receiverEmailTv);
        departureTv = (TextView) findViewById(R.id.orderDepartureTv);
        destinationTv = (TextView) findViewById(R.id.orderDestionationTv);
        spnnrRegion = (Spinner) findViewById(R.id.spnnrRegion);
        destinationEd = (EditText) findViewById(R.id.destionationEd);
        quantityOfGoodsEd = (EditText) findViewById(R.id.quantityOfGoodsEd);
        editBtn = (Button) findViewById(R.id.orderEditBtn);
        selectDepartureBtn = (Button) findViewById(R.id.selectDepatureBtn);
        selectDestinationBtn = (Button) findViewById(R.id.selectDestionationBtn);
        switchAddressBtn = (Button) findViewById(R.id.switchBtn);
    }

    private void setAllView() {
        editBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int regionId = StaffData.regionList.idOfIndex(spnnrRegion.getSelectedItemPosition());
                Log.e("id", regionId + "");
                quantity = Integer.parseInt(quantityOfGoodsEd.getText().toString());
                if (quantity <= 0) {
                    Toast.makeText(getApplicationContext(), "Wrong quantity", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (destinationType.equals("SpecifyAddress")) {
                    if (destinationEd.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Destination can not be null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressDialog = ProgressDialog.show(EditOrder_Activity.this, "Loading...", "Sending Edit Staff Profile Request...", true);
                    new Thread(sendGetSpecifyAddressIdRequest).start();
                } else {
                    progressDialog = ProgressDialog.show(EditOrder_Activity.this, "Loading...", "Sending Edit Staff Profile Request...", true);
                    new Thread(SendOrderEditRequest).start();
                }
            }
        });
        selectDepartureBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDepartureAlert();
            }
        });
        selectDestinationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestinationAlert();
            }
        });
        switchAddressBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (layLinearShop.getVisibility() == View.VISIBLE) {
                    layLinearShop.setVisibility(View.GONE);
                    layLinearAddress.setVisibility(View.VISIBLE);
                    destinationType = "SpecifyAddress";
                    switchAddressBtn.setText("Select Shop");
                } else {
                    layLinearShop.setVisibility(View.VISIBLE);
                    layLinearAddress.setVisibility(View.GONE);
                    destinationType = "Shop";
                    switchAddressBtn.setText("Input Address");
                }
            }
        });
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                getApplicationContext(), StaffData.regionList, android.R.layout.simple_list_item_1,
                new String[]{"name"}, new int[]{android.R.id.text1}) {

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(Color.WHITE);
                ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
                return view;
            }
        };
        spnnrRegion.setAdapter(simpleAdapter);
        if (destinationType.equals("SpecifyAddress")) {
            spnnrRegion.setSelection(StaffData.regionList.indexOfId(destinationRegionId));
        }
        if (destinationType.equals("SpecifyAddress")) {
            layLinearShop.setVisibility(View.GONE);
            layLinearAddress.setVisibility(View.VISIBLE);
            switchAddressBtn.setText("Select Shop");
        } else {
            layLinearShop.setVisibility(View.VISIBLE);
            layLinearAddress.setVisibility(View.GONE);
            switchAddressBtn.setText("Input Address");
        }
        if (receiverType.equals("PublicReceiver")) {
            layLinearAddress.setVisibility(View.GONE);
            switchAddressBtn.setVisibility(View.GONE);
            try {
                JSONObject destinationObject = order.getJSONObject("destination");
                destinationEd.setText(destinationObject.getString("short_name"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject receiverObject = order.getJSONObject("receiver");
            JSONObject senderObject = order.getJSONObject("sender");
            txtViewSender.setText(senderObject.getString("name"));
            receiverNameTv.setText(receiverObject.getString("name"));
            receiverEmailTv.setText(receiverObject.getString("email"));
            receiverPhoneTv.setText(receiverObject.getString("phone"));
            departureTv.setText(departure);
            if (destinationType.equals("SpecifyAddress"))
                destinationEd.setText(destination);
            else
                destinationTv.setText(destination);
            quantityOfGoodsEd.setText("" + order.getInt("goods_number"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDestinationAlert() {
        int size = StaffData.shopList.size();
        String[] items = new String[size];
        for (int i = 0; i < size; i++) {
            try {
                items[i] = StaffData.shopList.get(i).getString("short_name");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(EditOrder_Activity.this);
        dialog.setTitle("Make your selection");
        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {
                try {
                    destinationTv.setText( StaffData.shopList.get(position).getString("short_name"));
                    destinationType =  StaffData.shopList.get(position).getString("type");
                    destinationId =  StaffData.shopList.get(position).getInt("id");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }

    public void showDepartureAlert() {
        int size = departureList.size();
        String[] items = new String[size];
        for (int i = 0; i < size; i++) {
            try {
                if (departureList.size() > i)
                    items[i] = departureList.get(i).getString("short_name");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(EditOrder_Activity.this);
        dialog.setTitle("Make your selection");
        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {
                try {
                    departureTv.setText(departureList.get(position).getString("short_name"));
                    departureType = departureList.get(position).getString("type");
                    departureId = departureList.get(position).getInt("id");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }

    Thread SendGetInformationRequest = new Thread() {
        @Override
        public void run() {
            try {
                if (StaffData.regionList.size() == 0) {
                    final JSONObject regionListResult = HTTPRequest.region_get_list();
                    if (regionListResult != null) {
                        if (regionListResult.getBoolean("success")) {
                            JSONArray regionList = regionListResult.getJSONObject("content").getJSONArray("list");
                            StaffData.regionList.clear();
                            for (int i = 0; i < regionList.length(); i++) {
                                JSONObject region = regionList.getJSONObject(i);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("id", region.getInt("id"));
                                map.put("name", region.getString("name"));
                                StaffData.regionList.add(map);
                            }
                        } else {
                            try {
                                String error = regionListResult.getString("error");
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Fail to server. \n Please try again later.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                if (StaffData.shopList.size() == 0) {
                    final JSONObject locationListResult = HTTPRequest.location_get_list();
                    if (locationListResult != null) {
                        if (locationListResult.getBoolean("success")) {
                            final JSONArray shopList = locationListResult.getJSONObject("content").getJSONArray("shops");
                            StaffData.shopList.clear();
                            for (int i = 0; i < shopList.length(); i++)
                                StaffData.shopList.add(shopList.getJSONObject(i));
                            departureList.clear();
                            JSONArray array = order.getJSONObject("receiver").getJSONArray("specify_addresses");
                            for (int i = 0; i < array.length(); i++) {
                                departureList.add(array.getJSONObject(i));
                            }
                            departureList.addAll(StaffData.shopList);
                        } else {
                            try {
                                String error = locationListResult.getString("error");
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Fail to server. \n Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAllView();
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };

    Thread sendGetSpecifyAddressIdRequest = new Thread() {
        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.location_get_specify_address_id(
                        destinationEd.getText().toString(), StaffData.regionList.idOfIndex(spnnrRegion.getSelectedItemPosition()));
                if (result != null) {
                    if (result.getBoolean("success")) {
                        destinationId = result.getJSONObject("content").getInt("id");
                        departureType = "SpecifyAddress";
                        new Thread(SendOrderEditRequest).start();
                    } else {
                        try {
                            String error = result.getString("error");
                            Toast.makeText(getApplicationContext(), error,
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Fail to server. \n Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Thread SendOrderEditRequest = new Thread() {
        @Override
        public void run() {
            final JSONObject result = HTTPRequest.order_edit(orderId, departureId, departureType, destinationId, destinationType, quantity);
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Edit Success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), result.getString("error"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };
}