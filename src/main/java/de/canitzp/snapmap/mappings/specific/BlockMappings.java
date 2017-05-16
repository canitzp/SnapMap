package de.canitzp.snapmap.mappings.specific;

import de.canitzp.snapmap.mappings.CustomMappingBase;
import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.canitzp.snapmap.mappings.ClassNames.*;

/**
 * @author canitzp
 */
public class BlockMappings extends CustomMappingBase {

    @Mapping(
            depends = {BLOCK_REED, BLOCK}
    )
    public boolean processBlock() {
        ClassNode reed = getClassNodeFromMapping(BLOCK_REED);
        ClassNode block = getClassNodeFromMapping(BLOCK);
        if (MeddleUtil.notNull(reed)) {
            List<MethodNode> methods = getMatchingMethods(reed, Opcodes.ACC_PUBLIC, Type.OBJECT, Type.OBJECT, Type.OBJECT, Type.OBJECT, Type.OBJECT);
            if (methods.size() == 1) {
                String boundsClass = Type.getReturnType(methods.get(0).desc).getClassName();
                // // EnumBoundingBoxType
                addClassMapping(ENUM_BOUNDING_BOX_TYPES, boundsClass);
                // Block.getBoundingBoxType(IBlockAccess, IBlockState, BlockPos, EnumFacing)EnumBoundingBoxType
                addMethodMapping(reed, "getBoundingBoxType", "(L" + IBLOCK_ACCESS + ";L" + IBLOCK_STATE + ";L" + BLOCK_POS + ";L" + ENUM_FACING + ";)L" + ENUM_BOUNDING_BOX_TYPES + ";", methods.get(0));
                for (AbstractInsnNode node : methods.get(0).instructions.toArray()) {
                    if (node.getOpcode() == Opcodes.GETSTATIC) {
                        // EnumBoundingBoxType.UNDEFINED
                        addFieldMapping(ENUM_BOUNDING_BOX_TYPES + " UNDEFINED L" + ENUM_BOUNDING_BOX_TYPES + ";", (FieldInsnNode) node);
                    }
                }
                // EnumBoundingBoxType.SOLID
                MethodNode method = getMethodNodeFromMapping(block, BLOCK + " getBoundingBoxType (L" + IBLOCK_ACCESS + ";L" + IBLOCK_STATE + ";L" + BLOCK_POS + ";L" + ENUM_FACING + ";)L" + ENUM_BOUNDING_BOX_TYPES + ";");
                if(method != null){
                    for (AbstractInsnNode node : method.instructions.toArray()) {
                        if (node.getOpcode() == Opcodes.GETSTATIC) {
                            addFieldMapping(ENUM_BOUNDING_BOX_TYPES + " SOLID L" + ENUM_BOUNDING_BOX_TYPES + ";", (FieldInsnNode) node);
                        }
                    }
                }
            }
        }
        return false;
    }

}
