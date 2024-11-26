package me.kopamed.galacticc.clickgui.component;

import java.awt.*;
import java.util.ArrayList;

import me.kopamed.galacticc.Galacticc;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import me.kopamed.galacticc.clickgui.ClickGui;
import me.kopamed.galacticc.clickgui.component.components.Button;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;

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

	public void renderFrame(FontRenderer fontRenderer) {
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");

		// Primary header color
		int headerRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Rot").getValDouble();
		int headerGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Green").getValDouble();
		int headerBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Blau").getValDouble();
		int headerAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Alpha").getValDouble();

		// Gradient header color
		int gradientHeaderRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Header Rot").getValDouble();
		int gradientHeaderGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Header Green").getValDouble();
		int gradientHeaderBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Header Blau").getValDouble();

		boolean enableGradientHeader = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Enable Gradient Header").getValBoolean();

		// Render the header
		if (enableGradientHeader) {
			drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.barHeight,
					new Color(headerRed, headerGreen, headerBlue, headerAlpha).getRGB(),
					new Color(gradientHeaderRed, gradientHeaderGreen, gradientHeaderBlue, headerAlpha).getRGB());
		} else {
			Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.barHeight,
					new Color(headerRed, headerGreen, headerBlue, headerAlpha).getRGB());
		}

		// Reset OpenGL state before drawing text
		GL11.glEnable(GL11.GL_TEXTURE_2D); // Ensure textures are enabled for text rendering
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Draw the category name and toggle indicator
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		fontRenderer.drawStringWithShadow(this.category.name(), (this.x + 2) * 2 + 5, (this.y + 2.5f) * 2 + 5, 0xFFFFFFFF);
		fontRenderer.drawStringWithShadow(this.open ? "-" : "+", (this.x + this.width - 10) * 2 + 5, (this.y + 2.5f) * 2 + 5, -1);
		GL11.glPopMatrix();

		// Render components if open
		if (this.open) {
			for (Component component : components) {
				component.renderComponent();
			}
		}

		// Restore OpenGL state
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
		float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
		float startRed = (float) (startColor >> 16 & 255) / 255.0F;
		float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
		float startBlue = (float) (startColor & 255) / 255.0F;

		float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
		float endRed = (float) (endColor >> 16 & 255) / 255.0F;
		float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
		float endBlue = (float) (endColor & 255) / 255.0F;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glBegin(GL11.GL_QUADS);

		GL11.glColor4f(startRed, startGreen, startBlue, startAlpha);
		GL11.glVertex2f(left, top);
		GL11.glVertex2f(left, bottom);

		GL11.glColor4f(endRed, endGreen, endBlue, endAlpha);
		GL11.glVertex2f(right, bottom);
		GL11.glVertex2f(right, top);

		GL11.glEnd();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}