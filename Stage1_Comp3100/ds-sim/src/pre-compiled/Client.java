
import java.net.*;
import java.io.*;
import java.nio.charset.*;
import java.util.ArrayList;


public class Client {
    private static boolean initialLoop = true;
    private static int serverSelector = 0;

    public static void main(String[] args) {
        try{
           Socket s = new Socket("localhost", 50000);
       
         
           String lastServerMsg = ""; //Declaring variable
          
           writeMessage(s, "HELO\n");       //First call to connect with server
           readMessage(s);          //Viewing reply to HELO from server
           
           writeMessage(s, "AUTH" + System.getProperty("user.name")+ "\n"); //Providing udername for authentication
           lastServerMsg = readMessage(s);  //Viewing if sever replied OK to authentication
            

           ArrayList <String> servers = new ArrayList <>(); //constructor call to store servers
           String Higestcore_type = null;
 
           ArrayList <Integer> HighestCore_serverID = new ArrayList<>(); //contructor call to store server IDs of server with largest core
           int numOfCores = 0;

           
           writeMessage(s, "REDY\n");
           lastServerMsg =  readMessage(s);
           System.out.println(lastServerMsg);
           String lpr = lastServerMsg.substring(0, 4); 


           while (!lpr.equals("NONE")) { //more jobs have to be done
              
            
            String[] JOBNSplit = lastServerMsg.split("");
           
               if ((JOBNSplit[0].toUpperCase()).equals("JOBN")){ //checks if the received response from the server is a new job
                
                   writeMessage(s, "GETS Capable" + JOBNSplit[4] + "" + JOBNSplit[5] + "" + JOBNSplit[6] + "\n"); //request for servers capable of running jobs for the provided data
                   lastServerMsg = readMessage(s); 
                   System.out.println(lastServerMsg);
                   writeMessage(s, "OK\n");

                   String[] grabber = lastServerMsg.split(" "); //picking the nRecs from the sent message
                   int nRecs = Integer.parseInt(grabber[1]);
                   writeMessage(s, "OK\n"); //responding with OK

                   
                   for (int i =0; i<nRecs; i++){ //Loop to go through the number of records found
                    lastServerMsg = readMessage(s);
                    System.out.println(lastServerMsg);
                    if (initialLoop ){
                        servers.add(lastServerMsg);
                        String[] breaker = lastServerMsg.split(" "); // Finding number of cores and id from server data
                        int core = Integer.parseInt(breaker[4]);
                        int id = Integer.parseInt(breaker[1]);
            

                        if (numOfCores < core){ //the highest core number found gets assiged to numberOfCores
                            numOfCores = core;
                            Higestcore_type = breaker[0];
                            HighestCore_serverID.clear();
                            HighestCore_serverID.add(id); //keeps record of the serverid of the lergest server
                        }

                        else if (Higestcore_type.equals(breaker[0])){
                            HighestCore_serverID.add(id);

                        }

                    }

               }
                   writeMessage(s, "OK\n");
                   lastServerMsg = readMessage(s);
                   System.out.println(lastServerMsg);

                   
                   if (serverSelector < HighestCore_serverID.size()){ //keeps scheduling jobs untill largest server type exhausts
                
                    writeMessage(s, "SCHD" + JOBNSplit [2] + " " + Higestcore_type  + " " + HighestCore_serverID.get(serverSelector)+ "\n");
                    lastServerMsg = readMessage(s);
                    System.out.println(lastServerMsg);
                    serverSelector++;
 
                }

                    if (serverSelector > (HighestCore_serverID.size()) - 1) { //after exhaustion of largest server type selector gets set to 0
                    serverSelector = 0;
                }
                
            }
            writeMessage(s, "REDY\n"); 
            lastServerMsg = readMessage(s);
            lpr = lastServerMsg.substring(0, 4);
            initialLoop = false;
        }



            writeMessage(s, "QUIT\n");
            s.close(); //closing socket
        }catch (IOException e) {
            e.printStackTrace();
        }
        }

    //funtion to write messages to server
    public static synchronized void writeMessage (Socket s, String lastServerMsg){
       try{
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        byte [] bytArr = lastServerMsg.getBytes();
        dout.write(bytArr);
        dout.flush();
       }catch (IOException e) {
        e.printStackTrace();
    }
    }


    //function to read messages from server
    public static synchronized String readMessage (Socket s){
        String lastServerMsg = "FAIL";

        try {
        DataInputStream din = new DataInputStream(s.getInputStream());
        byte[] bytArr = new byte[din.available()];

        bytArr = new byte[0];
        while (bytArr.length == 0 ){
            bytArr = new byte[din.available()];
            din.read(bytArr);
            lastServerMsg = new String (bytArr, StandardCharsets.UTF_8);
        }
    }catch (IOException e) {
        e.printStackTrace();
    }
    return lastServerMsg;
}
}