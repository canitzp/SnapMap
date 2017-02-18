package de.canitzp.snapmap.mappings;

import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.dynamicmappings.mappers.MappingsBase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author canitzp
 */
public class ClassStringMapper extends MappingsBase {

    private static final HashMap<String[], String> stringClasses = new HashMap<String[], String>(){{
        put(new String[]{"BLACK", "DARK_RED"}, "util/EnumColor");
    }};

    @Mapping
    public void process() {
        for (String s : DynamicMappings.classDeps.keySet()) {
            for (Map.Entry<String[], String> entry : stringClasses.entrySet()) {
                if (searchConstantPoolForStrings(s, entry.getKey())) {
                    addClassMapping("net/minecraft/" + entry.getValue(), s);
                    break;
                }
            }
        }
    }

}
