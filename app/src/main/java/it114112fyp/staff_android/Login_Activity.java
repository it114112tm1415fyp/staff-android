package it114112fyp.staff_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

import it114112fyp.util.StaffData;

public class Login_Activity extends Activity {

    private ProgressDialog progressDialog;

    private Button btnlogin;
    private EditText edTxtUsername, edTxtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        StaffData.resetData();
        edTxtUsername = (EditText) findViewById(R.id.UsernameEditText);
        edTxtPassword = (EditText) findViewById(R.id.PasswordEditText);

        edTxtUsername.setText("alisa");
        edTxtPassword.setText("alisa");

        btnlogin = (Button) findViewById(R.id.LoginButton);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String format = getResources().getString(R.string.format);
                if (edTxtUsername.getText().toString().equals("") || edTxtPassword.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Username and password can not be empty", Toast.LENGTH_SHORT).show();
                }
                if (!edTxtUsername.getText().toString().matches(format) || !edTxtPassword.getText().toString().matches(format)) {
                    Toast.makeText(getApplicationContext(), "Only accept 0-9, a-z, A-Z", Toast.LENGTH_SHORT).show();
                }
                StaffData.username = edTxtUsername.getText().toString();
                StaffData.password = edTxtPassword.getText().toString();
                progressDialog = ProgressDialog.show(Login_Activity.this, "Loading...", "Waiting for server response", true);
                new Thread(LoginRequest).start();
            }
        });
        (findViewById(R.id.btnA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTxtUsername.setText("alisa");
                edTxtPassword.setText("alisa");
            }
        });
        (findViewById(R.id.btnB)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTxtUsername.setText("jennifer");
                edTxtPassword.setText("jennifer");
            }
        });
        (findViewById(R.id.btnC)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTxtUsername.setText("mark0");
                edTxtPassword.setText("mark0");
            }
        });
        (findViewById(R.id.btnD)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTxtUsername.setText("lance");
                edTxtPassword.setText("lance");
            }
        });
        (findViewById(R.id.btnF)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTxtUsername.setText("kelly");
                edTxtPassword.setText("kelly");
            }
        });
        (findViewById(R.id.btnG)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTxtUsername.setText("terence");
                edTxtPassword.setText("terence");
            }
        });
        new Thread(SendGetInformationRequest).start();
    }

    Thread LoginRequest = new Thread() {

        @Override
        public void run() {
            final JSONObject _login = HTTPRequest.staff_login();
            try {
                if (_login != null) {
                    if (_login.getBoolean("success")) {
                        final JSONObject content = _login.getJSONObject("content");
                        StaffData.id = content.getInt("id");
                        StaffData.username = content.getString("username");
                        StaffData.name = content.getString("name");
                        StaffData.email = content.getString("email");
                        StaffData.phone = content.getString("phone");
                        StaffData.workplace = content.getJSONObject("workplace").getString("type") + " - " + content.getJSONObject("workplace").getString("short_name");
                        StaffData.register_time = DateFormator.getDateTime(content.getString("created_at"));
                        StaffData.last_modify_time = DateFormator.getDateTime(content.getString("updated_at"));
                        StaffData.addresses = content.getJSONArray("specify_addresses");
                        StaffData.conveyorId = content.getJSONObject("workplace").optInt("conveyor_id");
                        StaffData.staffObject = content;
                        runOnUiThread(new Thread() {
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), Home_Activity.class));
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), _login.getString("error"), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT).show();
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
    };


    Thread SendGetInformationRequest = new Thread() {
        @Override
        public void run() {
            try {
                final JSONObject regionResult = HTTPRequest.region_get_list();
                if (regionResult != null) {
                    if (regionResult.getBoolean("success")) {
                        JSONArray list = regionResult.getJSONObject("content").getJSONArray("list");
                        StaffData.regionList.clear();
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject region = list.getJSONObject(i);
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", region.getInt("id"));
                            map.put("name", region.getString("name"));
                            StaffData.regionList.add(map);
                        }
                    }
                }
                final JSONObject LocationResult = HTTPRequest.location_get_list();
                if (LocationResult != null) {
                    if (LocationResult.getBoolean("success")) {
                        final JSONArray shopList = LocationResult.getJSONObject("content").getJSONArray("shops");
                        StaffData.shopList.clear();
                        for (int i = 0; i < shopList.length(); i++)
                            StaffData.shopList.add(shopList.getJSONObject(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}