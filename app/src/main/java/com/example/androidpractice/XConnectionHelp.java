package com.example.androidpractice;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class XConnectionHelp implements Serializable {

    private static final String TAG = XConnectionHelp.class.getName();

    private static final int PORT = 49371;  // 外网端口
//    private static final int PORT = 5222; // 内网端口 / openfire服务端口

    public static final String IP = "103.46.128.53";   // 外网ip
//    public static final String IP = "192.168.43.136";
//    public static final String IP = "172.21.42.126";

    public static final String DOMAIN = "ubuntu";

    public static final String RET_SUCC = "操作成功";
    public static final String RET_FAIL = "操作失败";

    private AbstractXMPPConnection connection;

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    private String connect2Server(User u) {
        try {
            // 建立连接配置
            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
            //设置用户名密码
            configBuilder.setUsernameAndPassword(u.getName(), u.getPwd())
                    //设置XMPP域名，也就是XMPP协议后的@后的东西
                    .setXmppDomain(u.getDom())
                    //设置主机位置，也就是服务器ip
                    .setHostAddress(InetAddress.getByName(u.getIp()))
                    //等同于上面那句话builder.setHost("xxx.xxx.xxx.xxx");
                    //设置端口号
                    .setPort(PORT)
                    //设置不验证，否则需要TLS验证
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    //设置可以更改用户当前状态（在线、离线等等）
                    .setSendPresence(true);

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

    public String register2Server(User u) {
        try {
            String ret = connect2Server(new User("admin", "admin", u.getDom(), u.getIp()));
            if (!(RET_SUCC.equals(ret))) {
                return ret;
            } else {
                Log.i(TAG, "register2Server: Successful connection");
            }

            Map<String, String> attributes = new HashMap<>();
            attributes.put("name", u.getName());

            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(u.getName()), u.getPwd(), attributes);
            Log.i(TAG, "register2Server: Successful registration");

            // account: admin/admin disconnect
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

    public String login2Server(User user) {
        try {
            String ret = connect2Server(user);
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
        } catch (SmackException | IOException | InterruptedException e) {
            e.printStackTrace();
            return RET_FAIL;
        }
        return RET_SUCC;
    }

    public boolean isUserExistInServer(String username) {
        try {
            DomainBareJid jid =
                    JidCreate.domainBareFrom("search." + connection.getXMPPServiceDomain());
            UserSearchManager search = new UserSearchManager(connection);
            Form searchForm = search.getSearchForm(jid);
            Form answerForm = searchForm.createAnswerForm();
            // 根据用户名查找
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", username);

            ReportedData data = search.getSearchResults(answerForm, jid);
            Log.i(TAG, data.getRows().size() + "");

            if (data.getRows() != null) {
                for (ReportedData.Row row : data.getRows()) {
                    for (CharSequence value : row.getValues("jid")) {
                        /*
                        *   举例：若a，ab，abc均存在：
                        *   输入a，返回a，ab，abc
                        *   输入ab，返回ab，abc
                        *   输入abc，返回abc
                        * **************************
                        *   而若仅有abc存在：
                        *   输入a，返回abc
                        *   输入ab，返回abc
                        *   输入abc，返回abc
                        * **************************
                        *   所以，若输入为xxx且xxx存在，则xxx必然在第1行
                        *   因此，只需判断第1行是否匹配，即第1次循环就会执行return
                        */
                        Log.i(TAG, " " + value);
                        return value.equals(username + "@" + DOMAIN);
                    }
                }
            } else {
                return false;
            }

        } catch (InterruptedException
                | XMPPException.XMPPErrorException
                | XmppStringprepException
                | SmackException.NotConnectedException
                | SmackException.NoResponseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
