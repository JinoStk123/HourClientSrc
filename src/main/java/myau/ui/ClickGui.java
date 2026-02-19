package myau.ui;

import myau.HourClient;
import myau.module.Module;
import myau.module.modules.GuiModule;
import myau.property.properties.IntProperty;
import myau.ui.components.CategoryComponent;
import myau.ui.Component;
import myau.ui.components.ModuleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import myau.util.ChatUtil;
import myau.property.Property; // Assuming Property class is available
import myau.module.modules.GuiModule;

import java.util.Objects;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.RenderHelper;
import myau.mixin.IAccessorMinecraft;


public class ClickGui extends GuiScreen {
    private static ClickGui instance;
    private final File configFile = new File("./config/HourClient/", "clickgui.txt");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ArrayList<CategoryComponent> categoryList;
    private ArrayList<Property<?>> properties = new ArrayList<>(); // Added to hold properties
    private static int lastMouseX, lastMouseY;

    public static ClickGui getInstance() {
        return instance == null ? instance = new ClickGui() : instance;
    }

    public static int getMouseX() {
        return lastMouseX;
    }

    public static int getMouseY() {
        return lastMouseY;
    }

    public ClickGui() {
        instance = this;

        List<Module> combatModules = new ArrayList<>();
        List<Module> movementModules = new ArrayList<>();
        List<Module> renderModules = new ArrayList<>();
        List<Module> playerModules = new ArrayList<>();
        List<Module> miscModules = new ArrayList<>();

        for (Module module : HourClient.moduleManager.modules.values()) {
            String name = module.getName().toLowerCase();
            if (name.contains("aura") || name.contains("aim") || name.contains("reach") || name.contains("velocity") || name.contains("criticals") || name.contains("auto") || name.contains("clicker") || name.contains("backtrack") || name.contains("hitbox") || name.contains("keep") || name.contains("swing") || name.contains("blockhit") || name.contains("wtap")) {
                combatModules.add(module);
            } else if (name.contains("speed") || name.contains("fly") || name.contains("jump") || name.contains("sprint") || name.contains("strafe") || name.contains("jesus") || name.contains("walk") || name.contains("eagle")) {
                movementModules.add(module);
            } else if (name.contains("esp") || name.contains("hud") || name.contains("xray") || name.contains("chams") || name.contains("tags") || name.contains("tracer") || name.contains("display") || name.contains("indicator") || name.contains("mark") || name.contains("radar") || name.contains("fullbright") || name.contains("view") || name.contains("effects")) {
                renderModules.add(module);
            } else if (name.contains("inv") || name.contains("chest") || name.contains("stealer") || name.contains("manager") || name.contains("refill") || name.contains("scaffold") || name.contains("nuker") || name.contains("nofall") || name.contains("blink") || name.contains("timer")) {
                playerModules.add(module);
            } else {
                miscModules.add(module);
            }

            ArrayList<Property<?>> moduleProperties = HourClient.propertyManager.properties.get(module.getClass());
            if (moduleProperties != null && !moduleProperties.isEmpty()) {
                for (Property<?> prop : moduleProperties) {
                    if (prop.getOwner() != null && prop.getOwner().equals(module)) {
                        // Keep properties logic
                    }
                }
            }
        }


        this.categoryList = new ArrayList<>();

        int currentX = 10;
        int currentY = 10;

        CategoryComponent combat = new CategoryComponent("Combat", combatModules);
        combat.setX(currentX); combat.setY(currentY);
        categoryList.add(combat);
        currentX += 100;

        CategoryComponent movement = new CategoryComponent("Movement", movementModules);
        movement.setX(currentX); movement.setY(currentY);
        categoryList.add(movement);
        currentX += 100;

        CategoryComponent render = new CategoryComponent("Render", renderModules);
        render.setX(currentX); render.setY(currentY);
        categoryList.add(render);
        currentX += 100;

        CategoryComponent player = new CategoryComponent("Player", playerModules);
        player.setX(currentX); player.setY(currentY);
        categoryList.add(player);
        currentX += 100;

        CategoryComponent misc = new CategoryComponent("Misc", miscModules);
        misc.setX(currentX); misc.setY(currentY);
        categoryList.add(misc);
    }

    @Override
    public void initGui() {
        super.initGui();
        // Populate categories based on modules
        // Example placeholder logic, actual population might be elsewhere
        // This section likely needs to be filled with logic to group modules by category
        // and create CategoryComponent instances.
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        // Handle button actions here
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        GuiModule guiModule = (GuiModule) HourClient.moduleManager.getModule(GuiModule.class);
        Color bgColor = new Color(guiModule.backgroundColor.getValue());
        int alpha = (int) (guiModule.backgroundOpacity.getValue().floatValue() / 100.0f * 255.0f);
        int finalColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), alpha).getRGB();
        
        drawRect(0, 0, this.width, this.height, finalColor);

        for (CategoryComponent category : categoryList) {
            category.handleDrag(mouseX, mouseY);
            category.update(mouseX, mouseY);
            category.render(mc.fontRendererObj);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            if (scroll > 1) {
                scroll = 1;
            }
            if (scroll < -1) {
                scroll = -1;
            }
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            for (CategoryComponent category : categoryList) {
                category.onScroll(mouseX, mouseY, scroll);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (CategoryComponent category : categoryList) {
            if (category.insideArea(mouseX, mouseY)) {
                if (mouseButton == 0) {
                    category.xx = mouseX - category.getX();
                    category.yy = mouseY - category.getY();
                    category.dragging = true;
                    return;
                } else if (mouseButton == 1) {
                    category.setOpened(!category.isOpened());
                    return;
                }
            }
            if (category.isOpened()) {
                for (myau.ui.Component component : category.getModules()) {
                    component.mouseDown(mouseX, mouseY, mouseButton);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (CategoryComponent category : categoryList) {
            category.dragging = false;
            if (category.isOpened()) {
                for (myau.ui.Component component : category.getModules()) {
                    component.mouseReleased(mouseX, mouseY, state);
                }
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (CategoryComponent category : categoryList) {
            if (category.isOpened()) {
                for (myau.ui.Component component : category.getModules()) {
                    component.keyTyped(typedChar, keyCode);
                }
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        // Save config when GUI is closed
        this.save();
        super.onGuiClosed();
    }

    // Load and Save methods for configuration
    public void load() {
        try {
            if (!configFile.exists()) {
                ChatUtil.sendFormatted(String.format("%sConfig file not found (&c&o%s&r). Creating default config...&r", HourClient.clientName, configFile.getName()));
                save();
                return;
            }

            JsonElement parsed = new JsonParser().parse(new BufferedReader(new FileReader(configFile)));
            if (parsed == null || !parsed.isJsonObject()) {
                ChatUtil.sendFormatted(String.format("%sInvalid config format (&c&o%s&r)&r", HourClient.clientName, configFile.getName()));
                return;
            }

            JsonObject jsonObject = parsed.getAsJsonObject();

            // Load UI specific settings like corner radius
            if (jsonObject.has("uiSettings")) {
                JsonObject uiSettings = jsonObject.getAsJsonObject("uiSettings");
                if (uiSettings.has("uiCornerRadius")) {
                    try {
                        ((GuiModule) HourClient.moduleManager.getModule(GuiModule.class)).cornerRadius.setValue(uiSettings.get("uiCornerRadius").getAsInt());
                    } catch (Exception e) {
                        // Handle potential errors if value is not an int or is missing
                    }
                }
            }

            // Load module settings
            for (Module module : HourClient.moduleManager.modules.values()) {
                JsonElement moduleObj = jsonObject.get(module.getName());
                if (moduleObj != null && moduleObj.isJsonObject()) {
                    JsonObject object = moduleObj.getAsJsonObject();

                    ArrayList<Property<?>> list = HourClient.propertyManager.properties.get(module.getClass());
                    if (list != null) {
                        for (Property<?> property : list) {
                            if (object.has(property.getName())) {
                                try {
                                    property.read(object);
                                } catch (Exception e) {
                                    // Log warning but continue loading other properties
                                    ((IAccessorMinecraft) mc).getLogger().warn(String.format("Failed to load property %s for module %s", property.getName(), module.getName()));
                                }
                            }
                        }
                    }

                    if (object.has("toggled")) {
                        JsonElement toggled = object.get("toggled");
                        if (toggled != null && toggled.isJsonPrimitive()) {
                            module.setEnabled(toggled.getAsBoolean());
                        }
                    }

                    if (object.has("key")) {
                        JsonElement key = object.get("key");
                        if (key != null && key.isJsonPrimitive()) {
                            module.setKey(key.getAsInt());
                        }
                    }

                    if (object.has("hidden")) {
                        JsonElement hidden = object.get("hidden");
                        if (hidden != null && hidden.isJsonPrimitive()) {
                            module.setHidden(hidden.getAsBoolean());
                        }
                    }
                }
            }
            ChatUtil.sendFormatted(String.format("%sConfig has been loaded (&a&o%s&r)&r", HourClient.clientName, configFile.getName()));
        } catch (FileNotFoundException e) {
            ChatUtil.sendFormatted(String.format("%sConfig file not found (&c&o%s&r)&r", HourClient.clientName, configFile.getName()));
        } catch (JsonSyntaxException e) {
            ChatUtil.sendFormatted(String.format("%sConfig has invalid JSON syntax (&c&o%s&r)&r", HourClient.clientName, configFile.getName()));
            ((IAccessorMinecraft) mc).getLogger().error("JSON Syntax Error: " + e.getMessage());
        } catch (Exception e) {
            ((IAccessorMinecraft) mc).getLogger().error("Error loading config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be loaded (&c&o%s&r)&r", HourClient.clientName, configFile.getName()));
        }
    }

    public void save() {
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            JsonObject object = new JsonObject();

            // Save UI specific settings like corner radius
            JsonObject uiSettings = new JsonObject();
            uiSettings.addProperty("uiCornerRadius", (Integer) ((GuiModule) HourClient.moduleManager.getModule(GuiModule.class)).cornerRadius.getValue());
            object.add("uiSettings", uiSettings);

            // Save module settings
            for (Module module : HourClient.moduleManager.modules.values()) {
                JsonObject moduleObject = new JsonObject();
                moduleObject.addProperty("toggled", module.isEnabled());
                moduleObject.addProperty("key", module.getKey());
                moduleObject.addProperty("hidden", module.isHidden());

                ArrayList<Property<?>> list = HourClient.propertyManager.properties.get(module.getClass());
                if (list != null) {
                    for (Property<?> property : list) {
                        try {
                            property.write(moduleObject);
                        } catch (Exception e) {
                            ((IAccessorMinecraft) mc).getLogger().warn(String.format("Failed to save property %s for module %s", property.getName(), module.getName()));
                        }
                    }
                }
                object.add(module.getName(), moduleObject);
            }

            PrintWriter printWriter = new PrintWriter(new FileWriter(configFile));
            printWriter.println(gson.toJson(object));
            printWriter.close();
        } catch (IOException e) {
            ((IAccessorMinecraft) mc).getLogger().error("Error saving config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be saved (&c&o%s&r)&r", HourClient.clientName, configFile.getName()));
        }
    }
}
