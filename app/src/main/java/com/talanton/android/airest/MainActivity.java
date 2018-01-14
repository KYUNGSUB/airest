package com.talanton.android.airest;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.talanton.android.airest.items.AiResult;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServerInterworking.ReportPlayStatus {
    private static final String STORY_ID = "599522eb65d4407baca2c25a";
    private static final String AI_SERVER_URL = "http://mindmap.ai:8000/v1/" + STORY_ID;
    private static final String TAG = "debug";
    TextView msgView;
    EditText dataView;
    private ServerInterworking mSi;
    private int status = 0;
    private AiResult aiResult;
    private static final int RESULT_SPEECH = 1;
    TextToSpeech tts;
    Handler mHandler = new Handler();

    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://192.168.0.3:1883";

    final String clientId = "paho";
    final String subscriptionTopic = "outTopic";
    final String publishTopic = "inTopic";
    String publishMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMQtt();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int state) {
                if(state != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        msgView = (TextView)findViewById(R.id.msg);
        dataView = (EditText)findViewById(R.id.data);
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataView.getText().length() > 0) {
                    String data = dataView.getText().toString();
                    status++;
                    msgView.append("-> " + data + "\n");
                    chatBotInterworking(AI_SERVER_URL, data);
                }
            }
        });

        Button sttBtn = (Button)findViewById(R.id.sttBtn);
        sttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);  // Intent 생성
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());  // 호출할 패키지
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");  // 인식할 언어 설정
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말해주세요");
                Toast.makeText(MainActivity.this, "start speak", Toast.LENGTH_SHORT).show();

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "Speech To Text를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    e.getStackTrace();
                }
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSi = new ServerInterworking(MainActivity.this);
                mSi.registerCallbackPlay(MainActivity.this);
                status = 1;
                String initialQuery = makeRestApiRequestData("");
                Log.i(TAG, "query = " + initialQuery);
                mSi.reportPlayStateToServer(AI_SERVER_URL, initialQuery);
            }
        }, 1000);
    }

    private void initMQtt() {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + serverUri);
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void addToHistory(String mainText){
        System.out.println("LOG: " + mainText);
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
//                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                    addToHistory("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            addToHistory("Message Published");
            if(!mqttAndroidClient.isConnected()){
                addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chatBotInterworking(String aiServerUrl, String data) {
        String initialQuery = makeRestApiRequestData(data);
        Log.i(TAG, "query = " + initialQuery);
        mSi.reportPlayStateToServer(AI_SERVER_URL, initialQuery);
    }

    @Override
    public void callbackReturnReportPlayStatus(String result) {
        Log.i(TAG, result);
        storeResult(result);
        String viewResult = aiResult.getOutput().getText().get(0);
        publishMessage = viewResult;
        publishMessage();
        msgView.append(viewResult + "\n");
        dataView.setText("");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(viewResult);
        } else {
            ttsUnder20(viewResult);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private void storeResult(String result) {
        Gson gson = new Gson();
        aiResult = gson.fromJson(result, AiResult.class);
    }

    private String makeRestApiRequestData(String s) {
        String request = null;
        switch(status) {
            case 1:
                request = "{\"story_id\":\"599522eb65d4407baca2c25a\",\"context\":{},\"input\":{}}";
                /* response
                {
                "entities":[],"story_id":"599522eb65d4407baca2c25a",
                "output":
                {
                    "visit_nodes_text":["안녕하세요. 만나서 반갑습니다."],"visit_nodes":["e6eb6dd2-e34e-480b-9ae7-3ff3a7e6aa5c"],"visit_nodes_name":[".start"],"text":["안녕하세요. 만나서 반갑습니다."]
                },
                "_id":"5996ed6c65d440218fa2c01d","timeutc":"2017-08-18T13:36:44.793",
                "intents":{"all":[],"selected":null,"candidates":[]},
                "channel":{"channel_id":"mrmind_client","user_id":null},
                "input":{"text":""},
                "context":
                {
                    "random":false,"input_field":null,
                    "information":
                    {
                        "conversation_counter":1,"user_request_counter":1,
                        "conversation_stack":[{"conversation_node":"e6eb6dd2-e34e-480b-9ae7-3ff3a7e6aa5c","conversation_node_name":".start"}]
                    },
                    "visit_counter":0,"retrieve_field":false,"conversation_id":"67926733-6d07-48ba-a3ff-45bb5d0c220c","variables":null,"reprompt":false,"keyboard":null,"message":null
                },
                "time":"2017-08-18T22:36:44.793"
                }
                */
                break;
            default:
           		/* request
                {"story_id":"599522eb65d4407baca2c25a",
                "context":
                {
                    "random":false,"input_field":null,
                    "information":
                    {
                        "conversation_counter":1,"user_request_counter":1,
                        "conversation_stack":[{"conversation_node":"e6eb6dd2-e34e-480b-9ae7-3ff3a7e6aa5c","conversation_node_name":".start"}]
                    },
                    "visit_counter":0,"retrieve_field":false,"conversation_id":"f79ab9d0-bb55-4c70-a6ba-17d4a5397466","variables":null,"reprompt":false,"keyboard":null,"message":null
                },
                "input":{"text":"안녕"}
                }
                */
           		JSONObject rootObj = new JSONObject();
                try {
                    rootObj.put("story_id", aiResult.getStory_id());
                    JSONObject context = new JSONObject();
                    context.put("random", aiResult.getContext().isRandom());
                    context.put("input_field", JSONObject.NULL);
                    JSONObject information = new JSONObject();
                    information.put("conversation_counter", aiResult.getContext().getInformation().getConversation_counter());
                    information.put("user_request_counter", aiResult.getContext().getInformation().getUser_request_counter());
                    JSONObject conversation_stackObj = new JSONObject();
                    conversation_stackObj.put("conversation_node", aiResult.getContext().getInformation().getConversation_stack().get(0).getConversation_node());
                    conversation_stackObj.put("conversation_node_name", aiResult.getContext().getInformation().getConversation_stack().get(0).getConversation_node_name());
                    JSONArray conversation_stackArray = new JSONArray();
                    conversation_stackArray.put(conversation_stackObj);
                    information.put("conversation_stack", conversation_stackArray);
                    context.put("information", information);
                    context.put("visit_counter", aiResult.getContext().getVisit_counter());
                    context.put("retrieve_field", aiResult.getContext().isRetrieve_field());
                    context.put("conversation_id", aiResult.getContext().getConversation_id());
                    context.put("variables", JSONObject.NULL);
                    context.put("reprompt", aiResult.getContext().isReprompt());
                    context.put("keyboard", JSONObject.NULL);
                    context.put("message", JSONObject.NULL);
                    rootObj.put("context", context);
                    JSONObject inputObj = new JSONObject();
                    inputObj.put("text", s);
                    rootObj.put("input", inputObj);
                    request = rootObj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return request;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == RESULT_SPEECH) {
            // 음성인식 결과
            ArrayList<String> sstResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // 결과들 중 음성과 가장 유사한 단어부터 시작되는 0번째 문자열을 저장
            String result_sst = sstResult.get(0);
            status++;
            msgView.append("-> " + result_sst + "\n");
            chatBotInterworking(AI_SERVER_URL, result_sst);
        }
    }
}

/*
{
"entities":[{"keyword":"삼성전자","entity":"service.주식종목명","value":"삼성전자"}],
"story_id":"599522eb65d4407baca2c25a",
"output":{"visit_nodes_text":["%s"],"visit_nodes":["54144d5d-c179-4ad3-a77f-59ba32ccfdac"],"visit_nodes_name":["#service.주식"],"text":["검색결과 값이 없습니다."]},"
_id":"5997e3be65d4403f98a2c023","timeutc":"2017-08-19T07:07:41.046",
"intents":
    {
    "all":[{"intent":"service.주식","confidence":"0.50098615872142","example":"주가"},{"intent":"service.주식","confidence":1.0,"example":"주식"}],
    "selected":{"intent":"service.주식","confidence":1.0,"example":"주식"},
    "candidates":[{"intent":"service.주식","confidence":"0.50098615872142","example":"주가"},{"intent":"service.주식","confidence":1.0,"example":"주식"}]
    },
"channel":{"channel_id":"mrmind_client","user_id":null},
"input":{"text":"삼성전자 주식"},
"context":
    {
    "random":false,"input_field":null,
    "information":
        {
        "conversation_counter":6,"user_request_counter":6,
        "conversation_stack":[{"conversation_node":"root","conversation_node_name":"루트노드"}]
        },
    "visit_counter":0,"retrieve_field":true,"conversation_id":"3b3e9999-ad0f-48a1-9074-5eac4f608722","variables":null,"reprompt":false,"keyboard":null,"message":null
    },
"time":"2017-08-19T16:07:41.046"
}
*/