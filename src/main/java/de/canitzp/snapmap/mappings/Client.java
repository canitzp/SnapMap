package de.canitzp.snapmap.mappings;

import com.google.common.collect.Maps;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.dynamicmappings.MappingsClass;
import net.fybertech.dynamicmappings.mappers.MappingsBase;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author canitzp
 */
@MappingsClass(clientSide = true)
public class Client extends MappingsBase {

    private static final Map<String[], String> unknownGuiClasses = new HashMap<String[], String>(){{
        put(new String[]{"enchantment.level.2", "enchantment.level.3", "enchantment.level.4"}, "inventory/InventoryEffectRenderer");
    }};

    @Mapping(
            depends = "net/minecraft/client/gui/Gui",
            provides = "net/minecraft/client/gui/inventory/InventoryEffectRenderer")
    public void processGuis(){
        ClassNode gui = getClassNodeFromMapping("net/minecraft/client/gui/Gui");
        if(MeddleUtil.notNull(gui)){
            for(String unknownClass : DynamicMappings.getChildClasses(gui.name)){
                for(Map.Entry<String[], String> entry : unknownGuiClasses.entrySet()){
                    String unobfClassName = "net/minecraft/client/gui/" + entry.getValue();
                    if(searchConstantPoolForStrings(unknownClass, entry.getKey())){
                        addClassMapping(unobfClassName, unknownClass);
                    }
                }
            }
        }
    }

    @Mapping(
            depends = "net/minecraft/client/gui/inventory/InventoryEffectRenderer",
            provides = {
                    "net/minecraft/client/gui/inventory/GuiInventory",
                    "net/minecraft/client/gui/inventory/GuiContainerCreative"
            })
    public void processInventoryEffectRender(){
        ClassNode inventoryEffectRender = getClassNodeFromMapping("net/minecraft/client/gui/inventory/InventoryEffectRenderer");
        if(MeddleUtil.notNull(inventoryEffectRender)){
            for(String child : DynamicMappings.getChildClasses(inventoryEffectRender.name)){
                if(searchConstantPoolForStrings(child, "textures/gui/container/creative_inventory/tabs.png")){
                    addClassMapping("net/minecraft/client/gui/inventory/GuiContainerCreative", child);
                } else {
                    addClassMapping("net/minecraft/client/gui/inventory/GuiInventory", child);
                }
            }
        }
    }

}
