/**
 * 
 */
package net.sourceforge.gjtapi.demo.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A filter for wave files.
 * @author Dirk Schnelle-Walka
 *
 */
class WaveFileFilter extends FileFilter {

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String name = f.getName();
        return name.endsWith(".wav");
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription() {
        return "Wave Files (*.wav)";
    }

}
