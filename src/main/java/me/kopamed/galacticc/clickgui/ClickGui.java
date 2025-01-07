package me.kopamed.galacticc.clickgui;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.clickgui.component.Component;
import me.kopamed.galacticc.clickgui.component.Frame;
import me.kopamed.galacticc.clickgui.component.components.Button;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class ClickGui extends GuiScreen {

	public static ArrayList<Frame> frames;
	public static int color = -1;

	// Initial position of the description box
	private int descBoxX = -1; // Default: centered
	private int descBoxY = -1; // Default: bottom-middle

	// Dragging state
	private boolean draggingDescBox = false;
	private int dragOffsetX;
	private int dragOffsetY;

	public ClickGui() {
		this.frames = new ArrayList<>();
		int frameX = 35;// center stuff
		for (Category category : Category.values()) {
			Frame frame = new Frame(category);
			frame.setX(frameX);
			frames.add(frame);
			frameX += frame.getWidth() + 5;
		}
	}

	public ArrayList<Frame> getFrames() {
		return frames;
	}

	public Frame getFrameByCategory(Category category) {
		for (Frame frame : frames) {
			if (frame.getCategory() == category) {
				return frame;
			}
		}
		return null;
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
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");

		// Background Gradient
		int red = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hintergrund2 Rot").getValDouble();
		int green = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hintergrund2 Green").getValDouble();
		int blue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hintergrund2 Blau").getValDouble();
		int alpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hintergrund2 Alpha").getValDouble();
		drawGradientRect(0, 0, this.width, this.height,
				new Color(red, green, blue, 0).getRGB(),
				new Color(red, green, blue, alpha).getRGB());

		// Update position of the description box while dragging
		if (draggingDescBox) {
			descBoxX = mouseX - dragOffsetX;
			descBoxY = mouseY - dragOffsetY;
		}

		// Draw Frames
		for (Frame frame : frames) {
			frame.renderFrame(this.fontRenderer);
			frame.updatePosition(mouseX, mouseY);
			if (frame.isOpen()) {
				frame.getComponents().forEach(c -> c.updateComponent(mouseX, mouseY));
			}
		}

		// Render Description Section
		renderDescriptionSection(mouseX, mouseY);
	}

	private void renderDescriptionSection(int mouseX, int mouseY) {
		Module clickGuiModule = Galacticc.instance.moduleManager.getModule("Menu");

		// Fetch colors
		int headerRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Rot").getValDouble();
		int headerGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Green").getValDouble();
		int headerBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Blau").getValDouble();
		int headerAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Header Alpha").getValDouble();

		int containerRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Red").getValDouble();
		int containerGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Green").getValDouble();
		int containerBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Blue").getValDouble();
		int containerAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Hinter-Linie Alpha").getValDouble();

		int lineRed = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Rot").getValDouble();
		int lineGreen = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Green").getValDouble();
		int lineBlue = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Blau").getValDouble();
		int lineAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(clickGuiModule, "Linie Alpha").getValDouble();

		if (descBoxX == -1) {
			descBoxX = (this.width / 2) - 125;
			descBoxY = this.height - 100;
		}

		Module hoveredModule = null;
		for (Frame frame : frames) {
			for (Component component : frame.getComponents()) {
				if (component instanceof Button) {
					Button button = (Button) component;
					if (button.isMouseOnButton(mouseX, mouseY)) {
						hoveredModule = button.mod;
						break;
					}
				}
			}
		}

		String description = (hoveredModule != null)
				? hoveredModule.getDescription()
				: "Halte deinen Mauszeiger ueber ein Modul um zu wissen was es tut.";

		int containerWidth = 370;
		int headerHeight = 20;

		Gui.drawRect(descBoxX, descBoxY, descBoxX + containerWidth, descBoxY + headerHeight,
				new Color(headerRed, headerGreen, headerBlue, headerAlpha).getRGB());
		this.fontRenderer.drawStringWithShadow("Beschreibung", descBoxX + 5, descBoxY + 5, 0xFFFFFF);

		int descriptionY = descBoxY + headerHeight + 5;
		ArrayList<String> lines = parseDescriptionIntoLines(description, containerWidth);

		int totalHeight = lines.size() * 10 + 10;
		Gui.drawRect(descBoxX, descriptionY - 5, descBoxX + containerWidth, descriptionY + totalHeight,
				new Color(containerRed, containerGreen, containerBlue, containerAlpha).getRGB());

		for (String line : lines) {
			this.fontRenderer.drawStringWithShadow(line, descBoxX + 5, descriptionY, 0xFFFFFF);
			descriptionY += 10;
		}

		Color lineColor = new Color(lineRed, lineGreen, lineBlue, lineAlpha);
		Gui.drawRect(descBoxX - 1, descBoxY - 1, descBoxX + containerWidth + 1, descBoxY, lineColor.getRGB());
		Gui.drawRect(descBoxX - 1, descBoxY - 1, descBoxX, descriptionY + 1, lineColor.getRGB());
		Gui.drawRect(descBoxX + containerWidth, descBoxY - 1, descBoxX + containerWidth + 1, descriptionY + 10, lineColor.getRGB());
		Gui.drawRect(descBoxX - 1, descriptionY, descBoxX + containerWidth + 1, descriptionY + 10, lineColor.getRGB());
	}

	private ArrayList<String> parseDescriptionIntoLines(String description, int maxWidth) {
		ArrayList<String> lines = new ArrayList<>();
		for (String section : description.split("\\|")) { // Break by '|'
			for (String sentence : section.split("\\.")) { // Break by '.'
				if (!sentence.trim().isEmpty()) {
					lines.add(sentence.trim());
				}
			}
		}
		return lines;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		// Check if the click is within the description box header
		int containerWidth = 250;
		int headerHeight = 20;
		if (mouseX >= descBoxX && mouseX <= descBoxX + containerWidth &&
				mouseY >= descBoxY && mouseY <= descBoxY + headerHeight) {
			if (mouseButton == 0) { // Left click starts dragging
				draggingDescBox = true;
				dragOffsetX = mouseX - descBoxX;
				dragOffsetY = mouseY - descBoxY;
				return; // Prevent further processing
			}
		}

		// Fallback to existing frame logic
		for (Frame frame : frames) {
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
		// Stop dragging the description box
		draggingDescBox = false;

		// Stop dragging frames
		for (Frame frame : frames) {
			frame.setDrag(false);
		}

		// Pass the release event to components
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
		return false;
	}//was true before, test in MP.
}