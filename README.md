# Overview

The project integrates [Graphviz](http://www.graphviz.org/) into [Fitnesse](http://www.fitnesse.org/) wiki.

# Installation

1. Install [Graphviz](http://www.graphviz.org/Download.php) on machine where your Fitnesse is running.
2. Copy jar file from [this project Releases](https://github.com/sbellus/fitnesse-graphviz-plugin/releases) to plugins directory of your Fitnesse.

## Configuration

The configuration has to be stored in file ```fitnesse-graphviz-plugin.properties``` located in same directory as jar file. Usually in plugins directory of your Fitnesse.

### Path to dot executable
It is optional. The plugin searches for dot executable. You can set dot executable in configuration file.  
```
dotExecutable = c:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe
```

# Usage

After installation and Fitnesse restart you should be able to use command on wiki
```
!startdot
digraph G {
	size ="4,4";
	fitnesse [shape=box];
    fitnesse -> graphviz [style=bold,label="uses"];
}
!enddot
```

The command ```!startdot``` has following syntax ```!startdot ["title"] [align] [width] [height]```
* Title has to be surronded by "" 
* align can be one of
  * c - center
  * r - right
  * l - left
* width is width in pixels 
* height is height in pixels

# How it works

The plugin parses all lines after ```!startdot``` and before ```!enddot```.
It creates dot file in temporary directory with parsed lines.
It starts dot executable with arguments ```dot -Tsvg <generated dot file>```.
It inserts stdout from dot execution into Fitnesse page.   