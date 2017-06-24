package de.canitzp.snapmap.transformer;

import de.canitzp.snapmap.mappings.remap.CustomClassAdapter;
import de.canitzp.snapmap.mappings.remap.CustomRemap;
import de.canitzp.snapmap.mappings.remap.CustomRemapper;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

/**
 * @author canitzp
 */
public class NameTransformer implements IClassNameTransformer, IClassTransformer {
    private CustomRemap remap;
    private MethodNode getMethod;

    public NameTransformer() {
        this.remap = new CustomRemap(DynamicMappings.reverseClassMappings, DynamicMappings.reverseMethodMappings, DynamicMappings.reverseFieldMappings);
        this.getMethod = new MethodNode(327680, 1, "doNothing", "()V", (String)null, (String[])null);
    }

    public String unmapClassName(String name) {
        if (!name.contains(".") || name.startsWith("net.minecraft")) {
            String newName = name.replace(".", "/");
            if (DynamicMappings.classMappings.containsKey(newName)) {
                name = ((String)DynamicMappings.classMappings.get(newName)).replace("/", ".");
            } else if (newName.contains("$")) {
                String inner = CustomRemapper.mapInnerClassName(newName, DynamicMappings.classMappings, (String)null, (String)null);
                name = inner.equals(newName) ? inner.replace("/", ".") : this.unmapClassName(inner);
            }
        }

        return name;
    }

    public String remapClassName(String name) {
        if (!name.contains(".") || name.startsWith("net.minecraft")) {
            String newName = name.replace(".", "/");
            if (DynamicMappings.reverseClassMappings.containsKey(newName)) {
                name = ((String)DynamicMappings.reverseClassMappings.get(newName)).replace("/", ".");
            } else if (newName.contains("$")) {
                String inner = CustomRemapper.mapInnerClassName(newName, DynamicMappings.reverseClassMappings, (String)null, (String)null);
                name = inner.equals(newName) ? inner.replace("/", ".") : this.remapClassName(inner);
            }
        }

        return name;
    }

    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.contains(".") && !name.startsWith("net.minecraft")) {
            return basicClass;
        } else {
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(new CustomClassAdapter(cn, new CustomRemapper(this.remap, cn, false)), 8);

            Iterator var6;
            MethodNode method;
            for(var6 = cn.methods.iterator(); var6.hasNext(); method.access = method.access & -8 | 1) {
                method = (MethodNode)var6.next();
            }

            var6 = cn.fields.iterator();

            while(true) {
                FieldNode field;
                do {
                    if (!var6.hasNext()) {
                        ClassWriter cw = new ClassWriter(1);
                        cn.accept(cw);
                        return cw.toByteArray();
                    }

                    field = (FieldNode)var6.next();
                } while(field.access != 8 && field.access != 64);

                field.access = field.access & -8 | 1;
            }
        }
    }
}
