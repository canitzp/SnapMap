package de.canitzp.snapmap.transformer;

import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.DynamicRemap;
import net.fybertech.dynamicmappings.InheritanceMap;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author canitzp
 */
public class ReobfuscationTransformer implements IClassTransformer {
    public static final List<String> EXCLUSIONS = Arrays.asList("com.jcraft.", "org.slf4j.", "org.apache.", "io.netty.", "com.google", "paulscode.", "joptsimple.", "com.mojang.", "net.minecraft.", "oshi.", "com.ibm.", "it.unimi.", "net.fybertech.dynamic", "de.canitzp.snapload");
    private DynamicRemap deobfMapper;
    private DynamicRemap obfMapper;

    public ReobfuscationTransformer() {
        this.deobfMapper = new DynamicRemap(DynamicMappings.reverseClassMappings, DynamicMappings.reverseMethodMappings, DynamicMappings.reverseFieldMappings);
        this.deobfMapper.inheritanceMapper = new InheritanceMap() {
            public ClassNode locateClass(String classname) throws IOException {
                return ReobfuscationTransformer.this.deobfMapper.remapClass(classname);
            }
        };
        this.obfMapper = new DynamicRemap(DynamicMappings.classMappings, DynamicMappings.methodMappings, DynamicMappings.fieldMappings) {
            public ClassNode getClassNode(String className) {
                if (className != null) {
                    className = className.replace(".", "/");
                    return DynamicMappings.classMappings.containsKey(className) ? ReobfuscationTransformer.this.deobfMapper.getClassNode((String)DynamicMappings.classMappings.get(className)) : ReobfuscationTransformer.this.deobfMapper.getClassNode(className);
                } else {
                    return null;
                }
            }
        };
        this.obfMapper.inheritanceMapper = new InheritanceMap() {
            public ClassNode locateClass(String classname) throws IOException {
                return ReobfuscationTransformer.this.obfMapper.getClassNode(classname);
            }
        };
    }

    public byte[] transform(String obfName, String transformedName, byte[] basicClass) {
        if (obfName.contains(".")) {
            Iterator var4 = EXCLUSIONS.iterator();

            String ex;
            do {
                if (!var4.hasNext()) {
                    ClassNode cn = this.deobfMapper.remapClass(new ClassReader(basicClass));
                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    System.out.println(cn.name);
                    return cw.toByteArray();
                }

                ex = (String)var4.next();
            } while(!obfName.startsWith(ex));

            return basicClass;
        } else {
            return basicClass;
        }
    }
}
