package me.kopamed.galacticc.clickgui.component.components.sub;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Module;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

//Your Imports
import me.kopamed.galacticc.clickgui.component.Component;
import me.kopamed.galacticc.clickgui.component.components.Button;
import me.kopamed.galacticc.settings.Setting;

import java.awt.*;

public class Checkbox extends Component {

	private boolean hovered;
	private Setting op;
	private Button parent;
	private int offset;
	private int x;
	private int y;

	public Checkbox(Setting option, Button button, int offset) {
		this.op = option;
		this.parent = button;
		this.x = button.parent.getX() + button.parent.getWidth();
		this.y = button.parent.getY() + button.offset;
		this.offset = offset;
	}

	@Override
	public void renderComponent() {
		// Retrieve checkbox colors
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");

		int checkboxRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Option Rot").getValDouble();
		int checkboxGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Option Green").getValDouble();
		int checkboxBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Option Blau").getValDouble();

		int backgroundColor = new Color(checkboxRed, checkboxGreen, checkboxBlue).getRGB();

		// Draw checkbox container background
		Gui.drawRect(parent.parent.getX() + 2, parent.parent.getY() + offset,
				parent.parent.getX() + parent.parent.getWidth(),
				parent.parent.getY() + offset + 12,
				this.hovered ? backgroundColor : 0xFF222222);

		// Draw text
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(
				this.op.getName(),
				(parent.parent.getX() + 10 + 4) * 2 + 5,
				(parent.parent.getY() + offset + 2) * 2 + 4,
				0xFFFFFFFF);
		GL11.glPopMatrix();

		// Render the checkbox
		int boxColor = this.op.getValBoolean() ? 0xFF666666 : 0xFF999999;
		Gui.drawRect(parent.parent.getX() + 3 + 4, parent.parent.getY() + offset + 3,
				parent.parent.getX() + 9 + 4, parent.parent.getY() + offset + 9,
				boxColor);
	}


	@Override
	public void setOff(int newOff) {
		offset = newOff;
	}

	@Override
	public void updateComponent(int mouseX, int mouseY) {
		this.hovered = isMouseOnButton(mouseX, mouseY);
		this.y = parent.parent.getY() + offset;
		this.x = parent.parent.getX();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if(isMouseOnButton(mouseX, mouseY) && button == 0 && this.parent.open) {
			this.op.setValBoolean(!op.getValBoolean());;
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		if(x > this.x && x < this.x + 88 && y > this.y && y < this.y + 12) {
			return true;
		}
		return false;
	}
}