## QuackHack
- ein 1.8.9 MC-client für mich und meine freundin.

> [!CAUTION]
> Client Structure Guidelines for Minecraft 1.8.9 Forge

This document outlines the structure and coding practices I follow when developing my Minecraft 1.8.9 Forge client. The goal is to ensure readability, maintainability, and adherence to good coding practices throughout the project.

### 1. Line Length Limitation

To improve readability, no line of code will exceed 80 characters. For example:

- Instead of writing a long line like this:
  ```java
  int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();

 * 2. **Settings Initialization:** 
 *    When initializing settings or instances, I will break the lines after key assignments 
 *    for better readability. For example:
 *    - Instead of writing the entire line in one go:
 *      java
 *      Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -screenHeight, screenHeight, true));
 *      I will split it after the `new Setting` keyword:
 *      java
 *      Galacticc.instance.settingsManager.rSetting(
 *          new Setting("Y Offset", this, 0, -screenHeight, screenHeight, true)
 *      );
 *
 * 3. **Booleans Assignment:** 
 *    Booleans will also be split after the `=` operator for clarity. For example:
 *    - Instead of writing:
 *      java
 *      boolean showFPS = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
 *      
 *      I will structure it as:
 *      java
 *      boolean showFPS = 
 *          Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
 *
 * 4. **Helpers and Reflection:** 
 *    I will leverage helper methods and reflection to access private Minecraft fields, 
 *    ensuring clean and efficient code while keeping field access encapsulated.
 *
 * 5. **Module Categorization:** 
 *    Modules specified in the module manager will be categorized by their respective GUI categories. 
 *    For example, movement-related modules will be grouped under a "Movement" category, 
 *    visual modules under "Visuals," and so on.
 *
 * By following these structure guidelines, I aim to create a well-organized and easy-to-maintain 
 * Minecraft client while adhering to good coding practices.
 */

