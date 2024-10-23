package net.wensc.mitemod.mitebroadcasttool.util;

import com.google.gson.Gson;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class MyWebSocketServer extends WebSocketServer {
    public static MobEffectList [] positivePotions = new MobEffectList[]{MobEffectList.resistance, MobEffectList.field_76444_x, MobEffectList.nightVision, MobEffectList.heal, MobEffectList.moveSpeed, MobEffectList.digSpeed, MobEffectList.damageBoost, MobEffectList.regeneration};
    public static MobEffectList [] negativePotions = new MobEffectList[]{MobEffectList.moveSlowdown, MobEffectList.digSlowdown, MobEffectList.confusion, MobEffectList.weakness, MobEffectList.blindness, MobEffectList.poison, MobEffectList.hunger};
    public MyWebSocketServer(InetSocketAddress address) {
        super(address);
        this.start();
    }

    public ArrayList<EntityPlayer> getTargetPlayer(String name) {
        ArrayList<EntityPlayer> entityPlayerList = new ArrayList<>();
        String [] names = name.split(",");
        for(Object object: MinecraftServer.F().getConfigurationManager().playerEntityList) {
            EntityPlayer skanPlayer = (EntityPlayer)object;
            if(name.contains("@a-")){
               String notNames = name.substring(name.indexOf("@a-")+3);
               String []notNameList = notNames.split(",");
               if(!Arrays.asList(notNameList).contains(skanPlayer.getEntityName())){
                   entityPlayerList.add(skanPlayer);
               }
            }else if(name.contains("@a")){
                entityPlayerList.add(skanPlayer);
            } else if(Arrays.asList(names).contains(skanPlayer.getEntityName())){
                entityPlayerList.add(skanPlayer);
            }
        }
        return entityPlayerList;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String messageOut) {
//        System.out.println("Received message from " + conn.getRemoteSocketAddress() + ": " + messageOut);
        Gson gson = new Gson();
        WebsocketInitMessage message1 = gson.fromJson(messageOut, WebsocketInitMessage.class);
        WebsocketSignleHeap  current = (WebsocketSignleHeap) MinecraftServer.F().WSMessageHeap.get(message1.account);
        if(message1.login_token.equals("pollMessage")) {
            if(current != null){
                current.fifoHeap.poll();
            }
        } else {
            if(current != null){
                if(!current.initMessage.login_token.equals(message1.login_token)){
                    // 要对存量数据进行清除，因为此时要改变密钥，存量消息无法再解析
                    current.initMessage.login_token = message1.login_token;
                    current.initMessage.getRequestToken();
                    current.fifoHeap = new FifoHeap<>();
                }
                current.fifoHeap.offer(message1.encryptMessage);
            }  else {
                message1.getRequestToken();
                WebsocketSignleHeap websocketSignleHeap = new WebsocketSignleHeap(message1);
                websocketSignleHeap.fifoHeap.offer(message1.encryptMessage);
                MinecraftServer.F().WSMessageHeap.put(message1.account, websocketSignleHeap);
            }
        }
    }


    public void spawnEntity(Integer entityId, EntityPlayer entityPlayer){
        Entity entity = EntityTypes.createEntityByID(entityId, entityPlayer.worldObj);
        if (entity != null && entity instanceof EntityInsentient) {
            EntityInsentient entityLiving = (EntityInsentient)entity;
            entityLiving.setPosition(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);
            entityLiving.refreshDespawnCounter(-9600);
            entityLiving.addPotionEffect(new MobEffect(31, 50, 0));
            entityPlayer.worldObj.spawnEntityInWorld(entity);
            entityLiving.onSpawnWithEgg(null);
            if(!entityPlayer.isDead && entityPlayer != null) {
                entityLiving.setTarget(entityPlayer);
            }
            entityLiving.entityFX(EnumEntityFX.summoned);
        }

    }



    public void addXp(EntityPlayer entityPlayer){
        if(!entityPlayer.isDead && entityPlayer != null) {
            entityPlayer.addExperience(Math.max(1, entityPlayer.worldObj.rand.nextInt(50)));
        }
    }

    public void addRandomPositivePotion(EntityPlayer entityPlayer) {
        if(!entityPlayer.isDead && entityPlayer != null) {
            entityPlayer.addPotionEffect(new MobEffect(positivePotions[entityPlayer.worldObj.rand.nextInt(positivePotions.length)].id, 400, entityPlayer.worldObj.rand.nextInt(3)));
        }
    }

    public void addRandomNegativePotion(EntityPlayer entityPlayer) {
        if(!entityPlayer.isDead && entityPlayer != null) {
            entityPlayer.addPotionEffect(new MobEffect(negativePotions[entityPlayer.worldObj.rand.nextInt(negativePotions.length)].id, 400, entityPlayer.worldObj.rand.nextInt(3)));
        }
    }


    public void subXp(EntityPlayer entityPlayer){
        if(!entityPlayer.isDead && entityPlayer != null) {
            entityPlayer.addExperience(Math.min(-entityPlayer.worldObj.rand.nextInt(50), -1));
        }
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("An error occurred: " + ex);
    }

    @Override
    public void onStart() {
        System.err.println("websocket启动");
    }

}