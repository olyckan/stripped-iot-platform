package tcpkandidat2;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
/**
 *
 * @author Fredrik Eriksson
 */
public class POSTvalue {
    public static void main(String [] argv) throws Exception {
        double n = 1;
        double totDeltaTime = 0;
        while(true){
            long startTime = System.nanoTime();
            Socket socket = new Socket("127.0.0.1", 4020);
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            OutputStream os = socket.getOutputStream();
            //String payload = URLEncoder.encode("key1", "UTF-8") + "=" + URLEncoder.encode("value1", "UTF-8");

            String path = "/servlet";

            String httppost = ("POST " + "/ HTTP/1.1\n" +
            "Host: " + socket.getOutputStream() +
            "Content-Type: text/plain; charset=UTF-8\n" + 
            "Content-Length: ");

            double randValue = ThreadLocalRandom.current().nextDouble(28, 31);
            int randType = ThreadLocalRandom.current().nextInt(1, 20);
            Date dNow = new Date();
            SimpleDateFormat ft = 
                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String payload = "location=Sundsvall&typeid="+randType+"&value="+randValue+"&time="+ft.format(dNow);

            String finalpost = httppost + payload.length() + "\n\n" + payload;
            os.write(finalpost.getBytes());

            //System.out.println(finalpost);

            os.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;

            while((line = rd.readLine()) != null){
                //System.out.println(line);
            }
            long stopTime = System.nanoTime();
            long deltaTime = stopTime - startTime;
            double deltaTimeInMs = deltaTime/(double)1000000;
            //totDeltaTime = totDeltaTime + deltaTimeInMs;
            //double meanDeltaTime = totDeltaTime/(double)n;
            System.out.println("Time to handle REST call: " + deltaTimeInMs + " ms");
            os.close();
            rd.close();
            n++;
            Thread.sleep(200);
        }
    }  
}
