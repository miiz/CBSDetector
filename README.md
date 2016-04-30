# Code Bad Smells Detector

This is a Java Code Bad Smells Detector. 
It detects five Martin Fowler's Code Bad Smells: Data Clumps, Message Chain, Middle Man, Speculative Generality, and Switch Statement from Java source code.
This detector bases on a static source code analysis technique called Meta-Programming. It uses an open source API "Recoder" to transform Java source code into abstract syntax trees, and then search Code Bad Smells in them.
This project is an Eclipse plug-in. The detection results can display in eclipse IDE or be exported as different formats, plain text, XML, Swing UI. 
It is a part of my previous PHD research in University of Hertfordshire.

Note: This project was tested in JDK 1.6 and Eclipse Helios, an issue has been found in the latest JDK 1.8.