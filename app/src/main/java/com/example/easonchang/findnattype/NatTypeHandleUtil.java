package com.example.easonchang.findnattype;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

/**
 * Created by EasonChang on 2018/1/2.
 */

public class NatTypeHandleUtil {
    private String TAG = "NAT Discovery";
    private String sourceIp, stunIp;
    private int sourcePort, stunPort;
    private static NatTypeHandleUtil NATTypeFunc;
    private int currentNATType;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Context context;

    //get sharePreferences from Xmpp service
    public void setSharePreferences(SharedPreferences settings){
        this.settings = settings;
    }

    public static NatTypeHandleUtil getInstance(){
        if (NATTypeFunc == null){
            NATTypeFunc = new NatTypeHandleUtil();
        }
        return NATTypeFunc;
    }

    public NatTypeHandleUtil(){
        sourceIp = "192.168.0.103"; //this address will be changed by executeStunNAT function to get the current IP address
        sourcePort = 0; // set 0 is mean to generate the port number randomly when socket created
        stunIp = "stun.stunprotocol.org";
        stunPort = 3478;
    }

    //get local ip address
    public void ExecuteStunNAT(){
        try{
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
                while (iaddresses.hasMoreElements()) {
                    InetAddress iaddress = iaddresses.nextElement();
                    if (Class.forName("java.net.Inet4Address").isInstance(iaddress)) {
                        if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
                            Thread thread = new Thread(new DiscoveryNAT(iaddress));
                            thread.start();
                        }
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DiscoveryNAT implements Runnable {

        InetAddress iaddress;
        int port, NatTypeResult=0;

        public DiscoveryNAT(InetAddress iaddress){
            this.iaddress = iaddress;
            this.port = sourcePort;
        }

        @Override
        public void run() {
            try {
                DiscoveryTest test = new DiscoveryTest(iaddress,stunIp,stunPort);
                DiscoveryInfo di = test.test(); //test nat type
                if (di.isFullCone())
                    NatTypeResult = 1; //type 1
                else if (di.isRestrictedCone())
                    NatTypeResult = 2; //type 2
                else if (di.isPortRestrictedCone())
                    NatTypeResult =3; //type 3
                else if (di.isSymmetric())
                    NatTypeResult =4; //type 4
                currentNATType = NatTypeResult;

                //write the current nat type to sharepreference
                editor = settings.edit();
                editor.putBoolean("testFinish",true);
                editor.putInt("NatType",currentNATType);
                editor.putString("LocalIP",di.getLocalIP().getHostAddress());
                editor.putString("PublicIP",di.getPublicIP().getHostAddress());
                editor.commit();
                Log.e(TAG, String.valueOf(currentNATType));



            } catch (BindException be) {
                System.out.println(iaddress.toString() + ": " + be.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
