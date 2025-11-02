package fr.boul2gom.blueprints.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class BlueprintScreen extends HandledScreen<BlueprintScreenHandler> {

    public BlueprintScreen(BlueprintScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.update_size();
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        // Fill the background with a semi-transparent blue color
        context.fill(this.x, this.y, this.x + this.backgroundWidth, this.y + this.backgroundHeight, 0xE61A1A4C);

        // Draw the frame, and its borders
        final int thickness = 2;
        final int border_color = 0xFFFFFFFF;
        context.fill(this.x, this.y, this.x + this.backgroundWidth, this.y + thickness, border_color);
        context.fill(this.x, this.y + this.backgroundHeight - thickness, this.x + this.backgroundWidth, this.y + this.backgroundHeight, border_color);
        context.fill(this.x, this.y, this.x + thickness, this.y + this.backgroundHeight, border_color);
        context.fill(this.x + this.backgroundWidth - thickness, this.y, this.x + this.backgroundWidth, this.y + this.backgroundHeight, border_color);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.update_size();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawBackground(context, delta, mouseX, mouseY);

        // Draw GUI-relative mouse coordinates in a toast-like box at the bottom-right of the GUI
        final int guiX = mouseX - this.x;
        final int guiY = mouseY - this.y;

        final String coords = "Coords: x is " + guiX + ", y is " + guiY;

        final int innerPadding = 4;
        final int outerMargin = 6;
        final int textWidth = this.textRenderer.getWidth(coords);
        final int textHeight = this.textRenderer.fontHeight;

        final int boxWidth = textWidth + innerPadding * 2;
        final int boxHeight = textHeight + innerPadding * 2;

        // Anchor the toast to the bottom-right corner of the GUI instead of the whole screen
        final int boxX = this.x + this.backgroundWidth - outerMargin - boxWidth;
        final int boxY = this.y + this.backgroundHeight - outerMargin - boxHeight;

        // Background (semi-transparent dark)
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xB0000000);
        // Border (light)
        final int borderColor = 0x80FFFFFF;
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, borderColor);
        context.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, borderColor);
        context.fill(boxX, boxY, boxX + 1, boxY + boxHeight, borderColor);
        context.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, borderColor);

        // Text
        context.drawText(this.textRenderer, coords, boxX + innerPadding, boxY + innerPadding, 0xFFFFFFFF, true);

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void update_size() {
        if (this.client == null) this.client = MinecraftClient.getInstance();

        final int screen_width = this.client.getWindow().getScaledWidth();
        final int screen_height = this.client.getWindow().getScaledHeight();

        final int padding = (int)(Math.min(screen_width, screen_height) * 0.03f);

        this.x = padding;
        this.y = padding;
        this.backgroundWidth = screen_width - 2 * padding;
        this.backgroundHeight = screen_height - 2 * padding;
    }
}
