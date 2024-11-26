package me.kopamed.galacticc.clickgui;

import java.io.IOException;
import java.util.ArrayList;

import me.kopamed.galacticc.Galacticc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import me.kopamed.galacticc.clickgui.component.Component;
import me.kopamed.galacticc.clickgui.component.Frame;
import me.kopamed.galacticc.module.Category;

public class ClickGui extends GuiScreen {

	public static ArrayList<Frame> frames;
	public static int color = -1;

	public ClickGui() {
		this.frames = new ArrayList<>();
		int frameX = 5;
		for (Category category : Category.values()) {
			Frame frame = new Frame(category);
			frame.setX(frameX);
			frames.add(frame);
			frameX += frame.getWidth() + 1;
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	@Override
	public void initGui() {
		// Initialize any necessary data
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		for (Frame frame : frames) {
			frame.renderFrame(this.fontRendererObj);
			frame.updatePosition(mouseX, mouseY);
			if (frame.isOpen()) {
				frame.getComponents().forEach(c -> c.updateComponent(mouseX, mouseY));
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (Frame frame : frames) {
			// Pass mouse clicks to components even if they are not in the frame header
			if (frame.isWithinHeader(mouseX, mouseY)) {
				if (mouseButton == 0) {
					frame.setDrag(true);
					frame.setDragX(mouseX - frame.getX());
					frame.setDragY(mouseY - frame.getY());
				} else if (mouseButton == 1) {
					frame.setOpen(!frame.isOpen());
				}
			} else if (frame.isOpen()) {
				for (Component component : frame.getComponents()) {
					component.mouseClicked(mouseX, mouseY, mouseButton); // Pass events to subcomponents
				}
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1) {
			this.mc.displayGuiScreen(null);
			return;
		}
		for (Frame frame : frames) {
			if (frame.isOpen()) {
				for (Component component : frame.getComponents()) {
					component.keyTyped(typedChar, keyCode);
				}
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		for (Frame frame : frames) {
			frame.setDrag(false);
		}
		for (Frame frame : frames) {
			if (frame.isOpen()) {
				for (Component component : frame.getComponents()) {
					component.mouseReleased(mouseX, mouseY, state);
				}
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
}