##What is in here?

This folder contains the final report, which is written in LaTeX, using the biblatex package for references.

###Dependencies

The biblatex package is already installed in the MikTeX distribution for Windows.

On Ubuntu, it is located in the biblatex package (apt-get install biblatex).

On Mac or other systems... I have no idea <TODO>

###Compilation

On Windows, using MikTeX and the included TeXworks, set the compilation type to `pdfLaTeX+MakeIndex+BibTeX' and press the `play' button.

On Ubuntu, run:    
pdflatex report.tex    
bibtex report.aux
pdflatex report.tex

On Mac or other systems... I have no idea <TODO>

###House keeping

Please make sure that whatever weird files are outputted by your LaTeX/biblatex compilation process, are actually in the .gitignore.
Others should only need the report.tex and references.bib file.
