package it114112fyp.staff_android;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltList_Activity extends Activity {

    public static JSONObject message = new JSONObject();

    private ArrayAdapter<String> lstBeltAdapter;

    private ProgressDialog progressDialog;

    private ListView lstBelt;

    private ArrayList<JSONObject> beltList = new ArrayList<>();
    private List<String> beltName = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conveyor_belt_list);
        lstBelt = (ListView) findViewById(R.id.lstBelt);
        lstBeltAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, beltName) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
                return view;
            }
        };
        lstBelt.setAdapter(lstBeltAdapter);
        progressDialog = ProgressDialog.show(ConveyorBeltList_Activity.this, "Loading...", "Getting Conveyor Belt List...", true);
        new Thread(SendGetBeltListRequest).start();
        lstBelt.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    int beltId = beltList.get(position).getInt("id");
                    new SendGetControlRequest(beltId).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class SendGetControlRequest extends Thread {
        int beltId;

        public SendGetControlRequest(int beltId) {
            this.beltId = beltId;
        }

        public void run() {
            JSONObject result = HTTPRequest.get_control(beltId);
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        message = result.getJSONObject("content");
                        Intent i = new Intent(getApplicationContext(), BeltController_Activity.class);
                        i.putExtra("id", beltId);
                        startActivity(i);
                    } else {
                        final String error = result.getString("error");
                        runOnUiThread(new Thread() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT)
                                        .show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Thread SendGetBeltListRequest = new Thread() {

        @Override
        public void run() {
            final JSONObject result = HTTPRequest.conveyor_getList();
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        JSONArray list = result.getJSONObject("content").getJSONArray("conveyors");
                        beltList.clear();
                        beltName.clear();
                        for (int i = 0; i < list.length(); i++) {
                            beltName.add(list.getJSONObject(i).getString("name"));
                            beltList.add(list.getJSONObject(i));
                        }
                    } else {
                        final String error = result.getString("error");
                        runOnUiThread(new Thread() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT)
                                        .show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("error", "");
            } finally {
                runOnUiThread(new Thread() {
                    public void run() {
                        lstBeltAdapter.notifyDataSetChanged();
                    }
                });
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };
}
