package de.canitzp.snapmap.mappings.specific;

import de.canitzp.snapmap.mappings.CustomMappingBase;
import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static de.canitzp.snapmap.mappings.ClassNames.ICHAT_COMPONENT;
import static de.canitzp.snapmap.mappings.ClassNames.INET_HANDLER;
import static de.canitzp.snapmap.mappings.ClassNames.PACKET;

/**
 * @author canitzp
 */
public class PacketMappings extends CustomMappingBase{

    @Mapping(depends = PACKET,
            provides = INET_HANDLER,
            providesMethods = {INET_HANDLER + " onDisconnect (L" + ICHAT_COMPONENT + ";)V",
                                PACKET + " processPacket (L" + INET_HANDLER + ";)V"})
    public boolean findINetHandler(){
        ClassNode packet = getClassNodeFromMapping(PACKET);
        if(MeddleUtil.notNull(packet)){
            List<MethodNode> methods = findMethodsWithSignature(packet, "(TT;)V");
            if(methods.size() == 1){
                String className = Type.getArgumentTypes(methods.get(0).desc)[0].getClassName();
                addMethodMapping(packet, "processPacket", "(L" + INET_HANDLER + ";)V", methods.get(0));
                addClassMapping(INET_HANDLER, className);
                ClassNode iNetHandler = getClassNode(className);
                addMethodMapping(iNetHandler, "onDisconnect", "(L" + ICHAT_COMPONENT + ";)V", iNetHandler.methods.get(0));
            }
            return true;
        }
        return false;
    }

}
