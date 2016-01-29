package com.github.sbellus.fitnesse.graphviz;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import fitnesse.html.HtmlTag;
import fitnesse.html.RawHtml;
import fitnesse.wikitext.parser.Matcher;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.Preformat;
import fitnesse.wikitext.parser.Rule;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Translation;
import fitnesse.wikitext.parser.Translator;

/**
 * Generates picture from graphvizDot source.
 */
public class GraphvizDotSymbol extends SymbolType implements Rule, Translation {
    private final String PropertyPictureAsSvg = "pictureAsSVG";
    private final String PropertyTitle = "Title";
    private final String PropertyAlign = "Align";
    private final String PropertyWidth = "Width";
    private final String PropertyHeight = "Height";
    private String DotExecutable = null;
    
    public GraphvizDotSymbol(Properties properties) {
        super("startdot");
        wikiMatcher(new Matcher().startLine().ignoreWhitespace().string("!startdot"));
        wikiRule(this);
        htmlTranslation(this);
        
        DotExecutable = properties.getProperty("graphviz.dotExecutable");
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {

    	if (DotExecutable == null) {
            Symbol error = new Symbol(new Preformat(), "").add("The property \"graphviz.dotExecutable\" in plugins.properties MUST BE set and it is not set.");
            return new Maybe<Symbol>(error);
    	}
    	
        final SymbolType Enduml = new SymbolType("enddot")
                .wikiMatcher(new Matcher().startLine().ignoreWhitespace().string("!enddot"));

        String pictureAttributes = parser.parseToAsString(SymbolType.Newline).getValue();
        if (parser.atEnd())
            return Symbol.nothing;

        String graphvizDotContext = parser.parseLiteral(Enduml);
        if (parser.atEnd())
            return Symbol.nothing;

        // get picture attributes
        String width = null;
        String height = null;
        
        Pattern pattern = Pattern.compile("[ \t]*(\".*\")?[ \t]*(l|r|c)?[ \t]*([0-9]+)?[ \t]*([0-9]+)?");
        java.util.regex.Matcher matcher = pattern.matcher(pictureAttributes);
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                Pattern patternTitle = Pattern.compile("\"[ \t]*(.*?)[ \t]*\"");
                java.util.regex.Matcher matcherTitle = patternTitle.matcher(matcher.group(1));
                if (matcherTitle.matches()) {
                    current.putProperty(PropertyTitle, matcherTitle.group(1));
                }
            }
            if (matcher.group(2) != null) {
                current.putProperty(PropertyAlign, matcher.group(2));
            }
            if (matcher.group(3) != null) {
            	width = matcher.group(3);
                current.putProperty(PropertyWidth, width);
            }
            if (matcher.group(4) != null) {
            	height = matcher.group(4);
                current.putProperty(PropertyHeight, height);
            }
        }

        try {
	        // create temporary dot file
	        File temporaryDirectory = Files.createTempDirectory(null).toFile();
			File dotFile = new File(temporaryDirectory.toString(), "context.dot");
			dotFile.createNewFile();
			FileUtils.writeStringToFile(dotFile, graphvizDotContext);
			
	        // convert it to picture
			String dotPath = FilenameUtils.getFullPath(dotFile.toString());

			Process runner = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", DotExecutable, "-Tsvg", dotFile.getName() }, null, new File(dotPath));
	
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
	            Symbol error = new Symbol(new Preformat(), "")
	            		.add(
	            				"Process that converts dot to svg exits with code " + exitCode + 
	            				" and produce following stderr\n\n" + errorText + 
	            				"\nWhen converting following dot\n" + graphvizDotContext
	            		);
	            return new Maybe<Symbol>(error);
			}
			
			// manually change dimensions directly in generated svg
			if (width != null) {
				if (height == null) {
					// calculate height automatically
					Pattern svgDimesionPattern = Pattern.compile(".*width=\"([0-9]+)pt\"[ \t]+height=\"([0-9]+)pt\".*", Pattern.DOTALL);
			        java.util.regex.Matcher svgDimesionMatcher = svgDimesionPattern.matcher(svg);
			        if (svgDimesionMatcher.matches()) {
			            if (svgDimesionMatcher.group(1) != null && svgDimesionMatcher.group(2) != null) {
			            	Float svgWidth = Float.parseFloat(svgDimesionMatcher.group(1));
			            	Float svgHeight = Float.parseFloat(svgDimesionMatcher.group(2));
			            	
			            	Float f = svgHeight / svgWidth;
			            	
			            	height = Integer.toString(Math.round(Float.parseFloat(width) * f));
			            }
			        }
				}
				
				svg = Pattern.compile("width=\"[0-9]+pt\"", Pattern.DOTALL).matcher(svg).replaceFirst("width=\"" + width + "px\"");
			}
			
			if (height != null) {
				svg = Pattern.compile("height=\"[0-9]+pt\"", Pattern.DOTALL).matcher(svg).replaceFirst("height=\"" + height + "px\"");
			}
			
			current.putProperty(PropertyPictureAsSvg, svg);
        }
		catch(Exception exception) {
            Symbol error = new Symbol(new Preformat(), "").add("During dot picture generation following exception occures:\n" + exception.toString());
            return new Maybe<Symbol>(error);
		}

        return new Maybe<Symbol>(current);
    }

    public String toTarget(Translator translator, Symbol symbol) {

        final HtmlTag newLine = new HtmlTag("br");
        
        HtmlTag graphvizDot = new HtmlTag("div");
        HtmlTag graphvizDotHeader = new HtmlTag("div");
        graphvizDotHeader.addAttribute("style", "display: block;margin: auto;");
        
        HtmlTag graphvizDotHolder = new HtmlTag("div");
        graphvizDotHolder.addAttribute("class", "graphvizDot");
        
        String position = "left";
        if (symbol.hasProperty(PropertyAlign)) {
            if (symbol.getProperty(PropertyAlign).equals("c")) {
                position =  "center";
            }
            if (symbol.getProperty(PropertyAlign).equals("r")) {
                position =  "right";
            }
        }
        
        graphvizDotHolder.addAttribute("style", "text-align: center;float: " + position + ";");
        
        HtmlTag graphvizDotPicture = new HtmlTag("div");
        graphvizDotPicture.addAttribute("class", "graphvizDot_picture");
        graphvizDotPicture.add(new RawHtml(symbol.getProperty(PropertyPictureAsSvg)));
        graphvizDotHolder.add(graphvizDotPicture);
        
        if (symbol.hasProperty(PropertyTitle)) {
            graphvizDotHolder.add(newLine);
            HtmlTag caption = new HtmlTag("div");
            caption.add(symbol.getProperty(PropertyTitle));
            caption.addAttribute("class", "graphvizDot_caption");
            caption.addAttribute("style", "font-style: italic;");
            graphvizDotHolder.add(caption);
        }

        
        graphvizDotHeader.add(graphvizDotHolder);
        graphvizDot.add(graphvizDotHeader);

        // fix of html formatting
        HtmlTag clearFix = new HtmlTag("div");
        clearFix.addAttribute("style", "clear: both;");
        
        graphvizDot.add(clearFix);
        
        return graphvizDot.html();
    }
}
