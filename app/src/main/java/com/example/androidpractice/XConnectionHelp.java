package com.example.androidpractice;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

public class XConnectionHelp implements Serializable {

    private static final String TAG = XConnectionHelp.class.getName();

    private static final int PORT = 5222;
    public static final String RET_SUCC = "Success";
    public static final String RET_FAIL = "Failure";

    private AbstractXMPPConnection connection;

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    private String connect2Server(String usr, String pwd, String ip) {
        try {
            // 建立连接配置
            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
            //设置用户名密码
            configBuilder.setUsernameAndPassword(usr, pwd)
                    //设置XMPP域名，也就是XMPP协议后的@后的东西
                    .setXmppDomain("ubuntu")
                    //设置主机位置，也就是服务器ip
                    .setHostAddress(InetAddress.getByName(ip))
                    //等同于上面那句话builder.setHost("xxx.xxx.xxx.xxx");
                    //设置端口号，默认5222
                    .setPort(PORT)
                    //设置不验证，否则需要TLS验证
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    //设置可以更改用户当前状态（在线、离线等等）
                    .setSendPresence(true);
            //设置在线
            //Presence presence = new Presence(Presence.Type.available);
            //通知在线
            //xmpptcpConnection.sendStanza(presence);
            //通过配置建立连接
            // AbstractXMPPConnection connection = new XMPPTCPConnection(configBuilder.build());
            connection = new XMPPTCPConnection(configBuilder.build());
            // 连接到服务器
            connection.connect();

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return e.getStanzaError().toString();
        } catch (XMPPException | IllegalArgumentException e) {
            e.printStackTrace();
            return e.toString();
        } catch (SmackException | IOException | InterruptedException e) {
            e.printStackTrace();
            return RET_FAIL;
        }
        return RET_SUCC;
    }

    public String register2Server(String usr, String pwd, String ip) {
        try {
            String ret = connect2Server("admin", "admin", ip);
            if (!(RET_SUCC.equals(ret))) {
                return ret;
            } else {
                Log.i(TAG, "register2Server: Successful connection");
            }

            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(usr), pwd);
            Log.i(TAG, "register2Server: Successful registration");

            // admin/admin disconnect
            connection.disconnect();

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return e.getStanzaError().toString();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return e.toString();
        } catch (SmackException | IOException | InterruptedException e) {
            e.printStackTrace();
            return RET_FAIL;
        }
        return RET_SUCC;
    }

    public String login2Server(String usr, String pwd, String ip) {
        try {
            String ret = connect2Server(usr, pwd, ip);
            if (!(RET_SUCC.equals(ret))) {
                return ret;
            } else {
                Log.i(TAG, "register2Server: Successful connection");
            }

            // 通知在线
            connection.sendStanza(new Presence(Presence.Type.available));
            connection.login();

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return e.getStanzaError().toString();
        } catch (XMPPException | IllegalArgumentException e) {
            e.printStackTrace();
            return e.toString();
        }catch (SmackException | IOException | InterruptedException e) {
            e.printStackTrace();
            return RET_FAIL;
        }
        return RET_SUCC;
    }
}
