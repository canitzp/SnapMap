package de.canitzp.snapmap.transformer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.Objects;

/**
 * @author canitzp
 */
public class GuiMainMenuTransfomer extends BaseTransformer {

    public ClassWriter transform(String obfName, String mappedName, ClassNode node, ClassWriter writer) {
        if (mappedName.equals("net/minecraft/client/gui/GuiMainMenu")) {
            Iterator var5 = node.methods.iterator();

            while(true) {
                MethodNode method;
                do {
                    if (!var5.hasNext()) {
                        return writer;
                    }

                    method = (MethodNode)var5.next();
                } while(!"drawScreen".equals(method.name));

                AbstractInsnNode[] array = method.instructions.toArray();
                AbstractInsnNode[] var8 = array;
                int var9 = array.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    AbstractInsnNode insn = var8[var10];
                    if (insn instanceof LdcInsnNode && Objects.equals(((LdcInsnNode)insn).cst, "Minecraft 1.12-pre5")) {
                        InsnList insnList = new InsnList();
                        insnList.add(new MethodInsnNode(184, "de/canitzp/snapmap/transformer/GuiMainMenuTransformer", "drawMainMenuBranding", "()V", false));
                        method.instructions.insertBefore(insn, insnList);
                        ClassWriter writer1 = new ClassWriter(3);
                        node.accept(writer1);
                        System.out.println("Transform");
                        return writer1;
                    }
                }
            }
        } else {
            return writer;
        }
    }

    public static void drawMainMenuBranding() {
        System.out.println("test");
    }

}
