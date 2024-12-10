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
		this.open = false;
		height = 12;
		int opY = offset + 12;
		if(Galacticc.instance.settingsManager.getSettingsByMod(mod) != null) {
			for(Setting s : Galacticc.instance.settingsManager.getSettingsByMod(mod)){
				if(s.isCombo()){
					this.subcomponents.add(new ModeButton(s, this, mod, opY));
					opY += 12;
				}
				if(s.isSlider()){
					this.subcomponents.add(new Slider(s, this, opY));
					opY += 12;
				}
				if(s.isCheck()){
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

		// Check for null and assign default values if necessary
		int headerRed = headerRotSetting != null ? (int) headerRotSetting.getValDouble() : 255;
		int headerGreen = headerGreenSetting != null ? (int) headerGreenSetting.getValDouble() : 255;
		int headerBlue = headerBlueSetting != null ? (int) headerBlueSetting.getValDouble() : 255;
		int headerAlpha = headerAlphaSetting != null ? (int) headerAlphaSetting.getValDouble() : 255;

		// Safely retrieve background colors
		Setting backgroundRedSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Rot");
		Setting backgroundGreenSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Green");
		Setting backgroundBlueSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Blau");
		Setting backgroundAlphaSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul Alpha");

		int backgroundRed = backgroundRedSetting != null ? (int) backgroundRedSetting.getValDouble() : 0;
		int backgroundGreen = backgroundGreenSetting != null ? (int) backgroundGreenSetting.getValDouble() : 0;
		int backgroundBlue = backgroundBlueSetting != null ? (int) backgroundBlueSetting.getValDouble() : 0;
		int backgroundAlpha = backgroundAlphaSetting != null ? (int) backgroundAlphaSetting.getValDouble() : 255;

		// Safely retrieve button colors
		Setting buttonRedSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Rot");
		Setting buttonGreenSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Green");
		Setting buttonBlueSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Blau");
		Setting buttonAlphaSetting = Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Modul-Hover Alpha");

		int buttonRed = buttonRedSetting != null ? (int) buttonRedSetting.getValDouble() : 100;
		int buttonGreen = buttonGreenSetting != null ? (int) buttonGreenSetting.getValDouble() : 100;
		int buttonBlue = buttonBlueSetting != null ? (int) buttonBlueSetting.getValDouble() : 100;
		int buttonAlpha = buttonAlphaSetting != null ? (int) buttonAlphaSetting.getValDouble() : 255;

		// Convert slider values to Color objects
		int backgroundColor = new Color(backgroundRed, backgroundGreen, backgroundBlue, backgroundAlpha).getRGB();
		int buttonColor = new Color(buttonRed, buttonGreen, buttonBlue, buttonAlpha).getRGB();

		// Determine the button's background color based on hover state
		int drawColor = this.isHovered ? buttonColor : backgroundColor;

		// Draw the button's background
		Gui.drawRect(
				parent.getX(),
				parent.getY() + offset,
				parent.getX() + parent.getWidth(),
				parent.getY() + offset + 12,
				drawColor
		);

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
		if(isMouseOnButton(mouseX, mouseY) && button == 0) {
			this.mod.toggle();
		}
		if(isMouseOnButton(mouseX, mouseY) && button == 1) {
			this.open = !this.open;
			this.parent.refresh();
		}
		for(Component comp : this.subcomponents) {
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

