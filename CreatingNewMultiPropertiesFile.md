# Using Wizard #
The most easiest way to create a new MultiProperties file is to use the new wizard in [Eclipse](http://eclipse.org/). Go to **File** > **New** > **Other...** in menu.

![http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/CreatingNewMultiPropertiesFile/mp_wizard1.jpg](http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/CreatingNewMultiPropertiesFile/mp_wizard1.jpg)

Then select **MultiProperties**, then on the next wizard page fill the **Location**, **File name** and **Name** fields properly.

![http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/CreatingNewMultiPropertiesFile/mp_wizard2.jpg](http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/CreatingNewMultiPropertiesFile/mp_wizard2.jpg)

By clicking on the **Finish** button, the wizard will create the new MultiProperties file with ![http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.ui/icons/table.gif](http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.ui/icons/table.gif) icon and `multiproperties` extension.

# Coding the `multiproperties` file #
You can also create and edit the XML file itself with `multiproperties` extension. Use a text or XML editor, but always ensure the validity of the XML file against the used [XSD file](http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.base/src/hu/skzs/multiproperties/base/model) (see the latest `fileformat` package). Please note that, this is not convenient way for most of the users, and this wiki page does not describe it in more details.