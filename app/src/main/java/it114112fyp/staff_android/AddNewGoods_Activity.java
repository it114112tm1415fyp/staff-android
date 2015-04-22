package it114112fyp.staff_android;

import java.io.ByteArrayOutputStream;

import net.sourceforge.zbar.Symbol;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import it114112fyp.util.StaffData;

public class AddNewGoods_Activity extends Activity {

    private static final int PICK_CONTACT_REQUEST = 2;
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private ProgressDialog progressDialog;

    private ImageView goodPreviewImageView;
    private Button openCameraButton, goodIdQrButton, finishAddButton;
    private EditText weightEditText, goodIdEditText;
    private Switch fragileSwitch, flammablesSwitch;

    private Bitmap goodsImage;
    private JSONObject qrCode;
    private String base64, good_id, action, imageBase64;
    private int orderId, taskId;
    private float weight;
    private boolean flammable, fragile;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_goods);
        Intent self = getIntent();
        orderId = self.getIntExtra("orderId", 0);
        action = self.getStringExtra("action");
        taskId = self.getIntExtra("taskId", 0);
        findAllView();
        setAllView();
        if (action.equals("edit good")) {
            good_id = self.getStringExtra("goodsId");
            progressDialog = ProgressDialog.show(AddNewGoods_Activity.this, "Loading...", "Getting Goods Details ...", true);
            new Thread(SendGetGoodDetailsRequest).start();
        }
    }

    private void findAllView() {
        goodPreviewImageView = (ImageView) findViewById(R.id.goodImageView);
        openCameraButton = (Button) findViewById(R.id.openCameraButton);
        finishAddButton = (Button) findViewById(R.id.finishAddGoodButton);
        goodIdQrButton = (Button) findViewById(R.id.goodIdQrButton);
        weightEditText = (EditText) findViewById(R.id.WeightEditText);
        goodIdEditText = (EditText) findViewById(R.id.goodIdEditText);
        fragileSwitch = (Switch) findViewById(R.id.FragileSwitch);
        flammablesSwitch = (Switch) findViewById(R.id.FlammableSwitch);
    }

    private void setAllView() {
        finishAddButton.setText(action.equals("add good") ? "Add Goods" : "Edit Goods");
        setSwitchTextOnAndOff(fragileSwitch);
        setSwitchTextOnAndOff(flammablesSwitch);
        openCameraButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent_camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent_camera, PICK_CONTACT_REQUEST);
            }
        });
        goodIdQrButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                launchQRScanner(v);
            }
        });
        finishAddButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        good_id = goodIdEditText.getText().toString();
                        Log.e("good_id", good_id);
                        if (good_id.equals("")) {
                            Toast.makeText(getApplicationContext(), "Goods Id cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        weight = Float.parseFloat(weightEditText.getText().toString());
                        if (weight <= 0.0) {
                            Toast.makeText(getApplicationContext(), "Weight cannot lower than 0.0 kg", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        flammable = flammablesSwitch.isChecked();
                        fragile = flammablesSwitch.isChecked();
                        if (imageBase64 == null) {
                            Toast.makeText(getApplicationContext(), "Need Photo", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (action.equals("add good")) {
                            progressDialog = ProgressDialog.show(AddNewGoods_Activity.this, "Loading...", "Sending Add Goods Request...", true);
                            new Thread(SendAddGoodRequest).start();
                        } else {
                            progressDialog = ProgressDialog.show(AddNewGoods_Activity.this, "Loading...", "Sending Edit Goods Request...", true);
                            new Thread(SendEditGoodRequest).start();
                        }
                    }
                }
        );
    }

    private void updateView() {
        flammablesSwitch.setSelected(flammable);
        fragileSwitch.setSelected(fragile);
        goodIdEditText.setText(good_id);
        weightEditText.setText(weight + "");
        goodPreviewImageView.setImageBitmap(goodsImage);
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void launchQRScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(getApplicationContext(), ZBarScannerActivity.class);
            intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(getApplicationContext(), "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public Switch setSwitchTextOnAndOff(Switch s) {
        s.setTextOn("Yes");
        s.setTextOff("No");
        return s;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case ZBAR_SCANNER_REQUEST:
                case ZBAR_QR_SCANNER_REQUEST:
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i("QR Code", data.getStringExtra(ZBarConstants.SCAN_RESULT));
                        qrCode = new JSONObject(data.getStringExtra(ZBarConstants.SCAN_RESULT).substring(
                                ("it114112tm1415fyp.temporary_goods_tag").length()));
                        goodIdEditText.setText(qrCode.getString("goods_id"));
                        Toast.makeText(getApplicationContext(), "QRCode : " + qrCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PICK_CONTACT_REQUEST:
                    if (resultCode == Activity.RESULT_OK) {
                        Bundle extras = data.getExtras();
                        Bitmap bmp = (Bitmap) extras.get("data");
                        int width = bmp.getWidth();
                        int height = bmp.getHeight();
                        int min_edge = Math.min(width, height);
                        bmp = Bitmap.createBitmap(bmp, (width - min_edge) / 2, (height - min_edge) / 2, min_edge, min_edge);
                        goodPreviewImageView.setImageBitmap(bmp);

                        int newWidth = 500;
                        int newHeight = 500;
                        float scaleWidth = ((float) newWidth) / min_edge;
                        float scaleHeight = ((float) newHeight) / min_edge;

                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleHeight);
                        Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, min_edge, min_edge, matrix, true);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        newbm.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte bytes[] = stream.toByteArray();
                        base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                        imageBase64 = base64;
                        Log.d("base64", base64);
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Thread SendAddGoodRequest = new Thread() {
        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.goods_add(
                        taskId, orderId, good_id, weight, fragile, flammable, imageBase64);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Add New Goods Success", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Thread SendEditGoodRequest = new Thread() {

        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.goods_edit(
                        orderId, good_id, weight, fragile, flammable, imageBase64);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Edit Goods Success", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
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
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };

    Thread SendGetGoodDetailsRequest = new Thread() {

        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.goods_get_details(good_id);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        JSONObject picture = HTTPRequest.picture_good(good_id);
                        imageBase64 = picture.getJSONObject("content").getString("goods_photo");
                        byte bytes[] = Base64.decode(imageBase64, Base64.DEFAULT);
                        goodsImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        JSONObject goodsObject = result.getJSONObject("content");
                        weight = (float) goodsObject.getDouble("weight");
                        fragile = goodsObject.getBoolean("fragile");
                        flammable = goodsObject.getBoolean("flammable");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateView();
                            }
                        });
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
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }
    };

    class SendRemoveGoodRequest extends Thread {
        String goodsId;

        public SendRemoveGoodRequest(String good_id) {
            this.goodsId = goodsId;
        }

        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.good_remove(goodsId, orderId);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Add Goods Success", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
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
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
