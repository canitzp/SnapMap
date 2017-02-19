package de.canitzp.snapmap.tweaker;

import net.fybertech.dynamicmappings.DynamicMappings;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author canitzp
 */
public abstract class ClassTransformBase implements IClassTransformer {

    private String unobfClassName;

    public ClassTransformBase(String unobfName){
        this.unobfClassName = unobfName;
    }

    @Override
    public final byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals(DynamicMappings.getClassMapping(this.unobfClassName)) || transformedName.equals(this.unobfClassName)){
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.EXPAND_FRAMES);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            transform(this.unobfClassName, cn).accept(cw);
            return cw.toByteArray();
        }
        return basicClass;
    }

    public abstract ClassNode transform(String unobfName, ClassNode node);

}
