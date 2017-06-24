package de.canitzp.snapmap;

import de.canitzp.snapmap.mappings.remap.CustomRemap;
import net.fybertech.dynamicmappings.AccessUtil;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.DynamicRemap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author canitzp
 */
public class Remapper {

    public static void main(String[] args) throws InterruptedException, IOException {
        File out = new File("cache/remapped-minecraft.jar");
        if(out.exists()){
            out.delete();
            Thread.sleep(5000);
        }

        new SnapMap();

        DynamicMappings.generateClassMappings();

        AccessUtil accessUtil = new AccessUtil();
        //accessUtil.readAllTransformerConfigs();

        JarFile mcJar = new JarFile("cache/1.12-pre2.jar");//DynamicMappings.getMinecraftJar();

        DynamicRemap.remapUnknownChildren(mcJar, "net/minecraft/block/Block", "net/minecraft/item/Item", "net/minecraft/entity/monster/EntityMob",
                "net/minecraft/entity/Entity", "net/minecraft/tileentity/TileEntity", "net/minecraft/inventory/Container",
                "net/minecraft/client/gui/inventory/GuiContainer", "net/minecraft/client/gui/Gui", "net/minecraft/stats/StatBase", "net/minecraft/command/CommandBase");

        DynamicRemap remapper = new CustomRemap(
                DynamicMappings.reverseClassMappings,
                DynamicMappings.reverseFieldMappings,
                DynamicMappings.reverseMethodMappings);


        JarOutputStream outJar = new JarOutputStream(new FileOutputStream(out));

        for (Enumeration<JarEntry> enumerator = mcJar.entries(); enumerator.hasMoreElements();)
        {
            JarEntry entry = enumerator.nextElement();
            String name = entry.getName();
            byte[] bytes;

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

                //accessUtil.transformDeobfuscatedClass(mapped);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                mapped.accept(writer);
                name = mapped.name + ".class";
                bytes = writer.toByteArray();
            }
            else bytes = DynamicRemap.getFileFromZip(entry, mcJar);

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
            mcJar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
