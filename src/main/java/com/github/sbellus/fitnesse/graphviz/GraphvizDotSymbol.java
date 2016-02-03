package com.github.sbellus.fitnesse.graphviz;


import java.util.Properties;

import com.github.sbellus.fitnesse.graphviz.graphics.GraphicsSymbol;

import fitnesse.wikitext.parser.SymbolType;

/**
 * Generates picture from graphvizDot source.
 */
public class GraphvizDotSymbol {
    private GraphicsSymbol symbol;
    private GraphvizDotConvertor convertor;

    public static SymbolType make(Properties properties) {
        return new GraphvizDotSymbol(properties).symbol;
    }

    public GraphvizDotSymbol(Properties properties) {
        convertor = new GraphvizDotConvertor(properties.getProperty("graphviz.dotExecutable"));
        symbol = new GraphicsSymbol("dot", convertor);
    }
}
