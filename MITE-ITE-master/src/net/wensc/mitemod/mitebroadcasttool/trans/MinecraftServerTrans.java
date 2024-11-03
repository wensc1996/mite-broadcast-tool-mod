package net.wensc.mitemod.mitebroadcasttool.trans;

import com.google.gson.Gson;
import net.minecraft.DedicatedServer;
import net.minecraft.EntityPlayer;
import net.minecraft.server.MinecraftServer;
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
import java.util.Map;

import static net.wensc.mitemod.mitebroadcasttool.util.StaticKeeper.decryptString;

@Mixin(value = MinecraftServer.class)
public class MinecraftServerTrans {
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
        if(!this.WSMessageHeap.isEmpty()) {
            this.WSMessageHeap.forEach((account, instance) -> {
                if(instance.initMessage.request_token !=  null && !instance.fifoHeap.isEmpty()){
                    String encryptMessage = (String)instance.fifoHeap.peek();// 先去查询
                    String [] ivMessage = encryptMessage.split(":");
                    try {
                        String message = decryptString(ivMessage[1], instance.initMessage.request_token, ivMessage[0]);
                        WebsocktMessage message1 = gson.fromJson(message, WebsocktMessage.class);
                        ArrayList<EntityPlayer> entityPlayers = myWebSocketServer.getTargetPlayer(message1.playerName);
                        if(!entityPlayers.isEmpty()) {
                            instance.fifoHeap.poll(); // 处理就清除
                            entityPlayers.forEach(entityPlayer -> {
                                entityPlayer.addChatMessage("["+message1.plaftform+"]-["+message1.nickname+"]"+message1.content);
                                if(message1.action.startsWith("spawn:")){
                                    int entityId = Integer.parseInt(message1.action.split(":")[1]);
                                    for (int i = 0; i < message1.count; i++) {
                                        myWebSocketServer.spawnEntity(entityId, entityPlayer);
                                    }
                                } else {
                                    switch (message1.action){
                                        case "addXp":
                                            for (int i = 0; i < message1.count; i++) {
                                                myWebSocketServer.addXp(entityPlayer);
                                            }
                                            break;
                                        case "subXp":
                                            for (int i = 0; i < message1.count; i++) {
                                                myWebSocketServer.subXp(entityPlayer);
                                            }
                                            break;
                                        case "addRandomPositivePotion":
                                            for (int i = 0; i < message1.count; i++) {
                                                myWebSocketServer.addRandomPositivePotion(entityPlayer);
                                            }
                                            break;
                                        case "addRandomNegativePotion":
                                            for (int i = 0; i < message1.count; i++) {
                                                myWebSocketServer.addRandomNegativePotion(entityPlayer);
                                            }
                                            break;
                                    }
                                }

                            });
                        }
                    } catch (InvalidAlgorithmParameterException | RuntimeException | NoSuchPaddingException |
                             NoSuchAlgorithmException |
                             InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
