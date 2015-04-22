package it114112fyp.staff_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import it114112fyp.util.StaffData;

public class MoreFunction_Activity extends Activity {
    private Button btnFindOrder, btnFindGoods, btnConveyorBelt, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_function);
        findAllView();
        setAllView();
    }

    @Override
    public void onBackPressed() {
        if (!StaffData.username.equals("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MoreFunction_Activity.this);
            alertDialog.setTitle("Do you want exit?");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } else
            super.onBackPressed();
    }

    private void findAllView() {
        btnFindGoods = (Button) findViewById(R.id.btnFindGoods);
        btnFindOrder = (Button) findViewById(R.id.btnFindOrder);
        btnConveyorBelt = (Button) findViewById(R.id.btnConveyorBelt);
        btnLogout = (Button) findViewById(R.id.btnLogout);
    }

    private void setAllView() {
        btnFindOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FindOrderDetails_Activity.class);
                i.putExtra("activity", "more");
                startActivity(i);
            }
        });
        btnFindGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FindGoodsDetails_Activity.class));
            }
        });
        btnConveyorBelt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ConveyorBeltList_Activity.class));
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(SendLogoutRequest).start();
            }
        });
    }

    Thread SendLogoutRequest = new Thread() {
        public void run() {
            final JSONObject result = HTTPRequest.logout();
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        startActivity(new Intent(getApplicationContext(), Login_Activity.class));
                        finish();
                    } else {
                        try {
                            String error = result.getString("error");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
