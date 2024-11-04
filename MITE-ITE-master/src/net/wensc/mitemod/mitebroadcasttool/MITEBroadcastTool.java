package net.wensc.mitemod.mitebroadcasttool;


import net.wensc.mitemod.mitebroadcasttool.trans.TransMarker;
import net.wensc.mitemod.mitebroadcasttool.util.Configs;
import net.xiaoyu233.fml.AbstractMod;


import net.xiaoyu233.fml.classloading.Mod;
import net.xiaoyu233.fml.config.InjectionConfig;
import org.spongepowered.asm.mixin.MixinEnvironment;

@Mod()
public class MITEBroadcastTool extends AbstractMod {

    public MITEBroadcastTool() {
    }

    public void preInit() {
    }


    @Override
    public InjectionConfig getInjectionConfig() {
        return InjectionConfig.Builder.of("mite-broadcast-tool", TransMarker.class.getPackage(), MixinEnvironment.Phase.INIT).setRequired().build();
    }


    public void postInit() {
        super.postInit();
        Configs.loadConfigs();
    }


    public String modId() {
        return "mite-broadcast-tool";
    }

    public int modVerNum() {
        return 5;
    }

    public String modVerStr() {
        return "0.0.5";
    }
}
