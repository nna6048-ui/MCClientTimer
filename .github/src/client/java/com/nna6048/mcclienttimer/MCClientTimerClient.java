package com.nna6048.mcclienttimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MCClientTimerClient implements ClientModInitializer {

    private static final int DEFAULT_SECONDS = 5 * 60;

    private static int remainingSeconds = DEFAULT_SECONDS;
    private static int tickCounter = 0;
    private static boolean running = false;
    private static boolean hudEnabled = true;

    private static KeyBinding openKey;
    private static KeyBinding startPauseKey;

    @Override
    public void onInitializeClient() {

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mcclienttimer.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.mcclienttimer"
        ));

        startPauseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mcclienttimer.start_pause",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.mcclienttimer"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openKey.wasPressed()) {
                client.setScreen(new TimerScreen());
            }

            while (startPauseKey.wasPressed()) {
                running = !running;
            }

            if (running) {
                tickCounter++;

                if (tickCounter >= 20) {
                    tickCounter = 0;

                    if (remainingSeconds > 0) {
                        remainingSeconds--;
                    }

                    if (remainingSeconds <= 0) {
                        remainingSeconds = 0;
                        running = false;
                    }
                }
            }
        });
    }

    public static void addTime(int seconds) {
        remainingSeconds += seconds;
    }

    public static void reset() {
        remainingSeconds = DEFAULT_SECONDS;
        running = false;
        tickCounter = 0;
    }

    public static String getTimeText() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public static class TimerScreen extends Screen {

        private EditBoxWidget minutesBox;
        private EditBoxWidget secondsBox;

        protected TimerScreen() {
            super(Text.literal("MC Client Timer"));
        }

        @Override
        protected void init() {

            int centerX = this.width / 2;

            minutesBox = new EditBoxWidget(
                    this.textRenderer,
                    centerX - 90,
                    70,
                    80,
                    20,
                    Text.literal("Minutes")
            );

            secondsBox = new EditBoxWidget(
                    this.textRenderer,
                    centerX + 10,
                    70,
                    80,
                    20,
                    Text.literal("Seconds")
            );

            minutesBox.setMaxLength(4);
            secondsBox.setMaxLength(2);

            this.addDrawableChild(minutesBox);
            this.addDrawableChild(secondsBox);

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Start / Pause"),
                    button -> running = !running
            ).dimensions(centerX - 100, 105, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("+1m"),
                    button -> addTime(60)
            ).dimensions(centerX - 100, 130, 60, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("+5m"),
                    button -> addTime(300)
            ).dimensions(centerX - 30, 130, 60, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("+10m"),
                    button -> addTime(600)
            ).dimensions(centerX + 40, 130, 60, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("-1m"),
                    button -> addTime(-60)
            ).dimensions(centerX - 100, 155, 60, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("-5m"),
                    button -> addTime(-300)
            ).dimensions(centerX - 30, 155, 60, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("-10m"),
                    button -> addTime(-600)
            ).dimensions(centerX + 40, 155, 60, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Set Time"),
                    button -> setTimeFromBoxes()
            ).dimensions(centerX - 100, 185, 95, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Reset"),
                    button -> reset()
            ).dimensions(centerX + 5, 185, 95, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("HUD: " + (hudEnabled ? "ON" : "OFF")),
                    button -> {
                        hudEnabled = !hudEnabled;
                        button.setMessage(
                                Text.literal("HUD: " + (hudEnabled ? "ON" : "OFF"))
                        );
                    }
            ).dimensions(centerX - 100, 215, 200, 20).build());
        }

        private void setTimeFromBoxes() {

            try {
                int minutes = Integer.parseInt(minutesBox.getText());
                int seconds = Integer.parseInt(secondsBox.getText());

                if (minutes < 0) {
                    minutes = 0;
                }

                if (seconds < 0) {
                    seconds = 0;
                }

                seconds = Math.min(seconds, 59);

                remainingSeconds = minutes * 60 + seconds;
                running = false;

            } catch (NumberFormatException ignored) {
            }
        }

        @Override
        public void render(
                DrawContext context,
                int mouseX,
                int mouseY,
                float delta
        ) {

            this.renderBackground(context, mouseX, mouseY, delta);

            int centerX = this.width / 2;

            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("⏱ " + getTimeText()),
                    centerX,
                    35,
                    0xFFFFFF
            );

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
