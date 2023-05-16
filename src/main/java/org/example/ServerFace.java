package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerFace {
    private ServerSocket server;
    private ExecutorService executorService;

    public ServerFace(int port) throws IOException {
        server = new ServerSocket(port);
        executorService = Executors.newFixedThreadPool(10);
        System.out.println("Khởi tạo server thành công.");
    }

    private void start() {
        while (true) {
            try {
                Socket clientSocket = server.accept();
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " đã kết nối.");
                // Tạo một luồng xử lý riêng cho khách hàng bằng ExecutorService
                executorService.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() throws IOException {
        executorService.shutdown();
        server.close();
    }

    public static void main(String[] args) throws IOException {
        ServerFace myServer = new ServerFace(6001);
        myServer.start();
    }

    /**
     * Function nhận dữ liệu từ client -> gọi processData để xử lý. Vì dữ liệu từ server gửi về client nhiều dòng, nên từ khóa -End- được gửi vào cuối stream để báo hiệu client biết dữ liệu đã gửi hết.
     */
    class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader inputStream;
        private BufferedWriter outputStream;
        PrivateKey privateKey;
        PublicKey publicKey;
        SecretKey secretKey;

        public ClientHandler(Socket socket) {
            try {
                this.clientSocket = socket;
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
                inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void GetKey() throws IOException {
            String input = inputStream.readLine();
            System.out.println("Server nhận được: " + input);
            String[] req = input.split(":");
            String req1 = req[0];
            if (req1.equals("Hello")) {
                // Chuyển đổi public key thành mảng byte
                byte[] publicKeybytes = publicKey.getEncoded();// Chuyển đổi mảng byte thành chuỗi Base64
                String publicKeystring = Base64.getEncoder().encodeToString(publicKeybytes);
                String mess = "Hello:" + publicKeystring + "\n";
                outputStream.write(mess);
                outputStream.flush();
            } else if (req1.equals("Key")) {
                String in = req[1];
                //System.out.println(in);
                try {
                    byte[] messbyte = Base64.getDecoder().decode(in);
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    byte[] decryptedMessage = cipher.doFinal(messbyte);
                    secretKey = new SecretKeySpec(decryptedMessage, "AES");
                } catch (NoSuchPaddingException e) {
                    throw new RuntimeException(e);
                } catch (IllegalBlockSizeException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (BadPaddingException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        private void encodeAES(String input){
            try {
                Cipher cipherr = Cipher.getInstance("AES");
                cipherr.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encryptedValue = cipherr.doFinal(input.getBytes());
                String out = Base64.getEncoder().encodeToString(encryptedValue);
                String mess = out + "\n";
                outputStream.write(mess);
                outputStream.flush();
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private String decodeAES(String input){
            String data;
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decryptedValue = cipher.doFinal(Base64.getDecoder().decode(input));
                data = new String(decryptedValue);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            return data;
        }
        private boolean Save(String input) throws UnsupportedEncodingException {
            // Cắt lấy tên người dùng
            String[] req = input.split(":"); // cắt chuỗi dựa trên dấu chấm, kết quả trả về là một mảng các chuỗi
            String name = req[0];
            // Tạo thư mục để lưu hình ảnh khuôn mặt
            File directory = new File("faces/" + name);
            if (!directory.exists()) {
                directory.mkdir();
                System.out.println("Đã tạo " + directory);
            }
            // Giai ma chuoi String thanh mot mang byte
            String data = req[1];
            byte[] imagedata = Base64.getDecoder().decode(data);
            String facetoken = faceToken(imagedata);
            String namefile ="faces/" + name + "/" + facetoken + ".png" ;
            try{
                // Lưu ảnh với tên là facetoken
                FileOutputStream fileOutputStream = new FileOutputStream(namefile);
                fileOutputStream.write(imagedata);
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            boolean faceUser = facesetUserID(facetoken,name);
            if (faceUser){
                boolean faceAdd = facesetAdd(facetoken);
                if (faceAdd){
                    return true;
                }
                else return false;
            }else return false;
        }
        private String Compare(String input){
            String message = null;
            byte[] imagedata = Base64.getDecoder().decode(input);
            String facetoken = faceToken(imagedata);
            HashMap<String, String> result = new HashMap<>();
            result = faceSearch(facetoken);
            String confiden = result.get("confiden");
            if (Float.parseFloat(confiden) > 80){
                String face_token = result.get("facetoken");
                String user_id = result.get("user");
                // Chuyển đổi ảnh sang chuỗi Base64
                String imageString =  null;
                try {
                    String namefile = "faces/" + user_id + "/" + face_token + ".png";
                    File file = new File(namefile);
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    imageString = Base64.getEncoder().encodeToString(fileContent);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                message = "Compare:True:" + user_id + ":" + confiden + ":" + imageString;
            }
            else {
                String user_id = result.get("user");
                message = "Compare:False:" + user_id + ":" + confiden;
            }
            System.out.println(message);
            return message;
        }
        private String Object(String object) throws IOException {
            // Tạo JSON payload để gửi đến Flask API
            String jsonPayload = String.format("{\"image\": \"%s\"}", object);

            // Tạo request
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonPayload);
            Request request = new Request.Builder()
                    .url("http://localhost:5000/detect")
                    .post(requestBody)
                    .build();

            // Gửi request và nhận response
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            // Xử lý response
            JsonObject jsonObject = new Gson().fromJson(responseData, JsonObject.class);
            String mess = "Object:" + jsonObject;
            //System.out.println(mess);
            return mess;
        }
        public String faceToken(File file){
            String faceToken = null;
            byte[] buff = getBytesFromFile(file);
            String url = "https://api-us.faceplusplus.com/facepp/v3/detect";
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, byte[]> byteMap = new HashMap<>();
            map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
            map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
            byteMap.put("image_file", buff);
            try{
                byte[] bacd = post(url, map, byteMap);
                String str = new String(bacd);
                // Sử dụng Gson để chuyển đổi chuỗi JSON thành đối tượng JsonObject
                JsonObject jsonObject = new Gson().fromJson(str, JsonObject.class);

                // Lấy các thuộc tính
                JsonArray requestfaces = jsonObject.getAsJsonArray("faces");
                String facejson = String.valueOf(requestfaces.get(0));
                // Sử dụng Gson để chuyển đổi chuỗi JSON thành đối tượng JsonObject
                JsonObject jsonface = new Gson().fromJson(facejson, JsonObject.class);
                // Lấy giá trị face_token
                faceToken = jsonface.get("face_token").getAsString();
                System.out.println(faceToken);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return faceToken;
        }
        public String faceToken(byte[] bytes){
            String faceToken = null;
            byte[] buff = bytes;
            String url = "https://api-us.faceplusplus.com/facepp/v3/detect";
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, byte[]> byteMap = new HashMap<>();
            map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
            map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
            byteMap.put("image_file", buff);
            try{
                byte[] bacd = post(url, map, byteMap);
                String str = new String(bacd);
                // Sử dụng Gson để chuyển đổi chuỗi JSON thành đối tượng JsonObject
                JsonObject jsonObject = new Gson().fromJson(str, JsonObject.class);

                // Lấy các thuộc tính
                JsonArray requestfaces = jsonObject.getAsJsonArray("faces");
                String facejson = String.valueOf(requestfaces.get(0));
                // Sử dụng Gson để chuyển đổi chuỗi JSON thành đối tượng JsonObject
                JsonObject jsonface = new Gson().fromJson(facejson, JsonObject.class);
                // Lấy giá trị face_token
                faceToken = jsonface.get("face_token").getAsString();
                System.out.println(faceToken);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return faceToken;
        }
        public boolean facesetAdd(String facetoken){
            String url = "https://api-us.faceplusplus.com/facepp/v3/faceset/addface";
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, byte[]> byteMap = new HashMap<>();
            map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
            map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
            map.put("faceset_token", "3a8b95b6ea781be3b80d9070c4b1d989"); // Faceset ID
            map.put("face_tokens",facetoken);
            //byteMap.put("image_file", buff);
            try{
                byte[] bacd = post(url, map, byteMap);
                String str = new String(bacd);
                System.out.println(str);
                JSONObject responseJson = new JSONObject(str); // responseString là chuỗi JSON trả về từ API
                if (responseJson.has("error_message")) {
                    return false;
                } else {
                    return true;
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        public boolean facesetUserID(String facetoken, String username) throws UnsupportedEncodingException {
            String name = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
            String url = "https://api-us.faceplusplus.com/facepp/v3/face/setuserid";
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, byte[]> byteMap = new HashMap<>();
            map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
            map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
            map.put("face_token",facetoken);
            map.put("user_id",name);
            //byteMap.put("image_file", buff);
            try{
                byte[] bacd = post(url, map, byteMap);
                String str = new String(bacd);
                System.out.println(str);
                JSONObject responseJson = new JSONObject(str); // responseString là chuỗi JSON trả về từ API
                if (responseJson.has("error_message")) {
                    return false;
                } else {
                    return true;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        public HashMap<String, String> faceSearch(String facetoken){
            HashMap<String, String> result = new HashMap<>();
            String url = "https://api-us.faceplusplus.com/facepp/v3/search";
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, byte[]> byteMap = new HashMap<>();
            map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
            map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
            map.put("face_token",facetoken);
            map.put("faceset_token", "3a8b95b6ea781be3b80d9070c4b1d989"); // Faceset ID
            try{
                byte[] bacd = post(url, map, byteMap);
                String str = new String(bacd);
                // Sử dụng Gson để chuyển đổi chuỗi JSON thành đối tượng JsonObject
                JsonObject jsonObject = new Gson().fromJson(str, JsonObject.class);
                // Lấy các thuộc tính
                JsonArray requestfaces = jsonObject.getAsJsonArray("results");
                String facejson = String.valueOf(requestfaces.get(0));
                // Sử dụng Gson để chuyển đổi chuỗi JSON thành đối tượng JsonObject
                JsonObject jsonface = new Gson().fromJson(facejson, JsonObject.class);
                // Lấy giá trị face_token
                String face_token = jsonface.get("face_token").getAsString();
                String confidence = jsonface.get("confidence").getAsString();
                String user_id = URLDecoder.decode(jsonface.get("user_id").getAsString(), "UTF-8");
                System.out.println(str);
                result.put("confiden",confidence);
                result.put("user",user_id);
                result.put("facetoken",face_token);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        public void run() {
            try {
                inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                // Thực hiện xử lý yêu cầu từ khách hàng
                // ... (Thêm mã xử lý của bạn ở đây)
                while (secretKey==null){
                    GetKey();
                }
                while (true) {
                    String input = inputStream.readLine();
                    System.out.println("Server nhận được: " + input);
                    String data = decodeAES(input);
                    System.out.println(data);
                    String[] request = data.split(":");
                    String request1 = request[0];
                    if (request1.equals("Save")) {
                        String in = request[1] + ":" + request[2];
                        System.out.println(in);
                        boolean save = Save(in);
                        if (save) {
                            String output = "Save:True";
                            encodeAES(output);
                        } else {
                            String output = "Save:False";
                            encodeAES(output);
                        }
                    } else if (request1.equals("Compare")) {
                        String in = request[1];
                        System.out.println(in);
                        String output = Compare(in);
                        System.out.println(output);
                        encodeAES(output);
                    } else if (request1.equals("Object")) {
                        String in  = request[1];
                        System.out.println(in);
                        String output = Object(in);
                        System.out.println(output);
                        encodeAES(output);
                    } else if (input.equals("bye")) {
                        System.out.println("Server đã đóng");
                        outputStream.write("Server đã đóng\n-End-\n");
                        outputStream.flush();
                        break;
                    }
                }

                inputStream.close();
                outputStream.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //API
    private final static int CONNECT_TIME_OUT = 30000;
    private final static int READ_OUT_TIME = 50000;
    private static String boundaryString = getBoundary();
    protected static byte[] post(String url, HashMap<String, String> map, HashMap<String, byte[]> fileMap) throws Exception {
        HttpURLConnection conne;
        URL url1 = new URL(url);
        conne = (HttpURLConnection) url1.openConnection();
        conne.setDoOutput(true);
        conne.setUseCaches(false);
        conne.setRequestMethod("POST");
        conne.setConnectTimeout(CONNECT_TIME_OUT);
        conne.setReadTimeout(READ_OUT_TIME);
        conne.setRequestProperty("accept", "*/*");
        conne.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
        conne.setRequestProperty("connection", "Keep-Alive");
        conne.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
        DataOutputStream obos = new DataOutputStream(conne.getOutputStream());
        Iterator iter = map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String, String> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            obos.writeBytes("--" + boundaryString + "\r\n");
            obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                    + "\"\r\n");
            obos.writeBytes("\r\n");
            obos.writeBytes(value + "\r\n");
        }
        if(fileMap != null && fileMap.size() > 0){
            Iterator fileIter = fileMap.entrySet().iterator();
            while(fileIter.hasNext()){
                Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                obos.writeBytes("--" + boundaryString + "\r\n");
                obos.writeBytes("Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                        + "\"; filename=\"" + encode(" ") + "\"\r\n");
                obos.writeBytes("\r\n");
                obos.write(fileEntry.getValue());
                obos.writeBytes("\r\n");
            }
        }
        obos.writeBytes("--" + boundaryString + "--" + "\r\n");
        obos.writeBytes("\r\n");
        obos.flush();
        obos.close();
        InputStream ins = null;
        int code = conne.getResponseCode();
        try{
            if(code == 200){
                ins = conne.getInputStream();
            }else{
                ins = conne.getErrorStream();
            }
        }catch (SSLException e){
            e.printStackTrace();
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int len;
        while((len = ins.read(buff)) != -1){
            baos.write(buff, 0, len);
        }
        byte[] bytes = baos.toByteArray();
        ins.close();
        return bytes;
    }
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }
    private static String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }

    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
}