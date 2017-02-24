package de.canitzp.snapmap.mappings;

import net.fybertech.dynamicmappings.Mapping;
import net.fybertech.meddle.MeddleUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * @author canitzp
 */
public class MethodRemapper extends CustomMappingBase {

    @Mapping(depends = "net/minecraft/client/Minecraft")
    public void mapMinecraftClass(){
        ClassNode minecraft = getClassNodeFromMapping("net/minecraft/client/Minecraft");
        if(MeddleUtil.notNull(minecraft)){
            addMethodIfContainsString(minecraft, "run", "Initializing game", "Initialization", "Reported exception thrown!");
            addMethodIfContainsString(minecraft, "createDisplay", "Couldn\'t set pixel format");
            addMethodIfContainsString(minecraft, "setWindowIcon", "icons/icon_16x16.png", "icons/icon_32x32.png", "Couldn\'t set icon");
            addMethodIfContainsString(minecraft, "isJvm64bit", "sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch", "64");
            addMethodIfContainsString(minecraft, "startTimerHackThread", "Timer hack thread");
            addMethodIfContainsString(minecraft, "displayCrashReport", "crash-reports", "yyyy-MM-dd_HH.mm.ss", "-client.txt");
            addMethodIfContainsString(minecraft, "drawSplashScreen", "logo", "Unable to load logo: {}");
            addMethodIfContainsString(minecraft, "checkGLError", "########## GL ERROR ##########", "@ {}", "{}: {}");
            addMethodIfContainsString(minecraft, "shutdownMinecraftApplet", "Stopping!");
            addMethodIfContainsString(minecraft, "runGameLoop", "root", "scheduledExecutables", "tick", "preRenderErrors");
            addMethodIfContainsString(minecraft, "updateDisplay", "display_update");
            addMethodIfContainsString(minecraft, "updateDebugProfilerName", "unspecified", ".");
            addMethodIfContainsString(minecraft, "displayDebugInfo", "##0.00", "ROOT ", "[?] ");
            addMethodIfContainsString(minecraft, "toggleFullscreen", "Couldn\'t toggle fullscreen");
            addMethodIfContainsString(minecraft, "runTick", "gui", "gameMode", "textures", "Updating screen events");
            addMethodIfContainsString(minecraft, "runTickKeyboard", "Manually triggered debug crash");
            addMethodIfContainsString(minecraft, "processKeyF3", "debug.reload_chunks.message", "debug.cycle_renderdistance.message");
            addMethodIfContainsString(minecraft, "processKeyBinds", "/");
            addMethodIfContainsString(minecraft, "debugFeedbackTranslated", "", "[Debug]: ");
            addMethodIfContainsString(minecraft, "middleClickMouse", "Picking on: [{}] {} gave null item");
            addMethodIfContainsString(minecraft, "storeTEInStack", "Owner", "SkullOwner", "(+NBT)");
            addMethodIfContainsString(minecraft, "addGraphicsAndWorldToCrashReport", "Launched Version", "Resource Packs");
            addMethodIfContainsString(minecraft, "addServerStatsToSnooper", "fps", "vsync_enabled", "snooper_partner");
            addMethodIfContainsString(minecraft, "getCurrentAction", "hosting_lan", "singleplayer", "playing_lan","multiplayer", "out_of_game");
            addMethodIfContainsString(minecraft, "addServerTypeToSnooper", "opengl_version", "gl_caps[ARB_compressed_texture_pixel_storage]");
        }
    }

    @Mapping(depends = "net/minecraft/entity/Entity")
    public void mapEntityClass(){
        ClassNode entity = getClassNodeFromMapping("net/minecraft/entity/Entity");
        if(MeddleUtil.notNull(entity)){
            List<MethodNode> methods = getMatchingMethods(entity, Opcodes.ACC_PUBLIC, Type.OBJECT, Type.INT);
            if(methods.size() == 1){
                addMethodMapping(entity, "changeDimension", methods.get(0).desc, methods.get(0));
            }
        }
    }

}
