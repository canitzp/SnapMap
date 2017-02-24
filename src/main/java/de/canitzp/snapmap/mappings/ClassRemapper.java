package de.canitzp.snapmap.mappings;

import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Set;

/**
 * @author canitzp
 */
public class ClassRemapper extends CustomMappingBase {

    @Mapping(depends = "net/minecraft/entity/projectile/EntityPotion")
    public void findAndProcessEntityThrowable(){
        ClassNode entity = getClassNodeFromMapping("net/minecraft/entity/Entity");
        ClassNode potion = getClassNodeFromMapping("net/minecraft/entity/projectile/EntityPotion");
        ClassNode throwable = getClassNode(potion.superName);
        ClassNode iProjectile = getClassNode(throwable.interfaces.get(0));
        if(MeddleUtil.notNull(entity, potion, throwable, iProjectile)){
            addClassMapping("net/minecraft/entity/projectile/EntityThrowable", throwable);
            addClassMapping("net/minecraft/entity/projectile/IProjectile", iProjectile);
            addMethodMapping(iProjectile, "setThrowableHeading", iProjectile.methods.get(0).desc, iProjectile.methods.get(0));
            List<MethodNode> methodNodes = getMatchingMethods(potion, Opcodes.ACC_PUBLIC, Type.VOID, Type.OBJECT);
            for(MethodNode methodNode : methodNodes){
                List<String> strings = getStringsFromMethod(methodNode);
                if(!strings.isEmpty() && strings.get(0).equals("Potion")){
                    for(AbstractInsnNode insnNode : methodNode.instructions.toArray()){
                        if(insnNode.getOpcode() == Opcodes.NEW){
                            if(((TypeInsnNode)insnNode).desc.equals(getClassNodeFromMapping("net/minecraft/nbt/NBTTagCompound").name)){
                                addMethodMapping(potion, "writeNBT", methodNode.desc, methodNode);
                            } else {
                                addMethodMapping(entity, "readNBT", methodNode.desc, methodNode);
                            }
                        }
                    }
                }
            }
        }
    }

    @Mapping(depends = "net/minecraft/entity/Entity",
            provides = "net/minecraft/entity/EntityHanging",
            providesMethods = "net/minecraft/entity/Entity setPosition (DDD)V"
    )
    public void findAndProcessEntityHanging(){
        ClassNode entity = getClassNodeFromMapping("net/minecraft/entity/Entity");
        if(MeddleUtil.notNull(entity)){
            for(String child : DynamicMappings.getChildClasses(entity.name)){
                if(!DynamicMappings.reverseClassMappings.containsKey(child)){
                    if(!DynamicMappings.getChildClasses(child).isEmpty()){
                        ClassNode cn = getClassNode(child);
                        if(cn.interfaces.isEmpty() && searchConstantPoolForStrings(cn.name, "Facing", "TileX", "TileY", "TileZ")){
                            addClassMapping("net/minecraft/entity/EntityHanging", cn);
                            for(String child1 : DynamicMappings.getChildClasses(cn.name)) {
                                if (!DynamicMappings.reverseClassMappings.containsKey(child1)) {
                                    ClassNode cn1 = getClassNode(child1);
                                    addClassMapping("net/minecraft/entity/EntityLeashKnot", cn1);
                                    List<MethodNode> methods = getMatchingMethods(cn1, Opcodes.ACC_PUBLIC, Type.VOID, Type.DOUBLE, Type.DOUBLE, Type.DOUBLE);
                                    if(methods.size() == 1){
                                        addMethodMapping(entity, "setPosition", methods.get(0).desc, methods.get(0));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
