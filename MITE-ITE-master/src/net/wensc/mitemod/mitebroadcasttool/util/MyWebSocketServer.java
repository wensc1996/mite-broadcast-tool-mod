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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.DedicatedServer.theDedicatedServer;

public class MyWebSocketServer extends WebSocketServer {
    public static Random rand = new Random();
    public static String [] positiveRandomNames  = new String[]{"addXp", "addRandomPositivePotion"};
    public static String [] negativeRandomNames  = new String[]{"randomSpawn", "subXp",  "addRandomNegativePotion"};
    public static int [] randomBaseEntityIDS = new int[] {50, 51, 52,  54, 57, 59, 60, 65, 66};
    public static MobEffectList [] positivePotions = new MobEffectList[]{MobEffectList.resistance, MobEffectList.field_76444_x, MobEffectList.nightVision, MobEffectList.heal, MobEffectList.moveSpeed, MobEffectList.digSpeed, MobEffectList.damageBoost, MobEffectList.regeneration};
    public static MobEffectList [] negativePotions = new MobEffectList[]{MobEffectList.moveSlowdown, MobEffectList.digSlowdown, MobEffectList.confusion, MobEffectList.weakness, MobEffectList.blindness, MobEffectList.poison, MobEffectList.hunger};
    public MyWebSocketServer(InetSocketAddress address) {
        super(address);
        this.start();
    }

    public void randomSpawn(EntityPlayer entityPlayer){
        Entity entity = null;
        if(rand.nextInt(2) == 0)  {
            entity =  EntityTypes.createEntityByID(randomBaseEntityIDS[rand.nextInt(randomBaseEntityIDS.length)], entityPlayer.worldObj);
        } else {
            entity =  EntityTypes.createEntityByID(513 + rand.nextInt(38), entityPlayer.worldObj);
        }
        if (entity != null ) {
            EntityInsentient entityLiving = (EntityInsentient)entity;
            entityLiving.setPosition(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);
            if(!entityPlayer.isDead && entityPlayer != null && (entity instanceof EntityMonster || entity instanceof EntityBat || entity instanceof EntityCubic)) {
                // PotionCannotHarm
                entityLiving.addPotionEffect(new MobEffect(31, 30, 0));
            }
            entityPlayer.worldObj.spawnEntityInWorld(entity);
            entityLiving.onSpawnWithEgg(null);
            entityLiving.entityFX(EnumEntityFX.summoned);
        }
    }

    public void handleAction(String action,int count, EntityPlayer entityPlayer, boolean isRandom){
        switch (action){
            case  "randomSpawn":
                for (int i = 0; i < count; i++) {
                    this.randomSpawn(entityPlayer);
                }
                break;
            case "killNeighbouringMob":
                for (int i = 0; i < count; i++) {
                    this.killNeighbouringMob(entityPlayer);
                }
                break;
            case "addXp":
                for (int i = 0; i < count; i++) {
                    this.addXp(entityPlayer);
                }
                break;
            case "subXp":
                for (int i = 0; i < count; i++) {
                    this.subXp(entityPlayer);
                }
                break;
            case "addRandomPositivePotion":
                for (int i = 0; i < count; i++) {
                    this.addRandomPositivePotion(entityPlayer);
                }
                break;
            case "addRandomNegativePotion":
                for (int i = 0; i < count; i++) {
                    this.addRandomNegativePotion(entityPlayer);
                }
                break;
        }
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
        Gson gson = new Gson();
        WebsocketInitMessage message1 = gson.fromJson(messageOut, WebsocketInitMessage.class);
        WebsocketSignleHeap  current = (WebsocketSignleHeap) theDedicatedServer.WSMessageHeap.get(message1.account);
        if(current != null){
            if(!current.initMessage.login_token.equals(message1.login_token)){
                // 要对存量数据进行清除，因为此时要改变密钥，存量消息无法再解析
                current.initMessage.login_token = message1.login_token;
                current.initMessage.getRequestToken();
                current.fifoHeap = new ArrayList<>();
            }
            current.fifoHeap.add(message1.encryptMessage);
        }  else {
            message1.getRequestToken();
            WebsocketSignleHeap websocketSignleHeap = new WebsocketSignleHeap(message1);
            websocketSignleHeap.fifoHeap.add(message1.encryptMessage);
            theDedicatedServer.WSMessageHeap.put(message1.account, websocketSignleHeap);
        }
    }

    public void killNeighbouringMob(EntityPlayer entityPlayer){
        if(!entityPlayer.isDead && entityPlayer != null) {
            List entityList = entityPlayer.getNearbyEntities(8f, 8f);
            AtomicInteger size = new AtomicInteger();
            entityList.forEach(e -> {
                if(e instanceof EntityMonster || e instanceof EntityBat || e instanceof EntityCubic){
                    size.getAndIncrement();
                    EntityLiving entityLiving = (EntityLiving)e;
                    entityLiving.setDead();
                }
            });
            entityPlayer.addChatMessage("已清除附近的怪物，合计：" + size.get());
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
        addRandomPotion(entityPlayer, positivePotions);
    }

    public void addRandomNegativePotion(EntityPlayer entityPlayer) {
        addRandomPotion(entityPlayer, negativePotions);
    }

    private void addRandomPotion(EntityPlayer entityPlayer, MobEffectList[] potions) {
        if(!entityPlayer.isDead && entityPlayer != null) {
            MobEffectList potion = potions[entityPlayer.worldObj.rand.nextInt(potions.length)];
            int amplifier = entityPlayer.worldObj.rand.nextInt(3);
            MobEffect activeEffect = entityPlayer.getActivePotionEffect(potion);
            if(activeEffect != null && activeEffect.getAmplifier() == amplifier) {
                activeEffect.setDuration(activeEffect.getDuration() + 400);
            } else {
                entityPlayer.addPotionEffect(new MobEffect(potion.id, 400, amplifier));
            }
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