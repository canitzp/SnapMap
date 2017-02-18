package de.canitzp.snapmap;

import com.google.common.collect.Lists;
import de.canitzp.snapmap.mappings.*;
import net.fybertech.dynamicmappings.AccessUtil;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.DynamicRemap;
import net.fybertech.dynamicmappings.mappers.MappingsBase;
import net.fybertech.meddle.Meddle;
import net.fybertech.meddle.MeddleMod;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author canitzp
 */
@MeddleMod(id = "snapmap", name = "SnapMap", author = "canitzp", version = "1.0.0", depends = {"dynamicmappings"})
public class SnapMap implements ITweaker {

    public SnapMap(){
        DynamicMappings.MAPPINGS_CLASSES.clear();
        addMapperClass(DefaultSharedMappings.class);
        addMapperClass(DefaultClientMappings.class);
        addMapperClass(Common.class);
        addMapperClass(Client.class);
        addMapperClass(ClassStringMapper.class);
    }

    @Override
    public void acceptOptions(List<String> list, File file, File file1, String s) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {

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

    public static void main(String[] args){
        File out = new File("cache/remapped-minecraft.jar");
        new SnapMap();

        DynamicMappings.generateClassMappings();

        AccessUtil accessUtil = new AccessUtil();
        accessUtil.readAllTransformerConfigs();

        JarFile mcJar = DynamicMappings.getMinecraftJar();

        if (mcJar != null) {
            DynamicRemap.remapUnknownChildren(mcJar, "net/minecraft/block/Block", "net/minecraft/item/Item", "net/minecraft/entity/monster/EntityMob",
                    "net/minecraft/entity/Entity", "net/minecraft/tileentity/TileEntity", "net/minecraft/inventory/Container",
                    "net/minecraft/client/gui/inventory/GuiContainer", "net/minecraft/client/gui/Gui", "net/minecraft/stats/StatBase");
        }

        DynamicRemap remapper = new DynamicRemap(
                DynamicMappings.reverseClassMappings,
                DynamicMappings.reverseFieldMappings,
                DynamicMappings.reverseMethodMappings);


        JarFile jar = mcJar;

        if (jar == null) { System.out.println("Couldn't locate Minecraft jar!"); return; }

        JarOutputStream outJar = null;
        try {
            outJar = new JarOutputStream(new FileOutputStream(out));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Enumeration<JarEntry> enumerator = jar.entries(); enumerator.hasMoreElements();)
        {
            JarEntry entry = enumerator.nextElement();
            String name = entry.getName();
            byte[] bytes = null;

            if (name.startsWith("META-INF/")) {
                if (name.endsWith(".RSA") || name.endsWith(".SF")) continue;
            }

            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                ClassNode mapped = remapper.remapClass(name);

                // Correct the source filename
                if (mapped.sourceFile != null && mapped.sourceFile.equals("SourceFile")) {
                    String sourceName = mapped.name;
                    if (sourceName.indexOf('$') >= 0)
                        sourceName = sourceName.substring(0, sourceName.indexOf('$'));
                    mapped.sourceFile = sourceName + ".java";
                }

                accessUtil.transformDeobfuscatedClass(mapped);
                ClassWriter writer = new ClassWriter(0);
                mapped.accept(writer);
                name = mapped.name + ".class";
                bytes = writer.toByteArray();
            }
            else bytes = DynamicRemap.getFileFromZip(entry, jar);

            ZipEntry ze = new ZipEntry(name);
            try {
                outJar.putNextEntry(ze);
                outJar.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            outJar.close();
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllUnmappedClasses(){
        List<String> ret = new ArrayList<>();
        return ret;
    }

}
