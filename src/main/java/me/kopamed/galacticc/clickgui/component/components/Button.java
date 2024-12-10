package me.kopamed.galacticc.clickgui.component.components;

import java.awt.Color;
import java.util.ArrayList;

import me.kopamed.galacticc.Galacticc;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

//Your Imports
import me.kopamed.galacticc.clickgui.ClickGui;
import me.kopamed.galacticc.clickgui.component.Component;
import me.kopamed.galacticc.clickgui.component.Frame;
import me.kopamed.galacticc.clickgui.component.components.sub.Checkbox;
import me.kopamed.galacticc.clickgui.component.components.sub.Keybind;
import me.kopamed.galacticc.clickgui.component.components.sub.ModeButton;
import me.kopamed.galacticc.clickgui.component.components.sub.Slider;
import me.kopamed.galacticc.clickgui.component.components.sub.VisibleButton;
import me.kopamed.galacticc.settings.Setting;
import me.kopamed.galacticc.module.Module;

public class Button extends Component {

	public Module mod;
	public Frame parent;
	public int offset;
	private boolean isHovered;
	private ArrayList<Component> subcomponents;
	public boolean open;
	private int height;

public Button(Module mod, Frame parent, int offset) {
			this.mod = mod;
			this.parent = parent;
			this.offset = offset;
			this.subcomponents = new ArrayList<Component>();
			this.open = Galacticc.instance.moduleManager.isModuleOpen(mod); // Retrieve persisted state
			height = 12;

			int opY = offset + 12;
			if (Galacticc.instance.settingsManager.getSettingsByMod(mod) != null) {
				for (Setting s : Galacticc.instance.settingsManager.getSettingsByMod(mod)) {
					if (s.isCombo()) {
						this.subcomponents.add(new ModeButton(s, this, mod, opY));
						opY += 12;
					}
					if (s.isSlider()) {
						this.subcomponents.add(new Slider(s, this, opY));
						opY += 12;
					}
					if (s.isCheck()) {
						this.subcomponents.add(new Checkbox(s, this, opY));
						opY += 12;
					}
				}
			}
			this.subcomponents.add(new Keybind(this, opY));
			this.subcomponents.add(new VisibleButton(this, mod, opY));
		}

	@Override
	public void setOff(int newOff) {
		offset = newOff;
		int opY = offset + 12;
		for(Component comp : this.subcomponents) {
			comp.setOff(opY);
			opY += 12;
		}
	}

	@Override
	public void renderComponent() {
		// Retrieve the ClickGui module for settings
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");
		if (clickGuiModule == null) return; // Ensure the module exists

		// Safely retrieve header colors
		Setting headerRotSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Rot");
		Setting headerGreenSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Green");
		Setting headerBlueSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Blau");
		Setting headerAlphaSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Alpha");

		// Safely retrieve background and gradient settings
		Setting backgroundRedSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Rot");
		Setting backgroundGreenSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Green");
		Setting backgroundBlueSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Blau");
		Setting backgroundAlphaSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Alpha");

		Setting gradientRedSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Modul Rot");
		Setting gradientGreenSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Modul Green");
		Setting gradientBlueSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Modul Blau");
		Setting gradientAlphaSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Gradient Modul Alpha");

		// Safely retrieve button colors
		Setting buttonRedSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Rot");
		Setting buttonGreenSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Green");
		Setting buttonBlueSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Blau");
		Setting buttonAlphaSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Alpha");

		int modulebackgroundRed = backgroundRedSetting != null ? (int) backgroundRedSetting.getValDouble() : 0;
		int modulebackgroundGreen = backgroundGreenSetting != null ? (int) backgroundGreenSetting.getValDouble() : 0;
		int modulebackgroundBlue = backgroundBlueSetting != null ? (int) backgroundBlueSetting.getValDouble() : 0;
		int modulebackgroundAlpha = backgroundAlphaSetting != null ? (int) backgroundAlphaSetting.getValDouble() : 255;

		int gradientRed = gradientRedSetting != null ? (int) gradientRedSetting.getValDouble() : 255;
		int gradientGreen = gradientGreenSetting != null ? (int) gradientGreenSetting.getValDouble() : 255;
		int gradientBlue = gradientBlueSetting != null ? (int) gradientBlueSetting.getValDouble() : 255;
		int gradientAlpha = gradientAlphaSetting != null ? (int) gradientAlphaSetting.getValDouble() : 255;

		// Check for null and assign default values if necessary
		int headerRed = headerRotSetting != null ? (int) headerRotSetting.getValDouble() : 255;
		int headerGreen = headerGreenSetting != null ? (int) headerGreenSetting.getValDouble() : 255;
		int headerBlue = headerBlueSetting != null ? (int) headerBlueSetting.getValDouble() : 255;
		int headerAlpha = headerAlphaSetting != null ? (int) headerAlphaSetting.getValDouble() : 255;

		int buttonRed = buttonRedSetting != null ? (int) buttonRedSetting.getValDouble() : 100;
		int buttonGreen = buttonGreenSetting != null ? (int) buttonGreenSetting.getValDouble() : 100;
		int buttonBlue = buttonBlueSetting != null ? (int) buttonBlueSetting.getValDouble() : 100;
		int buttonAlpha = buttonAlphaSetting != null ? (int) buttonAlphaSetting.getValDouble() : 255;

		boolean enableGradient = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Enable Gradient Modul").getValBoolean();

		// Colors for background and gradient
		int backgroundColor = new Color(modulebackgroundRed, modulebackgroundGreen, modulebackgroundBlue, modulebackgroundAlpha).getRGB();
		int gradientColor = new Color(gradientRed, gradientGreen, gradientBlue, gradientAlpha).getRGB();

		// Hover color
		int hoverColor = new Color(buttonRed, buttonGreen, buttonBlue, buttonAlpha).getRGB();

		// Check hover state
		boolean isHovered = this.isHovered;

		// Determine drawing logic
		if (enableGradient) {
			if (isHovered) {
				Gui.drawRect(
						parent.getX(),
						parent.getY() + offset,
						parent.getX() + parent.getWidth(),
						parent.getY() + offset + 12,
						hoverColor
				);
			} else {
				drawGradientRect(
						parent.getX(),
						parent.getY() + offset,
						parent.getX() + parent.getWidth(),
						parent.getY() + offset + 12,
						backgroundColor,
						gradientColor
				);
			}
		} else {
			int colorToDraw = isHovered ? hoverColor : backgroundColor;
			Gui.drawRect(
					parent.getX(),
					parent.getY() + offset,
					parent.getX() + parent.getWidth(),
					parent.getY() + offset + 12,
					colorToDraw
			);
		}

		// Render the text for the module name
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
				this.mod.getName(),
				(parent.getX() + 2) * 2,
				(parent.getY() + offset + 2) * 2 + 4,
				0xFFFFFFFF // Text color (sliders for text color can be added here if needed)
		);

		// Render the toggle indicator
		if (this.subcomponents.size() > 2) {
			Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
					this.open ? "-" : "+",
					(parent.getX() + parent.getWidth() - 10) * 2,
					(parent.getY() + offset + 2) * 2 + 4,
					0xFFFFFFFF
			);
		}
		GL11.glPopMatrix();

		// Render subcomponents if the button is open
		if (this.open) {
			for (Component comp : subcomponents) {
				comp.renderComponent();
			}
		}
	}

	// Utility method for drawing a gradient rectangle
	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		boolean wasTextureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean wasAlphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		GL11.glBegin(GL11.GL_QUADS);
		float startAlpha = (startColor >> 24 & 255) / 255.0F;
		float startRed = (startColor >> 16 & 255) / 255.0F;
		float startGreen = (startColor >> 8 & 255) / 255.0F;
		float startBlue = (startColor & 255) / 255.0F;

		float endAlpha = (endColor >> 24 & 255) / 255.0F;
		float endRed = (endColor >> 16 & 255) / 255.0F;
		float endGreen = (endColor >> 8 & 255) / 255.0F;
		float endBlue = (endColor & 255) / 255.0F;

		GL11.glColor4f(startRed, startGreen, startBlue, startAlpha);
		GL11.glVertex2f(left, top);
		GL11.glVertex2f(left, bottom);

		GL11.glColor4f(endRed, endGreen, endBlue, endAlpha);
		GL11.glVertex2f(right, bottom);
		GL11.glVertex2f(right, top);

		GL11.glEnd();

		GL11.glShadeModel(GL11.GL_FLAT);
		if (wasAlphaEnabled) GL11.glEnable(GL11.GL_ALPHA_TEST);
		if (wasBlendEnabled) GL11.glEnable(GL11.GL_BLEND);
		if (wasTextureEnabled) GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public int getHeight() {
		if(this.open) {
			return (12 * (this.subcomponents.size() + 1));
		}
		return 12;
	}

	@Override
	public void updateComponent(int mouseX, int mouseY) {
		this.isHovered = isMouseOnButton(mouseX, mouseY);
		if(!this.subcomponents.isEmpty()) {
			for(Component comp : this.subcomponents) {
				comp.updateComponent(mouseX, mouseY);
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (isMouseOnButton(mouseX, mouseY) && button == 0) {
			this.mod.toggle();
		}
		if (isMouseOnButton(mouseX, mouseY) && button == 1) {
			this.open = !this.open;
			Galacticc.instance.moduleManager.setModuleOpen(this.mod, this.open); // Save new state
			this.parent.refresh();
		}
		for (Component comp : this.subcomponents) {
			comp.mouseClicked(mouseX, mouseY, button);
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		for(Component comp : this.subcomponents) {
			comp.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void keyTyped(char typedChar, int key) {
		for(Component comp : this.subcomponents) {
			comp.keyTyped(typedChar, key);
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		if(x > parent.getX() && x < parent.getX() + parent.getWidth() && y > this.parent.getY() + this.offset && y < this.parent.getY() + 12 + this.offset) {
			return true;
		}
		return false;
	}
}