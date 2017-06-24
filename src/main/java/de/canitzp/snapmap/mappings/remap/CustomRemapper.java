package de.canitzp.snapmap.mappings.remap;

import net.fybertech.dynamicmappings.DynamicRemap;
import net.fybertech.dynamicmappings.InheritanceMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author canitzp
 */
public class CustomRemapper extends Remapper {

    boolean showMapMethod = false;
    final ClassVisitor classNode;
    private CustomRemap oldRemapper;

    public CustomRemapper(CustomRemap oldRemapper, ClassVisitor cn, boolean debug) {
        this.oldRemapper = oldRemapper;
        this.classNode = cn;
        this.showMapMethod = debug;
    }

    public String map(String typeName) {
        if (typeName.contains("/") && !typeName.startsWith("net/minecraft/")) {
            return typeName;
        } else if (this.oldRemapper.classMappings.containsKey(typeName)) {
            return (String)this.oldRemapper.classMappings.get(typeName);
        } else {
            if (typeName.contains("$")) {
                typeName = mapInnerClassName(typeName, this.oldRemapper.classMappings, DynamicRemap.unpackagedInnerPrefix, DynamicRemap.unpackagedPrefix);
                if (!typeName.contains("/") && DynamicRemap.unpackagedPrefix != null) {
                    typeName = DynamicRemap.unpackagedPrefix + typeName;
                }
            }

            return super.map(typeName);
        }
    }

    public String mapFieldName(String owner, String name, String desc) {
        ClassNode cn = this.oldRemapper.getClassNode(owner);
        if (cn == null) {
            return super.mapFieldName(owner, name, desc);
        } else {
            InheritanceMap map = null;

            try {
                map = this.oldRemapper.inheritanceMapper.buildMap(cn);
            } catch (IOException var11) {
                var11.printStackTrace();
            }

            Set<InheritanceMap.FieldHolder> fields = (Set)map.fields.get(name + " " + desc);
            if (fields == null) {
                return super.mapFieldName(owner, name, desc);
            } else {
                Iterator var7 = fields.iterator();

                while(var7.hasNext()) {
                    InheritanceMap.FieldHolder holder = (InheritanceMap.FieldHolder)var7.next();
                    String key = holder.cn.name + " " + holder.fn.name + " " + holder.fn.desc;
                    if (this.oldRemapper.fieldMappings.containsKey(key)) {
                        String mapping = (String)this.oldRemapper.fieldMappings.get(key);
                        String[] split = mapping.split(" ");
                        return super.mapFieldName(owner, split[1], desc);
                    }
                }

                return super.mapFieldName(owner, name, desc);
            }
        }
    }

    public String mapMethodName(String owner, String name, String desc) {
        if (!owner.startsWith("[") && !name.startsWith("<")) {
            if (this.showMapMethod) {
                System.out.println("mapMethod: " + owner + " " + name + " " + desc);
            }

            ClassNode cn = this.oldRemapper.getClassNode(owner);
            if (cn == null) {
                return super.mapMethodName(owner, name, desc);
            } else {
                InheritanceMap map = null;

                try {
                    map = this.oldRemapper.inheritanceMapper.buildMap(cn);
                } catch (IOException var11) {
                    var11.printStackTrace();
                }

                Set<InheritanceMap.MethodHolder> methods = (Set)map.methods.get(name + " " + desc);
                if (methods == null) {
                    return super.mapMethodName(owner, name, desc);
                } else {
                    Iterator var7 = methods.iterator();

                    String key;
                    do {
                        if (!var7.hasNext()) {
                            return super.mapMethodName(owner, name, desc);
                        }

                        InheritanceMap.MethodHolder holder = (InheritanceMap.MethodHolder)var7.next();
                        key = holder.cn.name + " " + holder.mn.name + " " + holder.mn.desc;
                        if (this.showMapMethod) {
                            System.out.println("Key: " + key);
                        }
                    } while(!this.oldRemapper.methodMappings.containsKey(key));

                    if (this.showMapMethod) {
                        System.out.println("    HAS KEY");
                    }

                    String mapping = (String)this.oldRemapper.methodMappings.get(key);
                    String[] split = mapping.split(" ");
                    return super.mapMethodName(owner, split[1], desc);
                }
            }
        } else {
            return super.mapMethodName(owner, name, desc);
        }
    }

    public static String mapInnerClassName(String name, Map<String, String> mappingsMap, @Nullable String innerPrefix, @Nullable String unknownClassPrefix) {
        String[] split = name.split("\\$");
        if (mappingsMap.containsKey(split[0])) {
            split[0] = (String)mappingsMap.get(split[0]);
        } else if (unknownClassPrefix != null) {
            split[0] = unknownClassPrefix + split[0];
        }

        StringBuilder typeNameBuilder = new StringBuilder(split[0]);

        for(int n = 1; n < split.length; ++n) {
            String inner = split[n];
            if (!name.contains("/") && name.length() == 1 && innerPrefix != null) {
                inner = innerPrefix + inner + n;
            }

            typeNameBuilder.append("$").append(inner);
        }

        name = typeNameBuilder.toString();
        return name;
    }

}
