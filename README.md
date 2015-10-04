diffxml
=======

Diff and Patch XML files

To build from source:

    $ ant jarDiffXML
   
Usage instructions: 

    $ ./diffxml.sh --help

    Usage: diffxml [OPTION]... XMLFILE1 XMLFILE2

    Find the differences between two XML files.

    --brief  -q  Report only if files differ, don't output the delta.
    --version  -V  Output version number of program.
    --help  -h  Output this help.

    This product includes software developed by the Indiana University Extreme! Lab (http://www.extreme.indiana.edu/).

To run:

    $ ./diffxml.sh test1a.xml test2a.xml
    ...
    
To diff and patch:

    $ ./diffxml.sh test1a.xml test2a.xml > /tmp/diff.xml
    $ ./patchxml.sh test1a.xml /tmp/diff.xml > /tmp/p.xml
    $ ./diffxml.sh /tmp/p.xml test2a.xml
    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <delta xmlns="http://www.adrianmouat.com/dul"/>
    
The empty XML document indicates there were no differences.

