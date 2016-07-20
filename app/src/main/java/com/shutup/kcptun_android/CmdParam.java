package com.shutup.kcptun_android;

/**
 * Created by shutup on 16/7/19.
 */
public class CmdParam {

    public static String LOCALADDR = "-l";
    public static String REMOTEADDR = "-r";
    public static String KEY = "-key";
    public static String CRYPT = "-crypt";
    public static String MODE = "-mode";


    public String localaddr;
    public String remoteaddr;
    public String key;
    public String crypt;
    public String mode;
    public String mtu;
    public String sndwnd;
    public String revwnd;
    public boolean nocmp;

    public Boolean isBasicOk(){
        if (remoteaddr.equalsIgnoreCase("")){
            return false;
        }
        if (key.equalsIgnoreCase("")) {
            return false;
        }

        return true;
    }

}
