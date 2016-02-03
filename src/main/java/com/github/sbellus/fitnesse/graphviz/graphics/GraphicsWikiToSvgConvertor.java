package com.github.sbellus.fitnesse.graphviz.graphics;

public interface GraphicsWikiToSvgConvertor {
    public GraphicsSvg convert(GraphicsWiki wiki) throws GraphicsWikiToSvgConvertionException;
}
