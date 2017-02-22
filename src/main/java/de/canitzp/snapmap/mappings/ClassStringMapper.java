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
        put(new String[]{"name", "ip", "acceptTextures"}, "client/multiplayer/SaveData");
        put(new String[]{"MC|PickItem"}, "client/multiplayer/PlayerControllerMP");
        put(new String[]{"System Details", "JVM Flags", "Operating System"}, "crash/CrashReport");
        put(new String[]{"Snooper Timer", "http://snoop.minecraft.net/", "os_architecture"}, "profiler/Snooper");
        put(new String[]{"default", "slim", "Rendering entity in world", "Entity being rendered"}, "client/renderer/entity/RenderManager");
        put(new String[]{"textures/map/map_background.png", "textures/misc/underwater.png", "minecraft:blocks/fire_layer_1"}, "client/renderer/ItemRenderer");
        put(new String[]{"textures/particle/particles.png", "Ticking Particle", "Particle Type"}, "client/particle/ParticleManager");
        put(new String[]{"textures/environment/rain.png", "textures/environment/snow.png", "shaders/post/fxaa.json", "shaders/post/notch.json"}, "client/renderer/EntityRenderer");
        put(new String[]{"hotbar.nbt", "Failed to load creative mode options"}, "util/CreativeInventorySaver");
        put(new String[]{"NETWORK", "disconnect.endOfStream", "Set listener of {} to {}", "Disabled auto read"}, "network/NetworkManager");
        put(new String[]{"Something\'s taking too long! \'{}\' took aprox {} ms", "[UNKNOWN]"}, "profiler/Profiler");
        put(new String[]{"Metadata section name cannot be null", "Don\'t know how to handle metadata section \'", "\' - expected object, found "}, "client/resources/data/MetadataSerializer");
        put(new String[]{"minecraft", "realms", "/assets/", "pack.png", "Default"}, "client/resources/DefaultResourcePack");
        put(new String[]{"^[a-fA-F0-9]{40}$", "textures/misc/unknown_pack.png", "Removed selected resource pack {} because it\'s no longer compatible"}, "client/resources/ResourcePackRepository");
        put(new String[]{"language", "Unable to parse language metadata section of resourcepack: {}", "en_us"}, "client/resources/LanguageManager");
        put(new String[]{"GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT", "glCheckFramebufferStatus returned unknown status:"}, "client/shader/Framebuffer");
        put(new String[]{"sounds.json", "Invalid sounds.json", "Not having sound event for: {}"}, "client/audio/SoundHandler");
        put(new String[]{"skins/", "xx"}, "client/resources/SkinManager");
        put(new String[]{"os.name", "sunos", "Error executing task"}, "util/Util");
        put(new String[]{"commands.generic.player.unspecified", "commands.tellraw.jsonException", "setLenient"}, "command/CommandBase");
        put(new String[]{"invulnerable", "flying", "mayfly", "instabuild"}, "entity/player/PlayerCapabilities");
        put(new String[]{"Using ARB_multitexture.\n", "Using GL 1.3 multitexturing.\n", "Using ARB_texture_env_combine.\n"}, "client/renderer/OpenGlHelper");
    }};

    @Mapping
    public void process() {
        for (Map.Entry<String[], String> entry : stringClasses.entrySet()) {
            for (String s : DynamicMappings.classDeps.keySet()) {
                if (!DynamicMappings.classMappings.containsValue(s) && searchConstantPoolForStrings(s, entry.getKey())) { // check if the class isn't already remapped
                    addClassMapping("net/minecraft/" + entry.getValue(), s);
                    break;
                }
            }
        }
    }

}
