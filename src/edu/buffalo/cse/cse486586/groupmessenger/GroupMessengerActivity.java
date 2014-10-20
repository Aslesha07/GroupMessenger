package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

	
	static final String TAG = GroupMessengerActivity.class.getSimpleName();
	static final int SERVER_PORT = 10000;
	public static String[] REMOTE_PORTS={"11108","11112","11116","11120", "11124"};
	final Uri myUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
	public static int counter=0;
	static final String seq_port="11112";
	public static String myPort=null;
	public static int seq_number=0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);
        findViewById(R.id.button4).setOnClickListener(new OnClickListener(){
        	public void onClick(View tv)
        	{
        		String msg = editText.getText().toString()+"\n";
                editText.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
        	}
        }); 
        try{
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
       
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            } catch (IOException e) {
        	Log.e(TAG, "Can't create a ServerSocket");
            return;
            }
    }
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
 private class ClientTask extends AsyncTask<String, Void, Void> {
    	
        @Override
        protected Void doInBackground(String... msgs) {
           //msgs[1] is my own port
        	//msgs[0] is the msg to send
        	 Log.v("inside", "serverTask");	
                String msgToSend = msgs[0];
                
                
                
                if(!msgToSend.startsWith("###")) 
                {	
                	String msg=msgToSend;
                	Socket socket=null;
    				try {
    				socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
    					        Integer.parseInt(seq_port));
                    PrintWriter printwriter = new PrintWriter(socket.getOutputStream(),true);
                    printwriter.write(msg);  //write the message to output stream
                    printwriter.flush();
                    printwriter.close(); //close the output stream
                    socket.close(); //close connection
                } catch (Exception e) {
                    Log.e(TAG, "ClientTask Exception"+e);
                }
                }
                else if((msgs[1].equals(seq_port))&&(msgToSend.startsWith("###"))){ //sender is sequencer
                	String msg=msgToSend;
                	 
                	for(String remotePort:REMOTE_PORTS){
                     Log.v("CLIENT TASK", "Port "+myPort+" sending message"+msgToSend+" to "+remotePort);
                		                Socket socket=null;
                						try {
                						socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                							        Integer.parseInt(remotePort));
                		                PrintWriter printwriter = new PrintWriter(socket.getOutputStream(),true);
                		                printwriter.write(msg);  //write the message to output stream
                		                printwriter.flush();
                		                printwriter.close(); //close the output stream
                		                socket.close(); //close connection
                		            } catch (Exception e) {
                		                Log.e(TAG, "ClientTask Exception"+e);
                		            }
                }
                }
              return null; 
            }  
          
        }
 
 private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
 	
     @Override
     protected Void doInBackground(ServerSocket... sockets) {	
         ServerSocket serverSocket = sockets[0];
         
         /*
          * TODO: Fill in your server code that receives messages and passes them
          * to onProgressUpdate().
          */
         Log.v("inside", "serverTask");
         Socket clientSocket;
         InputStreamReader inputStreamReader;
         BufferedReader bufferedReader;
         String msg=null;
         
         while (true) {
             try {
             	
                 clientSocket = serverSocket.accept();   //accept the client connection
                 inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                 bufferedReader = new BufferedReader(inputStreamReader); //get the client message
                 msg = bufferedReader.readLine();
                 inputStreamReader.close(); 
                 clientSocket.close();
             }catch(Exception e){
             Log.e("SERVERTASK error", e.toString());	
             }
            
         if(myPort.equals(seq_port))
         {	
         	     
                   if(!msg.startsWith("###")){
                 	  
                 	  msg="###"+seq_number+"###"+msg;
                 	  seq_number++;
                      new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort); 
                   }
                   else if(msg.startsWith("###")){
                 	  publishProgress(msg);
                 	  }
                   	
         	}
         else{ //for non sequencer ports
                     if(msg.startsWith("###")){
                     	publishProgress(msg);
                   	  }
                      
           	}
         }
    
     }
     protected void onProgressUpdate(String...strings) {
         /*
          * The following code displays what is received in doInBackground().
          */
         String strReceived = strings[0].trim();
         String msg= strReceived;
         String[] x=msg.split("###");
         String m=x[2];
         String n=x[1];
         TextView remoteTextView = (TextView) findViewById(R.id.textView1);
         remoteTextView.append(strReceived + "\t\n");
         
         
         
         ContentValues keyValueToInsert = new ContentValues();
         keyValueToInsert.put("key", n);
         keyValueToInsert.put("value", m);
         
         Uri newUri = getContentResolver().insert(
         myUri,    // assume we already created a Uri object with our provider URI
         keyValueToInsert
         		);
         
         return;
     }
    
 }
    
}
