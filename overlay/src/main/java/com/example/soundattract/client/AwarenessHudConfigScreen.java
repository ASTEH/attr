package com.example.soundattract.client;

import com.example.soundattract.config.ClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AwarenessHudConfigScreen extends Screen {
    private static final float[] ANGLES = {14.0F, 36.0F, 58.0F, 80.0F};
    private static final int TEXTURE_SIZE = 512;

    private static final ResourceLocation[] VISIBILITY_ICONS = {
            texture("visibility_1"), texture("visibility_2"), texture("visibility_3"),
            texture("visibility_4"), texture("visibility_5")
    };
    private static final ResourceLocation[] NOISE_ICONS = {
            texture("noise_0"), texture("noise_1"), texture("noise_2"),
            texture("noise_3"), texture("noise_4")
    };
    private static final ResourceLocation[] SCENT_ICONS = {
            texture("scent_0"), texture("scent_1"), texture("scent_2"),
            texture("scent_3"), texture("scent_4")
    };
    private static final ResourceLocation WINDSHIELD_ICON = texture("windshield");

    private final Screen parent;
    private boolean draggingHud;
    private double dragOffsetX;
    private double dragOffsetY;

    public AwarenessHudConfigScreen(Screen parent) {
        super(Component.translatable("screen.soundattract.hud.title"));
        this.parent = parent;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("soundattract", "textures/hud/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();

        int columnWidth = Math.max(110, (this.width - 32) / 2);
        int left = 12;
        int right = left + columnWidth + 8;
        int compactWidth = Math.max(100, columnWidth);

        addRenderableWidget(new ValueSlider(
                left, 48, compactWidth, 20,
                0.5, 2.0, ClientConfig.HUD_SCALE.get(),
                value -> ClientConfig.HUD_SCALE.set(value),
                value -> Component.translatable("screen.soundattract.hud.scale", Math.round(value * 100.0))));

        addRenderableWidget(new ValueSlider(
                right, 48, compactWidth, 20,
                40.0, 255.0, ClientConfig.HUD_OPACITY.get(),
                value -> ClientConfig.HUD_OPACITY.set((int) Math.round(value)),
                value -> Component.translatable("screen.soundattract.hud.opacity",
                        Math.round(value / 255.0 * 100.0))));

        addRenderableWidget(new ValueSlider(
                left, 72, Math.min(this.width - 24, columnWidth * 2 + 8), 20,
                0.25, 4.0, ClientConfig.MOB_ALERT_SCALE.get(),
                value -> ClientConfig.MOB_ALERT_SCALE.set(value),
                value -> Component.translatable("screen.soundattract.hud.mob_scale",
                        Math.round(value * 100.0))));

        addToggleButton(left, 98, compactWidth, "screen.soundattract.hud.visibility",
                ClientConfig.SHOW_VISIBILITY::get, ClientConfig.SHOW_VISIBILITY::set);
        addToggleButton(right, 98, compactWidth, "screen.soundattract.hud.noise",
                ClientConfig.SHOW_ENVIRONMENTAL_NOISE::get, ClientConfig.SHOW_ENVIRONMENTAL_NOISE::set);
        addToggleButton(left, 122, compactWidth, "screen.soundattract.hud.scent",
                ClientConfig.SHOW_SCENT::get, ClientConfig.SHOW_SCENT::set);
        addToggleButton(right, 122, compactWidth, "screen.soundattract.hud.windshield",
                ClientConfig.SHOW_WIND_SHIELD::get, ClientConfig.SHOW_WIND_SHIELD::set);

        addRenderableWidget(Button.builder(
                Component.translatable("screen.soundattract.hud.reset_position"),
                button -> resetPosition()
        ).bounds(left, 148, compactWidth, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(right, 148, compactWidth, 20).build());
    }

    private void addToggleButton(int x, int y, int width, String labelKey,
                                 Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Button button = Button.builder(toggleMessage(labelKey, getter.get()), pressed -> {
            boolean enabled = !getter.get();
            setter.accept(enabled);
            pressed.setMessage(toggleMessage(labelKey, enabled));
        }).bounds(x, y, width, 20).build();
        addRenderableWidget(button);
    }

    private static Component toggleMessage(String labelKey, boolean enabled) {
        return Component.translatable(labelKey)
                .append(Component.literal(": "))
                .append(Component.translatable(enabled ? "options.on" : "options.off"));
    }

    private void resetPosition() {
        ClientConfig.HUD_POSITION.set(ClientConfig.HudPosition.BOTTOM_LEFT);
        ClientConfig.HUD_OFFSET_X.set(0);
        ClientConfig.HUD_OFFSET_Y.set(0);
        savePosition();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(8, 8, this.width - 8, 174, 0xB010151B);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.soundattract.hud.instructions"),
                12, 31, 0xC8D4E0, false);

        renderHudPreview(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderHudPreview(GuiGraphics graphics) {
        HudLayout layout = getLayout();
        int opacity = Mth.clamp(ClientConfig.HUD_OPACITY.get(), 40, 255);
        int direction = layout.direction();
        int originX = layout.originX();
        int originY = layout.originY();
        int radius = layout.radius();
        int badge = layout.badge();
        int iconSize = layout.iconSize();

        int lineColor = (Math.max(20, opacity / 7) << 24) | 0x4A6A50;
        for (int point = 0; point <= 50; point++) {
            float angle = ANGLES[0] - 3.0F + (ANGLES[3] - ANGLES[0] + 6.0F) * point / 50.0F;
            int x = originX + direction * Math.round(radius * Mth.cos(angle * Mth.DEG_TO_RAD));
            int y = originY - Math.round(radius * Mth.sin(angle * Mth.DEG_TO_RAD));
            graphics.fill(x, y, x + 1, y + 1, lineColor);
        }

        boolean[] visible = {
                ClientConfig.SHOW_VISIBILITY.get(),
                ClientConfig.SHOW_ENVIRONMENTAL_NOISE.get(),
                ClientConfig.SHOW_SCENT.get(),
                ClientConfig.SHOW_WIND_SHIELD.get()
        };

        float visibility = AwarenessClientState.visibility();
        float noise = AwarenessClientState.noise();
        int scent = AwarenessClientState.scent();
        ResourceLocation[] icons = {
                VISIBILITY_ICONS[Mth.clamp((int) (visibility * 5.0F), 0, 4)],
                NOISE_ICONS[Mth.clamp((int) (noise * 5.0F), 0, 4)],
                SCENT_ICONS[Mth.clamp(scent, 0, 4)],
                WINDSHIELD_ICON
        };

        for (int index = 0; index < visible.length; index++) {
            if (!visible[index]) {
                continue;
            }

            float angle = ANGLES[index] * Mth.DEG_TO_RAD;
            int centerX = originX + direction * Math.round(radius * Mth.cos(angle));
            int centerY = originY - Math.round(radius * Mth.sin(angle));
            int color = index == 3 && AwarenessClientState.windshielded() ? 0xFF38F0FF : 0xFF7C98A8;

            drawCircle(graphics, centerX, centerY, badge / 2, (opacity << 24) | (color & 0xFFFFFF));
            drawCircle(graphics, centerX, centerY, Math.max(1, badge / 2 - 2),
                    (Math.min(opacity, 220) << 24) | 0x10151B);

            RenderSystem.enableBlend();
            graphics.setColor(1.0F, 1.0F, 1.0F, opacity / 255.0F);
            graphics.blit(icons[index], centerX - iconSize / 2, centerY - iconSize / 2,
                    0.0F, 0.0F, iconSize, iconSize, TEXTURE_SIZE, TEXTURE_SIZE);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void drawCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        if ((color >>> 24) == 0 || radius <= 0) {
            return;
        }
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(Math.max(0, radius * radius - y * y));
            graphics.fill(centerX - halfWidth, centerY + y,
                    centerX + halfWidth + 1, centerY + y + 1, color);
        }
    }

    private HudLayout getLayout() {
        float scale = ClientConfig.HUD_SCALE.get().floatValue();
        int radius = Math.round(62 * scale);
        int badge = Math.round(24 * scale);
        int iconSize = Math.round(20 * scale);
        int padding = Math.round(8 * scale);
        int extentX = Math.round(radius * Mth.cos(ANGLES[0] * Mth.DEG_TO_RAD)) + badge / 2 + 2;
        int extentY = Math.round(radius * Mth.sin(ANGLES[3] * Mth.DEG_TO_RAD)) + badge / 2 + 2;

        boolean right = ClientConfig.HUD_POSITION.get() == ClientConfig.HudPosition.BOTTOM_RIGHT;
        int insetX = Mth.clamp(padding + ClientConfig.HUD_OFFSET_X.get(),
                padding, Math.max(padding, this.width - extentX));
        int insetY = Mth.clamp(padding + ClientConfig.HUD_OFFSET_Y.get(),
                padding, Math.max(padding, this.height - extentY));
        int originX = right ? this.width - insetX : insetX;
        int originY = this.height - insetY;
        return new HudLayout(originX, originY, right ? -1 : 1,
                radius, badge, iconSize, extentX, extentY, padding);
    }

    private boolean isInsideHud(double mouseX, double mouseY, HudLayout layout) {
        int minX = layout.direction() == 1 ? layout.originX() : layout.originX() - layout.extentX();
        int maxX = layout.direction() == 1 ? layout.originX() + layout.extentX() : layout.originX();
        int minY = layout.originY() - layout.extentY();
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= layout.originY();
    }

    private void moveHudTo(double requestedX, double requestedY) {
        HudLayout layout = getLayout();
        boolean right = requestedX >= this.width / 2.0;
        int minX = right ? layout.extentX() : layout.padding();
        int maxX = right ? this.width - layout.padding() : this.width - layout.extentX();
        if (maxX < minX) {
            int center = this.width / 2;
            minX = center;
            maxX = center;
        }

        int minY = Math.min(layout.extentY(), this.height - layout.padding());
        int maxY = Math.max(minY, this.height - layout.padding());
        int x = Mth.clamp((int) Math.round(requestedX), minX, maxX);
        int y = Mth.clamp((int) Math.round(requestedY), minY, maxY);
        int insetX = right ? this.width - x : x;

        ClientConfig.HUD_POSITION.set(right
                ? ClientConfig.HudPosition.BOTTOM_RIGHT
                : ClientConfig.HudPosition.BOTTOM_LEFT);
        ClientConfig.HUD_OFFSET_X.set(Mth.clamp(insetX - layout.padding(), 0, 4096));
        ClientConfig.HUD_OFFSET_Y.set(Mth.clamp(this.height - y - layout.padding(), 0, 4096));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            HudLayout layout = getLayout();
            if (isInsideHud(mouseX, mouseY, layout)) {
                draggingHud = true;
                dragOffsetX = mouseX - layout.originX();
                dragOffsetY = mouseY - layout.originY();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingHud && button == 0) {
            moveHudTo(mouseX - dragOffsetX, mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingHud && button == 0) {
            draggingHud = false;
            savePosition();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static void savePosition() {
        ClientConfig.HUD_POSITION.save();
        ClientConfig.HUD_OFFSET_X.save();
        ClientConfig.HUD_OFFSET_Y.save();
    }

    private static void saveAllSettings() {
        ClientConfig.HUD_SCALE.save();
        ClientConfig.HUD_OPACITY.save();
        ClientConfig.MOB_ALERT_SCALE.save();
        ClientConfig.SHOW_VISIBILITY.save();
        ClientConfig.SHOW_ENVIRONMENTAL_NOISE.save();
        ClientConfig.SHOW_SCENT.save();
        ClientConfig.SHOW_WIND_SHIELD.save();
        savePosition();
    }

    @Override
    public void onClose() {
        saveAllSettings();
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record HudLayout(int originX, int originY, int direction, int radius, int badge,
                             int iconSize, int extentX, int extentY, int padding) {
    }

    private static final class ValueSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final DoubleFunction<Component> labelFactory;

        private ValueSlider(int x, int y, int width, int height, double min, double max,
                            double current, DoubleConsumer setter,
                            DoubleFunction<Component> labelFactory) {
            super(x, y, width, height, Component.empty(),
                    Mth.clamp((current - min) / (max - min), 0.0, 1.0));
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.labelFactory = labelFactory;
            updateMessage();
        }

        private double actualValue() {
            return this.min + this.value * (this.max - this.min);
        }

        @Override
        protected void updateMessage() {
            setMessage(this.labelFactory.apply(actualValue()));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(actualValue());
        }
    }
}
