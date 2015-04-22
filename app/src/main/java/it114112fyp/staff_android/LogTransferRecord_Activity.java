package it114112fyp.staff_android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LogTransferRecord_Activity extends Activity implements SurfaceHolder.Callback {

    private SimpleAdapter goodsInTaskAdapter;

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private SurfaceView surface_view;
    private ImageScanner scanner;
    private Handler autoFocusHandler;
    private AsyncDecode asyncDecode;

    private FinderView finder_view;
    private ListView lstGoods;
    //    private RelativeLayout layRelativeShelfId;
    private TextView txtViewGoodId, txtViewOrderId, txtViewRFID, txtViewWeight, txtViewFragile, txtViewDestination, txtViewDeparture, txtViewFlammable, txtViewOrderTime;
//    private EditText edTxtShelfId;

    private JSONArray goodsInTask = null;
    private List<HashMap<String, Object>> goodsItemList = new ArrayList<>();
    private List<HashMap<String, Object>> goodsInTaskItemList = new ArrayList<>();
    private String taskAction, activity, goodsQRCode;
    private int taskId, orderId, shelfId = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_transfer_record);
        Intent self = getIntent();
        taskId = self.getIntExtra("taskId", 0);
        activity = self.getStringExtra("activity");
        taskAction = self.getStringExtra("action");
        if (activity.equals("Issue")) {
            goodsInTask = IssueOrder_Activity.goodsInTask;
            orderId = self.getIntExtra("orderId", 0);
        } else if (activity.equals("Receive"))
            goodsInTask = ReceiveOrder_Activity.goodsInTask;
        else if (activity.equals("Task"))
            goodsInTask = TaskDetails_Activity.goodsInTask;
        Log.e("goodsInTask", goodsInTask.toString());
        for (int i = 0; i < goodsInTask.length(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            try {
                map.put("goods_id", goodsInTask.getJSONObject(i).getString("goods_id"));
                map.put("completed", goodsInTask.getJSONObject(i).getBoolean("completed"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            goodsInTaskItemList.add(map);
        }
        checkFinishTask();
        findAllView();
        setAllView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void findAllView() {
        surface_view = (SurfaceView) findViewById(R.id.surface_view);
        finder_view = (FinderView) findViewById(R.id.finder_view);
        lstGoods = (ListView) findViewById(R.id.lstGoods);
//        layRelativeShelfId = (RelativeLayout) findViewById(R.id.layLinearShelfId);
        txtViewGoodId = (TextView) findViewById(R.id.goodIdTextView);
        txtViewOrderId = (TextView) findViewById(R.id.orderIdTextView);
        txtViewRFID = (TextView) findViewById(R.id.RFIDtagTextView);
        txtViewWeight = (TextView) findViewById(R.id.weightTextView);
        txtViewFragile = (TextView) findViewById(R.id.fragileTextView);
        txtViewDestination = (TextView) findViewById(R.id.destinationTextView);
        txtViewDeparture = (TextView) findViewById(R.id.departureTextView);
        txtViewFlammable = (TextView) findViewById(R.id.flammableTextView);
        txtViewOrderTime = (TextView) findViewById(R.id.orderTimeTextView);
//        edTxtShelfId = (EditText) findViewById(R.id.edTxtShelfId);
    }

    private void setAllView() {
        mHolder = surface_view.getHolder();
        mHolder.addCallback(LogTransferRecord_Activity.this);
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        autoFocusHandler = new Handler();
        asyncDecode = new AsyncDecode();
//        if (!taskAction.equals("warehouse")) {
//            shelfId = 0;
////            layRelativeShelfId.setVisibility(View.GONE);
//        }
        goodsInTaskAdapter = new SimpleAdapter(
                getApplicationContext(), goodsInTaskItemList, R.layout.goods_in_task_list_item,
                new String[]{"goods_id", "completed"},
                new int[]{R.id.txtViewGoodsId, R.id.txtViewGoodsState}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, Object> taskMap = goodsInTaskItemList.get(position);
                boolean completed = (boolean) taskMap.get("completed");
                if (!completed)
                    view.setBackgroundColor(Color.rgb(220, 220, 220));
                else
                    view.setBackgroundColor(Color.rgb(160, 255, 160));
                return view;
            }
        };
        lstGoods.setAdapter(goodsInTaskAdapter);
    }

    private void setTextView(int index) {
        HashMap<String, Object> goodsItem = goodsItemList.get(index);
        txtViewGoodId.setText(goodsItem.get("goodsId") + "");
        txtViewOrderId.setText(goodsItem.get("orderId") + "");
        txtViewRFID.setText(goodsItem.get("rfidTag") + "");
        txtViewWeight.setText(goodsItem.get("weight") + "");
        txtViewFlammable.setText(goodsItem.get("flammable") + "");
        txtViewFragile.setText(goodsItem.get("fragile") + "");
        txtViewDeparture.setText(goodsItem.get("departure") + "");
        txtViewDestination.setText(goodsItem.get("destination") + "");
        txtViewOrderTime.setText(goodsItem.get("orderTime") + "");
    }


    private void checkFinishTask() {
        if (activity.equals("Issue")) {
            boolean issueFinish = false;
            for (HashMap<String, Object> map : goodsInTaskItemList) {
                if ((boolean) map.get("completed")) {
                    issueFinish = true;
                } else {
                    issueFinish = false;
                    break;
                }
            }
            if (issueFinish) {
                setResult(RESULT_OK);
                Intent intent = new Intent(getApplicationContext(), ConfirmOrder_Activity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("taskId", taskId);
                intent.putExtra("action", "Issue");
                intent.putExtra("activity", "Issue");
                startActivityForResult(intent, 1);
                setResult(RESULT_OK);
            }
        }
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (asyncDecode.isStoped()) {
                camera.setDisplayOrientation(0);
                Camera.Parameters parameters = camera.getParameters();
//                parameters.set("orientation", "landscape");
                Camera.Size size = parameters.getPreviewSize();
                //图片是被旋转了90度的
                Image source = new Image(size.width, size.height, "Y800");
                Rect scanImageRect = finder_view.getScanImageRect(size.height, size.width);
                //图片旋转了90度，将扫描框的TOP作为left裁剪
                source.setCrop(scanImageRect.top, scanImageRect.left, scanImageRect.bottom, scanImageRect.right);
                source.setData(data);
                asyncDecode = new AsyncDecode();
                asyncDecode.execute(source);
            }
        }
    };

    private class AsyncDecode extends AsyncTask<Image, Void, Void> {
        private boolean stoped = true;
        private String qrcode = "";

        @Override
        protected Void doInBackground(Image... params) {
            stoped = false;
            Image barcode = params[0];
            int result = scanner.scanImage(barcode);
            if (result != 0) {
                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    if (sym.getType() == Symbol.QRCODE) {
                        qrcode = sym.getData();
                        Log.e("qrcode", qrcode);
                        if (qrcode.contains("it114112tm1415fyp"))
                            try {
                                JSONObject qrCodeObject;
                                if (qrcode.contains("it114112tm1415fyp.temporary_goods_tag")) {
                                    String qrCodeString = qrcode.substring("it114112tm1415fyp.temporary_goods_tag".length());
                                    qrCodeObject = new JSONObject(qrCodeString);
                                    final String goodsId = qrCodeObject.getString("goods_id");
                                    Thread t = new SendGetGoodsQrCodeRequest(goodsId);
                                    t.start();
                                    t.join();
                                    if (goodsQRCode == null)
                                        return null;
                                    qrcode = goodsQRCode;
                                }
                                if (qrcode.contains("it114112tm1415fyp.goods")) {
                                    String qrCodeString = qrcode.substring("it114112tm1415fyp.goods".length());
                                    qrCodeObject = new JSONObject(qrCodeString);
                                    final String goodsId = qrCodeObject.getString("goods_id");
                                    for (int i = 0; i < goodsItemList.size(); i++) {
                                        final int index = i;
                                        if (goodsItemList.get(i).get("goodsId").toString().equals(goodsId)) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setTextView(index);
                                                }
                                            });
                                            return null;
                                        }
                                    }
                                    new SendTransferRequest(taskId, shelfId, goodsId).start();
                                    String orderTime = null;
                                    try {
                                        orderTime = DateFormator.getDateTime(qrCodeObject.getString("order_time"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    int orderId = qrCodeObject.getInt("order_id");
                                    String rfidTag = qrCodeObject.getString("rfid_tag");
                                    double weight = qrCodeObject.getDouble("weight");
                                    boolean flammable = qrCodeObject.getBoolean("flammable");
                                    boolean fragile = qrCodeObject.getBoolean("fragile");
                                    String departure = qrCodeObject.getString("departure");
                                    String destination = qrCodeObject.getString("destination");
                                    HashMap<String, Object> goodsMap = new HashMap<>();
                                    goodsMap.put("goodsId", goodsId);
                                    goodsMap.put("orderId", orderId);
                                    goodsMap.put("rfidTag", rfidTag);
                                    goodsMap.put("weight", weight);
                                    goodsMap.put("flammable", flammable);
                                    goodsMap.put("fragile", fragile);
                                    goodsMap.put("departure", departure);
                                    goodsMap.put("destination", destination);
                                    goodsMap.put("orderTime", orderTime);
                                    goodsItemList.add(goodsMap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            stoped = true;
        }

        public boolean isStoped() {
            return stoped;
        }
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (null == mCamera || null == autoFocusCallback) {
                return;
            }
            mCamera.autoFocus(autoFocusCallback);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    class SendTransferRequest extends Thread {
        int taskId, shelfId;
        String goodsId;

        public SendTransferRequest(int taskId, int shelfId, String goodsId) {
            this.taskId = taskId;
            this.shelfId = shelfId;
            this.goodsId = goodsId;
        }

        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.do_task(taskId, shelfId, goodsId);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        for (HashMap<String, Object> map : goodsInTaskItemList) {
                            if (map.get("goods_id").toString().equals(goodsId))
                                map.put("completed", true);
                        }
                        checkFinishTask();
                        setResult(RESULT_OK);
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
                            Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTextView(goodsItemList.size() - 1);
                        goodsInTaskAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    class SendGetGoodsQrCodeRequest extends Thread {
        String goodsId;

        public SendGetGoodsQrCodeRequest(String goodsId) {
            this.goodsId = goodsId;
            goodsQRCode = null;
        }

        @Override
        public void run() {
            try {
                final JSONObject result = HTTPRequest.get_goods_qrcode(goodsId);
                if (result != null) {
                    if (result.getBoolean("success")) {
                        goodsQRCode = result.getString("content");
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
                            Toast.makeText(getApplicationContext(), "Fail to connect server.\nPlease try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
