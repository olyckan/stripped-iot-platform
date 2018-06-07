
package tcpkandidat2;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author Fredrik Eriksson
 */

public class ServerSideSocket implements Runnable {
    
    Socket msocket;
    ServerSideSocket(Socket msocket){
        this.msocket = msocket;
    }
    public static void main(String[] args) throws Exception {
        ServerSocket srv = new ServerSocket(4020);
        System.out.println("Listening on port 4020...");
        while(true){
            Socket sock = srv.accept();
            //System.out.println("connected");
            new Thread(new ServerSideSocket(sock)).start();
        }
    }
        
    public void run() {
        try {            
            long startTime = System.nanoTime();
            //System.out.println("Just connceted to " + msocket.getRemoteSocketAddress());
            OutputStream os = msocket.getOutputStream();
            PrintWriter toClient =
                    new PrintWriter(msocket.getOutputStream(), true);
            BufferedReader fromClient =
                    new BufferedReader(new InputStreamReader(msocket.getInputStream()));
            String line = fromClient.readLine();
            if (line.equalsIgnoreCase("GET / HTTP/1.1")){
                try{
                    
                    String httpStart =  "HTTP/1.1 200 OK\n" +
                                        "Content-Type: text/plain; charset=UTF-8\n" +
                                        "Connection: close\n" + 	
                                        "Content-Length: ";

                    //os.write(httpStart.getBytes());
                    //1. få en anslutning till databasen.
                    Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kandidat?autoReconnect=true&useSSL=false", "root", "1234");
                    //2. Göra ett statement
                    Statement myStmt = myConn.createStatement();
                    //3. Exekvera SQL query.
                    ResultSet myRs = myStmt.executeQuery("select * from sensor_value");
                    //4. process result set.

                    String httpData = "";
                    while (myRs.next()){
                        //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));
                        httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												
                    }
                    String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                    os.write(finalString.getBytes());
                    long stopTime = System.nanoTime();
                    long deltaTime = stopTime - startTime;
                    double deltaTimeInMs = deltaTime/(double)1000000;
                    System.out.println("Time to take all information from database: " + deltaTimeInMs + " ms");

                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            else if(line.startsWith("GET")){
                try{
                    String httpStart =  "HTTP/1.1 200 OK\n" +
                                        "Content-Type: text/plain; charset=UTF-8\n" +
                                        "Connection: close\n" + 	
                                        "Content-Length: ";

                    //os.write(httpStart.getBytes());

                    Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kandidat?autoReconnect=true&useSSL=false", "root", "1234");
                    Statement myStmt = myConn.createStatement();
                    //String[] splitLineList = line.split("\\s+|/|\\?|=");
                    String[] firstSplit =  line.split("/|\\s+");
                    int index = 0;
                    for(String retVal: firstSplit){
                        if(retVal.startsWith("Get") == true){
                            String[] secondSplit = retVal.split("\\?|\\&|=");
                            for(String parameterVal: secondSplit){

                                if (parameterVal.equals("GetValue")){
                                    if(retVal.indexOf('&') == -1 && retVal.contains("typeid") == true){

                                        String typeid = secondSplit[index+2];

                                        ResultSet myRs = myStmt.executeQuery("select * from sensor_value WHERE typeid=" + typeid);
                                        String httpData = "";
                                        while (myRs.next()){
                                            //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                            httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";	
                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());
                                        long stopTime = System.nanoTime();
                                        long deltaTime = stopTime - startTime;
                                        double deltaTimeInMs = deltaTime/(double)1000000;
                                        System.out.println("Time to take specific information from database: " + deltaTimeInMs + " ms");
                                    }
                                    else if(retVal.indexOf('&') >= 0 && retVal.contains("typeid") == true && retVal.contains("location") == true ){
                                        String typeid = secondSplit[index+2];
                                        String location = secondSplit[index+4];

                                        ResultSet myRs = myStmt.executeQuery("select * from sensor_value where typeid=" + typeid + " AND location=" + "'" + location + "'");
                                        String httpData = "";
                                        while (myRs.next()){
                                            //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                            httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";											

                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());
                                    }
                                    else if(retVal.indexOf('&') == -1 && retVal.contains("location") == true){
                                        String location = secondSplit[index+2];

                                        ResultSet myRs = myStmt.executeQuery("select * from sensor_value where location=" + "'" + location + "'");
                                        String httpData = "";
                                        while (myRs.next()){
                                            //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                            httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());
                                    }
                                }
                                else if(parameterVal.equals("GetRange")){
                                    if(retVal.indexOf('&') == -1 && retVal.contains("start_date") == true){

                                        //Ger all lagrad data från ett specifikt datum.

                                    String datetime = secondSplit[index+2];
                                    if(datetime.length()<= 12){
                                        String time = "00:00:00";

                                        ResultSet myRs = myStmt.executeQuery("select * from sensor_value where time>=" + "'" + datetime + " " + time + "'");
                                        String httpData = "";
                                        while (myRs.next()){
                                            //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                            httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());

                                    }
                                    else{
                                        String newdatetime = datetime.replace('T', ' ');
                                        String fulldate = newdatetime.replace('Z', ' ');

                                        ResultSet myRs = myStmt.executeQuery("select * from sensor_value where time>=" + "'" + fulldate + "'");
                                        String httpData = "";
                                        while (myRs.next()){
                                            //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                            httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());
                                        }
                                    }
                                    else if(retVal.indexOf('&') == -1 && retVal.contains("end_date") == true){

                                        //Ger all lagrad data till ett specifikt datum.

                                        String datetime = secondSplit[index+2];
                                        if(datetime.length()<= 12){
                                            String time = "00:00:00";

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where time<=" + "'" + datetime + " " + time + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());

                                        }
                                        else{
                                            String newdatetime = datetime.replace('T', ' ');
                                            String fulldate = newdatetime.replace('Z', ' ');

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where time<=" + "'" + fulldate + "'");
                                        String httpData = "";
                                        while (myRs.next()){
                                            //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                            httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                        }
                                        String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                        os.write(finalString.getBytes());
                                        }
                                    }
                                    else if(retVal.contains("start_date") == true && retVal.contains("end_date") == true && retVal.contains("typeid") == false && retVal.contains("location") == false){

                                        //Hämtar all information mellan ett start- och slutdatum.

                                        if(secondSplit[index+2].length() <= 12 && secondSplit[index+4].length() <=12){
                                            String startdate = secondSplit[index+2];
                                            String enddate = secondSplit[index+4];
                                            String time = "00:00:00";

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where time BETWEEN" + "'" + startdate + " " + time + "'" + "AND" + "'" + enddate + " " + time + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                            }
                                            String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                            os.write(finalString.getBytes());
                                        }
                                        else{

                                            String startdate = secondSplit[index+2];
                                            String enddate = secondSplit[index+4];

                                            String newstartdate = startdate.replace('T', ' ');
                                            String finalstartdate = newstartdate.replace('Z' , ' ');

                                            String newenddate = enddate.replace('T', ' ');
                                            String finalenddate = newenddate.replace('Z', ' ');

                                            ResultSet myRs = myStmt.executeQuery("select * from sesnor_value where time BETWEEN" + "'" + finalstartdate + "'" + "AND" + "'" + finalenddate + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												
                                            }
                                            String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                            os.write(finalString.getBytes());
                                        }
                                    }
                                    else if(retVal.contains("typeid") == true && retVal.contains("start_date") == true && retVal.contains("end_date") == true && retVal.contains("location") == false){
                                        String typeid = secondSplit[index+2];

                                        if(secondSplit[index+4].length() <= 12 && secondSplit[index+6].length() <= 12){
                                            String startdate = secondSplit[index+4];
                                            String enddate = secondSplit[index+6];
                                            String time = "00:00:00";

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where tempid=" + typeid + " AND time BETWEEN" + "'" + startdate + " " + time + "'" + "AND" + "'" + enddate + " " + time + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                            }
                                            String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                            os.write(finalString.getBytes());
                                        }
                                        else{
                                            String startdate = secondSplit[index+4];
                                            String enddate = secondSplit[index+6];

                                            String newstartdate = startdate.replace('T', ' ');
                                            String finalstartdate = newstartdate.replace('Z' , ' ');

                                            String newenddate = enddate.replace('T', ' ');
                                            String finalenddate = newenddate.replace('Z', ' ');

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where tempid =" + typeid + " AND time BETWEEN" + "'" + finalstartdate + "'" + "AND" + "'" + finalenddate + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                            }
                                            String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                            os.write(finalString.getBytes());  
                                        }
                                    }
                                    else if(retVal.contains("typeid") == true && retVal.contains("start_date") == true && retVal.contains("end_date") == true && retVal.contains("location") == true){

                                        //Hämtar information från databasen baserat på typ, plats samt start- och slutdatum.

                                        String typeid = secondSplit[index+2];
                                        String location = secondSplit[index+4];

                                        if(secondSplit[index+6].length() <= 12 && secondSplit[index+8].length() <= 12){
                                             String startdate = secondSplit[index+6];
                                            String enddate = secondSplit[index+8];
                                            String time = "00:00:00";

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where typeid=" + typeid + " AND location=" + "\"" + location + "\"" + " AND time BETWEEN" + "'" + startdate + " " + time + "'" + "AND" + "'" + enddate + " " + time + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                            }
                                            String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                            os.write(finalString.getBytes());
                                        }
                                        else{
                                            String startdate = secondSplit[index+6];
                                            String enddate = secondSplit[index+8];

                                            String newstartdate = startdate.replace('T', ' ');
                                            String finalstartdate = newstartdate.replace('Z' , ' ');

                                            String newenddate = enddate.replace('T', ' ');
                                            String finalenddate = newenddate.replace('Z', ' ');

                                            ResultSet myRs = myStmt.executeQuery("select * from sensor_value where typeid =" + typeid + " AND location=" + "\"" + location + "\"" + " AND time BETWEEN" + "'" + finalstartdate + "'" + "AND" + "'" + finalenddate + "'");
                                            String httpData = "";
                                            while (myRs.next()){
                                                //System.out.println(myRs.getString("tempid") + ", " + myRs.getString("location") + ", " + myRs.getString("temp") + ", " + myRs.getString("time"));

                                                httpData = httpData + myRs.getString("id") + ", " + myRs.getString("location") + ", " + myRs.getString("typeid") + ", " + myRs.getString("value") + ", " + myRs.getString("time") + "\n\n";												

                                            }
                                            String finalString = httpStart + httpData.length() + "\n\n" + httpData;
                                            os.write(finalString.getBytes());      
                                        }
                                    }
                                }
                            index++;    
                            }
                        }                         
                    }
                }
                catch (SQLException | IOException e){
                    e.printStackTrace();
                }
                       
            }
            
            else if(line.startsWith("POST")){
                try{
                    List<String> headers = new ArrayList();
                    String str;
                    while ((str = fromClient.readLine()) != null)
                    {
                       headers.add(str);
                       if (str.startsWith("Content-Length: "))
                       {
                          break; 
                       }
                    }
                    int contentLength = Integer.parseInt(headers
                                             .get(headers.size() - 1).substring(
                                                       "Content-Length: ".length()));

                    StringBuilder requestContent = new StringBuilder();
                    int ch;
                    for (int i = 0; i < contentLength; i++)
                    {
                        requestContent.append((char) fromClient.read());

                    }
                   //System.out.println(requestContent);
                   String stringRequestContent = requestContent.toString();
                   String[] postSplit = stringRequestContent.split("\\&|\\=");

                   String location = postSplit[1];
                   String typeid = postSplit[3];
                   String value = postSplit[5];
                   String date = postSplit[7];
                    //1. Skapar en uppkoppling till databasen.
                    Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kandidat?autoReconnect=true&useSSL=false", "root", "1234");

                    //2. Skapar ett statement.
                    Statement myStmt = myConn.createStatement();

                    String sql = "insert into sensor_value"
                             + " (location, typeid, value, time)"
                             + " values ( '" + location + "', '" + typeid + "', '" + value + "', '" + date +"')";

                    myStmt.executeUpdate(sql);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
               
            toClient.println("\nThank you for connecting to " + msocket.getLocalSocketAddress());            
            os.close();
            msocket.close();
            toClient.close();
        }
        
        
        catch (UnknownHostException ex){
            ex.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }        
    }
}   