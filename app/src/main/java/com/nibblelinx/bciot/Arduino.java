package com.nibblelinx.bciot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
// Melhorias Necessarias:
// substituir os metodos deprected: startActivityForResult e aqules da Classe Lista de Dispositivos
//      este sao parte do controle de Bluetooth;
// (Concluido)Utilizar o metodo de leitura de TX completa via Bitails API
//      https://docs.bitails.net/#download-transaction
// Melhorar metodos de guardar no dispositivo TXs que ainda nao foram confirmadas;
// Melhorar metodo de acesso as TXs armazenadas no dispositivo;
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////

public class Arduino extends AppCompatActivity {



    //////////////////////////////////////////////////////////////////
    //Buca Na Rede
    //////////////////////////////////////////////////////////////////

    ListView lista;
    Boolean AddSend = true;

    String dataOR = "";
    String lastDataOR = "";

    /////////////////////////////////////
    // Ouvir a Blockchian
    /////////////////////////////////////
    String jsonStr = "";
    String myURL = "";
    String searchStr = "\"tx_hash\":\"";
    String urlBaseAddress = "https://api.whatsonchain.com/v1/bsv/main/address/";

    private String myBSVaddress;
    private String BSVAdddressSend;
    private String BSVAdddressReceive;

    int lastLength = 0;
    int lastLengthSend = 0;
    int lastLengthReceive = 0;
    int numberOfTxid = 0;

    Timer timer = new Timer();
    //Timer timer;

    Boolean firstRunTimer = true;

    static Boolean TimerRun = true;

    Boolean netRunFirst = true;

    //String[] txIDVector = new String[1000];
    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////


    // Button ButtonMS9Back;

    Button buttonConectarArduino, buttonTEMP, buttonLED1, buttonLED2, buttonLED3;
    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;

    private static final int MESSAGE_READ = 3;

    MyBluetoothService.ConnectedThread connectedThread;

    static Handler handler;

    StringBuilder dadosBluetooth = new StringBuilder();

    BluetoothAdapter meuBluetoothAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;

    boolean conexao = false;
    UUID meu_UUID =  UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////////////////////
        // Ouvir a Blockchian
        /////////////////////////////////////
        BSVAdddressSend = Variables.ArduinoBSVAdd;

        BSVAdddressReceive = BSVAdddressSend;
        myBSVaddress = BSVAdddressSend;
        lastDataOR = dataOR;

        myURL = urlBaseAddress + myBSVaddress + "/history";

//        for(int i = 0; i < 1000; i++)
//            txIDVector[i] = "";

        // A cada 5 segundos o sistema checa se tem algo novo no endereco informado
//        timer.schedule(new TimeCheckURL(), 0, 5000);

        /////////////////////////////////////
        /////////////////////////////////////




        setContentView(R.layout.activity_arduino);

        buttonConectarArduino = findViewById(R.id.buttonConectarArduino);
        buttonTEMP = findViewById(R.id.buttonTEMP);
        buttonLED1 = findViewById(R.id.buttonLED1);
        buttonLED2 = findViewById(R.id.buttonLED2);
        buttonLED3 = findViewById(R.id.buttonLED3);

        meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        if(meuBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui bluetooth", Toast.LENGTH_LONG).show();
        }else if(!meuBluetoothAdapter.isEnabled()) {
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);
        }

        buttonConectarArduino.setOnClickListener(view -> {
            if(conexao){
                //desconectar
                try{
                    meuSocket.close();
                    conexao = false;
                    buttonConectarArduino.setText("Conectar");
                    Toast.makeText(getApplicationContext(), "O bluetooth foi desconectado", Toast.LENGTH_LONG).show();

                } catch (IOException erro){
                    Toast.makeText(getApplicationContext(), "ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();

                }
            }else{
                Intent abreLista = new Intent(Arduino.this, ListaDispositivos.class);//conectar
                startActivityForResult(abreLista, SOLICITA_CONEXAO );
            }
            //timer.schedule(new TimeCheckURL(), 0, 5000);
        });

        buttonTEMP.setOnClickListener(view -> {

            /*
            if(newTimer) {
                timer = new Timer();

                newTimer = false;
            }
            timer.schedule(new TimeCheckURL(), 0, 5000);

            */


            if(conexao) {

                //    connectedThread.enviar("led1");
                //    sendDataToBc(arduinoIDsend + "100");
                cyclesHandShack = 6;
                //timer.schedule(new TimeCheckURL(), 5000, 5000);


                if(newTimer) {
                    timer = new Timer();

                    newTimer = false;
                    //timer.schedule(new TimeCheckURL(), 5000, 5000);
                    timer.schedule(new TimeCheckURL(),0, 5000);

                    connectedThread.enviar("led1");  //adicionado
                    Toast.makeText(getApplicationContext(), "Testando", Toast.LENGTH_LONG).show();  //adicionado
                    addNewData("0100");  //adicionado

                }
                //timer.schedule(new TimeCheckURL(), 0, 5000);


            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
            }

        });

        buttonLED1.setOnClickListener(view -> {

            /*
            if(newTimer) {
                timer = new Timer();

                newTimer = false;
            }
            timer.schedule(new TimeCheckURL(), 0, 5000);

            */


            if(conexao) {

            //    connectedThread.enviar("led1");
            //    sendDataToBc(arduinoIDsend + "100");
                cyclesHandShack = 6;
                //timer.schedule(new TimeCheckURL(), 5000, 5000);


                if(newTimer) {
                    timer = new Timer();

                    newTimer = false;
                    //timer.schedule(new TimeCheckURL(), 5000, 5000);
                    timer.schedule(new TimeCheckURL(),0, 5000);
                }
                //timer.schedule(new TimeCheckURL(), 0, 5000);


            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
            }

        });



        buttonLED2.setOnClickListener(view -> {

            if(conexao){
                connectedThread.enviar("led2");
            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();

            }
        });

        buttonLED3.setOnClickListener(view -> {

            if(conexao){
                connectedThread.enviar("led3");
            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth não está conectado", Toast.LENGTH_LONG).show();

            }
        });

        handler = new Handler(Looper.myLooper()) {

            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_READ){
                    String recebidos = (String) msg.obj;

                    dadosBluetooth.append(recebidos);

                    int fimInformacao = dadosBluetooth.indexOf("}");

                    if(fimInformacao > 0) {

                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);

                        int tamInformacao = dadosCompletos.length();

                        if(dadosBluetooth.charAt(0) ==  '{'){

                            String dadosFinais = dadosBluetooth.substring(1, tamInformacao);

                            Log.d("Recebidos", dadosFinais);
                            Variables.Arduino = dadosFinais;

                            if(dadosFinais.contains("l1on")){
                                buttonLED1.setText("LED 1 LIGADO");
                                Log.d("LED1", "ligado");
                            }else if(dadosFinais.contains("l1off")){
                                buttonLED1.setText("LED 1 DESLIGADO");
                                Log.d("LED1", "desligado");

                            }

                            if(dadosFinais.contains("l2on")){
                                buttonLED2.setText("LED 2 LIGADO");
                                Log.d("LED2", "ligado");
                            }else if(dadosFinais.contains("l2off")){
                                buttonLED2.setText("LED 2 DESLIGADO");
                                Log.d("LED2", "desligado");

                            }


                            if(dadosFinais.contains("l3on")){
                                buttonLED3.setText("LED 3 LIGADO");
                                Log.d("LED3", "ligado");
                            }else if(dadosFinais.contains("l3off")){
                                buttonLED3.setText("LED 3 DESLIGADO");
                                Log.d("LED3", "desligado");

                            }

                        }
                        dadosBluetooth.delete(0,dadosBluetooth.length());
//                        textFormat(Variables.Arduino);

                    }
                }
            }
        };



    }



    public void textFormat(String text){

        int numberOfNonChar = 0;

        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) > 0xFF) numberOfNonChar++;

        byte[] newTextChar = new byte[text.length() + numberOfNonChar];

        for (int i = 0, j = 0; i < text.length(); i++, j++) {
            if (text.charAt(i) > 0xFF) {
                newTextChar[j] = (byte) ((text.charAt(i) / 0x100) & 0xFF);
                j++;
                newTextChar[j] = (byte) (text.charAt(i) & 0xFF);
            } else {
                newTextChar[j] = (byte) (text.charAt(i) & 0xFF);
            }
        }
        sendTX(newTextChar);
    }

    public void sendTX(byte[] newTextChar)
    {

        String pvtkey = Variables.MainPaymail;
        //String pvtkey = "9567767df8b9bd7e2b003b25db22e63f013f8bf8f53299890e3a811f60bad57b";
        //String sendTo = "17h1rUo6Dnzfy7bApVzi1WZmxtWVWBGaet";
        //String sats = Satoshis.getText().toString();
        String data = SHA256G.ByteToStrHex(newTextChar);


        //////////////////////////////////////////////////////////////////////////////////////////////////
        //Preparação das Chaves
        //////////////////////////////////////////////////////////////////////////////////////////////////
        Keygen pubKey = new Keygen();
        //Boolean CompPKey = false;
        //Variables.CompPKey = false;

        Variables.CompPKey = true;

        String PUBKEY = pubKey.publicKeyHEX(pvtkey); //PVTKEY - string Hexadecimal de 64 elementos.
        String BSV160 = pubKey.bsvWalletRMD160(PUBKEY, Variables.CompPKey);
        String BSVADD = pubKey.bsvWalletFull(PUBKEY, Variables.CompPKey);


        /////////////////////////////////////////////////////////////////////
        //User Data Input
        /////////////////////////////////////////////////////////////////////

        String [] PayWallets = new String[10];
        String [] PayValues = new String[10];
        String [] OP_RETURNs = new String[10];

        //PayWallets[0] = "1B69q3ZY6VsuKwCinvbB5tkKWLjHWfGz1J"; //MoneyButton
        //PayWallets[0] = sendTo; //Carteira para onde esta sendo enviado

        //PayWallets[1] = BSVADD;
        PayWallets[0] = BSVADD;

        //PayWallets[0] = "17h1rUo6Dnzfy7bApVzi1WZmxtWVWBGaet"; //Enviar para o Arduino 2
        //PayWallets[0] = "1HGKo8anaxokGkLzhbxYMGWpwC7yrxeeGd"; //Enviar para o Arduino 2

        //PayWallets[0] = Variables.ArduinoBSVAdd;
        //PayValues[0] = "20";



        //PayValues[0] = "1000";
        //PayValues[0] = sats;
        //...at the name of Jesus every knee should bow, of things in heaven, and things in earth, and things under the earth;
        //OP_RETURNs[0] = "2e2e2e617420746865206e616d65206f66204a65737573206576657279206b6e65652073686f756c6420626f772c206f66207468696e677320696e2068656176656e2c20616e64207468696e677320696e2065617274682c20616e64207468696e677320756e646572207468652065617274683b";

        int nOR = 0;
        if(data.length() > 0) {
            //OP_RETURNs[0] = StrToHex(data);
            OP_RETURNs[0] = SHA256G.ByteToStrHex(newTextChar);
            //OP_RETURNs[0] = "5465737465204e205454542074" + "5465737465204e205454542074";
            nOR = 1;
        }

        if(nOR == 0) {

            Toast.makeText(Arduino.this, "No Data!!!"
                    , Toast.LENGTH_LONG).show();
            return;
        }

        /////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////

        BsvTxCreation txCreate = new BsvTxCreation();

        //Problema Aqui;
        String newTX = txCreate.txBuilder(pvtkey, Variables.CompPKey,1 + nOR, PayWallets,PayValues,OP_RETURNs, nOR);
        //String newTX = txCreate.txBuilder(pvtkey, Variables.CompPKey,2 + nOR, PayWallets,PayValues,OP_RETURNs, nOR);
        String result = "";
        //if(newTX.compareTo("Error 1")==0 || newTX.compareTo("Error 2")==0)

        if(newTX.length()>5)
            if(newTX.substring(0,5).compareTo("Error")==0)
                result = newTX;


        //if(newTX.substring(0,5).compareTo("Error")==0)
        //    result = newTX;
        //else
        {

            Variables.LastTxHexData = newTX;

            BsvTxOperations bsvTxOp = new BsvTxOperations();
            bsvTxOp.txID(newTX);
            Variables.LastTXID = bsvTxOp.TXID;

//            int i = 0;

            ////////////////////////////////////////////////////////////////////////////////
            //Guarda a ultima transação que cujo envio foi mal sucedido e tenta novamente
            ////////////////////////////////////////////////////////////////////////////////
/*            if(Variables.txPrevious.compareTo("") == 0)
            {
                result = txCreate.txBroadCast(newTX);

                if(result.length() == 64)
                    Variables.txPrevious = "";
                else
                    Variables.txPrevious = newTX;

            }
            else
            {
                result = txCreate.txBroadCast(Variables.txPrevious);
                if(result.length() == 64)
                    Variables.txPrevious = "";
                //else
                //    Variables.txPrevious = Variables.txPrevious;
            }
*/                //    result = txCreate.txBroadCast(newTX);
            ////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////


            result = txCreate.txBroadCast(newTX);

            //não esta dando certo desta forma
            //if(result.compareTo("Bad Request")==0)
            //    result = txCreate.txBroadCast(newTX);

            /*
            while (result.length() < 64 && i < 5) {
                result = txCreate.txBroadCast(newTX);
                i++;
            }

            */


            //result = "Debug";
        }

        //result = txCreate.totalUnspent(BSVADD);

        //Toast.makeText(NFTText.this, "Result: " + OP_RETURNs[0]
        //Toast.makeText(NFTText.this, "Result: " + result + 1 + nOR
       // Toast.makeText(Arduino.this, "Result: " + result
                        //+ newTX
       //         , Toast.LENGTH_LONG).show();

        Variables.resultArduino = result;

        //((EditText) findViewById(R.id.ET_TEXTOST)).setText(result);

        if(firstRunTimer) {
            //timer = new Timer();
            //timer.schedule(new TimeCheckURL(), 0, 5000);
            firstRunTimer = false;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {

            case SOLICITA_ATIVACAO:
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(), "O bluetooth foi ativado", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "O bluetooth não foi ativado, o aplicativo será encerrado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case SOLICITA_CONEXAO:
                if(resultCode == Activity.RESULT_OK){
                    assert data != null;
                    String MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
                    Toast.makeText(getApplicationContext(), "MAC Final: " + MAC, Toast.LENGTH_LONG).show();
                    meuDevice = meuBluetoothAdapter.getRemoteDevice(MAC);

                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(meu_UUID);
                        meuSocket.connect();
                        conexao = true;

                        connectedThread = new MyBluetoothService.ConnectedThread(meuSocket);
                        connectedThread.start();

                        buttonConectarArduino.setText("Desconectar");
                        Toast.makeText(getApplicationContext(), "Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        conexao = false;
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + MAC, Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "falha ao obter o MAC", Toast.LENGTH_LONG).show();

                }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public static class MyBluetoothService {
        private static final String TAG = "MY_APP_DEBUG_TAG";

        // Defines several constants used when transmitting messages between the
        // service and the UI.


        private static class ConnectedThread extends Thread {
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket) {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating input stream", e);
                }
                try {
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating output stream", e);
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                // mmBuffer store for the stream
                byte[] mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    try {
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);

                        String dadosBt = new String(mmBuffer,0, numBytes);

                        // Send the obtained bytes to the UI activity.
                        handler.obtainMessage(MESSAGE_READ, numBytes, -1, dadosBt).sendToTarget();

                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        break;
                    }
                }
            }


            public void enviar(String dadosEnviar) {
                byte[] msgBuffer = dadosEnviar.getBytes();
                try {
                    mmOutStream.write(msgBuffer);

                } catch (IOException ignored) {

                }
            }

        }
    }


    /////////////////////////////////////
    // Ouvir a Blockchian
    /////////////////////////////////////

    private void sendDataToBc (String dataToSend)
    {
        //BcDataSend sendData = new BcDataSend();
        //String result = sendData.dataSend(dataToSend);


        textFormat(dataToSend);

        //String result = dataSend(dataToSend);

        //String result = Variables.MainPaymail;
       // Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    int sendCont = 0;

    //int runTurn = 0;

    String arduinoIDsend = "0";
    String arduinoIDthis = "0";

    //int cyclesHandShack = 5;
    int cyclesHandShack = 0;

    private void addNewData (String newData)
    {
        //timer.schedule(new TimeCheckURL(), 0, 5000);
        dataOR = newData;
        //Toast.makeText(this, newData, Toast.LENGTH_LONG).show();

        /*
        if(newData.length()>0) {
            Toast.makeText(this, "Inicio de Sequencia:\n" + newData, Toast.LENGTH_LONG).show();
            return;
        }
        */


        /*
        if(cyclesHandShack > 0)
            cyclesHandShack--;
        else
            return;

        */

        if(cyclesHandShack <= 0)
            return;

        timer.cancel();
        timer.purge();


        if(lastDataOR.compareTo(dataOR) != 0) {
            Toast.makeText(this, "Inicio de Sequencia:\n" + newData, Toast.LENGTH_LONG).show();
            //Toast.makeText(this, "ABC  " + OPRETURNDATA, Toast.LENGTH_LONG).show();
            lastDataOR = dataOR;

            if(newData.substring(0,1).compareTo("0")==0)
            {

                if(newData.substring(1,2).compareTo("1")==0)
                {
                    Variables.resultArduino = "";

                    sendDataToBc(arduinoIDsend + "010");

                    if(Variables.resultArduino.length() == 64) {
                        Toast.makeText(this,
                                "Input: 0100\n" +
                                        "Output: " + arduinoIDthis + "010" + "\n" +
                                        "TXID: " + Variables.resultArduino,
                                Toast.LENGTH_LONG).show();
                        cyclesHandShack--;

                        if(conexao){
                            connectedThread.enviar("led1");
                        }
                    }
                    //Toast.makeText(this, arduinoIDthis + "100", Toast.LENGTH_LONG).show();

                    //return;
                }

                if(newData.substring(2,3).compareTo("1")==0)
                {
                    Variables.resultArduino = "";

                    sendDataToBc(arduinoIDsend + "001");

                    if(Variables.resultArduino.length() == 64) {

                        Toast.makeText(this,
                                "Input: 0010\n" +
                                        "Output: " + arduinoIDthis + "001" + "\n" +
                                        "TXID: " + Variables.resultArduino,
                                Toast.LENGTH_LONG).show();


                        //Toast.makeText(this, arduinoIDthis + "010" + "\n" + Variables.resultArduino, Toast.LENGTH_LONG).show();
                        cyclesHandShack--;

                        if(conexao){
                            connectedThread.enviar("led2");
                        }
                    }

                    //Toast.makeText(this, arduinoIDthis + "010", Toast.LENGTH_LONG).show();
                    //return;
                }

                if(newData.substring(3,4).compareTo("1")==0)
                {
                    Variables.resultArduino = "";

                    sendDataToBc(arduinoIDsend + "100");

                    if(Variables.resultArduino.length() == 64) {

                        Toast.makeText(this,
                                "Input: 0001\n" +
                                        "Output: " + arduinoIDthis + "100" + "\n" +
                                        "TXID: " + Variables.resultArduino,
                                Toast.LENGTH_LONG).show();

                        //Toast.makeText(this, arduinoIDthis + "001" + "\n" + Variables.resultArduino, Toast.LENGTH_LONG).show();
                        cyclesHandShack--;

                        if(conexao){
                            connectedThread.enviar("led3");
                        }
                    }

                    //Toast.makeText(this, arduinoIDthis + "001", Toast.LENGTH_LONG).show();

                    //return;
                }
            }

            /////////////////////////////////////////
            /////////////////////////////////////////
            /*
            if(sendCont == 0)
            {
                //sendDataToBc("Teste 1 2 + SendCont: " + sendCont + 1);
                sendCont++;
            }

            */
            /////////////////////////////////////////
            /////////////////////////////////////////
        }

        timer = new Timer();
        timer.schedule(new TimeCheckURL(), 1000, 5000);

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
// Monitoramento de chegada de dados
//////////////////////////////////////////////////////////////////////////////////////////////////

    //NESTED CLASS: nao precisa colocar fora
    //Inner Class: https://www.tutorialspoint.com/java/java_innerclasses.htm
    //https://stackoverflow.com/questions/12908412/print-hello-world-every-x-seconds
    private String resultSTR;

    class TimeCheckURL extends TimerTask
    {
        public void run()
        {

            if(TimerRun) {
                //if(netOn) {

                resultSTR = null;
                new JsonTask().onPreExecute();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //if(!firstRunTimer) {
                            resultSTR = new JsonTask().execute(myURL);

                            //Para a execução na thread pricipal
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // do onPostExecute stuff
                                    // if(!firstRunTimer)
                                    new JsonTask().onPostExecute(resultSTR);
                                }
                            });
                        //}
                    }
                }).start();
            }


        }
    }

    //https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    //https://stackoverflow.com/questions/37232927/app-crashes-when-no-internet-connection-is-available
    //https://stackoverflow.com/questions/32547006/connectivitymanager-getnetworkinfoint-deprecated

    //CheckNetwork network = new CheckNetwork(getApplicationContext());

    // Network Check
    //https://gist.github.com/PasanBhanu/730a32a9eeb180ec2950c172d54bb06a
    //https://gist.github.com/Abhinav1217/0ff6b39e70fa38379d61e85e09b49fe7

    //https://developer.android.com/reference/android/net/ConnectivityManager
    //https://developer.android.com/reference/android/net/ConnectivityManager#getActiveNetwork()
    //public void onNetwork()
    //public boolean onNetwork()

    //NESTED CLASS: nao precisa colocar fora
    //Inner Class: https://www.tutorialspoint.com/java/java_innerclasses.htm
    // https://stackoverflow.com/questions/33229869/get-json-data-from-url-using-android
    private class JsonTask //extends AsyncTask<String, String, String>
    {
        protected void onPreExecute()
        {

            //super.onPreExecute();
        }

        //protected String doInBackground(String... params)
        protected String execute(String... params)
        {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try
            {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line+"\n");
                    //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                    ///////////////////////////////////////////////////////////////////
                    //NAO FUNCIONA COLOCAR O CONTEXTO DA CLASSE EXTERNA
                    ///////////////////////////////////////////////////////////////////
                    //Toast.makeText(mContext, "> "+ line, Toast.LENGTH_LONG).show();
                    //Toast.makeText(MActJSON, "> "+ line, Toast.LENGTH_LONG).show();
                }

                return buffer.toString();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();

                //return null;
                //urlProccess("ABC");
            }
            catch (IOException e)
            {
                e.printStackTrace();
                //return null;
                //urlProccess("ABC");
            }
            finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    //return null;
                }
                //urlProccess("ABC");
                //return null;
            }
            return null;
        }

        //@Override
        protected void onPostExecute(String result)
        {
            //super.onPostExecute(result);

            ///////////////////////////////////////////////////////////////////
            //VERIFICAR
            ///////////////////////////////////////////////////////////////////

            //AQUI ESTE METODO FUNCIONA
            //((EditText)findViewById(R.id.etvJsonItem)).setText(result);

            //Resposta precisa ser uma variavel global

            //jsonStr = result;

            //CHAMA A FUNCAO DE PROCESSAMENTO
            //https://stackoverflow.com/questions/2920525/how-to-check-a-string-against-null-in-java
            //if(result.equals(null)) urlProccess(jsonStr, false);
            if(result == null) urlProccess(jsonStr, false);
            else urlProccess(result, true);

            //lastLength = jsonStr.length();
        }
    }

    private Boolean firstRun = true;

    private Boolean txProcessed = false;

    //Executa depois que o JSON eh extraido
    //public String urlProccess(String urlContent, Boolean Flag)
    //private String urlProccess(String urlContent, Boolean Flag)
    private void urlProccess(String urlContent, Boolean Flag)
    {
        jsonStr = urlContent;
        int firstIndiceOf;
        int lastIndiceOf;

        if(myBSVaddress.compareTo(BSVAdddressSend) == 0) lastLength = lastLengthSend;
        else lastLength = lastLengthReceive;

        String lastTXIDSEQ = "";

        //Registrar no DB este tamanho;
        if(Flag)
        {
            //Toast.makeText(TxidList.this, "Length: " + lastLength + "\n" + jsonStr, Toast.LENGTH_SHORT).show();
            //if (jsonStr.length() > 0)
            if (lastLength != jsonStr.length() || !txProcessed)
            //if (lastJsonStr.compareTo(jsonStr) != 0)
            {
                //jsonStrLast = jsonStr;
                String Txid;

                txProcessed = true;

                //https://www.geeksforgeeks.org/searching-for-character-and-substring-in-a-string/
                firstIndiceOf = jsonStr.indexOf(searchStr);
                lastIndiceOf = jsonStr.lastIndexOf(searchStr);

                if((firstIndiceOf == -1) || (lastIndiceOf == -1))
                {
                    //Toast.makeText(TxidList.this, "NO DATA IN: " + myBSVaddress, Toast.LENGTH_SHORT).show();
                    if(AddSend) {
                        myBSVaddress = BSVAdddressReceive;
                        AddSend = false;
                    }
                    else
                    {
                        myBSVaddress = BSVAdddressSend;
                        AddSend = true;
                    }
                    myURL = urlBaseAddress + myBSVaddress + "/history";
                    return;
                    //return jsonStr;
                }

                int i = 1;
                //position = "Posicao " + i + ": " + firstIndiceOf + "\n";

                //https://beginnersbook.com/2013/12/java-string-substring-method-example/
                //TXID tem sempre 64 characteres 256 bits
                Txid = jsonStr.substring(firstIndiceOf + searchStr.length(), firstIndiceOf + searchStr.length() + 64);

                /*
                if(firstRun)
                {
                    txIDVector[i-1] = Txid;
                }
                else {

                    for (int j = 0; j < 1000; j++) {

                        if(txIDVector[j].compareTo(Txid) == 0)
                            break;
                        else if (txIDVector[j].compareTo("") == 0){
                            txIDVector[j] = Txid;
                            break;
                        }
                    }
                }
                */

                //Registro e Organizacao de TXIDs
                for (int j = 0; j < 1000; j++) {

                    if(Variables.txIDVector[j].compareTo(Txid) == 0)
                        break;
                    else if (Variables.txIDVector[j].compareTo("") == 0){
                        Variables.txIDVector[j] = Txid;
                        break;
                    }
                }


                while (firstIndiceOf < lastIndiceOf) {
                    i++;
                    firstIndiceOf = jsonStr.indexOf(searchStr, firstIndiceOf + 1);
                    //position += "Posicao " + i + ": " + firstIndiceOf + "\n";

                    Txid = jsonStr.substring(firstIndiceOf + searchStr.length(), firstIndiceOf + searchStr.length() + 64);

                    //txIDVector[i-1] = Txid;

                    for (int j = 0; j < 1000; j++) {

                        if(Variables.txIDVector[j].compareTo(Txid) == 0)
                            break;
                        else if (Variables.txIDVector[j].compareTo("") == 0){
                            Variables.txIDVector[j] = Txid;
                            break;
                        }
                    }

                    //Iniciar, processar o ultimo TXID;
                    //Depois disso registrar todos
                    //Comparar o ultimo processado;
                    //registar todos os TXIDs
                    //registrar os ultimos TXIDs processados;

                    lastTXIDSEQ = Txid;
                }
                numberOfTxid = i;

                //Toast.makeText(BcListen.this, "Last TXID: " + Txid, Toast.LENGTH_SHORT).show();

                firstRun = false;
                //lastDataOR = dataOR;
                //dataRead(Txid);

                Toast.makeText(this, "Last TXID: " + Variables.txIDVector[numberOfTxid-1], Toast.LENGTH_SHORT).show();

                dataRead(Variables.txIDVector[numberOfTxid-1]);

                //Toast.makeText(BcListen.this, "Data OR: " + dataOR, Toast.LENGTH_SHORT).show();



                //  lista.setAdapter(banco.ReadContacts(BSVAdddressSend, BSVAdddressReceive));

            }

            if(myBSVaddress.compareTo(BSVAdddressSend) == 0) lastLengthSend = jsonStr.length();
            else lastLengthReceive = jsonStr.length();


            netRunFirst = false;

            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(netRunFirst) {
                //   lista.setAdapter(banco.ReadContacts(BSVAdddressSend, BSVAdddressReceive));
                netRunFirst = false;
            }
            //Toast.makeText(TxidList.this, myNet, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Bad or no Conection 01", Toast.LENGTH_SHORT).show();
        }


        if(AddSend) {
            myBSVaddress = BSVAdddressReceive;
            AddSend = false;
        }
        else
        {
            myBSVaddress = BSVAdddressSend;
            AddSend = true;
        }
        myURL = urlBaseAddress + myBSVaddress + "/history";

        //return jsonStr;
        return;
    }



    String TXID;
    String urlBaseTXID2 = "";
    boolean url2 = false;
    private String result;
    String jsonStrTXID = "";


    private void dataRead(String TXID)
    {
        url2 = true;
        //urlBaseTXID2 = "https://api.whatsonchain.com/v1/bsv/main/tx/" + TXID + "/out/0/hex"; //não funciona mais para TX nao confimada


        urlBaseTXID2 = "https://whatsonchain.com/tx/" + TXID;

        //urlBaseTXID2 = "https://api.whatsonchain.com/v1/bsv/main/tx/" + TXID + "/out/hex";

        result = null;

        new Thread(new Runnable() {
            @Override
            public void run() {

                //Looper.prepare();
                result = new JsonTask().execute(urlBaseTXID2);;
                //Looper.loop();

                //Para a execução na thread pricipal
                runOnUiThread(new Runnable() {
                    public void run() {
                        // do onPostExecute stuff
                        //new JsonTask().onPostExecute(result);


                        if(result == null) //urlProccessTXID(jsonStrTXID, false);
                            urlProccessTXID(result, false);
                        else
                            urlProccessTXID(result, true);
                    }
                });
            }
        }).start();
    }


    public void urlProccessTXID(final String urlContent, Boolean Flag) //content
    {
        jsonStrTXID = urlContent;

        if (Flag) {

            //https://www.geeksforgeeks.org/searching-for-character-and-substring-in-a-string/
            //new urlProccessTXIDBackGround().execute(urlContent);

            //Esquecer de chamar o preExecute causou um monte de problemas
            //pois ao tentar finalizar o progressBar o sistema quebrava
            //pois esta não havia sido inicializada
            new urlProccessTXIDBackGround().onPreExecute();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    final String resultIn;

                    //Looper.prepare();
                    resultIn = new urlProccessTXIDBackGround().execute(urlContent);;
                    //Looper.loop();

                    //Para a execução na thread pricipal
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // do onPostExecute stuff
                            new urlProccessTXIDBackGround().onPostExecute(resultIn);

                            //new urlProccessTXIDBackGround().onPostExecute(OPRETURNDATA);
                        }
                    });

                }
            }).start();
        }
        else{
            Toast.makeText(this, "Bad or no Conection 02", Toast.LENGTH_SHORT).show();
            txProcessed = false;
        }
    }

    String OPRETURNDATA = "";
    Boolean firstOR = true;

    class urlProccessTXIDBackGround //extends AsyncTask<String, String,String>
    {
        //@Override
        protected void onPreExecute() {
            //super.onPreExecute();
            //exibirProgress(true);
        }

        //@Override
        //protected String doInBackground(String... params) {
        protected String execute(String... params) {

            String jsonStrTXID = params[0];

            int firstIndiceOf;
            int nextIndex;

            //OPRETURNDATA = "006a0430303030";//evitar a quebra do App

            OPRETURNDATA = "X";
            //https://www.geeksforgeeks.org/searching-for-character-and-substring-in-a-string/

            //firstIndiceOf = jsonStrTXID.indexOf(searchStrOPRETURN);
            //firstIndiceOf = jsonStrTXID.indexOf("006a4c");

            //firstIndiceOf = jsonStrTXID.indexOf("006a4");
            firstIndiceOf = jsonStrTXID.indexOf("006a");


            if(firstIndiceOf < 0) {
                //OPRETURNDATA = "006a0430303030";
                //return "006a0430303030"; // para evitar a quebra do APP;
                return OPRETURNDATA;
            }


            nextIndex = jsonStrTXID.indexOf("<", firstIndiceOf);



            jsonStrTXID = jsonStrTXID.substring(firstIndiceOf, nextIndex);

            firstIndiceOf = jsonStrTXID.indexOf("006a");


            if(jsonStrTXID.length()>0) {
                OPRETURNDATA = jsonStrTXID;
                return OPRETURNDATA;
            }


/*

            //Finaliza o thread de tempo
            //timer.cancel();
            //timer.purge();

            if (firstIndiceOf == -1) {
                OPRETURNDATA = "X";
                // return OPRETURNDATA;
            }
            else if(url2)
            {
                //OPRETURNDATA = jsonStrTXID.substring(14, jsonStrTXID.length());

                if(firstOR) {
                    //NFT = 4e 46 54
                    for (int i = 0; i < jsonStrTXID.length(); i++) {
                        //if (jsonStrTXID.substring(i, i + 6).compareTo("4e4654") == 0) {
                        //if (jsonStrTXID.substring(i, i + 6).compareTo("006a4c") == 0) {


                        if (jsonStrTXID.substring(i, i + 5).compareTo("006a4") == 0) {


                            //OPRETURNDATA = jsonStrTXID.substring(i+2, jsonStrTXID.length());


                            if (jsonStrTXID.substring(i + 5, i + 6).compareTo("c") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 2, jsonStrTXID.length());


                            else if (jsonStrTXID.substring(i + 5, i + 6).compareTo("d") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 4, jsonStrTXID.length());


                            else if (jsonStrTXID.substring(i + 5, i + 6).compareTo("e") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 6, jsonStrTXID.length());
                            else if (jsonStrTXID.substring(i, i + 4).compareTo("006a") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 0, jsonStrTXID.length());


                            break;
                        }
                        else if (jsonStrTXID.substring(i, i + 4).compareTo("006a") == 0) {
                            OPRETURNDATA = jsonStrTXID.substring(i + 0, jsonStrTXID.length());
                            break;

                        }
                    }

                }
                else
                {
                    //NFT = 4e 46 54
                    for (int i = 0; i < jsonStrTXID.length(); i++) {
                        //if (jsonStrTXID.substring(i, i + 6).compareTo("4e4654") == 0){
                        //if (jsonStrTXID.substring(i, i + 6).compareTo("006a4c") == 0){
                        if (jsonStrTXID.substring(i, i + 5).compareTo("006a4") == 0) {


                            //Aqui deve ser feito a cosinderação de OP_RETURNs com tamanhos diferentes;

                            OPRETURNDATA = jsonStrTXID.substring(i + 6 + 2, jsonStrTXID.length());
                            //OPRETURNDATA = jsonStrTXID.substring(i + 6 + 2 + 1, jsonStrTXID.length());


                            if (jsonStrTXID.substring(i + 5, i + 6).compareTo("c") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 6 + 2, jsonStrTXID.length());

                            else if (jsonStrTXID.substring(i + 5, i + 6).compareTo("d") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 6 + 4, jsonStrTXID.length());

                            else if (jsonStrTXID.substring(i + 5, i + 6).compareTo("e") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 6 + 6, jsonStrTXID.length());

                            else if (jsonStrTXID.substring(i, i + 4).compareTo("006a") == 0)
                                OPRETURNDATA = jsonStrTXID.substring(i + 6 + 0, jsonStrTXID.length());

                            break;
                        }
                        else if (jsonStrTXID.substring(i, i + 4).compareTo("006a") == 0) {
                            OPRETURNDATA = jsonStrTXID.substring(i + 6 + 0, jsonStrTXID.length());
                            break;

                        }
                    }
                }
            }
            */
            return OPRETURNDATA;

        }

        //@Override
        protected void onPostExecute(String params) {
            //super.onPostExecute(params);
            //((EditText)findViewById(R.id.ET_TEXTOROR2)).setText(OPRETURNDATA);
            processOPRETURNresult();
        }
    }

    int nORTotal = 1;
    int nORCont = 0;
    String[] OPRETURNPKG = new String[111];

    private void processOPRETURNresult()
    {
        /*
        if(OPRETURNDATA.compareTo("X")==0)
            OPRETURNDATA = "006a0430303030"; // para evitar a quebra do APP

        if(OPRETURNDATA.length() > 0) {
            Toast.makeText(this, OPRETURNDATA, Toast.LENGTH_LONG).show();
            return;
        }

        */

        if(firstOR)
        {
            if(OPRETURNDATA.compareTo("X")==0) {

                Toast.makeText(this, "No Data!!!", Toast.LENGTH_LONG).show();

                //exibirProgress(false);

                /*
                ((TextView)findViewById(R.id.TV_TEXTROR2)).setText("NO GOOD DATA");
                ((EditText)findViewById(R.id.ET_TEXTOROR3)).setText(

                        "No Good Data to Display!!!"
                );

                */

                return;
            }

            firstOR = false;
            nORTotal = 1;
        }

        if(nORCont + 1 == nORTotal)
        {
            //Toast.makeText(NFTOPReturn.this, "Até Aqui  X2!!", Toast.LENGTH_LONG).show();
            //BarCont = BarCont + BarSize;

            OPRETURNPKG[nORTotal - 1] = OPRETURNDATA;
            OPRETURNDATA = OPRETURNPKG[0];

            for (int i=1; i < nORTotal; i++)
            {
                OPRETURNDATA = OPRETURNDATA.substring(0, OPRETURNDATA.length()-1) + OPRETURNPKG[i];
            }
            //exibirProgress(false);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    final String resultIn;
                    //Looper.prepare();
                    resultIn = new DecBackGround().execute(OPRETURNDATA);
                    //result = new urlProccessTXIDBackGround().execute(jsonStrTXID);;
                    //Looper.loop();

                    //Para a execução na thread pricipal
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // do onPostExecute stuff
                            new DecBackGround().onPostExecute(resultIn);
                        }
                    });
                }
            }).start();
        }
        else
        {
            //BarCont = BarCont + BarSize;
            //OPRETURNPKG[nORCont] = OPRETURNDATA;
            //newSearch();
        }

        //new DecBackGround().execute(OPRETURNDATA);
        //exibirProgress(false);
    }

    //class MinhaTask extends AsyncTask<Location,Void,Location> {
    class DecBackGround //extends AsyncTask<String, String,String>
    {

        //@Override
        protected void onPreExecute() {
            //super.onPreExecute();
            //exibirProgress(true);
        }

        //@Override
        //protected String doInBackground(String... params) {
        protected String execute(String... params) {

            Variables.progressBar = 0;
            //decriptografia(params[0]);

            //PDPUtils.byteToString(Arrays.copyOfRange(text, 65, (text.length - 32)));

            return PDPUtils.byteToString(decriptografia(SHA256G.HashStrToByte2(params[0])));
            //return PDPUtils.byteToString(null);
            //return null;

            //return Variables.LastRawDecriptData;
        }

        //@Override
        protected void onPostExecute(String params) {
            //super.onPostExecute(params);

            if(dec)
            {
                DecResult(params);
            }
            //exibirProgress(false);
        }
    }
    private Boolean dec = true;
    private Boolean decSuccess = false;
    private void DecResult(String result)
    {
        //Toast.makeText(NFTOPReturn.this, Variables.Test1, Toast.LENGTH_LONG).show();
        if(decSuccess) {

            decSuccess = false;
            chooseDataType(result);

        }
        else {
            //Snackbar.make(view, "Dados inválidos" + " NULL", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            //Toast.makeText(NFTOPReturn.this, "Dados inválidos", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Invalid NFT Data", Toast.LENGTH_LONG).show();
            //for (int 1 = 1000000)
            //sleepOver();
            this.finish();

        }
    }

    String NFTInfo = "";
    String NFTDescription;
    private int NFTType;


    private byte[] decriptografia(byte[] text)
    {
        NFTInfo = "";
        NFTDescription = "";
        decSuccess = true;
        //DecOnly = false;

        NFTType = 0;
        //Retirar NFT do DADO
        return Arrays.copyOfRange(text, 3, text.length);
    }


    private void chooseDataType (String text)
    {
        putText(text);
    }
    private void putText (String text)
    {
        Variables.LastRawDecriptData = text;
        //((TextView)findViewById(R.id.TV_TEXTROR2)).setText("BLOCKCHAIN DATA");
        //((EditText) findViewById(R.id.ET_TEXTOROR2)).setText(text);
        //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        addNewData (text);

    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////
    /////////////////////////////////////

    Boolean newTimer = false;

    //https://stackoverflow.com/questions/4783960/call-method-when-home-button-pressed
    @Override
    public void onPause(){
        super.onPause();
        Variables.activityPause = true;
        timer.cancel();
        timer.purge();

        newTimer = true;
    }



}