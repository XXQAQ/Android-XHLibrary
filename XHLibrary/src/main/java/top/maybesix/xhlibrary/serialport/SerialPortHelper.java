package top.maybesix.xhlibrary.serialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import android_serialport_api.SerialPort;

/**
 * @author MaybeSix
 */
public class SerialPortHelper {

    private OnSerialPortReceivedListener onSerialPortReceivedListener;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ReadThread readThread;
    private String port;
    private int baudRate;
    private boolean openState = false;


    public SerialPortHelper(String port, int baudRate, OnSerialPortReceivedListener listener) {
        this.port = port;
        this.baudRate = baudRate;
        this.onSerialPortReceivedListener = listener;
    }


    public void setSerialPortReceivedListener(OnSerialPortReceivedListener onSerialPortReceivedListener) {
        this.onSerialPortReceivedListener = onSerialPortReceivedListener;
    }

    /**
     * 是否开启串口
     *
     * @return
     */
    public boolean isOpen() {
        return openState;
    }

    /**
     * 串口打开方法
     */
    public void open() throws Exception{

        baseOpen();

    }

    private void baseOpen() throws SecurityException, IOException, InvalidParameterException {
        serialPort = new SerialPort(new File(port), baudRate, 0);
        outputStream = serialPort.getOutputStream();
        inputStream = serialPort.getInputStream();
        readThread = new ReadThread();
        readThread.start();
        openState = true;
    }

    /**
     * 串口关闭方法
     */
    public void close() {
        if (readThread != null) {
            readThread.interrupt();
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        openState = false;
    }

    /**
     * 执行发送程序，若未开启，则会先开启，然后再发送
     *
     * @param bytes
     */
    public void send(byte[] bytes) throws Exception {
        if (openState) {
            outputStream.write(bytes);
        } else {
            open();
            outputStream.write(bytes);
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (inputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[2048];
                    int size = inputStream.read(buffer);
                    if (size > 0) {
                        ComPortData comPortData = new ComPortData(port, buffer, size);
                        onSerialPortReceivedListener.onSerialPortDataReceived(comPortData);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public int getBaudRate() {
        return baudRate;
    }

    public boolean setBaudRate(int iBaud) {
        if (openState) {
            return false;
        } else {
            baudRate = iBaud;
            return true;
        }
    }

    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    public String getPort() {
        return port;
    }

    public boolean setPort(String sPort) {
        if (openState) {
            return false;
        } else {
            this.port = sPort;
            return true;
        }
    }

    /**
     * 实现串口数据的接收监听
     */
    public interface OnSerialPortReceivedListener {
        /**
         * 串口接收到数据后的回调
         *
         * @param comPortData 接收到的数据类
         */
        void onSerialPortDataReceived(ComPortData comPortData);
    }
}