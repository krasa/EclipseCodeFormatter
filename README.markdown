Allows using Eclipse's code formatter directly from IntelliJ. Solves the problem of maintaining a common code style in team environments where both IDEA and Eclipse are used.

Currently supports formatting of Java and JavaScript? files and GWT. Also emulates Eclipse's imports optimizing for java files. It is possible to add support for more file types...



# Instructions #
-  [Download the plugin and install](https://code.google.com/p/eclipse-code-formatter-intellij-plugin/)
-  Configure it
  -  Get Eclipse formatter settings:
  	-  Either enable project specific formatter settings to get org.eclipse.jdt.core.prefs
  	  - Go to Windows | Preferences | Java | Code Style | Formatter and click on "Export all..." button.
  	  - With your project open in Eclipse's workspace, right-click the project and choose Properties.
  	  - Go to Java Code Style | Formatter and select Enable project specific settings.
  	  - Also in the project properties, go to Java Compiler and select Enable project specific settings. *Make sure that the Compiler compliance level is set right.*
  	  - Click OK to save the settings.
  	  - Go to the .settings directory inside your project's directory. There you will find the org.eclipse.jdt.core.prefs file which contains the Eclipse formatter settings.
  	  - the result should look like [this](http://code.google.com/p/eclipse-code-formatter-intellij-plugin/source/browse/EclipseFormatter/test/resources/org.eclipse.jdt.core.prefs) 
  	- Or export formatter profiles to get *.xml file
  	  - Eclipse do not export default profiles, so you have to make your own
  	  - the result should look like [this](http://code.google.com/p/eclipse-code-formatter-intellij-plugin/source/browse/EclipseFormatter/test/resources/format.xml)
  - Set path to the file (IntelliJ | Settings | Eclipse Code Formatter | field "Eclipse preference file"), 
  - When using exported profiles(xml file), select desired profile in the combobox "Java formatter profile"
  - Check "Optimizing Imports" configuration
  	- Value of "Class count to use import with '*'" and "Name count to use static import with '*'" (Settings | Code Style | Imports) - Eclipse uses value of 30 by default
  	- Import order Settings | Eclipse Code Formatter | Import Order
  	- Make sure to keep disabled IntelliJ's Import Optimizing in the reformat dialog (Settings | Editor | Show "Reformat Code" dialog), the plugin will take care of imports anyway 
-  Format code as usual, notice the green bubble notification about successful formatting 
  -  you can turn notifications off at (Settings | Notifications)
-  Use Ctrl+Alt+O as usual, it was overridden to use this plugin
-  Use Ctrl + ~ for quick switch between formatters or icon at the main toolbar
 
[Quick video](http://www.dropbox.com/s/2vw60a0nmpcxuiq/settingsEclipseFormatter.avi )





# Possible problems with Java formatting #
- nothing was formatted or formatting failed 
  - make sure you are using proper language level
    -Java language level is set to 1.5 by default. For 1.6 make sure following lines are in the config file, same goes for 1.7
      - org.eclipse.jdt.core.compiler.compliance=1.6
      - org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6
      - org.eclipse.jdt.core.compiler.source=1.6
- trailing spaces inside javadocs are stripped.  
  - set "Strip trailing spaces on save" to "None" 
- file seems to be formatted differently
  - probably tab size and indendation is set differently than in eclipse (Settings-Code Style-Java). Using of either tab only or space only whitespace is recommended.
- Eclipse indendation is configured for 2 spaces, but a new line gets indented by 4 spaces when Enter is pressed.
  - change code style in IntelliJ. Not all things get formatted by this plugin when you type them.
- If nothing helps
  - [check old issues](https://code.google.com/p/eclipse-code-formatter-intellij-plugin/issues/list)
  - create new issue here or write me an email


# Troubleshooting #
If it is mysteriously not working, edit IntelliJ IDEA XX\bin\log.xml:
add following lines before < root >:
```
	<category name="krasa.formatter">
		<level value="DEBUG"/>
	</category>
````
and restart. Try to reformat something and send me the log...
