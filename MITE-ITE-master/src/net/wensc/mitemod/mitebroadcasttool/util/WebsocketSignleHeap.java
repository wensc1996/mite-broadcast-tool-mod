package net.wensc.mitemod.mitebroadcasttool.util;

import java.util.ArrayList;

public class WebsocketSignleHeap {
    public WebsocketInitMessage initMessage;
    public ArrayList<String> fifoHeap = new ArrayList();
    public WebsocketSignleHeap(WebsocketInitMessage initMessage){
        this.initMessage =  initMessage;
    }
}
