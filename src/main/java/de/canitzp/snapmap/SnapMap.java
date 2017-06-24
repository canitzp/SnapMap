package de.canitzp.snapmap;

import de.canitzp.snapload.loader.SnapMod;
import de.canitzp.snapmap.mappings.*;
import de.canitzp.snapmap.mappings.remap.CustomRemap;
import de.canitzp.snapmap.mappings.remap.CustomRemapper;
import de.canitzp.snapmap.mappings.specific.BlockMappings;
import de.canitzp.snapmap.mappings.specific.PacketMappings;
import de.canitzp.snapmap.transformer.NameTransformer;
import net.fybertech.dynamicmappings.AccessUtil;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.DynamicRemap;
import net.fybertech.dynamicmappings.mappers.MappingsBase;
import net.fybertech.meddle.MeddleMod;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author canitzp
 */
@SnapMod(
        modid = "snapmap",
        name = "SnapMap",
        author = "canitzp",
        version = "19"
)
//@MeddleMod(id = "snapmap", name = "SnapMap", author = "canitzp", version = "1.0.0", depends = {"dynamicmappings"})
public class SnapMap implements ITweaker {

    private static List<String> childreensToMap = Arrays.asList("net/minecraft/block/Block", "net/minecraft/item/Item", "net/minecraft/entity/passive/EntityAnimal", "net/minecraft/entity/monster/EntityMob", "net/minecraft/entity/Entity");

    public SnapMap(){
        DynamicMappings.MAPPINGS_CLASSES.clear();
        addMapperClass(DefaultSharedMappings.class);
        addMapperClass(DefaultClientMappings.class);
        addMapperClass(Common.class);
        addMapperClass(Client.class);
        addMapperClass(PacketMappings.class);
        addMapperClass(BlockMappings.class);
        addMapperClass(ClassStringMapper.class);
        addMapperClass(ClassRemapper.class);
        addMapperClass(MethodRemapper.class);
        addMapperClass(FieldRemapper.class);
        DynamicRemap.unpackagedInnerPrefix = null;
        DynamicRemap.unpackagedPrefix = null;
    }

    @Override
    public void acceptOptions(List<String> list, File gameDir, File file1, String profile) {
        DynamicMappings.discoverMapperConfigs();
        DynamicMappings.generateClassMappings();

        try {
            File mapFile = new File(gameDir, "currentMappings.txt");
            if (mapFile.exists()) {
                mapFile.delete();
            }

            mapFile.createNewFile();
            List<String> lines = new ArrayList();
            Iterator var7 = DynamicMappings.reverseClassMappings.entrySet().iterator();

            while(var7.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var7.next();
                lines.add((String)entry.getKey() + " ==> " + (String)entry.getValue());
            }

            FileUtils.writeLines(mapFile, lines);
        } catch (IOException var11) {
            var11.printStackTrace();
        }

        Map<String, String> unknownClasses = new ConcurrentHashMap();
        Iterator var13 = DynamicMappings.classDeps.keySet().iterator();

        String s1;
        while(var13.hasNext()) {
            String s = (String)var13.next();
            if (!DynamicMappings.reverseClassMappings.containsKey(s) && !s.contains("/")) {
                if (s.contains("$")) {
                    s1 = CustomRemapper.mapInnerClassName(s, DynamicMappings.reverseClassMappings, (String)null, "net/minecraft/class_");
                    if (s1.contains("net/minecraft/class_")) {
                        unknownClasses.put(s, s1);
                    } else {
                        DynamicMappings.addClassMapping(s1, s);
                    }
                } else {
                    unknownClasses.put(s, "net/minecraft/class_" + s);
                }
            }
        }

        var13 = unknownClasses.entrySet().iterator();

        Map.Entry entry;
        while(var13.hasNext()) {
            entry = (Map.Entry)var13.next();
            boolean flag = false;
            Iterator var9 = childreensToMap.iterator();

            while(var9.hasNext()) {
                String s = (String)var9.next();
                if (DynamicMappings.isSubclassOf((String)entry.getKey(), (String)DynamicMappings.classMappings.get(s))) {
                    DynamicMappings.addClassMapping(s.concat("_" + (String)entry.getKey()), (String)entry.getKey());
                    unknownClasses.remove(entry.getKey());
                    flag = true;
                    break;
                }
            }

            if (!flag && !((String)entry.getKey()).contains("$")) {
                DynamicMappings.addClassMapping("net/minecraft/class_" + (String)entry.getKey(), (String)entry.getKey());
                unknownClasses.remove(entry.getKey());
            }
        }

        var13 = unknownClasses.entrySet().iterator();

        while(var13.hasNext()) {
            entry = (Map.Entry)var13.next();
            s1 = CustomRemapper.mapInnerClassName((String)entry.getKey(), DynamicMappings.reverseClassMappings, (String)null, "net/minecraft/class_");
            DynamicMappings.addClassMapping(s1, (String)entry.getKey());
            unknownClasses.remove(entry.getKey());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.registerTransformer(NameTransformer.class.getName());
    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    public static void addMapperClass(Class<? extends MappingsBase> mapper){
        DynamicMappings.MAPPINGS_CLASSES.add(mapper.getName());
    }

}
