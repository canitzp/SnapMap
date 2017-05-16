package de.canitzp.snapmap.mappings.remap;

import net.fybertech.dynamicmappings.DynamicRemap;
import net.fybertech.dynamicmappings.InheritanceMap;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author canitzp
 */
public class CustomRemapper extends Remapper {

    boolean showMapMethod = false;
    final ClassNode classNode;

    private CustomRemap oldRemapper;

    public CustomRemapper(CustomRemap oldRemapper, ClassNode cn, boolean debug) {
        this.oldRemapper = oldRemapper;
        this.classNode = cn;
        this.showMapMethod = debug;
    }

    public String map(String typeName) {
        boolean originallyUnpackaged = !typeName.contains("/");
        if(oldRemapper.classMappings.containsKey(typeName)) {
            return oldRemapper.classMappings.get(typeName);
        } else {
            String[] split = typeName.split("\\$");
            if(oldRemapper.classMappings.containsKey(split[0])) {
                split[0] = oldRemapper.classMappings.get(split[0]);
            }

            typeName = split[0];

            for(int n = 1; n < split.length; ++n) {
                String inner = split[n];
                if(originallyUnpackaged && oldRemapper.isObfInner(inner) && DynamicRemap.unpackagedInnerPrefix != null) {
                    inner = DynamicRemap.unpackagedInnerPrefix + inner + n;
                }

                typeName = typeName + "$" + inner;
            }

            if(!typeName.contains("/") && DynamicRemap.unpackagedPrefix != null) {
                typeName = DynamicRemap.unpackagedPrefix + typeName;
            }

            return super.map(typeName);
        }
    }

    public String mapFieldName(String owner, String name, String desc) {
        ClassNode cn = oldRemapper.getClassNode(owner);
        if(cn == null) {
            return super.mapFieldName(owner, name, desc);
        } else {
            InheritanceMap map = null;

            try {
                map = oldRemapper.inheritanceMapper.buildMap(cn);
            } catch (IOException var12) {
                var12.printStackTrace();
            }

            Set<InheritanceMap.FieldHolder> fields = map.fields.get(name + " " + desc);
            if(fields == null) {
                return super.mapFieldName(owner, name, desc);
            } else {
                Iterator var7 = fields.iterator();

                String key;
                do {
                    if(!var7.hasNext()) {
                        return super.mapFieldName(owner, name, desc);
                    }

                    InheritanceMap.FieldHolder holder = (InheritanceMap.FieldHolder)var7.next();
                    key = holder.cn.name + " " + holder.fn.name + " " + holder.fn.desc;
                } while(!oldRemapper.fieldMappings.containsKey(key));

                String mapping = oldRemapper.fieldMappings.get(key);
                String[] split = mapping.split(" ");
                return super.mapFieldName(owner, split[1], desc);
            }
        }
    }

    public String mapMethodName(String owner, String name, String desc) {
        if(!owner.startsWith("[") && !name.startsWith("<")) {
            if(this.showMapMethod) {
                System.out.println("mapMethod: " + owner + " " + name + " " + desc);
            }

            ClassNode cn = oldRemapper.getClassNode(owner);
            if(cn == null) {
                return super.mapMethodName(owner, name, desc);
            } else {
                InheritanceMap map = null;

                try {
                    map = oldRemapper.inheritanceMapper.buildMap(cn);
                } catch (IOException var12) {
                    var12.printStackTrace();
                }

                Set<InheritanceMap.MethodHolder> methods = map.methods.get(name + " " + desc);
                if(methods == null) {
                    return super.mapMethodName(owner, name, desc);
                } else {
                    Iterator var7 = methods.iterator();

                    String key;
                    do {
                        if(!var7.hasNext()) {
                            return super.mapMethodName(owner, name, desc);
                        }

                        InheritanceMap.MethodHolder holder = (InheritanceMap.MethodHolder)var7.next();
                        key = holder.cn.name + " " + holder.mn.name + " " + holder.mn.desc;
                        if(this.showMapMethod) {
                            System.out.println("Key: " + key);
                        }
                    } while(!oldRemapper.methodMappings.containsKey(key));

                    if(this.showMapMethod) {
                        System.out.println("    HAS KEY");
                    }

                    String mapping = oldRemapper.methodMappings.get(key);
                    String[] split = mapping.split(" ");
                    return super.mapMethodName(owner, split[1], desc);
                }
            }
        } else {
            return super.mapMethodName(owner, name, desc);
        }
    }

}
