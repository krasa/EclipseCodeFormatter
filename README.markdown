# Eclipse Code Formatter [![Donate][badge-paypal-img]][badge-paypal]

Allows using Eclipse's code formatter directly from IntelliJ. Solves the problem of maintaining a common code style in team environments where both IDEA and Eclipse are used.

Currently supports formatting of 
 - Java (also emulates Eclipse's imports optimizing)
 - JavaScript 
 - GWT
 - C/C++

## Instructions ##
1. Install the plugin
   - [Tutorial: Installing, Updating and Uninstalling Repository Plugins](http://www.jetbrains.com/idea/webhelp/installing-updating-and-uninstalling-repository-plugins.html)
   - [Plugin repository page](http://plugins.jetbrains.com/plugin/?idea&id=6546)		
2. Configure it
   1. Get Eclipse formatter settings file: 
      - Either, export formatter profiles to get a `*.xml` file
         1. Go to `Eclipse | Windows | Preferences | Java | Code Style | Formatter`
         2. Eclipse does not export default profiles, so you have to make your own via the `New` button
         3. Export the profile via the `Export all...` button
            - The result should look like [this](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/format.xml)
      - Or, enable project specific formatter settings to get `org.eclipse.jdt.core.prefs`
         1. With your project open in Eclipse's workspace, `right-click` the project and choose Properties
         2. Go to `Java Code Style | Formatter` and select `Enable project specific settings`
         3. Click `OK` to save the settings
         4. Go to the `.settings` directory inside your project's directory. There you will find the `org.eclipse.jdt.core.prefs` file which contains the Eclipse formatter settings
            - The result should look like [this](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/org.eclipse.jdt.core.prefs) 
      - Or, export a [Workspace Mechanic] (http://marketplace.eclipse.org/content/workspace-mechanic/) configuration to get a `*.epf` file
         - The result should look like [this](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/mechanic-formatter.epf) 
   2. Open a project in IntelliJ
   3. Set path to the config file via `IntelliJ | Settings | Other Settings | Eclipse Code Formatter | Eclipse preference file`
   4. When using exported profiles (the xml file), select desired profile in the combobox `Java formatter profile`
   5. Check `Optimizing Imports` configuration
      1. Set import order
         - Either, leave the default
         - Or, set path to Eclipse configuration file:
            -  Go to `Eclipse | Windows | Preferences | Java | Code Style | Organize Imports`
               - Either, click on `Export...`, ([example](https://github.com/krasa/EclipseCodeFormatter/blob/master/test/resources/bcjur2.importorder))
               - Or, enable project specific settings and use `org.eclipse.jdt.ui.prefs` file which should contain the line `org.eclipse.jdt.ui.importorder=...`
      2. Set the value of `Class count to use import with` and `Name count to use static import with` in `Settings | Editor | Code Style | Java | Imports` for Idea 14 or `Settings | Editor | Code Style | Imports` for older Idea. Eclipse uses 99 by default
      3. For versions lower than 4.0 - make sure to disable IntelliJ's `Import Optimizing` in the reformat dialog via `Settings | Editor | Show "Reformat Code" dialog`. The plugin will take care of imports anyway
      4. For versions higher than 4.0 - imports will be reordered together with normal IntelliJ's import optimizing
      5. **Disable `Optimize imports on the fly`**
3. Format code as usual, notice the green bubble notification about successful formatting 
   -  notifications can be disabled at `Settings | Notifications`
4. Use `Ctrl + Alt + O` as usual, it will use this plugin
5. Use `Ctrl + ~` for quick switch between formatters or icon at the main toolbar
6. [Give it 5 stars](http://plugins.jetbrains.com/plugin/?idea&id=6546)
7. [Make a donation](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Eclipse%20code%20formatter%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest)

## Possible problems with Java formatting ##
- `@formatter:off` is not working
   See: https://github.com/krasa/EclipseCodeFormatter/issues/64
- Nothing was formatted or formatting failed 
  - Make sure you are using proper language level in `Main Menu | File | Project Structure`
- Trailing spaces inside javadocs are stripped
  - Set `Strip trailing spaces on save` to `None`
- File is formatted differently
  - The file is actually formatted fine, it just looks different in the editor, as the tab size and indendation are set differently in IntelliJ in `Settings | Editor | Code Style | Java` than in Eclipse. Using of either tab only or space only whitespace is recommended.
  - Or it is a bug
- Eclipse indendation is configured for 2 spaces, but a new line gets indented by 4 spaces when Enter is pressed.
  - Change code style in IntelliJ. Not all things get formatted by this plugin when you type them.
- If nothing helps
  - [check old issues](https://github.com/krasa/eclipse-code-formatter-intellij-plugin/issues?q=is%3Aissue+is%3Aclosed)
  - Create a new issue [here](https://github.com/krasa/eclipse-code-formatter-intellij-plugin/issues/new)


# Troubleshooting #
If it is mysteriously not working, go to `Main Menu | Help | Edit Debug Settings` and add:
```
krasa.formatter
````
Try to reformat something and [create a new issue](https://github.com/krasa/eclipse-code-formatter-intellij-plugin/issues/new), including the log



------
![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)
and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
innovative and intelligent tools for profiling Java and .NET applications.



[badge-paypal-img]:       https://img.shields.io/badge/donate-paypal-yellow.svg
[badge-paypal]:           https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Eclipse%20code%20formatter%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest
