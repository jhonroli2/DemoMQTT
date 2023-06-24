package com.example.demomqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private String clientID = "";
    private String host = "tcp://jhonroli2.cloud.shiftr.io:1883";
    private String user = "jhonroli2";
    private String pass = "BRp6M6oGXzHrYlsd";

    public static String topic = "LED";
    private String ON = "ON";
    private String OFF = "OFF";

    private MqttAndroidClient client;
    private MqttConnectOptions options;

    private TextView labClientID;
    private Button butON;
    private Button butOFF;
    private TextView labInfo;
    private boolean permission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();

        getClientName();

        connectBroker();
    }

    public void initViews(){
        labClientID = findViewById(R.id.labClientID);
        butON = findViewById(R.id.butON);
        butOFF = findViewById(R.id.butOFF);
        labInfo = findViewById(R.id.labInfo);
    }

    public void initEvents(){
        butON.setOnClickListener(view -> {
            sendMessage(topic, ON);
            Toast.makeText(getBaseContext(), "Click ON", Toast.LENGTH_SHORT).show();
        });

        butOFF.setOnClickListener(view -> {
            sendMessage(topic, OFF);
            Toast.makeText(getBaseContext(), "Click OFF", Toast.LENGTH_SHORT).show();
        });
    }

    public void getClientName(){
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        clientID = manufacturer + " " + model;

        labClientID.setText(clientID);
    }


    private void connectBroker(){
        client = new MqttAndroidClient(getApplicationContext(), host, clientID);
        options = new MqttConnectOptions();
        options.setUserName(user);
        options.setPassword(pass.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getBaseContext(), "Conectado", Toast.LENGTH_SHORT).show();
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getBaseContext(), "Conexión fallida", Toast.LENGTH_SHORT).show();
                }
            });



        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void checkConnection(){
        if (client.isConnected()){
            permission = true;
        }else{
            permission = false;
        }
    }

    public void sendMessage(String topic, String msg){
        checkConnection();
        if (permission){
            int qos = 0;

            try {
                client.publish(topic, msg.getBytes(), qos, false);
            } catch (MqttException e) {
               e.printStackTrace();
            }

        }
    }

    private void subscribeToTopic(){
        try {
            client.subscribe(topic, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), "lost connect", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message)
                    throws Exception {
                if (topic.matches(MainActivity.topic)){
                    String msg = new String(message.getPayload());
                    if(msg.matches(ON)){
                        labInfo.setBackgroundColor(Color.GREEN);
                    }else if (msg.matches(OFF)){
                        labInfo.setBackgroundColor(Color.RED);
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }

}