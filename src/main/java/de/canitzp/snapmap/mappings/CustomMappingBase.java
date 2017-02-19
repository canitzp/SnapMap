package de.canitzp.snapmap.mappings;

import com.google.common.collect.Lists;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.mappers.MappingsBase;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class CustomMappingBase extends MappingsBase {

    public void addFieldMapping(ClassNode owner, String name, String desc, FieldNode fieldNode){
        this.addFieldMapping(owner.name + " " + name + " " + desc, owner.name + " " + fieldNode.name + " " + desc);
    }

    public void addFieldMappingIfSingle(ClassNode classNode, String unobfFieldName, ClassNode fieldType){
        this.addFieldMappingIfSingle(classNode, unobfFieldName, "L" + fieldType.name + ";");
    }

    public void addFieldMappingIfSingle(ClassNode classNode, String unobfFieldName, String fieldDesc){
        List<FieldNode> fieldNodes = getMatchingFields(classNode, null, fieldDesc);
        if(fieldNodes.size() == 1){
            addFieldMapping(classNode, unobfFieldName, fieldDesc, fieldNodes.get(0));
        }
    }

    public void automapAllFieldNames(ClassNode cn){
        Map<String, List<FieldNode>> descToFields = new HashMap<>();
        for(FieldNode field : cn.fields){
            if(descToFields.containsKey(field.desc)){
                descToFields.get(field.desc).add(field);
            } else {
                descToFields.put(field.desc, Lists.newArrayList(field));
            }
        }
        for(Map.Entry<String, List<FieldNode>> entry : descToFields.entrySet()){
            if(entry.getValue().size() == 1){
                FieldNode field = entry.getValue().get(0);
                String mappedName = getMappedClassName(field.desc);
                if(mappedName != null){
                    addFieldMapping(cn, StringUtils.uncapitalize(mappedName), field.desc, field);
                }
            }
        }
    }

    public String getMappedName(String obfName){
        for(Map.Entry<String, String> entry : DynamicMappings.classMappings.entrySet()){
            if(entry.getValue().equals(obfName)){
                return entry.getKey();
            }
        }
        return obfName;
    }

    public String getMappedClassName(String obfName){
        String classDesc = getMappedName(Type.getType(obfName).getClassName()).replace(".", "/");
        if(classDesc.equals(obfName)){
            return null;
        }
        String[] split = classDesc.split("/");
        return split[split.length-1];
    }

}
