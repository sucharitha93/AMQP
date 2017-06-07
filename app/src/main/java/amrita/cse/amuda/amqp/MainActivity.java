package amrita.cse.amuda.amqp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import amrita.cse.amuda.amqp.Trilateration;

public class MainActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    WiFiScanReceiver wifiReceiver;
    int[] rssiList = new int[20];
    int[] cnt = new int[20];
    File fileWifi;
    String[] macList = {
            "EC:1A:59:89:B0:F7",  "ec:1a:59:89:b0:f9",
            "EC:1A:59:4A:DE:51","EC:1A:59:4A:DE:53",
            "18:D6:C7:79:13:49","18:d6:c7:79:13:48",
            "18:D6:C7:79:1E:D4","18:D6:C7:79:1E:D3",
            "18:D6:c7:79:14:8d","18:d6:c7:79:14:8c",
            "18:D6:c7:79:1D:E1","18:D6:c7:79:1D:E0",
            "EC:1A:59:8A:06:80",  "EC:1A:59:8A:06:82",
            "EC:1A:59:4A:DB:E4", "EC:1A:59:4A:DB:E6"
    };
    TextView textView;
    Button btnScan;
    EditText xCoordinate, yCoordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        btnScan = (Button) findViewById(R.id.publish);
        xCoordinate = (EditText) findViewById(R.id.xCoordinate);
        yCoordinate = (EditText) findViewById(R.id.yCoordinate);

        setupConnectionFactory();
        publishToAMQP();


        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                TextView tv = (TextView) findViewById(R.id.textView);
                Date now = new Date();
                SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss");
                tv.append(ft.format(now) + ' ' + message + '\n');
            }
        };
        subscribe(incomingMessageHandler);

        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsEnabled)
        {
            Toast.makeText(this,"Please TURN ON location service to get data!",Toast.LENGTH_SHORT).show();
        }

        //wifi service enabling
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WiFiScanReceiver();

        //file path
        fileWifi = new File(Environment.getExternalStorageDirectory()+"//WiFiLog.csv");
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        //check write to storage permission
        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //file create if does not exist
        try{
            if (!fileWifi.exists()) {
                fileWifi.createNewFile();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnClick(View v)
    {
        btnScan.setBackgroundColor(Color.LTGRAY);
        String x,y;
        x= xCoordinate.getText().toString();
        y = yCoordinate.getText().toString();

        //check if the co-ordinate points are entered
        if (x.isEmpty() && y.isEmpty())
        {
            Toast.makeText(this,"Enter the co-ordinates to begin SCAN",Toast.LENGTH_SHORT).show();
        }
        else
        {
            scan(v);
        }
    }
    public void scan(View v){

        textView.setText("");
        IntentFilter filterScanResult = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        IntentFilter filterRSSIChange = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
        IntentFilter filterChange = new IntentFilter(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
        if(!wifiManager.isWifiEnabled())
        {
            Toast.makeText(this,"Wifi Turned On",Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
        Toast.makeText(this,"Wifi Scan started",Toast.LENGTH_SHORT).show();
        this.registerReceiver(wifiReceiver, filterScanResult);
        this.registerReceiver(wifiReceiver, filterRSSIChange);
        this.registerReceiver(wifiReceiver, filterChange);
        wifiManager.startScan();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    unregisterReceiver(wifiReceiver);
                    btnScan.setBackgroundColor(Color.GREEN);
                    String msg="";
                    FileWriter fwWifi = new FileWriter(fileWifi,true);
                    BufferedWriter bwWifi = new BufferedWriter(fwWifi);
                    ;
                    for(int i = 0;i<16;i++){
                        if(cnt[i] != 0)
                        {
                            bwWifi.append(String.valueOf(rssiList[i] / cnt[i])+",");
                            msg = msg + String.valueOf(rssiList[i] / cnt[i])+",";
                        }
                        else
                        {
                            bwWifi.append(String.valueOf(0)+",");
                            msg = msg + String.valueOf(0)+";";
                        }
                    }
//                    try {
//                        queue.putLast(msg);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    WifiInfo info = wifiManager.getConnectionInfo();
                    String macaddress = info.getMacAddress();
                    bwWifi.append("\n");
                    bwWifi.close();
                    //=POWER(10,(B2+38.6667)/-(10*1.4))
                    /*Location l = Trilateration.trilaterate(Float.parseFloat(xCoordinate.getText().toString()),Float.parseFloat(yCoordinate.getText().toString()),
                            (float)Math.pow(10,(rssiList[0]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[2]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[4]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[6]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[8]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[10]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[12]+38.667)/-16),
                            (float)Math.pow(10,(rssiList[14]+38.667)/-16)); */
                    //String body = macaddress+","+l.getActualX()+","+l.getActualY()+","+l.getExperimentalX()+","+l.getExperimentalY()+","+msg;
                    //2011-05-16 15:36:38
                    String body =macaddress+","+xCoordinate.getText().toString()+","+yCoordinate.getText().toString()+","+6567+","+4567+","+msg;

                    try {
                            queue.putLast(body);
                       } catch (InterruptedException e) {
                            e.printStackTrace();
                       }

                    Arrays.fill(rssiList,new Integer(0));
                    Arrays.fill(cnt,new Integer(0));
                    Toast.makeText(MainActivity.this,"20 seconds scan results done",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, 20000);
    }

    class WiFiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) || WifiManager.RSSI_CHANGED_ACTION.equals(action)|| WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE.equals(action))
            {
                List<ScanResult> wifiScanResultList = wifiManager.getScanResults();
                System.out.print(wifiScanResultList.toString());

                for(int i = 0; i < wifiScanResultList.size(); i++){
                    ScanResult accessPoint = wifiScanResultList.get(i);
                    String listItem = "SSID: "+accessPoint.SSID + "\n" + "MAC Address: "+accessPoint.BSSID + "\n" + "RSSI Signal Level"+accessPoint.level;
                    if(accessPoint.frequency < 5000)
                    {
                        if(macList[0].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[0] = rssiList[0]+ accessPoint.level;
                            cnt[0] = cnt[0] + 1;
                        }
                        else if(macList[2].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[2] = rssiList[2]+ accessPoint.level;
                            cnt[2] = cnt[2] + 1;
                        }
                        else if(macList[4].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[4] = rssiList[4]+ accessPoint.level;
                            cnt[4] = cnt[4] + 1;
                        }
                        else if(macList[6].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[6] = rssiList[6]+ accessPoint.level;
                            cnt[6] = cnt[6] + 1;
                        }
                        else if(macList[8].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[8] = rssiList[8]+ accessPoint.level;
                            cnt[8] = cnt[8] + 1;
                        }
                        else if(macList[10].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[10] = rssiList[10]+ accessPoint.level;
                            cnt[10] = cnt[10] + 1;
                        }
                        else if(macList[12].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[12] = rssiList[12]+ accessPoint.level;
                            cnt[12] = cnt[12] + 1;
                        }
                        else if(macList[14].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[14] = rssiList[14]+ accessPoint.level;
                            cnt[14] = cnt[14] + 1;
                        }
                            /*else if(macList[16].equalsIgnoreCase(accessPoint.BSSID))
                            {
                                rssiList[16] = rssiList[16]+ accessPoint.level;
                                cnt[16] = cnt[16] + 1;
                            }
                            else if(macList[18].equalsIgnoreCase(accessPoint.BSSID))
                            {
                                rssiList[18] = rssiList[18]+ accessPoint.level;
                                cnt[18] = cnt[18] + 1;
                            }
                            */
                        else
                        {

                        }
                    }
                    else if (accessPoint.frequency > 5000)
                    {
                        if(macList[1].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[1] = rssiList[1]+ accessPoint.level;
                            cnt[1] = cnt[1] + 1;
                        }
                        else if(macList[3].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[3] = rssiList[3]+ accessPoint.level;
                            cnt[3] = cnt[3] + 1;
                        }
                        else if(macList[5].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[5] = rssiList[5]+ accessPoint.level;
                            cnt[5] = cnt[5] + 1;
                        }
                        else if(macList[7].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[7] = rssiList[7]+ accessPoint.level;
                            cnt[7] = cnt[7] + 1;
                        }
                        else if(macList[9].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[9] = rssiList[9]+ accessPoint.level;
                            cnt[9] = cnt[9] + 1;
                        }
                        else if(macList[11].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[11] = rssiList[11]+ accessPoint.level;
                            cnt[11] = cnt[11] + 1;
                        }
                        else if(macList[13].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[13] = rssiList[13]+ accessPoint.level;
                            cnt[13] = cnt[13] + 1;
                        }
                        else if(macList[15].equalsIgnoreCase(accessPoint.BSSID))
                        {
                            rssiList[15] = rssiList[15]+ accessPoint.level;
                            cnt[15] = cnt[15] + 1;
                        }
                            /*else if(macList[17].equalsIgnoreCase(accessPoint.BSSID))
                            {
                                rssiList[17] = rssiList[17]+ accessPoint.level;
                                cnt[17] = cnt[17] + 1;
                            }
                            else if(macList[19].equalsIgnoreCase(accessPoint.BSSID))
                            {
                                rssiList[19] = rssiList[19]+ accessPoint.level;
                                cnt[19] = cnt[19] + 1;
                            }
                            */
                        else
                        {

                        }
                    }

                   // textView.append(listItem + "\n\n");
                }
                //textView.append("***********************************\n");
            }
        }
    }

    private BlockingDeque queue = new LinkedBlockingDeque();
//    void publishMessage(String message) {
//
//        try {
//            queue.putLast(message);
//            Toast.makeText(getApplicationContext(),queue.toString(),Toast.LENGTH_LONG).show();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
    ConnectionFactory factory = new ConnectionFactory();
    private void setupConnectionFactory() {
        String uri = "amqp://amuda:amuda2017@172.17.9.61:5672/%2f";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUsername("amuda");
            factory.setPassword("amuda2017");
            factory.setHost("172.17.9.61");
            factory.setPort(5672);
            factory.setVirtualHost("amudavhost");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    public void publishToAMQP()
    {
        Log.i("","Reached publish to AMQP");
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        //ch.queueDeclare("PQ", false, false, false, null);
                        ch.confirmSelect();
                        Log.i("","Reached publish to AMQP");

                        while(true) {

                            String message = queue.takeFirst().toString();
                            Log.i("",message+" is the message to be published");
                            try{
                                //ch.basicPublish("", "PQ", null, message.getBytes());
                                ch.basicPublish("amq.fanout","severity" , null, message.getBytes());
                                ch.waitForConfirmsOrDie();
                            } catch (Exception e){
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(1000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }
    void subscribe(final Handler handler)
    {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.basicQos(1);
                        AMQP.Queue.DeclareOk q = channel.queueDeclare();
                        channel.queueBind(q.getQueue(), "amq.fanout", "severity");
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);

                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String message = new String(delivery.getBody());
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        Log.d("", "Connection broken: " + e1.getClass().getName());
                        try {

                            Thread.sleep(1000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }
    Thread subscribeThread;
    Thread publishThread;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        publishThread.interrupt();
        //subscribeThread.interrupt();
    }
}
