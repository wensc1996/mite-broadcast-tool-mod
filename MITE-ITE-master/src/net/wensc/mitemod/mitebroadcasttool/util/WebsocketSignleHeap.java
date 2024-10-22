package net.wensc.mitemod.mitebroadcasttool.util;

public class WebsocketSignleHeap {
    public WebsocketInitMessage initMessage;
    public FifoHeap fifoHeap = new FifoHeap();
    public WebsocketSignleHeap(WebsocketInitMessage initMessage){
        this.initMessage =  initMessage;
    }
}
