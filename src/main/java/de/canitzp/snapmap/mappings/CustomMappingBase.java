package de.canitzp.snapmap.mappings;

import com.google.common.collect.Lists;
import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.dynamicmappings.mappers.MappingsBase;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

import static de.canitzp.snapmap.mappings.ClassNames.*;
/**
 * @author canitzp
 */
public class CustomMappingBase extends MappingsBase {

    public void addFieldMapping(ClassNode owner, String name, String desc, FieldNode fieldNode){
        this.addFieldMapping(DynamicMappings.reverseClassMappings.get(owner.name) + " " + name + " " + desc, owner.name + " " + fieldNode.name + " " + fieldNode.desc);
    }

    public void addMethodMapping(ClassNode owner, String name, String mappedDesc, MethodNode method){
        if(owner.superName != null){
            if(DynamicMappings.reverseClassMappings.containsKey(owner.superName)){
                ClassNode cn = getClassNode(owner.superName);
                for(MethodNode methodNode : cn.methods){
                    if(methodNode.name.equals(method.name) && methodNode.desc.equals(method.desc)){
                        addMethodMapping(cn, name, mappedDesc, method);
                        return;
                    }
                }
            }
        }
        this.addMethodMapping(DynamicMappings.reverseClassMappings.get(owner.name) + " " + name + " " + mappedDesc, owner.name + " " + method.name + " " + method.desc);
    }

    public void addFieldMappingIfSingle(ClassNode classNode, String unobfFieldName, ClassNode fieldType){
        this.addFieldMappingIfSingle(classNode, unobfFieldName, "L" + fieldType.name + ";");
    }

    public void addFieldMappingIfSingle(ClassNode classNode, String unobfFieldName, String obfFieldDesc){
        List<FieldNode> fieldNodes = getMatchingFields(classNode, null, obfFieldDesc);
        if(fieldNodes.size() == 1){
            addFieldMapping(classNode, unobfFieldName, getMappedFieldDesc(obfFieldDesc), fieldNodes.get(0));
        }
    }

    public boolean isMethodMapped(ClassNode classNode, MethodNode methodNode){
        return DynamicMappings.reverseMethodMappings.containsKey(classNode.name + " " + methodNode.name + " " + methodNode.desc);
    }

    public void addMethodIfSingle(ClassNode classNode, String unobfMethodName, String obfDesc){
        List<MethodNode> methodNodes = getMatchingMethods(classNode, null, obfDesc);
        if(methodNodes.size() == 1){
            addMethodMapping(classNode, unobfMethodName, obfDesc, methodNodes.get(0));
        }
    }

    public void addMethodIfContainsString(ClassNode classNode, String mappedMethodName, String... strings){
        List<MethodNode> methods = new ArrayList<>();
        for(MethodNode method : classNode.methods){
            boolean flag = false;
            for(String s : strings){
                if(!getStringsFromMethod(method).contains(s)){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                methods.add(method);
            }
        }
        if(methods.size() == 1){
            addMethodMapping(classNode, mappedMethodName, methods.get(0).desc, methods.get(0));
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

    public void automapAllGetter(ClassNode cn){
        for(MethodNode method : cn.methods){
            if(method.access == Opcodes.ACC_PUBLIC && method.desc.startsWith("()") && method.instructions.size() == 6){
                FieldInsnNode fieldInsnNode = (FieldInsnNode) method.instructions.get(3);
                String mapping = fieldInsnNode.owner + " " + fieldInsnNode.name + " " + fieldInsnNode.desc;
                if(DynamicMappings.reverseFieldMappings.containsKey(mapping)){
                    String unobfName = DynamicMappings.reverseFieldMappings.get(mapping).split(" ")[1];
                    addMethodMapping(cn, "get" + StringUtils.capitalize(unobfName), method.desc, method);
                }
            }
        }
    }

    public void automapAllSetter(ClassNode cn){
        for(MethodNode method : cn.methods){
            if(method.access == Opcodes.ACC_PUBLIC && method.desc.endsWith(")V") && method.instructions.size() == 9){
                Type[] argsTypes = Type.getArgumentTypes(method.desc);
                if(argsTypes.length == 1){
                    FieldInsnNode fieldInsnNode = ((FieldInsnNode)method.instructions.get(4));
                    String obfName = fieldInsnNode.name;
                    if(DynamicMappings.reverseFieldMappings.containsKey(fieldInsnNode.owner + " " + obfName + " " + fieldInsnNode.desc)){
                        String unobfName = DynamicMappings.reverseFieldMappings.get(fieldInsnNode.owner + " " + obfName + " " + fieldInsnNode.desc).split(" ")[1];
                        addMethodMapping(cn, "set" + StringUtils.capitalize(unobfName), method.desc, method);
                    }
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
        return getClassName(classDesc);
    }

    public String getClassName(String deobfClass){
        String[] split = deobfClass.split("/");
        return split[split.length-1];
    }

    public String getMappedFieldDesc(String obfFieldDesc){
        return "L" + DynamicMappings.reverseClassMappings.get(Type.getType(obfFieldDesc).getClassName()) + ";";
    }

    //TODO
    public String getMappedMethodDesc(String obfMethodDesc){
        return obfMethodDesc;
    }

    public List<MethodNode> cleanMethodsMap(List<MethodNode> oldMethods, String returnTypeMapped, String... parameterMapped){
        List<MethodNode> newMethods = new ArrayList<>();
        for(MethodNode method : oldMethods){
            Type retType = Type.getReturnType(method.desc);
            if(retType.getClassName().equals("void") || (returnTypeMapped != null && getMappedName(retType.getClassName()).equals(returnTypeMapped))){
                Type[] params = Type.getArgumentTypes(method.desc);
                boolean flag = true;
                for (int i = 0; i < params.length; i++) {
                    Type type = params[i];
                    if (!getMappedName(type.getClassName()).equals(parameterMapped[i])){
                        flag = false;
                    }
                }
                if(flag){
                    newMethods.add(method);
                }
            }
        }
        return newMethods;
    }

    public List<MethodNode> findMethodsWithSignature(ClassNode node, String signature){
        List<MethodNode> methods = new ArrayList<>();
        for(MethodNode method : node.methods){
            if(signature.equals(method.signature)){
                methods.add(method);
            }
        }
        return methods;
    }

}
