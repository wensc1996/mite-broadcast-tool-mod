package net.wensc.mitemod.mitebroadcasttool.trans;

import com.google.gson.Gson;
import net.minecraft.ChatMessage;
import net.minecraft.DedicatedServer;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumChatFormat;
import net.wensc.mitemod.mitebroadcasttool.util.Configs;
import net.wensc.mitemod.mitebroadcasttool.util.MyWebSocketServer;
import net.wensc.mitemod.mitebroadcasttool.util.WebsocketSignleHeap;
import net.wensc.mitemod.mitebroadcasttool.util.WebsocktMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static net.wensc.mitemod.mitebroadcasttool.util.StaticKeeper.decryptString;

@Mixin(value = DedicatedServer.class)

public class DedicatedServerTrans {
    public Map<String, WebsocketSignleHeap> WSMessageHeap  = new HashMap<>();
    public MyWebSocketServer myWebSocketServer;

    @Inject( method = "<init>", at = @At("RETURN"))
    public void injectInit(File par1File, CallbackInfo callbackInfo) {
        myWebSocketServer = new MyWebSocketServer(new InetSocketAddress(Configs.wenscConfig.WSPort.ConfigValue));
        System.out.println("WebSocket server started on port " + Configs.wenscConfig.WSPort.ConfigValue);
    }
    public Gson gson = new Gson();
    @Inject(method = "s", at = @At("RETURN"))
    public void tick(CallbackInfo callbackInfo) {
        Iterator<Map.Entry<String, WebsocketSignleHeap>> wsInstanceIterator = this.WSMessageHeap.entrySet().iterator();
        while (wsInstanceIterator.hasNext()){
            Map.Entry<String, WebsocketSignleHeap> wsInstance = wsInstanceIterator.next();
            WebsocketSignleHeap instance = wsInstance.getValue();
            if(instance.initMessage.request_token !=  null){
                for (int messageIndex  = 0; messageIndex< instance.fifoHeap.size(); messageIndex++){
                    String encryptMessage = instance.fifoHeap.get(messageIndex);// 先去查询
                    String [] ivMessage = encryptMessage.split(":");
                    try {
                        String message = decryptString(ivMessage[1], instance.initMessage.request_token, ivMessage[0]);
                        WebsocktMessage message1 = gson.fromJson(message, WebsocktMessage.class);
                        ArrayList<EntityPlayer> entityPlayers = myWebSocketServer.getTargetPlayer(message1.playerName);
                        instance.fifoHeap.remove(messageIndex); // 处理就清除
                        if(!entityPlayers.isEmpty()) {
                            entityPlayers.forEach(entityPlayer -> {
                                entityPlayer.sendChatToPlayer(
                                        ChatMessage.createFromTranslationKey("["+message1.plaftform+"]").setColor(EnumChatFormat.YELLOW)
                                                .appendComponent(ChatMessage.createFromTranslationKey("["+message1.nickname+"]").setColor(EnumChatFormat.AQUA))
                                                .appendComponent(ChatMessage.createFromTranslationKey(message1.content).setColor(EnumChatFormat.WHITE)));
                                if(message1.action.startsWith("spawn:")){
                                    int entityId = Integer.parseInt(message1.action.split(":")[1]);
                                    for (int i = 0; i < message1.count; i++) {
                                        myWebSocketServer.spawnEntity(entityId, entityPlayer);
                                    }
                                } else {
                                    if(message1.action.equals("random"))  {
                                        for (int i = 0; i < message1.count; i++) {
                                            if(MyWebSocketServer.rand.nextInt(7) < 3) {
                                                myWebSocketServer.handleAction(MyWebSocketServer.positiveRandomNames[MyWebSocketServer.rand.nextInt(MyWebSocketServer.positiveRandomNames.length)], 1, entityPlayer, true);
                                            } else {
                                                myWebSocketServer.handleAction(MyWebSocketServer.negativeRandomNames[MyWebSocketServer.rand.nextInt(MyWebSocketServer.negativeRandomNames.length)], 1, entityPlayer, true);
                                            }
                                        }
                                    } else {
                                        myWebSocketServer.handleAction(message1.action, message1.count, entityPlayer, false);
                                    }
                                }
                            });
                        }
                    } catch (InvalidAlgorithmParameterException | RuntimeException | NoSuchPaddingException | NoSuchAlgorithmException |
                             InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
