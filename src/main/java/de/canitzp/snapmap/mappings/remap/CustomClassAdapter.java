package de.canitzp.snapmap.mappings.remap;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * @author canitzp
 */
public class CustomClassAdapter extends ClassRemapper {

    public CustomClassAdapter(ClassVisitor classVisitor, Remapper remapper) {
        super(classVisitor, remapper);
    }

}
