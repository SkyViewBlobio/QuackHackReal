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
		// Retrieve colors
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");

		int headerRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Rot").getValDouble();
		int headerGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Green").getValDouble();
		int headerBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Blau").getValDouble();
		int headerAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Alpha").getValDouble();

		int backgroundColor = new Color(headerRed, headerGreen, headerBlue, headerAlpha).getRGB();

		// Draw background
		Gui.drawRect(parent.getX(), parent.getY() + offset,
				parent.getX() + parent.getWidth(),
				parent.getY() + offset + 12,
				this.isHovered ? backgroundColor : 0xFF222222);

		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
				this.mod.getName(),
				(parent.getX() + 2) * 2,
				(parent.getY() + offset + 2) * 2 + 4,
				0xFFFFFFFF);

		// Toggle indicator
		if (this.subcomponents.size() > 2) {
			Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
					this.open ? "-" : "+",
					(parent.getX() + parent.getWidth() - 10) * 2,
					(parent.getY() + offset + 2) * 2 + 4,
					0xFFFFFFFF);
		}
		GL11.glPopMatrix();

		// Render subcomponents
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