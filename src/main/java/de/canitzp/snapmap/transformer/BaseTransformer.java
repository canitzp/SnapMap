package de.canitzp.snapmap.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author canitzp
 */
public abstract class BaseTransformer implements IClassTransformer {

    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }

    public abstract ClassWriter transform(String var1, String var2, ClassNode var3, ClassWriter var4);

}
