Allows using Eclipse's code formatter directly from IntelliJ. Solves the problem of maintaining a common code style in team environments where both IDEA and Eclipse are used.

Currently supports formatting of 
 - Java (also emulates Eclipse's imports optimizing)
 - JavaScript 
 - GWT
 - C/C++


# Instructions #
-  Install the plugin
  - [Tutorial: Installing, Updating and Uninstalling Repository Plugins](http://www.jetbrains.com/idea/webhelp/installing-updating-and-uninstalling-repository-plugins.html)
  - [Plugin repository page](http://plugins.jetbrains.com/plugin/?idea&id=6546)		
-  Configure it
  -  Get Eclipse formatter settings file: 
  	- Either export formatter profiles to get *.xml file
  	  - Go to Eclipse | Windows | Preferences | Java | Code Style | Formatter
  	  - Eclipse do not export default profiles, so you have to make your own ("New" button)
  	  - Export the profile ("Export all..." button.)
  	  - The result should look like [this](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/format.xml)
	- Or enable project specific formatter settings to get org.eclipse.jdt.core.prefs
  	  - With your project open in Eclipse's workspace, right-click the project and choose Properties.
  	  - Go to Java Code Style | Formatter and select Enable project specific settings.
  	  - Click OK to save the settings.
  	  - Go to the .settings directory inside your project's directory. There you will find the org.eclipse.jdt.core.prefs file which contains the Eclipse formatter settings.
  	  - The result should look like [this](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/org.eclipse.jdt.core.prefs) 
  	- Or export a [Workspace Mechanic] (http://marketplace.eclipse.org/content/workspace-mechanic/) configuration to get *.epf file
  	  - The result should look like [this](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/mechanic-formatter.epf) 
  - Open a project in IntelliJ
  - Set path to the config file (IntelliJ | Settings | Other Settings | Eclipse Code Formatter | field "Eclipse preference file")
  - When using exported profiles(xml file), select desired profile in the combobox "Java formatter profile"
  - Check "Optimizing Imports" configuration
  	- Set import order
  	  - Either leave the default
  	  - Or set path to Eclipse configuration file:
    		-  Go to Eclipse | Windows | Preferences | Java | Code Style | Organize Imports
    			- Either click on "Export...", ([example](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/bcjur2.importorder))
    			- Or enable project specific settings and use "org.eclipse.jdt.ui.prefs" file which should contain the line "org.eclipse.jdt.ui.importorder=..."
  	- Value of "Class count to use import with ''" and "Name count to use static import with ''" (Settings | Editor | Code Style | Java | Imports) for Idea 14 or (Settings | Editor | Code Style | Imports) for older Idea - Eclipse uses value of 99 by default
  	- For versions lower than 4.0 - make sure to keep disabled IntelliJ's Import Optimizing in the reformat dialog (Settings | Editor | Show "Reformat Code" dialog), the plugin will take care of imports anyway
  	- For versions higher than 4.0 - imports will be reordered together with normal IntelliJ's import optimizing
  	- **Disable "Optimize imports on the fly"**
-  Format code as usual, notice the green bubble notification about successful formatting 
  -  notifications can be disabled at (Settings | Notifications)
- Use Ctrl+Alt+O as usual, it will use this plugin
- Use Ctrl + ~ for quick switch between formatters or icon at the main toolbar
- [Give it 5 stars] (http://plugins.jetbrains.com/plugin/?idea&id=6546)
- [Donate some money] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Eclipse%20code%20formatter%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest)



# Possible problems with Java formatting #
- @formatter:off is not working 
  - https://github.com/krasa/EclipseCodeFormatter/issues/64
- nothing was formatted or formatting failed 
  - make sure you are using proper language level (Main Menu | File | Project Structure) 
- trailing spaces inside javadocs are stripped.  
  - set "Strip trailing spaces on save" to "None" 
- file is formatted differently
  - The file is actually formatted fine, it just looks different in the editor, as the tab size and indendation are  set differently in IntelliJ (Settings | Editor | Code Style | Java) than in Eclipse. Using of either tab only or space only whitespace is recommended.
  - Or it is a bug.
- Eclipse indendation is configured for 2 spaces, but a new line gets indented by 4 spaces when Enter is pressed.
  - change code style in IntelliJ. Not all things get formatted by this plugin when you type them.
- If nothing helps
  - [check old issues](https://github.com/krasa/eclipse-code-formatter-intellij-plugin/issues?q=is%3Aissue+is%3Aclosed)
  - create new issue here or write me an email


# Troubleshooting #
If it is mysteriously not working, go to `Main Menu | Help | Edit Debug Settings` and add:
```
krasa.formatter
````
Try to reformat something and send me the log...
