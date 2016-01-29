package com.github.sbellus.fitnesse.graphviz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.regex.Pattern;

import net.sourceforge.graphvizDot.FileFormat;
import net.sourceforge.graphvizDot.FileFormatOption;
import net.sourceforge.graphvizDot.SourceStringReader;

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
    private final String PropertyHigh = "High";

    public GraphvizDotSymbol(Properties properties) {
        super("startdot");
        wikiMatcher(new Matcher().startLine().ignoreWhitespace().string("!startdot"));
        wikiRule(this);
        htmlTranslation(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {

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
                current.putProperty(PropertyHigh, height);
            }
        }

        // convert it to picture
        // Symbol error = new Symbol(new Preformat(), "").add("Picture generation error:\n" + e.toString());
        // return new Maybe<Symbol>(error);

        //return new Maybe<Symbol>(current);
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
