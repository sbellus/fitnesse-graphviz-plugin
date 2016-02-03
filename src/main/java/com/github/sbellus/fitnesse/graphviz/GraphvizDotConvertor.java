package com.github.sbellus.fitnesse.graphviz;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.github.sbellus.fitnesse.graphviz.graphics.GraphicsProperties;
import com.github.sbellus.fitnesse.graphviz.graphics.GraphicsSvg;
import com.github.sbellus.fitnesse.graphviz.graphics.GraphicsWiki;
import com.github.sbellus.fitnesse.graphviz.graphics.GraphicsWikiToSvgConvertionException;
import com.github.sbellus.fitnesse.graphviz.graphics.GraphicsWikiToSvgConvertor;

public class GraphvizDotConvertor implements GraphicsWikiToSvgConvertor {
    private String dotExecutable;
    
    public GraphvizDotConvertor(String dotExecutable) {
        this.dotExecutable = dotExecutable;
    }
    
    public GraphicsSvg convert(GraphicsWiki wiki) throws GraphicsWikiToSvgConvertionException   {
        
        if (dotExecutable == null) {
            throw new GraphicsWikiToSvgConvertionException(
                    "dotExecutable has been not found (Is Graphviz installed?) or set in property file fitnesse-graphviz-plugin.properties as variable \"dotExecutable\"");
        }

        try {
            // create temporary dot file
            File temporaryDirectory = Files.createTempDirectory(null).toFile();
            File dotFile = new File(temporaryDirectory.toString(), "context.dot");
            dotFile.createNewFile();
            FileUtils.writeStringToFile(dotFile, wiki.getContent());

            // convert it to picture
            String dotPath = FilenameUtils.getFullPath(dotFile.toString());

            Process runner = Runtime.getRuntime().exec(
                    new String[] { "cmd.exe", "/c", dotExecutable, "-Tsvg", dotFile.getName() }, null,
                    new File(dotPath));

            InputStream stdout = runner.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String svg = "";
            String line;
            while ((line = reader.readLine()) != null) {
                svg += line + "\n";
            }
            Integer exitCode = runner.waitFor();

            if (exitCode != 0) {
                InputStream stderr = runner.getErrorStream();
                String errorText = IOUtils.toString(stderr);
                throw new GraphicsWikiToSvgConvertionException("Process \"" + dotExecutable + "\" that converts dot to svg exits with code "
                        + exitCode + " and produce following stderr\n\n" + errorText
                        + "\nWhen converting following dot\n" + wiki.getContent());
            }

            // manually change dimensions directly in generated svg
            Integer width =  wiki.getProperties().getWidth();
            Integer height = wiki.getProperties().getHeight();
            
            if (width != null) {
                if (height == null) {
                    // calculate height automatically
                    Pattern svgDimesionPattern = Pattern.compile(".*width=\"([0-9]+)pt\"[ \t]+height=\"([0-9]+)pt\".*",
                            Pattern.DOTALL);
                    java.util.regex.Matcher svgDimesionMatcher = svgDimesionPattern.matcher(svg);
                    if (svgDimesionMatcher.matches()) {
                        if (svgDimesionMatcher.group(1) != null && svgDimesionMatcher.group(2) != null) {
                            Float svgWidth = Float.parseFloat(svgDimesionMatcher.group(1));
                            Float svgHeight = Float.parseFloat(svgDimesionMatcher.group(2));

                            Float f = svgHeight / svgWidth;

                            height = Math.round(wiki.getProperties().getWidth() * f);
                        }
                    }
                }

                svg = Pattern.compile("width=\"[0-9]+pt\"", Pattern.DOTALL).matcher(svg)
                        .replaceFirst("width=\"" + width + "px\"");
            }

            if (height != null) {
                svg = Pattern.compile("height=\"[0-9]+pt\"", Pattern.DOTALL).matcher(svg)
                        .replaceFirst("height=\"" + height + "px\"");
            }

            GraphicsProperties svgProperties = new GraphicsProperties(wiki.getProperties());
            svgProperties.setHeight(height);
            return new GraphicsSvg(svg, svgProperties);
            
        } catch (Exception exception) {
            throw new GraphicsWikiToSvgConvertionException("During dot picture generation following exception occures:\n" + exception.toString());
        }
    }
}
