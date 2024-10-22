package net.wensc.mitemod.mitebroadcasttool.util;

import com.google.gson.Gson;

public class WebsocketInitMessage {
    public String account;
    public String login_token;
    public String request_token;
    public String  encryptMessage;
    public String getRequestToken() {
        try {
            Gson gson = new Gson();
            String res = HttpUtils.sendPost("https://wensc.cn/mite-broadcast/getRequestToken", "{\"login_token\": \""+this.login_token+"\"}");
            RequestResponse requestResponse = gson.fromJson(res,  RequestResponse.class);
            if(requestResponse.code == 0) {
                this.request_token = requestResponse.data;
                return requestResponse.data;
            } else {
                this.request_token = null;
                return  null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
