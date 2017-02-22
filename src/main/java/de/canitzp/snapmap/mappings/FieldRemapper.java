package de.canitzp.snapmap.mappings;

import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

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
            automapAllGetter(minecraft);
            automapAllSetter(minecraft);
        }
    }

    @Mapping(depends = "net/minecraft/entity/Entity")
    public void findEntityFields(){
        ClassNode entity = getClassNodeFromMapping("net/minecraft/entity/Entity");
        if(MeddleUtil.notNull(entity)){
            automapAllFieldNames(entity);
            addFieldMappingIfSingle(entity, "LOGGER", "Lorg/apache/logging/log4j/Logger;");
        }
    }

    @Mapping(depends = "net/minecraft/entity/EntityLivingBase")
    public void findEntityLivingFields() {
        ClassNode entity = getClassNodeFromMapping("net/minecraft/entity/EntityLivingBase");
        if (MeddleUtil.notNull(entity)) {
            automapAllFieldNames(entity);
        }
    }

    @Mapping(depends = "net/minecraft/command/CommandBase")
    public void findCommandBaseFields(){
        ClassNode commandBase = getClassNodeFromMapping("net/minecraft/command/CommandBase");
        if (MeddleUtil.notNull(commandBase)) {
            automapAllFieldNames(commandBase);

            List<MethodNode> methods = getMatchingMethods(commandBase, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, Type.OBJECT, Type.OBJECT, Type.OBJECT, Type.OBJECT, Type.OBJECT);
            if(methods.size() == 1){
                addMethodMapping(commandBase, "getEntity", methods.get(0).desc, methods.get(0));
            }
        }
    }

}
