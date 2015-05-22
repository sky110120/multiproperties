Previous step: [Creating the new project for the new handler](ImplementingNewHandlerProject.md)<br />
Next step: [How to implement new handler class](ImplementingNewHandlerClass.md)


---


# Introduction #
As it is already written in the [previous part](ImplementingNewHandlerTutorial.md) of the tutorial, we have the skeleton of the new handler, together with it's empty `class` and `configuratorClass` implementations, but it does not do anything. By the end of this wiki page, a real handler configuration will be implemented. That means the new handler can be configured by the **Configure** button on the [Columns](FeaturesOfEditor#Columns.md) page for each column.

# Handler configuration format #
The MultiProperties file format reserves only a String value for each column's handler configuration. The format of these values are absolutely handler specific, so the MultiProperties plug-in only stores the values and does not understand them. If you take a look into a `multiproperties` file, then find the `HandlerConfiguration` element under each `Column` element.

When the user clicks on the **Configure** button on the [Columns](FeaturesOfEditor#Columns.md) page, then the handler's `configuratorClass` is triggered. It must implement the `hu.skzs.multiproperties.base.api.IHandlerConfigurator` which specifies only the following method.
```java

public String configure(final Shell shell, final String configuration) throws HandlerException
```

The method receives a `Shell` instance which is useful for representing any user interface window like a wizard, and it also receives the current configuration String value. The returned String value should be the new configuration value, or `null` if the modification is canceled.

After understanding the handler configuration basis and taking consideration of [required features](ImplementingNewHandlerTutorial#Required_features.md) we can design our handler's configuration format. We store at first the target file name, then the _header_, _property_, _comment_, _empty_ and finally the _footer_ patterns. It is ended with a fix `ETX` character sequence and each value is separated by a `/#/` character sequence.
<pre>
path/#/header/#/property/#/comment/#/empty/#/footer/#/ETX<br>
</pre>

# Configurator classes #
Create a new `hu.skzs.multiproperties.handler.test.configurator` package for handler configuration codes. First of all, we need a _configurator_ class for **formatting** and **parsing** the above seen configuration format. But when we use the handler from [command line](CommandLineHandlerExecutor.md), then the `path` part of the configuration means a **file system** location instead of Eclipse **workspace** location. That means we need two different implementations, and of course a factory class which is able to choose the proper implementation.

See below the `AbstractConfigurator` class. Please note that the constructor will parse the String based configuration, and the `toString` method will format it. Furthermore at this point we cannot know whether the plug-in is used from [command line](CommandLineHandlerExecutor.md) or not, so this is just an abstract implementation together with the abstract `parsePath` and `formatPath` methods.

Finally you need to implement the missing getter and setter methods for `headerPattern`, `footerPattern`, `propertyPattern`, `commentPattern`, `emptyPattern` fields.
```java
package hu.skzs.multiproperties.handler.test.configurator;

import hu.skzs.multiproperties.base.api.HandlerException;

public abstract class AbstractConfigurator
{
public static final String DELIM = "/#/";

protected String headerPattern;
protected String footerPattern;
protected String propertyPattern;
protected String commentPattern;
protected String emptyPattern;

AbstractConfigurator(final String configuration) throws HandlerException
{
if (configuration == null || configuration.equals(""))
return;
try
{
final String[] tokens = configuration.split(DELIM);
if (tokens.length != 7)
throw new IllegalArgumentException("Invalid configuration: " + configuration);

parsePath(tokens[0]);
headerPattern = tokens[1];
propertyPattern = tokens[2];
commentPattern = tokens[3];
emptyPattern = tokens[4];
footerPattern = tokens[5];
// The last pattern is the ETX. It is used only when the last token is empty String,
// in that case the split does not result enough tokens.
}
catch (final Exception e)
{
throw new HandlerException("Unexpected error occurred during parsing the handler configuration", e);
}
}

@Override
public String toString()
{
final StringBuilder stringBuilder = new StringBuilder();
stringBuilder.append(formatPath());
stringBuilder.append(DELIM);
stringBuilder.append(headerPattern);
stringBuilder.append(DELIM);
stringBuilder.append(propertyPattern);
stringBuilder.append(DELIM);
stringBuilder.append(commentPattern);
stringBuilder.append(DELIM);
stringBuilder.append(emptyPattern);
stringBuilder.append(DELIM);
stringBuilder.append(footerPattern);
stringBuilder.append(DELIM);
stringBuilder.append("ETX");
return stringBuilder.toString();
}

public abstract void parsePath(String path);

public abstract String formatPath();

// FIXME: create all of the getter and setter methods for headerPattern, propertyPattern, commentPattern, emptyPattern and footerPattern fields
}
```

The `WorkspaceConfigurator` class is the Eclipse workspace based implementation of `AbstractConfigurator` class. You need to implement the missing getter and setter methods again for `containerName` and `fileName` fields.

```java
package hu.skzs.multiproperties.handler.test.configurator;

import hu.skzs.multiproperties.base.api.HandlerException;

public class WorkspaceConfigurator extends AbstractConfigurator
{

private String containerName;
private String fileName;

WorkspaceConfigurator(final String configuration) throws HandlerException
{
super(configuration);
}

@Override
public void parsePath(final String path)
{
containerName = path.substring(0, path.lastIndexOf("/"));
fileName = path.substring(path.lastIndexOf("/") + 1);
}

@Override
public String formatPath()
{
final StringBuilder stringBuilder = new StringBuilder(containerName);
stringBuilder.append("/");
stringBuilder.append(fileName);
return stringBuilder.toString();
}

// FIXME: create all of the getter and setter methods for containerName and fileName fields
}
```

The `FileSystemConfigurator` class is the file system based implementation of `AbstractConfigurator` class. You need to implement the missing getter and setter methods again for `fileName` field.

```java
package hu.skzs.multiproperties.handler.test.configurator;

import hu.skzs.multiproperties.base.api.HandlerException;

public class FileSystemConfigurator extends AbstractConfigurator
{

private String fileName;

FileSystemConfigurator(final String configuration) throws HandlerException
{
super(configuration);
}

@Override
public void parsePath(final String path)
{
fileName = path;
}

@Override
public String formatPath()
{
return fileName;
}

// FIXME: create the getter and setter methods for fileName field
}
```

The `ConfiguratorFactory` class can be used for getting an actually proper configurator implementation.

```java

package hu.skzs.multiproperties.handler.test.configurator;

import hu.skzs.multiproperties.base.api.HandlerException;

public class ConfiguratorFactory
{
private static Boolean presenseOfEclipse;

public static AbstractConfigurator getConfigurator(final String configuration) throws HandlerException
{
if (presenceOfEclipse())
return new WorkspaceConfigurator(configuration);
else
return new FileSystemConfigurator(configuration);
}

protected static boolean presenceOfEclipse()
{
if (presenseOfEclipse == null)
{
synchronized (ConfiguratorFactory.class)
{
if (presenseOfEclipse == null)
{
try
{
Class.forName("org.eclipse.ui.plugin.AbstractUIPlugin");
presenseOfEclipse = Boolean.TRUE;
return true;
}
catch (final ClassNotFoundException e)
{
presenseOfEclipse = Boolean.FALSE;
return false;
}
}
}
}
return presenseOfEclipse.booleanValue();
}
}
```

# Writer classes #
Create a new `hu.skzs.multiproperties.handler.test.writer` package for handler writer codes. Based on a `configuration` implementation and on the content byte array, a writer class is able to produce the output file. Because the [command line](CommandLineHandlerExecutor.md) usage again we need to introduce two different implementation. The only different is that instead of an abstract base class we need only an `IWriter` interface.

```java
package hu.skzs.multiproperties.handler.test.writer;

import hu.skzs.multiproperties.base.api.HandlerException;

public interface IWriter
{

public abstract void write(byte[] bytes) throws HandlerException;
}
```

The `WorkspaceWriter` class is the Eclipse workspace based implementation of `IWriter` interface.

```java
package hu.skzs.multiproperties.handler.test.writer;

import hu.skzs.multiproperties.base.api.HandlerException;
import hu.skzs.multiproperties.handler.test.configurator.WorkspaceConfigurator;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class WorkspaceWriter implements IWriter
{

private final WorkspaceConfigurator configurator;

WorkspaceWriter(final WorkspaceConfigurator configurator)
{
this.configurator = configurator;
}

public void write(final byte[] bytes) throws HandlerException
{
try
{
final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
final IResource resource = root.findMember(new Path(configurator.getContainerName()));
if (resource == null)
throw new HandlerException("Non existing workspace location: "
+ configurator.getContainerName());
final IContainer container = (IContainer) resource;

final IFile file = container.getFile(new Path(configurator.getFileName()));

// Writing the content
final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
if (file.exists())
{
file.setContents(stream, false, true, null);
}
else
{
file.create(stream, false, null);
}
file.setCharset("UTF-8", null);
}
catch (final CoreException e)
{
throw new HandlerException("Unable to write the content", e);
}
}
}
```

The `FileSystemWriter` class is the file system based implementation of `IWriter` interface.

```java
package hu.skzs.multiproperties.handler.test.writer;

import hu.skzs.multiproperties.base.api.HandlerException;
import hu.skzs.multiproperties.handler.test.configurator.FileSystemConfigurator;

import java.io.File;
import java.io.FileOutputStream;

public class FileSystemWriter implements IWriter
{

private final FileSystemConfigurator configurator;

FileSystemWriter(final FileSystemConfigurator configurator)
{
this.configurator = configurator;
}

public void write(final byte[] bytes) throws HandlerException
{
final File file = new File(configurator.getFileName());
FileOutputStream outputStream = null;
try
{
outputStream = new FileOutputStream(file);
outputStream.write(bytes);
outputStream.flush();
outputStream.close();
}
catch (final Exception e)
{
throw new HandlerException("Unable to write the content", e);
}
finally
{
try
{
if (outputStream != null)
outputStream.close();
}
catch (final Exception e)
{
}
}
}
}
```

The `WriterFactory` class can be used for getting an actually proper writer implementation.

```java
package hu.skzs.multiproperties.handler.test.writer;

import hu.skzs.multiproperties.handler.test.configurator.AbstractConfigurator;
import hu.skzs.multiproperties.handler.test.configurator.FileSystemConfigurator;
import hu.skzs.multiproperties.handler.test.configurator.WorkspaceConfigurator;

public class WriterFactory
{

public static IWriter getWriter(final AbstractConfigurator configurator)
{
if (configurator instanceof FileSystemConfigurator)
{
final FileSystemConfigurator fileSystemConfigurator = (FileSystemConfigurator) configurator;
return new FileSystemWriter(fileSystemConfigurator);
}
else if (configurator instanceof WorkspaceConfigurator)
{
final WorkspaceConfigurator workspaceConfigurator = (WorkspaceConfigurator) configurator;
return new WorkspaceWriter(workspaceConfigurator);
}
else
throw new IllegalArgumentException("Unsupported configurator instance, " + configurator);
}
}
```

# Wizard classes #

Create a new `hu.skzs.multiproperties.handler.test.wizard` package for handler configuration wizard codes. On the first page of the wizard the user can specify the target workspace location and the file name. On the second page the patterns can be specified.

```java
package hu.skzs.multiproperties.handler.test.wizard;

import hu.skzs.multiproperties.handler.test.configurator.WorkspaceConfigurator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class TextFileSelectionPage extends WizardPage
{

private final WorkspaceConfigurator configurator;
private Text textLocation;
private Text textFile;

public TextFileSelectionPage(final WorkspaceConfigurator configurator)
{
super("column.configuration.page");
this.configurator = configurator;
setTitle("Output text file");
setDescription("Specify the output text file.");
}

public void createControl(final Composite parent)
{
final Composite container = new Composite(parent, SWT.NULL);
setControl(container);
final GridLayout layout = new GridLayout();
layout.numColumns = 3;
layout.verticalSpacing = 9;
container.setLayout(layout);

Label label = new Label(container, SWT.NULL);
label.setText("Location");
textLocation = new Text(container, SWT.BORDER | SWT.SINGLE);
if (configurator.getContainerName() != null)
textLocation.setText(configurator.getContainerName());
textLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
textLocation.addModifyListener(new ModifyListener()
{
public void modifyText(final ModifyEvent e)
{
dialogChanged();
}
});

final Button button = new Button(container, SWT.PUSH);
button.setText("Browse...");
button.addSelectionListener(new SelectionAdapter()
{
@Override
public void widgetSelected(final SelectionEvent e)
{
handleBrowse();
}
});
label = new Label(container, SWT.NULL);
label.setText("File name");
textFile = new Text(container, SWT.BORDER | SWT.SINGLE);
if (configurator.getFileName() != null)
textFile.setText(configurator.getFileName());
textFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
textFile.addModifyListener(new ModifyListener()
{
public void modifyText(final ModifyEvent e)
{
dialogChanged();
}
});

dialogChanged();
}

private void handleBrowse()
{
final ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace()
.getRoot(), false, "Select location for the properties file.");
if (dialog.open() == ContainerSelectionDialog.OK)
{
final Object[] result = dialog.getResult();
if (result.length == 1)
{
textLocation.setText(((Path) result[0]).toString());
}
}
}

private void dialogChanged()
{
final IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getLocation()));
final String fileName = getFileName();

if (getLocation().length() == 0)
{
updateStatus("File location must be specified!");
return;
}
if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0)
{
updateStatus("File location must exist!");
return;
}
if (!container.isAccessible())
{
updateStatus("Project must be writable!");
return;
}
if (fileName.length() == 0)
{
updateStatus("File name must be specified!");
return;
}
if (fileName.replace('\\', '/').indexOf('/', 1) > 0)
{
updateStatus("File name must be valid!");
return;
}
updateStatus(null);
}

private void updateStatus(final String message)
{
setErrorMessage(message);
setPageComplete(message == null);
}

public String getLocation()
{
return textLocation.getText();
}

public String getFileName()
{
return textFile.getText();
}
}
```

The `PatternPage` is the second page for editing the patterns.

```java
package hu.skzs.multiproperties.handler.test.wizard;

import hu.skzs.multiproperties.handler.test.configurator.AbstractConfigurator;
import hu.skzs.multiproperties.handler.test.configurator.WorkspaceConfigurator;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PatternPage extends WizardPage
{

private final AbstractConfigurator configurator;

private Text headerText;
private Text footerText;
private Text propertyText;
private Text commentText;
private Text emptyText;

public PatternPage(final WorkspaceConfigurator configurator)
{
super("column.configuration.page");
this.configurator = configurator;
setTitle("Patterns");
setDescription("Specify the desired patterns.");
}

public void createControl(final Composite parent)
{
final Composite container = new Composite(parent, SWT.NULL);
setControl(container);
final GridLayout layout = new GridLayout();
layout.numColumns = 2;
layout.verticalSpacing = 5;
container.setLayout(layout);

headerText = createField(container, "Header pattern", configurator.getHeaderPattern());
footerText = createField(container, "Footer pattern", configurator.getFooterPattern());
propertyText = createField(container, "Property pattern", configurator.getPropertyPattern());
commentText = createField(container, "Comment pattern", configurator.getCommentPattern());
emptyText = createField(container, "Empty pattern", configurator.getEmptyPattern());
dialogChanged();
}

private void dialogChanged()
{
if (headerText.getText().contains(AbstractConfigurator.DELIM)
|| footerText.getText().contains(AbstractConfigurator.DELIM)
|| propertyText.getText().contains(AbstractConfigurator.DELIM)
|| commentText.getText().contains(AbstractConfigurator.DELIM)
|| emptyText.getText().contains(AbstractConfigurator.DELIM))
{
setErrorMessage("The " + AbstractConfigurator.DELIM + " character sequence is not allowed!");
setPageComplete(false);
return;
}

if (headerText.getText().trim().equals("") && footerText.getText().trim().equals("")
&& propertyText.getText().trim().equals("") && commentText.getText().trim().equals("")
&& emptyText.getText().trim().equals(""))
{
setErrorMessage(null);
setPageComplete(false);
return;
}
setErrorMessage(null);
setPageComplete(true);
}

private Text createField(final Composite parent, final String labelText, final String defaultValue)
{
final Label label = new Label(parent, SWT.NULL);
label.setText(labelText);
final Text text = new Text(parent, SWT.BORDER | SWT.MULTI);
if (defaultValue != null)
text.setText(defaultValue);
final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
gridData.heightHint = 40;
text.setLayoutData(gridData);
text.addModifyListener(new ModifyListener()
{
public void modifyText(final ModifyEvent e)
{
dialogChanged();
}
});

return text;
}

public String getHeaderPattern()
{
return headerText.getText();
}

public String getFooterPattern()
{
return footerText.getText();
}

public String getPropertyPattern()
{
return propertyText.getText();
}

public String getCommentPattern()
{
return commentText.getText();
}

public String getEmptyPattern()
{
return emptyText.getText();
}
}
```

The `TestHandlerConfigurationWizard` is the JFace wizard implementation. Please note that when the user finish the wizard, then the `performFinish` method only updates the related fields in the configurator. Because the wizard cannot be used from [command line](CommandLineHandlerExecutor.md), thus the configurator instance can be only `WorkspaceConfigurator` and never `FileSystemConfigurator`.

```java
package hu.skzs.multiproperties.handler.test.wizard;

import hu.skzs.multiproperties.handler.test.configurator.WorkspaceConfigurator;

import org.eclipse.jface.wizard.Wizard;

public class TestHandlerConfigurationWizard extends Wizard
{
private final WorkspaceConfigurator configurator;
private TextFileSelectionPage textFileSelectionPage;
private PatternPage patternPage;

public TestHandlerConfigurationWizard(final WorkspaceConfigurator configurator)
{
this.configurator = configurator;
}

@Override
public void addPages()
{
setWindowTitle("Output text file");
textFileSelectionPage = new TextFileSelectionPage(configurator);
addPage(textFileSelectionPage);
patternPage = new PatternPage(configurator);
addPage(patternPage);
}

@Override
public boolean performFinish()
{		configurator.setContainerName(textFileSelectionPage.getLocation());
configurator.setFileName(textFileSelectionPage.getFileName());
configurator.setHeaderPattern(patternPage.getHeaderPattern());
configurator.setFooterPattern(patternPage.getFooterPattern());
configurator.setPropertyPattern(patternPage.getPropertyPattern());
configurator.setCommentPattern(patternPage.getCommentPattern());
configurator.setEmptyPattern(patternPage.getEmptyPattern());
return true;
}
}
```

# Finishing the configurator #
To finish the configurator class we just need to update the already existing `TestHandlerConfigurator` class as follows.

```java
package hu.skzs.multiproperties.handler.test;

import hu.skzs.multiproperties.base.api.HandlerException;
import hu.skzs.multiproperties.base.api.IHandlerConfigurator;
import hu.skzs.multiproperties.handler.test.configurator.ConfiguratorFactory;
import hu.skzs.multiproperties.handler.test.configurator.WorkspaceConfigurator;
import hu.skzs.multiproperties.handler.test.wizard.TestHandlerConfigurationWizard;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class TestHandlerConfigurator implements IHandlerConfigurator
{

public String configure(final Shell shell, final String configuration) throws HandlerException
{
try
{
final WorkspaceConfigurator workspaceConfigurator = (WorkspaceConfigurator) ConfiguratorFactory
.getConfigurator(configuration); // it must be WorkspaceConfigurator in this case

final TestHandlerConfigurationWizard wizard = new TestHandlerConfigurationWizard(workspaceConfigurator);
final WizardDialog wizarddialog = new WizardDialog(shell, wizard);
if (wizarddialog.open() == Window.OK)
return workspaceConfigurator.toString();
else
return null;
}
catch (final Exception e)
{
throw new HandlerException("Unexpected error occurred during configuring the column by handler", e);
}
}
}
```

# Testing #
If you launch the test Eclipse again, then you can now use the **Configure** button. It will open our wizard, the MultiProperties plug-in will store our changes in the `multiproperties` file, etc. Only the handler class itself is left.

![http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/ImplementingNewHandler/configuration01.jpg](http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/ImplementingNewHandler/configuration01.jpg)

![http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/ImplementingNewHandler/configuration02.jpg](http://svn.codespot.com/a/eclipselabs.org/multiproperties/trunk/hu.skzs.multiproperties.util/wiki/ImplementingNewHandler/configuration02.jpg)


---


Previous step: [Creating the new project for the new handler](ImplementingNewHandlerProject.md)<br />
Next step: [How to implement new handler class](ImplementingNewHandlerClass.md)