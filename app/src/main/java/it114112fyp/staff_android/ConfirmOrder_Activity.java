package it114112fyp.staff_android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.signatureview.SignatureView;

public class ConfirmOrder_Activity extends Activity {

    private ProgressDialog progressDialog;

    private SignatureView signature;
    private TableRow tableRowPrice;
    private Button btnReset, btnConfirm;
    private TextView txtViewOrderId, txtViewSender, txtViewReceiver, txtViewDeparture, txtViewDestination, txtViewNumberOfGoods, txtViewPrice;

    private JSONObject orderObject;
    private String signBase64, action;
    private int orderId, taskId;
    private String price;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_order);
        findAllView();
        Intent self = getIntent();
        action = self.getStringExtra("action");
        orderId = self.getIntExtra("orderId", 0);
        taskId = self.getIntExtra("taskId", 0);
        if (action.equals("Issue")) {
            orderObject = IssueOrder_Activity.orderObject;
        } else if (action.equals("Receive")) {
            orderObject = ReceiveOrder_Activity.orderObject;
            progressDialog = ProgressDialog.show(ConfirmOrder_Activity.this, "Loading...", "Getting Order Price...", true);
            new Thread(SendGetPriceRequest).start();
        }
        setAllView();
    }

    private void findAllView() {
        tableRowPrice = (TableRow) findViewById(R.id.tableRowPrice);
        signature = (SignatureView) findViewById(R.id.signatureView);
        txtViewOrderId = (TextView) findViewById(R.id.txtViewOrderId);
        txtViewSender = (TextView) findViewById(R.id.txtViewSender);
        txtViewReceiver = (TextView) findViewById(R.id.txtViewReceiver);
        txtViewDeparture = (TextView) findViewById(R.id.txtViewDeparture);
        txtViewDestination = (TextView) findViewById(R.id.txtViewDestination);
        txtViewNumberOfGoods = (TextView) findViewById(R.id.txtViewNumberOfGoods);
        txtViewPrice = (TextView) findViewById(R.id.txtViewPrice);
        btnReset = (Button) findViewById(R.id.resetSignButton);
        btnConfirm = (Button) findViewById(R.id.signConfirmButton);
    }

    private void setAllView() {
        if (action.equals("Issue")) {
            btnConfirm.setText("Issue Order");
            tableRowPrice.setVisibility(View.INVISIBLE);
        } else if (action.equals("Receive")) {
            btnConfirm.setText("Receive Order");
            tableRowPrice.setVisibility(View.VISIBLE);
        }
        btnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("orderId", orderId + "");
                Log.e("taskId", taskId + "");
                signBase64 = pictureToByte(signature.getImage());

                Log.e("size", signBase64.length() + "");
                AlertDialog.Builder dialog = new AlertDialog.Builder(ConfirmOrder_Activity.this);
                if (action.equals("Issue")) {
                    dialog.setTitle("Issue Order");
                } else if (action.equals("Receive")) {
                    dialog.setTitle("Receive Order");
                    dialog.setMessage("Total Price : $" + price);
                }
                dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog = ProgressDialog.show(ConfirmOrder_Activity.this, "Loading...", "Sending" + action + "order request ...", true);
                        new Thread(SendConfirmOrderRequest).start();
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
        btnReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signature.clearSignature();
            }
        });
        setTextView();
    }

    private void setTextView() {
        try {
            orderId = orderObject.getInt("id");
            txtViewOrderId.setText(orderId + "");
            txtViewSender.setText(orderObject.getJSONObject("sender").getString("name"));
            txtViewReceiver.setText(orderObject.getJSONObject("receiver").getString("name"));
            txtViewDeparture.setText(orderObject.getJSONObject("departure").getString("short_name"));
            txtViewDestination.setText(orderObject.getJSONObject("destination").getString("short_name"));
            txtViewNumberOfGoods.setText(orderObject.getInt("goods_number") + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String pictureToByte(Bitmap signPicture) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        signPicture.compress(Bitmap.CompressFormat.JPEG, 30, stream);
        byte bytes[] = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    Thread SendConfirmOrderRequest = new Thread() {
        @Override
        public void run() {
            final JSONObject result = HTTPRequest.order_confirm(taskId, orderId, signBase64);
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
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
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }
    };

    Thread SendGetPriceRequest = new Thread() {
        @Override
        public void run() {
            final JSONObject result = HTTPRequest.order_get_price(orderId);
            try {
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    price = String.valueOf(result.getDouble("content"));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtViewPrice.setText(price + "");
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
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
                            try {
                                Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT).show();
                                finish();
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
}
