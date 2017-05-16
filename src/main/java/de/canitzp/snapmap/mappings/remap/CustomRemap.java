package de.canitzp.snapmap.mappings.remap;

import net.fybertech.dynamicmappings.DynamicRemap;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static de.canitzp.snapmap.mappings.ClassNames.*;

/**
 * @author canitzp
 */
public class CustomRemap extends DynamicRemap {

    private static final Map<String, String> paramNames = new HashMap<String, String>(){{
        put("L" + BLOCK_POS + ";", "pos");
        put("L" + IBLOCK_STATE + ";", "state");
        put("L" + IBLOCK_ACCESS + ";", "blockAccess");
        put("L" + WORLD + ";", "world");
    }};

    public Map<String, String> classMappings;
    public Map<String, String> fieldMappings;
    public Map<String, String> methodMappings;

    public CustomRemap(Map<String, String> cm, Map<String, String> fm, Map<String, String> mm) {
        super(cm, fm, mm);
        this.classMappings = cm;
        this.fieldMappings = fm;
        this.methodMappings = mm;
    }

    public boolean isObfInner(String s) {
        try {
            Integer.parseInt(s);
            return false;
        } catch (NumberFormatException var3) {
            return true;
        }
    }

    public ClassNode remapClass(ClassReader reader) {
        boolean showDebug = false;
        ClassNode cn = new ClassNode();
        reader.accept(new CustomClassAdapter(cn, new CustomRemapper(this, cn, showDebug)), 8);
        Iterator var4 = cn.methods.iterator();

        while(true) {
            MethodNode method;
            int paramCount;
            int varCount;
            Map<String, Integer> paramCountSpecific = new HashMap<>();
            do {
                if(!var4.hasNext()) {
                    if(showDebug) {
                        System.out.println(cn.name);
                        var4 = cn.methods.iterator();

                        while(var4.hasNext()) {
                            method = (MethodNode)var4.next();
                            System.out.println("  " + method.name + " " + method.desc);
                        }
                    }

                    return cn;
                }

                method = (MethodNode)var4.next();
                paramCount = 0;
                varCount = 0;
                paramCountSpecific.clear();
            } while(method.localVariables == null);

            for (LocalVariableNode lvn : method.localVariables) {
                if (lvn.name.equals("☃")) {
                    if (lvn.start == method.instructions.getFirst()) {
                        if(paramNames.containsKey(lvn.desc)){
                            if(!paramCountSpecific.containsKey(lvn.desc)){
                                paramCountSpecific.put(lvn.desc, 0);
                            }
                            if(paramCountSpecific.get(lvn.desc) == 0){
                                lvn.name = paramNames.get(lvn.desc);
                                paramCountSpecific.put(lvn.desc, 1);
                            } else if(paramCountSpecific.get(lvn.desc) == 1){
                                lvn.name = "other" + StringUtils.capitalize(paramNames.get(lvn.desc));
                                paramCountSpecific.put(lvn.desc, 2);
                            } else {
                                lvn.name = paramNames.get(lvn.desc) + paramCount++;
                            }
                        } else {
                            lvn.name = "param" + paramCount++;
                        }
                    } else {
                        lvn.name = "var" + varCount++;
                    }
                }
            }
        }
    }

}
