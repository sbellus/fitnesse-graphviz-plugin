package com.github.sbellus.fitnesse.graphviz;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

public class GraphvizDotFinder {
    
    public String findDot() {
        if (SystemUtils.IS_OS_LINUX) {
            return findLinuxDot().getAbsolutePath();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return findWindowsDotExe().getAbsolutePath();
        }
        
        return null;
    }

    private File findLinuxDot() {
        final File usrLocalBinDot = new File("/usr/local/bin/dot");

        if (usrLocalBinDot.exists()) {
            return usrLocalBinDot;
        }
        final File usrBinDot = new File("/usr/bin/dot");
        return usrBinDot;
    }
    
    private File findWindowsDotExe() {
        final File result = searchInDir(new File("c:/Program Files"));
        if (result != null) {
            return result;
        }
        final File result86 = searchInDir(new File("c:/Program Files (x86)"));
        if (result86 != null) {
            return result86;
        }
        final File resultEclipse = searchInDir(new File("c:/eclipse/graphviz"));
        if (resultEclipse != null) {
            return resultEclipse;
        }
        return null;
    }

    private static File searchInDir(final File programFile) {
        if (programFile.exists() == false || programFile.isDirectory() == false) {
            return null;
        }
        final List<File> dots = new ArrayList<File>();
        for (File f : programFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().toLowerCase().startsWith("graphviz");
            }
        })) {
            final File result = new File(new File(f, "bin"), "dot.exe");
            if (result.exists() && result.canRead()) {
                dots.add(result.getAbsoluteFile());
            }
        }
        return higherVersion(dots);
    }

    private static File higherVersion(List<File> dots) {
        if (dots.size() == 0) {
            return null;
        }
        Collections.sort(dots, Collections.reverseOrder());
        return dots.get(0);
    }
}
