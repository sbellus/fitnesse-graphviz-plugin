package com.github.sbellus.fitnesse.graphviz;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import fitnesse.plugins.PluginException;
import fitnesse.plugins.PluginFeatureFactoryBase;
import fitnesse.wikitext.parser.SymbolProvider;

/**
 * Register graphviz symbol.
 */
public class GraphvizPlugin extends PluginFeatureFactoryBase {
    private Properties properties;

    public GraphvizPlugin() {
        this.properties = makeProperties();
    }

    public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
        symbolProvider.add(GraphvizDotSymbol.make(properties));
    }

    private Properties makeProperties() {
        Properties pluginProperties = new Properties();

        try {
            File currentJarFile = new File(
                    GraphvizPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            String currentJarPath = FilenameUtils.getFullPath(currentJarFile.getAbsolutePath());
            File pluginPropertiesFile = new File(currentJarPath + "fitnesse-graphviz-plugin.properties");
            if (pluginPropertiesFile.exists()) {
                InputStream is = new FileInputStream(pluginPropertiesFile);
                pluginProperties.load(is);
                is.close();
            }
        } catch (Exception e) {
            // do nothing
        }

        if (pluginProperties.getProperty("dotExecutable") == null) {
            GraphvizDotFinder finder = new GraphvizDotFinder();
            String dotExecutable = finder.findDot();
            
            if (dotExecutable != null) {
                pluginProperties.setProperty("dotExecutable", dotExecutable);
            }
        }

        return pluginProperties;
    }
}
