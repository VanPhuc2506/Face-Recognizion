package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

public class Client extends JFrame implements ActionListener {
    private Socket clients;
    private File selectedFile = new File("" );
    private File selectedFileobject = new File("" );
    private static CascadeClassifier faceDetector;
    private static VideoCapture capture;
    private static Mat grayFrame;
    private  BufferedImage bufferedImage;
    private JLabel cameraLabel;
    private ImageIcon imageIcon, objectimageIcon;
    private static String filechoose,fileobject;
    private BufferedReader in;
    private BufferedWriter out;
    private JFrame frame,mainframe,objectframe;
    private JPanel mainPanel, topPanel, leftPanel, rightPanel, centerPanel, bottomPanel, menuPanel, namePanel, mainframePanel,facePanel,objectPanel;
    private JLabel title, imageView,faceLabel,objectLabel;
    private JButton saveButton, detectButton, chooseButton, deleteButton,faceButton,objectButton, backButton;
    private static JTextField nametxt;
    // Khai báo các thành phần giao diện
    private JPanel objectmainPanel, objecttopPanel, objectleftPanel, objectrightPanel, objectcenterPanel, objectbottomPanel, objectmenuPanel;
    private JLabel objecttitle, objectimageView;
    private JButton objectdetectButton, objectchooseButton, objectdeleteButton, objectbackButton;
    private static boolean check = true, checkframe = false;
    private SecretKeySpec secretKey;
    private  PublicKey publicKey;
    private static final String ALGORITHM = "AES";
    private static final String KEY = "mySecretKey12345";
    // Tạo một DefaultListModel để chứa các mục trong JList
    DefaultListModel<String> listModel = new DefaultListModel<>();
    // Tạo một JList và đặt DefaultListModel làm model
    JList<String> list = new JList<>(listModel);
    public Client() {
        // Khởi tạo giao diện
        initComponentsmain();
        initComponents();
        InitComponentsObject();
        try {
            clients = new Socket("localhost", 6001);
            in = new BufferedReader(new InputStreamReader(clients.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clients.getOutputStream()));
            secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            out.write("Hello" + "\n");
            out.flush();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        //Load OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Client client = new Client();
        client.actionPerformed(null);
        new Thread(() -> {
            try {
                client.Result();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
    private void initComponentsmain(){
        mainframe = new JFrame("Ứng dụng nhận diện khuôn mặt và đối tươợng");
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainframe.setSize(800, 720);
        mainframe.setLocationRelativeTo(null);
        mainframe.setLayout(new BorderLayout());

        mainframePanel = new JPanel(new GridLayout(1,2));

        facePanel = new JPanel();
        facePanel.setPreferredSize(new Dimension(400,720));
        faceLabel = new JLabel();
        faceLabel.setIcon(new ImageIcon("face.jpg"));
        faceButton = new JButton("Face Recognized");
        faceButton.setPreferredSize(new Dimension(200, 50));
        faceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainframe.setVisible(false);
                objectframe.setVisible(false);
                frame.setVisible(true);
                checkframe= true;
                if (checkframe){
                    // Khởi tạo Haar Cascades classifier
                    faceDetector = new CascadeClassifier();
                    faceDetector.load("C:\\opencv\\build\\etc\\haarcascades\\haarcascade_frontalface_default.xml");
                    // Khởi tạo đối tượng VideoCapture để truy cập camera
                    capture = new VideoCapture(0);
                    // Kiểm tra xem camera đã được kết nối thành công hay chưa
                    if (!capture.isOpened()) {
                        System.err.println("Unable to open camera");
                        System.exit(-1);
                    }
                    // Khởi tạo đối tượng Mat để lưu trữ hình ảnh từ camera
                    grayFrame = new Mat();
                    // Khởi tạo mảng Mat faces
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(grayFrame, faces);
                    // Bắt đầu vòng lặp để hiển thị hình ảnh từ camera lên giao diện và nhận diện khuôn mặt
                    new Thread(() -> {
                        while (true) {
                            //System.out.println(check);
                            // Đọc hình ảnh từ camera
                            capture.read(grayFrame);
                            // Nhận diện khuôn mặt từ hình ảnh camera
                            detectFaces();
                            if (check){
                                //System.out.println(check);
                                // Hiển thị hình ảnh giao diện
                                showImage();
                            }
                        }
                    }).start();
                }
            }
        });
        facePanel.add(faceLabel);
        facePanel.add(faceButton);

        objectPanel = new JPanel();
        objectPanel.setPreferredSize(new Dimension(400,720));
        objectLabel = new JLabel();

        objectLabel.setIcon(new ImageIcon("object.jpg"));
        objectButton = new JButton("Object Recognized");
        objectButton.setPreferredSize(new Dimension(200, 50));
        objectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainframe.setVisible(false);
                frame.setVisible(false);
                objectframe.setVisible(true);
            }
        });
        objectPanel.add(objectLabel);
        objectPanel.add(objectButton);

        mainframePanel.add(facePanel);
        mainframePanel.add(objectPanel);
        // Thêm mainPanel vào frame và hiển thị frame
        mainframe.add(mainframePanel);
        mainframe.setVisible(true);
    }
    private void initComponents() {
        // Khởi tạo frame
        frame = new JFrame("Face Detection App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 720);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Tạo các panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        topPanel.setPreferredSize(new Dimension(0, 30));

        leftPanel = new JPanel();
        leftPanel.setLayout(null);
        //leftPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        rightPanel = new JPanel();
        rightPanel.setLayout(null);
        //rightPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        //centerPanel.setPreferredSize(new Dimension(720, 580));
//        centerPanel.setBackground(Color.BLACK);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(2,1));
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        bottomPanel.setPreferredSize(new Dimension(0, 120));

        // Tạo tiêu đề
        title = new JLabel("Face Detection App", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(title);
        // Tạo TextField name
        namePanel = new JPanel();
        namePanel.add(new JLabel("Name: "));
        nametxt = new JTextField(30);
        namePanel.add(nametxt);
        bottomPanel.add(namePanel);

        menuPanel = new JPanel();
        //menuPanel.setPreferredSize(new Dimension(0,100));
        menuPanel.setBounds(10,10,780,100);
        bottomPanel.add(menuPanel);

        // Tạo button và label cho chức năng lưu ảnh
        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(100, 30));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inp = saveFace();
                if (inp != null) {
                    System.out.println(inp);
                    encodeAES(inp);
                }
            }
        });
        menuPanel.add(saveButton);

        // Tạo button và label cho chức năng nhận diện khuôn mặt
        detectButton = new JButton("Detect");
        detectButton.setPreferredSize(new Dimension(100, 30));
        detectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inp = compareFace();
                if (inp != null) {
                    System.out.println(inp);
                    encodeAES(inp);
                }
            }
        });
        menuPanel.add(detectButton);

        // Tạo button và label cho chức năng chọn ảnh
        chooseButton = new JButton("Choose file");
        chooseButton.setPreferredSize(new Dimension(100, 30));
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
                fileChooser.setFileFilter(filter);
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    check = false;
                    selectedFile = fileChooser.getSelectedFile();
                    // Do something with the selected file
                    imageIcon = new ImageIcon(selectedFile.getAbsolutePath());

                    // Get the image from the ImageIcon
                    Image image = imageIcon.getImage();

                    // Resize the image to 480 x 480
                    Image resizedImage = image.getScaledInstance(300, 300, Image.SCALE_DEFAULT);

                    // Create a new ImageIcon with the resized image
                    imageIcon = new ImageIcon(resizedImage);

                    // Set the new ImageIcon to the imageView
                    imageView.setIcon(imageIcon);
                    deleteButton.setVisible(true);
                    String path = selectedFile.getAbsolutePath();
                    //path = path.replace(" ","\\");
                    filechoose = path;
                    selectedFile = new File("");
                    //System.out.println(filechoose);
                }
            }
        });
        menuPanel.add(chooseButton);

        // Tạo button và label cho chức năng xóa ảnh
        deleteButton = new JButton("Delete file");
        deleteButton.setPreferredSize(new Dimension(100, 30));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filechoose = null;
                imageIcon = new ImageIcon(filechoose);
                imageView.setIcon(imageIcon);
                check = true;
                deleteButton.setVisible(false);
            }
        });
        menuPanel.add(deleteButton);
        deleteButton.setVisible(false);

        // Tạo button back
        backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(100, 30));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectframe.setVisible(false);
                frame.setVisible(false);
                mainframe.setVisible(true);
                checkframe=false;
                capture.release();
                filechoose = null;
                imageIcon = new ImageIcon(filechoose);
                imageView.setIcon(imageIcon);
                check = true;
                nametxt.setText("");
            }
        });
        menuPanel.add(backButton);

        // Tạo label cho hình ảnh
        imageView = new JLabel();
        imageView.setBackground(Color.BLUE);
        centerPanel.add(imageView,BorderLayout.CENTER);
        imageView.setHorizontalAlignment(SwingConstants.CENTER);
        imageView.setVerticalAlignment(SwingConstants.CENTER);

        // Thêm các panel vào mainPanel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Thêm mainPanel vào frame và hiển thị frame
        frame.add(mainPanel);
        frame.setVisible(false);

    }
    private void InitComponentsObject(){
        // Khởi tạo frame
        objectframe = new JFrame("Object Detection App");
        objectframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        objectframe.setSize(800, 720);
        objectframe.setLocationRelativeTo(null);
        objectframe.setLayout(new BorderLayout());

        // Tạo các panel
        objectmainPanel = new JPanel();
        objectmainPanel.setLayout(new BorderLayout());
        objectmainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        objecttopPanel = new JPanel();
        objecttopPanel.setLayout(new BoxLayout(objecttopPanel, BoxLayout.PAGE_AXIS));
        objecttopPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        objecttopPanel.setPreferredSize(new Dimension(0, 30));

        objectleftPanel = new JPanel();
        objectleftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        objectrightPanel = new JPanel();
        objectrightPanel.setLayout(null);
        //rightPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        objectcenterPanel = new JPanel();
        objectcenterPanel.setLayout(new BorderLayout());
        objectcenterPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        //objectcenterPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        objectbottomPanel = new JPanel();
        objectbottomPanel.setLayout(null);
        objectbottomPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        objectbottomPanel.setPreferredSize(new Dimension(0, 120));

        objectmenuPanel = new JPanel();
        //menuPanel.setPreferredSize(new Dimension(0,100));
        objectmenuPanel.setBounds(10,10,780,100);
        objectbottomPanel.add(objectmenuPanel);

        // Tạo tiêu đề
        objecttitle = new JLabel("Object Detection App", JLabel.CENTER);
        objecttitle.setFont(new Font("Arial", Font.BOLD, 20));
        objecttopPanel.add(Box.createVerticalStrut(5));
        objecttopPanel.add(objecttitle);

        // Đặt list vào objectleftPanel (thay thế BorderLayout.CENTER của label)
        objectleftPanel.add(new JScrollPane(list), BorderLayout.CENTER);

        // Tạo button và label cho chức năng nhận diện khuôn mặt
        objectdetectButton = new JButton("Detect");
        objectdetectButton.setPreferredSize(new Dimension(100, 30));
        objectdetectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inp = Objectdetect();
                System.out.println(inp);
                encodeAES(inp);
            }
        });
        objectmenuPanel.add(objectdetectButton);

        // Tạo button và label cho chức năng chọn ảnh
        objectchooseButton = new JButton("Choose file");
        objectchooseButton.setPreferredSize(new Dimension(100, 30));
        objectchooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
                fileChooser.setFileFilter(filter);
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedFileobject = fileChooser.getSelectedFile();
                    // Do something with the selected file
                    objectimageIcon = new ImageIcon(selectedFileobject.getAbsolutePath());

                    // Get the image from the ImageIcon
                    Image image = objectimageIcon.getImage();

                    // Resize the image to 480 x 480
                    Image resizedImage = image.getScaledInstance(300, 300, Image.SCALE_DEFAULT);

                    // Create a new ImageIcon with the resized image
                    objectimageIcon = new ImageIcon(resizedImage);

                    // Set the new ImageIcon to the imageView
                    objectimageView.setIcon(objectimageIcon);
                    objectdeleteButton.setVisible(true);
                    String path = selectedFileobject.getAbsolutePath();
                    //path = path.replace(" ","\\");
                    fileobject = path;
                    System.out.println(fileobject);
                }
            }
        });
        objectmenuPanel.add(objectchooseButton);

        // Tạo button và label cho chức năng xóa ảnh
        objectdeleteButton = new JButton("Delete file");
        objectdeleteButton.setPreferredSize(new Dimension(100, 30));
        objectdeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileobject = null;
                objectimageIcon = new ImageIcon(fileobject);
                objectimageView.setIcon(objectimageIcon);
                objectdeleteButton.setVisible(false);
                listModel.removeAllElements();
            }
        });
        objectmenuPanel.add(objectdeleteButton);
        objectdeleteButton.setVisible(false);

        // Tạo button back
        objectbackButton = new JButton("Back");
        objectbackButton.setPreferredSize(new Dimension(100, 30));
        objectbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectframe.setVisible(false);
                frame.setVisible(false);
                mainframe.setVisible(true);
                fileobject = null;
                objectimageIcon = new ImageIcon(fileobject);
                objectimageView.setIcon(objectimageIcon);
                objectdeleteButton.setVisible(false);
                listModel.removeAllElements();
            }
        });
        objectmenuPanel.add(objectbackButton);

        // Tạo label cho hình ảnh
        objectimageView = new JLabel();
        objectcenterPanel.add(objectimageView,BorderLayout.CENTER);
        objectimageView.setHorizontalAlignment(SwingConstants.CENTER);
        objectimageView.setVerticalAlignment(SwingConstants.CENTER);

        // Thêm các panel vào mainPanel
        objectmainPanel.add(objecttopPanel, BorderLayout.NORTH);
        objectmainPanel.add(objectleftPanel, BorderLayout.WEST);
        objectmainPanel.add(objectrightPanel, BorderLayout.EAST);
        objectmainPanel.add(objectcenterPanel, BorderLayout.CENTER);
        objectmainPanel.add(objectbottomPanel, BorderLayout.SOUTH);

        // Thêm mainPanel vào frame và hiển thị frame
        objectframe.add(objectmainPanel);
        objectframe.setVisible(false);

    }

    private void detectFaces() {
        // Chuyển đổi hình ảnh sang đen trắng để tăng tốc độ xử lý
        Imgproc.cvtColor(grayFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Phát hiện khuôn mặt từ hình ảnh đen trắng sử dụng Haar Cascades classifier
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(grayFrame, faces);

        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray) {
            Imgproc.rectangle(grayFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
    }
    private void showImage() {
        // Chuyển đổi Mat thành BufferedImage để hiển thị lên giao diện
        bufferedImage = matToBufferedImage(grayFrame);

        // Cập nhật hình ảnh lên JLabel
        imageView.setIcon(new ImageIcon(bufferedImage));

        // Vẽ hình ảnh lên JFrame
        revalidate();
        repaint();
    }
    private static BufferedImage matToBufferedImage(Mat mat) {
        // Chuyển đổi Mat thành BufferedImage để hiển thị lên giao diện
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }
    public static boolean isValidName(String name) {
        // Biểu thức chính quy để so khớp tên hợp lệ
        String regex = "^[\\p{L} '-]+$";
        return name.matches(regex);
    }
    public static boolean isValidFilename(String filename) {
        // Kiểm tra xem tên file có dấu hay không
        if (!filename.matches("\\A\\p{ASCII}*\\z")) {
            return false;
        }
        return true;
    }

    public static String saveFace(){
        String message = null;
        String name = nametxt.getText();
        if(!isValidName(name)){
            JOptionPane.showMessageDialog(null,"Hãy nhập tên hợp lệ");
        } else {
            //System.out.println(filechoose);
            if (filechoose != null) {
                if (isValidFilename(filechoose)) {
                    //System.out.println(filechoose);
                    Mat image = Imgcodecs.imread(String.valueOf(Paths.get(filechoose)));
                    // Phát hiện khuôn mặt từ hình ảnh đen trắng sử dụng Haar Cascades classifier
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(image, faces);
                    Mat face = new Mat(image, faces.toArray()[0]);

                    // Chuyển đổi ma trận ảnh sang mảng byte
                    MatOfByte matOfByte = new MatOfByte();
                    Imgcodecs.imencode(".png", face, matOfByte);
                    byte[] imageBytes = matOfByte.toArray();

                    // Chuyển đổi mảng byte thành chuỗi String
                    String imageData = Base64.getEncoder().encodeToString(imageBytes);
                    message = "Save:" + name + ":" + imageData;
                }
                else {
                    JOptionPane.showMessageDialog(null,"File không hợp lệ! Hãy chọn file hoặc thư mục có tên không dấu!");
                    filechoose = null;
                    check = true;
                }
            } else {
                // Đọc frame từ camera
                capture.read(grayFrame);

                // Phát hiện khuôn mặt từ hình ảnh đen trắng sử dụng Haar Cascades classifier
                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(grayFrame, faces);
                if ( faces.toArray().length > 1 || faces.toArray().length < 1){
                    JOptionPane.showMessageDialog(null,"Chưa tìm thấy khuôn mặt để nhận diện");
                }
                else {
                    Mat face = new Mat(grayFrame, faces.toArray()[0]);

                    // Chuyển đổi ma trận ảnh sang mảng byte
                    MatOfByte matOfByte = new MatOfByte();
                    Imgcodecs.imencode(".png", face, matOfByte);
                    byte[] imageBytes = matOfByte.toArray();

                    // Chuyển đổi mảng byte thành chuỗi String
                    String facedata = Base64.getEncoder().encodeToString(imageBytes);
                    message = "Save:" + name + ":" + facedata;
                }
            }
        }
        //System.out.println(message);
        return message;
    }
    public static String compareFace(){
        String message = null;
        if (filechoose != null ) {
            if (isValidFilename(filechoose)) {
                String imageData = null;
                try {
                    // Đọc nội dung của tập tin ảnh dưới dạng một mảng byte
                    byte[] imageBytes = Files.readAllBytes(Paths.get(filechoose));

                    // Mã hóa mảng byte thành chuỗi String sử dụng Base64
                    imageData = Base64.getEncoder().encodeToString(imageBytes);

                    //System.out.println(imageData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                message = "Compare:" + imageData;
            } else {
                JOptionPane.showMessageDialog(null,"File không hợp lệ! Hãy chọn file hoặc thư mục có tên không dấu!");
                filechoose = null;
                check = true;
            }
        }
        else {
            // Đọc frame từ camera
            capture.read(grayFrame);

            // Phát hiện khuôn mặt từ hình ảnh đen trắng sử dụng Haar Cascades classifier
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(grayFrame, faces);
            if ( faces.toArray().length > 1 || faces.toArray().length < 1){
                JOptionPane.showMessageDialog(null,"Chưa tìm thấy khuôn mặt để nhận diện");
            }
            else {
                Mat face = new Mat(grayFrame, faces.toArray()[0]);
                // Chuyển đổi ma trận ảnh sang mảng byte
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".png", face, matOfByte);
                byte[] imageBytes = matOfByte.toArray();

                // Chuyển đổi mảng byte thành chuỗi String
                String imageData = Base64.getEncoder().encodeToString(imageBytes);
                message = "Compare:" + imageData;
            }
        }
        return message;
    }
    public static String Objectdetect(){
        String message = null;
        if (fileobject != null ) {
            if (isValidFilename(fileobject)) {
                String imageData = null;
                try {
                    // Đọc nội dung của tập tin ảnh dưới dạng một mảng byte
                    byte[] imageBytes = Files.readAllBytes(Paths.get(fileobject));

                    // Mã hóa mảng byte thành chuỗi String sử dụng Base64
                    imageData = Base64.getEncoder().encodeToString(imageBytes);

                    //System.out.println(imageData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                message = "Object:" + imageData;
            } else {
                JOptionPane.showMessageDialog(null,"File không hợp lệ! Hãy chọn file hoặc thư mục có tên không dấu!");
                fileobject = null;
            }
        }
        else {
            JOptionPane.showMessageDialog(null,"Chưa chọn hình ảnh! Hãy chọn lại.");
        }
        return message;
    }
    private void Result() throws IOException {
        while (publicKey==null){
            GetKey();
        }
        while(true) {
            String input = in.readLine();
            System.out.println("Nhận được: " + input);
            String data = decodeAES(input);
            String[] request = data.split(":");
            String request1 = request[0];
            if (request1.equals("Save")) {
                String resultboolen = request[1];
//            System.out.println(resultboolen);
                if (resultboolen.equals("True")) {
                    JOptionPane.showMessageDialog(null, "Lưu khuôn mặt thành công!");
                } else {
                    JOptionPane.showMessageDialog(null, "Lưu khuôn mặt không thành công! Hãy thử lại");
                }
            } else if (request1.equals("Compare")) {
                String resultboolen = request[1];
                if (resultboolen.equals("True")) {
                    String user = request[2];
                    String confiden = request[3];
                    String imageString = request[4];
                    // Chuyển đổi chuỗi Base64 thành ảnh
                    try {
                        byte[] decodedBytes = Base64.getDecoder().decode(imageString);
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
                        Image imageresize = image.getScaledInstance(250, 250, Image.SCALE_DEFAULT);
                        check = false;
                        imageView.setIcon(new ImageIcon(imageresize));
                        deleteButton.setVisible(true);
                        nametxt.setText(user);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    JOptionPane.showMessageDialog(null, "Kết quả nhận diện khuôn mặt là " + user + " với độ chính xác là: " + confiden);
                } else {
                    String user = request[2];
                    String confiden = request[3];
                    JOptionPane.showMessageDialog(null, "Không tìm thấy đối tượng chứa khuôn mặt! Khuôn mặt gần giống nhất được tìm thấy là " + user + " với độ chính xác là: " + confiden);
                }
            }
            else if (request1.equals("Object")){
                String result = data.substring(input.indexOf(":") + 8);
                // Xử lý response
                JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
                // Lấy dữ liệu từ mảng detections
                JsonArray detectionsArray = jsonObject.getAsJsonArray("detections");
                if (detectionsArray.size() < 1){
                    JOptionPane.showMessageDialog(null, "Không tìm thấy đối tượng nào trong hình");
                }
                else {
                    // Kiểm tra trường "image" có tồn tại và không null
                    if (jsonObject.has("image") && !jsonObject.get("image").isJsonNull()) {
                        // Lấy dữ liệu hình ảnh từ JSON
                        String imageBase64 = jsonObject.get("image").getAsString();
                        byte[] decodedBytes = Base64.getDecoder().decode(imageBase64);
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
                        Image imageresize = image.getScaledInstance(400, 400, Image.SCALE_DEFAULT);
                        objectimageView.setIcon(new ImageIcon(imageresize));
                        objectdeleteButton.setVisible(true);
                    }

                    for (int i = 0; i < detectionsArray.size(); i++) {
                        JsonObject detectionObj = detectionsArray.get(i).getAsJsonObject();
                        String objectName = detectionObj.get("name").getAsString();
                        double confidence = detectionObj.get("confidence").getAsDouble();

                        // Tạo chuỗi hiển thị tên và confidence
                        String displayText = "Object Name: " + objectName + ", Confidence: " + confidence;

                        // Thêm chuỗi vào DefaultListModel
                        listModel.addElement(displayText);
                    }
                }
            }
        }
    }
    private void GetKey() throws IOException {
        String input = in.readLine();
        System.out.println("Nhận được: " + input);
        String[] result = input.split(":");
        String resultt = result[0];
        if (resultt.equals("Hello")) {
            String publickeyString = result[1];
            try {
                // Chuyển đổi chuỗi Base64 thành mảng byte
                byte[] publicKeyBytes = Base64.getDecoder().decode(publickeyString);
                // Chuyển đổi mảng byte thành đối tượng PublicKey
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(keySpec);
                Cipher cipherr = Cipher.getInstance("RSA");
                cipherr.init(Cipher.ENCRYPT_MODE, publicKey);
                byte[] encryptedMessage = cipherr.doFinal(secretKey.getEncoded());
                String base = Base64.getEncoder().encodeToString(encryptedMessage);
                String mess = "Key:" + base + "\n";
                //System.out.println(mess);
                out.write(mess);
                out.flush();
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void encodeAES(String input){
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(input.getBytes());
            String mess = Base64.getEncoder().encodeToString(encryptedValue);
            String message = mess + "\n";
            out.write(message);
            out.flush();
            System.out.println(mess);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchPaddingException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalBlockSizeException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (BadPaddingException ex) {
            throw new RuntimeException(ex);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }
    private String decodeAES(String input){
        String data;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        detectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        /*closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clients.close();
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clients = new Socket("localhost", 6001);
                    in = new BufferedReader(new InputStreamReader(clients.getInputStream()));
                    out = new BufferedWriter(new OutputStreamWriter(clients.getOutputStream()));

                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });*/
    }
}