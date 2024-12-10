package me.kopamed.galacticc.clickgui.component;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.clickgui.component.components.Button;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class Frame {

	public ArrayList<Component> components;
	public Category category;
	private boolean open;
	private int width;
	private int y;
	private int x;
	private int barHeight;
	private boolean isDragging;
	public int dragX;
	public int dragY;

	public Frame(Category cat) {
		this.components = new ArrayList<>();
		this.category = cat;
		this.width = 88;
		this.x = 5;
		this.y = 5;
		this.barHeight = 13;
		this.dragX = 0;
		this.open = false;
		this.isDragging = false;
		int tY = this.barHeight;

		for (Module mod : Galacticc.instance.moduleManager.getModulesInCategory(category)) {
			Button modButton = new Button(mod, this, tY);
			this.components.add(modButton);
			tY += 12;
		}
	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	public void setDragX(int dragX) {
		this.dragX = dragX;
	}

	public void setDragY(int dragY) {
		this.dragY = dragY;
	}

	public void setX(int newX) {
		this.x = newX;
	}

	public void setY(int newY) {
		this.y = newY;
	}

	public void setDrag(boolean drag) {
		this.isDragging = drag;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public int getContainerHeight() {
		int height = 0;
		for (Component component : components) {
			height += component.getHeight();
		}
		return height;
	}

	public void renderFrame(FontRenderer fontRenderer) {
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");

		// Fetch container color values
		int containerRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Red").getValDouble();
		int containerGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Green").getValDouble();
		int containerBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Blue").getValDouble();
		int containerAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Alpha").getValDouble();

		// Linie color values (no change)
		int lineRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Rot").getValDouble();
		int lineGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Green").getValDouble();
		int lineBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Blau").getValDouble();
		int lineAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Alpha").getValDouble();

		// Header and gradient values (no change)
		int headerRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Rot").getValDouble();
		int headerGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Green").getValDouble();
		int headerBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Blau").getValDouble();
		int headerAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Alpha").getValDouble();

		int gradientHeaderRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Header Rot").getValDouble();
		int gradientHeaderGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Header Green").getValDouble();
		int gradientHeaderBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Header Blau").getValDouble();

		boolean enableGradientHeader = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Enable Gradient Header").getValBoolean();

		// Render the header (no change)
		if (enableGradientHeader) {
			drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.barHeight,
					new Color(headerRed, headerGreen, headerBlue, headerAlpha).getRGB(),
					new Color(gradientHeaderRed, gradientHeaderGreen, gradientHeaderBlue, headerAlpha).getRGB());
		} else {
			Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.barHeight,
					new Color(headerRed, headerGreen, headerBlue, headerAlpha).getRGB());
		}

		// Render container background
		int containerTop = this.y + this.barHeight; // Below header
		int containerBottom = containerTop + this.getContainerHeight(); // Extends to container height
		Gui.drawRect(this.x, containerTop, this.x + this.width, containerBottom,
				new Color(containerRed, containerGreen, containerBlue, containerAlpha).getRGB());

		// Render the side and enclosing lines (no change)
		Color lineColor = new Color(lineRed, lineGreen, lineBlue, lineAlpha);
		int bottomY = this.y + this.barHeight + this.getContainerHeight();

		// Top line (above header)
		Gui.drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y, lineColor.getRGB());
		// Left line
		Gui.drawRect(this.x - 1, this.y - 1, this.x, bottomY + 1, lineColor.getRGB());
		// Right line
		Gui.drawRect(this.x + this.width, this.y - 1, this.x + this.width + 1, bottomY + 1, lineColor.getRGB());
		// Bottom line (after last module)
		Gui.drawRect(this.x - 1, bottomY, this.x + this.width + 1, bottomY + 1, lineColor.getRGB());

		// Reset OpenGL state before drawing text
		GL11.glEnable(GL11.GL_TEXTURE_2D); // Ensure textures are enabled for text rendering
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the category name and toggle indicator (no change)
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		fontRenderer.drawStringWithShadow(this.category.name(), (this.x + 2) * 2 + 5, (this.y + 2.5f) * 2 + 5, 0xFFFFFFFF);
		fontRenderer.drawStringWithShadow(this.open ? "-" : "+", (this.x + this.width - 10) * 2 + 5, (this.y + 2.5f) * 2 + 5, -1);
		GL11.glPopMatrix();

		// Render components if open (no change)
		if (this.open) {
			for (Component component : components) {
				component.renderComponent();
			}
		}

		// Restore OpenGL state (no change)
		GL11.glDisable(GL11.GL_BLEND);
	}

	public void refresh() {
		int off = this.barHeight;
		for (Component comp : components) {
			comp.setOff(off);
			off += comp.getHeight();
		}
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public void updatePosition(int mouseX, int mouseY) {
		if (this.isDragging) {
			this.setX(mouseX - dragX);
			this.setY(mouseY - dragY);
		}
	}

	public boolean isWithinHeader(int x, int y) {
		return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.barHeight;
	}

	// Utility method to draw a gradient rectangle
	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		// Save current OpenGL states
		boolean wasTextureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean wasAlphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);

		// Prepare for gradient rendering
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		GL11.glBegin(GL11.GL_QUADS);
		// Gradient colors
		float startAlpha = (startColor >> 24 & 255) / 255.0F;
		float startRed = (startColor >> 16 & 255) / 255.0F;
		float startGreen = (startColor >> 8 & 255) / 255.0F;
		float startBlue = (startColor & 255) / 255.0F;

		float endAlpha = (endColor >> 24 & 255) / 255.0F;
		float endRed = (endColor >> 16 & 255) / 255.0F;
		float endGreen = (endColor >> 8 & 255) / 255.0F;
		float endBlue = (endColor & 255) / 255.0F;

		// Apply colors to gradient
		GL11.glColor4f(startRed, startGreen, startBlue, startAlpha);
		GL11.glVertex2f(left, top);
		GL11.glVertex2f(left, bottom);

		GL11.glColor4f(endRed, endGreen, endBlue, endAlpha);
		GL11.glVertex2f(right, bottom);
		GL11.glVertex2f(right, top);

		GL11.glEnd();

		// Restore OpenGL state
		GL11.glShadeModel(GL11.GL_FLAT);
		if (wasAlphaEnabled) GL11.glEnable(GL11.GL_ALPHA_TEST);
		else GL11.glDisable(GL11.GL_ALPHA_TEST);

		if (wasBlendEnabled) GL11.glEnable(GL11.GL_BLEND);
		else GL11.glDisable(GL11.GL_BLEND);

		if (wasTextureEnabled) GL11.glEnable(GL11.GL_TEXTURE_2D);
		else GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}