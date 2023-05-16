package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.SSLException;
public class FaceSet {

    public static void main(String[] args) throws Exception{

//        File file = new File("D:\\Face\\Putin\\Putin_1681885481439.png");
//        String facetoken = faceToken(file);
        //facesetUserID(facetoken,"Putin");
        //facesetAdd(facetoken);
        //facesetAdd(file);
//        String kq = faceSearch(facetoken);
//        System.out.println(kq);
//        faceget(kq);
        faceset();
    }
    public static void faceset(){
        String url = "https://api-us.faceplusplus.com/facepp/v3/faceset/removeface";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
        map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
        map.put("faceset_token","3a8b95b6ea781be3b80d9070c4b1d989");
        //map.put("face_tokens","d20ad5be9b1896c88c3d16d73aa0291c,fa86b07d119051dde78e2986ecd861a0,3632fc2a2a6f8eb7c0d6a587bd3024b3");
        //byteMap.put("image_file", buff);
        map.put("face_tokens","ca92bd9ea079a80fc0df50753f8a05b4");
        try{
            byte[] bacd = post(url, map, byteMap);
            String str = new String(bacd);
            System.out.println(str);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void faceget(String facetoken){
        String url = "https://api-us.faceplusplus.com/facepp/v3/face/getdetail";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
        map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
        map.put("face_token",facetoken);
        //byteMap.put("image_file", buff);
        try{
            byte[] bacd = post(url, map, byteMap);
            String str = new String(bacd);
            System.out.println(str);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void facesetUserID(String facetoken, String username){
        String url = "https://api-us.faceplusplus.com/facepp/v3/face/setuserid";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "zqbeo6X0Yh8hfJ5Qbol8gbf6mkErINLN");
        map.put("api_secret", "r2vHwRQEX_NHQULsrbT0JT_QH2wJg5O4");
        map.put("face_token",facetoken);
        map.put("user_id",username);
        //byteMap.put("image_file", buff);
        try{
            byte[] bacd = post(url, map, byteMap);
            String str = new String(bacd);
            System.out.println(str);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void facesetAdd(String facetoken){
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
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String faceSearch(String facetoken){
        String faceToken = null;
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
            faceToken = jsonface.get("face_token").getAsString();
            String confiden = jsonface.get("confidence").getAsString();
            System.out.println(confiden);
            faceToken = str;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return faceToken;
    }

    public static String faceToken(File file){
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