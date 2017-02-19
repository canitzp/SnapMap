package de.canitzp.snapmap.mappings;

import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author canitzp
 */
public class FieldRemapper extends CustomMappingBase {

    @Mapping(depends = "net/minecraft/client/Minecraft")
    public void findMinecraftFields(){
        ClassNode minecraft = getClassNodeFromMapping("net/minecraft/client/Minecraft");
        if(MeddleUtil.notNull(minecraft)){
            automapAllFieldNames(minecraft);
            addFieldMappingIfSingle(minecraft, "LOGGER", "Lorg/apache/logging/log4j/Logger;");
            addFieldMappingIfSingle(minecraft, "memoryReserve", "[B");
            addFieldMappingIfSingle(minecraft, "effectRenderer", getClassNodeFromMapping("net/minecraft/client/particle/ParticleManager"));
            addFieldMappingIfSingle(minecraft, "currentScreen", getClassNodeFromMapping("net/minecraft/client/gui/GuiScreen"));
            addFieldMappingIfSingle(minecraft, "objectMouseOver", getClassNodeFromMapping("net/minecraft/util/math/RayTraceResult"));
        }
    }

}
