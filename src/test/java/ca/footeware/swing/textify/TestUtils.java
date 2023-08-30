package ca.footeware.swing.textify;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import javax.swing.JMenu;

public class TestUtils {

    private static int counter;

    public static Component getChildNamed(Component parent, String name) {

        if (name.equals(parent.getName())) {
            return parent;
        }

        if (parent instanceof Container) {
            Component[] children = ((Container) parent).getComponents();

            for (int i = 0; i < children.length; ++i) {
                Component child = getChildNamed(children[i], name);
                if (child != null) {
                    return child;
                }
            }
        }

        return null;
    }

    public static Component getChildIndexed(
            Component parent, String klass, int index) {
        counter = 0;

        // Step in only owned windows and ignore its components in JFrame
        if (parent instanceof Window) {
            Component[] children = ((Window) parent).getOwnedWindows();

            for (int i = 0; i < children.length; ++i) {
                // Take only active windows
                if (children[i] instanceof Window
                        && !((Window) children[i]).isActive()) {
                    continue;
                }

                Component child = getChildIndexedInternal(
                        children[i], klass, index);

                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    private static Component getChildIndexedInternal(
            Component parent, String klass, int index) {

        if (parent.getClass().toString().endsWith(klass)) {
            if (counter == index) {
                return parent;
            }
            ++counter;
        }

        if (parent instanceof Container) {
            Component[] children = (parent instanceof JMenu)
                    ? ((JMenu) parent).getMenuComponents()
                    : ((Container) parent).getComponents();

            for (int i = 0; i < children.length; ++i) {
                Component child = getChildIndexedInternal(
                        children[i], klass, index);
                if (child != null) {
                    return child;
                }
            }
        }

        return null;
    }
}
