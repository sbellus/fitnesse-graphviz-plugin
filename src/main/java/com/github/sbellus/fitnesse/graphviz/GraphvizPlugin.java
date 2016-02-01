package com.github.sbellus.fitnesse.graphviz;

import java.util.Properties;

import fitnesse.plugins.PluginException;
import fitnesse.plugins.PluginFeatureFactoryBase;
import fitnesse.wikitext.parser.SymbolProvider;

/**
 * Register graphviz symbol.
 */
public class GraphvizPlugin extends PluginFeatureFactoryBase {
    private Properties properties;

    public GraphvizPlugin(Properties properties) {
        this.properties = properties;
    }

    public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
        symbolProvider.add(new GraphvizDotSymbol(properties));
    }
}
